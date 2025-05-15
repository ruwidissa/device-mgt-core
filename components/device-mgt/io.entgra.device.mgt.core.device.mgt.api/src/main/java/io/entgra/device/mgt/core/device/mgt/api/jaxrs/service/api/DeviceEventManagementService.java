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
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceEventManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/events"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Add Event Definitions for a device type",
                        description = "Add Event Definitions for a device type",
                        key = "dm:device-type:event:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/event/create"}
                ),
                @Scope(
                        name = "Update Event Definitions for a device type",
                        description = "Update Event Definitions for a device type",
                        key = "dm:device-type:event:modify",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/event/modify"}
                ),
                @Scope(
                        name = "Get Event Definitions of a Device Type",
                        description = "Get Event Definitions of a Device Type",
                        key = "dm:device-type:event:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/event/view"}
                ),
                @Scope(
                        name = "Delete Event Definitions for a device type",
                        description = "Delete Event Definitions for a device type",
                        key = "dm:device-type:event:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/device-type/event/delete"}
                ),
        }
)
@Path("/events")
@Api(value = "Device Event Management")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceEventManagementService {

    @POST
    @Path("/{type}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding Event Definitions",
            notes = "Add event definitions for a device type.",
            tags = "Device Event Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:event:create")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully added the event definitions.",
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
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict.\n The event definitions already exist."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deploying the " +
                                    "event definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response deployDeviceTypeEventDefinitions(
            @ApiParam(name = "type", value = "The device type, such as android, ios, and windows.")
            @PathParam("type") String deviceType,
            @ApiParam(name = "skipPersist", value = "Is it required to persist the data or not")
            @QueryParam("skipPersist") boolean skipPersist,
            @ApiParam(name = "isSharedWithAllTenants", value = "Should artifacts be available to all tenants")
            @QueryParam("isSharedWithAllTenants") boolean isSharedWithAllTenants,
            @ApiParam(name = "deviceTypeEvents", value = "Add the data to complete the  DeviceTypeEvent object.",
                    required = true)
            @Valid List<DeviceTypeEvent> deviceTypeEvent);

    @PUT
    @Path("/{type}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Event Definitions ",
            notes = "Updating event definitions for a device type.",
            tags = "Device Event Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:event:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the event definitions.",
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
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict.\n Device type event definitions not updated due to devices that are already enrolled."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while updating " +
                                    "event definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response updateDeviceTypeEventDefinitions(
            @ApiParam(name = "type", value = "The device type, such as android, ios, and windows.")
            @PathParam("type") String deviceType,
            @ApiParam(name = "skipPersist", value = "Is it required to persist the data or not")
            @QueryParam("skipPersist") boolean skipPersist,
            @ApiParam(name = "isSharedWithAllTenants", value = "Should artifacts be available to all tenants")
            @QueryParam("isSharedWithAllTenants") boolean isSharedWithAllTenants,
            @ApiParam(name = "deviceTypeEvents", value = "Add the data to complete the  DeviceTypeEvent object.",
                    required = true)
            @Valid List<DeviceTypeEvent> deviceTypeEvent);

    @DELETE
    @Path("/{type}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete Event Definitions",
            notes = "Delete event definitions for a device type.",
            tags = "Device Event Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:event:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 204,
                            message = "OK. \n Successfully deleted the event definitions.",
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
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict.\n Device type event definitions not deleted due to devices that are already enrolled."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deleting the " +
                                    "event definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response deleteDeviceTypeEventDefinitions(@ApiParam(name = "type", value = "The device type, such as android, " +
            "ios, and windows.")
                                              @PathParam("type") String deviceType);

//    @GET
//    @Path("/{type}/{deviceId}")
//    @ApiOperation(
//            produces = MediaType.APPLICATION_JSON,
//            httpMethod = "GET",
//            value = "Getting Device Events",
//            notes = "Get the events for the device.",
//            tags = "Device Event Management",
//            extensions = {
//                    @Extension(properties = {
//                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:event:view")
//                    })
//            }
//    )
//    @ApiResponses(
//            value = {
//                    @ApiResponse(
//                            code = 200,
//                            message = "OK. \n Successfully fetched the event definition.",
//                            response = EventRecords.class,
//                            responseHeaders = {
//                                    @ResponseHeader(
//                                            name = "Content-Type",
//                                            description = "The content type of the body"),
//                                    @ResponseHeader(
//                                            name = "ETag",
//                                            description = "Entity Tag of the response resource.\n" +
//                                                    "Used by caches, or in conditional requests."),
//                                    @ResponseHeader(
//                                            name = "Last-Modified",
//                                            description =
//                                                    "Date and time the resource was last modified.\n" +
//                                                            "Used by caches, or in conditional requests."),
//                            }
//                    ),
//                    @ApiResponse(
//                            code = 400,
//                            message =
//                                    "Bad Request. \n"),
//                    @ApiResponse(
//                            code = 406,
//                            message = "Not Acceptable.\n The requested media type is not supported"),
//                    @ApiResponse(
//                            code = 500,
//                            message = "Internal Server Error. \n Server error occurred while fetching the " +
//                                    "list of supported device types.",
//                            response = ErrorResponse.class)
//            }
//    )
//    Response getData(@ApiParam(name = "deviceId", value = "id of the device ", required = false)
//                     @PathParam("deviceId") String deviceId,
//                     @ApiParam(name = "from", value = "unix timestamp to retrieve", required = false)
//                     @QueryParam("from") long from,
//                     @ApiParam(name = "to", value = "unix time to retrieve", required = false)
//                     @QueryParam("to") long to,
//                     @ApiParam(name = "type", value = "name of the device type", required = false)
//                     @PathParam("type")  String deviceType,
//                     @ApiParam(name = "offset", value = "offset of the records that needs to be picked up", required = false)
//                     @QueryParam("offset") int offset,
//                     @ApiParam(name = "limit", value = "limit of the records that needs to be picked up", required = false)
//                     @QueryParam("limit") int limit);

