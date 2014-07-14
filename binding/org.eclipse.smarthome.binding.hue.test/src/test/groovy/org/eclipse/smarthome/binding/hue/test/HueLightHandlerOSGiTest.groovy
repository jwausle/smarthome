/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.binding.hue.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*
import nl.q42.jue.MockedHttpClient
import nl.q42.jue.HttpClient.Result

import org.eclipse.smarthome.binding.hue.config.HueBridgeConfiguration
import org.eclipse.smarthome.binding.hue.config.HueLightConfiguration
import org.eclipse.smarthome.binding.hue.internal.HueThingTypeProvider
import org.eclipse.smarthome.binding.hue.internal.handler.HueBridgeHandler
import org.eclipse.smarthome.binding.hue.internal.handler.HueLightHandler
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.events.EventPublisher
import org.eclipse.smarthome.core.library.types.HSBType
import org.eclipse.smarthome.core.library.types.OnOffType
import org.eclipse.smarthome.core.library.types.PercentType
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ManagedThingProvider
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingProvider
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.core.thing.util.ThingHelper
import org.eclipse.smarthome.core.types.Command
import org.eclipse.smarthome.test.AsyncResultWrapper
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests for {@link HueLightHandler}.
 *
 * @author Oliver Libutzki - Initial contribution
 */
class HueLightHandlerOSGiTest extends OSGiTest {

    ManagedThingProvider managedThingProvider
    VolatileStorageService volatileStorageService = new VolatileStorageService()

    @Before
    void setUp() {
        registerService(volatileStorageService)
        managedThingProvider = getService(ThingProvider, ManagedThingProvider)
        assertThat managedThingProvider, is(notNullValue())
    }

    @After
    void tearDown() {
        managedThingProvider.things.each {managedThingProvider.removeThing(it.getUID())}
    }


