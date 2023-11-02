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

import io.entgra.device.mgt.core.ui.request.interceptor.beans.JITData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.JITEnrollmentData;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;

@WebServlet(
        name = "JIT callback handler",
        description = "Call token endpoint and retrieve token",
        urlPatterns = {
                "/jit-provision-callback"
        }
)
public class JITProvisionCallbackHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(JITProvisionCallbackHandler.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        String state = request.getParameter("state");
        HttpSession session = request.getSession(false);
        String JITProvisionCallbackURL = request.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getCorePort(request.getScheme())
                + request.getContextPath()
                + HandlerConstants.JIT_PROVISION_CALLBACK_URL;
        try {
            if (session == null) {
                response.sendError(HttpStatus.SC_UNAUTHORIZED);
                return;
            }

            if (state == null || !Objects.equals(state, session.getAttribute("state").toString())) {
                response.sendError(org.apache.http.HttpStatus.SC_BAD_REQUEST, "MismatchingStateError: CSRF Warning! " +
                        "State not equal in request and response");
                return;
            }

            JITData JITInfo = (JITData) session.getAttribute(HandlerConstants.SESSION_JIT_DATA_KEY);
            if (JITInfo == null) {
                response.sendError(HttpStatus.SC_UNAUTHORIZED);
                return;
            }

            response.sendRedirect(JITInfo.getRedirectUrl() + "?code=" + request.getParameter("code")
                    + "&redirectUrl=" + JITProvisionCallbackURL);
        } catch (IOException ex) {
            log.error("Error occurred while processing JIT provisioning callback request", ex);
        }
    }
}
