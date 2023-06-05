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

import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.ScopeUtils;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class represents the Consumer API Information.
 */

public class APIInfo {

    private String id;
    private String name;
    private String description;
    private String context;
    private String version;
    private String provider;
    private JSONObject apiDefinition;
    private String wsdlUri;
    private String lifeCycleStatus;
    private boolean isDefaultVersion;
    private String type;
    private Set<String> transport;
    private List<JSONObject> operations;
    private String authorizationHeader;
    private String securityScheme;
    private Set<String> tags;
    private List<JSONObject> tiers;
    private boolean hasThumbnail;
    private String additionalProperties;
    private JSONObject monetization;
    private List<JSONObject> endpointURLs;
    private JSONObject businessInformation;
    private List<JSONObject> environmentList;
    private List<ScopeUtils> scopes;
    private String avgRating;
    private JSONObject advertiseInfo;
    private boolean isSubscriptionAvailable;
    private List<JSONObject> categories;
    private List<String> keyManagers = new ArrayList();
    private String createdTime;
    private String lastUpdatedTime;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public JSONObject getApiDefinition() {
        return apiDefinition;
    }

    public void setApiDefinition(JSONObject apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    public String getWsdlUri() {
        return wsdlUri;
    }

    public void setWsdlUri(String wsdlUri) {
        this.wsdlUri = wsdlUri;
    }

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(String lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        isDefaultVersion = defaultVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getTransport() {
        return transport;
    }

    public void setTransport(Set<String> transport) {
        this.transport = transport;
    }

    public List<JSONObject> getOperations() {
        return operations;
    }

    public void setOperations(List<JSONObject> operations) {
        this.operations = operations;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getSecurityScheme() {
        return securityScheme;
    }

    public void setSecurityScheme(String securityScheme) {
        this.securityScheme = securityScheme;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public List<JSONObject> getTiers() {
        return tiers;
    }

    public void setTiers(List<JSONObject> tiers) {
        this.tiers = tiers;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    public String getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public JSONObject getMonetization() {
        return monetization;
    }

    public void setMonetization(JSONObject monetization) {
        this.monetization = monetization;
    }

    public List<JSONObject> getEndpointURLs() {
        return endpointURLs;
    }

    public void setEndpointURLs(List<JSONObject> endpointURLs) {
        this.endpointURLs = endpointURLs;
    }

    public JSONObject getBusinessInformation() {
        return businessInformation;
    }

    public void setBusinessInformation(JSONObject businessInformation) {
        this.businessInformation = businessInformation;
    }

    public List<JSONObject> getEnvironmentList() {
        return environmentList;
    }

    public void setEnvironmentList(List<JSONObject> environmentList) {
        this.environmentList = environmentList;
    }

    public List<ScopeUtils> getScopes() {
        return scopes;
    }

    public void setScopes(List<ScopeUtils> scopes) {
        this.scopes = scopes;
    }

    public String getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(String avgRating) {
        this.avgRating = avgRating;
    }

    public JSONObject getAdvertiseInfo() {
        return advertiseInfo;
    }

    public void setAdvertiseInfo(JSONObject advertiseInfo) {
        this.advertiseInfo = advertiseInfo;
    }

    public boolean isSubscriptionAvailable() {
        return isSubscriptionAvailable;
    }

    public void setSubscriptionAvailable(boolean subscriptionAvailable) {
        isSubscriptionAvailable = subscriptionAvailable;
    }

    public List<JSONObject> getCategories() {
        return categories;
    }

    public void setCategories(List<JSONObject> categories) {
        this.categories = categories;
    }

    public List<String> getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {
        this.keyManagers = keyManagers;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
