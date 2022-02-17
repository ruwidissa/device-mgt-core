package io.entgra.analytics.mgt.grafana.proxy.api.service.impl;

import com.google.gson.JsonObject;
import io.entgra.analytics.mgt.grafana.proxy.api.service.GrafanaAPIProxyService;
import io.entgra.analytics.mgt.grafana.proxy.api.service.bean.ErrorResponse;
import io.entgra.analytics.mgt.grafana.proxy.api.service.exception.RefererNotValid;
import io.entgra.analytics.mgt.grafana.proxy.api.service.impl.util.GrafanaRequestHandlerUtil;
import io.entgra.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.analytics.mgt.grafana.proxy.core.bean.GrafanaPanelIdentifier;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.MaliciousQueryAttempt;
import io.entgra.analytics.mgt.grafana.proxy.core.internal.GrafanaMgtDataHolder;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;

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
            GrafanaPanelIdentifier panelIdentifier = GrafanaRequestHandlerUtil.getPanelIdentifier(headers);
            GrafanaMgtDataHolder.getInstance().getGrafanaQueryService().
                    buildSafeQuery(body,  panelIdentifier.getDashboardId(), panelIdentifier.getPanelId(), requestUriInfo.getRequestUri());
            return GrafanaRequestHandlerUtil.proxyPassPostRequest(body, requestUriInfo, panelIdentifier.getOrgId());
        } catch (MaliciousQueryAttempt e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        } catch (GrafanaManagementException e) {
            return GrafanaRequestHandlerUtil.constructInternalServerError(e, e.getMessage());
        } catch (RefererNotValid e) {
            return GrafanaRequestHandlerUtil.constructInvalidReferer();
        } catch (SQLException | IOException | DBConnectionException |
                io.entgra.application.mgt.common.exception.DBConnectionException e) {
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dashboards/uid/{uid}")
    @Override
    public Response getDashboard(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) {
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
