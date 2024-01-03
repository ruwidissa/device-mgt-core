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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.JITData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.JITEnrollmentData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.exceptions.JITEnrollmentException;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

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
import java.util.Objects;

@WebServlet(
        name = "JIT Enrollment callback handler",
        description = "Call token endpoint and retrieve token",
        urlPatterns = {
                "/jit-enrollment-callback"
        }
)
public class JITEnrollmentCallbackHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(JITEnrollmentCallbackHandler.class);
    private String gatewayUrl;
    private String keyManagerUrl;
    private JITData JITInfo;
    private String encodedClientCredentials;
    private String applicationName;
    private String clientId;
    private String clientSecret;
    private String scope;
    private String JITConfigurationPath;
    private JITEnrollmentData JITEnrollmentInfo;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        gatewayUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(request.getScheme());
        keyManagerUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(request.getScheme());
        JITConfigurationPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "jit-config.xml";
        HttpSession session = request.getSession(false);
        try {
            if (session == null) {
                response.sendError(HttpStatus.SC_UNAUTHORIZED);
                return;
            }

            JITInfo = (JITData) session.getAttribute(HandlerConstants.SESSION_JIT_DATA_KEY);
            if (JITInfo == null) {
                response.sendError(HttpStatus.SC_UNAUTHORIZED);
                return;
            }

            JITEnrollmentInfo = (JITEnrollmentData)
                    session.getAttribute(HandlerConstants.SESSION_JIT_ENROLLMENT_DATA_KEY);
            if (JITEnrollmentInfo == null) {
                response.sendError(HttpStatus.SC_UNAUTHORIZED);
                return;
            }
            applicationName = request.getContextPath().substring(1,
                    request.getContextPath().indexOf("-ui-request-handler"));
            initializeJITEnrollmentConfigurations();
            populateApplicationData(registerApplication());
            persistAuthData(session, getToken());
            response.sendRedirect(JITEnrollmentInfo.getRedirectUrl() + "?ownershipType=" +
                    JITEnrollmentInfo.getOwnershipType() + "&os=" + JITEnrollmentInfo.getOs() + "&username=" +
                    JITEnrollmentInfo.getUsername() + "&tenantDomain=" + JITEnrollmentInfo.getTenantDomain());
        } catch (JITEnrollmentException | IOException ex) {
            log.error("Error occurred while processing JIT provisioning callback request", ex);
        }
    }

    private void initializeJITEnrollmentConfigurations() throws JITEnrollmentException {
        try {
            File JITConfigurationFile = new File(JITConfigurationPath);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document JITConfigurationDoc = documentBuilder.parse(JITConfigurationFile);
            JITConfigurationDoc.getDocumentElement().normalize();
            Element enrollmentScopes;
            if (Objects.equals(JITEnrollmentInfo.getOs(), HandlerConstants.OS_ANDROID)) {
                enrollmentScopes = (Element) JITConfigurationDoc.
                        getElementsByTagName(HandlerConstants.TAG_ANDROID_ENROLLMENT_SCOPES).item(0);
            } else if (Objects.equals(JITEnrollmentInfo.getOs(), HandlerConstants.OS_IOS)) {
                enrollmentScopes = (Element) JITConfigurationDoc.
                        getElementsByTagName(HandlerConstants.TAG_IOS_ENROLLMENT_SCOPES).item(0);
            } else if (Objects.equals(JITEnrollmentInfo.getOs(), HandlerConstants.OS_WINDOWS))  {
                enrollmentScopes = (Element) JITConfigurationDoc.
                        getElementsByTagName(HandlerConstants.TAG_WINDOWS_ENROLLMENT_SCOPES).item(0);
            } else {
                String msg = "OS type not supported";
                if (log.isDebugEnabled()) {
                    log.error(msg);
                }
                throw new JITEnrollmentException(msg);
            }
            NodeList scopeList = enrollmentScopes.getElementsByTagName("Scope");
            StringBuilder scopeStr = new StringBuilder();
            for (int idx = 0; idx < scopeList.getLength(); idx++) {
                Node scopeNode = scopeList.item(idx);
                if (scopeNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element scopeElement = (Element) scopeNode;
                    scopeStr.append(" ").append(scopeElement.getTextContent());
                }
            }
            scope = scopeStr.toString();
        } catch (ParserConfigurationException ex) {
            String msg = "Error occurred when document builder creating the file configuration";
            throw new JITEnrollmentException(msg, ex);
        } catch (IOException ex) {
            String msg = "IO error occurred while parsing the JIT config file";
            throw new JITEnrollmentException(msg, ex);
        } catch (SAXException ex) {
            String msg = "Parse error occurred while parsing the JIT config document";
            throw new JITEnrollmentException(msg, ex);
        }
    }

    /***
     * Parse string data and build json object
     * @param data  - Json string
     * @return {@link JsonObject} Json object corresponding to provided json string
     * @throws JITEnrollmentException throws when error occurred while parsing
     */
    private JsonObject parseResponseData(String data) throws JITEnrollmentException {
        JsonParser parser = new JsonParser();
        JsonElement responseData = parser.parse(data);
        if (responseData.isJsonObject()) {
            return responseData.getAsJsonObject();
        }
        throw new JITEnrollmentException("Unexpected response body return");
    }

    /***
     * Build application registration request
     * @return {@link HttpPost} Application registration request
     */
    private HttpPost buildApplicationRegistrationRequest() {
        HttpPost applicationRegistrationRequest = new HttpPost(gatewayUrl + HandlerConstants.APP_REG_ENDPOINT);
        applicationRegistrationRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC
                + JITInfo.getEncodedClientCredentials());
        applicationRegistrationRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        JsonArray tags = new JsonArray();
        tags.add("device_management");
        JsonObject payload = new JsonObject();
        payload.addProperty("applicationName", applicationName);
        payload.add("tags", tags);
        payload.addProperty("allowedToAllDomains", false);
        payload.addProperty("mappingAnExistingOAuthApp", false);
        applicationRegistrationRequest.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        return applicationRegistrationRequest;
    }

    /***
     * Populate dynamic client's data
     * @param application - application data receiving from dcr request
     */
    private void populateApplicationData(JsonObject application) {
        clientId = application.get("client_id").getAsString();
        clientSecret = application.get("client_secret").getAsString();
        String headerValue = clientId+ ':' + clientSecret;
        encodedClientCredentials = Base64.getEncoder().encodeToString(headerValue.getBytes());
    }

    /***
     * Register client application
     * @return {@link JsonObject} Json object contain registered application data
     * @throws JITEnrollmentException throws when error occurred while application registration
     */
    private JsonObject registerApplication() throws JITEnrollmentException {
        try {
            ProxyResponse proxyResponse = HandlerUtil.execute(buildApplicationRegistrationRequest());
            if (proxyResponse.getCode() == HttpStatus.SC_CREATED ||
                    proxyResponse.getCode() == HttpStatus.SC_OK) {
                return parseResponseData(proxyResponse.getData());
            }
            throw new JITEnrollmentException("Unexpected response status return for application registration request");
        } catch (IOException ex) {
            throw new JITEnrollmentException("Error occurred while executing application registration request", ex);
        }
    }

    /***
     * Acquire token
     * @return {@link JsonObject} Json object containing token data
     * @throws JITEnrollmentException throws when error occurred while acquiring token
     */
    private JsonObject getToken() throws JITEnrollmentException {
        try {
            ProxyResponse proxyResponse = HandlerUtil.execute(buildTokenAcquireRequest());
            if (proxyResponse.getCode() == org.apache.http.HttpStatus.SC_CREATED ||
                    proxyResponse.getCode() == org.apache.http.HttpStatus.SC_OK) {
                return parseResponseData(proxyResponse.getData());
            }
            throw new JITEnrollmentException("Unexpected response status return for token acquiring request");
        } catch (IOException ex) {
            throw new JITEnrollmentException("Error occurred while executing token acquiring request", ex);
        }
    }

    /***
     * Build token acquire request
     * @return {@link HttpPost} Token acquire request
     */
    private HttpPost buildTokenAcquireRequest() {
        HttpPost tokenAcquiringRequest = new HttpPost(keyManagerUrl + HandlerConstants.OAUTH2_TOKEN_ENDPOINT);
        tokenAcquiringRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
        tokenAcquiringRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC
                + encodedClientCredentials);
        StringEntity payload = new StringEntity(
                "grant_type=" + HandlerConstants.CLIENT_CREDENTIAL_GRANT_TYPE + "&scope=" + scope,
                ContentType.APPLICATION_FORM_URLENCODED);
        tokenAcquiringRequest.setEntity(payload);
        return tokenAcquiringRequest;
    }

    /***
     * Persists auth data in session
     * @param session   - {@link HttpSession}
     * @param token     - Json object containing token data
     */
    private void persistAuthData(HttpSession session, JsonObject token) {
        AuthData authData = new AuthData();
        authData.setAccessToken(token.get("access_token").getAsString());
        authData.setClientId(clientId);
        authData.setClientSecret(clientSecret);
        authData.setEncodedClientApp(encodedClientCredentials);
        authData.setScope(token.get("scope").getAsString());
        session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, authData);
    }
}
