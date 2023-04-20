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

package io.entgra.devicemgt.apimgt.extension.rest.api;

import io.entgra.devicemgt.apimgt.extension.rest.api.constants.Constants;
import io.entgra.devicemgt.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.devicemgt.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.devicemgt.apimgt.extension.rest.api.exceptions.APIApplicationServicesException;
import io.entgra.devicemgt.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.devicemgt.apimgt.extension.rest.api.util.ScopeUtils;

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
import org.wso2.carbon.apimgt.api.model.Scope;

import java.io.IOException;

import static io.entgra.devicemgt.apimgt.extension.rest.api.APIApplicationServicesImpl.getOkHttpClient;

public class PublisherRESTAPIServices {
    private static final Log log = LogFactory.getLog(PublisherRESTAPIServices.class);
    private static final OkHttpClient client = getOkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public JSONObject getScopes(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo)
            throws APIApplicationServicesException, BadRequestException {

        String getScopesUrl = "https://localhost:9443/api/am/publisher/v2/scopes?limit=1000";
        Request request = new Request.Builder()
                .url(getScopesUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, "Bearer " + accessTokenInfo.getAccess_token())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HttpStatus.SC_OK) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                return jsonObject;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(), apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return getScopes(apiApplicationKey, refreshedAccessToken);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                log.info(response);
                throw new BadRequestException(response.toString());
            } else {
                return null;
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            throw new APIApplicationServicesException(msg);
        }
    }

    public boolean isSharedScopeNameExists(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, String key)
            throws APIApplicationServicesException, BadRequestException {

        String keyValue = new String(Base64.encodeBase64((key).getBytes())).replace("=", "");
        String getScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + keyValue;

        Request request = new Request.Builder()
                .url(getScopeUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, "Bearer " + accessTokenInfo.getAccess_token())
                .head()
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HttpStatus.SC_OK) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(), apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return isSharedScopeNameExists(apiApplicationKey, refreshedAccessToken, key);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                log.info(response);
                throw new BadRequestException(response.toString());
            } else {
                return false;
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            throw new APIApplicationServicesException(msg);
        }
    }

    public boolean updateSharedScope(APIApplicationKey apiApplicationKey, AccessTokenInfo accessTokenInfo, Scope scope)
            throws APIApplicationServicesException, BadRequestException {

        String updateScopeUrl = "https://localhost:9443/api/am/publisher/v2/scopes/" + scope.getId();

        ScopeUtils scopeUtil = new ScopeUtils();
        scopeUtil.setKey(scope.getKey());
        scopeUtil.setName(scope.getName());
        scopeUtil.setDescription(scope.getDescription());
        scopeUtil.setRoles(scope.getRoles());
        String scopeString = scopeUtil.toJSON();

        RequestBody requestBody = RequestBody.create(JSON, scopeString);
        Request request = new Request.Builder()
                .url(updateScopeUrl)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, "Bearer " + accessTokenInfo.getAccess_token())
                .put(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.code() == HttpStatus.SC_OK) {
                return true;
            } else if (HttpStatus.SC_UNAUTHORIZED == response.code()) {
                APIApplicationServices apiApplicationServices = new APIApplicationServicesImpl();
                AccessTokenInfo refreshedAccessToken = apiApplicationServices.
                        generateAccessTokenFromRefreshToken(accessTokenInfo.getRefresh_token(), apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());
                //TODO: max attempt count
                return updateSharedScope(apiApplicationKey, refreshedAccessToken, scope);
            } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                log.info(response);
                throw new BadRequestException(response.toString());
            } else {
                return false;
            }
        } catch (IOException e) {
            String msg = "Error occurred while processing the response";
            throw new APIApplicationServicesException(msg);
        }
    }
}
