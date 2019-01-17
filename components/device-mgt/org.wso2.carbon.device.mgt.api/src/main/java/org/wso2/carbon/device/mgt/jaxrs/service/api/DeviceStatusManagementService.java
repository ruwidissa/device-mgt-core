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
package org.wso2.carbon.device.mgt.jaxrs.service.api;

import io.swagger.annotations.*;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.DeviceCount;
import org.wso2.carbon.device.mgt.common.DeviceIDList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Device related REST-API. This can be used to manipulated device related details.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceStatusManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/device-status"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_status_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Getting Details of Registered Devices",
                        description = "Getting Details of Registered Devices",
                        key = "perm:devices:view",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Details of a Device",
                        description = "Getting Details of a Device",
                        key = "perm:devices:details",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Update the device specified by device id",
                        description = "Update the device specified by device id",
                        key = "perm:devices:update",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Delete the device specified by device id",
                        description = "Delete the device specified by device id",
                        key = "perm:devices:delete",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Feature Details of a Device",
                        description = "Getting Feature Details of a Device",
                        key = "perm:devices:features",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Advanced Search for Devices",
                        description = "Advanced Search for Devices",
                        key = "perm:devices:search",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Installed Application Details of a Device",
                        description = "Getting Installed Application Details of a Device",
                        key = "perm:devices:applications",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Device Operation Details",
                        description = "Getting Device Operation Details",
                        key = "perm:devices:operations",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Get the details of the policy that is enforced on a device.",
                        description = "Get the details of the policy that is enforced on a device.",
                        key = "perm:devices:effective-policy",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Policy Compliance Details of a Device",
                        description = "Getting Policy Compliance Details of a Device",
                        key = "perm:devices:compliance-data",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Change device status.",
                        description = "Change device status.",
                        key = "perm:devices:change-status",
                        permissions = {"/device-mgt/devices/change-status"}
                ),
        }
)
@Path("/device-status")
@Api(value = "Device Status Management", description = "This API carries all device status management related operations " +
        "such as get all the available devices, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DeviceStatusManagementService {

    @GET
    @Path("/count/{type}/{status}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Device",
            notes = "Get the details of a device by specifying the device type and device identifier and optionally " +
                    "the owner.",
            tags = "Device Status Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:details")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the details of the device.",
                            response = DeviceCount.class,
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
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest version" +
                                    " of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n A device with the specified device type and id was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response getDeviceCountByStatus(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "status",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("status")
            @Size(max = 45)
                    String status,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);


    @GET
    @Path("/ids/{type}/{status}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Device",
            notes = "Get the details of a device by specifying the device type and device identifier and optionally " +
                    "the owner.",
            tags = "Device Status Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:details")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the details of the device.",
                            response = DeviceIDList.class,
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
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest version" +
                                    " of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n A device with the specified device type and id was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response getDeviceIdentifiersByStatus(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "status",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("status")
            @Size(max = 45)
                    String status,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);


    @PUT
    @Path("/update/{type}/{status}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Getting the Device Enrollment Status",
            notes = "Get the device enrollment status and the device details of the device.",
            tags = "Device Status Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully created a device instance.",
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
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest version" +
                                    " of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n A deviceType with the specified device type was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response bulkUpdateDeviceStatus(@ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
                                    @PathParam("type") String type,
                                    @ApiParam(name = "status", value = "The device type, such as ios, android or windows.", required = true)
                                    @PathParam("status") String status,
                                    @ApiParam(
                                            name = "statusList",
                                            value = "The payload containing the new name of the device.",
                                            required = true)
                                            List<String> deviceList);


}
