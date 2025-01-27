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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationKey;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.ApiApplicationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.Token;
import io.entgra.device.mgt.core.apimgt.application.extension.bean.TokenCreationProfile;
import io.entgra.device.mgt.core.apimgt.application.extension.constants.ApiApplicationConstants;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.IOAuthClientService;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Application;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.ApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.IDNApplicationKeys;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.KeyManager;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.Subscription;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.OAuthClientException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.identity.jwt.client.extension.JWTClient;
import io.entgra.device.mgt.core.identity.jwt.client.extension.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents an implementation of APIManagementProviderService.
 */
public class APIManagementProviderServiceImpl implements APIManagementProviderService {
    private static final Log log = LogFactory.getLog(APIManagementProviderServiceImpl.class);
    private static final APIManagerConfiguration config =
            ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    private static final OkHttpClient client = new OkHttpClient();
    private static final String UNLIMITED_TIER = "Unlimited";
    private static final Gson gson = new Gson();

    /**
     * Construct request body for acquiring token
     *
     * @param tokenCreationProfile {@link TokenCreationProfile}
     * @return Constructed json body payload object
     */
    private static JSONObject generateRequestBody(TokenCreationProfile tokenCreationProfile) {
        JSONObject requestBody = new JSONObject();

        switch (tokenCreationProfile.getGrantType()) {
            case "password": {
                requestBody.put("username", tokenCreationProfile.getUsername());
                requestBody.put("password", tokenCreationProfile.getPassword());
                break;
            }
            case "refresh_token": {
                requestBody.put("refresh_token", tokenCreationProfile.getRefreshToken());
                break;
            }
            case "authorization_code": {
                requestBody.put("code", tokenCreationProfile.getCode());
                requestBody.put("redirect_uri", tokenCreationProfile.getCallbackUrl());
                break;
            }
            default: {
                requestBody.put("grant_type", tokenCreationProfile.getGrantType());
            }
        }

        requestBody.put("scope", tokenCreationProfile.getScope());
        return requestBody;
    }

