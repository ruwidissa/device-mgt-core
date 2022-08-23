/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyManagerPayload {
    private String name;
    private String displayName;
    private String type;
    private String description;
    private String wellKnownEndpoint;
    private String introspectionEndpoint;
    private String clientRegistrationEndpoint;
    private String tokenEndpoint;
    private String displayTokenEndpoint;
    private String revokeEndpoint;
    private String displayRevokeEndpoint;
    private String userInfoEndpoint;
    private String authorizeEndpoint;
    private Map<String, String> certificates;
    private String issuer;
    private String scopeManagementEndpoint;
    private List<String> availableGrantTypes;
    private boolean enableTokenGeneration;
    private boolean enableTokenEncryption;
    private boolean enableTokenHashing;
    private boolean enableMapOAuthConsumerApps;
    private boolean enableOAuthAppCreation;
    private boolean enableSelfValidationJWT;
    private List<String> claimMapping;
    private String consumerKeyClaim;
    private String scopesClaim;
    private List<Map<String, String>> tokenValidation;
    private boolean enabled;
    private Map<String, Object> additionalProperties;

    public KeyManagerPayload(String domainName, int tenantId, String serverUrl, String name,
                             List<String> availableGrantTypes, Map<String, Object> additionalProperties) {
        this.name = name;
        this.displayName = name;
        this.type = KeyMgtConstants.CUSTOM_TYPE;
        this.description = "Custom Key Manager";
        this.wellKnownEndpoint = null;
        this.introspectionEndpoint = serverUrl + KeyMgtConstants.INTROSPECT_ENDPOINT;
        this.clientRegistrationEndpoint = serverUrl + "/t/" + domainName + KeyMgtConstants.CLIENT_REGISTRATION_ENDPOINT;
        this.tokenEndpoint = serverUrl + KeyMgtConstants.OAUTH2_TOKEN_ENDPOINT;
        this.displayTokenEndpoint = serverUrl + KeyMgtConstants.OAUTH2_TOKEN_ENDPOINT;
        this.revokeEndpoint = serverUrl + KeyMgtConstants.REVOKE_ENDPOINT;
        this.displayRevokeEndpoint = serverUrl + KeyMgtConstants.REVOKE_ENDPOINT;
        this.userInfoEndpoint = serverUrl + KeyMgtConstants.USER_INFO_ENDPOINT;
        this.authorizeEndpoint = serverUrl + KeyMgtConstants.AUTHORIZE_ENDPOINT;

        Map<String, String> certificates = new HashMap<>();
        certificates.put("type", "JWKS");
        certificates.put("value", serverUrl + "/t/" + domainName + KeyMgtConstants.JWKS_ENDPOINT);
        this.certificates = certificates;

        this.issuer = serverUrl + "/t/" + domainName + KeyMgtConstants.OAUTH2_TOKEN_ENDPOINT;
        this.scopeManagementEndpoint = serverUrl + "/t/" + domainName + KeyMgtConstants.SCOPE_MANAGEMENT_ENDPOINT;
        this.availableGrantTypes = availableGrantTypes;
        this.enableTokenGeneration = true;
        this.enableTokenEncryption = false;
        this.enableTokenHashing = false;
        this.enableMapOAuthConsumerApps = true;
        this.enableOAuthAppCreation = true;
        this.enableSelfValidationJWT = true;
        this.claimMapping = new ArrayList<>();
        this.consumerKeyClaim = KeyMgtConstants.CONSUMER_KEY_CLAIM;
        this.scopesClaim = KeyMgtConstants.SCOPE_CLAIM;

        List<Map<String, String>> tokenValidationList = new ArrayList<>();
        Map<String, String> tokenValidation = new HashMap<>();
        tokenValidation.put("type", KeyMgtConstants.REFERENCE);
        tokenValidation.put("value", KeyMgtConstants.TOKEN_REGEX.replaceAll("<<tenantId>>", String.valueOf(tenantId)));
        tokenValidationList.add(tokenValidation);
        this.tokenValidation = tokenValidationList;

        this.enabled = true;
        this.additionalProperties = additionalProperties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWellKnownEndpoint() {
        return wellKnownEndpoint;
    }

    public void setWellKnownEndpoint(String wellKnownEndpoint) {
        this.wellKnownEndpoint = wellKnownEndpoint;
    }

    public String getIntrospectionEndpoint() {
        return introspectionEndpoint;
    }

    public void setIntrospectionEndpoint(String introspectionEndpoint) {
        this.introspectionEndpoint = introspectionEndpoint;
    }

    public String getClientRegistrationEndpoint() {
        return clientRegistrationEndpoint;
    }

    public void setClientRegistrationEndpoint(String clientRegistrationEndpoint) {
        this.clientRegistrationEndpoint = clientRegistrationEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getDisplayTokenEndpoint() {
        return displayTokenEndpoint;
    }

    public void setDisplayTokenEndpoint(String displayTokenEndpoint) {
        this.displayTokenEndpoint = displayTokenEndpoint;
    }

    public String getRevokeEndpoint() {
        return revokeEndpoint;
    }

    public void setRevokeEndpoint(String revokeEndpoint) {
        this.revokeEndpoint = revokeEndpoint;
    }

    public String getDisplayRevokeEndpoint() {
        return displayRevokeEndpoint;
    }

    public void setDisplayRevokeEndpoint(String displayRevokeEndpoint) {
        this.displayRevokeEndpoint = displayRevokeEndpoint;
    }

    public String getUserInfoEndpoint() {
        return userInfoEndpoint;
    }

    public void setUserInfoEndpoint(String userInfoEndpoint) {
        this.userInfoEndpoint = userInfoEndpoint;
    }

    public String getAuthorizeEndpoint() {
        return authorizeEndpoint;
    }

    public void setAuthorizeEndpoint(String authorizeEndpoint) {
        this.authorizeEndpoint = authorizeEndpoint;
    }

    public Map<String, String> getCertificates() {
        return certificates;
    }

    public void setCertificates(Map<String, String> certificates) {
        this.certificates = certificates;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getScopeManagementEndpoint() {
        return scopeManagementEndpoint;
    }

    public void setScopeManagementEndpoint(String scopeManagementEndpoint) {
        this.scopeManagementEndpoint = scopeManagementEndpoint;
    }

    public List<String> getAvailableGrantTypes() {
        return availableGrantTypes;
    }

    public void setAvailableGrantTypes(List<String> availableGrantTypes) {
        this.availableGrantTypes = availableGrantTypes;
    }

    public boolean isEnableTokenGeneration() {
        return enableTokenGeneration;
    }

    public void setEnableTokenGeneration(boolean enableTokenGeneration) {
        this.enableTokenGeneration = enableTokenGeneration;
    }

    public boolean isEnableTokenEncryption() {
        return enableTokenEncryption;
    }

    public void setEnableTokenEncryption(boolean enableTokenEncryption) {
        this.enableTokenEncryption = enableTokenEncryption;
    }

    public boolean isEnableTokenHashing() {
        return enableTokenHashing;
    }

    public void setEnableTokenHashing(boolean enableTokenHashing) {
        this.enableTokenHashing = enableTokenHashing;
    }

    public boolean isEnableMapOAuthConsumerApps() {
        return enableMapOAuthConsumerApps;
    }

    public void setEnableMapOAuthConsumerApps(boolean enableMapOAuthConsumerApps) {
        this.enableMapOAuthConsumerApps = enableMapOAuthConsumerApps;
    }

    public boolean isEnableOAuthAppCreation() {
        return enableOAuthAppCreation;
    }

    public void setEnableOAuthAppCreation(boolean enableOAuthAppCreation) {
        this.enableOAuthAppCreation = enableOAuthAppCreation;
    }

    public boolean isEnableSelfValidationJWT() {
        return enableSelfValidationJWT;
    }

    public void setEnableSelfValidationJWT(boolean enableSelfValidationJWT) {
        this.enableSelfValidationJWT = enableSelfValidationJWT;
    }

    public List<String> getClaimMapping() {
        return claimMapping;
    }

    public void setClaimMapping(List<String> claimMapping) {
        this.claimMapping = claimMapping;
    }

    public String getConsumerKeyClaim() {
        return consumerKeyClaim;
    }

    public void setConsumerKeyClaim(String consumerKeyClaim) {
        this.consumerKeyClaim = consumerKeyClaim;
    }

    public String getScopesClaim() {
        return scopesClaim;
    }

    public void setScopesClaim(String scopesClaim) {
        this.scopesClaim = scopesClaim;
    }

    public List<Map<String, String>> getTokenValidation() {
        return tokenValidation;
    }

    public void setTokenValidation(List<Map<String, String>> tokenValidation) {
        this.tokenValidation = tokenValidation;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
