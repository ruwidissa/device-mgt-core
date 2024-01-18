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

import com.fasterxml.jackson.databind.JsonNode;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Objects;

@MultipartConfig
@WebServlet("/ssoLoginCallback")
public class SsoLoginCallbackHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(SsoLoginCallbackHandler.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String state = req.getParameter("state");
        String code = req.getParameter("code");
        HttpSession session = req.getSession(false);

        String iotsCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getCorePort(req.getScheme());
        String keyManagerUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(req.getScheme());

        if (session == null) {
            String baseContextPath = req.getContextPath();
            String applicationName = baseContextPath.substring(1, baseContextPath.indexOf("-ui-request-handler"));
            if (applicationName.equals("entgra")) {
                resp.sendRedirect(iotsCoreUrl + "/endpoint-mgt");
            } else {
                resp.sendRedirect(iotsCoreUrl + "/" + applicationName);
            }
            return;
        }

        if (state == null || !Objects.equals(state, session.getAttribute("state").toString())) {
            resp.sendError(HttpStatus.SC_BAD_REQUEST, "MismatchingStateError: CSRF Warning! State not equal in request and response");
            return;
        }

        String scope = session.getAttribute("scope").toString();
        String loginCallbackUrl = iotsCoreUrl + req.getContextPath() + HandlerConstants.SSO_LOGIN_CALLBACK;

        StringEntity tokenEPPayload = new StringEntity(
                "grant_type=" + HandlerConstants.CODE_GRANT_TYPE + "&code=" + code + "&scope=" + scope +
                        "&redirect_uri=" + loginCallbackUrl,
                ContentType.APPLICATION_FORM_URLENCODED);

        ClassicHttpRequest tokenEndpoint = ClassicRequestBuilder.post(keyManagerUrl + HandlerConstants.OAUTH2_TOKEN_ENDPOINT)
                .setEntity(tokenEPPayload)
                .setHeader(org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE, org.apache.hc.core5.http.ContentType.APPLICATION_FORM_URLENCODED.toString())
                .setHeader(org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + session.getAttribute("encodedClientApp"))
                .build();

        ProxyResponse tokenResultResponse = HandlerUtil.execute(tokenEndpoint);
        JsonNode jsonNode = tokenResultResponse.getData();

        if (jsonNode != null) {
            AuthData authData = new AuthData();
            authData.setClientId(session.getAttribute("clientId").toString());
            authData.setClientSecret(session.getAttribute("clientSecret").toString());
            authData.setEncodedClientApp(session.getAttribute("encodedClientApp").toString());
            authData.setAccessToken(jsonNode.get("access_token").textValue());
            authData.setRefreshToken(jsonNode.get("refresh_token").textValue());
            authData.setScope(jsonNode.get("scope"));
            session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, authData);
            resp.sendRedirect(session.getAttribute("redirectUrl").toString());
        } else {
            log.error("Found empty response for token call.");
            HandlerUtil.handleError(resp, HandlerConstants.INTERNAL_ERROR_CODE);
        }
    }
}
