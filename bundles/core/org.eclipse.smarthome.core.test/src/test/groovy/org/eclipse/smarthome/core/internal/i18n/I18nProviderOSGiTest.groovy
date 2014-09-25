package org.eclipse.smarthome.core.internal.i18n;

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.core.i18n.I18nProvider
import org.eclipse.smarthome.test.OSGiTest
import org.junit.Before
import org.junit.Test
import org.osgi.framework.Bundle


/**
 * The {@link I18nProviderOSGiTest} checks if the {@link I18nProvider} service is working
 * properly. This test retrieves the {@link I18nProvider} service from the <i>OSGi</i>
 * service registry and calls any public methods on it in different scenarios. 
 * 
 * @author Michael Grammling - Initial Contribution
 */
class I18nProviderOSGiTest extends OSGiTest {

    def RESOURCE = "resourceName"

    def KEY_HELLO = "HELLO"
    def KEY_BYE = "BYE"

    def HELLO_WORLD_DEFAULT = "Hallo Welt!"
    def HELLO_WORLD_EN = "Hello World!"
    def HELLO_WORLD_FR = "Bonjour le monde!"
    def BYE_DEFAULT = "Tschuess!"

    I18nProvider i18nProvider


    @Before
    void setUp() {
        i18nProvider = getService(I18nProvider)
        assertThat i18nProvider, is(notNullValue())
    }

    @Test
    void 'assert that getText without a resource or bundle is working properly'() {
        def text

        text = i18nProvider.getText((String) null, null, null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(""))

        text = i18nProvider.getText((String) null, null, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))
    }

    @Test
    void 'assert that getText via resource is working properly'() {
        def text

        text = i18nProvider.getText(RESOURCE, null, null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(""))

        text = i18nProvider.getText(RESOURCE, null, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(RESOURCE, "UNKNOWN_HELLO", "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(RESOURCE, "UNKNOWN_HELLO", null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("UNKNOWN_HELLO"))

        text = i18nProvider.getText(RESOURCE, KEY_HELLO, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_DEFAULT))

        text = i18nProvider.getText(RESOURCE, KEY_HELLO, "default", Locale.ITALIAN)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_DEFAULT))

        text = i18nProvider.getText(RESOURCE, KEY_HELLO, "default", Locale.FRENCH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_FR))

        text = i18nProvider.getText(RESOURCE, KEY_HELLO, "default", Locale.ENGLISH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_EN))

        text = i18nProvider.getText(RESOURCE, KEY_BYE, "default", Locale.ENGLISH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))
    }

    @Test
    void 'assert that getText via bundle is working properly'() {
        def text

        Bundle bundle = getBundleContext().bundle

        text = i18nProvider.getText(bundle, null, null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(""))

        text = i18nProvider.getText(bundle, null, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, "UNKNOWN_HELLO", "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, "UNKNOWN_HELLO", null, null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("UNKNOWN_HELLO"))

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_DEFAULT))

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", Locale.FRENCH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_FR))

        text = i18nProvider.getText(bundle, KEY_HELLO, "default", Locale.ENGLISH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(HELLO_WORLD_EN))

        text = i18nProvider.getText(bundle, KEY_BYE, "default", null)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo("default"))

        text = i18nProvider.getText(bundle, KEY_BYE, "default", Locale.ENGLISH)
        assertThat text, is(notNullValue())
        assertThat text, is(equalTo(BYE_DEFAULT))
    }

}