//    @GET
//    @Path("last-known/{type}/{deviceId}")
//    @ApiOperation(
//            produces = MediaType.APPLICATION_JSON,
//            httpMethod = "GET",
//            value = "Getting Last Known Device Events",
//            notes = "Get the last known events for the device.",
//            tags = "Device Event Management",
//            extensions = {
//                    @Extension(properties = {
//                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:event:view")
//                    })
//            }
//    )
//    @ApiResponses(
//            value = {
//                    @ApiResponse(
//                            code = 200,
//                            message = "OK. \n Successfully fetched the event.",
//                            response = EventRecords.class,
//                            responseHeaders = {
//                                    @ResponseHeader(
//                                            name = "Content-Type",
//                                            description = "The content type of the body"),
//                                    @ResponseHeader(
//                                            name = "ETag",
//                                            description = "Entity Tag of the response resource.\n" +
//                                                    "Used by caches, or in conditional requests."),
//                                    @ResponseHeader(
//                                            name = "Last-Modified",
//                                            description =
//                                                    "Date and time the resource was last modified.\n" +
//                                                            "Used by caches, or in conditional requests."),
//                            }
//                    ),
//                    @ApiResponse(
//                            code = 400,
//                            message =
//                                    "Bad Request. \n"),
//                    @ApiResponse(
//                            code = 406,
//                            message = "Not Acceptable.\n The requested media type is not supported"),
//                    @ApiResponse(
//                            code = 500,
//                            message = "Internal Server Error. \n Server error occurred while fetching the " +
//                                    "list of supported device types.",
//                            response = ErrorResponse.class)
//            }
//    )
//    Response getLastKnownData(@ApiParam(name = "deviceId", value = "id of the device ", required = true)
//                              @PathParam("deviceId") String deviceId,
//                              @ApiParam(name = "type", value = "name of the device type", required = true)
//                              @PathParam("type") String deviceType,
//                              @ApiParam(name = "limit", value = "limit of the records that needs to be picked up", required = false)
//                              @QueryParam("limit") int limit);

//    @GET
//    @Path("filter/{type}/{parameter}")
//    @ApiOperation(
//            produces = MediaType.APPLICATION_JSON,
//            httpMethod = "GET",
//            value = "Getting the filtered devices",
//            notes = "Get the list of devices based on the filter parameter",
//            tags = "Device Event Management",
//            extensions = {
//                    @Extension(properties = {
//                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:event:view")
//                    })
//            }
//    )
//    @ApiResponses(
//            value = {
//                    @ApiResponse(
//                            code = 200,
//                            message = "OK. \n Successfully fetched the event.",
//                            response = EventRecords.class,
//                            responseHeaders = {
//                                    @ResponseHeader(
//                                            name = "Content-Type",
//                                            description = "The content type of the body"),
//                                    @ResponseHeader(
//                                            name = "ETag",
//                                            description = "Entity Tag of the response resource.\n" +
//                                                    "Used by caches, or in conditional requests."),
//                                    @ResponseHeader(
//                                            name = "Last-Modified",
//                                            description =
//                                                    "Date and time the resource was last modified.\n" +
//                                                            "Used by caches, or in conditional requests."),
//                            }
//                    ),
//                    @ApiResponse(
//                            code = 400,
//                            message =
//                                    "Bad Request. \n"),
//                    @ApiResponse(
//                            code = 406,
//                            message = "Not Acceptable.\n The requested media type is not supported"),
//                    @ApiResponse(
//                            code = 500,
//                            message = "Internal Server Error. \n Server error occurred while fetching the " +
//                                    "list of supported device types.",
//                            response = ErrorResponse.class)
//            }
//    )
//    Response getFilteredDevices(
//            @ApiParam(name = "type", value = "name of the device type", required = true)
//            @PathParam("type") String deviceType,
//            @ApiParam(name = "type", value = "name of the parameter", required = true)
//            @PathParam("type") String parameter,
//            @ApiParam(name = "limit", value = "minimum value the parameter can have", required = false)
//            @QueryParam("min") double min,
//            @ApiParam(name = "max", value = "max value the parameter can have", required = false)
//            @QueryParam("max") double max
//    );

    @GET
    @Path("/{type}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Event Definitions",
            notes = "Get event definitions for a device type.",
            tags = "Device Event Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:device-type:event:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the event definitions.",
                            response = DeviceTypeEvent.class,
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
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "event definitions for a device type.",
                            response = ErrorResponse.class)
            }
    )
    Response getDeviceTypeEventDefinitions(
            @ApiParam(name = "type", value = "The type of the device, such as android, ios, or windows.")
            @PathParam("type") String deviceType);
}
