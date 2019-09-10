/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.jaxrs.service.api;

import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Adapter;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventStream;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.SiddhiExecutionPlan;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventPublisher;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventReceiver;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "AnalyticsArtifactsManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/analytics/artifacts"),
                        })
                }
        ),
        tags = {
                @Tag(name = "analytics_artifacts_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Create Event Stream Artifact",
                        description = "Create Event Stream Artifact",
                        key = "perm:analytics:artifacts:stream",
                        permissions = {"/device-mgt/analytics/artifacts/stream/add"}
                ),
                @Scope(
                        name = "Create Event Receiver Artifact",
                        description = "Create Event Receiver Artifact",
                        key = "perm:analytics:artifacts:receiver",
                        permissions = {"/device-mgt/analytics/artifacts/receiver/add"}
                ),
                @Scope(
                        name = "Create Event Publisher Artifact",
                        description = "Create Event Publisher Artifact",
                        key = "perm:analytics:artifacts:publisher",
                        permissions = {"/device-mgt/analytics/artifacts/publisher/add"}
                ),
                @Scope(
                        name = "Create Siddhi Script Artifact",
                        description = "Create Siddhi Script Artifact",
                        key = "perm:analytics:artifacts:siddhi",
                        permissions = {"/device-mgt/analytics/artifacts/siddhi-script/add"}
                )
        }
)

@Path("/analytics/artifacts")
@Api(value = "Analytics Artifacts Management", description = "This API corresponds to services" +
                                                             " related to Analytics Artifacts management")
public interface AnalyticsArtifactsManagementService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/stream/{id}")
    @ApiOperation(
            httpMethod = "POST",
            value = "Create Event Stream Artifact as String",
            notes = "Deploy a Json Stream Artifact in Analytics server.",
            tags = "Analytics Artifacts Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:artifacts:stream")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deployed the Stream Artifact.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                          "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                      "Stream Artifact.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventDefinitionAsString(@PathParam("id") String id,
                                           @QueryParam("isEdited") boolean isEdited,
                                           @Valid EventStream stream);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/stream")
    @ApiOperation(
            httpMethod = "POST",
            value = "Create Event Stream Artifact as DTO",
            notes = "Deploy a Json Stream Artifact in Analytics server.",
            tags = "Analytics Artifacts Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:artifacts:stream")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deployed the Stream Artifact.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                          "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                      "Stream Artifact.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventDefinitionAsDto(@Valid EventStream stream);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/receiver/{name}")
    @ApiOperation(
            httpMethod = "POST",
            value = "Create Event Receiver Artifact as String",
            notes = "Deploy a XML Event Receiver Artifact in Analytics server.",
            tags = "Analytics Artifacts Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:artifacts:receiver")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deployed the Receiver Artifact.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                          "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                      "Event Receiver Artifact.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventReceiverAsString(@PathParam("name") String name,
                                         @QueryParam("isEdited") boolean isEdited,
                                         @Valid EventReceiver receiver);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/receiver")
    @ApiOperation(
            httpMethod = "POST",
            value = "Create Event Receiver Artifact as DTO",
            notes = "Deploy a JSON Event Receiver Artifact in Analytics server.",
            tags = "Analytics Artifacts Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:artifacts:receiver")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deployed the Receiver Artifact.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                          "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                      "Event Receiver Artifact.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventReceiverAsDto(@Valid Adapter receiver);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/publisher/{name}")
    @ApiOperation(
            httpMethod = "POST",
            value = "Create Event Publisher Artifact as String",
            notes = "Deploy a XML Event Publisher Artifact in Analytics server.",
            tags = "Analytics Artifacts Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:artifacts:publisher")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deployed the Publisher Artifact.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                          "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                      "Event Publisher Artifact.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventPublisherAsString(@PathParam("name") String name,
                                          @QueryParam("isEdited") boolean isEdited,
                                          @Valid EventPublisher publisher);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/publisher")
    @ApiOperation(
            httpMethod = "POST",
            value = "Create Event Publisher Artifact as DTO",
            notes = "Deploy a JSON Event Publisher Artifact in Analytics server.",
            tags = "Analytics Artifacts Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:artifacts:publisher")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deployed the Publisher Artifact.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                          "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                      "Event Publisher Artifact.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventPublisherAsDto(@Valid Adapter publisher);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/siddhi-script/{name}")
    @ApiOperation(
            httpMethod = "POST",
            value = "Create Siddhi Script Artifact as String",
            notes = "Deploy a SiddhiQL Siddhi script Artifact in Analytics server.",
            tags = "Analytics Artifacts Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:artifacts:siddhi")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deployed the Siddhi script Artifact.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                          "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while " +
                                      "deploying the Siddhi script Artifact.",
                            response = ErrorResponse.class)
            }
    )
    Response deploySiddhiExecutableScript(
            @PathParam("name") String name, @QueryParam("isEdited") boolean isEdited,
            @Valid SiddhiExecutionPlan plan);
}
