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

import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@MultipartConfig
@WebServlet("/login-user/scopes")
public class PermissionScopeHandler extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession httpSession = req.getSession(false);
        if (httpSession == null) {
            HandlerUtil.sendUnAuthorizeResponse(resp);
            return;
        }

        AuthData authData = (AuthData) httpSession.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        if (authData == null) {
            HandlerUtil.sendUnAuthorizeResponse(resp);
            return;
        }

        if (!StringUtils.isEmpty(authData.getScope())) {
            ProxyResponse proxyResponse = new ProxyResponse();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(HandlerConstants.USER_SCOPES, authData.getScope());
            proxyResponse.setCode(HttpStatus.SC_OK);
            proxyResponse.setData(jsonObject.toString());
            HandlerUtil.handleSuccess(resp, proxyResponse);
        }
        HandlerUtil.handleError(resp, null);
    }
}
