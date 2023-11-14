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

import com.google.gson.*;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.cache.LoginCache;
import io.entgra.device.mgt.core.ui.request.interceptor.cache.OAuthApp;
import io.entgra.device.mgt.core.ui.request.interceptor.cache.OAuthAppCacheKey;
import io.entgra.device.mgt.core.ui.request.interceptor.exceptions.LoginException;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@MultipartConfig
@WebServlet("/ssoLogin")
public class SsoLoginHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(SsoLoginHandler.class);

    private static String adminUsername;
    private static String adminPassword;
    private static String gatewayUrl;
    private static String iotsCoreUrl;
    private static String apiMgtUrl;
    private static String keyManagerUrl;
    private static int sessionTimeOut;
    private static String encodedAdminCredentials;
    private static String encodedClientApp;
    private static String applicationId;
    private static String applicationName;
    private static String baseContextPath;

    private JsonObject uiConfigJsonObject;
    private HttpSession httpSession;
    private LoginCache loginCache;
    private OAuthApp oAuthApp;
    private OAuthAppCacheKey oAuthAppCacheKey;
    private String state;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }

            httpSession = req.getSession(true);

            state = HandlerUtil.generateStateToken();
            initializeAdminCredentials();
            baseContextPath = req.getContextPath();
            applicationName = baseContextPath.substring(1, baseContextPath.indexOf("-ui-request-handler"));

            gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
            iotsCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getCorePort(req.getScheme());
            apiMgtUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_APIM_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getAPIManagerPort(req.getScheme());
            keyManagerUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(req.getScheme());

            // Fetch ui config and persists in session
            String uiConfigUrl = iotsCoreUrl + HandlerConstants.UI_CONFIG_ENDPOINT;
            uiConfigJsonObject = HandlerUtil.getUIConfigAndPersistInSession(uiConfigUrl, gatewayUrl, httpSession, resp);

            // Retrieving login cache and do a DCR if the cache is not available.
            loginCache = HandlerUtil.getLoginCache(httpSession);
            oAuthAppCacheKey = new OAuthAppCacheKey(applicationName, adminUsername);
            oAuthApp = loginCache.getOAuthAppCache(oAuthAppCacheKey);
            if (oAuthApp == null) {
                dynamicClientRegistration(req, resp);
            }

            String clientId = oAuthApp.getClientId();
            JsonArray scopesSsoJson = uiConfigJsonObject.get("scopes").getAsJsonArray();
            String scopesSsoString = HandlerUtil.getScopeString(scopesSsoJson);
            String loginCallbackUrl = iotsCoreUrl + baseContextPath + HandlerConstants.SSO_LOGIN_CALLBACK;
            persistAuthSessionData(req, oAuthApp.getClientId(), oAuthApp.getClientSecret(),
                    oAuthApp.getEncodedClientApp(), scopesSsoString, state);
            resp.sendRedirect(keyManagerUrl + HandlerConstants.AUTHORIZATION_ENDPOINT +
                    "?response_type=code" +
                    "&state=" + state +
                    "&client_id=" + clientId +
                    "&scope=openid " + scopesSsoString +
                    "&redirect_uri=" + loginCallbackUrl);
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        } catch (ParserConfigurationException e) {
            log.error("Error while creating the document builder.");
        } catch (SAXException e) {
            log.error("Error while parsing xml file.", e);
        } catch (LoginException e) {
            log.error("SSO Login is failed. Application: " + applicationName, e);
        }
    }

    /***
     * Handles DCR and updates grant types of the application
     * before redirecting to the authorization endpoint.
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     */
    private void dynamicClientRegistration(HttpServletRequest req, HttpServletResponse resp) throws LoginException {
        try {
            JsonArray tags = uiConfigJsonObject.get("appRegistration").getAsJsonObject().get("tags").getAsJsonArray();
            JsonArray scopes = uiConfigJsonObject.get("scopes").getAsJsonArray();
            sessionTimeOut = Integer.parseInt(String.valueOf(uiConfigJsonObject.get("sessionTimeOut")));
            JsonArray supportedGrantTypes = constructAppGrantTypeUpdateArray();
            String callbackUrl = iotsCoreUrl + baseContextPath + HandlerConstants.SSO_LOGIN_CALLBACK;

            // Register the client application
            HttpPost apiRegEndpoint = new HttpPost(gatewayUrl + HandlerConstants.APP_REG_ENDPOINT);
            encodedAdminCredentials = Base64.getEncoder()
                    .encodeToString((adminUsername + HandlerConstants.COLON + adminPassword).getBytes());
            apiRegEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC +
                    encodedAdminCredentials);
            apiRegEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            apiRegEndpoint.setEntity(HandlerUtil.constructAppRegPayload(tags, applicationName, adminUsername, adminPassword,
                    callbackUrl, supportedGrantTypes));

            ProxyResponse clientAppResponse = HandlerUtil.execute(apiRegEndpoint);

            if (clientAppResponse.getCode() == HttpStatus.SC_CREATED) {
                JsonParser jsonParser = new JsonParser();
                JsonElement jClientAppResult = jsonParser.parse(clientAppResponse.getData());
                String clientId = null;
                String clientSecret = null;

                if (jClientAppResult.isJsonObject()) {
                    JsonObject jClientAppResultAsJsonObject = jClientAppResult.getAsJsonObject();
                    clientId = jClientAppResultAsJsonObject.get("client_id").getAsString();
                    clientSecret = jClientAppResultAsJsonObject.get("client_secret").getAsString();
                    encodedClientApp = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
                    String scopesString = HandlerUtil.getScopeString(scopes);
                    persistAuthSessionData(req, clientId, clientSecret, encodedClientApp, scopesString, state);
                }

                // cache the oauth app credentials
                oAuthApp = new OAuthApp(applicationName, adminUsername, clientId, clientSecret, encodedClientApp);
                loginCache.addOAuthAppToCache(oAuthAppCacheKey, oAuthApp);
            } else if (clientAppResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                String msg = "Unauthorized attempt to register the client application. " +
                        "Application Name: " + applicationName + ". Response message: " + clientAppResponse.getData();
                log.error(msg);
                HandlerUtil.handleError(resp, clientAppResponse);
                throw new LoginException(msg);
            } else {
                String msg = "Failed the process while registering the client application. " +
                        "Application Name: " + applicationName + ". Response Code: "
                        + clientAppResponse.getCode() + ", Response message: " + clientAppResponse.getData();
                log.error(msg);
                HandlerUtil.handleError(resp, null);
                throw new LoginException(msg);
            }
        } catch (IOException e) {
            throw new LoginException("Error occurred while sending the response into the socket.", e);
        } catch (JsonSyntaxException e) {
            throw new LoginException("Error occurred while parsing the response.", e);
        }
    }

    /**
     * Initialize the admin credential variables
     *
     * @throws ParserConfigurationException - Throws when error occur during initializing the document builder
     * @throws IOException                  - Throws when error occur during document parsing
     * @throws SAXException                 - Throws when error occur during document parsing
     */
    private void initializeAdminCredentials() throws ParserConfigurationException, IOException, SAXException {
        File userMgtConf = new File("repository/conf/user-mgt.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(userMgtConf);

        adminUsername = doc.getElementsByTagName("UserName").item(0).getTextContent();
        adminPassword = doc.getElementsByTagName("Password").item(0).getTextContent();
    }


    /**
     * Persist the Auth data inside the session
     *
     * @param req              - Http Servlet request
     * @param clientId         - Client ID of the SP
     * @param clientSecret     - Client secret of the SP
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @param scopes           - User scopes
     */
    private void persistAuthSessionData(HttpServletRequest req, String clientId, String clientSecret,
                                        String encodedClientApp, String scopes, String state) {
        httpSession = req.getSession(false);
        httpSession.setAttribute("clientId", clientId);
        httpSession.setAttribute("clientSecret", clientSecret);
        httpSession.setAttribute("encodedClientApp", encodedClientApp);
        httpSession.setAttribute("scope", scopes);
        httpSession.setAttribute("redirectUrl", req.getParameter("redirect"));
        httpSession.setAttribute("state", state);
        httpSession.setMaxInactiveInterval(sessionTimeOut);
    }

    /***
     * Generates payload for application grant_type update payload
     *
     * @return {@link JsonArray} of the payload to update application grant type
     */
    private JsonArray constructAppGrantTypeUpdateArray() {
        JsonArray jsonArray = new JsonArray();

        jsonArray.add(HandlerConstants.CODE_GRANT_TYPE);
        jsonArray.add(HandlerConstants.REFRESH_TOKEN_GRANT_TYPE);
        jsonArray.add(HandlerConstants.PASSWORD_GRANT_TYPE);
        jsonArray.add(HandlerConstants.JWT_BEARER_GRANT_TYPE);

        return jsonArray;
    }

    /***
     * Generates tokens using password grant_type by invoking token endpoint
     *
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @return Invoke token endpoint and return the response as string.
     * @throws IOException IO exception throws if an error occurred when invoking token endpoint
     */
    private ProxyResponse getTokenResult(String encodedClientApp) throws IOException {
        HttpPost tokenEndpoint = new HttpPost(keyManagerUrl + HandlerConstants.OAUTH2_TOKEN_ENDPOINT);
        tokenEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + encodedClientApp);
        tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());

        StringEntity tokenEPPayload = new StringEntity(
                "grant_type=" + HandlerConstants.PASSWORD_GRANT_TYPE + "&username=" + adminUsername + "&password=" + adminPassword +
                        "&scope=apim:api_view apim:api_create apim:api_publish apim:subscribe",
                ContentType.APPLICATION_FORM_URLENCODED);
        tokenEndpoint.setEntity(tokenEPPayload);
        return HandlerUtil.execute(tokenEndpoint);
    }

    /***
     * Retrieves and returns access token
     *
     * @param resp - Http Servlet Response
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @return Returns access token
     * @throws IOException IO exception throws if an error occurred when invoking token endpoint
     */
    private String getAccessToken(HttpServletResponse resp, String encodedClientApp) throws IOException {
        ProxyResponse tokenResultResponse = getTokenResult(encodedClientApp);

        if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
            log.error("Error occurred while invoking the API to get token data.");
            HandlerUtil.handleError(resp, tokenResultResponse);
        }
        String tokenResult = tokenResultResponse.getData();
        if (tokenResult == null) {
            log.error("Invalid token response is received.");
            HandlerUtil.handleError(resp, tokenResultResponse);
        }

        JsonParser jsonParser = new JsonParser();
        JsonElement jTokenResult = jsonParser.parse(tokenResult);

        JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
        return jTokenResultAsJsonObject.get("access_token").getAsString();
    }
}
