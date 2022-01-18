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

import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.ui.request.interceptor.util.GrafanaHandlerUtil;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.device.mgt.common.exceptions.GrafanaManagementException;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;
import org.wso2.carbon.device.mgt.core.grafana.mgt.exception.GrafanaEnvVariablesNotDefined;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaUtil;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

@MultipartConfig
@WebServlet(
        name = "GrafanaRequestHandler",
        description = "This servlet intercepts the iframe requests initiated from the user interface and validate before" +
                      " forwarding to the backend",
        urlPatterns = {
                "/grafana/*"
        }
)
public class GrafanaHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(GrafanaHandler.class);
    private static final long serialVersionUID = -6508020875358160165L;
    private static AuthData authData;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpGet grafanaRequest = new HttpGet();
                HandlerUtil.copyRequestHeaders(req, grafanaRequest, true);
                if (!GrafanaUtil.isGrafanaAPI(req.getRequestURI())) {
                    proxyPassGrafanaRequest(grafanaRequest, resp, req);
                    return;
                }
                grafanaRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse grafanaAPIResponse = executeGrafanaAPIRequest(grafanaRequest, req);
                String keyManagerUrl = HandlerUtil.getKeyManagerUrl(req.getScheme());
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(grafanaAPIResponse.getExecutorResponse())) {
                    grafanaAPIResponse = HandlerUtil.retryRequestWithRefreshedToken(req, grafanaRequest, keyManagerUrl);
                    if (!HandlerUtil.isResponseSuccessful(grafanaAPIResponse)) {
                        HandlerUtil.handleError(resp, grafanaAPIResponse);
                        return;
                    }
                }
                if (grafanaAPIResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    if (grafanaAPIResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                        grafanaAPIResponse = HandlerUtil.retryRequestWithRefreshedToken(req, grafanaRequest, keyManagerUrl);
                        if (!HandlerUtil.isResponseSuccessful(grafanaAPIResponse)) {
                            HandlerUtil.handleError(resp, grafanaAPIResponse);
                            return;
                        }
                    } else {
                        log.error("Error occurred while invoking the GET API endpoint.");
                        HandlerUtil.handleError(resp, grafanaAPIResponse);
                        return;
                    }
                }
                handleSuccess(resp, grafanaAPIResponse);
            }
        }  catch (ProcessingException e) {
            String msg = "Grafana server is down or invalid grafana dashboard url provided";
            log.error(msg, e);
        }
        catch (IOException e) {
            log.error("Error occurred when processing Iframe request.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                HttpPost grafanaRequest = new HttpPost();
                HandlerUtil.generateRequestEntity(req, grafanaRequest);
                HandlerUtil.copyRequestHeaders(req, grafanaRequest, true);
                if (!GrafanaUtil.isGrafanaAPI(req.getRequestURI())) {
                    proxyPassGrafanaRequest(grafanaRequest, resp, req);
                    return;
                }
                grafanaRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER + authData.getAccessToken());
                ProxyResponse grafanaAPIResponse = executeGrafanaAPIRequest(grafanaRequest, req);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(grafanaAPIResponse.getExecutorResponse())) {
                    String keyManagerUrl = HandlerUtil.getKeyManagerUrl(req.getScheme());
                    grafanaAPIResponse = HandlerUtil.retryRequestWithRefreshedToken(req, grafanaRequest, keyManagerUrl);
                    if (!HandlerUtil.isResponseSuccessful(grafanaAPIResponse)) {
                        handleError(resp, grafanaAPIResponse);
                        return;
                    }
                }
                if (grafanaAPIResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the POST API endpoint.");
                    handleError(resp, grafanaAPIResponse);
                    return;
                }
                handleSuccess(resp, grafanaAPIResponse);
            }
        } catch (FileUploadException e) {
            log.error("Error occurred when processing Multipart POST request.", e);
        } catch (ProcessingException e) {
            String msg = "Grafana server is down or invalid grafana dashboard url provided";
            log.error(msg, e);
        } catch (IOException e) {
            log.error("Error occurred when processing Iframe request.", e);
        }
    }

    private void handleSuccess(HttpServletResponse resp, ProxyResponse grafanaAPIResponse) throws IOException{
        String contentType = HandlerUtil.getHeaderValue(HttpHeaders.CONTENT_TYPE, grafanaAPIResponse.getHeaders());
        resp.setContentType(contentType);
        resp.setStatus(grafanaAPIResponse.getCode());
        addXFrameOptionsHeaders(resp);
        resp.getWriter().print(grafanaAPIResponse.getData());
    }

    private void handleError(HttpServletResponse resp, int errCode, String errMsg) throws IOException {
        resp.sendError(errCode, errMsg);
    }

    private void handleError(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        resp.sendError(proxyResponse.getCode());
    }

    /***
     * Validates the incoming request.
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @return If request is a valid one, returns TRUE, otherwise return FALSE
     * @throws IOException If and error occurs while witting error response to client side
     */
    private boolean validateRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (req.getMethod() == null) {
            String errMsg = "Bad Request, Request method is empty";
            log.error(errMsg);
            handleError(resp, HttpStatus.SC_BAD_REQUEST, errMsg);
            return false;
        }
        if (HandlerUtil.isPropertyDefined(HandlerConstants.IOT_REPORTING_WEBAPP_HOST_ENV_VAR)) {
            String errMsg = "Reporting Endpoint is not defined in the iot-server.sh properly.";
            log.error(errMsg);
            resp.sendError(500, errMsg);
            return false;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            String errMsg = "Unauthorized, You are not logged in. Please log in to the portal";
            log.error(errMsg);
            handleError(resp, HttpStatus.SC_UNAUTHORIZED, errMsg);
            return false;
        }

        authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        if (authData == null) {
            String errMsg = "Unauthorized, Access token not found in the current session";
            log.error(errMsg);
            handleError(resp, HttpStatus.SC_UNAUTHORIZED, errMsg);
            return false;
        }

        return true;
    }

    private ProxyResponse executeGrafanaAPIRequest(HttpRequestBase requestBase, HttpServletRequest request)
            throws IOException {
        URI grafanaUri = HttpUtil.createURI(generateGrafanaAPIUrl(request));
        requestBase.setURI(grafanaUri);
        return HandlerUtil.execute(requestBase);
    }

    private String generateGrafanaAPIUrl(HttpServletRequest request) {
        String apiBase = generateGrafanaAPIBase(request);
        String grafanaUri = getURIWithQuery(request);
        return apiBase + grafanaUri;
    }

    private String generateGrafanaAPIBase(HttpServletRequest request) {
        return HandlerUtil.getIOTGatewayBase(request) + HandlerConstants.GRAFANA_API;
    }

    private String getURIWithQuery(HttpServletRequest request) {
        String uri = request.getPathInfo();
        if (request.getQueryString() != null) {
            uri +=  HandlerConstants.URI_QUERY_SEPARATOR + request.getQueryString();
        }
        return uri;
    }
    private void proxyPassGrafanaRequest(HttpRequestBase requestBase, HttpServletResponse response,
                                         HttpServletRequest request) throws IOException {
        try (CloseableHttpClient client = HandlerUtil.getHttpClient()) {
            String grafanaUriStr = GrafanaHandlerUtil.generateGrafanaUrl(HttpUtil.createURI(getURIWithQuery(request)),
                    GrafanaUtil.getGrafanaHTTPBase(request.getScheme()));
            URI grafanaURI = HttpUtil.createURI(grafanaUriStr);
            requestBase.setURI(grafanaURI);
            HttpResponse grafanaResponse = invokeGrafanaAPI(client, requestBase);
            forwardGrafanaResponse(grafanaResponse, response);
        } catch (GrafanaEnvVariablesNotDefined e) {
            handleError(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (GrafanaManagementException e) {
            String errMsg = "Error occurred while retrieving grafana user credentials";
            log.error(errMsg, e);
            handleError(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, errMsg);
        }
    }

    private HttpResponse invokeGrafanaAPI(HttpClient client, HttpRequestBase request) throws IOException, GrafanaManagementException {
        setBasicAuthHeader(request);
        return client.execute(request);
    }

    private void setBasicAuthHeader(HttpRequestBase request) throws GrafanaManagementException {
        String basicAuth = GrafanaUtil.getBasicAuthBase64Header();
        request.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
    }

    private void forwardGrafanaResponse(HttpResponse grafanaResponse, HttpServletResponse response) throws IOException {
        InputStream responseContent = grafanaResponse.getEntity().getContent();
        String grafanaContentType = HandlerUtil.getMemeType(grafanaResponse);
        response.setHeader(HttpHeaders.CONTENT_TYPE, grafanaContentType);
        addXFrameOptionsHeaders(response);
        byte[] buffer = new byte[10240];
        try (InputStream input = responseContent; OutputStream output = response.getOutputStream()) {
            for (int length = 0; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
            }
        }

    }

    private void addXFrameOptionsHeaders(HttpServletResponse response) {
        response.setHeader(HandlerConstants.X_FRAME_OPTIONS, HandlerConstants.X_FRAME_OPTIONS_SAMEORIGIN);
    }

}
