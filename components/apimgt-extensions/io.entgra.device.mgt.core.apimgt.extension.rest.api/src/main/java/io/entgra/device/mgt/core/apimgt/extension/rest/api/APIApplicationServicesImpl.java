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
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.internal.APIManagerServiceDataHolder;
import org.json.JSONObject;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.HttpsTrustManagerUtils;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIApplicationKey;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.Credentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class APIApplicationServicesImpl implements APIApplicationServices {

    private static final Log log = LogFactory.getLog(APIApplicationServicesImpl.class);
    private static final OkHttpClient client = new OkHttpClient(HttpsTrustManagerUtils.getSSLClient().newBuilder());
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    String msg = null;
    APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
            getAPIManagerConfigurationService().getAPIManagerConfiguration();

    @Override
    public APIApplicationKey createAndRetrieveApplicationCredentials()
            throws APIServicesException {

        log.error("=====createAndRetrieveApplicationCredentials=====1");

        String serverUser = null;
        String serverPassword = null;
        try {
            log.error("=====createAndRetrieveApplicationCredentials=====2");
            UserRealm userRealm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();

            createUserIfNotExists(Constants.RESERVED_USER_NAME, Constants.RESERVED_USER_PASSWORD, userStoreManager);

            if(tenantDomain.equals("carbon.super")) {
                log.error("=====createAndRetrieveApplicationCredentials=====3");
                serverUser = config.getFirstProperty(Constants.SERVER_USER);
                serverPassword = config.getFirstProperty(Constants.SERVER_PASSWORD);
            } else {
                log.error("=====createAndRetrieveApplicationCredentials=====4");
                serverUser = Constants.RESERVED_USER_NAME + "@" + tenantDomain;
                serverPassword = Constants.RESERVED_USER_PASSWORD;
            }
        } catch (UserStoreException e) {
            throw new RuntimeException(e);
        }

        String applicationEndpoint = config.getFirstProperty(Constants.DCR_END_POINT);

        log.error("=====createAndRetrieveApplicationCredentials=====5");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("callbackUrl", Constants.EMPTY_STRING);
        jsonObject.put("clientName", Constants.CLIENT_NAME);
        jsonObject.put("grantType", Constants.GRANT_TYPE);
        jsonObject.put("owner", serverUser);
        jsonObject.put("saasApp", true);

        log.error("=====createAndRetrieveApplicationCredentials=====6");

        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(applicationEndpoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic(serverUser, serverPassword))
                .post(requestBody)
                .build();

        log.error("=====createAndRetrieveApplicationCredentials=====7");

        try {
            log.error("=====createAndRetrieveApplicationCredentials=====8");
            try (Response response = client.newCall(request).execute()) {
                log.error("=====createAndRetrieveApplicationCredentials=====9");
                return gson.fromJson(response.body().string(), APIApplicationKey.class);
            }
        } catch (IOException e) {
            msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(e);
        }
    }

    @Override
    public void createAndRetrieveApplicationCredentialsAndGenerateToken()
            throws APIServicesException {

        log.error("=====createAndRetrieveApplicationCredentials=====1");

        String serverUser = null;
        String serverPassword = null;
        try {
            log.error("=====createAndRetrieveApplicationCredentials=====2");
            UserRealm userRealm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();

            createUserIfNotExists(Constants.RESERVED_USER_NAME, Constants.RESERVED_USER_PASSWORD, userStoreManager);

            if(tenantDomain.equals("carbon.super")) {
                log.error("=====createAndRetrieveApplicationCredentials=====3");
                serverUser = config.getFirstProperty(Constants.SERVER_USER);
                serverPassword = config.getFirstProperty(Constants.SERVER_PASSWORD);
            } else {
                log.error("=====createAndRetrieveApplicationCredentials=====4");
                serverUser = Constants.RESERVED_USER_NAME + "@" + tenantDomain;
                serverPassword = Constants.RESERVED_USER_PASSWORD;
            }
        } catch (UserStoreException e) {
            throw new RuntimeException(e);
        }

        String applicationEndpoint = config.getFirstProperty(Constants.DCR_END_POINT);

        log.error("=====createAndRetrieveApplicationCredentials=====5");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("callbackUrl", Constants.EMPTY_STRING);
        jsonObject.put("clientName", Constants.CLIENT_NAME);
        jsonObject.put("grantType", Constants.GRANT_TYPE);
        jsonObject.put("owner", serverUser);
        jsonObject.put("saasApp", true);

        log.error("=====createAndRetrieveApplicationCredentials=====6");

        RequestBody requestBody = RequestBody.Companion.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(applicationEndpoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic(serverUser, serverPassword))
                .post(requestBody)
                .build();

        log.error("=====createAndRetrieveApplicationCredentials=====7");

        try {
            log.error("=====createAndRetrieveApplicationCredentials=====8");
            try (Response response = client.newCall(request).execute()) {
                log.error("=====createAndRetrieveApplicationCredentials=====9");
                APIApplicationKey apiApplicationKey = gson.fromJson(response.body().string(), APIApplicationKey.class);
                AccessTokenInfo accessTokenInfo = generateAccessTokenFromRegisteredApplication(
                    apiApplicationKey.getClientId(), apiApplicationKey.getClientSecret());

                PublisherRESTAPIServices publisherRESTAPIServices = new PublisherRESTAPIServicesImpl();

                Scope[] scopes = publisherRESTAPIServices.getScopes(apiApplicationKey, accessTokenInfo);

            } catch (BadRequestException e) {
                throw new RuntimeException(e);
            } catch (UnexpectedResponseException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(e);
        }
    }

    @Override
    public AccessTokenInfo generateAccessTokenFromRegisteredApplication(String consumerKey, String consumerSecret)
            throws APIServicesException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String userName = null;
        String userPassword = null;
        if(tenantDomain.equals("carbon.super")) {
            userName = config.getFirstProperty(Constants.SERVER_USER);
            userPassword = config.getFirstProperty(Constants.SERVER_PASSWORD);
        } else {
            userName = "shamalka@shamalka.com";
            userPassword = "admin";
        }

        JSONObject params = new JSONObject();
        params.put(Constants.GRANT_TYPE_PARAM_NAME, Constants.PASSWORD_GRANT_TYPE);
        params.put(Constants.PASSWORD_GRANT_TYPE_USERNAME, userName);
        params.put(Constants.PASSWORD_GRANT_TYPE_PASSWORD, userPassword);
        params.put(Constants.SCOPE_PARAM_NAME, Constants.SCOPES);
        return getToken(params, consumerKey, consumerSecret);
    }

    @Override
    public AccessTokenInfo generateAccessTokenFromRefreshToken(String refreshToken, String consumerKey, String consumerSecret)
            throws APIServicesException {

        JSONObject params = new JSONObject();
        params.put(Constants.GRANT_TYPE_PARAM_NAME, Constants.REFRESH_TOKEN_GRANT_TYPE);
        params.put(Constants.REFRESH_TOKEN_GRANT_TYPE_PARAM_NAME, refreshToken);
        params.put(Constants.SCOPE_PARAM_NAME, Constants.SCOPES);
        return getToken(params, consumerKey, consumerSecret);
    }

    public AccessTokenInfo getToken(JSONObject nameValuePairs, String clientId, String clientSecret)
            throws APIServicesException {

        String tokenEndPoint = config.getFirstProperty(Constants.TOKE_END_POINT);

        RequestBody requestBody = RequestBody.Companion.create(nameValuePairs.toString(), JSON);
        Request request = new Request.Builder()
                .url(tokenEndPoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic(clientId, clientSecret))
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return gson.fromJson(response.body().string(), AccessTokenInfo.class);
        } catch (IOException e) {
            msg = "Error occurred while processing the response";
            log.error(msg, e);
            throw new APIServicesException(e);
        }
    }

    private void createUserIfNotExists(String username, String password, UserStoreManager userStoreManager) {

        try {
            if (!userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(username))) {
                String[] roles = {"admin"};
                userStoreManager.addUser(MultitenantUtils.getTenantAwareUsername(username), password, roles, null, "");

//                userStoreManager.updateCredential(MultitenantUtils.getTenantAwareUsername(username), "reservedpwd", password);
            }
        } catch (UserStoreException e) {
            String msg = "Error when trying to fetch tenant details";
            log.error(msg);
        }
    }

    private String generateInitialUserPassword() {
        int passwordLength = 6;
        //defining the pool of characters to be used for initial password generation
        String lowerCaseCharset = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numericCharset = "0123456789";
        SecureRandom randomGenerator = new SecureRandom();
        String totalCharset = lowerCaseCharset + upperCaseCharset + numericCharset;
        int totalCharsetLength = totalCharset.length();
        StringBuilder initialUserPassword = new StringBuilder();
        for (int i = 0; i < passwordLength; i++) {
            initialUserPassword.append(
                    totalCharset.charAt(randomGenerator.nextInt(totalCharsetLength)));
        }
        if (log.isDebugEnabled()) {
            log.debug("Initial user password is created for new user: " + initialUserPassword);
        }
        return initialUserPassword.toString();
    }
}
