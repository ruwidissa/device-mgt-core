/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.jaxrs.service.impl.util;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.device.mgt.common.exceptions.GrafanaManagementException;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;
import org.wso2.carbon.device.mgt.core.grafana.mgt.bean.GrafanaPanelIdentifier;
import org.wso2.carbon.device.mgt.core.grafana.mgt.exception.GrafanaEnvVariablesNotDefined;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaConstants;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaUtil;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.exception.RefererNotValid;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class GrafanaRequestHandlerUtil {

    private static final Log log = LogFactory.getLog(GrafanaRequestHandlerUtil.class);

    public static Response proxyPassGetRequest(UriInfo requestUriInfo, String orgId) throws GrafanaEnvVariablesNotDefined {
        HttpGet grafanaGetReq = new HttpGet();
        return forwardRequestToGrafanaEndpoint(grafanaGetReq, requestUriInfo, orgId);
    }

    public static Response proxyPassPostRequest(JsonObject body, UriInfo requestUriInfo, String orgId)
            throws GrafanaEnvVariablesNotDefined {
        HttpPost grafanaPostReq = new HttpPost();
        try {
            setRequestEntity(grafanaPostReq, body);
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Error occurred while parsing body";
            log.error(errorMsg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMsg).build()).build();
        }
        return forwardRequestToGrafanaEndpoint(grafanaPostReq, requestUriInfo, orgId);
    }

    private static Response forwardRequestToGrafanaEndpoint(HttpRequestBase requestBase, UriInfo requestUriInfo, String orgId)
            throws GrafanaEnvVariablesNotDefined {
        URI grafanaUri = generateGrafanaUri(requestUriInfo);
        requestBase.setURI(grafanaUri);
        requestBase.setHeader(GrafanaConstants.X_GRAFANA_ORG_ID_HEADER, orgId);
        try(CloseableHttpClient client = HttpClients.createDefault())  {
            HttpResponse grafanaResponse = invokeGrafanaAPI(client, requestBase);
            String grafanaResponseBody = HttpUtil.getResponseString(grafanaResponse);
            return Response.status(Response.Status.OK).entity(grafanaResponseBody).
                    header(HttpHeaders.CONTENT_TYPE, HttpUtil.getContentType(grafanaResponse)).build();
        } catch (IOException e) {
            String msg = "Error occurred while calling Grafana API";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (GrafanaManagementException e) {
            String err = "Error occurred while retrieving Grafana configuration";
            log.error(err, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(err).build()).build();
        }
    }

    public static HttpResponse invokeGrafanaAPI(HttpClient client, HttpRequestBase request) throws IOException, GrafanaManagementException {
        setBasicAuthHeader(request);
        return client.execute(request);
    }

    public static void setBasicAuthHeader(HttpRequestBase request) throws GrafanaManagementException {
        String basicAuth = GrafanaUtil.getBasicAuthBase64Header();
        request.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
    }

    public static  URI generateGrafanaUri(UriInfo requestUriInfo) throws GrafanaEnvVariablesNotDefined {
        String base = GrafanaUtil.getGrafanaHTTPBase(requestUriInfo.getRequestUri().getScheme());
        String grafanaRequestPath = getGrafanaRequestPathWQuery(requestUriInfo);
        return HttpUtil.createURI(GrafanaUtil.generateGrafanaUrl(grafanaRequestPath, base));
    }

    public static  String getGrafanaRequestPathWQuery(UriInfo requestUriInfo) {
        String contextPath = "/reports/grafana";
        String path = requestUriInfo.getPath().substring(contextPath.length());
        String queryParam = requestUriInfo.getRequestUri().getRawQuery();
        if (queryParam != null) {
            path += Constants.URI_QUERY_SEPARATOR + queryParam;
        }
        return path;
    }

    public static GrafanaPanelIdentifier getPanelIdentifier(HttpHeaders headers) throws RefererNotValid {
        String referer = headers.getHeaderString(GrafanaConstants.REFERER_HEADER);
        if(referer == null) {
            String errMsg = "Request does not contain Referer header";
            log.error(errMsg);
            throw new RefererNotValid(errMsg);
        }
        GrafanaPanelIdentifier panelIdentifier = GrafanaUtil.getPanelIdentifierFromReferer(referer);
        if(panelIdentifier.getDashboardId() == null ||
                panelIdentifier.getPanelId() == null || panelIdentifier.getOrgId() == null) {
            String errMsg = "Referer must contain dashboardId, panelId and orgId";
            log.error(errMsg);
            throw new RefererNotValid(errMsg);
        }
        return panelIdentifier;
    }

    public static  void setRequestEntity(HttpPost postRequest, JsonObject body) throws UnsupportedEncodingException {
        StringEntity bodyEntity = new StringEntity(body.toString());
        bodyEntity.setContentType(MediaType.APPLICATION_JSON);
        postRequest.setEntity(bodyEntity);
    }

    public static Response constructInvalidReferer() {
        String errorMsg = "Request does not contain a valid Referer header";
        return Response.status(Response.Status.BAD_REQUEST).entity(
                new ErrorResponse.ErrorResponseBuilder().setMessage(errorMsg).build()).build();
    }
    public static Response constructInternalServerError(Exception e, String errMsg) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                new ErrorResponse.ErrorResponseBuilder().setMessage(errMsg).build()).build();
    }

    public static void copyHeadersToGrafanaRequest(HttpRequestBase grafanaRequest, HttpHeaders headers) {
        Map<String, List<String>> headerValues = headers.getRequestHeaders();
        for (String key : headerValues.keySet()) {
            if (!key.equals(HttpHeaders.AUTHORIZATION) && !key.equals(HttpHeaders.CONTENT_LENGTH)) {
                for (String value : headerValues.get(key)) {
                    grafanaRequest.setHeader(key, value);
                }
            }
        }
    }
}
