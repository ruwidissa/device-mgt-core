/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.carbon.device.mgt.config.jaxrs.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entgra.carbon.device.mgt.config.jaxrs.beans.ErrorResponse;
import io.entgra.carbon.device.mgt.config.jaxrs.service.DeviceManagementConfigService;
import io.entgra.carbon.device.mgt.config.jaxrs.util.DeviceMgtAPIUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.AppRegistrationCredentials;
import org.wso2.carbon.device.mgt.common.ApplicationRegistrationException;
import org.wso2.carbon.device.mgt.common.DeviceTransferRequest;
import org.wso2.carbon.device.mgt.common.configuration.mgt.AmbiguousConfigurationException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.DeviceConfiguration;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.general.TenantDetail;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.keymanager.KeyManagerConfigurations;
import org.wso2.carbon.device.mgt.core.config.ui.UIConfiguration;
import org.wso2.carbon.device.mgt.core.config.ui.UIConfigurationManager;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManagementConfigServiceImpl implements DeviceManagementConfigService {

    private static final Log log = LogFactory.getLog(DeviceManagementConfigServiceImpl.class);

    @Override
    @GET
    @Path("/configurations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfiguration(@HeaderParam("token") String token,
                                     @QueryParam("properties") String properties) {
        DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            if (token == null || token.isEmpty()) {
                String msg = "No valid token property found";
                log.error(msg);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()
                ).build();
            }

            if (properties == null || properties.isEmpty()) {
                String msg = "Devices configuration retrieval criteria cannot be null or empty.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()
                ).build();
            }

            ObjectMapper mapper = new ObjectMapper();
            properties = parseUriParamsToJSON(properties);
            Map<String, String> deviceProps = mapper.readValue(properties,
                                                               new TypeReference<Map<String, String>>() {
                                                               });
            deviceProps.put("token", token);
            DeviceConfiguration devicesConfiguration =
                    dms.getDeviceConfiguration(deviceProps);
            setAccessTokenToDeviceConfigurations(devicesConfiguration);
            return Response.status(Response.Status.OK).entity(devicesConfiguration).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving configurations";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceNotFoundException e) {
            log.warn(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        } catch (AmbiguousConfigurationException e) {
            String msg = "Configurations are ambiguous. " + e.getMessage();
            log.warn(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (JsonParseException | JsonMappingException e) {
            String msg = "Malformed device property structure";
            log.error(msg.concat(" ").concat(properties), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (IOException e) {
            String msg = "Error occurred while parsing query param JSON data.";
            log.error(msg.concat(" ").concat(properties), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @PUT
    @Path("/transfer")
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response transferDevices(DeviceTransferRequest deviceTransferRequest) {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        int tenantId = ctx.getTenantId(false);
        if (tenantId != -1234) {
            return Response.status(Response.Status.FORBIDDEN).entity("Tenant '" + ctx.getTenantDomain(true) +
                    "' does not have privilege to transfer device").build();
        }
        DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            List<String> devicesTransferred = dms.transferDeviceToTenant(deviceTransferRequest);
            if (devicesTransferred.isEmpty()) {
                String msg = "Devices are not enrolled to super tenant";
                log.warn(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            } else {
                return Response.status(Response.Status.OK).entity(devicesTransferred).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while transferring device to tenant " +
                    deviceTransferRequest.getDestinationTenant();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceNotFoundException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        }
    }

    @GET
    @Override
    @Consumes("application/json")
    @Path("/configurations/ui-config")
    public Response getUiConfig() {
        UIConfigurationManager uiConfigurationManager = UIConfigurationManager.getInstance();
        if (uiConfigurationManager == null) {
            String msg = "IoTS UI configuration manager is not initialized.";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        UIConfiguration uiConfiguration = uiConfigurationManager.getUIConfig();
        if (uiConfiguration == null) {
            String msg = "IoTS UI configuration is not defined.";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(uiConfiguration).build();
    }

    private String parseUriParamsToJSON(String uriParams) {
        uriParams = uriParams.replaceAll("=", "\":\"");
        uriParams = uriParams.replaceAll("&", "\",\"");
        return "{\"" + uriParams + "\"}";
    }

    private void setAccessTokenToDeviceConfigurations(DeviceConfiguration devicesConfiguration)
            throws DeviceManagementException {
        try {
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            KeyManagerConfigurations kmConfig = deviceManagementConfig.getKeyManagerConfigurations();
            AppRegistrationCredentials credentials = DeviceManagerUtil.getApplicationRegistrationCredentials(
                    System.getProperty(DeviceManagementConstants.ConfigurationManagement.IOT_GATEWAY_HOST),
                    System.getProperty(DeviceManagementConstants.ConfigurationManagement.IOT_GATEWAY_HTTPS_PORT),
                    kmConfig.getAdminUsername(),
                    kmConfig.getAdminPassword());
            AccessTokenInfo accessTokenForAdmin = DeviceManagerUtil.getAccessTokenForDeviceOwner(
                    DeviceManagementConstants.ConfigurationManagement.SCOPES_FOR_TOKEN,
                    credentials.getClient_id(), credentials.getClient_secret(),
                    devicesConfiguration.getDeviceOwner());
            devicesConfiguration.setAccessToken(accessTokenForAdmin.getAccessToken());
            devicesConfiguration.setRefreshToken(accessTokenForAdmin.getRefreshToken());
        } catch (ApplicationRegistrationException e) {
            String msg = "Failure on retrieving application registration";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (JWTClientException e) {
            String msg = "Error occurred while creating JWT client : " + e.getMessage();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    @Override
    @Path("/tenants")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTenants() {
        List<TenantDetail> tenantDetails;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            RealmService realmService = DeviceMgtAPIUtils.getRealmService();
            try {
                Tenant[] tenants = realmService.getTenantManager().getAllTenants();
                tenantDetails = new ArrayList<>();
                Tenant superTenant = new Tenant();
                superTenant.setId(MultitenantConstants.SUPER_TENANT_ID);
                superTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                superTenant.setAdminName(realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID)
                        .getRealmConfiguration().getAdminUserName());
                superTenant.setActive(true);
                tenantDetails.add(getTenantDetail(superTenant));
                if (tenants != null && tenants.length > 0) {
                    for (Tenant tenant : tenants) {
                        tenantDetails.add(getTenantDetail(tenant));
                    }
                }
                return Response.status(Response.Status.OK).entity(tenantDetails).build();
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

    private TenantDetail getTenantDetail(Tenant tenant) {
        TenantDetail tenantDetail = new TenantDetail();
        tenantDetail.setId(tenant.getId());
        tenantDetail.setAdminFirstName(tenant.getAdminFirstName());
        tenantDetail.setAdminFullName(tenant.getAdminFullName());
        tenantDetail.setAdminLastName(tenant.getAdminLastName());
        tenantDetail.setAdminName(tenant.getAdminName());
        tenantDetail.setDomain(tenant.getDomain());
        tenantDetail.setEmail(tenant.getEmail());
        return tenantDetail;
    }

    @POST
    @Path("/permissions")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addPermission(List<String> permissions) {
        for (String path : permissions) {
            try {
                PermissionUtils.putPermission(path);
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
