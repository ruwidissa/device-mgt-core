/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.jaxrs.service.api;

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
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
                                @ExtensionProperty(name = "name", value = "DeviceReportnManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/reports"),
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
                        name = "Getting Details of Registered Devices",
                        description = "Getting Details of Registered Devices",
                        key = "perm:devices:view",
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
        }
)

@Api(value = "Device Report Management", description = "Device report related operations can be found here.")
@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ReportManagementService {

    @GET
    @Path("/devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices",
            notes = "Provides details of all the devices enrolled with Entgra IoT Server.",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the list of devices.",
                            response = DeviceList.class,
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
                            code = 400,
                            message = "Bad Request. \n Invalid device status type received. \n" +
                                    "Valid status types are NEW | CHECKED",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n There are no devices.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching the device list.",
                            response = ErrorResponse.class)
            })
    Response getDevicesByDuration(
            @ApiParam(
                    name = "status",
                    value = "Provide the device status details, such as active or inactive.")
            @QueryParam("status") List<String> status,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n" +
                            "- BYOD: Bring Your Own Device\n" +
                            "- COPE: Corporate-Owned, Personally-Enabled")
            @QueryParam("ownership") String ownership,
            @ApiParam(
                    name = "fromDate",
                    value = "Start date of the duration",
                    required = true)
            @QueryParam("from") String fromDate,
            @ApiParam(
                    name = "toDate",
                    value = "end date of the duration",
                    required = true)
            @QueryParam("to") String toDate,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit) throws ReportManagementException;


    @GET
    @Path("/count")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices",
            notes = "Provides details of all the devices enrolled with Entgra IoT Server.",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the list of devices.",
                            response = DeviceList.class,
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
                            code = 400,
                            message = "Bad Request. \n Invalid device status type received. \n" +
                                    "Valid status types are NEW | CHECKED",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n There are no devices.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching the device list.",
                            response = ErrorResponse.class)
            })
    Response getDevicesByDurationCount(
            @ApiParam(
                    name = "status",
                    value = "Provide the device status details, such as active or inactive.")
            @QueryParam("status") List<String> status,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n" +
                            "- BYOD: Bring Your Own Device\n" +
                            "- COPE: Corporate-Owned, Personally-Enabled")
            @QueryParam("ownership") String ownership,
            @ApiParam(
                    name = "fromDate",
                    value = "Start date of the duration",
                    required = true)
            @QueryParam("from") String fromDate,
            @ApiParam(
                    name = "toDate",
                    value = "end date of the duration",
                    required = true)
            @QueryParam("to") String toDate) throws ReportManagementException;


    @GET
    @Path("/devices/count")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices",
            notes = "Provides details of all the devices enrolled with Entgra IoT Server.",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the list of devices.",
                            response = DeviceList.class,
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
                            code = 400,
                            message = "Bad Request. \n Invalid device status type received. \n" +
                                    "Valid status types are NEW | CHECKED",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching the device list.",
                            response = ErrorResponse.class)
            })
    Response getCountOfDevicesByDuration(
            @ApiParam(
                    name = "status",
                    value = "Provide the device status details, such as active or inactive.")
            @QueryParam("status") List<String> status,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n" +
                            "- BYOD: Bring Your Own Device\n" +
                            "- COPE: Corporate-Owned, Personally-Enabled")
            @QueryParam("ownership") String ownership,
            @ApiParam(
                    name = "fromDate",
                    value = "Start date of the duration",
                    required = true)
            @QueryParam("from") String fromDate,
            @ApiParam(
                    name = "toDate",
                    value = "end date of the duration",
                    required = true)
            @QueryParam("to") String toDate,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.")
            @QueryParam("limit")
                    int limit) throws ReportManagementException;

    @GET
    @Path("/expired-devices/{deviceType}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices filtered by OS version",
            notes = "Provides details of devices that have a OS version older than the provided version.",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the list of devices.",
                            response = DeviceList.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. " +
                                      "\n Contents of the request are invalid",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                      "\n Server error occurred while fetching the devices.",
                            response = ErrorResponse.class)
            })
    Response getExpiredDevicesByOSVersion(
            @ApiParam(
                    name = "deviceType",
                    value = "Name of the device type.",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "osVersion",
                    value = "Minimum OS version which is used to filter the devices.",
                    required = true)
            @QueryParam("osVersion") String osVersion,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the list of filtered devices.",
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Limit of the number of deices that should be returned.",
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);

    @GET
    @Path("/encryption-status")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices filtered by encryption status",
            notes = "Provides details of devices which is in provided encryption status",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the list of devices.",
                            response = DeviceList.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body")}),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                      "\n Server error occurred while fetching the devices.",
                            response = ErrorResponse.class)
            })
    Response getDevicesByEncryptionStatus(
            @ApiParam(
                    name = "isEncrypted",
                    value = "The encryption states which used to filter the devices",
                    required = true)
            @QueryParam("isEncrypted")
                    boolean isEncrypted,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the list of filtered devices.",
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Limit of the number of deices that should be returned.",
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);

    @GET
    @Path("/devices/{device-type}/{package-name}/not-installed")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Application Not Installed Devices",
            notes = "Provides details of all the devices enrolled with Entgra IoT Server.",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the list of devices.",
                            response = DeviceList.class,
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
                            code = 404,
                            message = "Not Found. \n There are no devices.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching the device list.",
                            response = ErrorResponse.class)
            })
    Response getAppNotInstalledDevices(
            @ApiParam(
                    name = "device-type",
                    value = "The device type name, such as ios, android, windows, or fire-alarm.",
                    required = true)
            @PathParam("device-type")
                    String deviceType,
            @ApiParam(
                    name = "package-name",
                    value = "The package name of the app.",
                    required = true)
            @PathParam("package-name")
                    String packageName,
            @ApiParam(
                    name = "app-version",
                    value = "Version of the app")
            @QueryParam("app-version") String version,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);
}
