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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer;

import org.json.JSONObject;

import java.util.List;

/**
 * This class represents the Consumer Key manager Information.
 */

public class KeyManager {

    private String id;
    private String name;
    private String type;
    private String displayName;
    private String description;
    private boolean enabled;
    private List<String> availableGrantTypes;
    private String tokenEndpoint;
    private String revokeEndpoint;
    private String userInfoEndpoint;
    private String enableTokenGeneration;
    private String enableTokenEncryption;
    private String enableTokenHashing;
    private String enableOAuthAppCreation;
    private String enableMapOAuthConsumerApps;
    private List<ApplicationConfigurations> applicationConfiguration;
    private JSONObject additionalProperties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAvailableGrantTypes() {
        return availableGrantTypes;
    }

    public void setAvailableGrantTypes(List<String> availableGrantTypes) {
        this.availableGrantTypes = availableGrantTypes;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getRevokeEndpoint() {
        return revokeEndpoint;
    }

    public void setRevokeEndpoint(String revokeEndpoint) {
        this.revokeEndpoint = revokeEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public String getEnableTokenGeneration() {
        return enableTokenGeneration;
    }

    public void setEnableTokenGeneration(String enableTokenGeneration) {
        this.enableTokenGeneration = enableTokenGeneration;
    }

    public String getEnableTokenEncryption() {
        return enableTokenEncryption;
    }

    public void setEnableTokenEncryption(String enableTokenEncryption) {
        this.enableTokenEncryption = enableTokenEncryption;
    }

    public String getEnableTokenHashing() {
        return enableTokenHashing;
    }

    public void setEnableTokenHashing(String enableTokenHashing) {
        this.enableTokenHashing = enableTokenHashing;
    }

    public String getEnableOAuthAppCreation() {
        return enableOAuthAppCreation;
    }

    public void setEnableOAuthAppCreation(String enableOAuthAppCreation) {
        this.enableOAuthAppCreation = enableOAuthAppCreation;
    }

    public String getEnableMapOAuthConsumerApps() {
        return enableMapOAuthConsumerApps;
    }

    public void setEnableMapOAuthConsumerApps(String enableMapOAuthConsumerApps) {
        this.enableMapOAuthConsumerApps = enableMapOAuthConsumerApps;
    }

    public List<ApplicationConfigurations> getApplicationConfiguration() {
        return applicationConfiguration;
    }

    public void setApplicationConfiguration(List<ApplicationConfigurations> applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    public JSONObject getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(JSONObject additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
