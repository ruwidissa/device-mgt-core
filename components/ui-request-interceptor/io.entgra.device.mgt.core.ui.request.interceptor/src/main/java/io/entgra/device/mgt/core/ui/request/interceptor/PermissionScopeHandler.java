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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@MultipartConfig
@WebServlet("/login-user/scopes")
public class PermissionScopeHandler extends HttpServlet {
    private static final long serialVersionUID = 976006906915355611L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        if (!StringUtils.isEmpty(authData.getScope().toString())) {
            ProxyResponse proxyResponse = new ProxyResponse();
            JsonNode authDataScope = authData.getScope();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> nodeMap = new HashMap<>();
            nodeMap.put(HandlerConstants.USER_SCOPES, authDataScope.asText().replace("\"", ""));
            proxyResponse.setCode(HttpStatus.SC_OK);
            proxyResponse.setStatus(ProxyResponse.Status.SUCCESS);
            proxyResponse.setData(mapper.convertValue(nodeMap, JsonNode.class));
            HandlerUtil.handleSuccess(resp, proxyResponse);
            return;
        }
        HandlerUtil.handleError(resp, null);
    }
}
