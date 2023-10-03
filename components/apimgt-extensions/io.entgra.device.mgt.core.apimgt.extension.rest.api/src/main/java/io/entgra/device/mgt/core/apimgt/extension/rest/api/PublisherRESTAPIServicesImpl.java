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
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Mediation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Documentation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevision;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevisionDeployment;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.HttpsTrustManagerUtils;
import okhttp3.*;
import okhttp3.Request.Builder;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class PublisherRESTAPIServicesImpl implements PublisherRESTAPIServices {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIServicesImpl.class);
    private static final OkHttpClient client = new OkHttpClient(HttpsTrustManagerUtils.getSSLClient().newBuilder());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final String host = System.getProperty(Constants.IOT_CORE_HOST);
    private static final String port = System.getProperty(Constants.IOT_CORE_HTTPS_PORT);
    private static final String endPointPrefix = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host
            + Constants.COLON + port;

    @Override
    public Scope[] getScopes(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllScopesUrl = endPointPrefix + Constants.GET_ALL_SCOPES;
        Request request = new Request.Builder()
                .url(getAllScopesUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONArray scopeList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(scopeList.toString(), Scope[].class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getScopes(apiApplicationKey, refreshedAccessToken);
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
    public boolean isSharedScopeNameExists(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String key)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String keyValue = new String(Base64.encodeBase64((key).getBytes())).replace(Constants.QUERY_KEY_VALUE_SEPARATOR,
                Constants.EMPTY_STRING);
        String getScopeUrl = endPointPrefix + Constants.SCOPE_API_ENDPOINT + keyValue;

        Request request = new Request.Builder()
                .url(getScopeUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .head()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return isSharedScopeNameExists(apiApplicationKey, refreshedAccessToken, key);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid request";
                log.error(msg);
                throw new BadRequestException(msg);
            } else if (HttpStatus.SC_NOT_FOUND == response.code()) {
                String msg = "Shared scope key not found";
                log.info(msg);
                return false;
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
    public boolean addNewSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewSharedScopeEndPoint = endPointPrefix + Constants.SCOPE_API_ENDPOINT;

        JSONArray bindings = new JSONArray();
        for (String str : scope.getBindings()) {
            bindings.put(str);
        }

        JSONObject payload = new JSONObject();
        payload.put("name", (scope.getName() != null ? scope.getName() : ""));
        payload.put("displayName", (scope.getDisplayName() != null ? scope.getDisplayName() : ""));
        payload.put("description", (scope.getDescription() != null ? scope.getDescription() : ""));
        payload.put("bindings", (bindings != null ? bindings : ""));
        payload.put("usageCount", (scope.getUsageCount() != 0 ? scope.getUsageCount() : 0));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(addNewSharedScopeEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return addNewSharedScope(apiApplicationKey, refreshedAccessToken, scope);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid scope object";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.message();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String updateScopeUrl = endPointPrefix + Constants.SCOPE_API_ENDPOINT + scope.getId();

        JSONArray bindings = new JSONArray();
        for (String str : scope.getBindings()) {
            bindings.put(str);
        }

        JSONObject payload = new JSONObject();
        payload.put("name", (scope.getName() != null ? scope.getName() : ""));
        payload.put("displayName", (scope.getDisplayName() != null ? scope.getDisplayName() : ""));
        payload.put("description", (scope.getDescription() != null ? scope.getDescription() : ""));
        payload.put("bindings", (bindings != null ? bindings : ""));
        payload.put("usageCount", (scope.getUsageCount() != 0 ? scope.getUsageCount() : 0));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(updateScopeUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .put(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return updateSharedScope(apiApplicationKey, refreshedAccessToken, scope);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid scope object";
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
    public JSONObject getApi(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllApi = endPointPrefix + Constants.API_ENDPOINT + apiUuid;
        Request request = new Request.Builder()
                .url(getAllApi)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getApi(apiApplicationKey, refreshedAccessToken, apiUuid);
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
    public JSONObject getApis(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllApis = endPointPrefix + Constants.GET_ALL_APIS;
        Request request = new Request.Builder()
                .url(getAllApis)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getApis(apiApplicationKey, refreshedAccessToken);
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
    public JSONObject addAPI(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIInfo api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addAPIEndPoint = endPointPrefix + Constants.API_ENDPOINT;

        String apiString = "{\n" +
                "    \"name\": \"" + api.getName() + "\",\n" +
                "   \"description\":\"" + api.getDescription() + "\",\n" +
                "   \"context\":\"" + api.getContext() + "\",\n" +
                "   \"version\":\"" + api.getVersion() + "\",\n" +
                "   \"provider\":\"" + api.getProvider() + "\",\n" +
                "   \"lifeCycleStatus\":\"" + api.getLifeCycleStatus() + "\",\n" +
                "    \"wsdlInfo\": " + api.getWsdlInfo() + ",\n" +
                "   \"wsdlUrl\":" + api.getWsdlUrl() + ",\n" +
                "    \"responseCachingEnabled\": " + api.isResponseCachingEnabled() + ",\n" +
                "    \"cacheTimeout\": " + api.getCacheTimeout() + ",\n" +
                "    \"hasThumbnail\": " + api.isHasThumbnail() + ",\n" +
                "    \"isDefaultVersion\": " + api.isDefaultVersion() + ",\n" +
                "    \"isRevision\": " + api.isRevision() + ",\n" +
                "    \"revisionedApiId\": " + api.getRevisionedApiId() + ",\n" +
                "    \"revisionId\": " + api.getRevisionId() + ",\n" +
                "    \"enableSchemaValidation\": " + api.isEnableSchemaValidation() + ",\n" +
                "    \"type\": \"" + api.getType() + "\",\n" +
                "    \"transport\": " + gson.toJson(api.getTransport()) + ",\n" +
                "    \"tags\": " + gson.toJson(api.getTags()) + ",\n" +
                "    \"policies\": " + gson.toJson(api.getPolicies()) + ",\n" +
                "    \"apiThrottlingPolicy\": " + api.getApiThrottlingPolicy() + ",\n" +
                "    \"authorizationHeader\": \"" + api.getAuthorizationHeader() + "\",\n" +
                "    \"visibility\": \"" + api.getVisibility() + "\",\n" +
                "    \"mediationPolicies\": " + (api.getInSequence() != null ? "[{\"name\": \"" + api.getInSequence() + "\",\"type\": \"in\"}]" : null) + ",\n" +
                "    \"subscriptionAvailability\": \"" + api.getSubscriptionAvailability() + "\",\n" +
                "    \"subscriptionAvailableTenants\": [],\n" +
                "    \"additionalProperties\": [],\n" +
                "    \"monetization\": " + api.getMonetization() + ",\n" +
                "    \"corsConfiguration\": " + gson.toJson(api.getCorsConfiguration()) + ",\n" +
                "    \"websubSubscriptionConfiguration\": {\n" +
                "        \"enable\": false,\n" +
                "        \"secret\": \"\",\n" +
                "        \"signingAlgorithm\": \"SHA1\",\n" +
                "        \"signatureHeader\": \"x-hub-signature\"\n" +
                "    },\n" +
                "    \"workflowStatus\": null,\n" +
                "    \"endpointConfig\": " + api.getEndpointConfig().toString() + ",\n" +
                "    \"endpointImplementationType\": \"ENDPOINT\",\n" +
                "    \"scopes\": " + api.getScopes().toString() + ",\n" +
                "    \"operations\": " + (api.getOperations() != null ? api.getOperations().toString() : null) + ",\n" +
                "    \"threatProtectionPolicies\": null,\n" +
                "    \"categories\": [],\n" +
                "    \"keyManagers\": " + gson.toJson(api.getKeyManagers()) + ",\n" +
                "    \"serviceInfo\": " + api.getServiceInfo() + "\n" +
                "}";

        RequestBody requestBody = RequestBody.create(JSON, apiString);
        Request request = new Request.Builder()
                .url(addAPIEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return addAPI(apiApplicationKey, refreshedAccessToken, api);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid API request body";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response status : " + response.code() + " Response message : " + response.message();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean updateApi(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIInfo api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String updateAPIEndPoint = endPointPrefix + Constants.API_ENDPOINT + api.getId();

        String apiString = "{\n" +
                "    \"name\": \"" + api.getName() + "\",\n" +
                "   \"description\":\"" + api.getDescription() + "\",\n" +
                "   \"context\":\"" + api.getContext() + "\",\n" +
                "   \"version\":\"" + api.getVersion() + "\",\n" +
                "   \"provider\":\"" + api.getProvider() + "\",\n" +
                "   \"lifeCycleStatus\":\"" + api.getLifeCycleStatus() + "\",\n" +
                "    \"wsdlInfo\": " + api.getWsdlInfo() + ",\n" +
                "   \"wsdlUrl\":" + api.getWsdlUrl() + ",\n" +
                "    \"responseCachingEnabled\": " + api.isResponseCachingEnabled() + ",\n" +
                "    \"cacheTimeout\": " + api.getCacheTimeout() + ",\n" +
                "    \"hasThumbnail\": " + api.isHasThumbnail() + ",\n" +
                "    \"isDefaultVersion\": " + api.isDefaultVersion() + ",\n" +
                "    \"isRevision\": " + api.isRevision() + ",\n" +
                "    \"revisionedApiId\": " + api.getRevisionedApiId() + ",\n" +
                "    \"revisionId\": " + api.getRevisionId() + ",\n" +
                "    \"enableSchemaValidation\": " + api.isEnableSchemaValidation() + ",\n" +
                "    \"type\": \"" + api.getType() + "\",\n" +
                "    \"transport\": " + gson.toJson(api.getTransport()) + ",\n" +
                "    \"tags\": " + gson.toJson(api.getTags()) + ",\n" +
                "    \"policies\": " + gson.toJson(api.getPolicies()) + ",\n" +
                "    \"apiThrottlingPolicy\": " + api.getApiThrottlingPolicy() + ",\n" +
                "    \"authorizationHeader\": \"" + api.getAuthorizationHeader() + "\",\n" +
                "    \"visibility\": \"" + api.getVisibility() + "\",\n" +
                "    \"mediationPolicies\": " + (api.getInSequence() != null ? "[{\"name\": \"" + api.getInSequence() + "\",\"type\": \"in\"}]" : null) + ",\n" +
                "    \"subscriptionAvailability\": \"" + api.getSubscriptionAvailability() + "\",\n" +
                "    \"subscriptionAvailableTenants\": [],\n" +
                "    \"additionalProperties\": [],\n" +
                "    \"monetization\": " + api.getMonetization() + ",\n" +
                "    \"corsConfiguration\": " + gson.toJson(api.getCorsConfiguration()) + ",\n" +
                "    \"websubSubscriptionConfiguration\": {\n" +
                "        \"enable\": false,\n" +
                "        \"secret\": \"\",\n" +
                "        \"signingAlgorithm\": \"SHA1\",\n" +
                "        \"signatureHeader\": \"x-hub-signature\"\n" +
                "    },\n" +
                "    \"workflowStatus\": null,\n" +
                "    \"endpointConfig\": " + api.getEndpointConfig().toString() + ",\n" +
                "    \"endpointImplementationType\": \"ENDPOINT\",\n" +
                "    \"scopes\": " + api.getScopes().toString() + ",\n" +
                "    \"operations\": " + (api.getOperations() != null ? api.getOperations().toString() : null) + ",\n" +
                "    \"threatProtectionPolicies\": null,\n" +
                "    \"categories\": [],\n" +
                "    \"keyManagers\": " + gson.toJson(api.getKeyManagers()) + ",\n" +
                "    \"serviceInfo\": " + api.getServiceInfo() + "\n" +
                "}";

        RequestBody requestBody = RequestBody.create(JSON, apiString);
        Request request = new Request.Builder()
                .url(updateAPIEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .put(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return true;

            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return updateApi(apiApplicationKey, refreshedAccessToken, api);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid API request body";
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
    public boolean saveAsyncApiDefinition(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          String uuid, String asyncApiDefinition)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String saveAsyncAPI = endPointPrefix + Constants.API_ENDPOINT + uuid + "/asyncapi";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apiDefinition", asyncApiDefinition)
                .build();

        Request request = new Request.Builder()
                .url(saveAsyncAPI)
                .addHeader(Constants.HEADER_CONTENT_TYPE, "multipart/form-data")
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .put(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) { //Check the response
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return saveAsyncApiDefinition(apiApplicationKey, refreshedAccessToken, uuid, asyncApiDefinition);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid API definition request body";
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
    public JSONObject getAllApiSpecificMediationPolicies(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                         String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAPIMediationEndPoint = endPointPrefix + Constants.API_ENDPOINT + apiUuid + "/mediation-policies";
        Request request = new Request.Builder()
                .url(getAPIMediationEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getAllApiSpecificMediationPolicies(apiApplicationKey, refreshedAccessToken, apiUuid);
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
    public boolean addApiSpecificMediationPolicy(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                 String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addAPIMediation = endPointPrefix + Constants.API_ENDPOINT + uuid + "/mediation-policies";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inlineContent", mediation.getConfig())
                .addFormDataPart("type", mediation.getType())
                .build();

        Request request = new Builder()
                .url(addAPIMediation)
                .addHeader(Constants.HEADER_CONTENT_TYPE, "multipart/form-data")
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) { // Check response status
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return addApiSpecificMediationPolicy(apiApplicationKey, refreshedAccessToken, uuid, mediation);
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
    public boolean deleteApiSpecificMediationPolicy(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                    String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String deleteApiMediationEndPOint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/mediation-policies/" + mediation.getUuid();

        Request request = new Request.Builder()
                .url(deleteApiMediationEndPOint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .delete()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_NO_CONTENT == response.code()) { // Check response status
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return deleteApiSpecificMediationPolicy(apiApplicationKey, refreshedAccessToken, uuid, mediation);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid mediation policy";
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
    public boolean changeLifeCycleStatus(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                         String uuid, String action)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String changeAPIStatusEndPoint = endPointPrefix + Constants.API_ENDPOINT + "change-lifecycle?apiId=" + uuid
                + "&action=" + action;

        RequestBody requestBody = RequestBody.create(JSON, Constants.EMPTY_STRING);
        Request request = new Request.Builder()
                .url(changeAPIStatusEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return changeLifeCycleStatus(apiApplicationKey, refreshedAccessToken, uuid, action);
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
    public JSONObject getAPIRevisions(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
                                      Boolean deploymentStatus)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAPIRevisionsEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions?query=deployed:"
                + deploymentStatus;

        Request request = new Request.Builder()
                .url(getAPIRevisionsEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getAPIRevisions(apiApplicationKey, refreshedAccessToken, uuid, deploymentStatus);
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
    public JSONObject addAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIRevision apiRevision)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewScope = endPointPrefix + Constants.API_ENDPOINT + apiRevision.getApiUUID() + "/revisions";

        String apiRevisionDescription = "{\n" +
                "   \"description\":\"" + apiRevision.getDescription() + "\"\n" +
                "}";

        RequestBody requestBody = RequestBody.create(JSON, apiRevisionDescription);
        Request request = new Request.Builder()
                .url(addNewScope)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return addAPIRevision(apiApplicationKey, refreshedAccessToken, apiRevision);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid API revision request body";
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
    public boolean deployAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
                                     String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeploymentList)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String deployAPIRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/deploy-revision?revisionId=" + apiRevisionId;
        APIRevisionDeployment apiRevisionDeployment = apiRevisionDeploymentList.get(0);

        String revision = "[\n" +
                "    {\n" +
                "        \"name\": \"" + apiRevisionDeployment.getDeployment() + "\",\n" +
                "        \"vhost\": \"" + apiRevisionDeployment.getVhost() + "\",\n" +
                "        \"displayOnDevportal\": " + apiRevisionDeployment.isDisplayOnDevportal() + "\n" +
                "    }\n" +
                "]";

        RequestBody requestBody = RequestBody.create(JSON, revision);
        Request request = new Request.Builder()
                .url(deployAPIRevisionEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return deployAPIRevision(apiApplicationKey, refreshedAccessToken, uuid, apiRevisionId,
                        apiRevisionDeploymentList);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid API revision request body";
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
    public boolean undeployAPIRevisionDeployment(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                 JSONObject apiRevisionDeployment, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String undeployAPIRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/undeploy-revision?revisionId="
                + apiRevisionDeployment.getString("id");
        JSONArray array = apiRevisionDeployment.getJSONArray("deploymentInfo");
        JSONObject obj = array.getJSONObject(0);

        String revision = "[\n" +
                "    {\n" +
                "        \"name\": \"" + obj.getString("name") + "\",\n" +
                "        \"vhost\": \"" + obj.getString("vhost") + "\",\n" +
                "        \"displayOnDevportal\": " + obj.get("displayOnDevportal") + "\n" +
                "    }\n" +
                "]";

        RequestBody requestBody = RequestBody.create(JSON, revision);
        Request request = new Request.Builder()
                .url(undeployAPIRevisionEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return undeployAPIRevisionDeployment(apiApplicationKey, refreshedAccessToken, apiRevisionDeployment, uuid);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid API revision request body";
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
    public boolean deleteAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                     JSONObject apiRevision, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String apiRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions/" +
                apiRevision.getString("id");

        Request request = new Request.Builder()
                .url(apiRevisionEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .delete()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return deleteAPIRevision(apiApplicationKey, refreshedAccessToken, apiRevision, uuid);
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
    public JSONObject getDocumentations(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getDocumentationsEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/documents?limit=1000";

        Request request = new Request.Builder()
                .url(getDocumentationsEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getDocumentations(apiApplicationKey, refreshedAccessToken, uuid);
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
    public boolean deleteDocumentations(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                        String uuid, String documentID)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getDocumentationsEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/documents/" + documentID;

        Request request = new Request.Builder()
                .url(getDocumentationsEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .delete()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return deleteDocumentations(apiApplicationKey, refreshedAccessToken, uuid, documentID);
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
    public Documentation addDocumentation(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          String uuid, Documentation documentation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewScope = endPointPrefix + Constants.API_ENDPOINT + uuid + "/documents";

        String document = "{\n" +
                "  \"name\": \"" + documentation.getName() + "\",\n" +
                "  \"type\": \"" + documentation.getType() + "\",\n" +
                "  \"summary\": \"" + documentation.getSummary() + "\",\n" +
                "  \"sourceType\": \"" + documentation.getSourceType() + "\",\n" +
                "  \"inlineContent\": \"" + documentation.getSourceType() + "\",\n" +
                "  \"visibility\": \"" + documentation.getVisibility() + "\",\n" +
                "  \"createdBy\": \"admin\"\n" +
                "}";

        RequestBody requestBody = RequestBody.create(JSON, document);
        Request request = new Request.Builder()
                .url(addNewScope)
                .addHeader(Constants.HEADER_CONTENT_TYPE, Constants.APPLICATION_JSON)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) { // Check response status
                return gson.fromJson(response.body().string(), Documentation.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return addDocumentation(apiApplicationKey, refreshedAccessToken, uuid, documentation);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid documentation request body";
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
    public boolean addDocumentationContent(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                           String apiUuid, String docId, String docContent)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addDocumentationContentEndPoint = endPointPrefix + Constants.API_ENDPOINT + apiUuid + "/documents/" + docId + "/content";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inlineContent", docContent)
                .build();

        Request request = new Request.Builder()
                .url(addDocumentationContentEndPoint)
                .addHeader(Constants.HEADER_CONTENT_TYPE, "multipart/form-data")
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) { // Check response status
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return addDocumentationContent(apiApplicationKey, refreshedAccessToken, apiUuid, docId, docContent);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid documentation request body";
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

}
