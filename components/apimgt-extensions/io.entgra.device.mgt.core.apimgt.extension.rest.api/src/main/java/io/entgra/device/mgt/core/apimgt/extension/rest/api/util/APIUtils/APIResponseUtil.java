/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.apimgt.extension.rest.api.util.APIUtils;

import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.WebsubSubscriptionConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class represents the API response.
 */

public class APIResponseUtil {

    private String id;
    private String name;
    private String description;
    private String context;
    private String version;
    private String provider;
    private String lifeCycleStatus;
    private String wsdlInfo;
    private String wsdlUrl;
    private boolean responseCachingEnabled;
    private int cacheTimeout;
    private boolean hasThumbnail;
    private boolean isDefaultVersion;
    private boolean isRevision;
    private  String revisionedApiId;
    private int revisionId;
    private boolean enableSchemaValidation;
    private String type;
    private Set<String> transport;
    private Set<String> tags;
    private Set<String> policies;
    private String apiThrottlingPolicy;
    private String authorizationHeader;
    private String securityScheme;
    private String maxTps;
    private String visibility;
    private String visibleRoles;
    private String visibleTenants;
    private String mediationPolicies;
    private String subscriptionAvailability;
    private String subscriptionAvailableTenants;
    private String additionalProperties;
    private String monetization;
    private String accessControl;
    private String accessControlRoles;
    private BusinessInformation businessInformation;
    private CORSConfiguration corsConfiguration;
    private WebsubSubscriptionConfiguration websubSubscriptionConfiguration;
    private String workflowStatus;
    private String createdTime;
    private String lastUpdatedTime;
    private JSONObject endpointConfig = new JSONObject();
    private String endpointImplementationType;
    private List<JSONObject> scopes = new ArrayList();
    private List<JSONObject> operations;
    private String threatProtectionPolicies;
    private List<APICategory> apiCategories;
    private List<String> keyManagers = new ArrayList();
    private JSONObject serviceInfo = new JSONObject();
    private AdvertiseInfo advertiseInfo;

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

    public String getWsdlInfo() {
        return wsdlInfo;
    }

    public void setWsdlInfo(String wsdlInfo) {
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

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getPolicies() {
        return policies;
    }

    public void setPolicies(Set<String> policies) {
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

    public String getSecurityScheme() {
        return securityScheme;
    }

    public void setSecurityScheme(String securityScheme) {
        this.securityScheme = securityScheme;
    }

    public String getMaxTps() {
        return maxTps;
    }

    public void setMaxTps(String maxTps) {
        this.maxTps = maxTps;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(String visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public String getVisibleTenants() {
        return visibleTenants;
    }

    public void setVisibleTenants(String visibleTenants) {
        this.visibleTenants = visibleTenants;
    }

    public String getMediationPolicies() {
        return mediationPolicies;
    }

    public void setMediationPolicies(String mediationPolicies) {
        this.mediationPolicies = mediationPolicies;
    }

    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }

    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    public String getSubscriptionAvailableTenants() {
        return subscriptionAvailableTenants;
    }

    public void setSubscriptionAvailableTenants(String subscriptionAvailableTenants) {
        this.subscriptionAvailableTenants = subscriptionAvailableTenants;
    }

    public String getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(String additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getMonetization() {
        return monetization;
    }

    public void setMonetization(String monetization) {
        this.monetization = monetization;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public String getAccessControlRoles() {
        return accessControlRoles;
    }

    public void setAccessControlRoles(String accessControlRoles) {
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

    public List<JSONObject>getScopes() {
        return scopes;
    }

    public void setScopes(List<JSONObject> scopes) {
        this.scopes = scopes;
    }

    public List<JSONObject> getOperations() {
        return operations;
    }

    public void setOperations(List<JSONObject> operations) {
        this.operations = operations;
    }

    public String getThreatProtectionPolicies() {
        return threatProtectionPolicies;
    }

    public void setThreatProtectionPolicies(String threatProtectionPolicies) {
        this.threatProtectionPolicies = threatProtectionPolicies;
    }

    public List<APICategory> getApiCategories() {
        return apiCategories;
    }

    public void setApiCategories(List<APICategory> apiCategories) {
        this.apiCategories = apiCategories;
    }

    public List<String> getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {
        this.keyManagers = keyManagers;
    }

    public JSONObject getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(JSONObject serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public AdvertiseInfo getAdvertiseInfo() {
        return advertiseInfo;
    }

    public void setAdvertiseInfo(AdvertiseInfo advertiseInfo) {
        this.advertiseInfo = advertiseInfo;
    }
}
