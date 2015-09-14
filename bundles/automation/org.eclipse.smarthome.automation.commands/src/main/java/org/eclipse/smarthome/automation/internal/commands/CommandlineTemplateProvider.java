/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.internal.commands;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Condition;
import org.eclipse.smarthome.automation.Trigger;
import org.eclipse.smarthome.automation.core.util.ConnectionValidator;
import org.eclipse.smarthome.automation.parser.Parser;
import org.eclipse.smarthome.automation.parser.Status;
import org.eclipse.smarthome.automation.template.RuleTemplate;
import org.eclipse.smarthome.automation.template.Template;
import org.eclipse.smarthome.automation.template.TemplateProvider;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * This class is implementation of {@link TemplateProvider}. It extends functionality of {@link AbstractCommandProvider}
 * <p>
 * It is responsible for execution of Automation {@link PluggableCommands}, corresponding to the {@link RuleTemplate}s:
 * <ul>
 * <li>imports the {@link RuleTemplate}s from local files or from URL resources
 * <li>provides functionality for persistence of the {@link RuleTemplate}s
 * <li>removes the {@link RuleTemplate}s and their persistence
 * </ul>
 *
 * @author Ana Dimova - Initial Contribution
 * @author Kai Kreuzer - refactored (managed) provider and registry implementation
 *
 */
public class CommandlineTemplateProvider extends AbstractCommandProvider<RuleTemplate>implements TemplateProvider {

    /**
     * This field holds a reference to the {@link ModuleTypeProvider} service registration.
     */
    @SuppressWarnings("rawtypes")
    protected ServiceRegistration tpReg;

    /**
     * This constructor creates instances of this particular implementation of {@link TemplateProvider}. It does not add
     * any new functionality to the constructors of the providers. Only provides consistency by invoking the parent's
     * constructor.
     *
     * @param context is the {@link BundleContext}, used for creating a tracker for {@link Parser} services.
     */
    public CommandlineTemplateProvider(BundleContext context) {
        super(context);
    }

    /**
     * This method differentiates what type of {@link Parser}s is tracked by the tracker.
     * For this concrete provider, this type is a {@link RuleTemplate} {@link Parser}.
     *
     * @see AbstractCommandProvider#addingService(org.osgi.framework.ServiceReference)
     */
    @Override
    public Object addingService(@SuppressWarnings("rawtypes") ServiceReference reference) {
        if (reference.getProperty(Parser.PARSER_TYPE).equals(Parser.PARSER_TEMPLATE)) {
            return super.addingService(reference);
        }
        return null;
    }

    /**
     * @see AutomationCommandsPluggable#exportTemplates(String, Set, File)
     */
    public Status exportTemplates(String parserType, Set<RuleTemplate> set, File file) {
        return super.exportData(parserType, set, file);
    }

    /**
     * @see AutomationCommandsPluggable#importTemplates(String, URL)
     */
    public Set<Status> importTemplates(String parserType, URL url) {
        InputStreamReader inputStreamReader = null;
        Parser<RuleTemplate> parser = parsers.get(parserType);
        if (parser != null)
            try {
                inputStreamReader = new InputStreamReader(new BufferedInputStream(url.openStream()));
                return importData(url, parser, inputStreamReader);
            } catch (IOException e) {
                Status s = new Status(logger, 0, null);
                s.error("Can't read from URL " + url, e);
                LinkedHashSet<Status> res = new LinkedHashSet<Status>();
                res.add(s);
                return res;
            } finally {
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException e) {
                }
            }
        return null;
    }

    /**
     * @see org.eclipse.smarthome.automation.TemplateProvider#getTemplate(java.lang.String, java.util.Locale)
     */
    @SuppressWarnings("unchecked")
    @Override
    public RuleTemplate getTemplate(String UID, Locale locale) {
        synchronized (providerPortfolio) {
            return providedObjectsHolder.get(UID);
        }
    }

    /**
     * @see org.eclipse.smarthome.automation.TemplateProvider#getTemplates(java.util.Locale)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<RuleTemplate> getTemplates(Locale locale) {
        synchronized (providedObjectsHolder) {
            return providedObjectsHolder.values();
        }
    }

    @Override
    public void close() {
        if (tpReg != null) {
            tpReg.unregister();
            tpReg = null;
        }
        super.close();
    }

    /**
     * @see AbstractCommandProvider#importData(URL, Parser, InputStreamReader)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected Set<Status> importData(URL url, Parser<RuleTemplate> parser, InputStreamReader inputStreamReader) {
        Set<Status> providedObjects = parser.importData(inputStreamReader);
        if (providedObjects != null && !providedObjects.isEmpty()) {
            List<String> portfolio = new ArrayList<String>();
            synchronized (providerPortfolio) {
                providerPortfolio.put(url, portfolio);
            }
            for (Status status : providedObjects) {
                if (status.hasErrors())
                    continue;
                RuleTemplate ruleT = (RuleTemplate) status.getResult();
                String uid = ruleT.getUID();
                try {
                    ConnectionValidator.validateConnections(AutomationCommandsPluggable.moduleTypeRegistry,
                            ruleT.getModules(Trigger.class), ruleT.getModules(Condition.class),
                            ruleT.getModules(Action.class));
                } catch (Exception e) {
                    status.success(null);
                    status.error("Failed to validate connections of RuleTemplate with UID \"" + uid + "\"! "
                            + e.getMessage(), e);
                    continue;
                }
                if (checkExistence(uid, status))
                    continue;
                portfolio.add(uid);
                synchronized (providedObjectsHolder) {
                    providedObjectsHolder.put(uid, ruleT);
                }
            }
        }
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(REG_PROPERTY_RULE_TEMPLATES, providedObjectsHolder.keySet());
        if (tpReg == null)
            tpReg = bc.registerService(TemplateProvider.class.getName(), this, properties);
        else {
            tpReg.setProperties(properties);
        }
        return providedObjects;
    }

    /**
     * This method is responsible for checking the existence of {@link Template}s with the same
     * UIDs before these objects to be added in the system.
     *
     * @param uid UID of the newly created {@link Template}, which to be checked.
     * @param status {@link Status} of the {@link AutomationCommand} operation. Can be successful or can fail for these
     *            {@link ModuleType}s or {@link Template}s, for which a {@link Template} with the same UID, exists.
     * @return <code>true</code> if {@link Template} with the same UID exists or <code>false</code> in the opposite
     *         case.
     */
    protected boolean checkExistence(String uid, Status s) {
        if (AutomationCommandsPluggable.templateRegistry == null) {
            s.error("Failed to create Rule Template with UID \"" + uid
                    + "\"! Can't guarantee yet that other Rule Template with the same UID does not exist.",
                    new IllegalArgumentException());
            s.success(null);
            return true;
        }
        if (AutomationCommandsPluggable.templateRegistry.get(uid) != null) {
            s.error("Rule Template with UID \"" + uid
                    + "\" already exists! Failed to create a second with the same UID!",
                    new IllegalArgumentException());
            s.success(null);
            return true;
        }
        return false;
    }

}