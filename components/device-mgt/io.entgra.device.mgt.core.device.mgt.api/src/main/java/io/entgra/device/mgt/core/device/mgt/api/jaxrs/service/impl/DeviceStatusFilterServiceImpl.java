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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceStatusFilterService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.DeviceStatusManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/device-status-filters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceStatusFilterServiceImpl implements DeviceStatusFilterService {

    private static final Log log = LogFactory.getLog(DeviceStatusFilterServiceImpl.class);

    @Override
    @GET
    @Path("/{deviceType}")
    public Response getDeviceStatusFilters(@PathParam("deviceType") String deviceType) {
        List<String> result;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceStatusManagementService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceStatusManagmentService();
            result = deviceManagementProviderService.getDeviceStatusFilters(deviceType, tenantId);
            if (result != null) {
                return Response.status(Response.Status.OK).entity(result).build();
            }
            return Response.status(Response.Status.NO_CONTENT).entity(false).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting device status filter of the tenant.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/is-enabled")
    @Override
    public Response getDeviceStatusCheck() {
        boolean result;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceStatusManagementService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceStatusManagmentService();
            result = deviceManagementProviderService.getDeviceStatusCheck(tenantId);
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting device status filter of the tenant.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("/toggle-device-status")
    public Response updateDeviceStatusCheck(
            @QueryParam("isEnabled")
            boolean isEnabled) {
        boolean result;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceStatusManagementService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceStatusManagmentService();
            result = deviceManagementProviderService.updateDefaultDeviceStatusCheck(tenantId, isEnabled);
            if (result) {
                return Response.status(Response.Status.OK).entity("Successfully updated device status check.").build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).entity(false).build();
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while updating device status check.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    public Response updateDeviceStatusFilters(
            @QueryParam("deviceType")
            String deviceType,
            @QueryParam("deviceStatus")
            List<String> deviceStatus
    ) {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            DeviceStatusManagementService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceStatusManagmentService();
            deviceManagementProviderService.updateDefaultDeviceStatusFilters(tenantId, deviceType, deviceStatus);
            return Response.status(Response.Status.OK).entity("Successfully updated device status filters for " + deviceType).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while updating device status for " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
