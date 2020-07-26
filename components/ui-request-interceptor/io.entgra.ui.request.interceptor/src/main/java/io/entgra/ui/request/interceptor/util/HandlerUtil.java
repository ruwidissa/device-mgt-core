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

package io.entgra.ui.request.interceptor.util;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class HandlerUtil {

    private static final Log log = LogFactory.getLog(HandlerUtil.class);

    /***
     *
     * @param httpRequest - httpMethod e.g:- HttpPost, HttpGet
     * @return response as string
     * @throws IOException IO exception returns if error occurs when executing the httpMethod
     */
    public static ProxyResponse execute(HttpRequestBase httpRequest) throws IOException {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpResponse response = client.execute(httpRequest);
            ProxyResponse proxyResponse = new ProxyResponse();

            if (response == null) {
                log.error("Received null response for http request : " + httpRequest.getMethod() + " " + httpRequest
                        .getURI().toString());
                proxyResponse.setCode(HandlerConstants.INTERNAL_ERROR_CODE);
                proxyResponse.setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(
                        HandlerConstants.INTERNAL_ERROR_CODE));
                return proxyResponse;
            } else {
                int statusCode = response.getStatusLine().getStatusCode();
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }

                    String jsonString = result.toString();
                    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                        proxyResponse.setCode(statusCode);
                        proxyResponse.setData(jsonString);
                        proxyResponse.setExecutorResponse("SUCCESS");
                        return proxyResponse;
                    } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                        if (jsonString.contains("Access token expired") || jsonString
                                .contains("Invalid input. Access token validation failed")) {
                            proxyResponse.setCode(statusCode);
                            proxyResponse.setExecutorResponse(HandlerConstants.TOKEN_IS_EXPIRED);
                            return proxyResponse;
                        } else {
                            log.error(
                                    "Received " + statusCode + " response for http request : " + httpRequest.getMethod()
                                            + " " + httpRequest.getURI().toString() + ". Error message: " + jsonString);
                            proxyResponse.setCode(statusCode);
                            proxyResponse.setData(jsonString);
                            proxyResponse.setExecutorResponse(
                                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                            return proxyResponse;
                        }
                    }
                    log.error("Received " + statusCode +
                            " response for http request : " + httpRequest.getMethod() + " " + httpRequest.getURI()
                            .toString() + ". Error message: " + jsonString);
                    proxyResponse.setCode(statusCode);
                    proxyResponse.setData(jsonString);
                    proxyResponse
                            .setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                    return proxyResponse;
                }
            }
        }
    }

    /***
     *
     * @param statusCode Provide status code, e.g:- 400, 401, 500 etc
     * @return relative status code key for given status code.
     */
    public static String getStatusKey (int statusCode){
        String statusCodeKey;

        switch (statusCode) {
        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            statusCodeKey = "internalServerError";
            break;
        case HttpStatus.SC_BAD_REQUEST:
            statusCodeKey = "badRequest";
            break;
        case HttpStatus.SC_UNAUTHORIZED:
            statusCodeKey = "unauthorized";
            break;
        case HttpStatus.SC_FORBIDDEN:
            statusCodeKey = "forbidden";
            break;
        case HttpStatus.SC_NOT_FOUND:
            statusCodeKey = "notFound";
            break;
        case HttpStatus.SC_METHOD_NOT_ALLOWED:
            statusCodeKey = "methodNotAllowed";
            break;
        case HttpStatus.SC_NOT_ACCEPTABLE:
            statusCodeKey = "notAcceptable";
            break;
        case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
            statusCodeKey = "unsupportedMediaType";
            break;
        default:
            statusCodeKey = "defaultPage";
            break;
        }
        return statusCodeKey;
    }


    /***
     *
     * @param resp {@link HttpServletResponse}
     * Return Error Response.
     */
    public static void handleError(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        Gson gson = new Gson();
        if (proxyResponse == null){
            proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            proxyResponse.setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil
                    .getStatusKey(HandlerConstants.INTERNAL_ERROR_CODE));
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());

        proxyResponse.setExecutorResponse(null);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(proxyResponse));
        }
    }

    /***
     *
     * @param resp {@link HttpServletResponse}
     * Return Success Response.
     */
    public static void handleSuccess(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        if (proxyResponse == null){
            handleError(resp, null);
            return;
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());
        JSONObject response = new JSONObject();
        String responseData = proxyResponse.getData();

        if (!StringUtils.isEmpty(responseData)){
            try {
                JSONObject responseDataJsonObj = new JSONObject(responseData);
                response.put("data", responseDataJsonObj);
            } catch (JSONException e) {
                log.debug("Response data is not valid json string");
                response.put("data", responseData);
            }
        }

        try (PrintWriter writer = resp.getWriter()) {
            writer.write(response.toString());
        }
    }

    /**
     * Get gatway port according to request recieved scheme
     * @param scheme https or https
     * @return {@link String} gateway port
     */
    public static String getGatewayPort(String scheme) {
        String gatewayPort = System.getProperty("iot.gateway.https.port");
        if (HandlerConstants.HTTP_PROTOCOL.equals(scheme)) {
            gatewayPort = System.getProperty("iot.gateway.http.port");
        }
        return gatewayPort;
    }

    /**
     * Retrieve Http client based on hostname verification.
     * @return {@link CloseableHttpClient} http client
     */
    public static CloseableHttpClient getHttpClient() {
        boolean isIgnoreHostnameVerification = Boolean.parseBoolean(System.
                getProperty("org.wso2.ignoreHostnameVerification"));
        if (isIgnoreHostnameVerification) {
            return HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        } else {
            return HttpClients.createDefault();
        }
    }

    /**
     * Send UnAuthorized Response to the user
     *
     * @param resp HttpServletResponse object
     */
    public static void sendUnAuthorizeResponse(HttpServletResponse resp)
            throws IOException {
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
        proxyResponse.setExecutorResponse(
                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
        handleError(resp, proxyResponse);
    }
}