    /**
     * Create API application describe by {@link ApiApplicationProfile}
     *
     * @param apiApplicationProfile {@link ApiApplicationProfile}
     * @return Return created API application details by populating {@link ApiApplicationKey}
     * @throws APIManagerException         Throws when error encountered while API application creation
     * @throws BadRequestException         Throws when API application profile contains an invalid properties
     * @throws UnexpectedResponseException Throws when unexpected error encountered while invoking REST services
     */
    private static ApiApplicationKey createApiApplication(ApiApplicationProfile apiApplicationProfile)
            throws APIManagerException, BadRequestException, UnexpectedResponseException {
        if (apiApplicationProfile.getGrantTypes().contains("authorization_code")
                && StringUtils.isEmpty(apiApplicationProfile.getCallbackUrl())) {
            throw new BadRequestException("Invalid request found.");
        }

        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();
        try {
            List<Application> applications =
                    Arrays.asList(consumerRESTAPIServices.getAllApplications(apiApplicationProfile.getApplicationName()));

            if (applications.size() > 1) {
                String msg = "Found more than one application with the same application name : [ " +
                        apiApplicationProfile.getApplicationName() + " ]";
                log.error(msg);
                throw new APIManagerException(msg);
            }

            Set<APIInfo> apis = new HashSet<>();
            Map<String, String> queryParam = new HashMap<>();
            for (String tag : apiApplicationProfile.getTags()) {
                queryParam.put("tag", tag);
                apis.addAll(Arrays.asList(consumerRESTAPIServices.getAllApis(queryParam, new HashMap<>())));
                queryParam.clear();
            }

            return applications.isEmpty() ? createAndRetrieveApplicationKeys(apiApplicationProfile, apis) :
                    updateAndRetrieveApplicationKeys(applications.get(0), apiApplicationProfile, apis);

        } catch (APIServicesException e) {
            String msg =
                    "Error encountered while creating API application : [ " + apiApplicationProfile.getApplicationName() + " ]";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }

    /**
     * In case of getting OPAQUE token, we need to alter the default API application registration procedure.
     * In such cases, create identity service provider(IDN DCR client) first and then create the API application.
     * After that map the IDN DCR's client credentials and secret with the created API application.
     *
     * @param application API application
     * @return {@link ApplicationKey}
     * @throws APIManagerException         Throws when error encountered while API application creation
     * @throws BadRequestException         Throws when API application profile contains an invalid properties
     * @throws UnexpectedResponseException Throws when unexpected error encountered while invoking REST services
     * @throws APIServicesException        Throws when error encountered while executing REST API invocations
     */
    private static ApiApplicationKey mapApiApplicationWithIdnDCRClient(Application application) throws APIManagerException,
            BadRequestException, UnexpectedResponseException, APIServicesException {
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();
        IOAuthClientService ioAuthClientService =
                APIApplicationManagerExtensionDataHolder.getInstance().getIoAuthClientService();
        IDNApplicationKeys idnApplicationKeys;

        try {
            idnApplicationKeys =
                    ioAuthClientService.getIdnApplicationKeys("opaque_token_issuer_for" + application.getApplicationId() +
                            "_" + ApiApplicationConstants.DEFAULT_TOKEN_TYPE);
        } catch (OAuthClientException e) {
            String msg = "Error encountered while registering IDN DCR client for generating OPAQUE token for API " +
                    "application [ " + application.getName() + " ]";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }

        if (idnApplicationKeys == null) {
            String msg = "Null received as registered DCR client for OPAQUE token issuing process";
            log.error(msg);
            throw new APIManagerException(msg);
        }

        KeyManager[] keyManagers = consumerRESTAPIServices.getAllKeyManagers();

        if (keyManagers.length != 1) {
            String msg = "Found invalid number of key managers.";
            log.error(msg);
            throw new APIManagerException(msg);
        }

        ApplicationKey applicationKey = consumerRESTAPIServices.mapApplicationKeys(idnApplicationKeys.getConsumerKey(),
                idnApplicationKeys.getConsumerSecret(), application, keyManagers[0].getName(),
                ApiApplicationConstants.DEFAULT_TOKEN_TYPE);
        return new ApiApplicationKey(applicationKey.getConsumerKey(), applicationKey.getConsumerSecret());
    }

    /**
     * Update an existing API application according to {@link ApiApplicationProfile}
     *
     * @param application           Existing API application
     * @param apiApplicationProfile {@link ApiApplicationProfile}
     * @param apis                  Existing subscription APIs
     * @return Return created API application details by populating {@link ApiApplicationKey}
     * @throws BadRequestException         Throws when API application profile contains an invalid properties
     * @throws UnexpectedResponseException Throws when unexpected error encountered while invoking REST services
     * @throws APIServicesException        Throws when error encountered while executing REST API invocations
     * @throws APIManagerException         Throws when error encountered while API updating application
     */
    private static ApiApplicationKey updateAndRetrieveApplicationKeys(Application application,
                                                                      ApiApplicationProfile apiApplicationProfile,
                                                                      Set<APIInfo> apis)
            throws BadRequestException, UnexpectedResponseException, APIServicesException, APIManagerException {
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();

        List<Subscription> availableSubscriptions =
                Arrays.asList(consumerRESTAPIServices.getAllSubscriptions(application.getApplicationId()));
        List<Subscription> allSubscriptions = constructSubscriptionList(application.getApplicationId(), apis);

        List<Subscription> newSubscriptions = new ArrayList<>();
        for (Subscription subscription : allSubscriptions) {
            if (!availableSubscriptions.contains(subscription)) {
                newSubscriptions.add(subscription);
            }
        }

        ApplicationKey[] applicationKeys = consumerRESTAPIServices.getAllKeys(application.getApplicationId());
        if (applicationKeys.length == 0) {
            return generateApplicationKeys(application.getApplicationId(), apiApplicationProfile.getGrantTypes(),
                    apiApplicationProfile.getCallbackUrl());
        }

        ApplicationKey applicationKey = applicationKeys[0];

        // Received { "code":900967,"message":"General Error" } when updating the grant types of existing application.
        // Hence, as an alternative we check if there is any grant type difference and if yes simply delete the
        // previous application and create a new one.
        boolean isGrantsAreUpdated =
                !new HashSet<>(applicationKey.getSupportedGrantTypes()).
                        equals(new HashSet<>(Arrays.asList(apiApplicationProfile.getGrantTypes().split(Constants.SPACE))));

        if (isGrantsAreUpdated) {
            consumerRESTAPIServices.deleteApplication(application.getApplicationId());
            return createAndRetrieveApplicationKeys(apiApplicationProfile, apis);
        }

        if (!newSubscriptions.isEmpty()) {
            consumerRESTAPIServices.createSubscriptions(newSubscriptions);
        }

        return new ApiApplicationKey(applicationKey.getConsumerKey(), applicationKey.getConsumerSecret());
    }

    /**
     * Create API application and generate application keys
     *
     * @param apiApplicationProfile {@link ApiApplicationProfile}
     * @param apis                  Set of API definitions associated with the tags
     * @return Return created API application details by populating {@link ApiApplicationKey}
     * @throws BadRequestException         Throws when API application profile contains an invalid properties
     * @throws UnexpectedResponseException Throws when unexpected error encountered while invoking REST services
     * @throws APIServicesException        Throws when error encountered while executing REST API invocations
     * @throws APIManagerException         Throws when error encountered while API creating application
     */
    private static ApiApplicationKey createAndRetrieveApplicationKeys(ApiApplicationProfile apiApplicationProfile,
                                                                      Set<APIInfo> apis)
            throws BadRequestException, UnexpectedResponseException, APIServicesException, APIManagerException {
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();

        Application application = new Application();
        application.setName(apiApplicationProfile.getApplicationName());
        application.setThrottlingPolicy(UNLIMITED_TIER);
        application.setTokenType(apiApplicationProfile.getTokenType().toString());
        application.setOwner(apiApplicationProfile.getOwner());

        application = consumerRESTAPIServices.createApplication(application);

        List<Subscription> subscriptions = constructSubscriptionList(application.getApplicationId(), apis);

        consumerRESTAPIServices.createSubscriptions(subscriptions);

        if (Objects.equals(apiApplicationProfile.getTokenType(), ApiApplicationProfile.TOKEN_TYPE.DEFAULT)) {
            return mapApiApplicationWithIdnDCRClient(application);
        }

        return generateApplicationKeys(application.getApplicationId(), apiApplicationProfile.getGrantTypes(),
                apiApplicationProfile.getCallbackUrl());
    }

    /**
     * Generate API application keys
     *
     * @param applicationId API application ID to retrieve application keys
     * @param grantTypes    Grant types
     * @param callbackUrl   Callback URL
     * @return Return created API application details by populating {@link ApiApplicationKey}
     * @throws APIManagerException         Throws when error encountered while API getting application keys
     * @throws BadRequestException         Throws when API application profile contains an invalid properties
     * @throws UnexpectedResponseException Throws when unexpected error encountered while invoking REST services
     * @throws APIServicesException        Throws when error encountered while executing REST API invocations
     */
    private static ApiApplicationKey generateApplicationKeys(String applicationId, String grantTypes,
                                                             String callbackUrl)
            throws APIManagerException, BadRequestException, UnexpectedResponseException, APIServicesException {
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();

        KeyManager[] keyManagers = consumerRESTAPIServices.getAllKeyManagers();

        if (keyManagers.length != 1) {
            String msg = "Found invalid number of key managers.";
            log.error(msg);
            throw new APIManagerException(msg);
        }

        ApplicationKey applicationKey = consumerRESTAPIServices.generateApplicationKeys(applicationId,
                keyManagers[0].getName(), ApiApplicationConstants.DEFAULT_VALIDITY_PERIOD,
                ApiApplicationConstants.DEFAULT_TOKEN_TYPE, grantTypes, callbackUrl);
        return new ApiApplicationKey(applicationKey.getConsumerKey(), applicationKey.getConsumerSecret());
    }

    /**
     * Construct subscription list
     *
     * @param applicationId API application ID
     * @param apiInfos      API definitions associated with tags
     * @return Returns list of subscriptions
     */
    private static List<Subscription> constructSubscriptionList(String applicationId, Set<APIInfo> apiInfos) {
        return apiInfos.stream().map(apiInfo -> {
            Subscription subscription = new Subscription();
            subscription.setApplicationId(applicationId);
            subscription.setApiId(apiInfo.getId());
            subscription.setThrottlingPolicy(UNLIMITED_TIER);
            return subscription;
        }).collect(Collectors.toList());
    }

    /**
     * Retrieve API publish enabled tenant domain list from the super tenant space
     *
     * @return Returns list of API publishing enabled tenant domains
     * @throws APIManagerException Throws when error encountered while getting tenant list from metadata registry
     */
    private static List<String> getApiPublishingEnabledTenantDomains() throws APIManagerException {
        MetadataManagementService metadataManagementService =
                APIApplicationManagerExtensionDataHolder.getInstance().getMetadataManagementService();
        Metadata metaData;
        try {
            if (Objects.equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                metaData = metadataManagementService.retrieveMetadata(Constants.API_PUBLISHING_ENABLED_TENANT_LIST_KEY);
            } else {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                    metaData =
                            metadataManagementService.retrieveMetadata(Constants.API_PUBLISHING_ENABLED_TENANT_LIST_KEY);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } catch (MetadataManagementException e) {
            String msg = "Failed to load API publishing enabled tenant domains from meta data registry.";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }

        if (metaData == null) {
            String msg = "Null retrieved for the metadata entry when getting API publishing enabled tenant domains.";
            log.error(msg);
            throw new APIManagerException(msg);
        }

        JsonArray tenants = gson.fromJson(metaData.getMetaValue(), JsonArray.class);
        List<String> tenantDomains = new ArrayList<>();
        for (JsonElement tenant : tenants) {
            tenantDomains.add(tenant.getAsString());
        }
        return tenantDomains;
    }

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
    public Token getToken(TokenCreationProfile tokenCreationProfile) throws APIManagerException {
        JSONObject requestBody = generateRequestBody(tokenCreationProfile);

        Request request = new Request.Builder()
                .url(config.getFirstProperty(Constants.TOKE_END_POINT))
                .post(RequestBody
                        .create(requestBody.toString(),
                                MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", Credentials.basic(tokenCreationProfile.getBasicAuthUsername(),
                        tokenCreationProfile.getBasicAuthPassword()))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return gson.fromJson(response.body() != null ? response.body().string() : null, Token.class);
            }

            String msg = "Error response [ " + response.code() + " ] received for the token acquiring request";
            log.error(msg);
            throw new APIManagerException(msg);

        } catch (IOException e) {
            String msg = "Error encountered while sending token acquiring request";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }

    @Override
    public ApiApplicationKey registerApiApplication(ApiApplicationProfile apiApplicationProfile) throws APIManagerException,
            BadRequestException, UnexpectedResponseException {
        String flowStartingDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

        String currentTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        // Here we are checking whether that the current tenant is API publishing enabled tenant or not
        // If the current tenant belongs to a publishing enabled tenant, then start the api application
        // registration sequences in current tenant space, otherwise in the super tenant
        for (String tenantDomain : getApiPublishingEnabledTenantDomains()) {
            if (Objects.equals(tenantDomain, currentTenantDomain)) {
                flowStartingDomain = currentTenantDomain;
                break;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Start API application registration sequences though " + flowStartingDomain + " domain.");
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(flowStartingDomain, true);
            return createApiApplication(apiApplicationProfile);

        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public Token getCustomToken(TokenCreationProfile tokenCreationProfile) throws APIManagerException {
        JWTClientManagerService jwtClientManagerService =
                APIApplicationManagerExtensionDataHolder.getInstance().getJwtClientManagerService();
        try {
            JWTClient jwtClient = jwtClientManagerService.getJWTClient();
            AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(tokenCreationProfile.getBasicAuthUsername(),
                    tokenCreationProfile.getBasicAuthPassword(), tokenCreationProfile.getUsername(),
                    tokenCreationProfile.getScope());

            if (accessTokenInfo == null) {
                String msg = "Received a null token when generating a custom JWT token";
                log.error(msg);
                throw new APIManagerException(msg);
            }

            Token token = new Token();
            token.setAccessToken(accessTokenInfo.getAccessToken());
            token.setRefreshToken(accessTokenInfo.getRefreshToken());
            token.setTokenType(accessTokenInfo.getTokenType());
            token.setScope(accessTokenInfo.getScopes());
            token.setExpiresIn(accessTokenInfo.getExpiresIn());

            return token;
        } catch (JWTClientException e) {
            String msg = "Error encountered while acquiring custom JWT token";
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }
}
