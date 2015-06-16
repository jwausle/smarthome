/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.internal.events;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.events.Event;
import org.eclipse.smarthome.core.events.EventFactory;
import org.eclipse.smarthome.core.events.EventFilter;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.events.EventSubscriber;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

/**
 * The {@link OSGiEventManager} provides an OSGi based default implementation of the Eclipse SmartHome event bus.
 * 
 * The OSGiEventHandler tracks {@link EventSubscriber}s and {@link EventFactory}s, receives OSGi events (by
 * implementing the OSGi {@link EventHandler} interface) and dispatches the received OSGi events as ESH {@link Event}s
 * to the {@link EventSubscriber}s if the provided filter applies.
 * 
 * The {@link OSGiEventManager} also serves as {@link EventPublisher} by implementing the EventPublisher interface.
 * Events are send in an asynchronous way via OSGi Event Admin mechanism.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public class OSGiEventManager implements EventHandler, EventPublisher {

    private Logger logger = LoggerFactory.getLogger(OSGiEventManager.class);

    private EventAdmin osgiEventAdmin;

    private final Map<String, EventFactory> typedEventFactoryCache = new ConcurrentHashMap<String, EventFactory>();

    private final SetMultimap<String, EventSubscriber> typedEventSubscriberCache = Multimaps
            .synchronizedSetMultimap(HashMultimap.<String, EventSubscriber> create());

    protected void setEventAdmin(EventAdmin eventAdmin) {
        this.osgiEventAdmin = eventAdmin;
    }

    protected void unsetEventAdmin(EventAdmin eventAdmin) {
        this.osgiEventAdmin = null;
    }

    protected void addEventFactory(EventFactory eventFactory) {
        Set<String> supportedEventTypes = eventFactory.getSupportedEventTypes();

        for (String supportedEventType : supportedEventTypes) {
            synchronized (this) {
                if (!typedEventFactoryCache.containsKey(supportedEventType)) {
                    typedEventFactoryCache.put(supportedEventType, eventFactory);
                }
            }
        }
    }

    protected void removeEventFactory(EventFactory eventFactory) {
        Set<String> supportedEventTypes = eventFactory.getSupportedEventTypes();

        for (String supportedEventType : supportedEventTypes) {
            typedEventFactoryCache.remove(supportedEventType);
        }
    }

    protected void addEventSubscriber(EventSubscriber eventSubscriber) {
        Set<String> subscribedEventTypes = eventSubscriber.getSubscribedEventTypes();

        for (String subscribedEventType : subscribedEventTypes) {
            synchronized (this) {
                if (!typedEventSubscriberCache.containsEntry(subscribedEventType, eventSubscriber)) {
                    typedEventSubscriberCache.put(subscribedEventType, eventSubscriber);
                }
            }
        }
    }

    protected void removeEventSubscriber(EventSubscriber eventSubscriber) {
        Set<String> subscribedEventTypes = eventSubscriber.getSubscribedEventTypes();

        for (String subscribedEventType : subscribedEventTypes) {
            typedEventSubscriberCache.remove(subscribedEventType, eventSubscriber);
        }
    }

    @Override
    public void handleEvent(org.osgi.service.event.Event osgiEvent) {
        Object typeObj = osgiEvent.getProperty("type");
        Object payloadObj = osgiEvent.getProperty("payload");
        Object topicObj = osgiEvent.getProperty("topic");
        Object sourceObj = osgiEvent.getProperty("source");

        if (typeObj instanceof String && payloadObj instanceof String && topicObj instanceof String) {
            String typeStr = (String) typeObj;
            String payloadStr = (String) payloadObj;
            String topicStr = (String) topicObj;
            String sourceStr = (sourceObj instanceof String) ? (String) sourceObj : null;
            if (!typeStr.isEmpty() && !payloadStr.isEmpty() && !topicStr.isEmpty()) {
                handleEvent(typeStr, payloadStr, topicStr, sourceStr);
            }
        } else {
            logger.error("The handled OSGi event is invalid. Received event: {}", osgiEvent);
        }
    }

    private void handleEvent(final String type, final String payload, final String topic, final String source) {
        EventFactory eventFactory = typedEventFactoryCache.get(type);
        Set<EventSubscriber> eventSubscribers = getEventSubscribers(type);

        if (eventFactory != null && !eventSubscribers.isEmpty()) {
            Event eshEvent = createESHEvent(eventFactory, type, payload, topic, source);
            if (eshEvent != null) {
                dispatchESHEvent(eventSubscribers, eshEvent);
            }
        }
    }

    private Event createESHEvent(final EventFactory eventFactory, final String type, final String payload,
            final String topic, final String source) {
        Event eshEvent = null;
        try {
            eshEvent = eventFactory.createEvent(type, topic, payload, source);
        } catch (Exception e) {
            logger.error("Creation of ESH-Event failed, "
                    + "because one of the registered event factories has thrown an exception.", e);
        }
        return eshEvent;
    }

    private void dispatchESHEvent(final Set<EventSubscriber> eventSubscribers, final Event event) {
        for (EventSubscriber eventSubscriber : eventSubscribers) {
            try {
                EventFilter filter = eventSubscriber.getEventFilter();
                if (filter == null) {
                    eventSubscriber.receive(event);
                } else if (filter.apply(event)) {
                    eventSubscriber.receive(event);
                }
            } catch (Exception e) {
                logger.error("Dispatching/filtering of ESH-Event failed, "
                        + "because one of the registered subscriber/filter has thrown an exception.", e);
            }
        }
    }

    private Set<EventSubscriber> getEventSubscribers(String eventType) {
        Set<EventSubscriber> eventTypeSubscribers = typedEventSubscriberCache.get(eventType);
        Set<EventSubscriber> allEventTypeSubscribers = typedEventSubscriberCache.get(EventSubscriber.ALL_EVENT_TYPES);

        Set<EventSubscriber> subscribers = new HashSet<EventSubscriber>();
        if (eventTypeSubscribers != null)
            subscribers.addAll(eventTypeSubscribers);
        if (allEventTypeSubscribers != null)
            subscribers.addAll(allEventTypeSubscribers);
        return subscribers;
    }

    @Override
    public void post(final Event event) throws IllegalArgumentException, IllegalStateException {
        EventAdmin eventAdmin = this.osgiEventAdmin;
        assertValidArgument(event);
        assertValidState(eventAdmin);
        postAsOSGiEvent(eventAdmin, event);
    }

    private void postAsOSGiEvent(final EventAdmin eventAdmin, final Event event) throws IllegalStateException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    Dictionary<String, Object> properties = new Hashtable<String, Object>(3);
                    properties.put("type", event.getType());
                    properties.put("payload", event.getPayload());
                    properties.put("topic", event.getTopic());
                    if(event.getSource() != null) {
                        properties.put("source", event.getSource());
                    }
                    eventAdmin.postEvent(new org.osgi.service.event.Event("smarthome", properties));
                    return null;
                }
            });
        } catch (PrivilegedActionException pae) {
            throw new IllegalStateException("Cannot post the event via the event bus.", pae.getException());
        }
    }

    private void assertValidArgument(Event event) throws IllegalArgumentException {
        String errorMsg = "The %s of the 'event' argument must not be null or empty.";
        if (event == null) {
            throw new IllegalArgumentException("Argument 'event' must not be null.");
        }
        if (event.getType() == null || event.getType().isEmpty()) {
            throw new IllegalArgumentException(String.format(errorMsg, "type"));
        }
        if (event.getPayload() == null || event.getPayload().isEmpty()) {
            throw new IllegalArgumentException(String.format(errorMsg, "payload"));
        }
        if (event.getTopic() == null || event.getTopic().isEmpty()) {
            throw new IllegalArgumentException(String.format(errorMsg, "topic"));
        }
    }

    private void assertValidState(EventAdmin eventAdmin) throws IllegalStateException {
        if (eventAdmin == null) {
            throw new IllegalStateException("The event bus module is not available!");
        }
    }

}