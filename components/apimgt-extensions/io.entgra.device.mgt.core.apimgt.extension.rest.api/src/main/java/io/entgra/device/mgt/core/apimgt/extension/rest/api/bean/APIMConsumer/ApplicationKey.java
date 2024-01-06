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
 * This class represents the Consumer Application key Information.
 */
public class ApplicationKey {

    private String keyMappingId;
    private String keyManager;
    private String consumerKey;
    private String consumerSecret;
    private List<String> supportedGrantTypes;
    private String callbackUrl;
    private String keyState;
    private String keyType;
    private String mode;
    private String groupId;
    private JSONObject token;
    private JSONObject additionalProperties;

    public String getKeyMappingId() {
        return keyMappingId;
    }

    public void setKeyMappingId(String keyMappingId) {
        this.keyMappingId = keyMappingId;
    }

    public String getKeyManager() {
        return keyManager;
    }

    public void setKeyManager(String keyManager) {
        this.keyManager = keyManager;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public List<String> getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public void setSupportedGrantTypes(List<String> supportedGrantTypes) {
        this.supportedGrantTypes = supportedGrantTypes;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getKeyState() {
        return keyState;
    }

    public void setKeyState(String keyState) {
        this.keyState = keyState;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public JSONObject getToken() {
        return token;
    }

    public void setToken(JSONObject token) {
        this.token = token;
    }

    public JSONObject getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(JSONObject additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
