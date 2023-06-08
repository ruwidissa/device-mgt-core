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

package io.entgra.device.mgt.core.apimgt.application.extension;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.ApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Subscription;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.ApiApplicationInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
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
import io.entgra.device.mgt.core.apimgt.application.extension.bean.APIRegistrationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.constants.ApiApplicationConstants;
import io.entgra.device.mgt.core.apimgt.application.extension.dto.ApiApplicationKey;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import io.entgra.device.mgt.core.apimgt.application.extension.util.APIManagerUtil;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.identity.jwt.client.extension.JWTClient;
import io.entgra.device.mgt.core.identity.jwt.client.extension.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.*;

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
            Application application = null; // todo:apim - apiConsumer.getApplicationsByName(username, applicationName, "");
//            curl -k -H "Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8" "https://localhost:9443/api/am/devportal/v3/applications?query=CalculatorApp"
            if (application != null) {
                // todo:apim - apiConsumer.removeApplication(application, username);
                //curl -k -H "Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8" -X DELETE "https://localhost:9443/api/am/devportal/v3/applications/896658a0-b4ee-4535-bbfa-806c894a4015"
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to remove api application : " + applicationName, e);
        }


    }

    @Override
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String tags[],
                                                                             String keyType, String username,
                                                                             boolean isAllowedAllDomains,
                                                                             String validityTime, String password)
            throws APIManagerException {

        APIApplicationServices apiApplicationServices = APIApplicationManagerExtensionDataHolder.getInstance()
                .getApiApplicationServices();
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();

        ApiApplicationInfo applicationInfo = applicationInfo(apiApplicationServices, username, password);
        try {
            io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application[] applications =
                    consumerRESTAPIServices.getAllApplications(applicationInfo, applicationName);
            io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application application;

            List<APIInfo> uniqueApiList = new ArrayList<>();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);

            Map<String, String> headerParams = new HashMap<>();
            if (!"carbon.super".equals(tenantDomain)) {
                headerParams.put("X-WSO2-Tenant", "carbon.super");
            }

            for (String tag : tags) {
                Map<String, String> queryParams = new HashMap<>();
                queryParams.put("tag", tag);

                APIInfo[] apiInfos = consumerRESTAPIServices.getAllApis(applicationInfo, queryParams, headerParams);

                uniqueApiList.addAll(List.of(apiInfos));
                Set<APIInfo> taggedAPISet = new HashSet<>(uniqueApiList);
                uniqueApiList.clear();
                uniqueApiList.addAll(taggedAPISet);
            }

            if (applications.length == 0) {
                application = new io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application();
                application.setName(applicationName);
                application = consumerRESTAPIServices.createApplication(applicationInfo, application);
                addSubscriptions(application, uniqueApiList, applicationInfo);
            } else {
                if (applications.length == 1) {
                    Optional<io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application> applicationOpt =
                            Arrays.stream(applications).findFirst();
                    application = applicationOpt.get();
                    Subscription[] subscriptions = consumerRESTAPIServices.getAllSubscriptions(applicationInfo, application.getApplicationId());
                    for (Subscription subscription : subscriptions) {
                        if (uniqueApiList.contains(subscription.getApiInfo())) {
                            uniqueApiList.remove(subscription.getApiInfo());
                        } else {
                            uniqueApiList.add(subscription.getApiInfo());
                        }
                    }
                    addSubscriptions(application, uniqueApiList, applicationInfo);
                } else {
                    String msg = "Found more than one application for application name: " + applicationName;
                    log.error(msg);
                    throw new APIManagerException(msg);
                }
            }

            if (application != null)  {
                if (!application.getKeys().isEmpty()) {
                    //todo return Application Keys
                    return null;
                } else{

                    ApplicationKey applicationKey = consumerRESTAPIServices.generateApplicationKeys(applicationInfo, application);
                    ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                    apiApplicationKey.setConsumerKey(applicationKey.getConsumerKey());
                    apiApplicationKey.setConsumerSecret(applicationKey.getConsumerSecret());
                    return apiApplicationKey;
                }
            } else{
                String msg = "Application retrieval process failed.";
                log.error(msg);
                throw new APIManagerException(msg);
            }
        } catch (APIServicesException e) {
            String msg = "Error occurred while processing the response of APIM REST endpoints.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (BadRequestException e) {
            String msg = "Provided incorrect payload when invoking APIM REST endpoints.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (UnexpectedResponseException e) {
            String msg = "Error occurred while invoking APIM REST endpoints.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }

    /**
     *
     * This method can be used to add a new subscriptions providing the ids of the APIs and the applications.
     *
     * @param application {@link io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application}
     * @param apiInfos {@link List<APIInfo>}
     * @param apiApplicationInfo {@link ApiApplicationInfo}
     *
     * @throws BadRequestException if incorrect data provided to call subscribing REST API.
     * @throws UnexpectedResponseException if error occurred while processing the subscribing REST API.
     * @throws APIServicesException if error occurred while invoking the subscribing REST API.
     */
    private void addSubscriptions(
            io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application application,
            List<APIInfo> apiInfos, ApiApplicationInfo apiApplicationInfo)
            throws BadRequestException, UnexpectedResponseException, APIServicesException {
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();

        List<Subscription> subscriptionList = new ArrayList<>();
        apiInfos.forEach(apiInfo -> {
            Subscription subscription = new Subscription();
            subscription.setApiId(apiInfo.getId());
            subscription.setApplicationId(application.getApplicationId());
            subscriptionList.add(subscription);
        });
        consumerRESTAPIServices.createSubscriptions(apiApplicationInfo, subscriptionList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String tags[],
            String keyType, String username, boolean isAllowedAllDomains, String validityTime)
            throws APIManagerException {


/*

todo - Modify generateAndRetrieveApplicationKeys

Check the existence of the API application.

if Application is not exists
    Create the Application

If super tenants
    Get set of tagged APIs
If the tenant domain is not super tenant
    Get set of tagged APIs from super tenant space

If new Application
    Subscribed to tagged APIs
Else
    Get all subscribed APIs of application
    Filter out APIs and subscribed to APIs which can be subscribed
        Filter ->   Use set of tagged APis
                    Remove already subscribed APIs from the set
                    Subscribed to remaining APIs

Get Application keys from application
    If API keys are there return API keys

Otherwise, Generate Application Keys and return them

 */

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (StringUtils.isEmpty(username)) {
            username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername() + "@" + tenantDomain;
        }
        try {
            APIConsumer apiConsumer = API_MANAGER_FACTORY.getAPIConsumer(username);
            Application application = null; // todo:apim - apiConsumer.getApplicationsByName(username, applicationName, "");
//            cnt rm
//            //            curl -k -H "Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8" "https://localhost:9443/api/am/devportal/v3/applications?query=CalculatorApp"

            int applicationId = 0;
            Subscriber subscriber = null;
            if (application == null) {
                subscriber = null; // todo:apim - apiConsumer.getSubscriber(username);
//                cnt rm
                if (subscriber == null) {
                    // create subscriber
                    // todo:apim - apiConsumer.addSubscriber(username, "");
//                    cnt rm
                    subscriber = null; // todo:apim - apiConsumer.getSubscriber(username);
//                    cnt rm
                }
                //create application
                application = new Application(applicationName, subscriber);
                application.setTier(ApiApplicationConstants.DEFAULT_TIER);
                application.setGroupId("");
                application.setTokenType("OAUTH");
                // todo:apim - apiConsumer.addApplication(application, username);
//                cnt rm
                application = null; // todo:apim - apiConsumer.getApplicationsByName(username, applicationName, "");
//                cnt rm
            } else {
                subscriber = null; // todo:apim - apiConsumer.getSubscriber(username);
//                cnt rm
            }

            Set<SubscribedAPI> subscribedAPIs =
                    null; // todo:apim - apiConsumer.getSubscribedAPIs(subscriber, applicationName, "");

            //curl -k -H "Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8" "https://localhost:9443/api/am/devportal/v3/subscriptions?apiId=02e658e7-71c7-4b1d-a623-be145b789340"
//            cnt rm

            log.info("Already subscribed API count: " + subscribedAPIs.size());

            // subscribe to apis.
            APIConsumer apiConsumerAPIPublishedTenant = apiConsumer;
            if (tags != null && tags.length > 0) {
                for (String tag : tags) {
                    boolean startedTenantFlow = false;
                    Set<API> apisWithTag = null; // todo:apim - apiConsumer.getAPIsWithTag(tag, tenantDomain);
//                    curl -k "https://localhost:9443/api/am/devportal/v3/apis"
//                    cnt rm

                    /**
                     * From APIM 4.0.0, APIs published in the super tenant can only be listed by
                     * APIConsumer, only if the APIConsumer belongs to the super tenant. So we
                     * are starting tenant flow if we are not already in super tenant(child
                     * tenant starting to create OAuth app).
                     */
                    if (apisWithTag == null || apisWithTag.size() == 0) {
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                                true);

                        try {
                            String superAdminUsername = PrivilegedCarbonContext
                                    .getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration().getAdminUserName();
                            apiConsumerAPIPublishedTenant = API_MANAGER_FACTORY.getAPIConsumer(superAdminUsername);
                        } catch (UserStoreException e) {
                            throw new APIManagerException("Failed to create api application for " +
                                    "tenant: " + tenantDomain +
                                    ". Caused by to inability to get super tenant username", e);
                        }

                        apisWithTag = null; // todo:apim - apiConsumerAPIPublishedTenant.getAPIsWithTag(tag, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
//                        cnt rm
                        startedTenantFlow = true;
                    }

                    Set<ApiTypeWrapper>  apiTypeWrapperList = new HashSet<>();
                    if (apisWithTag != null && apisWithTag.size() > 0) {
                        Set<String> tempApiIds = new HashSet<>();
                        for (API apiInfo : apisWithTag) {
                            String id = apiInfo.getId().getProviderName().replace("@", "-AT-")
                                    + "-" + apiInfo.getId().getName() + "-" + apiInfo.getId().getVersion();
                            boolean subscriptionExist = false;
                            if (subscribedAPIs.size() > 0) {
                                for (SubscribedAPI subscribedAPI : subscribedAPIs) {
                                    // todo:apim
//                                    cnt rm
//                                    if (String.valueOf(subscribedAPI.getApiId().toString()).equals(id)) {
//                                        subscriptionExist = true;
//                                        break;
//                                    }
                                }
                            }
                            if (!subscriptionExist && !tempApiIds.contains(id)) {
                                ApiTypeWrapper apiTypeWrapper;
                                if (startedTenantFlow) {
                                    /**
                                     * This mean APIs were not found in the child tenant, so all
                                     * calls to get info about APIs need to be to super tenant.
                                     */
                                    apiTypeWrapper = apiConsumerAPIPublishedTenant.getAPIorAPIProductByUUID(
                                            apiInfo.getUuid(), MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                                } else {
                                    /**
                                     * Ideally, in all usecases of IoT server, tenant domain here
                                     * will be carbon.super. This block is kept to make sure in
                                     * the future, if there are some APIs published to a specific
                                     * tenant only.
                                     */
                                    apiTypeWrapper = apiConsumerAPIPublishedTenant.getAPIorAPIProductByUUID(
                                            apiInfo.getUuid(), tenantDomain);
                                }
                                apiTypeWrapper.setTier(ApiApplicationConstants.DEFAULT_TIER);
                                apiTypeWrapperList.add(apiTypeWrapper);
                                tempApiIds.add(id);
                            }
                        }
                        if (startedTenantFlow) {
                            PrivilegedCarbonContext.endTenantFlow();
                        }

                        /** This is done in a redundant loop instead of doing in the same loop
                         * that populates apiTypeWrapperList because in a tenanted scenario,
                         * apiConsumerAPIPublishedTenant will belong to super tenant. So super
                         * tenant flow need to end before starting subscription to avoid adding
                         * subscriptions inside super tenant when we are trying to create an
                         * Oauth app for a child tenant.
                         */
                        for (ApiTypeWrapper apiTypeWrapper : apiTypeWrapperList) {
                            // todo:apim - apiConsumer.addSubscription(apiTypeWrapper, username, application);
//                            cnt rm
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
                List<KeyManagerConfigurationDTO> keyManagerConfigurations = null; // todo:apim -
                // apiAdmin.getKeyManagerConfigurationsByTenant(tenantDomain);
                if (keyManagerConfigurations != null) {
                    for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurations) {
                        keyManagerId = keyManagerConfigurationDTO.getUuid();
                    }
                }
                String applicationAccessTokenExpiryTime = "N/A";
                if (!StringUtils.isEmpty(validityTime)) {
                    applicationAccessTokenExpiryTime = validityTime;
                }
                String jsonString = "{\"grant_types\":\"refresh_token,access_token," +
                        "urn:ietf:params:oauth:grant-type:saml2-bearer," +
                        "password,client_credentials,iwa:ntlm,urn:ietf:params:oauth:grant-type:jwt-bearer\"," +
                        "\"additionalProperties\":\"{\\\"application_access_token_expiry_time\\\":\\\"" + applicationAccessTokenExpiryTime + "\\\"," +
                        "\\\"user_access_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"refresh_token_expiry_time\\\":\\\"N\\/A\\\"," +
                        "\\\"id_token_expiry_time\\\":\\\"N\\/A\\\"}\"," +
                        "\"username\":\"" + username + "\"}";

                Map<String, Object> keyDetails = null; // todo:apim - apiConsumer
//                        .requestApprovalForApplicationRegistration(username, applicationName, keyType, "",
//                                allowedDomains.toArray(new String[allowedDomains.size()]), validityTime, "default", "",
//                                jsonString, keyManagerId, tenantDomain);

                if (keyDetails != null) {
                    ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                    apiApplicationKey.setConsumerKey((String) keyDetails.get("consumerKey"));
                    apiApplicationKey.setConsumerSecret((String) keyDetails.get("consumerSecret"));
                    return apiApplicationKey;
                }
                throw new APIManagerException("Failed to generate keys for tenant: " + tenantDomain);
//            todo:apim - commected as it says never throw since we commented apim calls above
//                cnt rm
//             } catch (APIManagementException e) {
            } catch (Exception e) {
                throw new APIManagerException("Failed to create api application for tenant: " + tenantDomain, e);
            }
        } catch (APIManagementException e) {
            throw new APIManagerException("Failed to create api application for tenant: " + tenantDomain, e);
        }
    }

    @Override
    public AccessTokenInfo getAccessToken(String scopes, String[] tags, String applicationName, String tokenType,
            String validityPeriod, String username) throws APIManagerException {
        try {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
            ApiApplicationKey clientCredentials = getClientCredentials(tenantDomain, tags, applicationName, tokenType,
                    validityPeriod);

            if (clientCredentials == null) {
                String msg = "Oauth Application creation is failed.";
                log.error(msg);
                throw new APIManagerException(msg);
            }

            if (username == null || username.isEmpty()) {
                username =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername() + "@" + PrivilegedCarbonContext
                                .getThreadLocalCarbonContext().getTenantDomain(true);
            } else {
                if (!username.contains("@")) {
                    username += "@" + PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().getTenantDomain(true);
                }
            }

            JWTClientManagerService jwtClientManagerService = APIApplicationManagerExtensionDataHolder.getInstance()
                    .getJwtClientManagerService();
            JWTClient jwtClient = jwtClientManagerService.getJWTClient();
            AccessTokenInfo accessTokenForAdmin = jwtClient
                    .getAccessToken(clientCredentials.getConsumerKey(), clientCredentials.getConsumerSecret(), username,
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

    private ApiApplicationInfo applicationInfo(APIApplicationServices apiApplicationServices, String username, String password)
            throws APIManagerException {

        APIApplicationKey apiApplicationKey;
        io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo accessTokenInfo;
        try {
            if (username == null && password == null) {
                apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials();
            } else {
                apiApplicationKey = apiApplicationServices.generateAndRetrieveApplicationKeys(username, password);
            }
            accessTokenInfo = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
                    apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
        } catch (APIServicesException e) {
            String errorMsg = "Error occurred while generating the API application";
            log.error(errorMsg, e);
            throw new APIManagerException(errorMsg, e);
        }

        ApiApplicationInfo applicationInfo = null;
        applicationInfo.setClientId(apiApplicationKey.getClientId());
        applicationInfo.setClientSecret(apiApplicationKey.getClientSecret());
        applicationInfo.setAccess_token(accessTokenInfo.getAccess_token());
        applicationInfo.setRefresh_token(accessTokenInfo.getRefresh_token());

        return applicationInfo;
    }
}
