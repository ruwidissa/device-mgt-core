/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.ui.request.interceptor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.cache.LoginCacheManager;
import io.entgra.ui.request.interceptor.cache.OAuthApp;
import io.entgra.ui.request.interceptor.cache.OAuthAppCacheKey;
import io.entgra.ui.request.interceptor.exceptions.LoginException;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import org.json.JSONString;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;

@MultipartConfig
@WebServlet("/login")
public class LoginHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginHandler.class);
    private static final long serialVersionUID = 9050048549140517002L;

    private static String username;
    private static String password;
    private static String gatewayUrl;
    private static String uiConfigUrl;
    private static String iotsCoreUrl;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            validateLoginRequest(req);
            HttpSession httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }
            httpSession = req.getSession(true);

            JsonObject uiConfigJsonObject = HandlerUtil.getUIConfigAndPersistInSession(uiConfigUrl, gatewayUrl, httpSession, resp);
            JsonArray tags = uiConfigJsonObject.get("appRegistration").getAsJsonObject().get("tags").getAsJsonArray();
            JsonArray scopes = uiConfigJsonObject.get("scopes").getAsJsonArray();
            int sessionTimeOut = Integer.parseInt(String.valueOf(uiConfigJsonObject.get("sessionTimeOut")));

            //setting session to expire in 1h
            httpSession.setMaxInactiveInterval(sessionTimeOut);

            // Check if OAuth app cache exists. If not create a new application.
            LoginCacheManager loginCacheManager = new LoginCacheManager();
            loginCacheManager.initializeCacheManager();
            OAuthAppCacheKey oAuthAppCacheKey = new OAuthAppCacheKey(HandlerConstants.PUBLISHER_APPLICATION_NAME, username);
            OAuthApp oAuthApp = loginCacheManager.getOAuthAppCache(oAuthAppCacheKey);

            if (oAuthApp == null) {
                HttpPost apiRegEndpoint = new HttpPost(gatewayUrl + HandlerConstants.APP_REG_ENDPOINT);
                apiRegEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + Base64.getEncoder()
                        .encodeToString((username + HandlerConstants.COLON + password).getBytes()));
                apiRegEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                apiRegEndpoint.setEntity(HandlerUtil.constructAppRegPayload(tags, HandlerConstants.PUBLISHER_APPLICATION_NAME, username, password));

                ProxyResponse clientAppResponse = HandlerUtil.execute(apiRegEndpoint);

                if (clientAppResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                    HandlerUtil.handleError(resp, clientAppResponse);
                    return;
                }

                if (clientAppResponse.getCode() == HttpStatus.SC_CREATED) {
                    JsonParser jsonParser = new JsonParser();
                    JsonElement jClientAppResult = jsonParser.parse(clientAppResponse.getData());
                    String clientId = null;
                    String clientSecret = null;
                    String encodedClientApp = null;
                    if (jClientAppResult.isJsonObject()) {
                        JsonObject jClientAppResultAsJsonObject = jClientAppResult.getAsJsonObject();
                        clientId = jClientAppResultAsJsonObject.get("client_id").getAsString();
                        clientSecret = jClientAppResultAsJsonObject.get("client_secret").getAsString();
                        encodedClientApp = Base64.getEncoder()
                                .encodeToString((clientId + HandlerConstants.COLON + clientSecret).getBytes());

                        oAuthAppCacheKey = new OAuthAppCacheKey(HandlerConstants.PUBLISHER_APPLICATION_NAME, username);
                        oAuthApp = new OAuthApp(
                                HandlerConstants.PUBLISHER_APPLICATION_NAME,
                                username,
                                clientId,
                                clientSecret,
                                encodedClientApp
                        );
                        loginCacheManager.addOAuthAppToCache(oAuthAppCacheKey, oAuthApp);
                    }

                    if (getTokenAndPersistInSession(req, resp, clientId, clientSecret, encodedClientApp, scopes)) {
                        ProxyResponse proxyResponse = new ProxyResponse();
                        proxyResponse.setCode(HttpStatus.SC_OK);
                        HandlerUtil.handleSuccess(resp, proxyResponse);
                        return;
                    }
                }
                HandlerUtil.handleError(resp, null);
            } else {
                getTokenAndPersistInSession(req, resp, oAuthApp.getClientId(), oAuthApp.getClientSecret(), oAuthApp.getEncodedClientApp(), scopes);
            }
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        } catch (LoginException e) {
            log.error("Error occurred while getting token data. ", e);
        }
    }

    /**
     * Generates token from token endpoint and persists them inside the session
     *
     * @param req              - {@link HttpServletRequest}
     * @param resp             - {@link HttpServletResponse}
     * @param clientId         - clientId of the OAuth app
     * @param clientSecret     - clientSecret of the OAuth app
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @param scopes           - User scopes JSON Array
     * @return boolean response
     * @throws LoginException - Throws if any error occurs while getting login response
     */
    private boolean getTokenAndPersistInSession(HttpServletRequest req, HttpServletResponse resp,
                                                String clientId, String clientSecret, String encodedClientApp,
                                                JsonArray scopes) throws LoginException {
        JsonParser jsonParser = new JsonParser();
        try {

            ProxyResponse tokenResultResponse = getTokenResult(encodedClientApp, scopes);

            if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API to get token data.");
                HandlerUtil.handleError(resp, tokenResultResponse);
                return false;
            }
            String tokenResult = tokenResultResponse.getData();
            if (tokenResult == null) {
                log.error("Invalid token response is received.");
                HandlerUtil.handleError(resp, tokenResultResponse);
                return false;
            }

            JsonElement jTokenResult = jsonParser.parse(tokenResult);
            if (jTokenResult.isJsonObject()) {
                JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
                HttpSession session = req.getSession(false);
                if (session == null) {
                    return false;
                }
                AuthData authData = new AuthData();
                authData.setClientId(clientId);
                authData.setClientSecret(clientSecret);
                authData.setEncodedClientApp(encodedClientApp);
                authData.setAccessToken(jTokenResultAsJsonObject.get("access_token").getAsString());
                authData.setRefreshToken(jTokenResultAsJsonObject.get("refresh_token").getAsString());
                authData.setScope(jTokenResultAsJsonObject.get("scope").getAsString());
                session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, authData);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new LoginException("Error occurred while sending the response into the socket", e);
        }
    }

    /***
     *
     * @param req - {@link HttpServletRequest}
     * Define username and password static parameters.
     */
    private static void validateLoginRequest(HttpServletRequest req) throws LoginException {
        String iotsCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTPS_PORT_ENV_VAR);
        if (HandlerConstants.HTTP_PROTOCOL.equals(req.getScheme())) {
            iotsCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTP_PORT_ENV_VAR);
        }
        username = req.getParameter("username");
        password = req.getParameter("password");
        gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
        uiConfigUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                + HandlerConstants.COLON + iotsCorePort + HandlerConstants.UI_CONFIG_ENDPOINT;
        iotsCoreUrl = HandlerConstants.HTTPS_PROTOCOL + HandlerConstants.SCHEME_SEPARATOR +
                System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR) + HandlerConstants.COLON + iotsCorePort;
        if (username == null || password == null) {
            String msg = "Invalid login request. Username or Password is not received for login request.";
            log.error(msg);
            throw new LoginException(msg);
        }
    }

    /***
     * Generates tokens by invoking token endpoint
     *
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @param scopes - Scopes which are retrieved by reading application-mgt configuration
     * @return Invoke token endpoint and return the response as string.
     * @throws IOException IO exception throws if an error occurred when invoking token endpoint
     */
    private ProxyResponse getTokenResult(String encodedClientApp, JsonArray scopes) throws IOException {
        HttpPost tokenEndpoint = new HttpPost(iotsCoreUrl+ HandlerConstants.TOKEN_ENDPOINT);
        tokenEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + encodedClientApp);
        tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
        String scopeString = HandlerUtil.getScopeString(scopes);

        if (scopeString != null) {
            scopeString = scopeString.trim();
        } else {
            scopeString = "default";
        }

        StringEntity tokenEPPayload = new StringEntity(
                "grant_type=" + HandlerConstants.PASSWORD_GRANT_TYPE + "&username=" + username + "&password=" +
                        password + "&scope=" + scopeString,
                ContentType.APPLICATION_FORM_URLENCODED);
        tokenEndpoint.setEntity(tokenEPPayload);
        return HandlerUtil.execute(tokenEndpoint);
    }
}
