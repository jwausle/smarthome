package org.eclipse.smarthome.core.thing;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.GenericItem;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.ItemFactory;
import org.eclipse.smarthome.core.items.ManagedItemProvider;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.core.thing.link.ItemChannelLink;
import org.eclipse.smarthome.core.thing.link.ItemThingLink;
import org.eclipse.smarthome.core.thing.link.ManagedItemChannelLinkProvider;
import org.eclipse.smarthome.core.thing.link.ManagedItemThingLinkProvider;
import org.eclipse.smarthome.core.thing.type.ChannelGroupDefinition;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupManager {

    private static final String TAG_CHANNEL_GROUP = "channel_group";
    private static final String TAG_THING = "thing";
    
    private List<ItemFactory> itemFactories = new CopyOnWriteArrayList<>();;
    private final Logger logger = LoggerFactory.getLogger(SetupManager.class);
    private ManagedItemChannelLinkProvider managedItemChannelLinkProvider;
    private ManagedItemProvider managedItemProvider;
    private ManagedItemThingLinkProvider managedItemThingLinkProvider;
    private ManagedThingProvider managedThingProvider;
    private List<ThingHandlerFactory> thingHandlerFactories = new CopyOnWriteArrayList<>();
    private ThingTypeRegistry thingTypeRegistry;

    public void addThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID) {
        addThing(thingUID, configuration, bridgeUID, true);
    }

    public void addThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID, boolean enableChannels) {

        ThingTypeUID thingTypeUID = thingUID.getThingTypeUID();
        Thing thing = createThing(thingUID, configuration, bridgeUID, thingTypeUID);

        if (thing == null) {
            logger.warn("Cannot create thing. No binding found that supports creating a thing" + " of type {}.",
                    thingTypeUID);
            return;
        }

        String itemName = toItemName(thing.getUID());
        GroupItem groupItem = new GroupItem(itemName);
        groupItem.addTag(TAG_THING);

        managedThingProvider.add(thing);
        managedItemProvider.add(groupItem);
        managedItemThingLinkProvider.add(new ItemThingLink(itemName, thing.getUID()));

        ThingType thingType = thingTypeRegistry.getThingType(thingTypeUID);
        if (thingType != null) {
            List<ChannelGroupDefinition> channelGroupDefinitions = thingType.getChannelGroupDefinitions();
            for (ChannelGroupDefinition channelGroupDefinition : channelGroupDefinitions) {
                GroupItem channelGroupItem = new GroupItem(getChannelGroupItemName(itemName,
                        channelGroupDefinition.getId()));
                channelGroupItem.addTag(TAG_CHANNEL_GROUP);
                managedItemProvider.add(channelGroupItem);
            }
        }

        if (enableChannels) {
            List<Channel> channels = thing.getChannels();
            for (Channel channel : channels) {
                ChannelType channelType = this.thingTypeRegistry.getChannelType(channel.getUID());
                if (channelType != null && !channelType.isAdvanced()) {
                    enableChannel(channel.getUID());
                }
            }
        }
    }

    public void disableChannel(ChannelUID channelUID) {
        Collection<ItemChannelLink> itemChannelLinks = this.managedItemChannelLinkProvider.getAll();
        for (ItemChannelLink itemChannelLink : itemChannelLinks) {
            if (itemChannelLink.getUID().equals(channelUID)) {
                String itemName = itemChannelLink.getItemName();
                managedItemProvider.remove(itemName);
                managedItemChannelLinkProvider.remove(itemChannelLink.getID());
            }
        }
    }

    public void enableChannel(ChannelUID channelUID) {
        ChannelType channelType = thingTypeRegistry.getChannelType(channelUID);
        if (channelType != null) {
            String itemType = channelType.getItemType();
            ItemFactory itemFactory = getItemFactoryForItemType(itemType);
            if (itemFactory != null) {
                String itemName = toItemName(channelUID);
                GenericItem item = itemFactory.createItem(itemType, itemName);
                if (item != null) {
                    String thingGroupItemName = getThingGroupItemName(channelUID);
                    if (thingGroupItemName != null) {
                        if (!channelUID.isInGroup()) {
                            item.addGroupName(thingGroupItemName);
                        } else {
                            item.addGroupName(getChannelGroupItemName(thingGroupItemName, channelUID.getGroupId()));
                        }
                    }
                    item.addTags(channelType.getTags());
                    item.setCategory(channelType.getCategory());
                    this.managedItemProvider.add(item);
                    this.managedItemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));
                }
            }
        }

    }

    public void removeThing(ThingUID thingUID) {
        String itemName = toItemName(thingUID);
        managedThingProvider.remove(thingUID);
        managedItemProvider.remove(itemName, true);
        managedItemThingLinkProvider.remove(ItemThingLink.getIDFor(itemName, thingUID));
        managedItemChannelLinkProvider.removeLinksForThing(thingUID);
    }

    protected void addItemFactory(ItemFactory itemFactory) {
        this.itemFactories.add(itemFactory);
    }

    protected void addThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.add(thingHandlerFactory);
    }

    protected void removeItemFactory(ItemFactory itemFactory) {
        this.itemFactories.remove(itemFactory);
    }

    protected void removeThingHandlerFactory(ThingHandlerFactory thingHandlerFactory) {
        this.thingHandlerFactories.remove(thingHandlerFactory);
    }

    protected void setManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = managedItemChannelLinkProvider;
    }

    protected void setManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = managedItemProvider;
    }

    protected void setManagedItemThingLinkProvider(ManagedItemThingLinkProvider managedItemThingLinkProvider) {
        this.managedItemThingLinkProvider = managedItemThingLinkProvider;
    }

    protected void setManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = managedThingProvider;
    }

    protected void setThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = thingTypeRegistry;
    }

    protected void unsetManagedItemChannelLinkProvider(ManagedItemChannelLinkProvider managedItemChannelLinkProvider) {
        this.managedItemChannelLinkProvider = null;
    }

    protected void unsetManagedItemProvider(ManagedItemProvider managedItemProvider) {
        this.managedItemProvider = null;
    }

    protected void unsetManagedItemThingLinkProvider(ManagedItemThingLinkProvider managedItemThingLinkProvider) {
        this.managedItemThingLinkProvider = null;
    }

    protected void unsetManagedThingProvider(ManagedThingProvider managedThingProvider) {
        this.managedThingProvider = null;
    }

    protected void unsetThingTypeRegistry(ThingTypeRegistry thingTypeRegistry) {
        this.thingTypeRegistry = null;
    }

    private Thing createThing(ThingUID thingUID, Configuration configuration, ThingUID bridgeUID,
            ThingTypeUID thingTypeUID) {
        for (ThingHandlerFactory thingHandlerFactory : this.thingHandlerFactories) {
            if (thingHandlerFactory.supportsThingType(thingTypeUID)) {
                Thing thing = thingHandlerFactory.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
                return thing;
            }
        }
        return null;
    }

    private String getChannelGroupItemName(String itemName, String channelGroupId) {
        return itemName + "_" + channelGroupId;
    }

    private ItemFactory getItemFactoryForItemType(String itemType) {
        for (ItemFactory itemFactory : this.itemFactories) {
            String[] supportedItemTypes = itemFactory.getSupportedItemTypes();
            for (int i = 0; i < supportedItemTypes.length; i++) {
                String supportedItemType = supportedItemTypes[i];
                if (supportedItemType.equals(itemType)) {
                    return itemFactory;
                }
            }
        }
        return null;
    }

    private String getThingGroupItemName(ChannelUID channelUID) {
        Collection<ItemThingLink> links = this.managedItemThingLinkProvider.getAll();
        for (ItemThingLink link : links) {
            if (link.getUID().equals(channelUID.getThingUID())) {
                return link.getItemName();
            }
        }
        return null;
    }

    private String toItemName(UID uid) {
        String itemName = uid.getAsString().replaceAll("[^a-zA-Z0-9_]", "_");
        return itemName;
    }
}
