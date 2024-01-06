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

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.*;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.ApiApplicationInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.HttpsTrustManagerUtils;
import okhttp3.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConsumerRESTAPIServicesImpl implements ConsumerRESTAPIServices {

    private static final Log log = LogFactory.getLog(ConsumerRESTAPIServicesImpl.class);
    private static final OkHttpClient client = new OkHttpClient(HttpsTrustManagerUtils.getSSLClient().newBuilder());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final String host = System.getProperty(Constants.IOT_CORE_HOST);
    private static final String port = System.getProperty(Constants.IOT_CORE_HTTPS_PORT);
    private static final String endPointPrefix = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host
            + Constants.COLON + port;

    @Override
    public Application[] getAllApplications(ApiApplicationInfo apiApplicationInfo, String appName)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllApplicationsUrl = endPointPrefix + Constants.APPLICATIONS_API + "?query=" + appName;

        Request.Builder builder = new Request.Builder();
        builder.url(getAllApplicationsUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.get();
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONArray applicationList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(applicationList.toString(), Application[].class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return getAllApplications(refreshedApiApplicationInfo, appName);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Application getDetailsOfAnApplication(ApiApplicationInfo apiApplicationInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getDetailsOfAPPUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId;

        Request.Builder builder = new Request.Builder();
        builder.url(getDetailsOfAPPUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.get();
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return gson.fromJson(response.body().string(), Application.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return getDetailsOfAnApplication(refreshedApiApplicationInfo, applicationId);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Application createApplication(ApiApplicationInfo apiApplicationInfo, Application application)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllScopesUrl = endPointPrefix + Constants.APPLICATIONS_API;

        JSONArray groups = new JSONArray();
        JSONArray subscriptionScope = new JSONArray();

        if (application.getGroups() != null && application.getSubscriptionScopes() != null) {
            for (String string : application.getGroups()) {
                groups.put(string);
            }
            for (Scopes string : application.getSubscriptionScopes()) {
                subscriptionScope.put(string);
            }
        }

        JSONObject applicationInfo = new JSONObject();
        applicationInfo.put("name", application.getName());
        applicationInfo.put("throttlingPolicy", application.getThrottlingPolicy());
        applicationInfo.put("description", application.getDescription());
        applicationInfo.put("tokenType", application.getTokenType());
        applicationInfo.put("groups", groups);
        applicationInfo.put("attributes", new JSONObject());
        applicationInfo.put("subscriptionScopes", subscriptionScope);

        RequestBody requestBody = RequestBody.create(JSON, applicationInfo.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(getAllScopesUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.post(requestBody);
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return gson.fromJson(response.body().string(), Application.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return createApplication(refreshedApiApplicationInfo, application);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request body";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Boolean deleteApplication(ApiApplicationInfo apiApplicationInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String deleteScopesUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId;

        Request.Builder builder = new Request.Builder();
        builder.url(deleteScopesUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.delete();
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return deleteApplication(refreshedApiApplicationInfo, applicationId);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request body";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Subscription[] getAllSubscriptions(ApiApplicationInfo apiApplicationInfo, String applicationId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllScopesUrl = endPointPrefix + Constants.SUBSCRIPTION_API + "?applicationId=" + applicationId + "&limit=1000";

        Request.Builder builder = new Request.Builder();
        builder.url(getAllScopesUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.get();
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONArray subscriptionList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(subscriptionList.toString(), Subscription[].class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return getAllSubscriptions(refreshedApiApplicationInfo, applicationId);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo[] getAllApis(ApiApplicationInfo apiApplicationInfo, Map<String, String> queryParams, Map<String, String> headerParams)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        StringBuilder getAPIsURL = new StringBuilder(endPointPrefix + Constants.DEV_PORTAL_API);

        for (Map.Entry<String, String> query : queryParams.entrySet()) {
            getAPIsURL.append(Constants.AMPERSAND).append(query.getKey()).append(Constants.EQUAL).append(query.getValue());
        }

        Request.Builder builder = new Request.Builder();
        builder.url(getAPIsURL.toString());
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());

        for (Map.Entry<String, String> header : headerParams.entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }
        builder.get();
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONArray apiList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(apiList.toString(), APIInfo[].class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return getAllApis(refreshedApiApplicationInfo, queryParams, headerParams);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Subscription createSubscription(ApiApplicationInfo apiApplicationInfo, Subscription subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String createSubscriptionUrl = endPointPrefix + Constants.SUBSCRIPTION_API;

        JSONObject subscriptionObject = new JSONObject();
        subscriptionObject.put("applicationId", subscriptions.getApplicationId());
        subscriptionObject.put("apiId", subscriptions.getApiId());
        subscriptionObject.put("throttlingPolicy", subscriptions.getThrottlingPolicy());
        subscriptionObject.put("requestedThrottlingPolicy", subscriptions.getRequestedThrottlingPolicy());

        RequestBody requestBody = RequestBody.create(JSON, subscriptionObject.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(createSubscriptionUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());

        builder.post(requestBody);
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return gson.fromJson(response.body().string(), Subscription.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return createSubscription(refreshedApiApplicationInfo, subscriptions);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request body";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Subscription[] createSubscriptions(ApiApplicationInfo apiApplicationInfo, List<Subscription> subscriptions)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String createSubscriptionsUrl = endPointPrefix + Constants.SUBSCRIPTION_API + "/multiple";

        String subscriptionsList = gson.toJson(subscriptions);
        RequestBody requestBody = RequestBody.create(JSON, subscriptionsList);

        Request.Builder builder = new Request.Builder();
        builder.url(createSubscriptionsUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());

        builder.post(requestBody);
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return gson.fromJson(response.body().string(), Subscription[].class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return createSubscriptions(refreshedApiApplicationInfo, subscriptions);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request body";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey generateApplicationKeys(ApiApplicationInfo apiApplicationInfo, String applicationId, String keyManager,
                                                  String validityTime, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String generateApplicationKeysUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH +
                applicationId + "/generate-keys";

        JSONArray grantTypesToBeSupported = new JSONArray();
        grantTypesToBeSupported.put("password");
        grantTypesToBeSupported.put("client_credentials");

        JSONArray scopes = new JSONArray();
        scopes.put("am_application_scope");
        scopes.put("default");

        JSONObject keyInfo = new JSONObject();
        keyInfo.put("keyType", keyType);
        keyInfo.put("keyManager", keyManager);
        keyInfo.put("grantTypesToBeSupported", grantTypesToBeSupported);
        keyInfo.put("callbackUrl", "");
        keyInfo.put("scopes", scopes);
        keyInfo.put("validityTime", 3600);
        keyInfo.put("additionalProperties", new JSONObject());

        RequestBody requestBody = RequestBody.create(JSON, keyInfo.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(generateApplicationKeysUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.post(requestBody);
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return gson.fromJson(response.body().string(), ApplicationKey.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return generateApplicationKeys(refreshedApiApplicationInfo, applicationId, keyManager, validityTime, keyType);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request body";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey mapApplicationKeys(ApiApplicationInfo apiApplicationInfo, Application application, String keyManager, String keyType)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllScopesUrl = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH +
                application.getApplicationId() + "/map-keys";

        JSONObject payload = new JSONObject();
        payload.put("consumerKey", apiApplicationInfo.getClientId());
        payload.put("consumerSecret", apiApplicationInfo.getClientSecret());
        payload.put("keyManager", keyManager);
        payload.put("keyType", keyType);

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());

        Request.Builder builder = new Request.Builder();
        builder.url(getAllScopesUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.post(requestBody);
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return gson.fromJson(response.body().string(), ApplicationKey.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return mapApplicationKeys(refreshedApiApplicationInfo, application, keyManager, keyType);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request body";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey getKeyDetails(ApiApplicationInfo apiApplicationInfo, String applicationId, String keyMapId)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getKeyDetails = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId + "/oauth-keys/" + keyMapId;

        Request.Builder builder = new Request.Builder();
        builder.url(getKeyDetails);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.get();
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return gson.fromJson(response.body().string(), ApplicationKey.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return getKeyDetails(refreshedApiApplicationInfo, applicationId, keyMapId);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public ApplicationKey updateGrantType(ApiApplicationInfo apiApplicationInfo, String applicationId, String keyMapId, String keyManager,
                                          List<String> supportedGrantTypes, String callbackUrl)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getKeyDetails = endPointPrefix + Constants.APPLICATIONS_API + Constants.SLASH + applicationId + "/oauth-keys/" + keyMapId;

        Request.Builder builder = new Request.Builder();
        builder.url(getKeyDetails);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());

        JSONArray supportedGrantTypeList = new JSONArray();
        for (String string : supportedGrantTypes) {
            supportedGrantTypeList.put(string);
        }

        JSONObject payload = new JSONObject();
        payload.put("keyMappingId", keyMapId);
        payload.put("keyManager", keyManager);
        payload.put("supportedGrantTypes", supportedGrantTypeList);
        payload.put("callbackUrl", (callbackUrl != null ? callbackUrl : ""));
        payload.put("additionalProperties", new JSONObject());

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());

        builder.put(requestBody);
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return gson.fromJson(response.body().string(), ApplicationKey.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return updateGrantType(refreshedApiApplicationInfo, applicationId, keyMapId, keyManager, supportedGrantTypes, callbackUrl);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public KeyManager[] getAllKeyManagers(ApiApplicationInfo apiApplicationInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllKeyManagersUrl = endPointPrefix + Constants.KEY_MANAGERS_API;

        Request.Builder builder = new Request.Builder();
        builder.url(getAllKeyManagersUrl);
        builder.addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                + apiApplicationInfo.getAccess_token());
        builder.get();
        Request request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONArray keyManagerList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(keyManagerList.toString(), KeyManager[].class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(apiApplicationInfo.getRefresh_token(),
                                apiApplicationInfo.getClientId(), apiApplicationInfo.getClientSecret());
                ApiApplicationInfo refreshedApiApplicationInfo = returnApplicationInfo(apiApplicationInfo, refreshedAccessToken);
                return getAllKeyManagers(refreshedApiApplicationInfo);
                //TODO: max attempt count
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    private ApiApplicationInfo returnApplicationInfo(ApiApplicationInfo apiApplicationInfo, AccessTokenInfo refreshedToken) {

        ApiApplicationInfo applicationInfo = new ApiApplicationInfo();
        applicationInfo.setClientId(apiApplicationInfo.getClientId());
        applicationInfo.setClientSecret(apiApplicationInfo.getClientSecret());
        applicationInfo.setAccess_token(refreshedToken.getAccess_token());
        applicationInfo.setRefresh_token(refreshedToken.getRefresh_token());
        return applicationInfo;
    }
}
