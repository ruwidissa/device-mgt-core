/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import com.google.gson.JsonObject;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@MultipartConfig
@WebServlet(
        name = "HubspotRequestHandlerServlet",
        description = "",
        urlPatterns = {
                "/hubspot/*"
        }
)
public class HubspotHandler extends HttpServlet {

        private static final Log log = LogFactory.getLog(HubspotHandler.class);
        private HttpSession httpSession;
        private static String hubspotEndpoint;
        private static String chatConfig;
        private JsonObject uiConfigJsonObject;
        private static String gatewayUrl;
        private static String iotsCoreUrl;

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
                try {
                    if (validateRequest(req, resp)) {
                        HttpPost postRequest = new HttpPost(HandlerUtil.generateBackendRequestURL(req, hubspotEndpoint));
                        HandlerUtil.generateChatRequestEntity(req, postRequest);
                        postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                        postRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + chatConfig);
                        ProxyResponse proxyResponse = HandlerUtil.execute(postRequest);
                        HandlerUtil.handleSuccess(resp, proxyResponse);
                    }
                } catch (IOException e) {
                        log.error("Error occurred when processing POST request.", e);
                }
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
                try {
                    if (validateRequest(req, resp)) {
                        HttpGet getRequest = new HttpGet(HandlerUtil.generateBackendRequestURL(req,hubspotEndpoint));
                        getRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                        getRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + chatConfig);
                        ProxyResponse proxyResponse = HandlerUtil.execute(getRequest);
                        HandlerUtil.handleSuccess(resp, proxyResponse);
                    }
                } catch (IOException e) {
                        log.error("Error occurred when processing GET request.", e);
                }
        }

        /***
         * Validates the hubspot's incoming request.
         *
         * @param req {@link HttpServletRequest}
         * @param resp {@link HttpServletResponse}
         * @return If request is a valid one, returns TRUE, otherwise return FALSE
         * @throws IOException If and error occurs while witting error response to client side
         */
        private boolean validateRequest(HttpServletRequest req, HttpServletResponse resp)
                throws IOException {
            httpSession = req.getSession(false);
            if (httpSession == null) {
                log.error("Unauthorized, You are not logged in. Please log in to the portal");
                HandlerUtil.handleError(resp, HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
            if (req.getMethod() == null) {
                log.error("Bad Request, Request method is empty");
                HandlerUtil.handleError(resp, HttpStatus.SC_BAD_REQUEST);
                return false;
            }
            gatewayUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());
            iotsCoreUrl = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_CORE_HOST_ENV_VAR)
                    + HandlerConstants.COLON + HandlerUtil.getCorePort(req.getScheme());
            String uiConfigUrl = iotsCoreUrl + HandlerConstants.UI_CONFIG_ENDPOINT;
            uiConfigJsonObject = HandlerUtil.getUIConfigAndPersistInSession(uiConfigUrl, gatewayUrl, httpSession, resp);
            chatConfig = uiConfigJsonObject.get("hubspotChat").getAsJsonObject().get("accessToken").getAsString();
            hubspotEndpoint = HandlerConstants.HTTPS_PROTOCOL + HandlerConstants.SCHEME_SEPARATOR + HandlerConstants.HUBSPOT_CHAT_URL;
            return true;
        }
}
