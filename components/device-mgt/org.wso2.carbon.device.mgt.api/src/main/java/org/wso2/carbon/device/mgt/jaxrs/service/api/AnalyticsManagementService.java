/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.api;

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
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.Adapter;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.EventStream;
import org.wso2.carbon.device.mgt.jaxrs.beans.analytics.SiddhiExecutionPlan;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "AnalyticsManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/analytics"),
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
                        name = "Add Event stream",
                        description = "Add definition of a Event stream",
                        key = "perm:analytics:events",
                        permissions = {"/device-mgt/analytics/event/add"}
                ),
                @Scope(
                        name = "Add Publisher",
                        description = "Add definition of a Publisher",
                        key = "perm:analytics:publisher",
                        permissions = {"/device-mgt/analytics/publisher/add"}
                ),
                @Scope(
                        name = "Add Receiver",
                        description = "Add definition of a Receiver",
                        key = "perm:analytics:receiver",
                        permissions = {"/device-mgt/analytics/receiver/add"}
                ),
                @Scope(
                        name = "Add Siddhi script",
                        description = "Add definition of a Siddhi script",
                        key = "perm:analytics:script",
                        permissions = {"/device-mgt/analytics/script/add"}
                )
        }
)

@Path("/analytics")
@Api(value = "Analytics Management", description = "This API corresponds to all tasks related analytics server")
@Consumes(MediaType.APPLICATION_JSON)
public interface AnalyticsManagementService {

    @POST
    @Path("/event")
    @ApiOperation(
            httpMethod = "POST",
            value = "Adding the Event Type Definition",
            notes = "Add the event definition to analytics.",
            tags = "Analytics Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:events")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added the event defintion.",
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
                            message =
                                    "Bad Request."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while publishing the " +
                                      "Event stream.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventDefinition(@Valid EventStream stream);

    @POST
    @Path("/receiver")
    @ApiOperation(
            httpMethod = "POST",
            value = "Adding an Event Receiver",
            notes = "Add the receiver for an event",
            tags = "Analytics Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:receiver")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added the receiver.",
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
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while publishing the " +
                                      "Event receiver.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventReceiver(Adapter receiver);

    @POST
    @Path("/publisher")
    @ApiOperation(
            httpMethod = "POST",
            value = "Adding an Event Publisher",
            notes = "Add the publisher for an event",
            tags = "Analytics Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:publisher")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added the publisher.",
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
                            message = "Internal Server Error. \n Server error occurred while publishing the " +
                                      "Event publisher.",
                            response = ErrorResponse.class)
            }
    )
    Response deployEventPublisher(@Valid Adapter publisher);

    @POST
    @Path("/executable")
    @ApiOperation(
            httpMethod = "POST",
            value = "Adding a Siddhi executable",
            notes = "Add a Siddhi executable for a event",
            tags = "Analytics Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:analytics:script")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added the execution plan.",
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
                                      "publishing the Execution plan.",
                            response = ErrorResponse.class)
            }
    )
    Response deploySiddhiExecutableScript(@Valid SiddhiExecutionPlan plan);
}
