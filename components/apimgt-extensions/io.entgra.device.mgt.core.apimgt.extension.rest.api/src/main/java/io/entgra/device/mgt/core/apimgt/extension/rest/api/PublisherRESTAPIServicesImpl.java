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
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.*;
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
                log.error(msg);
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
                String msg = "Shared scope key not found : " + key;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                return false;
            } else {
                String msg = "Response : " + response.code() + response.body();
                log.error(msg);
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
        if (scope.getBindings() != null) {
            for (String str : scope.getBindings()) {
                bindings.put(str);
            }
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
                log.error(msg);
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
        if (scope.getBindings() != null) {
            for (String str : scope.getBindings()) {
                bindings.put(str);
            }
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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean deleteSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String updateScopeUrl = endPointPrefix + Constants.SCOPE_API_ENDPOINT + scope.getId();

        JSONArray bindings = new JSONArray();
        if (scope.getBindings() != null) {
            for (String str : scope.getBindings()) {
                bindings.put(str);
            }
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
                .delete(requestBody)
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
                return deleteSharedScope(apiApplicationKey, refreshedAccessToken, scope);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                String msg = "Bad Request, Invalid scope object";
                log.error(msg);
                throw new BadRequestException(msg);
            } else {
                String msg = "Response : " + response.code() + response.body();
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo getApi(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String apiUuid)
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
                return gson.fromJson(response.body().string(), APIInfo.class);
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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo[] getApis(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
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
                JSONArray apiList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(apiList.toString(), APIInfo[].class);
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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo addAPI(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIInfo api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addAPIEndPoint = endPointPrefix + Constants.API_ENDPOINT;

        JSONObject payload = new JSONObject();
        payload.put("name", api.getName());
        payload.put("description", api.getDescription());
        payload.put("context", api.getContext());
        payload.put("version", api.getVersion());
        payload.put("provider", api.getProvider());
        payload.put("lifeCycleStatus", api.getLifeCycleStatus());
        payload.put("wsdlInfo", (api.getWsdlInfo() != null ? api.getWsdlInfo() : null));
        payload.put("wsdlUrl", (api.getWsdlUrl() != null ? api.getWsdlUrl() : null));
        payload.put("responseCachingEnabled", api.isResponseCachingEnabled());
        payload.put("cacheTimeout", api.getCacheTimeout());
        payload.put("hasThumbnail", api.isHasThumbnail());
        payload.put("isDefaultVersion", api.isDefaultVersion());
        payload.put("isRevision", api.isRevision());
        payload.put("revisionedApiId", (api.getRevisionedApiId() != null ? api.getRevisionedApiId() : null));
        payload.put("revisionId", api.getRevisionId());
        payload.put("enableSchemaValidation", api.isEnableSchemaValidation());
        payload.put("type", api.getType());
        payload.put("apiThrottlingPolicy", api.getApiThrottlingPolicy());
        payload.put("authorizationHeader", api.getAuthorizationHeader());
        payload.put("visibility", api.getVisibility());
        payload.put("subscriptionAvailability", (api.getSubscriptionAvailability() != null ? api.getSubscriptionAvailability() : ""));

        //Lists
        if (api.getTransport() != null) {
            JSONArray transport = new JSONArray();
            for (String str : api.getTransport()) {
                transport.put(str);
            }
            payload.put("transport", transport);
        }
        if (api.getTags() != null) {
            JSONArray tags = new JSONArray();
            for (String str : api.getTags()) {
                tags.put(str);
            }
            payload.put("tags", tags);
        }
        if (api.getPolicies() != null) {
            JSONArray policies = new JSONArray();
            for (String str : api.getPolicies()) {
                policies.put(str);
            }
            payload.put("policies", policies);
        }
        if (api.getMediationPolicies() != null) {
            JSONArray mediationPolicies = new JSONArray();
            for (MediationPolicy object : api.getMediationPolicies()) {
                mediationPolicies.put(new JSONObject(gson.toJson(object)));
            }
            payload.put("mediationPolicies", mediationPolicies);
        }
        if (api.getSubscriptionAvailableTenants() != null) {
            JSONArray subscriptionAvailableTenants = new JSONArray();
            for (String str : api.getSubscriptionAvailableTenants()) {
                subscriptionAvailableTenants.put(str);
            }
            payload.put("subscriptionAvailableTenants", subscriptionAvailableTenants);
        }
        if (api.getAdditionalProperties() != null) {
            JSONArray additionalProperties = new JSONArray();
            for (AdditionalProperties str : api.getAdditionalProperties()) {
                additionalProperties.put(str);
            }
            payload.put("additionalProperties", additionalProperties);
        }
        if (api.getScopes() != null) {
            JSONArray scopes = new JSONArray();
            for (JSONObject object : api.getScopes()) {
                scopes.put(object);
            }
            payload.put("scopes", scopes);
        }
        if (api.getOperations() != null) {
            JSONArray operations = new JSONArray();
            for (Operations operation : api.getOperations()) {
                operations.put(new JSONObject(gson.toJson(operation)));
            }
            payload.put("operations", operations);
        }
        if (api.getCategories() != null) {
            JSONArray categories = new JSONArray();
            for (String str : api.getCategories()) {
                categories.put(str);
            }
            payload.put("categories", categories);
        }

        //objects
        payload.put("monetization", (api.getMonetization() != null ? new JSONObject(gson.toJson(api.getMonetization())) : null));
        payload.put("corsConfiguration", (api.getCorsConfiguration() != null ? new JSONObject(gson.toJson(api.getCorsConfiguration())) : null));
        payload.put("websubSubscriptionConfiguration", (api.getWebsubSubscriptionConfiguration() != null ? new JSONObject(gson.toJson(api.getWebsubSubscriptionConfiguration())) : null));
        payload.put("workflowStatus", (api.getWorkflowStatus() != null ? api.getWorkflowStatus() : null));
        payload.put("endpointConfig", (api.getEndpointConfig() != null ? api.getEndpointConfig() : null));
        payload.put("endpointImplementationType", (api.getEndpointImplementationType() != null ? api.getEndpointImplementationType() : null));
        payload.put("threatProtectionPolicies", (api.getThreatProtectionPolicies() != null ? api.getThreatProtectionPolicies() : null));
        payload.put("serviceInfo", (api.getServiceInfo() != null ? new JSONObject(gson.toJson(api.getServiceInfo())) : null));
        payload.put("advertiseInfo", (api.getAdvertiseInfo() != null ? new JSONObject(gson.toJson(api.getAdvertiseInfo())) : null));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(addAPIEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return gson.fromJson(response.body().string(), APIInfo.class);
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
                log.error(msg);
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

        JSONObject payload = new JSONObject();
        payload.put("name", api.getName());
        payload.put("description", api.getDescription());
        payload.put("context", api.getContext());
        payload.put("version", api.getVersion());
        payload.put("provider", api.getProvider());
        payload.put("lifeCycleStatus", api.getLifeCycleStatus());
        payload.put("wsdlInfo", (api.getWsdlInfo() != null ? api.getWsdlInfo() : null));
        payload.put("wsdlUrl", (api.getWsdlUrl() != null ? api.getWsdlUrl() : null));
        payload.put("responseCachingEnabled", api.isResponseCachingEnabled());
        payload.put("cacheTimeout", api.getCacheTimeout());
        payload.put("hasThumbnail", api.isHasThumbnail());
        payload.put("isDefaultVersion", api.isDefaultVersion());
        payload.put("isRevision", api.isRevision());
        payload.put("revisionedApiId", (api.getRevisionedApiId() != null ? api.getRevisionedApiId() : null));
        payload.put("revisionId", api.getRevisionId());
        payload.put("enableSchemaValidation", api.isEnableSchemaValidation());
        payload.put("type", api.getType());
        payload.put("apiThrottlingPolicy", api.getApiThrottlingPolicy());
        payload.put("authorizationHeader", api.getAuthorizationHeader());
        payload.put("visibility", api.getVisibility());
        payload.put("subscriptionAvailability", (api.getSubscriptionAvailability() != null ? api.getSubscriptionAvailability() : ""));

        //Lists
        if (api.getTransport() != null) {
            JSONArray transport = new JSONArray();
            for (String str : api.getTransport()) {
                transport.put(str);
            }
            payload.put("transport", transport);
        }
        if (api.getTags() != null) {
            JSONArray tags = new JSONArray();
            for (String str : api.getTags()) {
                tags.put(str);
            }
            payload.put("tags", tags);
        }
        if (api.getPolicies() != null) {
            JSONArray policies = new JSONArray();
            for (String str : api.getPolicies()) {
                policies.put(str);
            }
            payload.put("policies", policies);
        }
        if (api.getMediationPolicies() != null) {
            JSONArray mediationPolicies = new JSONArray();
            for (MediationPolicy object : api.getMediationPolicies()) {
                mediationPolicies.put(new JSONObject(gson.toJson(object)));
            }
            payload.put("mediationPolicies", mediationPolicies);
        }
        if (api.getSubscriptionAvailableTenants() != null) {
            JSONArray subscriptionAvailableTenants = new JSONArray();
            for (String str : api.getSubscriptionAvailableTenants()) {
                subscriptionAvailableTenants.put(str);
            }
            payload.put("subscriptionAvailableTenants", subscriptionAvailableTenants);
        }
        if (api.getAdditionalProperties() != null) {
            JSONArray additionalProperties = new JSONArray();
            for (AdditionalProperties str : api.getAdditionalProperties()) {
                additionalProperties.put(str);
            }
            payload.put("additionalProperties", additionalProperties);
        }
        if (api.getScopes() != null) {
            JSONArray scopes = new JSONArray();
            for (JSONObject object : api.getScopes()) {
                scopes.put(object);
            }
            payload.put("scopes", scopes);
        }
        if (api.getOperations() != null) {
            JSONArray operations = new JSONArray();
            for (Operations operation : api.getOperations()) {
                operations.put(new JSONObject(gson.toJson(operation)));
            }
            payload.put("operations", operations);
        }
        if (api.getCategories() != null) {
            JSONArray categories = new JSONArray();
            for (String str : api.getCategories()) {
                categories.put(str);
            }
            payload.put("categories", categories);
        }

        //objects
        payload.put("monetization", (api.getMonetization() != null ? new JSONObject(gson.toJson(api.getMonetization())) : null));
        payload.put("corsConfiguration", (api.getCorsConfiguration() != null ? new JSONObject(gson.toJson(api.getCorsConfiguration())) : null));
        payload.put("websubSubscriptionConfiguration", (api.getWebsubSubscriptionConfiguration() != null ? new JSONObject(gson.toJson(api.getWebsubSubscriptionConfiguration())) : null));
        payload.put("workflowStatus", (api.getWorkflowStatus() != null ? api.getWorkflowStatus() : null));
        payload.put("endpointConfig", (api.getEndpointConfig() != null ? api.getEndpointConfig() : null));
        payload.put("endpointImplementationType", (api.getEndpointImplementationType() != null ? api.getEndpointImplementationType() : null));
        payload.put("threatProtectionPolicies", (api.getThreatProtectionPolicies() != null ? api.getThreatProtectionPolicies() : null));
        payload.put("serviceInfo", (api.getServiceInfo() != null ? new JSONObject(gson.toJson(api.getServiceInfo())) : null));
        payload.put("advertiseInfo", (api.getAdvertiseInfo() != null ? new JSONObject(gson.toJson(api.getAdvertiseInfo())) : null));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
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
                log.error(msg);
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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }

    }

    @Override
    public MediationPolicy[] getAllApiSpecificMediationPolicies(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
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
                JSONArray mediationPolicyList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(mediationPolicyList.toString(), MediationPolicy[].class);
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
                log.error(msg);
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
                log.error(msg);
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
                log.error(msg);
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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIRevision[] getAPIRevisions(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
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
                JSONArray revisionList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(revisionList.toString(), APIRevision[].class);
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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIRevision addAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIRevision apiRevision)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewScope = endPointPrefix + Constants.API_ENDPOINT + apiRevision.getApiUUID() + "/revisions";

        JSONObject payload = new JSONObject();
        payload.put("description", (apiRevision.getDescription() != null ? apiRevision.getDescription() : null));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(addNewScope)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return gson.fromJson(response.body().string(), APIRevision.class);
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
                log.error(msg);
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

        JSONArray payload = new JSONArray();
        JSONObject revision = new JSONObject();
        revision.put("name", (apiRevisionDeployment.getName() != null ? apiRevisionDeployment.getName() : ""));
        revision.put("vhost", (apiRevisionDeployment.getVhost() != null ? apiRevisionDeployment.getVhost() : ""));
        revision.put("displayOnDevportal", apiRevisionDeployment.isDisplayOnDevportal());
        payload.put(revision);

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
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
                log.error(msg);
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
                                                 APIRevision apiRevisionDeployment, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String undeployAPIRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/undeploy-revision?revisionId="
                + apiRevisionDeployment.getId();
        List<APIRevisionDeployment> apiRevisionDeployments = apiRevisionDeployment.getDeploymentInfo();
        APIRevisionDeployment earliestDeployment = apiRevisionDeployments.get(0);

        JSONArray payload = new JSONArray();
        JSONObject revision = new JSONObject();
        revision.put("name", (earliestDeployment.getName() != null ? earliestDeployment.getName() : ""));
        revision.put("vhost", (earliestDeployment.getVhost() != null ? earliestDeployment.getVhost() : ""));
        revision.put("displayOnDevportal", earliestDeployment.isDisplayOnDevportal());
        payload.put(revision);

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
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
                log.error(msg);
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
                                     APIRevision apiRevision, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String apiRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions/" +
                apiRevision.getId();

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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Documentation[] getDocumentations(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid)
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
                JSONArray documentList = (JSONArray) new JSONObject(response.body().string()).get("list");
                return gson.fromJson(documentList.toString(), Documentation[].class);
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
                log.error(msg);
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
                log.error(msg);
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

        JSONObject payload = new JSONObject();
        payload.put("name", documentation.getName());
        payload.put("type", documentation.getType());
        payload.put("summary", documentation.getSummary());
        payload.put("sourceType", documentation.getSourceType());
        payload.put("inlineContent", documentation.getSourceType());
        payload.put("visibility", documentation.getVisibility());
        payload.put("createdBy", documentation.getCreatedBy());

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
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
                log.error(msg);
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
                log.error(msg);
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

}
