package org.wso2.carbon.device.mgt.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;

import java.util.List;

@ApiModel(value = "DeviceConfig", description = "Device config")
public class DeviceConfig {
    private String clientId;
    private String clientSecret;
    private String deviceId;
    private String type;
    private String accessToken;
    private String refreshToken;
    private String mqttGateway;
    private String httpsGateway;
    private String httpGateway;
    private PlatformConfiguration platformConfiguration;
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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

    public PlatformConfiguration getPlatformConfiguration() {
        return platformConfiguration;
    }

    public void setPlatformConfiguration(PlatformConfiguration platformConfiguration) {
        this.platformConfiguration = platformConfiguration;
    }
}
