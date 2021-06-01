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
import io.entgra.ui.request.interceptor.cache.LoginCacheManager;
import io.entgra.ui.request.interceptor.cache.OAuthApp;
import io.entgra.ui.request.interceptor.cache.OAuthAppCacheKey;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
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
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
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
    private static String encodedAdminCredentials;
    private static String encodedClientApp;
    private static String applicationId;
    private static String applicationName;
    private static String baseContextPath;

    private JsonObject uiConfigJsonObject;
    private HttpSession httpSession;

    private LoginCacheManager loginCacheManager;
    private OAuthApp oAuthApp;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            httpSession = req.getSession(false);
            if (httpSession != null) {
                httpSession.invalidate();
            }

            httpSession = req.getSession(true);
            httpSession.setMaxInactiveInterval(Math.toIntExact(HandlerConstants.TIMEOUT));
            initializeAdminCredentials();
            baseContextPath = req.getContextPath();
            applicationName = baseContextPath.substring(1, baseContextPath.indexOf("-ui-request-handler"));

            // Check if oauth app cache is available
            loginCacheManager = new LoginCacheManager();
            loginCacheManager.initializeCacheManager();
            oAuthApp = loginCacheManager.getOAuthAppCache(
                    new OAuthAppCacheKey(applicationName, adminUsername)
            );

            if (oAuthApp == null) {
                dynamicClientRegistration(req, resp);
            }

            String clientId = oAuthApp.getClientId();
            JsonArray scopesSsoJson = uiConfigJsonObject.get("scopes").getAsJsonArray();
            String scopesSsoString = HandlerUtil.getScopeString(scopesSsoJson);
            String loginCallbackUrl = iotsCoreUrl + baseContextPath + HandlerConstants.SSO_LOGIN_CALLBACK;
            persistAuthSessionData(req, oAuthApp.getClientId(), oAuthApp.getClientSecret(),
                    oAuthApp.getEncodedClientApp(), scopesSsoString);

            resp.sendRedirect(iotsCoreUrl + HandlerConstants.AUTHORIZATION_ENDPOINT +
                    "?response_type=code" +
                    "&client_id=" + clientId +
                    "&state=" +
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
        }
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
            String iotsCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTPS_PORT_ENV_VAR);

            if (HandlerConstants.HTTP_PROTOCOL.equals(req.getScheme())) {
                iotsCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTP_PORT_ENV_VAR);
            }

            gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
            iotsCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                    + HandlerConstants.COLON + iotsCorePort;
            String uiConfigUrl = iotsCoreUrl + HandlerConstants.UI_CONFIG_ENDPOINT;

            uiConfigJsonObject = HandlerUtil.getUIConfigAndPersistInSession(uiConfigUrl, gatewayUrl, httpSession, resp);
            JsonArray tags = uiConfigJsonObject.get("appRegistration").getAsJsonObject().get("tags").getAsJsonArray();
            JsonArray scopes = uiConfigJsonObject.get("scopes").getAsJsonArray();

            // Register the client application
            HttpPost apiRegEndpoint = new HttpPost(gatewayUrl + HandlerConstants.APP_REG_ENDPOINT);
            encodedAdminCredentials = Base64.getEncoder()
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
                String clientId = null;
                String clientSecret = null;

                if (jClientAppResult.isJsonObject()) {
                    JsonObject jClientAppResultAsJsonObject = jClientAppResult.getAsJsonObject();
                    clientId = jClientAppResultAsJsonObject.get("client_id").getAsString();
                    clientSecret = jClientAppResultAsJsonObject.get("client_secret").getAsString();
                    encodedClientApp = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
                    String scopesString = HandlerUtil.getScopeString(scopes);
                    persistAuthSessionData(req, clientId, clientSecret, encodedClientApp, scopesString);
                }

                // cache the oauth app credentials
                OAuthAppCacheKey oAuthAppCacheKey = new OAuthAppCacheKey(applicationName, adminUsername);
                oAuthApp = new OAuthApp(applicationName, adminUsername, clientId, clientSecret, encodedClientApp);
                loginCacheManager.addOAuthAppToCache(oAuthAppCacheKey, oAuthApp);
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
            String url = iotsCoreUrl + HandlerConstants.APIM_APPLICATIONS_ENDPOINT + applicationId + "/keys/" +
                    HandlerConstants.PRODUCTION_KEY;
            HttpPut updateApplicationGrantTypesEndpoint = new HttpPut(url);
            updateApplicationGrantTypesEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER +
                    getAccessToken(resp, encodedClientApp));
            updateApplicationGrantTypesEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            updateApplicationGrantTypesEndpoint.setEntity(constructAppGrantTypeUpdatePayload());

            ProxyResponse updateApplicationGrantTypesEndpointResponse = HandlerUtil.execute(updateApplicationGrantTypesEndpoint);

            // Update app as a SaaS app
            this.updateSaasApp(applicationName);

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
        } catch (ParserConfigurationException e) {
            log.error("Error while creating the document builder.");
        } catch (SAXException e) {
            log.error("Error while parsing xml file.", e);
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
        File userMgtConf = new File("conf/user-mgt.xml");
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
                                        String encodedClientApp, String scopes) {
        httpSession = req.getSession(false);
        httpSession.setAttribute("clientId", clientId);
        httpSession.setAttribute("clientSecret", clientSecret);
        httpSession.setAttribute("encodedClientApp", encodedClientApp);
        httpSession.setAttribute("scope", scopes);
        httpSession.setAttribute("redirectUrl", req.getParameter("redirect"));
    }

    /***
     * Generates payload for application grant_type update payload
     *
     * @return {@link StringEntity} of the payload to update application grant type
     */
    private StringEntity constructAppGrantTypeUpdatePayload() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("supportedGrantTypes",
                new JSONArray(new Object[]{
                        HandlerConstants.CODE_GRANT_TYPE,
                        HandlerConstants.REFRESH_TOKEN_GRANT_TYPE,
                        HandlerConstants.PASSWORD_GRANT_TYPE,
                        HandlerConstants.JWT_BEARER_GRANT_TYPE
                })
        );
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

    /***
     * Updates Application
     *
     * @param appName - Application name
     * @throws IOException IO exception throws if an error occurred when invoking token endpoint
     * @throws ParserConfigurationException,SAXException throws if an error occurred when parsing xml
     */
    private void updateSaasApp(String appName) throws ParserConfigurationException, IOException, SAXException {
        File getAppRequestXmlFile = new File(HandlerConstants.PAYLOADS_DIR + "/get-app-request.xml");
        String identityAppMgtUrl = iotsCoreUrl + HandlerConstants.IDENTITY_APP_MGT_ENDPOINT;

        HttpPost getApplicationEndpoint = new HttpPost(identityAppMgtUrl);
        getApplicationEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC +
                encodedAdminCredentials);
        getApplicationEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_XML.toString());
        getApplicationEndpoint.setHeader(HandlerConstants.SOAP_ACTION_HEADER, "urn:getApplication");

        String requestBodyString = HandlerUtil.xmlToString(getAppRequestXmlFile);

        Map<String, String> data = new HashMap<>();
        appName = adminUsername + HandlerConstants.UNDERSCORE + appName + HandlerConstants.UNDERSCORE +
                HandlerConstants.PRODUCTION_KEY;
        data.put("applicationName", appName);
        requestBodyString = StrSubstitutor.replace(requestBodyString, data);
        getApplicationEndpoint.setEntity(new StringEntity(requestBodyString, ContentType.TEXT_XML));

        ProxyResponse getApplicationEndpointResponse = HandlerUtil.execute(getApplicationEndpoint);

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(getApplicationEndpointResponse.getData())));
        NodeList nodeList = doc.getElementsByTagName(HandlerConstants.AX_PREFIX + "inboundAuthenticationConfig");
        NodeList childNodeList;
        String nodeName;

        data.clear();
        data.put("applicationId", doc.getElementsByTagName(HandlerConstants.AX_PREFIX + "applicationID").item(0).getTextContent());
        data.put("applicationName", doc.getElementsByTagName(HandlerConstants.AX_PREFIX + "applicationName").item(0).getTextContent());
        data.put("description", doc.getElementsByTagName(HandlerConstants.AX_PREFIX + "description").item(0).getTextContent());
        data.put("saasApp", "true");

        for (int i = 0; i < nodeList.getLength(); i++) {
            childNodeList = nodeList.item(i).getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                if (childNodeList.item(j).getNodeName().equalsIgnoreCase(HandlerConstants.AX_PREFIX + "inboundAuthenticationRequestConfigs")) {
                    NodeList inboundAuthRequestConfigs = childNodeList.item(j).getChildNodes();
                    for (int k = 0; k < inboundAuthRequestConfigs.getLength(); k++) {
                        nodeName = inboundAuthRequestConfigs.item(k).getNodeName();
                        if (nodeName.equalsIgnoreCase(HandlerConstants.AX_PREFIX + "inboundAuthKey")) {
                            data.put("inboundAuthKey", inboundAuthRequestConfigs.item(k).getTextContent());
                        }
                        if (nodeName.equalsIgnoreCase(HandlerConstants.AX_PREFIX + "inboundAuthType")) {
                            data.put("inboundAuthType", inboundAuthRequestConfigs.item(k).getTextContent());
                        }
                        if (nodeName.equalsIgnoreCase(HandlerConstants.AX_PREFIX + "inboundConfigType")) {
                            data.put("inboundConfigType", inboundAuthRequestConfigs.item(k).getTextContent());
                        }
                    }
                }
            }
        }

        nodeList = doc.getElementsByTagName(HandlerConstants.AX_PREFIX + "owner");
        for (int i = 0; i < nodeList.getLength(); i++) {
            childNodeList = nodeList.item(i).getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                switch (childNodeList.item(j).getNodeName()) {
                    case HandlerConstants.AX_PREFIX + "tenantDomain":
                        data.put("tenantDomain", childNodeList.item(j).getTextContent());
                        break;
                    case HandlerConstants.AX_PREFIX + "userName":
                        data.put("userName", childNodeList.item(j).getTextContent());
                        break;
                    case HandlerConstants.AX_PREFIX + "userStoreDomain":
                        data.put("userStoreDomain", childNodeList.item(j).getTextContent());
                        break;
                }
            }
        }

        nodeList = doc.getElementsByTagName(HandlerConstants.AX_PREFIX + "spProperties");
        for (int k = 0; k < nodeList.getLength(); k++) {
            childNodeList = nodeList.item(k).getChildNodes();
            for (int l = 0; l < childNodeList.getLength(); l++) {
                if (childNodeList.item(l).getNodeName().equalsIgnoreCase(HandlerConstants.AX_PREFIX + "value")) {
                    data.put("displayName", childNodeList.item(l).getTextContent());
                }
            }
        }

        File appUpdateRequest = new File(HandlerConstants.PAYLOADS_DIR + "/update-app-request.xml");
        String docStr = HandlerUtil.xmlToString(appUpdateRequest);
        requestBodyString = StrSubstitutor.replace(docStr, data);

        HttpPost updateApplicationEndpoint = new HttpPost(identityAppMgtUrl);
        updateApplicationEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC +
                encodedAdminCredentials);
        updateApplicationEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_XML.toString());
        updateApplicationEndpoint.setHeader(HandlerConstants.SOAP_ACTION_HEADER, "urn:updateApplication");
        updateApplicationEndpoint.setEntity(new StringEntity(requestBodyString, ContentType.TEXT_XML));

        HandlerUtil.execute(updateApplicationEndpoint);
    }
}
