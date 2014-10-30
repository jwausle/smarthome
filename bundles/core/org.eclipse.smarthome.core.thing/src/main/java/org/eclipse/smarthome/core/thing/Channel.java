/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing;

import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.items.Item;

/**
 * {@link Channel} is a part of a {@link Thing} that represents a functionality
 * of it. Therefore {@link Item}s can be bound a to a channel. The channel only
 * accepts a specific item type which is specified by
 * {@link Channel#getAcceptedItemType()} methods.
 * 
 * @author Dennis Nobel - Initial contribution and API
 * @author Alex Tugarev - Extended about default tags
 */
public class Channel {

    private String acceptedItemType;

    private ChannelUID uid;

    private Configuration configuration;

    private Set<String> defaultTags;    

    public Channel(ChannelUID uid, String acceptedItemType) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.configuration = configuration;
    }

    public Channel(ChannelUID uid, String acceptedItemType, Set<String> defaultTags) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.defaultTags = defaultTags;
    }

    public Channel(ChannelUID uid, String acceptedItemType, Configuration configuration,
            Set<String> defaultTags) {
        this.uid = uid;
        this.acceptedItemType = acceptedItemType;
        this.configuration = configuration;
        this.defaultTags = defaultTags;
    }

    /**
     * Returns the accepted item type.
     * 
     * @return accepted item type
     */
    public String getAcceptedItemType() {
        return this.acceptedItemType;
    }

    /**
     * Returns the unique id of the channel.
     * 
     * @return unique id of the channel
     */
    public ChannelUID getUID() {
        return this.uid;
    }

    /**
     * Returns the channel configuration
     * 
     * @return channel configuration or null
     */
    public Configuration getConfiguration() {
        return configuration;
    }
    
    /**
     * Returns default tags of this channel.
     * 
     * @return default tags of this channel.
     */
    public Set<String> getDefaultTags() {
        return defaultTags;
    }
}
