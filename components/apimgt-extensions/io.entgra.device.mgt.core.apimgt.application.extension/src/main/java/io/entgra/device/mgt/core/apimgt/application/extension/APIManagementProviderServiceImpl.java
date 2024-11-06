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

import io.entgra.device.mgt.core.apimgt.application.extension.bean.APIRegistrationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.dto.ApiApplicationKey;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.ApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.KeyManager;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Subscription;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.identity.jwt.client.extension.JWTClient;
import io.entgra.device.mgt.core.identity.jwt.client.extension.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.APIApplicationServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.ApiApplicationInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This class represents an implementation of APIManagementProviderService.
 */
public class APIManagementProviderServiceImpl implements APIManagementProviderService {

    private static final Log log = LogFactory.getLog(APIManagementProviderServiceImpl.class);
    public static final APIManagerFactory API_MANAGER_FACTORY = APIManagerFactory.getInstance();
    private static final String UNLIMITED_TIER = "Unlimited";

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
    public synchronized ApiApplicationKey generateAndRetrieveApplicationKeys(String applicationName, String[] tags,
                                                                             String keyType, String username,
                                                                             boolean isAllowedAllDomains,
                                                                             String validityTime,
                                                                             String password, String accessToken,
                                                                             ArrayList<String> supportedGrantTypes,
                                                                             String callbackUrl,
                                                                             boolean isMappingRequired)
            throws APIManagerException {

        ApiApplicationInfo apiApplicationInfo = new ApiApplicationInfo();
        if (StringUtils.isEmpty(accessToken)) {
            apiApplicationInfo = getApplicationInfo(username, password);
        } else {
            apiApplicationInfo.setAccess_token(accessToken);
        }

        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();

        try {
            Map<String, String> headerParams = new HashMap<>();
            if (!"carbon.super".equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true))) {
                headerParams.put("X-WSO2-Tenant", "carbon.super");
            }

            Map<String, APIInfo> uniqueApiSet = new HashMap<>();
            if (tags != null) {
                for (String tag : tags) {
                    Map<String, String> queryParams = new HashMap<>();
                    queryParams.put("tag", tag);

                    APIInfo[] apiInfos = consumerRESTAPIServices.getAllApis(apiApplicationInfo, queryParams, headerParams);
                    Arrays.stream(apiInfos).forEach(apiInfo -> uniqueApiSet.putIfAbsent(apiInfo.getName(), apiInfo));
                }
            }

            List<APIInfo> uniqueApiList = new ArrayList<>(uniqueApiSet.values());

            io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application[] applications =
                    consumerRESTAPIServices.getAllApplications(apiApplicationInfo, applicationName);
            if (applications.length == 0) {
                return handleNewAPIApplication(applicationName, uniqueApiList, apiApplicationInfo, keyType,
                        validityTime, supportedGrantTypes, callbackUrl, isMappingRequired);
            } else {
                if (applications.length == 1) {
                    Optional<io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application> applicationOpt =
                            Arrays.stream(applications).findFirst();
                    io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application application =
                            applicationOpt.get();

                    MetadataManagementService metadataManagementService = APIApplicationManagerExtensionDataHolder.getInstance().getMetadataManagementService();
                    Metadata metaData = metadataManagementService.retrieveMetadata(applicationName);
                    if (metaData == null) {
                        // Todo add a comment
                        consumerRESTAPIServices.deleteApplication(apiApplicationInfo, application.getApplicationId());
                        return handleNewAPIApplication(applicationName, uniqueApiList, apiApplicationInfo, keyType,
                                validityTime, supportedGrantTypes, callbackUrl, isMappingRequired);
                    } else {
                        Subscription[] subscriptions = consumerRESTAPIServices.getAllSubscriptions(apiApplicationInfo, application.getApplicationId());
                        for (Subscription subscription : subscriptions) {
                            uniqueApiList.removeIf(apiInfo -> Objects.equals(apiInfo.getId(), subscription.getApiInfo().getId()));
                        }

                        if (!uniqueApiList.isEmpty()) {
                            addSubscriptions(application, uniqueApiList, apiApplicationInfo);
                        }

                        String[] metaValues = metaData.getMetaValue().split(":");
                        if (metaValues.length != 2) {
                            String msg = "Found invalid Meta value for meta key: " + applicationName + ". Meta Value: "
                                    + metaData.getMetaValue();
                            log.error(msg);
                            throw new APIManagerException(msg);
                        }
                        String applicationId = metaValues[0];
                        String keyMappingId = metaValues[1];
                        ApplicationKey applicationKey = consumerRESTAPIServices.getKeyDetails(apiApplicationInfo, applicationId, keyMappingId);
                        ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
                        apiApplicationKey.setConsumerKey(applicationKey.getConsumerKey());
                        apiApplicationKey.setConsumerSecret(applicationKey.getConsumerSecret());
                        return apiApplicationKey;
                    }
                } else {
                    String msg = "Found more than one application for application name: " + applicationName;
                    log.error(msg);
                    throw new APIManagerException(msg);
                }
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
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting meta data for meta key: " + applicationName;
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }


