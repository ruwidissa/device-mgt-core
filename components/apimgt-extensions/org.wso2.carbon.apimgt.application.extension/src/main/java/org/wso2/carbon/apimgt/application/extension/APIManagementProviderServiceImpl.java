/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.application.extension;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.application.extension.bean.APIRegistrationProfile;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import org.wso2.carbon.apimgt.application.extension.util.APIManagerUtil;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an implementation of APIManagementProviderService.
 */
public class APIManagementProviderServiceImpl implements APIManagementProviderService {

    private static final Log log = LogFactory.getLog(APIManagementProviderServiceImpl.class);
    public static final APIManagerFactory API_MANAGER_FACTORY = APIManagerFactory.getInstance();

    @Override
    public boolean isTierLoaded() {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        try {
            APIUtil.getTiers(APIConstants.TIER_APPLICATION_TYPE, tenantDomain);
            return true;
        } catch (APIManagementException e) {
            log.error("APIs not ready", e);
        }

        return false;
    }

    @Override
    public void removeAPIApplication(String applicationName, String username) throws APIManagerException {

        try {
            APIConsumer apiConsumer = API_MANAGER_FACTORY.getAPIConsumer(username);
            Application application = apiConsumer.getApplicationsByName(username, applicationName, "");
            if (application != null) {
                apiConsumer.removeApplication(application, username);
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to remove api application : " + applicationName, e);
        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String tags[],
            String keyType, String username, boolean isAllowedAllDomains, String validityTime)
            throws APIManagerException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isEmpty(username)) {
            username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        }
        try {
            APIConsumer apiConsumer = API_MANAGER_FACTORY.getAPIConsumer(username);
            Application application = apiConsumer.getApplicationsByName(username, applicationName, "");

            int applicationId = 0;
            Subscriber subscriber = null;
            if (application == null) {
                subscriber = apiConsumer.getSubscriber(username);
                if (subscriber == null) {
                    // create subscriber
                    apiConsumer.addSubscriber(username, "");
                    subscriber = apiConsumer.getSubscriber(username);
                }
                //create application
                application = new Application(applicationName, subscriber);
                application.setTier(ApiApplicationConstants.DEFAULT_TIER);
                application.setGroupId("");
                application.setTokenType("OAUTH");
                apiConsumer.addApplication(application, username);
                application = apiConsumer.getApplicationsByName(username, applicationName, "");
            } else {
                subscriber = apiConsumer.getSubscriber(username);
            }

            Set<SubscribedAPI> subscribedAPIs =
                    apiConsumer.getSubscribedAPIs(subscriber, applicationName, "");

            log.info("Already subscribed API count: " + subscribedAPIs.size());

            // subscribe to apis.
            Set<String> tempApiIds = new HashSet<>();
            if (tags != null && tags.length > 0) {
                for (String tag : tags) {
                    Set<API> apisWithTag = apiConsumer.getAPIsWithTag(tag, tenantDomain);
                    if (apisWithTag == null || apisWithTag.size() == 0) {
                        apisWithTag = apiConsumer.getAPIsWithTag(tag, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    }

                    if (apisWithTag != null && apisWithTag.size() > 0) {
                        for (API apiInfo : apisWithTag) {
                            String id = apiInfo.getId().getProviderName().replace("@", "-AT-")
                                    + "-" + apiInfo.getId().getName() + "-" + apiInfo.getId().getVersion();
                            boolean subscriptionExist = false;
                            if (subscribedAPIs.size() > 0) {
                                for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                                    if (String.valueOf(subscribedAPI.getApiId().toString()).equals(id)) {
                                        subscriptionExist = true;
                                        break;
                                    }
                                }
                            }
                            if (!subscriptionExist && !tempApiIds.contains(id)) {
                                ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(
                                        apiInfo.getUuid(), tenantDomain);
                                apiTypeWrapper.setTier(ApiApplicationConstants.DEFAULT_TIER);

                                apiConsumer.addSubscription(apiTypeWrapper, username, application);
                                tempApiIds.add(id);
                            }
                        }
                    }
                }
            }
            //end of subscription

            List<APIKey> applicationKeys = application.getKeys();
            if (applicationKeys != null) {
                for (APIKey applicationKey : applicationKeys) {
                    if (keyType.equals(applicationKey.getType())) {
                        if (applicationKey.getConsumerKey() != null && !applicationKey.getConsumerKey().isEmpty()) {
                            ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                            apiApplicationKey.setConsumerKey(applicationKey.getConsumerKey());
                            apiApplicationKey.setConsumerSecret(applicationKey.getConsumerSecret());
                            return apiApplicationKey;
                        }
                    }
                }
            }

            List<String> allowedDomains = new ArrayList<>();
            if (isAllowedAllDomains) {
                allowedDomains.add(ApiApplicationConstants.ALLOWED_DOMAINS);
            } else {
                allowedDomains.add(APIManagerUtil.getTenantDomain());
            }

            APIAdmin apiAdmin = new APIAdminImpl();
            String keyManagerId = null;
            try {
                List<KeyManagerConfigurationDTO> keyManagerConfigurations = apiAdmin
                        .getKeyManagerConfigurationsByTenant(tenantDomain);
                if (keyManagerConfigurations != null) {
                    for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurations) {
                        keyManagerId = keyManagerConfigurationDTO.getUuid();
                    }
                }
                String jsonString = "{\"grant_types\":\"refresh_token,urn:ietf:params:oauth:grant-type:saml2-bearer," +
                        "password,client_credentials,iwa:ntlm,urn:ietf:params:oauth:grant-type:jwt-bearer\"," +
                        "\"additionalProperties\":\"{\\\"application_access_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"user_access_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"refresh_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"id_token_expiry_time\\\":\\\"N\\/A\\\"}\"," +
                        "\"username\":\"" + username + "\"}";

                Map<String, Object> keyDetails = apiConsumer
                        .requestApprovalForApplicationRegistration(username, applicationName, keyType, "",
                                allowedDomains.toArray(new String[allowedDomains.size()]), validityTime, "default", "",
                                jsonString, keyManagerId, tenantDomain);

                if (keyDetails != null) {
                    ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                    apiApplicationKey.setConsumerKey((String) keyDetails.get("consumerKey"));
                    apiApplicationKey.setConsumerSecret((String) keyDetails.get("consumerSecret"));
                    return apiApplicationKey;
                }
                throw new APIManagerException("Failed to generate keys for tenant: " + tenantDomain);
            } catch (APIManagementException e) {
                throw new APIManagerException("Failed to create api application for tenant: " + tenantDomain, e);
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to create api application for tenant: " + tenantDomain, e);
        }
    }

