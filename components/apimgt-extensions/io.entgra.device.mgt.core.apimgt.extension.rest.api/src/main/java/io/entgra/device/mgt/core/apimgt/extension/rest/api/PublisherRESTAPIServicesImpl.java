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

import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.HttpsTrustManagerUtils;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.ScopeUtils;
import okhttp3.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.ssl.Base64;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.io.IOException;

public class PublisherRESTAPIServicesImpl implements PublisherRESTAPIServices {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIServicesImpl.class);
    private static final OkHttpClient client = new OkHttpClient(HttpsTrustManagerUtils.getSSLClient().newBuilder());
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String host = System.getProperty(Constants.IOT_CORE_HOST);
    private static final String port = System.getProperty(Constants.IOT_CORE_HTTPS_PORT);

    @Override
    public JSONObject getScopes(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIServicesException, BadRequestException, UnexpectedResponseException {

        String getAllScopesUrl = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host + Constants.COLON
                + port + Constants.GET_ALL_SCOPES;
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
        String getScopeUrl = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host + Constants.COLON
                + port + Constants.GET_SCOPE + keyValue;

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

        String updateScopeUrl = Constants.HTTPS_PROTOCOL + Constants.SCHEME_SEPARATOR + host
                + Constants.COLON + port + Constants.GET_SCOPE + scope.getId();

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
}
