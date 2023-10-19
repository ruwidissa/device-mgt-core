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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo;

import org.json.JSONObject;

import java.util.List;

/**
 * This class represents the API response.
 */

public class APIInfo {

    private String id;
    private String name;
    private String description;
    private String context;
    private String version;
    private String provider;
    private String lifeCycleStatus;
    private type wsdlInfo;
    private String wsdlUrl;
    private boolean responseCachingEnabled;
    private int cacheTimeout;
    private boolean hasThumbnail;
    private boolean isDefaultVersion;
    private boolean isRevision;
    private String revisionedApiId;
    private int revisionId;
    private boolean enableSchemaValidation;
    private boolean enableStore;
    private String type;
    private List<String> transport;
    private List<String> tags;
    private List<String> policies;
    private String apiThrottlingPolicy;
    private String authorizationHeader;
    private List<String> securityScheme;
    private APIMaxTps maxTps;
    private String visibility;
    private List<String> visibleRoles;
    private List<String> visibleTenants;
    private List<MediationPolicy> mediationPolicies;
    private String subscriptionAvailability;
    private List<String> subscriptionAvailableTenants;
    private List<AdditionalProperties> additionalProperties;
    private Monetization monetization;
    private String accessControl;
    private List<String> accessControlRoles;
    private BusinessInformation businessInformation;
    private CORSConfiguration corsConfiguration;
    private WebsubSubscriptionConfiguration websubSubscriptionConfiguration;
    private String workflowStatus;
    private String createdTime;
    private String lastUpdatedTime;
    private JSONObject endpointConfig;
    private String endpointImplementationType;
    private List<JSONObject> scopes;
    private List<Operations> operations;
    private JSONObject threatProtectionPolicies;
    private List<String> categories;
    private List<String> keyManagers;
    private ServiceInfo serviceInfo;
    private AdvertiseInfo advertiseInfo;

    public enum type {
        WSDL, ZIP
    }

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

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(String lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public APIInfo.type getWsdlInfo() {
        return wsdlInfo;
    }

    public void setWsdlInfo(APIInfo.type wsdlInfo) {
        this.wsdlInfo = wsdlInfo;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public boolean isResponseCachingEnabled() {
        return responseCachingEnabled;
    }

    public void setResponseCachingEnabled(boolean responseCachingEnabled) {
        this.responseCachingEnabled = responseCachingEnabled;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        isDefaultVersion = defaultVersion;
    }

    public boolean isRevision() {
        return isRevision;
    }

    public void setRevision(boolean revision) {
        isRevision = revision;
    }

    public String getRevisionedApiId() {
        return revisionedApiId;
    }

    public void setRevisionedApiId(String revisionedApiId) {
        this.revisionedApiId = revisionedApiId;
    }

    public int getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(int revisionId) {
        this.revisionId = revisionId;
    }

    public boolean isEnableSchemaValidation() {
        return enableSchemaValidation;
    }

    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
    }

    public boolean isEnableStore() {
        return enableStore;
    }

    public void setEnableStore(boolean enableStore) {
        this.enableStore = enableStore;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTransport() {
        return transport;
    }

    public void setTransport(List<String> transport) {
        this.transport = transport;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

    public String getApiThrottlingPolicy() {
        return apiThrottlingPolicy;
    }

    public void setApiThrottlingPolicy(String apiThrottlingPolicy) {
        this.apiThrottlingPolicy = apiThrottlingPolicy;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public List<String> getSecurityScheme() {
        return securityScheme;
    }

    public void setSecurityScheme(List<String> securityScheme) {
        this.securityScheme = securityScheme;
    }

    public APIMaxTps getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(APIMaxTps maxTps) {
        this.maxTps = maxTps;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(List<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public List<String> getVisibleTenants() {
        return visibleTenants;
    }

    public void setVisibleTenants(List<String> visibleTenants) {
        this.visibleTenants = visibleTenants;
    }

    public List<MediationPolicy> getMediationPolicies() {
        return mediationPolicies;
    }

    public void setMediationPolicies(List<MediationPolicy> mediationPolicies) {
        this.mediationPolicies = mediationPolicies;
    }

    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }

    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    public List<String> getSubscriptionAvailableTenants() {
        return subscriptionAvailableTenants;
    }

    public void setSubscriptionAvailableTenants(List<String> subscriptionAvailableTenants) {
        this.subscriptionAvailableTenants = subscriptionAvailableTenants;
    }

    public List<AdditionalProperties> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(List<AdditionalProperties> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public Monetization getMonetization() {
        return monetization;
    }

    public void setMonetization(Monetization monetization) {
        this.monetization = monetization;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public List<String> getAccessControlRoles() {
        return accessControlRoles;
    }

    public void setAccessControlRoles(List<String> accessControlRoles) {
        this.accessControlRoles = accessControlRoles;
    }

    public BusinessInformation getBusinessInformation() {
        return businessInformation;
    }

    public void setBusinessInformation(BusinessInformation businessInformation) {
        this.businessInformation = businessInformation;
    }

    public CORSConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CORSConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public WebsubSubscriptionConfiguration getWebsubSubscriptionConfiguration() {
        return websubSubscriptionConfiguration;
    }

    public void setWebsubSubscriptionConfiguration(WebsubSubscriptionConfiguration websubSubscriptionConfiguration) {
        this.websubSubscriptionConfiguration = websubSubscriptionConfiguration;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
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

    public JSONObject getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(JSONObject endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getEndpointImplementationType() {
        return endpointImplementationType;
    }

    public void setEndpointImplementationType(String endpointImplementationType) {
        this.endpointImplementationType = endpointImplementationType;
    }

    public List<JSONObject> getScopes() {
        return scopes;
    }

    public void setScopes(List<JSONObject> scopes) {
        this.scopes = scopes;
    }

    public List<Operations> getOperations() {
        return operations;
    }

    public void setOperations(List<Operations> operations) {
        this.operations = operations;
    }

    public JSONObject getThreatProtectionPolicies() {
        return threatProtectionPolicies;
    }

    public void setThreatProtectionPolicies(JSONObject threatProtectionPolicies) {
        this.threatProtectionPolicies = threatProtectionPolicies;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {
        this.keyManagers = keyManagers;
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public AdvertiseInfo getAdvertiseInfo() {
        return advertiseInfo;
    }

    public void setAdvertiseInfo(AdvertiseInfo advertiseInfo) {
        this.advertiseInfo = advertiseInfo;
    }
}
