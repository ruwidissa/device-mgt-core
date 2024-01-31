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
import com.google.gson.JsonSyntaxException;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.UserLoginLogContext;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraUserLoginLoggerImpl;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@MultipartConfig
@WebServlet("/user")
public class UserHandler extends HttpServlet {
    private static final EntgraLogger log = new EntgraUserLoginLoggerImpl(UserHandler.class);
    UserLoginLogContext.Builder userLoginLogContextBuilder = new UserLoginLogContext.Builder();
    private static final long serialVersionUID = 9050048549140517002L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String keyManagerUrl =
                    req.getScheme() + HandlerConstants.SCHEME_SEPARATOR +
                            System.getProperty(HandlerConstants.IOT_KM_HOST_ENV_VAR)
                            + HandlerConstants.COLON + HandlerUtil.getKeyManagerPort(req.getScheme());
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

            String accessToken = authData.getAccessToken();
            DeviceManagementConfig dmc = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            String adminUsername = dmc.getKeyManagerConfigurations().getAdminUsername();
            String adminPassword = dmc.getKeyManagerConfigurations().getAdminPassword();

            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("token", accessToken));

            ClassicHttpRequest introspectCall = ClassicRequestBuilder.post(keyManagerUrl + HandlerConstants.INTROSPECT_ENDPOINT)
                    .setEntity(new UrlEncodedFormEntity(nameValuePairs))
                    .setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString())
                    .setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + Base64.getEncoder().encodeToString((adminUsername + HandlerConstants.COLON + adminPassword).getBytes()))
                    .build();

            ProxyResponse tokenStatus = HandlerUtil.execute(introspectCall);
            if (tokenStatus.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                if (tokenStatus.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                    tokenStatus = HandlerUtil.retryRequestWithRefreshedToken(req, introspectCall, keyManagerUrl);
                    if (!HandlerUtil.isResponseSuccessful(tokenStatus)) {
                        HandlerUtil.handleError(resp, tokenStatus);
                        return;
                    }
                } else {
                    log.error("Error occurred while invoking the API to get token status.");
                    HandlerUtil.handleError(resp, tokenStatus);
                    return;
                }
            }
            JsonNode tokenData = tokenStatus.getData();
            if (tokenData == null) {
                log.error("Invalid token data is received.");
                HandlerUtil.handleError(resp, tokenStatus);
                return;
            }

            if (!tokenData.get("active").asBoolean()) {
                HandlerUtil.sendUnAuthorizeResponse(resp);
                return;
            }
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setStatus(ProxyResponse.Status.SUCCESS);
            proxyResponse.setCode(HttpStatus.SC_OK);

            ObjectMapper mapper = new ObjectMapper();
            String data = tokenData.get("username").textValue().replaceAll("@carbon.super", "");
            proxyResponse.setData(mapper.convertValue(data, JsonNode.class));

            HandlerUtil.handleSuccess(resp, proxyResponse);
            httpSession.setAttribute(HandlerConstants.USERNAME_WITH_DOMAIN, tokenData.get("username").toString());
            log.info(
                    "User " + proxyResponse.getData() + " logged in",
                    userLoginLogContextBuilder
                            .setUserName(proxyResponse.getData().toString())
                            .setUserRegistered(true)
                            .build()
            );
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        }
    }
}
