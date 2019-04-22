/* Copyright (c) 2018, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;
import org.wso2.carbon.device.application.mgt.handler.beans.AuthData;
import org.wso2.carbon.device.application.mgt.handler.exceptions.LoginException;
import org.wso2.carbon.device.application.mgt.handler.util.HandlerConstants;
import org.wso2.carbon.device.application.mgt.handler.util.HandlerUtil;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;

import static org.wso2.carbon.device.application.mgt.handler.util.HandlerUtil.execute;

@MultipartConfig
@WebServlet("/login")
public class LoginHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginHandler.class);
    private static final long serialVersionUID = 9050048549140517002L;

    private static String username;
    private static String password;
    private static String platform;
    private static String serverUrl;
    private static String uiConfigUrl;
    private static JsonObject uiConfig;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            validateLoginRequest(req, resp);
            DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                    .getDeviceManagementConfig();
            String adminUsername = deviceManagementConfig.getIdentityConfigurations().getAdminUsername();
            String adminPwd = deviceManagementConfig.getIdentityConfigurations().getAdminPassword();

            HttpSession httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }
            httpSession = req.getSession(true);
            //setting session to expiry in 5 mins
            httpSession.setMaxInactiveInterval(Math.toIntExact(HandlerConstants.TIMEOUT));

            HttpGet uiConfigEndpoint = new HttpGet(uiConfigUrl);
            JsonParser jsonParser = new JsonParser();
            String uiConfigJsonString = execute(uiConfigEndpoint,HttpStatus.SC_OK);
            if (uiConfigJsonString.contains(HandlerConstants.EXECUTOR_XCEPTIO_PRFIX)){
                log.error("Error occurred while getting UI configurations by invoking " + uiConfigUrl);
                handleErrorResponse(resp, uiConfigJsonString);
            }

            JsonElement uiConfigJsonElement = jsonParser.parse(uiConfigJsonString);
            JsonObject uiConfigAsJsonObject = null ;
            if (uiConfigJsonElement.isJsonObject()) {
                uiConfigAsJsonObject = uiConfigJsonElement.getAsJsonObject();
            }
            if (uiConfigAsJsonObject == null) {
                resp.sendRedirect(serverUrl + "/" + platform + HandlerConstants.DEFAULT_ERROR_CALLBACK);
                return;
            }

            boolean isSsoEnable = uiConfigAsJsonObject.get("isSsoEnable").getAsBoolean();
            JsonArray tags = uiConfigAsJsonObject.get("appRegistration").getAsJsonObject().get("tags").getAsJsonArray();
            JsonArray scopes = uiConfigAsJsonObject.get("scopes").getAsJsonArray();

            if (isSsoEnable) {
                log.debug("SSO is enabled");
            } else {
                // default login
                HttpPost apiRegEndpoint = new HttpPost(serverUrl + HandlerConstants.APP_REG_ENDPOINT);
                apiRegEndpoint.setHeader(HandlerConstants.AUTHORIZATION, HandlerConstants.BASIC + Base64.getEncoder()
                        .encodeToString((adminUsername + HandlerConstants.COLON + adminPwd).getBytes()));
                apiRegEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
                apiRegEndpoint.setEntity(constructAppRegPayload(tags));

                String clientAppResult = execute(apiRegEndpoint, HttpStatus.SC_CREATED);

                if (!clientAppResult.isEmpty() && persistTokenInSession(req, resp, clientAppResult, scopes)) {
                    resp.sendRedirect(
                            serverUrl + "/" + platform + uiConfigAsJsonObject.get(HandlerConstants.LOGIN_RESPONSE_KEY)
                                    .getAsJsonObject().get("successCallback").getAsString());
                    return;
                }
                resp.sendRedirect(
                        serverUrl + "/" + platform + uiConfigAsJsonObject.get(HandlerConstants.LOGIN_RESPONSE_KEY)
                                .getAsJsonObject().get(HandlerConstants.FAILURE_CALLBACK_KEY).getAsJsonObject()
                                .get(HandlerUtil.getStatusKey(HandlerConstants.INTERNAL_ERROR_CODE)).getAsString());
            }
        } catch (IOException e) {
            log.error("Error occured while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occured while parsing the response. ", e);
        } catch (LoginException e) {
            log.error("Error occured while getting token data. ", e);
        }
    }

    /***
     *
     * @param req - {@link HttpServletRequest}
     * @param clientAppResult - clientAppResult
     * @param scopes - scopes defied in the application-mgt.xml
     * @throws LoginException - login exception throws when getting token result
     */
    private boolean persistTokenInSession(HttpServletRequest req, HttpServletResponse resp, String clientAppResult,
            JsonArray scopes) throws LoginException {
        JsonParser jsonParser = new JsonParser();
        String tokenResult;

        try {
            JsonElement jClientAppResult = jsonParser.parse(clientAppResult);
            if (jClientAppResult.isJsonObject()) {
                JsonObject jClientAppResultAsJsonObject = jClientAppResult.getAsJsonObject();
                String clientId = jClientAppResultAsJsonObject.get("client_id").getAsString();
                String clientSecret = jClientAppResultAsJsonObject.get("client_secret").getAsString();
                String encodedClientApp = Base64.getEncoder()
                        .encodeToString((clientId + ":" + clientSecret).getBytes());

                tokenResult = getTokenResult(encodedClientApp, scopes, resp);

                if (tokenResult != null) {
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
            }
            return false;
        } catch (IOException e) {
            throw new LoginException("Error occured while sending the response into the socket", e);
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
                String tmpscope = scope.getAsString() + " ";
                builder.append(tmpscope);
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
    private static void validateLoginRequest(HttpServletRequest req, HttpServletResponse resp) throws LoginException {
        username = req.getParameter("username");
        password = req.getParameter("password");
        platform = req.getParameter("platform");
        serverUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        uiConfigUrl = serverUrl + HandlerConstants.UI_CONFIG_ENDPOINT;

        try {
            if (platform == null) {
                resp.sendRedirect(serverUrl + HandlerConstants.DEFAULT_ERROR_CALLBACK);
                throw new LoginException("Invalid login request. Platform parameter is Null.");
            }
            if (username == null || password == null) {
                resp.sendRedirect(serverUrl + "/" + platform + HandlerConstants.DEFAULT_ERROR_CALLBACK);
                throw new LoginException(
                        " Invalid login request. Username or Password is not received for login request.");
            }
        } catch (IOException e) {
            throw new LoginException("Error Occured while redirecting to default error page.", e);
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
        jsonObject.addProperty("isAllowedToAllDomains", "false");
        jsonObject.add(HandlerConstants.TAGS_KEY, tags);
        String payload = jsonObject.toString();
        return new StringEntity(payload, ContentType.APPLICATION_JSON);
    }

    /***
     *
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @param scopes - Scopes which are retrieved by reading application-mgt configuration
     * @param resp - {@link HttpServletResponse}
     * @return Invoke token endpoint and return the response as string.
     * @throws IOException IO exception throws if an error occured when invoking token endpoint
     */
    private String getTokenResult(String encodedClientApp, JsonArray scopes, HttpServletResponse resp)
            throws IOException, LoginException {

            HttpPost tokenEndpoint = new HttpPost(serverUrl + HandlerConstants.TOKEN_ENDPOINT);
            tokenEndpoint.setHeader("Authorization", "Basic " + encodedClientApp);
            tokenEndpoint.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
            String scopeString = getScopeString(scopes);

            if (scopeString != null) {
                scopeString = scopeString.trim();
            }

            StringEntity tokenEPPayload = new StringEntity(
                    "grant_type=password&username=" + username + "&password=" + password + "&scope=" + scopeString,
                    ContentType.APPLICATION_FORM_URLENCODED);
            tokenEndpoint.setEntity(tokenEPPayload);

            String tokenResult = execute(tokenEndpoint, HttpStatus.SC_OK);

            if (tokenResult.contains(HandlerConstants.EXECUTOR_XCEPTIO_PRFIX)) {
                log.error("Error occurred while getting token data by invoking " + serverUrl
                        + HandlerConstants.TOKEN_ENDPOINT);
                handleErrorResponse(resp, tokenResult);
            }
            return tokenResult;
    }

    /***
     *
     * @param resp {@link HttpServletResponse}
     * corresponding error page.
     * @throws LoginException If an {@link IOException} occurs when redirecting to corresponding error page.
     */
    private void handleErrorResponse(HttpServletResponse resp, String respMessage) throws LoginException {
        try {
            resp.sendRedirect(serverUrl + uiConfig.get(HandlerConstants.LOGIN_RESPONSE_KEY).getAsJsonObject()
                    .get(HandlerConstants.FAILURE_CALLBACK_KEY).getAsJsonObject()
                    .get(respMessage.split(HandlerConstants.EXECUTOR_XCEPTIO_PRFIX)[0]).getAsString());
        } catch (IOException e) {
            throw new LoginException("Error occured while redirecting to corresponding error page. ", e);
        }
    }
}
