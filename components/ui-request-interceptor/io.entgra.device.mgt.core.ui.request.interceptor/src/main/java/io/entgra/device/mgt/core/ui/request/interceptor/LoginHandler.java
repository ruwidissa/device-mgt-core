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

package io.entgra.device.mgt.core.ui.request.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.gson.JsonSyntaxException;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.cache.LoginCache;
import io.entgra.device.mgt.core.ui.request.interceptor.cache.OAuthApp;
import io.entgra.device.mgt.core.ui.request.interceptor.cache.OAuthAppCacheKey;
import io.entgra.device.mgt.core.ui.request.interceptor.exceptions.LoginException;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@MultipartConfig
@WebServlet("/login")
public class LoginHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginHandler.class);
    private static final long serialVersionUID = 9050048549140517002L;

    private static String username;
    private static String password;
    private static String gatewayUrl;
    private static String uiConfigUrl;
    private static String iotCoreUrl;
    private static String kmManagerUrl;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            validateLoginRequest(req);
            HttpSession httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }
            httpSession = req.getSession(true);

            JsonNode uiConfigJsonObject = HandlerUtil.getUIConfigAndPersistInSession(uiConfigUrl, gatewayUrl, httpSession,
                    resp);
            ArrayNode tags = (ArrayNode) uiConfigJsonObject.get("appRegistration").get("tags");
            ArrayNode scopes = (ArrayNode) uiConfigJsonObject.get("scopes");
            int sessionTimeOut = Integer.parseInt(String.valueOf(uiConfigJsonObject.get("sessionTimeOut")));

            //setting session to expire in 1h
            httpSession.setMaxInactiveInterval(sessionTimeOut);

            // Check if OAuth app cache exists. If not create a new application.
            LoginCache loginCache = HandlerUtil.getLoginCache(httpSession);
            OAuthAppCacheKey oAuthAppCacheKey = new OAuthAppCacheKey(HandlerConstants.PUBLISHER_APPLICATION_NAME, username);
            OAuthApp oAuthApp = loginCache.getOAuthAppCache(oAuthAppCacheKey);

            if (oAuthApp == null) {

                ClassicHttpRequest apiRegEndpoint = ClassicRequestBuilder.post(gatewayUrl + HandlerConstants.APP_REG_ENDPOINT)
                        .setEntity(HandlerUtil.constructAppRegPayload(tags, HandlerConstants.PUBLISHER_APPLICATION_NAME,
                                username, password, null, null))
                        .setHeader(org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE,
                                org.apache.hc.core5.http.ContentType.APPLICATION_JSON.toString())
                        .setHeader(org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + Base64.getEncoder().encodeToString((username + HandlerConstants.COLON + password).getBytes()))
                        .build();

                ProxyResponse clientAppResponse = HandlerUtil.execute(apiRegEndpoint);

                if (clientAppResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                    HandlerUtil.handleError(resp, clientAppResponse);
                    return;
                }

                if (clientAppResponse.getCode() == HttpStatus.SC_CREATED) {
                    JsonNode jsonNode = clientAppResponse.getData();
                    String clientId = null;
                    String clientSecret = null;
                    String encodedClientApp = null;
                    if (jsonNode != null) {
                        clientId = jsonNode.get("client_id").textValue();
                        clientSecret = jsonNode.get("client_secret").textValue();
                        encodedClientApp = Base64.getEncoder()
                                .encodeToString((clientId + HandlerConstants.COLON + clientSecret).getBytes());
                        oAuthApp = new OAuthApp(
                                HandlerConstants.PUBLISHER_APPLICATION_NAME,
                                username,
                                clientId,
                                clientSecret,
                                encodedClientApp
                        );
                        loginCache.addOAuthAppToCache(oAuthAppCacheKey, oAuthApp);
                    }

                    if (getTokenAndPersistInSession(req, resp, clientId, clientSecret, encodedClientApp, scopes)) {
                        ProxyResponse proxyResponse = new ProxyResponse();
                        proxyResponse.setStatus(ProxyResponse.Status.SUCCESS);
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
                                                ArrayNode scopes) throws LoginException {
        try {

            ProxyResponse tokenResultResponse = getTokenResult(encodedClientApp, scopes);

            if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API to get token data.");
                HandlerUtil.handleError(resp, tokenResultResponse);
                return false;
            }
            JsonNode tokenResult = tokenResultResponse.getData();
            if (tokenResult == null) {
                log.error("Invalid token response is received.");
                HandlerUtil.handleError(resp, tokenResultResponse);
                return false;
            }

            HttpSession session = req.getSession(false);
            if (session == null) {
                return false;
            }
            AuthData authData = new AuthData();
            authData.setClientId(clientId);
            authData.setClientSecret(clientSecret);
            authData.setEncodedClientApp(encodedClientApp);
            authData.setAccessToken(tokenResult.get("access_token").textValue());
            authData.setRefreshToken(tokenResult.get("refresh_token").textValue());
            authData.setScope(tokenResult.get("scope"));
            session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, authData);
            return true;
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
        username = req.getParameter("username");
        password = req.getParameter("password");
        gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
        iotCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getCorePort(req.getScheme());
        uiConfigUrl = iotCoreUrl + HandlerConstants.UI_CONFIG_ENDPOINT;
        kmManagerUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(req.getScheme());

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
    private ProxyResponse getTokenResult(String encodedClientApp, JsonNode scopes) throws IOException {
        String scopeString = HandlerUtil.getScopeString(scopes);
        if (scopeString != null) {
            scopeString = scopeString.trim();
        } else {
            scopeString = "default";
        }

        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("grant_type", HandlerConstants.PASSWORD_GRANT_TYPE));
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", password));
        nameValuePairs.add(new BasicNameValuePair("scope", scopeString));


        ClassicHttpRequest tokenEndpoint = ClassicRequestBuilder.post(gatewayUrl + HandlerConstants.INTERNAL_TOKEN_ENDPOINT)
                .setEntity(new UrlEncodedFormEntity(nameValuePairs))
                .setHeader(org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE,
                        org.apache.hc.core5.http.ContentType.APPLICATION_FORM_URLENCODED.toString())
                .setHeader(org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + encodedClientApp)
                .build();
        return HandlerUtil.execute(tokenEndpoint);
    }
}
