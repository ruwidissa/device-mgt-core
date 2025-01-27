/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.apimgt.extension.rest.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.IDNApplicationKeys;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.OAuthClientResponse;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.OAuthClientException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.util.HttpsTrustManagerUtils;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OAuthClient implements IOAuthClientService {

    private static final Log log = LogFactory.getLog(OAuthClient.class);
    private static final OkHttpClient client = new OkHttpClient(HttpsTrustManagerUtils.getSSLClient().newBuilder());
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String IDN_DCR_CLIENT_PREFIX = "_REST_API_INVOKER_SERVICE";
    private static final String IDN_REST_API_INVOKER_USER = "rest_service_reserved_user";
    private static final String IDN_REST_API_INVOKER_USER_PWD = "rest_service_reserved_user";
    private static final APIManagerConfiguration config =
            ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    private static final String tokenEndpoint = config.getFirstProperty(Constants.TOKE_END_POINT);
    private static final String dcrEndpoint = config.getFirstProperty(Constants.DCR_END_POINT);
    private static final Map<String, CacheWrapper> cache = new ConcurrentHashMap<>();
    private static final int MAX_RETRY_ATTEMPT = 2;

    private OAuthClient() {
    }

    public static OAuthClient getInstance() {
        return OAuthClientHolder.INSTANCE;
    }

    /**
     * Handle execution of a APIM REST services invocation request. Token and cache handling will be handled by the
     * service itself.
     *
     * @param request Instance of {@link Request} to execute
     * @return Instance of {@link OAuthClientResponse} when successful invocation happens
     * @throws OAuthClientException        {@link OAuthClientException}
     * @throws BadRequestException         {@link BadRequestException}
     * @throws UnexpectedResponseException {@link UnexpectedResponseException}
     */
    @Override
    public OAuthClientResponse execute(Request request) throws OAuthClientException, BadRequestException,
            UnexpectedResponseException {
        int currentRetryAttempt = 0;
        OAuthClientResponse oAuthClientResponse;

        while (true) {
            try {
                request = intercept(request);
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        oAuthClientResponse = mapToOAuthClientResponse(response);
                        break;
                    }

                    if (response.code() == HttpStatus.SC_NOT_FOUND) {
                        if (log.isDebugEnabled()) {
                            log.info("Resource not found for the request [ " + request.url() + " ]");
                        }
                        oAuthClientResponse = mapToOAuthClientResponse(response);
                        break;
                    }

                    // entering to the retrying phase, so increment the counter
                    currentRetryAttempt++;
                    if (response.code() == HttpStatus.SC_UNAUTHORIZED) {
                        if (currentRetryAttempt <= MAX_RETRY_ATTEMPT) {
                            refresh();
                        } else {
                            String msg =
                                    "Request [ " + request.url() + " ] failed with code : [ " + response.code() + " ]" +
                                            " & body : [ " + (response.body() != null ?
                                            response.body().string() : " empty body received!") + " ]";
                            log.error(msg);
                            throw new UnexpectedResponseException(msg);
                        }
                    } else if (HttpStatus.SC_BAD_REQUEST == response.code()) {
                        String msg =
                                "Encountered a bad request! Request [ " + request.url() + " ] failed with code : " +
                                        "[ " + response.code() + " ] & body : [ " + (response.body() != null ?
                                        response.body().string() : " empty body received!") + " ]";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    } else {
                        String msg =
                                "Request [ " + request.url() + " ]failed with code : [ " + response.code() + " ] & " +
                                        "body : [ " + (response.body() != null ? response.body().string() : " empty " +
                                        "body received!") + " ]";
                        log.error(msg);
                        throw new UnexpectedResponseException(msg);
                    }
                }
            } catch (IOException ex) {
                String msg =
                        "Error occurred while executing the request : [ " + request.method() + ":" + request.url() +
                                " ]";
                log.error(msg, ex);
                throw new OAuthClientException(msg, ex);
            }
        }
        return oAuthClientResponse;
    }

    /**
     * Create and retrieve identity server side service provider applications
     *
     * @param clientName IDN client name
     * @return {@link IDNApplicationKeys}
     * @throws OAuthClientException Throws when error encountered while IDN client creation
     */
    @Override
    public IDNApplicationKeys getIdnApplicationKeys(String clientName) throws OAuthClientException {
        try {
            Keys keys =
                    idnDynamicClientRegistration(clientName);
            return new IDNApplicationKeys(keys.consumerKey, keys.consumerSecret);
        } catch (IOException e) {
            String msg = "IO exception encountered while registering DCR client for OPAQUE token generation";
            log.error(msg, e);
            throw new OAuthClientException(msg, e);
        }
    }

    /**
     * Dynamic client registration will be handled through here. These clients can be located under carbon console's
     * service provider section in respective tenants.
     *
     * @return Instance of {@link Keys} containing the dcr client's credentials
     * @throws IOException          Throws when error encountered while executing dcr request
     * @throws OAuthClientException Throws when failed to register dcr client
     */
    private Keys idnDynamicClientRegistration(String tenantAwareClientName) throws IOException, OAuthClientException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        createRestClientInvokerUserIfNotExists(tenantDomain);

        List<String> grantTypes = Arrays.asList(Constants.PASSWORD_GRANT_TYPE, Constants.REFRESH_TOKEN_GRANT_TYPE);
        String dcrRequestJsonStr = (new JSONObject())
                .put("clientName", tenantAwareClientName)
                .put("owner", IDN_REST_API_INVOKER_USER + "@" + tenantDomain)
                .put("saasApp", true)
                .put("grantType", String.join(Constants.SPACE, grantTypes))
                .put("tokenType", "Default")
                .toString();

        RequestBody requestBody = RequestBody.Companion.create(dcrRequestJsonStr, JSON);
        Request dcrRequest = new Request.Builder()
                .url(dcrEndpoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME,
                        Credentials.basic(IDN_REST_API_INVOKER_USER + "@" + tenantDomain
                                , IDN_REST_API_INVOKER_USER_PWD))
                .post(requestBody)
                .build();

        try (Response response = client.newCall(dcrRequest).execute()) {
            if (response.isSuccessful()) {
                return mapKeys(response.body());
            }
        }

        String msg = "Error encountered while processing DCR request. Tried client : [ " + tenantAwareClientName + " ]";
        log.error(msg);
        throw new OAuthClientException(msg);
    }

    /**
     * Token obtaining procedure will be handled here. Since the required permissions for invoking the APIM REST
     * services are available for the tenant admins, this procedure will use admin credentials for obtaining tokens.
     * Also, please note that these tokens are only use for invoking APIM REST services only. The password grant uses
     * since it facilitates the use of refresh token.
     *
     * @param keys Dcr client credentials to obtain a token
     * @return Instance of {@link Tokens} containing the tokens
     * @throws IOException          Throws when error encountered while executing token request
     * @throws OAuthClientException Throws when failed to obtain tokens
     */
    private Tokens idnTokenGeneration(Keys keys) throws IOException, OAuthClientException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String tokenRequestJsonStr = (new JSONObject())
                .put("grant_type", Constants.PASSWORD_GRANT_TYPE)
                .put("username", IDN_REST_API_INVOKER_USER + "@" + tenantDomain)
                .put("password", IDN_REST_API_INVOKER_USER_PWD)
                .put("scope", Constants.SCOPES)
                .put("callbackUrl", Constants.PLACEHOLDING_CALLBACK_URL)
                .toString();

        RequestBody requestBody = RequestBody.Companion.create(tokenRequestJsonStr, JSON);
        Request tokenRequest = new Request.Builder()
                .url(tokenEndpoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic(keys.consumerKey,
                        keys.consumerSecret))
                .post(requestBody)
                .build();

        try (Response response = client.newCall(tokenRequest).execute()) {
            if (response.isSuccessful()) {
                return mapTokens(response.body());
            }
        }

        String msg = "Error encountered while processing token registration request";
        log.error(msg);
        throw new OAuthClientException(msg);
    }

    /**
     * Obtain and refresh idn auth tokens. Note that in the first try it try to obtain new tokens via refresh_token
     * grant type, if it fails it tries to obtain new tokens via password grant type.
     *
     * @param keys         Instance of {@link Keys} containing the dcr client's credentials
     * @param refreshToken Refresh token
     * @return Instance of {@link Tokens} containing the tokens
     * @throws IOException          Throws when error encountered while executing token request
     * @throws OAuthClientException Throws when failed to obtain tokens
     */
    private Tokens idnTokenRefresh(Keys keys, String refreshToken) throws IOException,
            OAuthClientException {
        String tokenRequestJsonStr = (new JSONObject())
                .put("grant_type", Constants.REFRESH_TOKEN_GRANT_TYPE)
                .put("refresh_token", refreshToken)
                .put("scope", Constants.SCOPES)
                .toString();

        RequestBody requestBody = RequestBody.Companion.create(tokenRequestJsonStr, JSON);
        Request tokenRequest = new Request.Builder()
                .url(tokenEndpoint)
                .addHeader(Constants.AUTHORIZATION_HEADER_NAME, Credentials.basic(keys.consumerKey,
                        keys.consumerSecret))
                .post(requestBody)
                .build();

        try (Response response = client.newCall(tokenRequest).execute()) {
            if (response.isSuccessful()) {
                return mapTokens(response.body());
            } else {
                return idnTokenGeneration(keys);
            }
        }
    }

    /**
     * Intercept the request and add authorization header base on available tokens.
     *
     * @param request Instance of the {@link Request} to intercept
     * @return Intercepted request
     * @throws OAuthClientException Throws when error encountered while adding authorization header
     */
    private Request intercept(Request request) throws OAuthClientException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        CacheWrapper cacheWrapper = cache.computeIfAbsent(tenantDomain, key -> {
            CacheWrapper constructedWrapper = null;
            try {
                Keys keys = idnDynamicClientRegistration(tenantDomain.toUpperCase() + IDN_DCR_CLIENT_PREFIX);
                Tokens tokens = idnTokenGeneration(keys);
                constructedWrapper = new CacheWrapper(keys, tokens);
            } catch (OAuthClientException e) {
                log.error("Failed to register DCR client and obtain a token", e);
            } catch (IOException e) {
                log.error("Error encountered in token acquiring process", e);
            } catch (Exception e) {
                log.error("Error encountered while updating the cache", e);
            }
            return constructedWrapper;
        });

        if (cacheWrapper == null) {
            String msg = "Failed to obtain tokens. Hence aborting request intercepting sequence";
            log.error(msg);
            throw new OAuthClientException(msg);
        }

        return new Request.Builder(request).header(Constants.AUTHORIZATION_HEADER_NAME,
                Constants.AUTHORIZATION_HEADER_PREFIX_BEARER + cacheWrapper.tokens.accessToken).build();
    }

    /**
     * Refresh cached tokens.
     *
     * @throws OAuthClientException Throws when error encountered while refreshing the tokens
     */
    private void refresh() throws OAuthClientException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        CacheWrapper cacheWrapper = cache.computeIfPresent(tenantDomain, (key, value) -> {
            CacheWrapper updatedCacheWrapper = null;
            try {
                Tokens tokens = idnTokenRefresh(value.keys, value.tokens.refreshToken);
                updatedCacheWrapper = new CacheWrapper(value.keys, tokens);
            } catch (OAuthClientException e) {
                log.error("Failed to refresh the token using refresh token", e);
            } catch (IOException e) {
                log.error("Error encountered while executing token retrieving request", e);
            } catch (Exception e) {
                log.error("Error encountered while updating the cache", e);
            }
            return updatedCacheWrapper;
        });

        if (cacheWrapper == null) {
            String msg = "Failed to refresh tokens. Hence aborting request executing process";
            log.error(msg);
            throw new OAuthClientException(msg);
        }
    }

    private Keys mapKeys(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            String msg = "Received empty request body for mapping into keys";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        Keys keys = new Keys();
        JsonObject jsonObject = gson.fromJson(responseBody.string(), JsonObject.class);
        keys.consumerKey = jsonObject.get("clientId").getAsString();
        keys.consumerSecret = jsonObject.get("clientSecret").getAsString();
        return keys;
    }

    private Tokens mapTokens(ResponseBody responseBody) throws IOException {
        if (responseBody == null) {
            String msg = "Received empty request body for mapping into tokens";
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        Tokens tokens = new Tokens();
        JsonObject jsonObject = gson.fromJson(responseBody.string(), JsonObject.class);
        tokens.accessToken = jsonObject.get("access_token").getAsString();
        tokens.refreshToken = jsonObject.get("refresh_token").getAsString();
        return tokens;
    }

    private OAuthClientResponse mapToOAuthClientResponse(Response response) throws IOException {
        return new OAuthClientResponse(response.code(),
                response.body() != null ? response.body().string() : null, response.isSuccessful());
    }

    public void createRestClientInvokerUserIfNotExists(String tenantDomain) throws OAuthClientException {
        try {
            UserStoreManager userStoreManager =
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
            if (!userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(IDN_REST_API_INVOKER_USER))) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating user '" + IDN_REST_API_INVOKER_USER + "' in '" + tenantDomain + "' tenant " +
                            "domain.");
                }
                String[] roles = {Constants.ADMIN_ROLE_KEY};
                userStoreManager.addUser(
                        MultitenantUtils.getTenantAwareUsername(IDN_REST_API_INVOKER_USER),
                        IDN_REST_API_INVOKER_USER_PWD,
                        roles,
                        null,
                        ""
                );
            }
        } catch (UserStoreException e) {
            String msg =
                    "Error occurred while creating " + IDN_REST_API_INVOKER_USER + "in tenant: '" + tenantDomain + "'.";
            log.error(msg);
            throw new OAuthClientException(msg, e);
        }
    }

    /**
     * Holder for {@link OAuthClient} instance
     */
    private static class OAuthClientHolder {
        private static final OAuthClient INSTANCE = new OAuthClient();
    }

    /**
     * Act as an internal data class for containing dcr credentials, hence no need of expose as a bean
     */
    private static class Keys {
        String consumerKey;
        String consumerSecret;
    }

    /**
     * Act as an internal data class for containing dcr tokens, hence no need of expose as a bean
     */
    private static class Tokens {
        String accessToken;
        String refreshToken;
    }

    /**
     * Act as an internal data class for containing cached tokens and keys, hence no need of expose as a bean
     */
    private static class CacheWrapper {
        Keys keys;
        Tokens tokens;

        CacheWrapper(Keys keys, Tokens tokens) {
            this.keys = keys;
            this.tokens = tokens;
        }
    }
}
