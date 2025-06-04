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
package io.entgra.device.mgt.core.device.mgt.config.api.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.entgra.device.mgt.core.device.mgt.common.AppRegistrationCredentials;
import io.entgra.device.mgt.core.device.mgt.common.ApplicationRegistrationException;
import io.entgra.device.mgt.core.device.mgt.common.DeviceTransferRequest;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.AmbiguousConfigurationException;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.DeviceConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.OTPManagementException;
import io.entgra.device.mgt.core.device.mgt.common.general.TenantDetail;
import io.entgra.device.mgt.core.device.mgt.common.otp.mgt.OTPEmailTypes;
import io.entgra.device.mgt.core.device.mgt.common.otp.mgt.dto.OneTimePinDTO;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.device.mgt.config.api.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.config.api.service.DeviceManagementConfigService;
import io.entgra.device.mgt.core.device.mgt.config.api.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.common.util.SystemPropertyUtil;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.keymanager.KeyManagerConfigurations;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.UIConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.UIConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.OperationConfigurationService;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.dto.OperationConfig;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.exceptions.OperationConfigAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.exceptions.OperationConfigException;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.exceptions.OperationConfigNotFoundException;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import io.entgra.device.mgt.core.identity.jwt.client.extension.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import java.util.Collections;
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
                                     @QueryParam("properties") String properties,
                                     @QueryParam("withAccessToken") boolean withAccessToken,
                                     @QueryParam("withGateways") boolean withGateways) {
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
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }

            ObjectMapper mapper = new ObjectMapper();
            properties = parseUriParamsToJSON(properties);
            Map<String, String> deviceProps = mapper.readValue(properties,
                    new TypeReference<Map<String, String>>() {
                    });
            deviceProps.put("token", token);
            DeviceConfiguration devicesConfiguration =
                    dms.getDeviceConfiguration(deviceProps);

            if (withGateways) {
                devicesConfiguration.setMqttGateway(buildMqttGatewayUrl());
                devicesConfiguration.setHttpGateway(buildHttpGatewayUrl());
                devicesConfiguration.setHttpsGateway(buildHttpsGatewayUrl());
            }
            if (withAccessToken) setAccessTokenToDeviceConfigurations(devicesConfiguration);
            else setOTPTokenToDeviceConfigurations(devicesConfiguration);
            return Response.status(Response.Status.OK).entity(devicesConfiguration).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving configurations";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceNotFoundException e) {
            log.warn(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (AmbiguousConfigurationException e) {
            String msg = "Configurations are ambiguous. " + e.getMessage();
            log.warn(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (JsonParseException | JsonMappingException e) {
            String msg = "Malformed device property structure";
            log.error(msg.concat(" ").concat(properties), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (IOException e) {
            String msg = "Error occurred while parsing query param JSON data.";
            log.error(msg.concat(" ").concat(properties), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Builds the MQTT Gateway URL using system properties and predefined constants.
     * <p>
     * The URL is constructed using the TCP prefix, MQTT broker host, and HTTPS port,
     * all retrieved from system properties defined in the DeviceManagementConstants.
     *
     * @return the complete MQTT Gateway URL as a {@link String}
     */
    private static String buildMqttGatewayUrl() {
        return DeviceManagementConstants.ConfigurationManagement.TCP_PREFIX
                + SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.MQTT_BROKER_HOST)
                + DeviceManagementConstants.ConfigurationManagement.COLON
                + SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.MQTT_BROKER_HTTPS_PORT);
    }

    /**
     * Builds the HTTP Gateway URL using system properties and predefined constants.
     * <p>
     * The URL is constructed using the HTTP prefix, IoT core host, and HTTPS port,
     * all retrieved from system properties defined in the DeviceManagementConstants.
     *
     * @return the complete HTTP Gateway URL as a {@link String}
     */
    private static String buildHttpGatewayUrl() {
        return DeviceManagementConstants.ConfigurationManagement.HTTP_PREFIX
                + SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.IOT_CORE_HOST)
                + DeviceManagementConstants.ConfigurationManagement.COLON
                + SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.IOT_CORE_HTTPS_PORT);
    }

    /**
     * Builds the HTTPS Gateway URL using system properties and predefined constants.
     * <p>
     * The URL is constructed using the HTTPS prefix, IoT core host, and HTTPS port,
     * all retrieved from system properties defined in the DeviceManagementConstants.
     *
     * @return the complete HTTPS Gateway URL as a {@link String}
     */
    private static String buildHttpsGatewayUrl() {
        return DeviceManagementConstants.ConfigurationManagement.HTTPS_PREFIX
                + SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.IOT_CORE_HOST)
                + DeviceManagementConstants.ConfigurationManagement.COLON
                + SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.IOT_CORE_HTTPS_PORT);
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
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
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
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
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
                    SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.IOT_GATEWAY_HOST),
                    SystemPropertyUtil.getRequiredProperty(DeviceManagementConstants.ConfigurationManagement.IOT_GATEWAY_HTTPS_PORT),
                    kmConfig.getAdminUsername(),
                    kmConfig.getAdminPassword());
            AccessTokenInfo accessTokenForAdmin = DeviceManagerUtil.getAccessTokenForDeviceOwner(
                    buildDeviceScopes(devicesConfiguration),
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

    /**
     * Builds a space-separated list of OAuth2 scopes specific to the provided device configuration.
     *
     * <p>The generated scopes include:</p>
     * <ul>
     *   <li>A device-specific scope prefix (e.g., device:{type}:{id})</li>
     *   <li>Scopes for publishing to MQTT topics associated with the device type</li>
     *   <li>A scope for subscribing to retrieve operations for the device</li>
     *   <li>A scope for publishing to update operations for the device</li>
     *   <li>Predefined static scopes for the token</li>
     * </ul>
     *
     * <p>This method also performs topic placeholder replacement and handles the conversion of MQTT
     * topics into scope format by replacing slashes with colons.</p>
     *
     * @param devicesConfiguration the device configuration object containing device ID and type
     * @return a space-separated {@link String} of OAuth2 scopes for the device
     * @throws DeviceManagementException if an error occurs while retrieving device type information or building scopes
     */
    private String buildDeviceScopes(DeviceConfiguration devicesConfiguration) throws DeviceManagementException {
        String type = devicesConfiguration.getDeviceType();
        String id = devicesConfiguration.getDeviceId();
        StringBuilder scopes = new StringBuilder(
                DeviceManagementConstants.ConfigurationManagement.SCOPE_DEVICE_PREFIX +
                        type.replace(" ", "") + ":" + id);

        try {
            List<String> mqttEventTopicStructure = Collections.emptyList();
            DeviceType deviceType = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceType(type);
            if (deviceType != null && deviceType.getDeviceTypeMetaDefinition() != null) {
                mqttEventTopicStructure = deviceType.getDeviceTypeMetaDefinition().getMqttEventTopicStructures();
            }
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            for (String topic : mqttEventTopicStructure) {
                if (topic.contains(DeviceManagementConstants.ConfigurationManagement.DEVICE_ID)) {
                    topic = topic.replace(DeviceManagementConstants.ConfigurationManagement.DEVICE_ID, id);
                }
                if (topic.contains(DeviceManagementConstants.ConfigurationManagement.DEVICE_TYPE)) {
                    topic = topic.replace(DeviceManagementConstants.ConfigurationManagement.DEVICE_TYPE, type);
                }
                if (topic.contains(DeviceManagementConstants.ConfigurationManagement.TENANT_DOMAIN)) {
                    topic = topic.replace(DeviceManagementConstants.ConfigurationManagement.TENANT_DOMAIN, tenantDomain);
                }
                topic = topic.replace("/", ":");
                scopes.append(" ").append(DeviceManagementConstants.ConfigurationManagement.SCOPE_PUB_PREFIX).append(topic);
            }
            // Scope for retrieving operations
            scopes.append(" ").append(DeviceManagementConstants.ConfigurationManagement.SCOPE_SUB_PREFIX)
                    .append(tenantDomain).append(":").append(type).append(":")
                    .append(id).append(DeviceManagementConstants.ConfigurationManagement.SCOPE_OPERATION_SUFFIX);
            // Scope for updating operations
            scopes.append(" ").append(DeviceManagementConstants.ConfigurationManagement.SCOPE_PUB_PREFIX)
                    .append(tenantDomain).append(":").append(type).append(":")
                    .append(id).append(DeviceManagementConstants.ConfigurationManagement.SCOPE_UPDATE_OPERATION_SUFFIX);
            // Append predefined static scopes
            scopes.append(" ").append(DeviceManagementConstants.ConfigurationManagement.SCOPES_FOR_TOKEN);

            return scopes.toString();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device, device id : " + id + ", device type : " + type;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    private void setOTPTokenToDeviceConfigurations(DeviceConfiguration deviceConfiguration)
            throws DeviceManagementException {
        OneTimePinDTO oneTimePinData = new OneTimePinDTO();
        oneTimePinData.setEmail(OTPEmailTypes.DEVICE_ENROLLMENT.toString());
        oneTimePinData.setEmailType(OTPEmailTypes.DEVICE_ENROLLMENT.toString());
        oneTimePinData.setUsername(deviceConfiguration.getDeviceOwner());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                deviceConfiguration.getTenantDomain(), true);
        oneTimePinData.setTenantId(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        PrivilegedCarbonContext.endTenantFlow();
        OTPManagementService otpManagementService = DeviceMgtAPIUtils.getOtpManagementService();
        try {
            OneTimePinDTO oneTimePinDTO = otpManagementService.generateOneTimePin(oneTimePinData, true);
            if (oneTimePinDTO == null) {
                String msg = "Null value returned when generating OTP token for " + oneTimePinData.getOtpToken();
                log.error(msg);
                throw new DeviceManagementException(msg);
            }
            deviceConfiguration.setAccessToken(oneTimePinDTO.getOtpToken());
        } catch (OTPManagementException ex) {
            String msg = "Error occurred while generating one time pin: " + ex.getMessage();
            log.error(msg, ex);
            throw new DeviceManagementException(msg, ex);
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

    @GET
    @Path("/operation-configuration")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getOperationConfiguration() {
        OperationConfig config;
        try {
            config = OperationConfigurationService.getOperationConfig();
        } catch (OperationConfigException e) {
            String msg = "Error occurred getting operation configuration";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        if (config == null) {
            String msg = "Operation configuration not provided";
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } else {
            return Response.status(Response.Status.OK).entity(config).build();
        }
    }

    @POST
    @Path("/operation-configuration")
    @Produces({MediaType.APPLICATION_JSON})
    public Response addOperationConfiguration(OperationConfig config) {
        try {
            if (config != null) {
                OperationConfigurationService.addOperationConfiguration(config);
            } else {
                String msg = "Operation configuration not provided";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
        } catch (OperationConfigException e) {
            String msg = "Error occurred adding operation configuration";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (OperationConfigAlreadyExistsException e) {
            String msg = "Operation configuration already exists";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(config).build();
    }

    @PUT
    @Path("/operation-configuration")
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateOperationConfiguration(OperationConfig config) {
        try {
            if (config != null) {
                OperationConfigurationService.updateOperationConfiguration(config);
            } else {
                String msg = "Operation configuration body not provided";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
        } catch (OperationConfigException e) {
            String msg = "Error occurred adding operation configuration";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(config).build();
    }

    @DELETE
    @Path("/operation-configuration")
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteOperationConfiguration() {
        String msg;
        try {
            OperationConfigurationService.deleteOperationConfiguration();
        } catch (OperationConfigException e) {
            msg = "Error occurred while deleting operation configuration";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (OperationConfigNotFoundException e) {
            msg = "Operation configuration not provided";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
        msg = "Operation configuration deleted successfully";
        log.info(msg);
        return Response.status(Response.Status.OK).entity(msg).build();
    }
}
