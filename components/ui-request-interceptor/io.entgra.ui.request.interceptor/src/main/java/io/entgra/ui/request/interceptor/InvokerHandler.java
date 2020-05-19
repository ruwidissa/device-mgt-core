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
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.SM;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

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
    private static final Log log = LogFactory.getLog(InvokerHandler.class);
    private static final long serialVersionUID = -6508020875358160165L;
    private static AuthData authData;
    private static String apiEndpoint;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpPost postRequest = new HttpPost(generateBackendRequestURL(req));
                generateRequestEntity(req, postRequest);
                postRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = HandlerUtil.execute(postRequest);

                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, postRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the POST API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (FileUploadException e) {
            log.error("Error occurred when processing Multipart POST request.", e);
        } catch (IOException e) {
            log.error("Error occurred when processing POST request.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpGet getRequest = new HttpGet(generateBackendRequestURL(req));
                copyRequestHeaders(req, getRequest, false);
                getRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = HandlerUtil.execute(getRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, getRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the GET API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing GET request.", e);
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpHead headRequest = new HttpHead(generateBackendRequestURL(req));
                copyRequestHeaders(req, headRequest, false);
                headRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = HandlerUtil.execute(headRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, headRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the HEAD API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing HEAD request.", e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpPut putRequest = new HttpPut(generateBackendRequestURL(req));
                generateRequestEntity(req, putRequest);
                putRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = HandlerUtil.execute(putRequest);

                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, putRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the PUT API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (FileUploadException e) {
            log.error("Error occurred when processing Multipart PUT request.", e);
        } catch (IOException e) {
            log.error("Error occurred when processing PUT request.", e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpDelete deleteRequest = new HttpDelete(generateBackendRequestURL(req));
                copyRequestHeaders(req, deleteRequest, false);
                deleteRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse proxyResponse = HandlerUtil.execute(deleteRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = retryRequestWithRefreshedToken(req, resp, deleteRequest);
                    if (proxyResponse == null) {
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the DELETE API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred when processing DELETE request.", e);
        }
    }

    /**
     * Generate te request entity for POST and PUT requests from the incoming request.
     *
     * @param req incoming {@link HttpServletRequest}.
     * @param proxyRequest proxy request instance.
     * @throws FileUploadException If unable to parse the incoming request for multipart content extraction.
     * @throws IOException If error occurred while generating the request body.
     */
    private void generateRequestEntity(HttpServletRequest req, HttpEntityEnclosingRequestBase proxyRequest)
            throws FileUploadException, IOException {
        if (ServletFileUpload.isMultipartContent(req)) {
            ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
            List<FileItem> fileItemList = servletFileUpload.parseRequest(req);
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (FileItem item: fileItemList) {
                if (!item.isFormField()) {
                    entityBuilder.addPart(item.getFieldName(), new InputStreamBody(item.getInputStream(),
                            ContentType.create(item.getContentType()), item.getName()));
                } else {
                    entityBuilder.addTextBody(item.getFieldName(), item.getString());
                }
            }
            proxyRequest.setEntity(entityBuilder.build());
            copyRequestHeaders(req, proxyRequest, false);
        } else {
            if (StringUtils.isNotEmpty(req.getHeader(HttpHeaders.CONTENT_LENGTH)) ||
                StringUtils.isNotEmpty(req.getHeader(HttpHeaders.TRANSFER_ENCODING))) {
                InputStreamEntity entity = new InputStreamEntity(req.getInputStream(),
                        Long.parseLong(req.getHeader(HttpHeaders.CONTENT_LENGTH)));
                proxyRequest.setEntity(entity);
            }
            copyRequestHeaders(req, proxyRequest, true);
        }
    }

    /**
     * Generates the target URL for the proxy request.
     *
     * @param req incoming {@link HttpServletRequest}
     * @return Target URL
     */
    private String generateBackendRequestURL(HttpServletRequest req) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(apiEndpoint).append(HandlerConstants.API_COMMON_CONTEXT)
                .append(req.getPathInfo().replace(" ", "%20"));
        if (StringUtils.isNotEmpty(req.getQueryString())) {
            urlBuilder.append("?").append(req.getQueryString());
        }
        return urlBuilder.toString();
    }

    /**
     * Copy incoming request headers to the proxy request.
     *
     * @param req incoming {@link HttpServletRequest}
     * @param httpRequest proxy request instance.
     * @param preserveContentType <code>true</code> if content type header needs to be preserved.
     *                            This should be set to <code>false</code> when handling multipart requests as Http
     *                            client will generate the Content-Type header automatically.
     */
    private void copyRequestHeaders(HttpServletRequest req, HttpRequestBase httpRequest, boolean preserveContentType) {
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH) ||
                headerName.equalsIgnoreCase(SM.COOKIE) ||
                (!preserveContentType && headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE))) {
                continue;
            }
            Enumeration<String> headerValues = req.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                httpRequest.setHeader(headerName, headerValues.nextElement());
            }
        }
    }

    /***
     * Validates the incoming request.
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @return If request is a valid one, returns TRUE, otherwise return FALSE
     * @throws IOException If and error occurs while witting error response to client side
     */
    private static boolean validateRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        apiEndpoint = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + System.getProperty("iot.gateway.host")
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(req.getScheme());

        if ("reports".equalsIgnoreCase(req.getHeader("appName"))){
            apiEndpoint = System.getProperty("iot.reporting.webapp.host");
            if (StringUtils.isBlank(apiEndpoint)){
                log.error("Reporting Endpoint is not defined in the iot-server.sh properly.");
                handleError(resp, HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return false;
            }
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            log.error("Unauthorized, You are not logged in. Please log in to the portal");
            handleError(resp, HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        if (authData == null) {
            log.error("Unauthorized, Access token not found in the current session");
            handleError(resp, HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        if (req.getMethod() == null) {
            log.error("Bad Request, Request method is empty");
            handleError(resp, HttpStatus.SC_BAD_REQUEST);
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
    private ProxyResponse retryRequestWithRefreshedToken(HttpServletRequest req, HttpServletResponse resp,
                                                                HttpRequestBase httpRequest) throws IOException {
        if (refreshToken(req, resp)) {
            httpRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
            ProxyResponse proxyResponse = HandlerUtil.execute(httpRequest);
            if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API after refreshing the token.");
                HandlerUtil.handleError(resp, proxyResponse);
                return null;
            }
            return proxyResponse;
        }
        return null;
    }

    /***
     * This method is responsible to get the refresh token
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
                apiEndpoint + HandlerConstants.API_COMMON_CONTEXT + HandlerConstants.TOKEN_ENDPOINT);
        HttpSession session = req.getSession(false);
        if (session == null) {
            log.error("Couldn't find a session, hence it is required to login and proceed.");
            handleError(resp, HttpStatus.SC_UNAUTHORIZED);
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

        ProxyResponse tokenResultResponse = HandlerUtil.execute(tokenEndpoint);
        if (tokenResultResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
            log.error("Error occurred while refreshing access token.");
            HandlerUtil.handleError(resp, tokenResultResponse);
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
        handleError(resp, HttpStatus.SC_INTERNAL_SERVER_ERROR);
        return false;
    }

    /**
     * Handle error requests
     *
     * @param resp {@link HttpServletResponse}
     * @param errorCode HTTP error status code
     * @throws IOException If error occurred when trying to send the error response.
     */
    private static void handleError(HttpServletResponse resp, int errorCode)
            throws IOException {
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(errorCode);
        proxyResponse.setExecutorResponse(
                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(errorCode));
        HandlerUtil.handleError(resp, proxyResponse);
    }
}
