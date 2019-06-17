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
import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.SM;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;

import static io.entgra.ui.request.interceptor.util.HandlerUtil.execute;

@MultipartConfig
@WebServlet(
        name = "RequestHandlerServlet",
        description = "This servlet intercepts the api requests initiated from the user interface and validate before" +
                      " forwarding to the backend",
        urlPatterns = {
                "/invoke/*"
        }
)
public class InvokerHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginHandler.class);
    private static final long serialVersionUID = -6508020875358160165L;
//    private static final HeaderGroup nonForwardingHeaders = new HeaderGroup();
    private static AuthData authData;
    private static String apiEndpoint;
    private static String serverUrl;
    private static String platform;

//    static {
//        // Initializing hop-by-hop headers to omit them from forwarding to the backend
//        String[] headers = {HttpHeaders.CONNECTION, HttpHeaders.TRANSFER_ENCODING, HttpHeaders.PROXY_AUTHENTICATE,
//                HttpHeaders.PROXY_AUTHORIZATION, HttpHeaders.UPGRADE, HttpHeaders.TE, HttpHeaders.TRAILER,
//                HandlerConstants.KEEP_ALIVE, HandlerConstants.PUBLIC};
//        for (String header : headers) {
//            nonForwardingHeaders.addHeader(new BasicHeader(header, null));
//        }
//    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpPost postRequest = new HttpPost(generateBackendRequestURL(req));
                if (StringUtils.isNotEmpty(req.getHeader(HttpHeaders.CONTENT_LENGTH)) ||
                    StringUtils.isNotEmpty(req.getHeader(HttpHeaders.TRANSFER_ENCODING))) {
                    InputStreamEntity entity = new InputStreamEntity(req.getInputStream(),
                            Long.parseLong(req.getHeader(HttpHeaders.CONTENT_LENGTH)));
                    postRequest.setEntity(entity);
                }
                copyRequestHeaders(req, postRequest);
                postRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = execute(postRequest);

                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, postRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API endpoint.");
                    HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(req, resp, serverUrl, platform, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing POST request.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpGet getRequest = new HttpGet(generateBackendRequestURL(req));
                copyRequestHeaders(req, getRequest);
                getRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = execute(getRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, getRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API endpoint.");
                    HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(req, resp, serverUrl, platform, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing GET request.", e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpPut putRequest = new HttpPut(generateBackendRequestURL(req));
                if ((StringUtils.isNotEmpty(req.getHeader(HttpHeaders.CONTENT_LENGTH)) &&
                     Double.parseDouble(req.getHeader(HttpHeaders.CONTENT_LENGTH)) > 0) ||
                    StringUtils.isNotEmpty(req.getHeader(HttpHeaders.TRANSFER_ENCODING))) {
                    InputStreamEntity entity = new InputStreamEntity(req.getInputStream(),
                            Long.parseLong(req.getHeader(HttpHeaders.CONTENT_LENGTH)));
                    putRequest.setEntity(entity);
                }
                copyRequestHeaders(req, putRequest);
                putRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = execute(putRequest);

                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, putRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API endpoint.");
                    HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(req, resp, serverUrl, platform, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing PUT request.", e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpDelete deleteRequest = new HttpDelete(generateBackendRequestURL(req));
                copyRequestHeaders(req, deleteRequest);
                deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = execute(deleteRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, deleteRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the API endpoint.");
                    HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(req, resp, serverUrl, platform, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing DELETE request.", e);
        }
    }

    private String generateBackendRequestURL(HttpServletRequest req) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(serverUrl).append(HandlerConstants.API_COMMON_CONTEXT).append(apiEndpoint);
        if (StringUtils.isNotEmpty(req.getQueryString())) {
            urlBuilder.append("?").append(req.getQueryString());
        }
        return urlBuilder.toString();
    }

    private void copyRequestHeaders(HttpServletRequest req, HttpRequestBase httpRequest) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) ||
                headerName.equalsIgnoreCase(SM.COOKIE)) {
                continue;
            }
            Enumeration<String> headerValues = req.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                httpRequest.setHeader(headerName, headerValues.nextElement());
            }
        }
    }
    /***
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @return If request is a valid one, returns TRUE, otherwise return FALSE
     * @throws IOException If and error occurs while witting error response to client side
     */
    private static boolean validateRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        serverUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
        apiEndpoint = req.getPathInfo();
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
            log.error("Unauthorized, Access token not found in the current session");
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
            proxyResponse.setExecutorResponse(
                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
            HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
            return false;
        }

        if (apiEndpoint == null || req.getMethod() == null) {
            log.error("Bad Request, Either destination api-endpoint or method is empty");
            ProxyResponse proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_BAD_REQUEST);
            proxyResponse.setExecutorResponse(
                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_BAD_REQUEST));
            HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
            return false;
        }
        return true;
    }

    /**
     * Retry request again after refreshing the access token
     *
     * @param req incoming {@link HttpServletRequest}
     * @param resp resp {@link HttpServletResponse}
     * @param httpRequest subclass of {@link HttpRequestBase} related to the current request.
     * @return {@link ProxyResponse} if successful and <code>null</code> if failed.
     * @throws IOException If an error occurs when try to retry the request.
     */
    private static ProxyResponse retryRequestWithRefreshedToken(HttpServletRequest req, HttpServletResponse resp,
                                                                HttpRequestBase httpRequest) throws IOException {
        if (refreshToken(req, resp)) {
            httpRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
            ProxyResponse proxyResponse = execute(httpRequest);
            if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API after refreshing the token.");
                HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
                return null;
            }
            return proxyResponse;
        }
        return null;
    }

    /***
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @return If successfully renew tokens, returns TRUE otherwise return FALSE
     * @throws IOException If an error occurs while witting error response to client side or invoke token renewal API
     */
    private static boolean refreshToken(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("refreshing the token");
        }
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
        tokenEndpoint.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BASIC +
                                             encodedClientApp);
        tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());

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