    @Test
    void 'assert that HueLightHandler is registered and unregistered'() {
        Configuration bridgeConfiguration = new Configuration().with {
            put(HueBridgeConfiguration.IP_ADDRESS, "1.2.3.4")
            put(HueBridgeConfiguration.USER_NAME, "testUserName")
            put(HueBridgeConfiguration.BRIDGE_SERIAL_NUMBER, "testSerialNumber")
            it
        }
        ThingUID bridgeUID = new ThingUID(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), "testBridge")
        Bridge hueBridge = managedThingProvider.createThing(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), bridgeUID, null, bridgeConfiguration)
        assertThat hueBridge, is(notNullValue())


        HueLightHandler hueLightHandler = getService(ThingHandler, HueLightHandler)
        assertThat hueLightHandler, is(nullValue())
        Configuration lightConfiguration = new Configuration().with {
            put(HueLightConfiguration.LIGHT_ID, "1")
            it
        }
        Thing hueLight = managedThingProvider.createThing(HueThingTypeProvider.LIGHT_THING_TYPE.getUID(), new ThingUID(HueThingTypeProvider.LIGHT_THING_TYPE.getUID(), "Light1", bridgeUID.getId()), hueBridge.getUID(), lightConfiguration)
        assertThat hueLight, is(notNullValue())

        // wait for HueLightHandler to be registered
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueLightHandler, is(notNullValue())
        }, 10000)

        managedThingProvider.removeThing(hueLight.getUID())
        hueLightHandler = getService(ThingHandler, HueLightHandler)
        // wait for HueLightHandler to be unregistered
        waitForAssert({
            hueLightHandler = getService(ThingHandler, HueLightHandler)
            assertThat hueLightHandler, is(nullValue())
        }, 10000)

        managedThingProvider.removeThing(hueBridge.getUID())
    }



    @Test
    void 'assert command for color channel: on'() {
        def expectedBody =
                """
				{
					"on" : true
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, OnOffType.ON, expectedBody)
    }

    @Test
    void 'assert command for color temperature channel: on'() {
        def expectedBody =
                """
				{
					"on" : true
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, OnOffType.ON, expectedBody)
    }

    @Test
    void 'assert command for color channel: off'() {
        def expectedBody =
                """
				{
					"on" : false
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, OnOffType.OFF, expectedBody)
    }

    @Test
    void 'assert command for color temperature channel: off'() {
        def expectedBody =
                """
				{
					"on" : false
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, OnOffType.OFF, expectedBody)
    }

    @Test
    void 'assert command for color temperature channel: 0%'() {
        def expectedBody =
                """
				{
					"ct" : 154
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR_TEMPERATURE, new PercentType(0), expectedBody)
    }

    @Test
    void 'assert command for color temperature channel: 50%'() {
        def expectedBody =
                """
				{
					"ct" : 327
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR_TEMPERATURE, new PercentType(50), expectedBody)
    }

    @Test
    void 'assert command for color temperature channel: 100%'() {
        def expectedBody =
                """
				{
					"ct" : 500
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR_TEMPERATURE, new PercentType(100), expectedBody)
    }

    @Test
    void 'assert command for color channel: 0%'() {
        def expectedBody =
                """
				{
					"bri" : 0,
					"on" : false
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, new PercentType(0), expectedBody)
    }

    @Test
    void 'assert command for color channel: 50%'() {
        def expectedBody =
                """
				{
					"bri" : 127,
					"on" : true
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, new PercentType(50), expectedBody)
    }

    @Test
    void 'assert command for color channel: 100%'() {
        def expectedBody =
                """
				{
					"bri" : 254,
					"on" : true
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, new PercentType(100), expectedBody)
    }

    @Test
    void 'assert command for color channel: black'() {
        def expectedBody =
                """
				{
					"bri" : 0,
					"sat" : 0,
					"hue" : 0
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, HSBType.BLACK, expectedBody)
    }

    @Test
    void 'assert command for color channel: red'() {
        def expectedBody =
                """
				{
					"bri" : 254,
					"sat" : 254,
					"hue" : 0
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, HSBType.RED, expectedBody)
    }

    @Test
    void 'assert command for color channel: blue'() {
        def expectedBody =
                """
				{
					"bri" : 254,
					"sat" : 254,
					"hue" : 43680
				}
			"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, HSBType.BLUE, expectedBody)
    }

    @Test
    void 'assert command for color channel: white'() {
        def expectedBody =
                """
				{
				"bri" : 254,
				"sat" : 0,
				"hue" : 0
				}
				"""
        assertSendCommand(HueLightHandler.CHANNEL_ID_COLOR, HSBType.WHITE, expectedBody)
    }

    private void assertSendCommand(String channel, Command command, String expectedBody) {

        Configuration bridgeConfiguration = new Configuration().with {
            put(HueBridgeConfiguration.IP_ADDRESS, "1.2.3.4")
            put(HueBridgeConfiguration.USER_NAME, "testUserName")
            put(HueBridgeConfiguration.BRIDGE_SERIAL_NUMBER, "testSerialNumber")
            it
        }
        Bridge hueBridge = managedThingProvider.createThing(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), new ThingUID(HueThingTypeProvider.BRIDGE_THING_TYPE.getUID(), "testBridge"), null, bridgeConfiguration)
        HueLightHandler hueLightHandler = getService(ThingHandler, HueLightHandler)
        assertThat hueLightHandler, is(nullValue())
        Configuration lightConfiguration = new Configuration().with {
            put(HueLightConfiguration.LIGHT_ID, "1")
            it
        }
        Thing hueLight = managedThingProvider.createThing(HueThingTypeProvider.LIGHT_THING_TYPE.getUID(), new ThingUID(HueThingTypeProvider.LIGHT_THING_TYPE.getUID(), "Light1"), hueBridge.getUID(), lightConfiguration)
        try {
            assertThat hueLight, is(notNullValue())
            assertThat hueBridge, is(notNullValue())



            // wait for HueLightHandler to be registered
            waitForAssert({
                hueLightHandler = getService(ThingHandler, HueLightHandler)
                assertThat hueLightHandler, is(notNullValue())
            }, 10000)


            def AsyncResultWrapper<String> addressWrapper = new AsyncResultWrapper<String>()
            def AsyncResultWrapper<String> bodyWrapper = new AsyncResultWrapper<String>()
            MockedHttpClient mockedHttpClient =  [
                put: { String address, String body ->
                    addressWrapper.set(address)
                    bodyWrapper.set(body)
                    new Result("", 200)
                },
                get: { String address ->
                    if (address.endsWith("lights")) {
                        def body = """
							{
							  "1": {
							    "name": "Hue Light 1"
							  }
							}
						"""
                        new Result(body, 200)
                    } else if (address.endsWith("lights/1")) {
                        def body = """
							{
							  "state": {
							    "on": true,
							    "bri": 200,
							    "hue": 50000,
							    "sat": 0,
							    "xy": [
							      0,
							      0
							    ],
							    "ct": 0,
							    "alert": "none",
							    "effect": "none",
							    "colormode": "hs",
							    "reachable": true
							  },
							  "type": "Extended color light",
							  "name": "Hue Light 1",
							  "modelid": "LCT001",
							  "swversion": "65003148",
							  "pointsymbol": {
							    "1": "none",
							    "2": "none",
							    "3": "none",
							    "4": "none",
							    "5": "none",
							    "6": "none",
							    "7": "none",
							    "8": "none"
							  }
							}
						"""
                        new Result(body, 200)
                    }
                }
            ] as MockedHttpClient

            installHttpClientMock(hueLightHandler.getHueBridgeHandler(), mockedHttpClient)


            // Create items and channel bindings
            ThingHelper thingHelper = new ThingHelper(bundleContext)

            thingHelper.createAndBindItems(hueLight)
            def item = hueLight.getUID().toString().replace(":", "_") + "_" + channel


            EventPublisher eventPublisher = getService(EventPublisher)
            assertThat eventPublisher, is(notNullValue())

            eventPublisher.postCommand(item, command)

            waitForAssert({assertTrue addressWrapper.isSet}, 10000)
            waitForAssert({assertTrue bodyWrapper.isSet}, 10000)

            assertThat addressWrapper.wrappedObject, is("http://1.2.3.4/api/testUserName/lights/1/state")
            assertJson(expectedBody, bodyWrapper.wrappedObject)
        } finally {
            managedThingProvider.removeThing(hueLight.getUID())
            managedThingProvider.removeThing(hueBridge.getUID())
        }
    }

    private void assertJson(String expected, String actual) {
        def jsonSlurper = Class.forName("groovy.json.JsonSlurper").newInstance()
        def actualResult = jsonSlurper.parseText(actual)
        def expectedResult = jsonSlurper.parseText(expected)

        assertThat actualResult, is(expectedResult)
    }

    private void installHttpClientMock(HueBridgeHandler hueBridgeHandler, MockedHttpClient mockedHttpClient) {
        // mock HttpClient
        def hueBridgeField = hueBridgeHandler.getClass().getDeclaredField("bridge")
        hueBridgeField.accessible = true
        def hueBridgeValue = hueBridgeField.get(hueBridgeHandler)
        def httpClientField = hueBridgeValue.getClass().getDeclaredField("http")
        httpClientField.accessible = true
        httpClientField.set(hueBridgeValue, mockedHttpClient)
        hueBridgeHandler.initialize()
    }
}
