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
 * An {@link EventFactory} is responsible for creating {@link Event} instances of specific event types. The Eclipse
 * SmartHome framework uses Event Factories in order to create new Events ({@link #createEvent(String, String, String)})
 * based on the event type, the topic and the payload if an event type is supported ({@link #getSupportedEventTypes()}).
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public interface EventFactory {

    /**
     * Create a new event instance of a specific event type.
     * 
     * @param eventTypeName the event type
     * @param topic the topic
     * @param payload the payload
     * 
     * @return the created event
     * @throws Exception if the creation of the event failed
     */
    Event createEvent(String eventType, String topic, String payload) throws Exception;

    /**
     * Returns a list of all supported event types of this factory.
     * 
     * @return the supported event types
     */
    Set<String> getSupportedEventTypes();

}
