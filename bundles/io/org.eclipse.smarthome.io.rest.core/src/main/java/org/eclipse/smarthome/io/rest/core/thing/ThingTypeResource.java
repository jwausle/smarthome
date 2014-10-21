/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.core.thing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.smarthome.config.core.ConfigDescription;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.eclipse.smarthome.core.thing.type.ThingTypeRegistry;
import org.eclipse.smarthome.io.rest.AbstractRESTResource;
import org.eclipse.smarthome.io.rest.core.thing.beans.ConfigDescriptionParameterBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingTypeBean;
import org.eclipse.smarthome.io.rest.core.thing.beans.ThingTypeListBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a java bean that is used with JAXB to serialize things to XML or
 * JSON.
 * 
 * @author Dennis Nobel - Initial contribution
 */
@Path("thing-types")
public class ThingTypeResource extends AbstractRESTResource {

    private Logger logger = LoggerFactory.getLogger(ThingTypeResource.class);

    @GET
    @Produces({ MediaType.WILDCARD })
    public Response getAll() {
        ThingTypeRegistry thingTypeRegistry = getService(ThingTypeRegistry.class);
        ThingTypeListBean thingTypeListBean = convertToListBean(thingTypeRegistry.getThingTypes());
        return Response.ok(thingTypeListBean).build();
    }

    @GET
    @Path("/{thingTypeUID}")
    @Produces({ MediaType.WILDCARD })
    public Response getByUID(@PathParam("thingTypeUID") String thingTypeUID) {
        ThingTypeRegistry thingTypeRegistry = getService(ThingTypeRegistry.class);
        ThingType thingType = thingTypeRegistry.getThingType(new ThingTypeUID(thingTypeUID));
        if (thingType != null) {
            return Response.ok(convertToThingTypeBean(thingType)).build();
        } else {
            return Response.noContent().build();
        }
    }

    public List<ConfigDescriptionParameterBean> getConfigDescriptionParameterBeans(ThingTypeUID thingTypeUID) {
        ConfigDescriptionRegistry configDescriptionRegistry = getService(ConfigDescriptionRegistry.class);
        try {
            ConfigDescription configDescription = configDescriptionRegistry.getConfigDescription(new URI(
                    "thing-type", thingTypeUID.toString(), null));
            if (configDescription != null) {
                List<ConfigDescriptionParameterBean> configDescriptionParameterBeans = new ArrayList<>(
                        configDescription.getParameters().size());
                for (ConfigDescriptionParameter configDescriptionParameter : configDescription.getParameters()) {
                    ConfigDescriptionParameterBean configDescriptionParameterBean = new ConfigDescriptionParameterBean(
                            configDescriptionParameter.getName(), configDescriptionParameter.getType(),
                            configDescriptionParameter.getContext(), configDescriptionParameter.isRequired(),
                            String.valueOf(configDescriptionParameter.getDefault()),
                            configDescriptionParameter.getLabel(), configDescriptionParameter.getDescription());
                    configDescriptionParameterBeans.add(configDescriptionParameterBean);
                }
                return configDescriptionParameterBeans;
            }
        } catch (URISyntaxException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    public List<ThingTypeBean> getThingTypeBeans(String bindingId) {

        ThingTypeRegistry thingTypeRegistry = getService(ThingTypeRegistry.class);
        List<ThingType> thingTypes = thingTypeRegistry.getThingTypes(bindingId);

        List<ThingTypeBean> thingTypeBeans = convertToThingTypeBeans(thingTypes);

        return thingTypeBeans;
    }

    private ThingTypeListBean convertToListBean(List<ThingType> thingTypes) {
        return new ThingTypeListBean(convertToThingTypeBeans(thingTypes));
    }

    private ThingTypeBean convertToThingTypeBean(ThingType thingType) {
        return new ThingTypeBean(thingType.getUID().toString(), thingType.getLabel(), thingType.getDescription(),
                getConfigDescriptionParameterBeans(thingType.getUID()));
    }

    private List<ThingTypeBean> convertToThingTypeBeans(List<ThingType> thingTypes) {
        List<ThingTypeBean> thingTypeBeans = new ArrayList<>();

        for (ThingType thingType : thingTypes) {
            thingTypeBeans.add(convertToThingTypeBean(thingType));
        }

        return thingTypeBeans;
    }
}
