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
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.OAuthClientResponse;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevision;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.APIRevisionDeployment;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.AdditionalProperties;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Documentation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Mediation;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.MediationPolicy;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Operations;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.OAuthClientException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.internal.APIManagerServiceDataHolder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PublisherRESTAPIServicesImpl implements PublisherRESTAPIServices {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIServicesImpl.class);
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final String host = System.getProperty(Constants.IOT_CORE_HOST);
    private static final String port = System.getProperty(Constants.IOT_CORE_HTTPS_PORT);
    private static final String endPointPrefix = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host
            + Constants.COLON + port;
    private static final IOAuthClientService client =
            APIManagerServiceDataHolder.getInstance().getIoAuthClientService();

    @Override
    public Scope[] getScopes()
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllScopesUrl = endPointPrefix + Constants.GET_ALL_SCOPES;
        Request request = new Request.Builder()
                .url(getAllScopesUrl)
                .get()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray scopeList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(scopeList.toString(), Scope[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving scopes";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean isSharedScopeNameExists(String key) throws APIServicesException, BadRequestException,
            UnexpectedResponseException {
        String keyValue = new String(Base64.encodeBase64((key).getBytes())).replace(Constants.QUERY_KEY_VALUE_SEPARATOR,
                Constants.EMPTY_STRING);
        String getScopeUrl = endPointPrefix + Constants.SCOPE_API_ENDPOINT + keyValue;

        Request request = new Request.Builder()
                .url(getScopeUrl)
                .head()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while checking the shared scope existence for scope key : [ " + key + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean addNewSharedScope(Scope scope)
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
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while adding new shared scope : [ " + scope.getName() + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean updateSharedScope(Scope scope)
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
                .put(requestBody)
                .build();

        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while updating the scope : [ " + scope.getName() + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean deleteSharedScope(Scope scope)
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
                .delete(requestBody)
                .build();

        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while deleting shared scope : [ " + scope.getName() + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo getApi(String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllApi = endPointPrefix + Constants.API_ENDPOINT + apiUuid;
        Request request = new Request.Builder()
                .url(getAllApi)
                .get()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), APIInfo.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving API associated with API UUID : [ " + apiUuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo[] getApis() throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAllApis = endPointPrefix + Constants.GET_ALL_APIS;
        Request request = new Request.Builder()
                .url(getAllApis)
                .get()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray apiList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(apiList.toString(), APIInfo[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving APIs";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIInfo addAPI(APIInfo api) throws APIServicesException, BadRequestException, UnexpectedResponseException {
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
        payload.put("subscriptionAvailability", (api.getSubscriptionAvailability() != null ?
                api.getSubscriptionAvailability() : ""));

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
        payload.put("monetization", (api.getMonetization() != null ?
                new JSONObject(gson.toJson(api.getMonetization())) : null));
        payload.put("corsConfiguration", (api.getCorsConfiguration() != null ?
                new JSONObject(gson.toJson(api.getCorsConfiguration())) : null));
        payload.put("websubSubscriptionConfiguration", (api.getWebsubSubscriptionConfiguration() != null ?
                new JSONObject(gson.toJson(api.getWebsubSubscriptionConfiguration())) : null));
        payload.put("workflowStatus", (api.getWorkflowStatus() != null ? api.getWorkflowStatus() : null));
        payload.put("endpointConfig", (api.getEndpointConfig() != null ? api.getEndpointConfig() : null));
        payload.put("endpointImplementationType", (api.getEndpointImplementationType() != null ?
                api.getEndpointImplementationType() : null));
        payload.put("threatProtectionPolicies", (api.getThreatProtectionPolicies() != null ?
                api.getThreatProtectionPolicies() : null));
        payload.put("serviceInfo", (api.getServiceInfo() != null ? new JSONObject(gson.toJson(api.getServiceInfo()))
                : null));
        payload.put("advertiseInfo", (api.getAdvertiseInfo() != null ?
                new JSONObject(gson.toJson(api.getAdvertiseInfo())) : null));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(addAPIEndPoint)
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), APIInfo.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while creating API : [ " + api.getName() + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean updateApi(APIInfo api)
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
        payload.put("subscriptionAvailability", (api.getSubscriptionAvailability() != null ?
                api.getSubscriptionAvailability() : ""));

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
        payload.put("monetization", (api.getMonetization() != null ?
                new JSONObject(gson.toJson(api.getMonetization())) : null));
        payload.put("corsConfiguration", (api.getCorsConfiguration() != null ?
                new JSONObject(gson.toJson(api.getCorsConfiguration())) : null));
        payload.put("websubSubscriptionConfiguration", (api.getWebsubSubscriptionConfiguration() != null ?
                new JSONObject(gson.toJson(api.getWebsubSubscriptionConfiguration())) : null));
        payload.put("workflowStatus", (api.getWorkflowStatus() != null ? api.getWorkflowStatus() : null));
        payload.put("endpointConfig", (api.getEndpointConfig() != null ? api.getEndpointConfig() : null));
        payload.put("endpointImplementationType", (api.getEndpointImplementationType() != null ?
                api.getEndpointImplementationType() : null));
        payload.put("threatProtectionPolicies", (api.getThreatProtectionPolicies() != null ?
                api.getThreatProtectionPolicies() : null));
        payload.put("serviceInfo", (api.getServiceInfo() != null ? new JSONObject(gson.toJson(api.getServiceInfo()))
                : null));
        payload.put("advertiseInfo", (api.getAdvertiseInfo() != null ?
                new JSONObject(gson.toJson(api.getAdvertiseInfo())) : null));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(updateAPIEndPoint)
                .put(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while updating API : [ " + api.getName() + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean saveAsyncApiDefinition(String uuid, String asyncApiDefinition)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String saveAsyncAPI = endPointPrefix + Constants.API_ENDPOINT + uuid + "/asyncapi";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("apiDefinition", asyncApiDefinition)
                .build();

        Request request = new Request.Builder()
                .url(saveAsyncAPI)
                .addHeader(Constants.HEADER_CONTENT_TYPE, "multipart/form-data")
                .put(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while saving async definition of the API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }

    }

    @Override
    public MediationPolicy[] getAllApiSpecificMediationPolicies(String apiUuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAPIMediationEndPoint = endPointPrefix + Constants.API_ENDPOINT + apiUuid + "/mediation-policies";
        Request request = new Request.Builder()
                .url(getAPIMediationEndPoint)
                .get()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray mediationPolicyList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(mediationPolicyList.toString(), MediationPolicy[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving mediation policies for API : [ " + apiUuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean addApiSpecificMediationPolicy(String uuid, Mediation mediation) throws APIServicesException,
            BadRequestException, UnexpectedResponseException {
        String addAPIMediation = endPointPrefix + Constants.API_ENDPOINT + uuid + "/mediation-policies";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inlineContent", mediation.getConfig())
                .addFormDataPart("type", mediation.getType())
                .build();

        Request request = new Builder()
                .url(addAPIMediation)
                .addHeader(Constants.HEADER_CONTENT_TYPE, "multipart/form-data")
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_CREATED == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while adding mediation policies for API : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }

    }

    @Override
    public boolean deleteApiSpecificMediationPolicy(String uuid, Mediation mediation) throws APIServicesException,
            BadRequestException, UnexpectedResponseException {
        String deleteApiMediationEndPOint =
                endPointPrefix + Constants.API_ENDPOINT + uuid + "/mediation-policies/" + mediation.getUuid();

        Request request = new Request.Builder()
                .url(deleteApiMediationEndPOint)
                .delete()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_NO_CONTENT == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while deleting mediation policy for API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }

    }

    @Override
    public boolean changeLifeCycleStatus(String uuid, String action)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String changeAPIStatusEndPoint = endPointPrefix + Constants.API_ENDPOINT + "change-lifecycle?apiId=" + uuid
                + "&action=" + action;

        RequestBody requestBody = RequestBody.create(JSON, Constants.EMPTY_STRING);
        Request request = new Request.Builder()
                .url(changeAPIStatusEndPoint)
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while changing the life cycle state of the API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIRevision[] getAPIRevisions(String uuid, Boolean deploymentStatus)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getAPIRevisionsEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions?query=deployed:"
                + deploymentStatus;

        Request request = new Request.Builder()
                .url(getAPIRevisionsEndPoint)
                .get()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray revisionList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(revisionList.toString(), APIRevision[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving API revisions for API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public APIRevision addAPIRevision(APIRevision apiRevision)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewScope = endPointPrefix + Constants.API_ENDPOINT + apiRevision.getApiUUID() + "/revisions";

        JSONObject payload = new JSONObject();
        payload.put("description", (apiRevision.getDescription() != null ? apiRevision.getDescription() : null));

        RequestBody requestBody = RequestBody.create(JSON, payload.toString());
        Request request = new Request.Builder()
                .url(addNewScope)
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), APIRevision.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while creating API revision for API UUID : [ " + apiRevision.getApiUUID() +
                    " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean deployAPIRevision(String uuid, String apiRevisionId,
                                     List<APIRevisionDeployment> apiRevisionDeploymentList)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String deployAPIRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/deploy-revision" +
                "?revisionId=" + apiRevisionId;
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
                .post(requestBody)
                .build();

        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_CREATED == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while deploying API revision for API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean undeployAPIRevisionDeployment(APIRevision apiRevisionDeployment, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String undeployAPIRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/undeploy-revision" +
                "?revisionId="
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
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_CREATED == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while undeploy API revision associated with API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean deleteAPIRevision(APIRevision apiRevision, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String apiRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions/" +
                apiRevision.getId();

        Request request = new Request.Builder()
                .url(apiRevisionEndPoint)
                .delete()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while deleting API revision associated with API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Documentation[] getDocumentations(String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getDocumentationsEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/documents?limit=1000";

        Request request = new Request.Builder()
                .url(getDocumentationsEndPoint)
                .get()
                .build();

        try {
            OAuthClientResponse response = client.execute(request);
            JSONArray documentList = (JSONArray) new JSONObject(response.getBody()).get("list");
            return gson.fromJson(documentList.toString(), Documentation[].class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while retrieving API documentation for API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean deleteDocumentations(String uuid, String documentID)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String getDocumentationsEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/documents/" + documentID;

        Request request = new Request.Builder()
                .url(getDocumentationsEndPoint)
                .delete()
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_OK == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while deleting API documentation associated with API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public Documentation addDocumentation(String uuid, Documentation documentation)
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
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return gson.fromJson(response.getBody(), Documentation.class);
        } catch (OAuthClientException e) {
            String msg = "Error occurred while creating documentation for API UUID : [ " + uuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

    @Override
    public boolean addDocumentationContent(String apiUuid, String docId, String docContent)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {
        String addDocumentationContentEndPoint =
                endPointPrefix + Constants.API_ENDPOINT + apiUuid + "/documents/" + docId + "/content";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("inlineContent", docContent)
                .build();

        Request request = new Request.Builder()
                .url(addDocumentationContentEndPoint)
                .addHeader(Constants.HEADER_CONTENT_TYPE, "multipart/form-data")
                .post(requestBody)
                .build();
        try {
            OAuthClientResponse response = client.execute(request);
            return HttpStatus.SC_CREATED == response.getCode();
        } catch (OAuthClientException e) {
            String msg = "Error occurred while adding documentation content for API UUID : [ " + apiUuid + " ]";
            log.error(msg, e);
            throw new APIServicesException(msg, e);
        }
    }

}
