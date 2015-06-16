/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.events;

import org.eclipse.smarthome.core.thing.dto.ThingDTO;

/**
 * A {@link ThingAddedEvent} notifies subscribers that a thing has been added.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class ThingAddedEvent extends AbstractThingRegistryEvent {

    /**
     * The thing added event type.
     */
    public final static String TYPE = ThingAddedEvent.class.getSimpleName();

    /**
     * Constructs a new thing added event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param thing the thing
     */
    protected ThingAddedEvent(String topic, String payload, ThingDTO thing) {
        super(topic, payload, null, thing);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "Thing '" + getThing().UID + "' has been added.";
    }

}
