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

import io.entgra.device.mgt.core.ui.request.interceptor.beans.JITEnrollmentData;
import io.entgra.device.mgt.core.ui.request.interceptor.exceptions.JITEnrollmentException;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(
        name = "JIT enrollment handler",
        description = "Handle jit enrollment request",
        urlPatterns = {
                "/jit-enrollment"
        }
)
public class JITEnrollmentHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(JITEnrollmentHandler.class);
    private String username;
    private String ownershipType;
    private String os;
    private String redirectUrl;
    private String tenantDomain;
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            HttpSession session = request.getSession(true);
            String JITProvisionHandlerUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                    + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getCorePort(request.getScheme())
                    + request.getContextPath()
                    + HandlerConstants.JIT_PROVISION_HANDLER;
            String onCompletionUrl = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                    + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getCorePort(request.getScheme())
                    + request.getContextPath()
                    + "/jit-enrollment-callback";
            username = request.getParameter("username");
            ownershipType = request.getParameter("ownershipType");
            os = request.getParameter("os");
            redirectUrl = request.getParameter("redirectUrl");
            tenantDomain = request.getParameter("tenantDomain");
            String sp = request.getParameter("sp");
            persistJITData(session);
            response.sendRedirect(JITProvisionHandlerUrl + "?tenantDomain=" + tenantDomain
                    + "&sp=" + sp + "&redirectUrl=" + onCompletionUrl);
        } catch (IOException ex) {
            log.error("Error occurred while handling JIT enrollment request");
        }
    }

    /***
     * Persists JIT data in session
     * @param session   - {@link HttpSession}
     */
    private void persistJITData(HttpSession session) {
        JITEnrollmentData JITEnrollmentInfo = new JITEnrollmentData();
        JITEnrollmentInfo.setOwnershipType(ownershipType);
        JITEnrollmentInfo.setOs(os);
        JITEnrollmentInfo.setUsername(username);
        JITEnrollmentInfo.setRedirectUrl(redirectUrl);
        JITEnrollmentInfo.setTenantDomain(tenantDomain);
        session.setAttribute(HandlerConstants.SESSION_JIT_ENROLLMENT_DATA_KEY, JITEnrollmentInfo);
    }
}
