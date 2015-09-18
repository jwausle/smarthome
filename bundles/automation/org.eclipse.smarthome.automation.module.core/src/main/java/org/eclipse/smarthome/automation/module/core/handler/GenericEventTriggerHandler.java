/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.core.handler;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.handler.BaseTriggerHandler;
import org.eclipse.smarthome.automation.handler.RuleEngineCallback;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * This is an ModuleHandler implementation for Triggers which trigger the rule
 * if an event occurs. The eventType, eventSource and topic can be set with the
 * configuration. It is an generic approach which makes it easier to specify
 * more concrete event based triggers with the composite module approach of the
 * automation component. Each GenericTriggerHandler instance registers as
 * EventSubscriber, so the dispose method must be called for unregistering the
 * service.
 * 
 * @author Benedikt Niehues
 *
 */
public class GenericEventTriggerHandler extends BaseTriggerHandler implements EventSubscriber {

    private final Logger logger = LoggerFactory.getLogger(GenericEventTriggerHandler.class);

    private RuleEngineCallback callback;
    private Map<String, Object> config;
    private String source;
    private String topic;
    private Set<String> types;
    private Trigger trigger;
    private BundleContext bundleContext;

    public static final String MODULE_TYPE_ID = "GenericEventTrigger";

    private static final String CFG_EVENT_TOPIC = "eventTopic";
    private static final String CFG_EVENT_SOURCE = "eventSource";
    private static final String CFG_EVENT_TYPES = "eventTypes";

    private ServiceRegistration<EventSubscriber> eventSubscriberRegistration;

    public GenericEventTriggerHandler(Trigger module, List<ModuleType> moduleTypes, BundleContext bundleContext) {
        super(module, moduleTypes);
        this.trigger = (Trigger) module;
        this.config = getResolvedConfiguration(null);
        this.source = (String) config.get(CFG_EVENT_SOURCE);
        this.topic = (String) config.get(CFG_EVENT_TOPIC);
        this.types = ImmutableSet.copyOf(((String) config.get(CFG_EVENT_TYPES)).split(","));
        this.bundleContext = bundleContext;
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("event.topics", topic);
        eventSubscriberRegistration = this.bundleContext.registerService(EventSubscriber.class, this, properties);
        logger.debug("Registered EventSubscriber: Topic: {} Type: {} Source:{}", topic, types, source);
    }

    @Override
    public void setRuleEngineCallback(RuleEngineCallback ruleCallback) {
        this.callback = ruleCallback;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return types;
    }

    @Override
    public EventFilter getEventFilter() {
        return null;
    }

    @Override
    public void receive(Event event) {
        logger.debug("Received Event: Source:" + event.getSource() + " Topic:" + event.getTopic() + " Type:"
                + event.getType() + " Payload:" + event.getPayload());
        if (!event.getTopic().contains(source)) {
            return;
        }
        Map<String, Object> values = Maps.newHashMap();
        values.put("source", source);
        values.put("payload", event.getPayload());
        values.put("type", event.getType());
        values.put("topic", event.getTopic());
        callback.triggered(this.trigger, getResolvedOutputs(config, null, values));
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @param topic
     *            the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * do the cleanup: unregistering eventSubscriber...
     */
    public void dispose() {
        if (eventSubscriberRegistration != null) {
            eventSubscriberRegistration.unregister();
            eventSubscriberRegistration = null;
        }
    }

    @Override
    protected Map<String, Object> getTriggerValues() {
        // TODO Auto-generated method stub
        return null;
    }

}