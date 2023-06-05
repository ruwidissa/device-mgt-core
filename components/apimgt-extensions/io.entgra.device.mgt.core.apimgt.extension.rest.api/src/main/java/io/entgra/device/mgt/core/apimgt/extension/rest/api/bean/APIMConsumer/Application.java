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
 * This class represents the Consumer Application Information.
 */

public class Application {
    private String applicationId;
    private String name;
    private String throttlingPolicy;
    private String description;
    private String tokenType;
    private String status;
    private List<String> groups;
    private int subscriptionCount;
    private List<String> keys;
    private JSONObject attributes;
    private List<String> subscriptionScopes;
    private String owner;
    private boolean hashEnabled;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThrottlingPolicy() {
        return throttlingPolicy;
    }

    public void setThrottlingPolicy(String throttlingPolicy) {
        this.throttlingPolicy = throttlingPolicy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    public void setSubscriptionCount(int subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public JSONObject getAttributes() {
        return attributes;
    }

    public void setAttributes(JSONObject attributes) {
        this.attributes = attributes;
    }

    public List<String> getSubscriptionScopes() {
        return subscriptionScopes;
    }

    public void setSubscriptionScopes(List<String> subscriptionScopes) {
        this.subscriptionScopes = subscriptionScopes;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isHashEnabled() {
        return hashEnabled;
    }

    public void setHashEnabled(boolean hashEnabled) {
        this.hashEnabled = hashEnabled;
    }
}
