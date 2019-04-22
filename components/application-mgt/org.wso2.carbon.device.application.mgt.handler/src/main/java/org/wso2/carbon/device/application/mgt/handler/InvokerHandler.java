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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.device.application.mgt.handler.beans.AuthData;
import org.wso2.carbon.device.application.mgt.handler.util.HandlerConstants;


import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.wso2.carbon.device.application.mgt.handler.util.HandlerUtil.execute;
import static org.wso2.carbon.device.application.mgt.handler.util.HandlerUtil.retrieveResponseString;

@MultipartConfig
@WebServlet("/invoke")
public class InvokerHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(LoginHandler.class);
    private static final long serialVersionUID = -6508020875358160165L;
    private static AuthData authData;
    private static String apiEndpoint;
    private static String method;
    private static String serverUrl;


//    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
//        try {
//            if (!validateRequest(req, resp)) {
//                return;
//            }
//
//            HttpRequestBase executor = constructExecutor(req);
//            if (executor == null) {
//                resp.sendError(HTTP_BAD_REQUEST, "Bad Request, method: " + method + " is not supported");
//                return;
//            }
//            String accessToken = authData.getAccessToken();
//            executor.setHeader("Authorization", "Bearer " + accessToken);
//
//            String result = execute(executor, HttpStatus.SC_OK);
//
////            unauthorized
////            if (response == null) {
////                resp.sendError(HTTP_INTERNAL_ERROR, "Empty response retried for the API call.");
////                return;
////            }
//
////            int responseCode = response.getStatusLine().getStatusCode();
////            String result = retrieveResponseString(response);
//
//            if (responseCode == HttpStatus.SC_UNAUTHORIZED && (result.contains("Access token expired") || result
//                    .contains("Invalid input. Access token validation failed"))) {
//                if (!refreshToken(req, resp)) {
//                    return;
//                }
//                response = execute(executor);
//                if (response == null) {
//                    resp.sendError(HTTP_INTERNAL_ERROR, "Empty response retried for the token renewal API call.");
//                    return;
//                }
//                responseCode = response.getStatusLine().getStatusCode();
//                result = retrieveResponseString(response);
//            }
//            if (responseCode != HttpStatus.SC_OK && responseCode != HttpStatus.SC_CREATED) {
//                resp.sendError(responseCode, "Error response retrieved for the API call.");
//                return;
//            }
//            try (PrintWriter writer = resp.getWriter()) {
//                writer.write(result);
//            }
//        } catch (IOException e) {
//            log.error("Error occured when processing invoke call.", e);
//        }
//    }
//
//    /***
//     *
//     * @param req {@link HttpServletRequest}
//     * @return {@link HttpRequestBase} if method equals to either GET, POST, PUT or DELETE otherwise returns NULL.
//     */
//    private HttpRequestBase constructExecutor(HttpServletRequest req) {
//        String payload = req.getParameter("payload");
//        String contentType = req.getParameter("content-type");
//        if (contentType == null || contentType.isEmpty()) {
//            contentType = ContentType.APPLICATION_JSON.toString();
//        }
//
//        HttpRequestBase executor;
//        if (HttpGet.METHOD_NAME.equalsIgnoreCase(method)) {
//            executor = new HttpGet(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
//        } else if (HttpPost.METHOD_NAME.equalsIgnoreCase(method)) {
//            executor = new HttpPost(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
//            StringEntity payloadEntity = new StringEntity(payload, ContentType.create(contentType));
//            ((HttpPost) executor).setEntity(payloadEntity);
//        } else if (HttpPut.METHOD_NAME.equalsIgnoreCase(method)) {
//            executor = new HttpPut(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
//            StringEntity payloadEntity = new StringEntity(payload, ContentType.create(contentType));
//            ((HttpPut) executor).setEntity(payloadEntity);
//        } else if (HttpDelete.METHOD_NAME.equalsIgnoreCase(method)) {
//            executor = new HttpDelete(serverUrl + HandlerConstants.API_COMMON_CONTEXT + apiEndpoint);
//        } else {
//            return null;
//        }
//        return executor;
//    }
//
//    /***
//     *
//     * @param req {@link HttpServletRequest}
//     * @param resp {@link HttpServletResponse}
//     * @return If request is a valid one, returns TRUE, otherwise return FALSE
//     * @throws IOException If and error occurs while witting error response to client side
//     */
//    private static boolean validateRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        HttpSession session = req.getSession(false);
//        if (session == null) {
//            resp.sendError(HTTP_UNAUTHORIZED, "Unauthorized, You are not logged in. Please log in to the portal");
//            return false;
//        }
//        authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
//        if (authData == null) {
//            resp.sendError(HTTP_UNAUTHORIZED, "Unauthorized, Access token couldn't found in the current session");
//            return false;
//        }
//
//        apiEndpoint = req.getParameter("api-endpoint");
//        method = req.getParameter("method");
//
//        serverUrl = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort();
//        if (apiEndpoint == null || method == null) {
//            resp.sendError(HTTP_BAD_REQUEST, "Bad Request, Either api-endpoint or method is empty");
//            return false;
//        }
//        return true;
//    }
//
//    /***
//     *
//     * @param req {@link HttpServletRequest}
//     * @param resp {@link HttpServletResponse}
//     * @return If successfully renew tokens, returns TRUE otherwise return FALSE
//     * @throws IOException If and error occurs while witting error response to client side or invoke token renewal API
//     */
//    private static boolean refreshToken(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        log.debug("refreshing the token");
//        HttpPost tokenEndpoint = new HttpPost(
//                serverUrl + HandlerConstants.API_COMMON_CONTEXT + HandlerConstants.TOKEN_ENDPOINT);
//        HttpSession session = req.getSession(false);
//        if (session == null) {
//            resp.sendError(HTTP_UNAUTHORIZED, "Session is expired. Please log in to the server.");
//            return false;
//        }
//
//        StringEntity tokenEndpointPayload = new StringEntity(
//                "grant_type=refresh_token&refresh_token=" + authData.getRefreshToken() + "&scope=PRODUCTION",
//                ContentType.APPLICATION_FORM_URLENCODED);
//
//        tokenEndpoint.setEntity(tokenEndpointPayload);
//        String encodedClientApp = authData.getEncodedClientApp();
//        tokenEndpoint.setHeader("Authorization", "Basic " + encodedClientApp);
//        tokenEndpoint.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString());
//
//        HttpResponse response = execute(tokenEndpoint);
//        if (response == null) {
//            resp.sendError(HTTP_INTERNAL_ERROR,
//                    "Internal Server Error, response of the token refresh API call is null.");
//            return false;
//        } else if ((response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)) {
//            resp.sendError(response.getStatusLine().getStatusCode(),
//                    "Error occured while getting new access token by using refresh token.");
//            return false;
//        }
//        String tokenResult = retrieveResponseString(response);
//        JsonParser jsonParser = new JsonParser();
//
//        JsonElement jTokenResult = jsonParser.parse(tokenResult);
//        if (jTokenResult.isJsonObject()) {
//            JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
//            AuthData newAuthData = new AuthData();
//
//            newAuthData.setAccessToken(jTokenResultAsJsonObject.get("access_token").getAsString());
//            newAuthData.setRefreshToken(jTokenResultAsJsonObject.get("refresh_token").getAsString());
//            newAuthData.setScope(jTokenResultAsJsonObject.get("scope").getAsString());
//            newAuthData.setClientId(authData.getClientId());
//            newAuthData.setClientSecret(authData.getClientSecret());
//            newAuthData.setEncodedClientApp(authData.getEncodedClientApp());
//            newAuthData.setUsername(authData.getUsername());
//            authData = newAuthData;
//            session.setAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY, newAuthData);
//            return true;
//        }
//        return false;
//    }
}