    @Override
    public AccessTokenInfo getAccessToken(String scopes, String[] tags, String applicationName, String tokenType,
            String validityPeriod) throws APIManagerException {
        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            ApiApplicationKey clientCredentials = getClientCredentials(tenantDomain, tags, applicationName, tokenType,
                    validityPeriod);

            if (clientCredentials == null) {
                String msg = "Oauth Application creation is failed.";
                log.error(msg);
                throw new APIManagerException(msg);
            }

            String user =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername() + "@" + PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().getTenantDomain(true);

            JWTClientManagerService jwtClientManagerService = APIApplicationManagerExtensionDataHolder.getInstance()
                    .getJwtClientManagerService();
            JWTClient jwtClient = jwtClientManagerService.getJWTClient();
            AccessTokenInfo accessTokenForAdmin = jwtClient
                    .getAccessToken(clientCredentials.getConsumerKey(), clientCredentials.getConsumerSecret(), user,
                            scopes);

            return accessTokenForAdmin;
        } catch (JWTClientException e) {
            String msg = "JWT Error occurred while registering Application to get access token.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (APIManagerException e) {
            String msg = "Error occurred while getting access tokens.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (UserStoreException e) {
            String msg = "User management exception when getting client credentials.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }

    /**
     * Get Client credentials
     * @param tenantDomain Tenant Domain
     * @param tags Tags
     * @param applicationName Application Name
     * @param tokenType Token Type
     * @param validityPeriod Validity Period
     * @return {@link ApiApplicationKey}
     * @throws APIManagerException if error occurred while generating access token
     * @throws UserStoreException if error ocurred while getting admin username.
     */
    private ApiApplicationKey getClientCredentials(String tenantDomain, String[] tags, String applicationName,
            String tokenType, String validityPeriod) throws APIManagerException, UserStoreException {

        APIRegistrationProfile registrationProfile = new APIRegistrationProfile();
        registrationProfile.setAllowedToAllDomains(false);
        registrationProfile.setMappingAnExistingOAuthApp(false);
        registrationProfile.setTags(tags);
        registrationProfile.setApplicationName(applicationName);

        ApiApplicationKey info = null;
        if (tenantDomain == null || tenantDomain.isEmpty()) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                            .getAdminUserName());

            if (registrationProfile.getUsername() == null || registrationProfile.getUsername().isEmpty()) {
                info = generateAndRetrieveApplicationKeys(registrationProfile.getApplicationName(),
                        registrationProfile.getTags(), tokenType, null,
                        registrationProfile.isAllowedToAllDomains(), validityPeriod);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return info;
    }
}
