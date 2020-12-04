/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
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
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;
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
import java.util.Base64;

@MultipartConfig
@WebServlet("/ssoLogin")
public class SsoLoginHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(SsoLoginHandler.class);

    private static String adminUsername;
    private static String adminPassword;
    private static String gatewayUrl;
    private static String iotsCoreUrl;
    private static String encodedClientApp;
    private static String applicationId;
    private static String baseContextPath;

    private JsonObject uiConfigJsonObject;
    private HttpSession httpSession;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dynamicClientRegistration(req, resp);
        String clientId = httpSession.getAttribute("clientId").toString();
        JsonArray scopesSsoJson = uiConfigJsonObject.get("scopes").getAsJsonArray();
        String scopesSsoString = HandlerUtil.getScopeString(scopesSsoJson);
        String loginCallbackUrl = iotsCoreUrl + baseContextPath + HandlerConstants.SSO_LOGIN_CALLBACK;
        resp.sendRedirect(iotsCoreUrl + HandlerConstants.AUTHORIZATION_ENDPOINT +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&state=" +
                "&scope=openid " + scopesSsoString +
                "&redirect_uri=" + loginCallbackUrl);
    }

    /***
     * Handles DCR and updates grant types of the application
     * before redirecting to the authorization endpoint.
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     */
    private void dynamicClientRegistration(HttpServletRequest req, HttpServletResponse resp) {
        try {
            File userMgtConf = new File("conf/user-mgt.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(userMgtConf);

            adminUsername = doc.getElementsByTagName("UserName").item(0).getTextContent();
            adminPassword = doc.getElementsByTagName("Password").item(0).getTextContent();

            baseContextPath = req.getContextPath();
            String applicationName = baseContextPath.substring(1, baseContextPath.indexOf("-ui-request-handler"));

            String iotsCorePort = System.getProperty("iot.core.https.port");

            if (HandlerConstants.HTTP_PROTOCOL.equals(req.getScheme())) {
                iotsCorePort = System.getProperty("iot.core.http.port");
            }

            gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.gateway.host")
                    + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
            iotsCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.core.host")
                    + HandlerConstants.COLON + iotsCorePort;
            String uiConfigUrl = iotsCoreUrl + HandlerConstants.UI_CONFIG_ENDPOINT;

            httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }

            httpSession = req.getSession(true);
            uiConfigJsonObject = HandlerUtil.getUIConfigAndPersistInSession(uiConfigUrl, gatewayUrl, httpSession, resp);

            JsonArray tags = uiConfigJsonObject.get("appRegistration").getAsJsonObject().get("tags").getAsJsonArray();
            JsonArray scopes = uiConfigJsonObject.get("scopes").getAsJsonArray();

            // Register the client application
            HttpPost apiRegEndpoint = new HttpPost(gatewayUrl + HandlerConstants.APP_REG_ENDPOINT);
            String encodedAdminCredentials = Base64.getEncoder()
                    .encodeToString((adminUsername + HandlerConstants.COLON + adminPassword).getBytes());
            apiRegEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC +
                    encodedAdminCredentials);
            apiRegEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            apiRegEndpoint.setEntity(HandlerUtil.constructAppRegPayload(tags, applicationName, adminUsername, adminPassword));

            ProxyResponse clientAppResponse = HandlerUtil.execute(apiRegEndpoint);

            if (clientAppResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                HandlerUtil.handleError(resp, clientAppResponse);
            }

            if (clientAppResponse.getCode() == HttpStatus.SC_CREATED) {
                JsonParser jsonParser = new JsonParser();
                JsonElement jClientAppResult = jsonParser.parse(clientAppResponse.getData());
                if (jClientAppResult.isJsonObject()) {
                    JsonObject jClientAppResultAsJsonObject = jClientAppResult.getAsJsonObject();
                    String clientId = jClientAppResultAsJsonObject.get("client_id").getAsString();
                    String clientSecret = jClientAppResultAsJsonObject.get("client_secret").getAsString();
                    encodedClientApp = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
                    String redirectUrl = req.getParameter("redirect");
                    httpSession = req.getSession(false);
                    httpSession.setAttribute("clientId", clientId);
                    httpSession.setAttribute("clientSecret", clientSecret);
                    httpSession.setAttribute("encodedClientApp", encodedClientApp);
                    httpSession.setAttribute("scope", HandlerUtil.getScopeString(scopes));
                    httpSession.setAttribute("redirectUrl", redirectUrl);
                }
            }

            // Get the details of the registered application
            String getApplicationEndpointUrl = iotsCoreUrl + HandlerConstants.APIM_APPLICATIONS_ENDPOINT +
                    "?query=" + applicationName;
            HttpGet getApplicationEndpoint = new HttpGet(getApplicationEndpointUrl);
            getApplicationEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER +
                    getAccessToken(resp, encodedClientApp));

            ProxyResponse getApplicationResponse = HandlerUtil.execute(getApplicationEndpoint);

            if (getApplicationResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                HandlerUtil.handleError(resp, getApplicationResponse);
                return;
            }

            if (getApplicationResponse.getCode() == HttpStatus.SC_OK) {
                JsonParser jsonParser = new JsonParser();
                JsonElement jAppResult = jsonParser.parse(getApplicationResponse.getData());
                if (jAppResult.isJsonObject()) {
                    JsonObject jClientAppResultAsJsonObject = jAppResult.getAsJsonObject();
                    JsonArray appList = jClientAppResultAsJsonObject.getAsJsonArray("list");
                    JsonObject app;
                    for (JsonElement appJson : appList) {
                        app = appJson.getAsJsonObject();
                        if (app.get("name").getAsString().equals(applicationName)) {
                            applicationId = app.get("applicationId").getAsString();
                            break;
                        }
                    }
                }
            }

            // Update the grant types of the application
            String url = iotsCoreUrl + HandlerConstants.APIM_APPLICATIONS_ENDPOINT + applicationId + "/keys/PRODUCTION";
            HttpPut updateApplicationGrantTypesEndpoint = new HttpPut(url);
            updateApplicationGrantTypesEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER +
                    getAccessToken(resp, encodedClientApp));
            updateApplicationGrantTypesEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            updateApplicationGrantTypesEndpoint.setEntity(constructAppGrantTypeUpdatePayload());

            ProxyResponse updateApplicationGrantTypesEndpointResponse = HandlerUtil.execute(updateApplicationGrantTypesEndpoint);

            if (updateApplicationGrantTypesEndpointResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                HandlerUtil.handleError(resp, updateApplicationGrantTypesEndpointResponse);
                return;
            }

            if (updateApplicationGrantTypesEndpointResponse.getCode() == HttpStatus.SC_OK) {
                return;
            }

            HandlerUtil.handleError(resp, null);
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        } catch (ParserConfigurationException | SAXException e) {
            log.error("Error while parsing xml file.", e);
        }
    }

    /***
     * Generates payload for application grant_type update payload
     *
     * @return {@link StringEntity} of the payload to update application grant type
     */
    private StringEntity constructAppGrantTypeUpdatePayload() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("supportedGrantTypes", new JSONArray(new Object[]{HandlerConstants.CODE_GRANT_TYPE,
                HandlerConstants.REFRESH_TOKEN_GRANT_TYPE, HandlerConstants.PASSWORD_GRANT_TYPE}));
        jsonObject.put(HandlerConstants.CALLBACK_URL_KEY, iotsCoreUrl + baseContextPath + HandlerConstants.SSO_LOGIN_CALLBACK);
        String payload = jsonObject.toString();
        return new StringEntity(payload, ContentType.APPLICATION_JSON);
    }

    /***
     * Generates tokens using password grant_type by invoking token endpoint
     *
     * @param encodedClientApp - Base64 encoded clientId:clientSecret.
     * @return Invoke token endpoint and return the response as string.
     * @throws IOException IO exception throws if an error occurred when invoking token endpoint
     */
    private ProxyResponse getTokenResult(String encodedClientApp) throws IOException {
        HttpPost tokenEndpoint = new HttpPost(gatewayUrl + HandlerConstants.TOKEN_ENDPOINT);
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
