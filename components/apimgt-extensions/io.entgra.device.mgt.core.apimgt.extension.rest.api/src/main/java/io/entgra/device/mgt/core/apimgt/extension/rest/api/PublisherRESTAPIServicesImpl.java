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

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.HttpsTrustManagerUtils;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.ScopeUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Documentation;

import java.io.IOException;
import java.util.List;

public class PublisherRESTAPIServicesImpl implements PublisherRESTAPIServices {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIServicesImpl.class);
    private static final OkHttpClient client = new OkHttpClient(HttpsTrustManagerUtils.getSSLClient().newBuilder());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Gson gson = new Gson();
    private static final String host = System.getProperty(Constants.IOT_CORE_HOST);
    private static final String port = System.getProperty(Constants.IOT_CORE_HTTPS_PORT);
    private static final String endPointPrefix = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host + Constants.COLON + port;

    @Override
    public JSONObject getScopes(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
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
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
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
            throw new APIServicesException(e);
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
                log.error(msg);
                return false;
            } else {
                String msg = "Response : " + response.code() + response.body();
                throw new UnexpectedResponseException(msg);
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean addNewSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewSharedScopeEndPoint = endPointPrefix + Constants.SCOPE_API_ENDPOINT + scope.getId();

        ScopeUtils scopeUtil = new ScopeUtils();
        scopeUtil.setKey(scope.getKey());
        scopeUtil.setName(scope.getName());
        scopeUtil.setDescription(scope.getDescription());
        scopeUtil.setRoles(scope.getRoles());
        String scopeString = scopeUtil.toJSON();

        RequestBody requestBody = RequestBody.create(JSON, scopeString);
        Request request = new Request.Builder()
                .url(addNewSharedScopeEndPoint)
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
                return addNewSharedScope(apiApplicationKey, refreshedAccessToken, scope);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String updateScopeUrl = endPointPrefix + Constants.SCOPE_API_ENDPOINT + scope.getId();

        ScopeUtils scopeUtil = new ScopeUtils();
        scopeUtil.setKey(scope.getKey());
        scopeUtil.setName(scope.getName());
        scopeUtil.setDescription(scope.getDescription());
        scopeUtil.setRoles(scope.getRoles());
        String scopeString = scopeUtil.toJSON();

        RequestBody requestBody = RequestBody.create(JSON, scopeString);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public API getApi(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIIdentifier apiIdentifier)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllApis = endPointPrefix + Constants.API_ENDPOINT + apiIdentifier.getUUID();
        Request request = new Request.Builder()
                .url(getAllApis)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) {
                return gson.fromJson(response.body().string(), API.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getApi(apiApplicationKey, refreshedAccessToken, apiIdentifier);
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
            throw new APIServicesException(e);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public API createAPI(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, API api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String creatAPIEndPoint = endPointPrefix + Constants.API_ENDPOINT;

        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(api));
        Request request = new Request.Builder()
                .url(creatAPIEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_CREATED == response.code()) {
                return gson.fromJson(response.body().string(), API.class);

            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return createAPI(apiApplicationKey, refreshedAccessToken, api);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean updateApi(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, API api)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String updateAPIEndPoint = endPointPrefix + Constants.API_ENDPOINT + api.getUuid();

        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(api));
        Request request = new Request.Builder()
                .url(updateAPIEndPoint)
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
                return updateApi(apiApplicationKey, refreshedAccessToken, api);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean saveAsyncApiDefinition(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          String uuid, String asyncApiDefinition)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewScope = endPointPrefix + Constants.API_ENDPOINT + uuid;

        RequestBody requestBody = RequestBody.create(JSON, asyncApiDefinition);
        Request request = new Request.Builder()
                .url(addNewScope)
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
            throw new APIServicesException(e);
        }

    }

    @Override
    public JSONObject getAllApiSpecificMediationPolicies(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                         APIIdentifier apiIdentifier)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAPIMediationEndPoint = endPointPrefix + Constants.API_ENDPOINT + apiIdentifier.getUUID() + "/mediation-policies";

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
                return getAllApiSpecificMediationPolicies(apiApplicationKey, refreshedAccessToken, apiIdentifier);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean addApiSpecificMediationPolicy(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                 String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addAPIMediation = endPointPrefix + Constants.API_ENDPOINT + uuid + "/mediation-policies/" + mediation.getUuid()
                + "/content";

        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(mediation));
        Request request = new Request.Builder()
                .url(addAPIMediation)
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
            throw new APIServicesException(e);
        }

    }


    @Override
    public boolean updateApiSpecificMediationPolicyContent(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                           String uuid, Mediation mediation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String updateApiMediationEndPOint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/mediation-policies/" + mediation.getUuid()
                + "/content";

        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(mediation));
        Request request = new Request.Builder()
                .url(updateApiMediationEndPOint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .put(requestBody)
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
                return updateApiSpecificMediationPolicyContent(apiApplicationKey, refreshedAccessToken, uuid, mediation);
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
            throw new APIServicesException(e);
        }

    }

    @Override
    public boolean changeLifeCycleStatus(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                         String uuid, String action)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String changeStatusEndPoint = endPointPrefix + Constants.API_ENDPOINT + "change-lifecycle?apiId=" + uuid + "&action=" + action;

        RequestBody requestBody = RequestBody.create(JSON, Constants.EMPTY_STRING);
        Request request = new Request.Builder()
                .url(changeStatusEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) { // Check response status
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return changeLifeCycleStatus(apiApplicationKey, refreshedAccessToken, uuid, action);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public JSONObject getAPIRevisions(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
                                      String deploymentStatus)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAPIRevisionsEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions" + deploymentStatus;

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
            throw new APIServicesException(e);
        }
    }

    @Override
    public JSONObject getAPIRevisionDeployment(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getLatestRevisionUUIDEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/deployments";

        Request request = new Request.Builder()
                .url(getLatestRevisionUUIDEndPoint)
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
                return getAPIRevisionDeployment(apiApplicationKey, refreshedAccessToken, uuid);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public APIRevision addAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIRevision apiRevision)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewScope = endPointPrefix + Constants.API_ENDPOINT + apiRevision.getApiUUID() + "/revisions";

        String apiRevisionDescription = "{\n" +
                "   \"description\":\" " + apiRevision.getDescription() + "\",\n" +
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
            if (HttpStatus.SC_CREATED == response.code()) { // Check response status
                return gson.fromJson(response.body().string(), APIRevision.class);
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return addAPIRevision(apiApplicationKey, refreshedAccessToken, apiRevision);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean deployAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String uuid,
                                     String apiRevisionId, List<APIRevisionDeployment> apiRevisionDeploymentList)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String deployAPIRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/deploy-revision?revisionId=" + apiRevisionId;

        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(apiRevisionDeploymentList));
        Request request = new Request.Builder()
                .url(deployAPIRevisionEndPoint)
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
                return deployAPIRevision(apiApplicationKey, refreshedAccessToken, uuid, apiRevisionId,
                        apiRevisionDeploymentList);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean undeployAPIRevisionDeployment(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                                 APIRevisionDeployment apiRevisionDeployment, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String undeployAPIRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions/"
                + apiRevisionDeployment.getRevisionUUID();

        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(apiRevisionDeployment));
        Request request = new Request.Builder()
                .url(undeployAPIRevisionEndPoint)
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
                return undeployAPIRevisionDeployment(apiApplicationKey, refreshedAccessToken, apiRevisionDeployment, uuid);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean deleteAPIRevision(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                     APIRevision apiRevision, String uuid)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String apiRevisionEndPoint = endPointPrefix + Constants.API_ENDPOINT + uuid + "/revisions/" + apiRevision.getRevisionUUID();

        Request request = new Request.Builder()
                .url(apiRevisionEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Constants.AUTHORIZATION_HEADER_PREFIX_BEARER
                        + accessTokenInfo.getAccess_token())
                .delete()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (HttpStatus.SC_OK == response.code()) { // Check response status
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(),
                                apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return deleteAPIRevision(apiApplicationKey, refreshedAccessToken, apiRevision, uuid);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public JSONObject getDocumentations(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, APIIdentifier apiIdentifier)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getDocumentationsEndPoint = endPointPrefix + Constants.API_ENDPOINT + apiIdentifier.getUUID() + "/documents?limit=1000";

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
                return getDocumentations(apiApplicationKey, refreshedAccessToken, apiIdentifier);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean deleteDocumentations(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                        APIIdentifier apiIdentifier, String documentID)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getDocumentationsEndPoint = endPointPrefix + Constants.API_ENDPOINT + apiIdentifier.getUUID() + "/documents/" + documentID;

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
                return deleteDocumentations(apiApplicationKey, refreshedAccessToken, apiIdentifier, documentID);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public Documentation addDocumentation(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          APIIdentifier apiIdentifier, Documentation documentation)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addNewScope = endPointPrefix + Constants.API_ENDPOINT + apiIdentifier.getUUID() + "/documents";

        String document = "{\n" +
                "  \"name\": \" " + documentation.getName() + " \",\n" +
                "  \"type\": \" " + documentation.getType() + " \",\n" +
                "  \"summary\": \" " + documentation.getSummary() + " \",\n" +
                "  \"sourceType\": \" " + documentation.getSourceType() + " \",\n" +
                "  \"inlineContent\": \" " + documentation.getSourceType() + " \",\n" +
                "  \"visibility\": \" " + documentation.getVisibility() + " \",\n" +
                "  \"createdBy\": \" admin \"\n" +
                "}";

        RequestBody requestBody = RequestBody.create(JSON, document);
        Request request = new Request.Builder()
                .url(addNewScope)
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
                return addDocumentation(apiApplicationKey, refreshedAccessToken, apiIdentifier, documentation);
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
            throw new APIServicesException(e);
        }
    }

    @Override
    public boolean addDocumentationContent(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo,
                                          API api, String docId, String docContent)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String addDocumentationContentEndPoint = endPointPrefix + Constants.API_ENDPOINT + api.getUuid() + "/documents/" + docId;

        RequestBody requestBody = RequestBody.create(JSON, docContent);
        Request request = new Request.Builder()
                .url(addDocumentationContentEndPoint)
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
                return addDocumentationContent(apiApplicationKey, refreshedAccessToken, api,docId ,docContent);
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
            throw new APIServicesException(e);
        }
    }

}
