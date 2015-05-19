/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

/**
 * Abstract implementation of the {@link Event} interface.
 * 
 * @author Stefan Bußweiler - Initial contribution
 */
public abstract class AbstractEvent implements Event {

    private final String topic;

    private final String payload;

    /**
     * Creates a new event.
     * 
     * @param topic the topic
     * @param payload the payload
     */
    public AbstractEvent(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public String getPayload() {
        return payload;
    }

}