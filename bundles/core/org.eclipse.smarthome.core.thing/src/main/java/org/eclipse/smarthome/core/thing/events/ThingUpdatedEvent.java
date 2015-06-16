/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.events;

import org.eclipse.smarthome.core.thing.Thing;

/**
 * A {@link ThingUpdatedEvent} notifies subscribers that a thing has been updated.
 *
 * @author Stefan Bußweiler - Initial contribution
 */
public class ThingUpdatedEvent extends AbstractThingRegistryEvent {

    /**
     * The thing updated event type.
     */
    public final static String TYPE = ThingUpdatedEvent.class.getSimpleName();

    private final Thing oldThing;

    /**
     * Constructs a new thing updated event object.
     *
     * @param topic the topic
     * @param payload the payload
     * @param thing the thing
     * @param oldThing the old thing
     */
    public ThingUpdatedEvent(String topic, String payload, Thing thing, Thing oldThing) {
        super(topic, payload, null, thing);
        this.oldThing = oldThing;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Gets the old thing.
     * 
     * @return the oldThing
     */
    public Thing getOldThing() {
        return oldThing;
    }

    @Override
    public String toString() {
        return "Thing '" + getThing().getUID() + "' has been updated.";
    }

}
