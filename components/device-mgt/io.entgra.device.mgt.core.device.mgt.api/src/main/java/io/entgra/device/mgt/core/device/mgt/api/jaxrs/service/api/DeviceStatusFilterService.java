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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.swagger.annotations.*;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Metadata related REST-API implementation.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Device Status Filter Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceStatusManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/device-status-filters"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "View Device Status Filter",
                        description = "View device status details",
                        key = "dm:devicestatusfilter:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-status-filters/view"}
                ),
                @Scope(
                        name = "Update Device status filter",
                        description = "Updating Device status filter",
                        key = "dm:devicestatusfilter:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-status-filters/update"}
                ),
        }
)
@Api(value = "Device Status Management")
@Path("/device-status-filters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceStatusFilterService {

    @GET
    @Path("/{device-type}")
    @ApiOperation(
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get device status filters",
            notes = "Get device status filters for the tenant of the logged in user",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:devicestatusfilter:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved device status filters.",
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while getting device status filters.",
                            response = ErrorResponse.class)
            })
    Response getDeviceStatusFilters( @ApiParam(
            name = "deviceType",
            value = "The device type.",
            required = true) @PathParam ("device-type") String deviceType);

    @GET
    @Path("/is-enabled")
    @ApiOperation(
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get device status filter",
            notes = "Get device status filter enable or not for the tenant of the logged in user",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:devicestatusfilter:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved device status filter.",
                            response = Response.class,
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while getting device status check.",
                            response = ErrorResponse.class)
            })
    Response getDeviceStatusCheck();

    @PUT
    @Path("/toggle-device-status")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Update Device status check for tenant",
            notes = "Update Device status check for the tenant of the logged in user",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:devicestatusfilter:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated device status check.",
                            response = Response.class,
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while updating device status check.",
                            response = ErrorResponse.class)
            })
    Response updateDeviceStatusCheck(
            @ApiParam(
            name = "Device status check",
            value = "The device status filtering is enable or not.",
            required = true)
            @QueryParam("isEnabled") boolean isEnabled);

    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Update Device status filters for given device type for tenant",
            notes = "Update Device status filters for given device type for the tenant of the logged in user",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:devicestatusfilter:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated device status filters for given device type..",
                            response = Response.class,
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
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while updating device status filters for given device type.",
                            response = ErrorResponse.class)
            })
    Response updateDeviceStatusFilters(
            @ApiParam(
                    name = "deviceType",
                    value = "The device type for which you want to update device status filters.",
                    required = true)
            @QueryParam("deviceType") String deviceType,
            @ApiParam(
                    name = "deviceStatus",
                    value = "A list of device status values to update for the given device type.",
                    required = true)
            @QueryParam("deviceStatus") List<String> deviceStatus);
}
