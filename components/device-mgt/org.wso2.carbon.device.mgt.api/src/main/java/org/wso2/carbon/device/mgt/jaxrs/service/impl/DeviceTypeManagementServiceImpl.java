/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceTypeManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/device-types")
public class DeviceTypeManagementServiceImpl implements DeviceTypeManagementService {

    private static Log log = LogFactory.getLog(DeviceTypeManagementServiceImpl.class);

    @GET
    @Override
    public Response getDeviceTypes(@HeaderParam("If-Modified-Since") String ifModifiedSince,
                                   @QueryParam("offset") int offset,
                                   @QueryParam("limit") int limit,
                                   @QueryParam("filter") String filter) {

        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            List<DeviceType> deviceTypes;
            if (offset == 0 && limit == 0 && StringUtils.isEmpty(filter)) {
                deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService()
                        .getDeviceTypes();
            } else {
                PaginationRequest paginationRequest = new PaginationRequest(offset, limit);
                if (!StringUtils.isEmpty(filter)) {
                    paginationRequest.setFilter(filter);
                }
                deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService()
                        .getDeviceTypes(paginationRequest);
            }

            List<DeviceType> filteredDeviceTypes = new ArrayList<>();
            for (DeviceType deviceType : deviceTypes) {
                filteredDeviceTypes.add(clearMetaEntryInfo(deviceType));
            }
            return Response.status(Response.Status.OK).entity(filteredDeviceTypes).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred at server side while fetching device type.";
            log.error(msg, e);
            return Response.serverError().entity(msg).build();
        }  catch (InputValidationException e) {
            String msg = "Invalid pagination parameters";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

    }

    @Override
    @GET
    @Path("/{type}")
    public Response getDeviceTypeByName(@PathParam("type") @Size(min = 2, max = 45) String type) {
        if (type != null && type.length() > 0) {
            try {
                DeviceType deviceType = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(type);
                if (deviceType == null) {
                    String msg = "Device type does not exist, " + type;
                    return Response.status(Response.Status.NO_CONTENT).entity(msg).build();
                }
                return Response.status(Response.Status.OK).entity(clearMetaEntryInfo(deviceType)).build();
            } catch (DeviceManagementException e) {
                String msg = "Error occurred at server side while fetching device type.";
                log.error(msg, e);
                return Response.serverError().entity(msg).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Override
    @Path("/{type}/features")
    public Response getFeatures(@PathParam("type") @Size(max = 45) String type,
                                @QueryParam("featureType") String featureType,
                                @QueryParam("hidden") String hidden,
                                @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        List<Feature> features;
        DeviceManagementProviderService dms;
        try {
            if (StringUtils.isEmpty(type)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Type cannot be empty.").build()).build();
            }
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            FeatureManager fm = dms.getFeatureManager(type);

            if (fm == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("No feature manager is " +
                                "registered with the given type '" + type + "'").build()).build();
            }

            if (StringUtils.isEmpty(hidden)) {
                features = fm.getFeatures(featureType);
            } else {
                features = fm.getFeatures(featureType, Boolean.valueOf(hidden));
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of [" + type + "] features with params " +
                    "{featureType: " + featureType + ", hidden: " + hidden + "}";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceTypeNotFoundException e) {
            String msg = "No device type found with name '" + type + "'";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorResponse.ErrorResponseBuilder()
                            .setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(features).build();
    }

    @GET
    @Override
    @Path("/{type}/configs")
    public Response getConfigs(@PathParam("type") @Size(min = 2, max = 45) String type,
                               @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        PlatformConfiguration platformConfiguration;
        try {
            platformConfiguration = DeviceMgtAPIUtils.getDeviceManagementService().getConfiguration(type);
            if (platformConfiguration == null) {
                platformConfiguration = new PlatformConfiguration();
                platformConfiguration.setType(type);
                platformConfiguration.setConfiguration(new ArrayList<>());
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the '" + type + "' platform configuration";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(platformConfiguration).build();
    }

    /**
     * This cleans up the configs that should not be exposed to iot users.
     *
     * @param deviceType device type retrieved from service layer.
     * @return sanitized device type.
     */
    private DeviceType clearMetaEntryInfo(DeviceType deviceType) {
        DeviceTypeMetaDefinition metaDefinition = deviceType.getDeviceTypeMetaDefinition();
        if (metaDefinition != null) {
            metaDefinition.setInitialOperationConfig(null);
            if (metaDefinition.getPushNotificationConfig() != null) {
                metaDefinition.setPushNotificationConfig(new PushNotificationConfig(metaDefinition.
                        getPushNotificationConfig().getType(), false, null));
            }
            deviceType.setDeviceTypeMetaDefinition(metaDefinition);
        }
        return deviceType;
    }

}
