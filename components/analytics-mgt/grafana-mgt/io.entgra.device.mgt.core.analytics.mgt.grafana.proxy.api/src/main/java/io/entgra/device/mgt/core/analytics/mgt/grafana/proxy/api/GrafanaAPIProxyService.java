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
package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.api;

import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Grafana API Proxy Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "GrafanaAPIManagement"),
                                @ExtensionProperty(name = "context", value = "/api/grafana-mgt/v1.0/api"),
                        })
                }
        ),
        tags = {
                @Tag(name = "analytics_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Using Grafana APIs required for Grafana iframes",
                        description = "Grafana API proxy to validate requests.",
                        key = "grafana:api:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/grafana/view"}
                )
        }
)

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.MEDIA_TYPE_WILDCARD)
@Api(value = "Grafana API Management", description = "Grafana api related operations can be found here.")
public interface GrafanaAPIProxyService {

    String SCOPE = "scope";

    @POST
    @Path("/ds/query")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Grafana query API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response queryDatasource(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/frontend-metrics")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Grafana frontend-metric API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response frontendMetrics(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/user/auth-tokens/rotate")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Rotate authentication tokens",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response rotateAuthToken(JsonObject body, @Context HttpHeaders headers, @Context UriInfo requestUriInfo);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dashboards/uid/{uid}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Grafana dashboard details API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response getDashboard(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/folders/{uid}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Grafana dashboard folder information",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response getFolders(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/annotations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Grafana annotations API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response getAnnotations(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/prometheus/grafana/api/v1/rules")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Accessing Grafana Prometheus rule information",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response prometheusRuleInfo(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/alerts/states-for-dashboard")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Grafana alert states for dashboard details API proxy",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response getAlertStateForDashboards(@Context HttpHeaders headers, @Context UriInfo requestUriInfo) throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/plugins/grafana-lokiexplore-app/settings")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve Grafana Loki Explore App settings",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response loadLokiExploreAppSettings(@Context HttpHeaders headers, @Context UriInfo requestUriInfo)
            throws ClassNotFoundException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/plugins/grafana-pyroscope-app/settings")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve Grafana Pyroscope App settings",
            tags = "Analytics",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "grafana:api:view")
                    })
            }
    )
    Response loadGrafanaPyroscopeSettings(@Context HttpHeaders headers, @Context UriInfo requestUriInfo)
            throws ClassNotFoundException;
}
