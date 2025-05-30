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

package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api.impl;

import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api.GrafanaAPIProxyService;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api.bean.ErrorResponse;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api.exception.RefererNotValid;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api.impl.util.GrafanaMgtAPIUtils;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api.impl.util.GrafanaRequestHandlerUtil;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.bean.GrafanaPanelIdentifier;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.config.GrafanaConfiguration;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.config.GrafanaConfigurationManager;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.exception.MaliciousQueryAttempt;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DBConnectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.sql.SQLException;

@Path("/api")
public class GrafanaAPIProxyServiceImpl implements GrafanaAPIProxyService {

    private static final Log log = LogFactory.getLog(GrafanaAPIProxyServiceImpl.class);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/ds/query")
    @Override
    public Response queryDatasource(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        try {
            GrafanaConfiguration configuration = GrafanaConfigurationManager.getInstance().getGrafanaConfiguration();
            GrafanaPanelIdentifier panelIdentifier = GrafanaRequestHandlerUtil.getPanelIdentifier(headers);
            boolean queryValidationConfig = configuration.getValidationConfig().getDSQueryValidation();
            if (queryValidationConfig) {
                GrafanaMgtAPIUtils.getGrafanaQueryService().buildSafeQuery(body, panelIdentifier.getDashboardId(),
                        panelIdentifier.getPanelId(), requestUriInfo.getRequestUri());
            }
            return GrafanaRequestHandlerUtil.proxyPassPostRequest(body, requestUriInfo, panelIdentifier.getOrgId());
        } catch (MaliciousQueryAttempt e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        } catch (GrafanaManagementException e) {
            return GrafanaRequestHandlerUtil.constructInternalServerError(e, e.getMessage());
        } catch (RefererNotValid e) {
            return GrafanaRequestHandlerUtil.constructInvalidReferer();
        } catch (SQLException | IOException | DBConnectionException |
                 io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException e) {
            log.error(e);
            return GrafanaRequestHandlerUtil.constructInternalServerError(e, e.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/frontend-metrics")
    @Override
    public Response frontendMetrics(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassPostRequest(body, headers, requestUriInfo);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/user/auth-tokens/rotate")
    @Override
    public Response rotateAuthToken(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassPostRequest(body, headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dashboards/uid/{uid}")
    @Override
    public Response getDashboard(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/folders/{uid}")
    @Override
    public Response getFolders(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/annotations")
    @Override
    public Response getAnnotations(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/prometheus/grafana/api/v1/rules")
    @Override
    public Response prometheusRuleInfo(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/plugins/grafana-lokiexplore-app/settings")
    @Override
    public Response loadLokiExploreAppSettings(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/plugins/grafana-pyroscope-app/settings")
    @Override
    public Response loadGrafanaPyroscopeSettings(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/plugins/grafana-metricsdrilldown-app/settings")
    @Override
    public Response loadMetricsDrilldownAppSettings(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/plugins/grafana-exploretraces-app/settings")
    @Override
    public Response loadExploreTracesAppSettings(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alerts/states-for-dashboard")
    @Override
    public Response getAlertStateForDashboards(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
        return proxyPassGetRequest(headers, requestUriInfo);
    }

    public Response proxyPassGetRequest(HttpHeaders headers, UriInfo requestUriInfo) {
        try {
            GrafanaPanelIdentifier panelIdentifier = GrafanaRequestHandlerUtil.getPanelIdentifier(headers);
            return GrafanaRequestHandlerUtil.proxyPassGetRequest(requestUriInfo, panelIdentifier.getOrgId());
        } catch (RefererNotValid e) {
            return GrafanaRequestHandlerUtil.constructInvalidReferer();
        } catch (GrafanaManagementException e) {
            return GrafanaRequestHandlerUtil.constructInternalServerError(e, e.getMessage());
        }
    }

    public Response proxyPassPostRequest(JsonObject body, HttpHeaders headers, UriInfo requestUriInfo) {
        try {
            GrafanaPanelIdentifier panelIdentifier = GrafanaRequestHandlerUtil.getPanelIdentifier(headers);
            return GrafanaRequestHandlerUtil.proxyPassPostRequest(body, requestUriInfo, panelIdentifier.getOrgId());
        } catch (RefererNotValid e) {
            return GrafanaRequestHandlerUtil.constructInvalidReferer();
        } catch (GrafanaManagementException e) {
            return GrafanaRequestHandlerUtil.constructInternalServerError(e, e.getMessage());
        }
    }


}
