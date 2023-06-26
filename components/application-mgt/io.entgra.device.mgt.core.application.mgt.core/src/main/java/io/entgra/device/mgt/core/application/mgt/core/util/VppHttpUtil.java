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

package io.entgra.device.mgt.core.application.mgt.core.util;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.application.mgt.common.dto.ProxyResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class VppHttpUtil {
    private static final Log log = LogFactory.getLog(VppHttpUtil.class);

    public static ProxyResponse execute(String url,
                                  String payload,
                                  String accessToken,
                                  String method) throws IOException {

        HttpRequestBase endpoint = null;
        if (Constants.VPP.GET.equalsIgnoreCase(method) || Constants.VPP.DELETE.equalsIgnoreCase(method)) {
            endpoint = new HttpGet(url);
            addHeaders(endpoint, accessToken);
            return VppHttpUtil.execute(endpoint);
        } else if (Constants.VPP.POST.equalsIgnoreCase(method)) {
            endpoint = new HttpPost(url);
        } else if (Constants.VPP.PUT.equalsIgnoreCase(method)) {
            endpoint = new HttpPut(url);
        }
        addHeaders(endpoint, accessToken);


        if (payload != null) {
            HttpEntity forwardRequestBody = new StringEntity(payload, ContentType.APPLICATION_JSON.toString(), "utf-8");
            if (Constants.VPP.POST.equalsIgnoreCase(method)) {
                ((HttpPost) endpoint).setEntity(forwardRequestBody);
            } else if (Constants.VPP.PUT.equalsIgnoreCase(method)) {
                ((HttpPut) endpoint).setEntity(forwardRequestBody);
            }

        }
        if (log.isDebugEnabled()) {
            log.info("Forwarding request to " + url);
        }
        return VppHttpUtil.execute(endpoint);
    }

    private static void addHeaders(HttpRequestBase endpoint, String accessToken) {
        endpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        endpoint.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());
        endpoint.setHeader(HttpHeaders.AUTHORIZATION, Constants.VPP.BEARER + accessToken);
    }


    /***
     *
     * @param httpRequest - httpMethod e.g:- HttpPost, HttpGet
     * @return response as string
     * @throws IOException IO exception returns if error occurs when executing the httpMethod
     */
    private static ProxyResponse execute(HttpRequestBase httpRequest) throws IOException {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpResponse response = client.execute(httpRequest);
            ProxyResponse proxyResponse = new ProxyResponse();

            if (response == null) {
                log.error("Received null response for http request : " + httpRequest.getMethod() + " " + httpRequest
                        .getURI().toString());
                proxyResponse.setCode(Constants.VPP.INTERNAL_ERROR_CODE);
                proxyResponse.setExecutorResponse(Constants.VPP.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(
                        Constants.VPP.INTERNAL_ERROR_CODE));
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
                            proxyResponse.setExecutorResponse(Constants.VPP.TOKEN_IS_EXPIRED);
                            return proxyResponse;
                        } else {
                            log.error(
                                    "Received " + statusCode + " response for http request : " + httpRequest.getMethod()
                                            + " " + httpRequest.getURI().toString() + ". Error message: " + jsonString);
                            proxyResponse.setCode(statusCode);
                            proxyResponse.setData(jsonString);
                            proxyResponse.setExecutorResponse(
                                    Constants.VPP.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                            return proxyResponse;
                        }
                    }
                    log.error("Received " + statusCode +
                            " response for http request : " + httpRequest.getMethod() + " " + httpRequest.getURI()
                            .toString() + ". Error message: " + jsonString);
                    proxyResponse.setCode(statusCode);
                    proxyResponse.setData(jsonString);
                    proxyResponse
                            .setExecutorResponse(Constants.VPP.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
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
    public static String getStatusKey(int statusCode) {
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
     * Handle error requests.
     *
     * @param resp {@link HttpServletResponse}
     * @param proxyResponse {@link ProxyResponse}
     * @throws IOException If error occurred when trying to send the error response.
     */
    public static void handleError(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        Gson gson = new Gson();
        if (proxyResponse == null) {
            proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            proxyResponse.setExecutorResponse(Constants.VPP.EXECUTOR_EXCEPTION_PREFIX + VppHttpUtil
                    .getStatusKey(Constants.VPP.INTERNAL_ERROR_CODE));
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());

        proxyResponse.setExecutorResponse(null);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(proxyResponse));
        }
    }

    /**
     * Handle error requests with custom error codes.
     *
     * @param resp      {@link HttpServletResponse}
     * @param errorCode HTTP error status code
     * @throws IOException If error occurred when trying to send the error response.
     */
    public static void handleError(HttpServletResponse resp, int errorCode)
            throws IOException {
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(errorCode);
        proxyResponse.setExecutorResponse(
                Constants.VPP.EXECUTOR_EXCEPTION_PREFIX + VppHttpUtil.getStatusKey(errorCode));
        VppHttpUtil.handleError(resp, proxyResponse);
    }

    /***
     *
     * @param resp {@link HttpServletResponse}
     * Return Success Response.
     */
    public static void handleSuccess(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        if (proxyResponse == null) {
            handleError(resp, null);
            return;
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());
        JSONObject response = new JSONObject();
        String responseData = proxyResponse.getData();

        if (!StringUtils.isEmpty(responseData)) {
            try {
                if (responseData.startsWith("{")) {
                    JSONObject responseDataJsonObj = new JSONObject(responseData);
                    response.put("data", responseDataJsonObj);
                } else if (responseData.startsWith("[")) {
                    JSONArray responseDataJsonArr = new JSONArray(responseData);
                    response.put("data", responseDataJsonArr);
                } else {
                    log.warn("Response data is not valid json string >> " + responseData);
                    response.put("data", responseData);
                }
            } catch (JSONException e) {
                log.error("Response data is not passable");
                response.put("data", responseData);
            }
        }

        try (PrintWriter writer = resp.getWriter()) {
            writer.write(response.toString());
        }
    }

    /**
     * Retrieve Http client based on hostname verification.
     *
     * @return {@link CloseableHttpClient} http client
     */
    public static CloseableHttpClient getHttpClient() {
        return HttpClients.custom().setMaxConnTotal(1).setMaxConnPerRoute(1).build();
    }

}
