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

package io.entgra.device.mgt.core.device.mgt.common.configuration.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * This class is use to wrap and send device configuration data
 * to the device
 */
@ApiModel(value = "DeviceConfiguration", description = "This class carries all information related to " +
                                                       "Device configurations required to communicate with " +
                                                       "the server")
public class DeviceConfiguration {
    @ApiModelProperty(name = "deviceId", value = "ID of the device", required = true)
    private String deviceId;

    @ApiModelProperty(name = "deviceType", value = "Type of the device", required = true)
    private String deviceType;

    @ApiModelProperty(name = "tenantDomain", value = "Tenant which the device owned")
    private String tenantDomain;

    @ApiModelProperty(name = "configurationEntries", value = "Platform Configurations", required = true)
    private List<ConfigurationEntry> configurationEntries;

    @ApiModelProperty(name = "accessToken", value = "Token that can be use to communicate with the server")
    private String accessToken;

    @ApiModelProperty(name = "refreshToken", value = "Token that can be use to communicate with the server")
    private String refreshToken;

    @ApiModelProperty(name = "deviceOwner", value = "Owner of the selected device", required = true)
    private String deviceOwner;

    @ApiModelProperty(name = "mqttGateway", value = "Mqtt Gateway to communicate with the server")
    private String mqttGateway;

    @ApiModelProperty(name = "httpsGateway", value = "Https Gateway to communicate with the server")
    private String httpsGateway;

    @ApiModelProperty(name = "httpGateway", value = "Http Gateway to communicate with the server")
    private String httpGateway;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public List<ConfigurationEntry> getConfigurationEntries() {
        return configurationEntries;
    }

    public void setConfigurationEntries(
            List<ConfigurationEntry> configurationEntries) {
        this.configurationEntries = configurationEntries;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getDeviceOwner() {
        return deviceOwner;
    }

    public void setDeviceOwner(String deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getMqttGateway() {
        return mqttGateway;
    }

    public void setMqttGateway(String mqttGateway) {
        this.mqttGateway = mqttGateway;
    }

    public String getHttpsGateway() {
        return httpsGateway;
    }

    public void setHttpsGateway(String httpsGateway) {
        this.httpsGateway = httpsGateway;
    }

    public String getHttpGateway() {
        return httpGateway;
    }

    public void setHttpGateway(String httpGateway) {
        this.httpGateway = httpGateway;
    }
}
