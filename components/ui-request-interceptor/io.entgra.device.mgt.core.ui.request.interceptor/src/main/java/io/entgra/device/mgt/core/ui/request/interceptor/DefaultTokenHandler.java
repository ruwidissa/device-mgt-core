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
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.OTPManagementException;
import io.entgra.device.mgt.core.device.mgt.common.otp.mgt.OTPEmailTypes;
import io.entgra.device.mgt.core.device.mgt.common.otp.mgt.dto.OneTimePinDTO;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@MultipartConfig
@WebServlet("/default-credentials")
public class DefaultTokenHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(DefaultTokenHandler.class);


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            HttpSession httpSession = req.getSession(false);

            if (httpSession != null) {
                String userWithDomain = (String) httpSession.getAttribute(HandlerConstants.USERNAME_WITH_DOMAIN);
                String[] userNameParts = userWithDomain.split("@");

                OneTimePinDTO oneTimePinData = new OneTimePinDTO();
                oneTimePinData.setEmail(OTPEmailTypes.REMOTE_SESSION.toString());
                oneTimePinData.setEmailType(OTPEmailTypes.REMOTE_SESSION.toString());
                oneTimePinData.setUsername(userNameParts[0]);
                PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
                try {
                    oneTimePinData.setTenantId(realmService.getTenantManager().getTenantId(userNameParts[1]));
                } catch (UserStoreException e) {
                    throw new RuntimeException(e);
                }
                oneTimePinData.setExpiryTime(DeviceManagementConstants.OTPProperties.OTP_DEFAULT_EXPIRY_SECONDS);
                OTPManagementService otpManagementService = HandlerUtil.getOTPManagementService();
                try {
                    oneTimePinData = otpManagementService.generateOneTimePin(oneTimePinData, true);
                    HandlerUtil.handleSuccess(resp, constructSuccessProxyResponse(oneTimePinData.getOtpToken()));
                } catch (OTPManagementException e) {
                    log.error("Failed while generating remote session OTP for user " + userWithDomain, e);
                    HandlerUtil.handleError(resp, HttpStatus.SC_INTERNAL_SERVER_ERROR);
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
