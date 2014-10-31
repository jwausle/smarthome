/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.thing.binding

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.type.BridgeType
import org.eclipse.smarthome.core.thing.type.ChannelDefinition
import org.eclipse.smarthome.core.thing.type.ChannelType
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID
import org.eclipse.smarthome.core.thing.type.ThingType
import org.junit.Test

class ThingFactoryTest {

	@Test
	void 'create simple Thing'() {

		def thingType = new ThingType("bindingId", "thingTypeId", "label")
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing.getUID().toString(), is(equalTo("bindingId:thingTypeId:thingId"))
		assertThat thing.getThingTypeUID().toString(), is(equalTo("bindingId:thingTypeId"))
		assertThat thing.getConfiguration(), is(not(null))
	}

	@Test
	void 'create simple Bridge'() {

		def thingType = new BridgeType("bindingId", "thingTypeId", "label")
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing, is(instanceOf(Bridge))
	}

	@Test
	void 'create Thing with Bridge'() {

		def bridgeUID = new ThingUID("binding:bridge:1")

		def thingType = new ThingType("bindingId", "thingTypeId", "label")
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration, bridgeUID)

		assertThat thing.getBridgeUID(), is(equalTo(bridgeUID))
	}

	@Test
	void 'create Thing with Channels'() {

		ChannelType channelType1 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId1"), "Color", "label", "description", new HashSet([ "tag1", "tag2" ]), null)
		ChannelType channelType2 = new ChannelType(new ChannelTypeUID("bindingId:channelTypeId2"), "Dimmer", "label", "description", new HashSet([ "tag3" ]), null)

		ChannelDefinition channelDef1 = new ChannelDefinition("ch1", channelType1)
		ChannelDefinition channelDef2 = new ChannelDefinition("ch2", channelType2)
		
		def thingType = new ThingType(new ThingTypeUID("bindingId:thingType"), [], "label", null, [channelDef1, channelDef2], null)
		def configuration = new Configuration();

		def thing = ThingFactory.createThing(thingType, new ThingUID(thingType.getUID(), "thingId"), configuration)

		assertThat thing.getChannels().size, is(2)
		assertThat thing.getChannels().get(0).getUID().toString(), is(equalTo("bindingId:thingType:thingId:ch1"))
		assertThat thing.getChannels().get(0).getAcceptedItemType(), is(equalTo("Color"))
        assertThat thing.getChannels().get(0).getDefaultTags().contains("tag1"), is(true)
        assertThat thing.getChannels().get(0).getDefaultTags().contains("tag2"), is(true)
        assertThat thing.getChannels().get(0).getDefaultTags().contains("tag3"), is(false)
        assertThat thing.getChannels().get(1).getDefaultTags().contains("tag1"), is(false)
        assertThat thing.getChannels().get(1).getDefaultTags().contains("tag2"), is(false)
        assertThat thing.getChannels().get(1).getDefaultTags().contains("tag3"), is(true)
	}
}
