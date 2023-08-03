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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@MultipartConfig
@WebServlet("/default-oauth2-credentials")
public class DefaultOauth2TokenHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(DefaultTokenHandler.class);


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            HttpSession httpSession = req.getSession(false);

            if (httpSession != null) {
                AuthData authData = (AuthData) httpSession.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
                if (authData == null) {
                    HandlerUtil.sendUnAuthorizeResponse(resp);
                    return;
                }

                AuthData defaultAuthData = (AuthData) httpSession
                        .getAttribute(HandlerConstants.SESSION_DEFAULT_AUTH_DATA_KEY);
                if (defaultAuthData != null) {
                    HandlerUtil.handleSuccess(resp, constructSuccessProxyResponse(defaultAuthData.getAccessToken()));
                    return;
                }

                String clientId = authData.getClientId();
                String clientSecret = authData.getClientSecret();

                String queryString = req.getQueryString();
                String scopeString = "";
                if (StringUtils.isNotEmpty(queryString)) {
                    scopeString = req.getParameter("scopes");
                    if (scopeString != null) {
                        scopeString = "?scopes=" + scopeString;
                    }
                }

                String iotsCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR
                        + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                        + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
                String tokenUrl = iotsCoreUrl + "/api/device-mgt/v1.0/devices/" + clientId
                        + "/" + clientSecret + "/default-token" + scopeString;

                HttpGet defaultTokenRequest = new HttpGet(tokenUrl);
                defaultTokenRequest
                        .setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                defaultTokenRequest
                        .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
                ProxyResponse tokenResultResponse = HandlerUtil.execute(defaultTokenRequest);

                if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API to get default token data.");
                    HandlerUtil.handleError(resp, tokenResultResponse);
                    return;
                }
                String tokenResult = tokenResultResponse.getData();
                if (tokenResult == null) {
                    log.error("Invalid default token response is received.");
                    HandlerUtil.handleError(resp, tokenResultResponse);
                    return;
                }

                JsonParser jsonParser = new JsonParser();
                JsonElement jTokenResult = jsonParser.parse(tokenResult);
                if (jTokenResult.isJsonObject()) {
                    JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
                    AuthData newDefaultAuthData = new AuthData();
                    newDefaultAuthData.setClientId(clientId);
                    newDefaultAuthData.setClientSecret(clientSecret);

                    String defaultToken = jTokenResultAsJsonObject.get("accessToken").getAsString();
                    newDefaultAuthData.setAccessToken(defaultToken);
                    newDefaultAuthData.setRefreshToken(jTokenResultAsJsonObject.get("refreshToken").getAsString());
                    newDefaultAuthData.setScope(jTokenResultAsJsonObject.get("scopes").getAsString());
                    httpSession.setAttribute(HandlerConstants.SESSION_DEFAULT_AUTH_DATA_KEY, newDefaultAuthData);

                    HandlerUtil.handleSuccess(resp, constructSuccessProxyResponse(defaultToken));
                }
            } else {
                HandlerUtil.sendUnAuthorizeResponse(resp);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing GET request to get default token.", e);
        }
    }

    /**
     * Get Success Proxy Response
     * @param defaultAccessToken Access token which has default scope
     * @return {@link ProxyResponse}
     */
    private ProxyResponse constructSuccessProxyResponse (String defaultAccessToken) {

        URIBuilder ub = new URIBuilder();
        ub.setScheme(HandlerConstants.WSS_PROTOCOL);
        ub.setHost(System.getProperty(HandlerConstants.IOT_REMOTE_SESSION_HOST_ENV_VAR));
        ub.setPort(Integer.parseInt(System.getProperty(HandlerConstants.IOT_REMOTE_SESSION_HTTPS_PORT_ENV_VAR)));
        ub.setPath(HandlerConstants.REMOTE_SESSION_CONTEXT);

        URIBuilder ub2 = new URIBuilder();
        ub2.setScheme(HandlerConstants.WSS_PROTOCOL);
        ub2.setHost(System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR));
        ub2.setPort(Integer.parseInt(System.getProperty(HandlerConstants.IOT_GATEWAY_WEBSOCKET_WSS_PORT_ENV_VAR)));

        URIBuilder ub3 = new URIBuilder();
        ub3.setScheme(HandlerConstants.WS_PROTOCOL);
        ub3.setHost(System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR));
        ub3.setPort(Integer.parseInt(System.getProperty(HandlerConstants.IOT_GATEWAY_WEBSOCKET_WS_PORT_ENV_VAR)));

        JsonObject responseJsonObj = new JsonObject();
        responseJsonObj.addProperty("default-access-token", defaultAccessToken);
        responseJsonObj.addProperty("remote-session-base-url", ub.toString());
        responseJsonObj.addProperty("secured-websocket-gateway-url", ub2.toString());
        responseJsonObj.addProperty("unsecured-websocket-gateway-url", ub3.toString());

        Gson gson = new Gson();
        String payload = gson.toJson(responseJsonObj);

        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(HttpStatus.SC_OK);
        proxyResponse.setStatus(ProxyResponse.Status.SUCCESS);
        proxyResponse.setData(payload);
        return proxyResponse;
    }
}