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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@MultipartConfig
@WebServlet("/ssoLoginCallback")
public class SsoLoginCallbackHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(SsoLoginCallbackHandler.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        HttpSession session = req.getSession(false);
        String scope = session.getAttribute("scope").toString();
        String iotsCorePort = System.getProperty("iot.core.https.port");

        if (HandlerConstants.HTTP_PROTOCOL.equals(req.getScheme())) {
            iotsCorePort = System.getProperty("iot.core.http.port");
        }

        String gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.gateway.host")
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
        String iotsCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.core.host")
                + HandlerConstants.COLON + iotsCorePort;

        HttpPost tokenEndpoint = new HttpPost(gatewayUrl + HandlerConstants.TOKEN_ENDPOINT);
        tokenEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + session.getAttribute("encodedClientApp"));
        tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());

        String loginCallbackUrl = iotsCoreUrl + req.getContextPath() + HandlerConstants.SSO_LOGIN_CALLBACK;

        StringEntity tokenEPPayload = new StringEntity(
                "grant_type=" + HandlerConstants.CODE_GRANT_TYPE + "&code=" + code + "&state=&scope=" + scope +
                        "&redirect_uri=" + loginCallbackUrl,
                ContentType.APPLICATION_FORM_URLENCODED);
        tokenEndpoint.setEntity(tokenEPPayload);
        ProxyResponse tokenResultResponse = HandlerUtil.execute(tokenEndpoint);

        JsonParser jsonParser = new JsonParser();

        JsonElement jTokenResult = jsonParser.parse(tokenResultResponse.getData());
        if (jTokenResult.isJsonObject()) {
            JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();

            AuthData authData = new AuthData();
            authData.setClientId(session.getAttribute("clientId").toString());
            authData.setClientSecret(session.getAttribute("clientSecret").toString());
            authData.setEncodedClientApp(session.getAttribute("encodedClientApp").toString());
            authData.setAccessToken(jTokenResultAsJsonObject.get("access_token").getAsString());
            authData.setRefreshToken(jTokenResultAsJsonObject.get("refresh_token").getAsString());
            authData.setScope(jTokenResultAsJsonObject.get("scope").getAsString());
            session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, authData);

            resp.sendRedirect(session.getAttribute("redirectUrl").toString());
        }
    }
}
