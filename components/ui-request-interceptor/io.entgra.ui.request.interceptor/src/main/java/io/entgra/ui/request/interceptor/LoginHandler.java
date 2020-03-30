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
import io.entgra.ui.request.interceptor.exceptions.LoginException;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            validateLoginRequest(req);
            HttpSession httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }
            httpSession = req.getSession(true);
            //setting session to expiry in 5 minutes
            httpSession.setMaxInactiveInterval(Math.toIntExact(HandlerConstants.TIMEOUT));

            HttpGet uiConfigEndpoint = new HttpGet(uiConfigUrl);
            JsonParser jsonParser = new JsonParser();
            ProxyResponse uiConfigResponse = HandlerUtil.execute(uiConfigEndpoint);
            String executorResponse = uiConfigResponse.getExecutorResponse();
            if (!StringUtils.isEmpty(executorResponse) && executorResponse
                    .contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while getting UI configurations by invoking " + uiConfigUrl);
                HandlerUtil.handleError(resp, uiConfigResponse);
                return;
            }

            String uiConfig = uiConfigResponse.getData();
            if (uiConfig == null){
                log.error("UI config retrieval is failed, and didn't find UI configuration for App manager.");
                HandlerUtil.handleError(resp, null);
                return;
            }
            JsonElement uiConfigJsonElement = jsonParser.parse(uiConfigResponse.getData());
            JsonObject uiConfigJsonObject = null;
            if (uiConfigJsonElement.isJsonObject()) {
                uiConfigJsonObject = uiConfigJsonElement.getAsJsonObject();
                httpSession.setAttribute(HandlerConstants.UI_CONFIG_KEY, uiConfigJsonObject);
                httpSession.setAttribute(HandlerConstants.PLATFORM, gatewayUrl);
            }
            if (uiConfigJsonObject == null) {
                log.error(
                        "Either UI config json element is not an json object or converting rom json element to json object is failed.");
                HandlerUtil.handleError(resp, null);
                return;
            }

            boolean isSsoEnable = uiConfigJsonObject.get("isSsoEnable").getAsBoolean();
            JsonArray tags = uiConfigJsonObject.get("appRegistration").getAsJsonObject().get("tags").getAsJsonArray();
            JsonArray scopes = uiConfigJsonObject.get("scopes").getAsJsonArray();

            if (isSsoEnable) {
                log.debug("SSO is enabled");
            } else {
                // default login
                HttpPost apiRegEndpoint = new HttpPost(gatewayUrl + HandlerConstants.APP_REG_ENDPOINT);
                apiRegEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + Base64.getEncoder()
                        .encodeToString((username + HandlerConstants.COLON + password).getBytes()));
                apiRegEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                apiRegEndpoint.setEntity(constructAppRegPayload(tags));

                ProxyResponse clientAppResponse = HandlerUtil.execute(apiRegEndpoint);

                if (clientAppResponse.getCode() == HttpStatus.SC_UNAUTHORIZED){
                    HandlerUtil.handleError(resp, clientAppResponse);
                    return;
                }
                if (clientAppResponse.getCode() == HttpStatus.SC_CREATED && getTokenAndPersistInSession(req, resp,
                        clientAppResponse.getData(), scopes)) {
                    ProxyResponse proxyResponse = new ProxyResponse();
                    proxyResponse.setCode(HttpStatus.SC_OK);
                    HandlerUtil.handleSuccess(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleError(resp, null);
            }
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        } catch (LoginException e) {
            log.error("Error occurred while getting token data. ", e);
        }
    }

    /***
     *
     * @param req - {@link HttpServletRequest}
     * @param clientAppResult - clientAppResult
     * @param scopes - scopes defied in the application-mgt.xml
     * @throws LoginException - login exception throws when getting token result
     */
    private boolean getTokenAndPersistInSession(HttpServletRequest req, HttpServletResponse resp,
            String clientAppResult, JsonArray scopes) throws LoginException {
        JsonParser jsonParser = new JsonParser();
        try {
            JsonElement jClientAppResult = jsonParser.parse(clientAppResult);
            if (jClientAppResult.isJsonObject()) {
                JsonObject jClientAppResultAsJsonObject = jClientAppResult.getAsJsonObject();
                String clientId = jClientAppResultAsJsonObject.get("client_id").getAsString();
                String clientSecret = jClientAppResultAsJsonObject.get("client_secret").getAsString();
                String encodedClientApp = Base64.getEncoder()
                        .encodeToString((clientId + HandlerConstants.COLON + clientSecret).getBytes());

                ProxyResponse tokenResultResponse = getTokenResult(encodedClientApp, scopes);

                if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API to get token data.");
                    HandlerUtil.handleError(resp, tokenResultResponse);
                    return false;
                }
                String tokenResult = tokenResultResponse.getData();
                if (tokenResult == null){
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
            }
            return false;
        } catch (IOException e) {
            throw new LoginException("Error occurred while sending the response into the socket", e);
        }
    }

    /***
     *
     * @param scopes - scope Json Array and it is retrieved by reading UI config.
     * @return string value of the defined scopes
     */
    private String getScopeString(JsonArray scopes) {
        if (scopes != null && scopes.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (JsonElement scope : scopes) {
                String tmpScope = scope.getAsString() + " ";
                builder.append(tmpScope);
            }
            return builder.toString();
        } else {
            return null;
        }
    }

    /***
     *
     * @param req - {@link HttpServletRequest}
     * Define username and password static parameters.
     */
    private static void validateLoginRequest(HttpServletRequest req) throws LoginException {
        String gatewayCarbonPort = System.getProperty("iot.core.https.port");
        if (HandlerConstants.HTTP_PROTOCOL.equals(req.getScheme())) {
            gatewayCarbonPort = System.getProperty("iot.core.http.port");
        }
        username = req.getParameter("username");
        password = req.getParameter("password");
        gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.gateway.host")
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
        uiConfigUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.core.host")
                + HandlerConstants.COLON + gatewayCarbonPort + HandlerConstants.UI_CONFIG_ENDPOINT;
        if (username == null || password == null) {
            String msg = "Invalid login request. Username or Password is not received for login request.";
            log.error(msg);
            throw new LoginException(msg);
        }
    }

    /***
     *
     * @param tags - tags which are retrieved by reading app manager configuration
     * @return {@link StringEntity} of the payload to create the client application
     */
    private StringEntity constructAppRegPayload(JsonArray tags) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(HandlerConstants.APP_NAME_KEY, HandlerConstants.PUBLISHER_APPLICATION_NAME);
        jsonObject.addProperty(HandlerConstants.USERNAME, username);
        jsonObject.addProperty(HandlerConstants.PASSWORD, password);
        jsonObject.addProperty("isAllowedToAllDomains", "false");
        jsonObject.add(HandlerConstants.TAGS_KEY, tags);
        String payload = jsonObject.toString();
        return new StringEntity(payload, ContentType.APPLICATION_JSON);
    }

    /***
     *
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @param scopes - Scopes which are retrieved by reading application-mgt configuration
     * @return Invoke token endpoint and return the response as string.
     * @throws IOException IO exception throws if an error occurred when invoking token endpoint
     */
    private ProxyResponse getTokenResult(String encodedClientApp, JsonArray scopes) throws IOException {
        HttpPost tokenEndpoint = new HttpPost(gatewayUrl + HandlerConstants.TOKEN_ENDPOINT);
        tokenEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + encodedClientApp);
        tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
        String scopeString = getScopeString(scopes);

        if (scopeString != null) {
            scopeString = scopeString.trim();
        } else {
            scopeString = "default";
        }

        StringEntity tokenEPPayload = new StringEntity(
                "grant_type=password&username=" + username + "&password=" + password + "&scope=" + scopeString,
                ContentType.APPLICATION_FORM_URLENCODED);
        tokenEndpoint.setEntity(tokenEPPayload);
        return HandlerUtil.execute(tokenEndpoint);
    }
}
