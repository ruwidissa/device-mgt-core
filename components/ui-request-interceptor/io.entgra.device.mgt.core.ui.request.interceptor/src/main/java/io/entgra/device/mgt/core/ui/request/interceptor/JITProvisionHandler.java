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
import java.util.HashMap;
import java.util.Map;
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
    private String clientId;
    private String JITServiceProviderName;
    private String encodedClientCredentials;
    private String JITConfigurationPath;
    private String redirectUrl;
    private String state;
    private static final Map<String, Element> tenantConfigs = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String keyManagerUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(request.getScheme());
        String JITCallbackUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getCorePort(request.getScheme())
                + request.getContextPath()
                + HandlerConstants.JIT_PROVISION_CALLBACK_URL;
        JITConfigurationPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "jit-config.xml";
        String scope = "openid";
        state = HandlerUtil.generateStateToken();
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

            persistJITData(request.getSession(true));
            response.sendRedirect(keyManagerUrl + HandlerConstants.AUTHORIZATION_ENDPOINT +
                    "?response_type=code" +
                    "&client_id=" + clientId +
                    "&state=" + state +
                    "&scope=" + scope +
                    "&redirect_uri=" + JITCallbackUrl);
        } catch (JITProvisionException | IOException ex) {
            log.error("Error occurred while processing JIT provisioning request", ex);
        }
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
        session.setAttribute("state", state);
        session.setAttribute(HandlerConstants.SESSION_JIT_DATA_KEY, JITInfo);
    }

    /***
     * Find the tenant based configurations and return
     * @param tenantDomain  - Domain of the tenant
     * @param document      - Config doc
     * @return {@link Element} If config found return configuration element, otherwise null
     */
    private Element findServiceProvider(String tenantDomain, Document document) {
        NodeList serviceProviderConfiguration = document.getElementsByTagName("ServiceProvider");
        for (int idx = 0; idx < serviceProviderConfiguration.getLength(); idx++) {
            Node configNode = serviceProviderConfiguration.item(idx);
            if (configNode.getNodeType() == Node.ELEMENT_NODE) {
                Element configElement = (Element) configNode;
                if (Objects.equals(configElement.getAttributes().
                        getNamedItem("tenantDomain").getNodeValue(), tenantDomain) &&
                        Objects.equals(configElement.getAttributes().getNamedItem("name").getNodeValue(),
                                JITServiceProviderName)) {
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
            Element serviceProvider = tenantConfigs.get(tenantDomain);
            if (serviceProvider == null) {
                File JITConfigurationFile = new File(JITConfigurationPath);
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document JITConfigurationDoc = documentBuilder.parse(JITConfigurationFile);
                JITConfigurationDoc.getDocumentElement().normalize();
                serviceProvider = findServiceProvider(tenantDomain, JITConfigurationDoc);
                if (serviceProvider == null) return false;
                tenantConfigs.put(tenantDomain, serviceProvider);
            }
            clientId = serviceProvider.getElementsByTagName("ClientId").item(0).getTextContent();
            String clientSecret = serviceProvider.getElementsByTagName("ClientSecret").item(0).getTextContent();
            String headerValue = clientId + ":" + clientSecret;
            encodedClientCredentials = Base64.getEncoder().encodeToString(headerValue.getBytes());
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
