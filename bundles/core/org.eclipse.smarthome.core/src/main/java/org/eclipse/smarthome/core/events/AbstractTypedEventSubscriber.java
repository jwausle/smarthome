/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.events;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AbstractTypedEventSubscriber} is an abstract implementation of the {@link EventSubscriber} event listener
 * interface.
 * 
 * This class helps to subscribe to a specific event type and provides the {@link TypedEventFilter}. To receive an event - casted to
 * the specific event typ - the {@link #receiveTypedEvent(T)} method must be implemented.
 * 
 * @author Stefan Bußweiler - Initial contribution
 *
 * @param <T> The specific event type this class subscribes to.
 */
public abstract class AbstractTypedEventSubscriber<T extends Event> implements EventSubscriber {

    private final String eventType;

    public AbstractTypedEventSubscriber(String eventType) {
        this.eventType = eventType;
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return ImmutableSet.of(eventType);
    }

    @Override
    public EventFilter getEventFilter() {
        return new TypedEventFilter(eventType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void receive(Event event) {
        receiveTypedEvent((T) event);
    }

    protected abstract void receiveTypedEvent(T event);

}