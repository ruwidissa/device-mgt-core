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
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.exceptions.UserNotFoundException;
import org.wso2.carbon.device.mgt.common.general.TenantDetail;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.DeviceManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/admin/devices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManagementAdminServiceImpl implements DeviceManagementAdminService {

    private static final Log log = LogFactory.getLog(DeviceManagementAdminServiceImpl.class);

    @Override
    @GET
    public Response getDevicesByName(@QueryParam("name") @Size(max = 45) String name,
                                     @QueryParam("type") @Size(min = 2, max = 45) String type,
                                     @QueryParam("tenant-domain") String tenantDomain,
                                     @HeaderParam("If-Modified-Since") String ifModifiedSince,
                                     @QueryParam("offset") int offset,
                                     @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        int currentTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (MultitenantConstants.SUPER_TENANT_ID != currentTenantId) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(
                            "Current logged in user is not authorized to perform this operation").build()).build();
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(DeviceMgtAPIUtils.getTenantId(tenantDomain));

            PaginationRequest request = new PaginationRequest(offset, limit);
            request.setDeviceType(type);
            request.setDeviceName(name);
            List<Device> devices = DeviceMgtAPIUtils.getDeviceManagementService().
                    getDevicesByNameAndType(request, false);

            // setting up paginated result
            DeviceList deviceList = new DeviceList();
            deviceList.setList(devices);
            deviceList.setCount(devices.size());

            return Response.status(Response.Status.OK).entity(deviceList).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred at server side while fetching device list.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    @Path("/count")
    @GET
    public Response getDeviceCount(@QueryParam("status") String status) {
        int deviceCount;
        try {
            if (status == null) {
                deviceCount = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCount();
            } else {
                deviceCount = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCount(EnrolmentInfo.Status.valueOf(status));
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching device count.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceCount).build();
    }

    @PUT
    @Override
    @Path("/device-owner")
    public Response updateEnrollOwner(
            @QueryParam("owner") String owner,
            List<String> deviceIdentifiers){
        try {
            if (DeviceMgtAPIUtils.getDeviceManagementService().updateEnrollment(owner, deviceIdentifiers)) {
                String msg = "Device owner is updated successfully.";
                return Response.status(Response.Status.OK).entity(msg).build();
            }
            String msg = "Device owner updating is failed.";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidDeviceException e) {
            String msg = "Invalid device identifiers are found with the request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred when updating device owners.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (UserNotFoundException e) {
            String msg = "Couldn't found the owner in user store to update the owner of devices.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @PUT
    @Override
    @Path("/permanent-delete")
    public Response deleteDevicesPermanently(List<String> deviceIdentifiers) {
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            if (!deviceManagementProviderService.deleteDevices(deviceIdentifiers)) {
                String msg = "Found un-deployed device type.";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
            return Response.status(Response.Status.OK).entity(true).build();
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while permanently deleting devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        catch (InvalidDeviceException e) {
            String msg = "Found Invalid devices";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @Override
    @Path("/tenants")
    @GET
    public Response getTenants() {
        List<TenantDetail> tenantDetails;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            RealmService realmService = (RealmService) PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
            if (realmService == null) {
                String msg = "RealmService is not initialized";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }

            try {
                Tenant[] tenants = realmService.getTenantManager().getAllTenants();
                tenantDetails = new ArrayList<>();
                if (tenants != null && tenants.length > 0) {
                    for (Tenant tenant : tenants) {
                        TenantDetail tenantDetail = new TenantDetail();
                        tenantDetail.setId(tenant.getId());
                        tenantDetail.setAdminFirstName(tenant.getAdminFirstName());
                        tenantDetail.setAdminFullName(tenant.getAdminFullName());
                        tenantDetail.setAdminLastName(tenant.getAdminLastName());
                        tenantDetail.setAdminName(tenant.getAdminName());
                        tenantDetail.setDomain(tenant.getDomain());
                        tenantDetail.setEmail(tenant.getEmail());
                        tenantDetails.add(tenantDetail);
                    }
                    return Response.status(Response.Status.OK).entity(tenantDetails).build();
                } else {
                    return Response.status(Response.Status.NOT_FOUND).entity("No tenants found")
                            .build();
                }
            } catch (UserStoreException e) {
                String msg = "Error occurred while fetching tenant list";
                log.error(msg, e);
                return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("This API is available " +
                    "for super tenant admin only.").build();
        }
    }


    @POST
    @Path("/permissions")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addPermission(List<String> permissions) {
        String PERMISSION_PREFIX = "/permission/admin";
        PermissionManagerService permissionService = DeviceMgtAPIUtils.getPermissionManagerService();
        org.wso2.carbon.device.mgt.common.permission.mgt.Permission permission = new org
                .wso2.carbon.device.mgt.common.permission.mgt.Permission();

        for (String path : permissions) {
            path = PERMISSION_PREFIX + path;
            permission.setPath(path);
            permission.setUrl(path);
            try {
                permissionService.addPermission(permission);
            } catch (PermissionManagementException e) {
                String msg = "Error occurred adding permission";
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        }
        return Response.status(Response.Status.OK).build();

    }
}
