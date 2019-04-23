/* Copyright (c) 2018, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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


package org.wso2.carbon.device.application.mgt.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;
import org.wso2.carbon.device.application.mgt.handler.beans.AuthData;
import org.wso2.carbon.device.application.mgt.handler.util.HandlerConstants;
import org.wso2.carbon.device.application.mgt.handler.util.HandlerUtil;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static org.wso2.carbon.device.application.mgt.handler.util.HandlerUtil.execute;

@MultipartConfig
@WebServlet("/invoke")
public class InvokerHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginHandler.class);
    private static final long serialVersionUID = -6508020875358160165L;
    private static AuthData authData;
    private static String apiEndpoint;
    private static String method;
    private static String serverUrl;
    private static String platform;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (!validateRequest(req, resp)) {
                return;
            }
            HttpRequestBase executor = constructExecutor(req);
            if (executor == null) {
                resp.sendError(HTTP_BAD_REQUEST, "Bad Request, method: " + method + " is not supported");
                return;
            }
            executor.setHeader(HandlerConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + authData.getAccessToken());
            ProxyResponse proxyResponse = execute(executor);

            if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                if (!refreshToken(req, resp)) {
                    return;
                }
                executor.setHeader(HandlerConstants.AUTHORIZATION_HEADER_KEY, "Bearer " + authData.getAccessToken());
                proxyResponse = execute(executor);
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API after refreshing the token.");
                    HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
                    return;
                }
            }
            if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API endpoint.");
                HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
                return;
            }
            HandlerUtil.handleSuccess(req, resp, serverUrl, platform, proxyResponse);
        } catch (IOException e) {
            log.error("Error occured when processing invoke call.", e);
        }
    }

    /***
     *
     * @param req {@link HttpServletRequest}
     * @return {@link HttpRequestBase} if method equals to either GET, POST, PUT or DELETE otherwise returns NULL.
     */
    private HttpRequestBase constructExecutor(HttpServletRequest req) {
        String payload = req.getParameter("payload");
        String contentType = req.getParameter("content-type");
        if (contentType == null || contentType.isEmpty()) {
            contentType = ContentType.APPLICATION_JSON.toString();
        }

        HttpRequestBase executor;
        if (HttpGet.METHOD_NAME.equalsIgnoreCase(method)) {
            executor = new HttpGet(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
        } else if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
            executor = new HttpPost(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
            StringEntity payloadEntity = new StringEntity(payload, ContentType.create(contentType));
            ((HttpPost) executor).setEntity(payloadEntity);
        } else if (HttpPut.METHOD_NAME.equalsIgnoreCase(method)) {
            executor = new HttpPut(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
            StringEntity payloadEntity = new StringEntity(payload, ContentType.create(contentType));
            ((HttpPut) executor).setEntity(payloadEntity);
        } else if (HttpDelete.METHOD_NAME.equalsIgnoreCase(method)) {
            executor = new HttpDelete(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
        } else {
            return null;
        }
        return executor;
    }

    /***
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @return If request is a valid one, returns TRUE, otherwise return FALSE
     * @throws IOException If and error occurs while witting error response to client side
     */
    private static boolean validateRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        serverUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        apiEndpoint = req.getParameter("api-endpoint");
        method = req.getParameter("method");
        HttpSession session = req.getSession(false);
        if (session == null) {
            log.error("Unauthorized, You are not logged in. Please log in to the portal");
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
            proxyResponse.setExecutorResponse(
                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
            HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
            return false;
        }
        authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        platform = (String) session.getAttribute(HandlerConstants.PLATFORM);
        if (authData == null) {
            log.error("Unauthorized, Access token couldn't found in the current session");
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
            proxyResponse.setExecutorResponse(
                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
            HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
            return false;
        }

        if (apiEndpoint == null || method == null) {
            log.error("Bad Request, Either api-endpoint or method is empty");
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_BAD_REQUEST);
            proxyResponse.setExecutorResponse(
                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_BAD_REQUEST));
            HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
            return false;
        }
        return true;
    }

    /***
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @return If successfully renew tokens, returns TRUE otherwise return FALSE
     * @throws IOException If and error occurs while witting error response to client side or invoke token renewal API
     */
    private static boolean refreshToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.debug("refreshing the token");
        HttpPost tokenEndpoint = new HttpPost(
                serverUrl + HandlerConstants.API_COMMON_CONTEXT + HandlerConstants.TOKEN_ENDPOINT);
        HttpSession session = req.getSession(false);
        if (session == null) {
            log.error("Couldn't find a session, hence it is required to login and proceed.");
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
            proxyResponse.setExecutorResponse(
                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
            HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
            return false;
        }

        StringEntity tokenEndpointPayload = new StringEntity(
                "grant_type=refresh_token&refresh_token=" + authData.getRefreshToken() + "&scope=PRODUCTION",
                ContentType.APPLICATION_FORM_URLENCODED);

        tokenEndpoint.setEntity(tokenEndpointPayload);
        String encodedClientApp = authData.getEncodedClientApp();
        tokenEndpoint.setHeader("Authorization", "Basic " + encodedClientApp);
        tokenEndpoint.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());

        ProxyResponse tokenResultResponse = execute(tokenEndpoint);
        if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
            log.error("Error occurred while refreshing access token.");
            HandlerUtil.handleError(req, resp, serverUrl, platform, tokenResultResponse);
            return false;
        }

        JsonParser jsonParser = new JsonParser();
        JsonElement jTokenResult = jsonParser.parse(tokenResultResponse.getData());

        if (jTokenResult.isJsonObject()) {
            JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
            AuthData newAuthData = new AuthData();

            newAuthData.setAccessToken(jTokenResultAsJsonObject.get("access_token").getAsString());
            newAuthData.setRefreshToken(jTokenResultAsJsonObject.get("refresh_token").getAsString());
            newAuthData.setScope(jTokenResultAsJsonObject.get("scope").getAsString());
            newAuthData.setClientId(authData.getClientId());
            newAuthData.setClientSecret(authData.getClientSecret());
            newAuthData.setEncodedClientApp(authData.getEncodedClientApp());
            newAuthData.setUsername(authData.getUsername());
            authData = newAuthData;
            session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, newAuthData);
            return true;
        }

        log.error("Error Occurred in token renewal process.");
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        proxyResponse.setExecutorResponse(
                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
        return false;
    }
}
