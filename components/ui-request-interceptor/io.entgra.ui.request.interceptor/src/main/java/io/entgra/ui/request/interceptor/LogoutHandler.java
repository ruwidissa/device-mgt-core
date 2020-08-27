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

import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LogoutHandler.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession httpSession = req.getSession(false);
        if (httpSession != null) {
            httpSession.invalidate();
        } else {
            log.warn("No active session is available. User may not be logged in. Redirecting to the login page");
        }

        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(HttpStatus.SC_OK);
        try {
            HandlerUtil.handleSuccess(resp, proxyResponse);
        } catch (IOException e) {
            log.error("Error occurred when processing logout request.", e);
        }
    }
}
