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

import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.tenant.mgt.common.exception.TenantMgtException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.PrivacyComplianceException;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.PasswordResetWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin.UserManagementAdminService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.CredentialManagementResponseBuilder;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserManagementAdminServiceImpl implements UserManagementAdminService {

    private static final Log log = LogFactory.getLog(UserManagementAdminServiceImpl.class);

    @POST
    @Path("/credentials")
    @Override
    public Response resetUserPassword(@QueryParam("username")
                                      @Size(max = 45)
                                      String user, @QueryParam("domain") String domain, PasswordResetWrapper credentials) {
        if (domain != null && !domain.isEmpty()) {
            user = domain + '/' + user;
        }
        return CredentialManagementResponseBuilder.buildResetPasswordResponse(user, credentials);
    }

    @DELETE
    @Path("/devices")
    @Override
    public Response deleteDeviceOfUser(@QueryParam("username") String username) {
        try {
            DeviceMgtAPIUtils.getPrivacyComplianceProvider().deleteDevicesOfUser(username);
            return Response.status(Response.Status.OK).build();
        } catch (PrivacyComplianceException e) {
            String msg = "Error occurred while deleting the devices belongs to the user.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/type/{device-type}/id/{device-id}")
    @Override
    public Response deleteDevice(@PathParam("device-type") @Size(max = 45) String deviceType,
                                 @PathParam("device-id") @Size(max = 45) String deviceId) {

        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            DeviceMgtAPIUtils.getPrivacyComplianceProvider().deleteDeviceDetails(deviceIdentifier);
            return Response.status(Response.Status.OK).build();
        } catch (PrivacyComplianceException e) {
            String msg = "Error occurred while deleting the devices information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/domain/{tenantDomain}")
    @Override
    public Response deleteTenantByDomain(@PathParam("tenantDomain") String tenantDomain, @QueryParam("deleteAppArtifacts") boolean deleteAppArtifacts) {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            if (tenantId != MultitenantConstants.SUPER_TENANT_ID){
                String msg = "Only super tenants are allowed to delete tenants";
                log.error(msg);
                return Response.status(Response.Status.UNAUTHORIZED).entity(msg).build();
            } else {
                if (deleteAppArtifacts) {
                    DeviceMgtAPIUtils.getApplicationManager().deleteApplicationArtifactsByTenantDomain(tenantDomain);
                }
                DeviceMgtAPIUtils.getApplicationManager().deleteApplicationDataByTenantDomain(tenantDomain);
                DeviceMgtAPIUtils.getDeviceManagementService().deleteDeviceDataByTenantDomain(tenantDomain);
                DeviceMgtAPIUtils.getTenantManagerAdminService().deleteTenant(tenantDomain);
                String msg = "Tenant Deletion process has been initiated for tenant:" + tenantDomain;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return Response.status(Response.Status.OK).entity(msg).build();
            }

        } catch (TenantMgtException e) {
            String msg = "Error deleting tenant: " + tenantDomain;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error deleting application data of tenant: " + tenantDomain;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error deleting device data of tenant: " + tenantDomain;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
