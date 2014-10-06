package org.eclipse.smarthome.config.core.i18n;

import java.net.URI;
import java.util.Locale;

import org.eclipse.smarthome.core.i18n.I18nProvider;
import org.eclipse.smarthome.core.i18n.I18nUtil;
import org.osgi.framework.Bundle;

/**
 * The {@link ConfigDescriptionI18nUtil} uses the {@link I18nProvider} to
 * resolve the localized texts. It automatically infers the key if the default
 * text is not a constant.
 * 
 * @author Dennis Nobel - Initial contribution
 */
public class ConfigDescriptionI18nUtil {

    private I18nProvider i18nProvider;
    
    public ConfigDescriptionI18nUtil(I18nProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public String getParameterDescription(Bundle bundle, URI configDescriptionURI, String parameterName,
            String defaultDescription, Locale locale) {
        String key = I18nUtil.isConstant(defaultDescription) ? I18nUtil.stripConstant(defaultDescription) : inferKey(
                configDescriptionURI, parameterName, "description");
        return i18nProvider.getText(bundle, key, defaultDescription, locale);
    }

    public String getParameterLabel(Bundle bundle, URI configDescriptionURI, String parameterName, String defaultLabel,
            Locale locale) {
        String key = I18nUtil.isConstant(defaultLabel) ? I18nUtil.stripConstant(defaultLabel) : inferKey(
                configDescriptionURI, parameterName, "label");
        return i18nProvider.getText(bundle, key, defaultLabel, locale);
    }

    private String inferKey(URI configDescriptionURI, String parameterName, String lastSegment) {
        String uri = configDescriptionURI.getSchemeSpecificPart().replace(":", ".");
        return configDescriptionURI.getScheme() + ".config." + uri + "." + parameterName + "." + lastSegment;
    }

}
