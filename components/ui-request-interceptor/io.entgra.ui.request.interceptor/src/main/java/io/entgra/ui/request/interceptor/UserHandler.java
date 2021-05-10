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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;

@MultipartConfig
@WebServlet("/user")
public class UserHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(UserHandler.class);
    private static final long serialVersionUID = 9050048549140517002L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String serverUrl =
                    req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.core.host")
                            + HandlerConstants.COLON + HandlerUtil.getCorePort(req.getScheme());
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

            HttpPost introspectionEndpoint = new HttpPost(serverUrl + HandlerConstants.INTROSPECT_ENDPOINT);
            introspectionEndpoint.setHeader(HttpHeaders.CONTENT_TYPE,
                    ContentType.APPLICATION_FORM_URLENCODED.toString());
            DeviceManagementConfig dmc = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
            String username = dmc.getKeyManagerConfigurations().getAdminUsername();
            String password = dmc.getKeyManagerConfigurations().getAdminPassword();
            introspectionEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC + Base64.getEncoder()
                    .encodeToString((username + HandlerConstants.COLON + password).getBytes()));
            StringEntity introspectionPayload = new StringEntity("token=" + accessToken,
                    ContentType.APPLICATION_FORM_URLENCODED);
            introspectionEndpoint.setEntity(introspectionPayload);
            ProxyResponse introspectionStatus = HandlerUtil.execute(introspectionEndpoint);

            if (introspectionStatus.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API to get token status.");
                HandlerUtil.handleError(resp, introspectionStatus);
                return;
            }
            String introspectionData = introspectionStatus.getData();
            if (introspectionData == null) {
                log.error("Invalid token data is received.");
                HandlerUtil.handleError(resp, introspectionStatus);
                return;
            }
            JsonParser jsonParser = new JsonParser();
            JsonElement jTokenResult = jsonParser.parse(introspectionData);
            if (jTokenResult.isJsonObject()) {
                JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
                if (!jTokenResultAsJsonObject.get("active").getAsBoolean()) {
                    HandlerUtil.sendUnAuthorizeResponse(resp);
                    return;
                }
                ProxyResponse proxyResponse = new ProxyResponse();
                proxyResponse.setCode(HttpStatus.SC_OK);
                proxyResponse.setData(
                        jTokenResultAsJsonObject.get("username").getAsString().replaceAll("@carbon.super", ""));
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        }
    }
}
