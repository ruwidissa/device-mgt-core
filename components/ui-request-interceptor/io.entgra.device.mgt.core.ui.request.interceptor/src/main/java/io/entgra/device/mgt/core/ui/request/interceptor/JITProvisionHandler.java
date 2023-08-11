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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.JITData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.exceptions.JITEnrollmentException;
import io.entgra.device.mgt.core.ui.request.interceptor.exceptions.JITProvisionException;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
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
        name = "JITProvisionRequestHandlerServlet",
        description = "Handle Just In Time Provisioning requests",
        urlPatterns = {
                "/jit-provision"
        }
)
public class JITProvisionHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(JITProvisionHandler.class);
    private String tenantDomain;
    private String adminUsername;
    private String clientId;
    private String JITServiceProviderName;
    private String apiManagerUrl;
    private String encodedAdminCredentials;
    private String encodedClientCredentials;
    private String JITConfigurationPath;
    private String JITCallbackUrl;
    private String redirectUrl;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String keyManagerUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(request.getScheme());
        JITCallbackUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getCorePort(request.getScheme())
                + request.getContextPath()
                + HandlerConstants.JIT_PROVISION_CALLBACK_URL;
        apiManagerUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_APIM_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getAPIManagerPort(request.getScheme());
        JITConfigurationPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "jit-config.xml";
        String scope = "openid";
        tenantDomain = request.getParameter("tenantDomain");
        redirectUrl = request.getParameter("redirectUrl");
        JITServiceProviderName = request.getParameter("sp");
        try {
            if (tenantDomain == null || JITServiceProviderName == null) {
                HandlerUtil.handleError(response, HttpStatus.SC_BAD_REQUEST);
                return;
            }
            if (!initializeJITConfigurations()) {
                HandlerUtil.handleError(response, HttpStatus.SC_SERVICE_UNAVAILABLE);
                return;
            }

            populateServiceProvider();
            persistJITData(request.getSession(true));
            response.sendRedirect(keyManagerUrl + HandlerConstants.AUTHORIZATION_ENDPOINT +
                    "?response_type=code" +
                    "&client_id=" + clientId +
                    "&state=" +
                    "&scope=" + scope +
                    "&redirect_uri=" + JITCallbackUrl);
        } catch (JITProvisionException | IOException ex) {
            log.error("Error occurred while processing JIT provisioning request", ex);
        }
    }

    /***
     * Construct dynamic client registration request
     * @return {@link HttpPost} DCR request
     */
    private HttpPost buildDCRRequest() {
        HttpPost DCRRequest = new HttpPost(apiManagerUrl + HandlerConstants.DCR_URL);
        DCRRequest.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        DCRRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + encodedAdminCredentials);
        JsonObject payload = new JsonObject();
        payload.addProperty("clientName", JITServiceProviderName);
        payload.addProperty("owner", adminUsername);
        payload.addProperty("saasApp", true);
        payload.addProperty("grantType", HandlerConstants.CODE_GRANT_TYPE);
        payload.addProperty("callbackUrl", JITCallbackUrl);
        DCRRequest.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        return DCRRequest;
    }

    /***
     * Retrieve JIT data from current session if session exists, otherwise build and return
     * @param session - {@link HttpSession}
     * @return {@link JITData}
     */
    private JITData getJITData(HttpSession session) {
        return (session.getAttribute(HandlerConstants.SESSION_JIT_DATA_KEY) != null) ?
                (JITData) session.getAttribute(HandlerConstants.SESSION_JIT_DATA_KEY) : new JITData();
    }

    /***
     * Persists JIT data in session
     * @param session {@link HttpSession}
     */
    private void persistJITData(HttpSession session) {
        JITData JITInfo = getJITData(session);
        JITInfo.setEncodedClientCredentials(encodedClientCredentials);
        JITInfo.setTenantDomain(tenantDomain);
        JITInfo.setRedirectUrl(redirectUrl);
        JITInfo.setSp(JITServiceProviderName);
        session.setMaxInactiveInterval(3600);
        session.setAttribute(HandlerConstants.SESSION_JIT_DATA_KEY, JITInfo);
    }

    /***
     * Populate service provider details
     * @throws JITProvisionException throws when dcr request fails due to IO exception
     */
    private void populateServiceProvider() throws JITProvisionException {
        try {
            HttpPost DCRRequest = buildDCRRequest();
            ProxyResponse proxyResponse = HandlerUtil.execute(DCRRequest);
            if (proxyResponse.getCode() == HttpStatus.SC_OK) {
                JsonObject serviceProvider = parseResponseData(proxyResponse.getData());
                clientId = serviceProvider.get("clientId").getAsString();
                String clientSecret = serviceProvider.get("clientSecret").getAsString();
                String headerValue = clientId + ':' + clientSecret;
                encodedClientCredentials = Base64.getEncoder().encodeToString(headerValue.getBytes());
            }
        } catch (IOException ex) {
            String msg = "Error exception occurred while executing proxy request";
            throw new JITProvisionException(msg, ex);
        }
    }

    /***
     * Parse string data and build json object
     * @param data  - Json string
     * @return {@link JsonObject} Json object corresponding to provided json string
     * @throws JITProvisionException throws when error occurred while parsing
     */
    private JsonObject parseResponseData(String data) throws JITProvisionException {
        JsonParser parser = new JsonParser();
        JsonElement responseData = parser.parse(data);
        if (responseData.isJsonObject()) {
            return responseData.getAsJsonObject();
        }
        throw new JITProvisionException("Unexpected response body return");
    }

    /***
     * Find the tenant based configurations and return
     * @param tenantDomain  - Domain of the tenant
     * @param document      - Config doc
     * @return {@link Element} If config found return configuration element, otherwise null
     */
    private Element findTenantConfigs(String tenantDomain, Document document) {
        NodeList tenantConfigurations = document.getElementsByTagName("TenantConfiguration");
        for (int idx = 0; idx < tenantConfigurations.getLength(); idx++) {
            Node configNode = tenantConfigurations.item(idx);
            if (configNode.getNodeType() == Node.ELEMENT_NODE) {
                Element configElement = (Element) configNode;
                if (Objects.equals(configElement.getAttributes().
                        getNamedItem("tenantDomain").getNodeValue(), tenantDomain)) {
                    return configElement;
                }
            }
        }
        return null;
    }

    /***
     * Initialize JIT configurations
     * @return boolean true when successful initialization, otherwise false
     * @throws JITProvisionException throws when error occurred
     */
    private boolean initializeJITConfigurations() throws JITProvisionException {
        try {
            File JITConfigurationFile = new File(JITConfigurationPath);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document JITConfigurationDoc = documentBuilder.parse(JITConfigurationFile);
            JITConfigurationDoc.getDocumentElement().normalize();
            Element tenantConfig = findTenantConfigs(tenantDomain, JITConfigurationDoc);
            if (tenantConfig == null) return false;
            adminUsername = tenantConfig.getElementsByTagName("AdminUsername").item(0).getTextContent();
            String adminPassword = tenantConfig.getElementsByTagName("AdminPassword").item(0).getTextContent();
            String headerValue = adminUsername + ":" + adminPassword;
            encodedAdminCredentials = Base64.getEncoder().encodeToString(headerValue.getBytes());
            return true;
        } catch (ParserConfigurationException ex) {
            String msg = "Error occurred when document builder creating the file configuration";
            throw new JITProvisionException(msg, ex);
        } catch (IOException ex) {
            String msg = "IO error occurred while parsing the JIT config file";
            throw new JITProvisionException(msg, ex);
        } catch (SAXException ex) {
            String msg = "Parse error occurred while parsing the JIT config document";
            throw new JITProvisionException(msg, ex);
        }
    }
}
