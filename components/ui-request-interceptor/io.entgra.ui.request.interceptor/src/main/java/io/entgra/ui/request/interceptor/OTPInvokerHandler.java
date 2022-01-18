/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(
        name = "OTPRequestHandlerServlet",
        description = "This servlet intercepts the otp-api requests initiated from the user interface and validate "
                + "before forwarding to the backend",
        urlPatterns = {
                "/otp-invoke/*"
        }
)
public class OTPInvokerHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(OTPInvokerHandler.class);
    private static final long serialVersionUID = 3109569827313066220L;
    private static String apiEndpoint;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpPost postRequest = new HttpPost(HandlerUtil.generateBackendRequestURL(req, apiEndpoint));
                HandlerUtil.generateRequestEntity(req, postRequest);
                ProxyResponse proxyResponse = HandlerUtil.execute(postRequest);

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
                HttpGet getRequest = new HttpGet(HandlerUtil.generateBackendRequestURL(req, apiEndpoint));
                HandlerUtil.copyRequestHeaders(req, getRequest, false);
                ProxyResponse proxyResponse = HandlerUtil.execute(getRequest);

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
                HttpHead headRequest = new HttpHead(HandlerUtil.generateBackendRequestURL(req, apiEndpoint));
                HandlerUtil.copyRequestHeaders(req, headRequest, false);
                ProxyResponse proxyResponse = HandlerUtil.execute(headRequest);

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
                HttpPut putRequest = new HttpPut(HandlerUtil.generateBackendRequestURL(req, apiEndpoint));
                HandlerUtil.generateRequestEntity(req, putRequest);
                ProxyResponse proxyResponse = HandlerUtil.execute(putRequest);

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
                HttpDelete deleteRequest = new HttpDelete(HandlerUtil.generateBackendRequestURL(req, apiEndpoint));
                HandlerUtil.copyRequestHeaders(req, deleteRequest, false);
                ProxyResponse proxyResponse = HandlerUtil.execute(deleteRequest);

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
        String schema = req.getScheme();
        apiEndpoint = schema + HandlerConstants.SCHEME_SEPARATOR + System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR)
                + HandlerConstants.COLON + HandlerUtil.getGatewayPort(schema);

        if (StringUtils.isBlank(req.getHeader(HandlerConstants.OTP_HEADER))) {
            log.error("Unauthorized, Please provide OTP token.");
            HandlerUtil.handleError(resp, HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }

}
