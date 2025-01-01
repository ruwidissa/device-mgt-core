/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.admin;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceTypeVersionWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin.DeviceTypeManagementAdminService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.PlatformConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceTypeVersion;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/admin/device-types")
public class DeviceTypeManagementAdminServiceImpl implements DeviceTypeManagementAdminService {

    private static final Log log = LogFactory.getLog(DeviceTypeManagementAdminServiceImpl.class);
    private static final String DEVICETYPE_REGEX_PATTERN = "^[^ /]+$";
    private static final Pattern patternMatcher = Pattern.compile(DEVICETYPE_REGEX_PATTERN);

    @Override
    @GET
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
            DeviceTypeMetaDefinition deviceTypeMetaDefinition = deviceType.getDeviceTypeMetaDefinition();
            try {
                String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                if (deviceTypeMetaDefinition.isSharedWithAllTenants() &&
                    !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                    String msg = "Invalid request, device type can only be shared with all the tenants " +
                                 "only if the request is sent by the super tenant";
                    log.error(msg);
                    return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
                }
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
    @Path("/{type}")
    public Response updateDeviceType(@PathParam("type") String type, DeviceType deviceType) {
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
    @GET
    @Path("/{type}/configs")
    public Response getDeviceTypePlatformConfig(@PathParam("type") String type) {
        if (!StringUtils.isEmpty(type)) {
            try {
                PlatformConfiguration platformConfiguration = DeviceMgtAPIUtils.getDeviceManagementService().getConfiguration(type);
                return Response.status(Response.Status.OK).entity(platformConfiguration).build();
            } catch (DeviceManagementException e) {
                String msg = "Error occurred while retrieving the Device type platform configuration";
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    @POST
    @Path("/{type}/configs")
    public Response addDeviceTypePlatformConfig(@PathParam("type") String type,
                                                PlatformConfiguration platformConfiguration) {
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

    @Override
    @POST
    @Path("/{deviceTypeName}/versions")
    public Response addDeviceTypeVersion(@PathParam("deviceTypeName") String deviceTypeName,
                                         DeviceTypeVersionWrapper versionWrapper) {
        if (versionWrapper != null && deviceTypeName != null && !deviceTypeName.isEmpty()
                && versionWrapper.getVersionName() != null && !versionWrapper.getVersionName().isEmpty()) {
            try {
                // Handle device type availability in current tenant.
                DeviceTypeVersion deviceTypeVersion = DeviceMgtAPIUtils.getDeviceManagementService()
                        .getDeviceTypeVersion(deviceTypeName, versionWrapper.getVersionName());
                if (deviceTypeVersion != null) {
                    String msg = "Device type version already available, " + deviceTypeName;
                    return Response.status(Response.Status.CONFLICT).entity(msg).build();
                }

                // Handle device type availability in current tenant.
                DeviceType deviceType = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(deviceTypeName);
                if (deviceType != null) {
                    boolean result = DeviceMgtAPIUtils.getDeviceManagementService()
                            .addDeviceTypeVersion(DeviceMgtAPIUtils.convertDeviceTypeVersionWrapper(deviceTypeName,
                                    deviceType.getId(), versionWrapper));
                    if (result) {
                        return Response.serverError().entity("Could not add the version").build();
                    } else {
                        return Response.status(Response.Status.OK).build();
                    }
                } else {
                    String msg = "Device type is not available " + versionWrapper.getVersionName();
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }

            } catch (DeviceManagementException e) {
                String msg = "Error occurred while adding a device type version.";
                log.error(msg, e);
                return Response.serverError().entity(msg).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    @GET
    @Path("/{deviceTypeName}/versions")
    public Response getDeviceTypeVersion(@PathParam("deviceTypeName") String deviceTypeName) {
        try {
            List<DeviceTypeVersion> deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService()
                    .getDeviceTypeVersions(deviceTypeName);
            return Response.status(Response.Status.OK).entity(deviceTypes).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting device type version for device type: " + deviceTypeName;
            log.error(msg, e);
            return Response.serverError().entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("/{deviceTypeName}/versions")
    public Response updateDeviceTypeVersion(@PathParam("deviceTypeName") String deviceTypeName,
                                            DeviceTypeVersionWrapper deviceTypeVersion) {
        if (deviceTypeVersion != null && deviceTypeVersion.getVersionName() == null || deviceTypeVersion
                .getVersionName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Device type version cannot be empty.").build();
        } else if (deviceTypeName == null || deviceTypeName.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Device type name cannot be empty.").build();
        }

        try {
            boolean isAuthorized = DeviceMgtAPIUtils.getDeviceManagementService()
                    .isDeviceTypeVersionChangeAuthorized(deviceTypeName, deviceTypeVersion.getVersionName());
            if (!isAuthorized) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized to modify version.")
                        .build();
            }

            DeviceType deviceType = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(deviceTypeName);
            if (deviceType != null) {
                boolean result = DeviceMgtAPIUtils.getDeviceManagementService().updateDeviceTypeVersion(DeviceMgtAPIUtils
                        .convertDeviceTypeVersionWrapper(deviceTypeName, deviceType.getId(), deviceTypeVersion));
                if (result) {
                    return Response.serverError().entity("Could not update the version").build();
                } else {
                    return Response.status(Response.Status.OK).build();
                }
            } else {
                String msg = "Device type is not available " + deviceTypeName;
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating device type: " + deviceTypeName ;
            log.error(msg, e);
            return Response.serverError().entity(msg).build();
        }
    }

    @Override
    @DELETE
    @Path("/{deviceTypeName}/versions/{version}")
    public Response deleteDeviceTypeVersion(@PathParam("deviceTypeName") String deviceTypeName,
                                            @PathParam("version") String version) {
        if (version == null || version.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Device type version cannot be empty.").build();
        } else if (deviceTypeName == null || deviceTypeName.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Device type name cannot be empty.").build();
        }

        DeviceTypeVersion deviceTypeVersion = new DeviceTypeVersion();
        deviceTypeVersion.setDeviceTypeName(deviceTypeName);
        deviceTypeVersion.setVersionName(version);
        deviceTypeVersion.setVersionStatus("REMOVED");
        try {
            boolean isAuthorized = DeviceMgtAPIUtils.getDeviceManagementService().isDeviceTypeVersionChangeAuthorized
                    (deviceTypeVersion.getDeviceTypeName(), deviceTypeVersion.getVersionName());
            if (!isAuthorized) {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized to modify version.")
                        .build();
            }
            boolean result = DeviceMgtAPIUtils.getDeviceManagementService().updateDeviceTypeVersion(deviceTypeVersion);
            if (result) {
                return Response.serverError().entity("Could not delete the version").build();
            } else {
                return Response.status(Response.Status.OK).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while updating device type: " + deviceTypeVersion.getDeviceTypeId() ;
            log.error(msg, e);
            return Response.serverError().entity(msg).build();
        }
    }

    @Override
    @DELETE
    @Path("/{deviceTypeName}")
    public Response deleteDeviceType(@PathParam("deviceTypeName") String deviceTypeName) {
        try {
            DeviceManagementProviderService deviceManagementProviderService =
                    DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceType deviceType = deviceManagementProviderService.getDeviceType(deviceTypeName);
            if (deviceType == null) {
                String msg = "Error, device of type: " + deviceTypeName + " does not exist";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            if (!deviceManagementProviderService.deleteDeviceType(deviceTypeName, deviceType)){
                String msg = "Error occurred while deleting device of type: " + deviceTypeName;
                log.error(msg);
                return Response.serverError().entity(msg).build();
            }
            return Response.status(Response.Status.ACCEPTED)
                    .entity("Device of type: " + deviceTypeName + " permanently deleted.")
                    .build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while deleting device of type: " + deviceTypeName;
            log.error(msg, e);
            return Response.serverError().entity(msg).build();
        }
    }

}
