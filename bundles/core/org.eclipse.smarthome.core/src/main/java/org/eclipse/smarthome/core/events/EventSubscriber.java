/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

import java.util.Set;

/**
 * The {@link EventSubscriber} defines the callback interface for receiving events from
 * the Eclipse SmartHome event bus.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public interface EventSubscriber {

    /**
     * The constant {@link #ALL_EVENT_TYPES} must be returned by the {@link #getSubscribedEventTypes()} method, if the
     * event subscriber should subscribe to all event types.
     */
    public static String ALL_EVENT_TYPES = "ALL";

    /**
     * Gets the event types to which the event subscriber is subscribed to.
     * 
     * @return subscribed event types
     */
    Set<String> getSubscribedEventTypes();

    /**
     * Gets an {@link EventFilter} in order to receive specific events if the filter applies. If there is no
     * filter all subscribed event types are received.
     * 
     * @return the event filter, or null
     */
    EventFilter getEventFilter();

    /**
     * Callback method for receiving {@link Event}s from the Eclipse SmartHome event bus. This method is called for
     * every event where the event subscriber is subscribed to and the event filter applies.
     * 
     * @param event the received event
     */
    void receive(Event event);
}