    private ApiApplicationKey handleNewAPIApplication(String applicationName, List<APIInfo> uniqueApiList,
                                                      ApiApplicationInfo apiApplicationInfo, String keyType, String validityTime,
                                                      ArrayList<String> supportedGrantTypes, String callbackUrl,
                                                      boolean isMappingRequired) throws APIManagerException {
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();
        io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application application = new io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application();
        application.setName(applicationName);
        application.setThrottlingPolicy(UNLIMITED_TIER);

        try {
            application = consumerRESTAPIServices.createApplication(apiApplicationInfo, application);
            addSubscriptions(application, uniqueApiList, apiApplicationInfo);

            KeyManager[] keyManagers = consumerRESTAPIServices.getAllKeyManagers(apiApplicationInfo);
            KeyManager keyManager;
            if (keyManagers.length == 1) {
                keyManager = keyManagers[0];
            } else {
                String msg =
                        "Found invalid number of key managers. No of key managers found from the APIM: " + keyManagers.length;
                log.error(msg);
                throw new APIManagerException(msg);
            }

            ApplicationKey applicationKey;

            if (isMappingRequired) {
                // If we need to get opaque token instead of the JWT token, we have to do the mapping. Therefore, if
                // it is a requirement then we have to call the method with enabling the flag.
                APIApplicationServices apiApplicationServices = APIApplicationManagerExtensionDataHolder.getInstance()
                        .getApiApplicationServices();

                APIApplicationKey apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials(
                        "ClientForMapping",
                        "client_credentials password refresh_token urn:ietf:params:oauth:grant-type:jwt-bearer");

                apiApplicationInfo.setClientId(apiApplicationKey.getClientId());
                apiApplicationInfo.setClientSecret(apiApplicationKey.getClientSecret());

                applicationKey = consumerRESTAPIServices.mapApplicationKeys(apiApplicationInfo, application,
                        keyManager.getName(), keyType);
            } else {
                applicationKey = consumerRESTAPIServices.generateApplicationKeys(apiApplicationInfo, application.getApplicationId(),
                        keyManager.getName(), validityTime, keyType);
            }
            if (supportedGrantTypes != null || StringUtils.isNotEmpty(callbackUrl)) {
                applicationKey = consumerRESTAPIServices.updateGrantType(apiApplicationInfo, application.getApplicationId(),
                        applicationKey.getKeyMappingId(), keyManager.getName(), supportedGrantTypes, callbackUrl);
            }

            ApiApplicationKey apiApplicationKey = new ApiApplicationKey();
            apiApplicationKey.setConsumerKey(applicationKey.getConsumerKey());
            apiApplicationKey.setConsumerSecret(applicationKey.getConsumerSecret());

            Metadata metaData = new Metadata();
            metaData.setMetaKey(applicationName);
            String metaValue = application.getApplicationId() + ":" + applicationKey.getKeyMappingId();
            metaData.setMetaValue(metaValue);

            MetadataManagementService metadataManagementService = APIApplicationManagerExtensionDataHolder.getInstance().getMetadataManagementService();
            metadataManagementService.createMetadata(metaData);
            return apiApplicationKey;
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg = "Since meta key:" + applicationName + " already exists, meta data creating process failed.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while creating meta data for meta key: " + applicationName;
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (BadRequestException e) {
            String msg = "Provided incorrect payload when invoking APIM REST endpoints to handle new API application.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (UnexpectedResponseException e) {
            String msg = "Error occurred while invoking APIM REST endpoints to handle new API application.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        } catch (APIServicesException e) {
            String msg = "Error occurred while processing the response of APIM REST endpoints to handle new API application.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }

    /**
     * This method can be used to add a new subscriptions providing the ids of the APIs and the applications.
     *
     * @param application        {@link io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application}
     * @param apiInfos           {@link List<APIInfo>}
     * @param apiApplicationInfo {@link ApiApplicationInfo}
     * @throws BadRequestException         if incorrect data provided to call subscribing REST API.
     * @throws UnexpectedResponseException if error occurred while processing the subscribing REST API.
     * @throws APIServicesException        if error occurred while invoking the subscribing REST API.
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
            subscription.setThrottlingPolicy(UNLIMITED_TIER);
            subscription.setRequestedThrottlingPolicy(UNLIMITED_TIER);
            subscriptionList.add(subscription);
        });

        consumerRESTAPIServices.createSubscriptions(apiApplicationInfo, subscriptionList);
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

            return jwtClient
                    .getAccessToken(clientCredentials.getConsumerKey(), clientCredentials.getConsumerSecret(), username,
                            scopes);
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
     * Get Client credentials of application belongs to tenant admin
     *
     * @param tenantDomain    Tenant Domain
     * @param tags            Tags
     * @param applicationName Application Name
     * @param tokenType       Token Type
     * @param validityPeriod  Validity Period
     * @return {@link ApiApplicationKey}
     * @throws APIManagerException if error occurred while generating access token
     * @throws UserStoreException  if error occurred while getting admin username.
     */
    private ApiApplicationKey getClientCredentials(String tenantDomain, String[] tags, String applicationName,
                                                   String tokenType, String validityPeriod) throws APIManagerException, UserStoreException {

        APIRegistrationProfile registrationProfile = new APIRegistrationProfile();
        registrationProfile.setAllowedToAllDomains(false);
        registrationProfile.setMappingAnExistingOAuthApp(false);
        registrationProfile.setTags(tags);
        registrationProfile.setApplicationName(applicationName);

        if (tenantDomain == null || tenantDomain.isEmpty()) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                            .getAdminUserName());

            return generateAndRetrieveApplicationKeys(registrationProfile.getApplicationName(),
                    registrationProfile.getTags(), tokenType, PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                            .getRealmConfiguration().getAdminUserName(),
                    registrationProfile.isAllowedToAllDomains(), validityPeriod, PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                            .getRealmConfiguration().getAdminPassword(), null, null, null, false);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private ApiApplicationInfo getApplicationInfo(String username, String password)
            throws APIManagerException {

        APIApplicationServices apiApplicationServices = APIApplicationManagerExtensionDataHolder.getInstance()
                .getApiApplicationServices();

        APIApplicationKey apiApplicationKey;
        io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo accessTokenInfo;
        try {
            if (username == null || password == null) {
                apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentials(
                        "ClientForConsumerRestCalls",
                        "client_credentials password refresh_token urn:ietf:params:oauth:grant-type:jwt-bearer");
            } else {
                apiApplicationKey = apiApplicationServices.createAndRetrieveApplicationCredentialsWithUser(
                        "ClientForConsumerRestCalls",
                        username, password,
                        "client_credentials password refresh_token urn:ietf:params:oauth:grant-type:jwt-bearer");
            }
            accessTokenInfo = apiApplicationServices.generateAccessTokenFromRegisteredApplication(
                    apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
        } catch (APIServicesException e) {
            String errorMsg = "Error occurred while generating the API application";
            log.error(errorMsg, e);
            throw new APIManagerException(errorMsg, e);
        }

        ApiApplicationInfo applicationInfo = new ApiApplicationInfo();
        applicationInfo.setClientId(apiApplicationKey.getClientId());
        applicationInfo.setClientSecret(apiApplicationKey.getClientSecret());
        applicationInfo.setAccess_token(accessTokenInfo.getAccess_token());
        applicationInfo.setRefresh_token(accessTokenInfo.getRefresh_token());

        return applicationInfo;
    }
}
