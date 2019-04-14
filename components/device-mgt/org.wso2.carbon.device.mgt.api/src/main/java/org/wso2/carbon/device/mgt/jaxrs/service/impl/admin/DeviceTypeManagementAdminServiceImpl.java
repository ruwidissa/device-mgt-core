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
 *
 *
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceTypeManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/admin/device-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceTypeManagementAdminServiceImpl implements DeviceTypeManagementAdminService {

    private static final Log log = LogFactory.getLog(DeviceTypeManagementAdminServiceImpl.class);
    private static final String DEVICETYPE_REGEX_PATTERN = "^[^ /]+$";
    private static final Pattern patternMatcher = Pattern.compile(DEVICETYPE_REGEX_PATTERN);

    @GET
    @Override
    public Response getDeviceTypes() {
        try {
            List<DeviceType> deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceTypes();
            return Response.status(Response.Status.OK).entity(deviceTypes).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the list of device types.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @Override
    @GET
    @Path("/{type}")
    public Response getDeviceTypeByName(@PathParam("type") String type) {
        if (type != null && type.length() > 0) {
            try {
                DeviceType deviceType = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(type);
                if (deviceType == null) {
                    String msg = "Device type does not exist, " + type;
                    return Response.status(Response.Status.NO_CONTENT).entity(msg).build();
                }
                return Response.status(Response.Status.OK).entity(deviceType).build();
            } catch (DeviceManagementException e) {
                String msg = "Error occurred at server side while fetching device type.";
                log.error(msg, e);
                return Response.serverError().entity(msg).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    @POST
    public Response addDeviceType(DeviceType deviceType) {
        if (deviceType != null && deviceType.getDeviceTypeMetaDefinition() != null) {
            try {
                if (DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(deviceType.getName()) != null) {
                    String msg = "Device type already available, " + deviceType.getName();
                    return Response.status(Response.Status.CONFLICT).entity(msg).build();
                }
                Matcher matcher = patternMatcher.matcher(deviceType.getName());
                if(matcher.find()) {
                    DeviceManagementService httpDeviceTypeManagerService =
                            DeviceMgtAPIUtils.getDeviceTypeGeneratorService()
                                    .populateDeviceManagementService(deviceType.getName(),
                                                                     deviceType.getDeviceTypeMetaDefinition());
                    DeviceMgtAPIUtils.getDeviceManagementService().registerDeviceType(httpDeviceTypeManagerService);
                    return Response.status(Response.Status.OK).build();
                } else {
                    return Response.status(Response.Status.BAD_REQUEST).entity("Device type name does not match " +
                            "the pattern " + DEVICETYPE_REGEX_PATTERN).build();
                }
            } catch (DeviceManagementException e) {
                String msg = "Error occurred at server side while adding a device type.";
                log.error(msg, e);
                return Response.serverError().entity(msg).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    @PUT
    public Response updateDeviceType(String type, DeviceType deviceType) {
        if (deviceType != null && deviceType.getDeviceTypeMetaDefinition() != null) {
            if (deviceType.getName() == null || !deviceType.getName().equals(type)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Type name mismatch. Expected: '" + type +
                        "' Found: '"+ deviceType.getName() + "'").build();
            }
            try {
                if (DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(type) == null) {
                    String msg = "Device type does not exist, " + type;
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
                DeviceManagementService httpDeviceTypeManagerService = DeviceMgtAPIUtils.getDeviceTypeGeneratorService()
                        .populateDeviceManagementService(deviceType.getName(), deviceType.getDeviceTypeMetaDefinition());
                DeviceMgtAPIUtils.getDeviceManagementService().registerDeviceType(httpDeviceTypeManagerService);
                return Response.status(Response.Status.OK).build();
            } catch (DeviceManagementException e) {
                String msg = "Error occurred at server side while updating the device type.";
                log.error(msg, e);
                return Response.serverError().entity(msg).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    public Response addDeviceTypePlatformConfig(String type, PlatformConfiguration platformConfiguration) {
        boolean isSaved;
        if (platformConfiguration.getType() == null || !platformConfiguration.getType().equals(type)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Type name mismatch. Expected: '" + type +
                    "' Found: '"+ platformConfiguration.getType() + "'").build();
        }
        try {
            isSaved = DeviceMgtAPIUtils.getDeviceManagementService().saveConfiguration(platformConfiguration);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the Android tenant configuration";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(isSaved ? Response.Status.OK : Response.Status.BAD_REQUEST).build();
    }
}
