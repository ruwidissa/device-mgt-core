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

import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import java.io.IOException;

@MultipartConfig
@WebServlet("/ssoLogout")
public class SsoLogoutHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(SsoLogoutHandler.class);

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        for (String path : HandlerConstants.SSO_LOGOUT_COOKIE_PATHS) {
            removeCookie(HandlerConstants.JSESSIONID_KEY, path, resp);
        }
        removeCookie(HandlerConstants.COMMON_AUTH_ID_KEY, "/", resp);
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(HttpStatus.SC_OK);

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        try {
            HandlerUtil.handleSuccess(resp, proxyResponse);
        } catch (IOException e) {
            log.error("Error occurred when processing logout request.", e);
        }
    }

    private static void removeCookie(String cookieName,String path, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath(path);
        cookie.setValue(null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
