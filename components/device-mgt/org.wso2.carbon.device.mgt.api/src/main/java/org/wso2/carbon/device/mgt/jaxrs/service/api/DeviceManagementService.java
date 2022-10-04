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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.search.PropertyMap;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationList;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationRequest;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationStatusBean;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Timestamp;
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
                                @ExtensionProperty(name = "name", value = "DeviceManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/devices"),
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
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Details of a Device",
                        description = "Getting Details of a Device",
                        key = "perm:devices:details",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Update the device specified by device id",
                        description = "Update the device specified by device id",
                        key = "perm:devices:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Delete the device specified by device id",
                        description = "Delete the device specified by device id",
                        key = "perm:devices:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Feature Details of a Device",
                        description = "Getting Feature Details of a Device",
                        key = "perm:devices:features",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Advanced Search for Devices",
                        description = "Advanced Search for Devices",
                        key = "perm:devices:search",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Installed Application Details of a Device",
                        description = "Getting Installed Application Details of a Device",
                        key = "perm:devices:applications",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Device Operation Details",
                        description = "Getting Device Operation Details",
                        key = "perm:devices:operations",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Get the details of the policy that is enforced on a device.",
                        description = "Get the details of the policy that is enforced on a device.",
                        key = "perm:devices:effective-policy",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting Policy Compliance Details of a Device",
                        description = "Getting Policy Compliance Details of a Device",
                        key = "perm:devices:compliance-data",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Change device status.",
                        description = "Change device status.",
                        key = "perm:devices:change-status",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/change-status"}
                ),
                @Scope(
                        name = "Enroll Device",
                        description = "Register a device",
                        key = "perm:device:enroll",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/add"}
                ),
        }
)
@Path("/devices")
@Api(value = "Device Management", description = "This API carries all device management related operations " +
        "such as get all the available devices, etc.")
public interface DeviceManagementService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices",
            notes = "Provides details of all the devices enrolled with WSO2 IoT Server.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of devices.",
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 400,
                    message = "The incoming request has more than one selection criteria defined via the query parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "The search criteria did not match any device registered with the server.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response getDevices(
            @ApiParam(
                    name = "name",
                    value = "The device name. For example, Nexus devices can have names, suhc as shamu, bullhead or angler.",
                    required = false)
            @Size(max = 45)
                    String name,
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android, or windows.",
                    required = false)
            @QueryParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "user",
                    value = "The username of the owner of the device.",
                    required = false)
            @QueryParam("user")
                    String user,
            @ApiParam(
                    name = "userPattern",
                    value = "Define a few letters of the username in the order they appear. For example, you want to get the devices that belong to the admin, you can give \\\"ad\\\" or \\\"min\\\" here and you will get the devices that are enrolled under a user who has the given combination as part of the username .",
                    required = false)
            @QueryParam("userPattern")
                    String userPattern,
            @ApiParam(
                    name = "role",
                    value = "A role of device owners. Ex : store-admin",
                    required = false)
            @QueryParam("role")
            @Size(max = 45)
                    String role,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n" +
                            "- BYOD: Bring Your Own Device\n" +
                            "- COPE: Corporate-Owned, Personally-Enabled",
                    required = false)
            @QueryParam("ownership")
            @Size(max = 45)
                    String ownership,
            @ApiParam(
                    name = "serialNumber",
                    value = "The serial number of the device.",
                    required = false)
            @QueryParam("serialNumber")
                    String serialNumber,
            @ApiParam(
                    name = "status",
                    value = "Provide the device status details, such as active or inactive.",
                    required = false)
            @QueryParam("status")
                    List<String> status,
            @ApiParam(
                    name = "groupId",
                    value = "Id of the group which device belongs",
                    required = false)
            @QueryParam("groupId")
                    int groupId,
            @ApiParam(
                    name = "since",
                    value = "Checks if the requested variant was created since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @QueryParam("since")
                    String since,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String timestamp,
            @ApiParam(
                    name = "requireDeviceInfo",
                    value = "Boolean flag indicating whether to include device-info (location, application list etc) \n" +
                            " to the device object.",
                    required = false)
            @QueryParam("requireDeviceInfo")
                    boolean requireDeviceInfo,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);


    @GET
    @Path("/billing")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Cost details of devices in a tenant",
            notes = "Provides individual cost and total cost of all devices per tenant.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of devices.",
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 400,
                    message = "The incoming request has more than one selection criteria defined via the query parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "The search criteria did not match any device registered with the server.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response getDevicesBilling(
            @ApiParam(
                    name = "tenantDomain",
                    value = "The tenant domain.",
                    required = false)
                    String tenantDomain,
            @ApiParam(
                    name = "startDate",
                    value = "The start date.",
                    required = false)
                    Timestamp startDate,
            @ApiParam(
                    name = "endDate",
                    value = "The end date.",
                    required = false)
                    Timestamp endDate,
            @ApiParam(
                    name = "generateBill",
                    value = "The generate bill boolean.",
                    required = false)
                    boolean generateBill,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "10")
            @QueryParam("limit")
                    int limit);


    @GET
    @Path("/billing/file")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Cost details of devices in a tenant",
            notes = "Provides individual cost and total cost of all devices per tenant.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of devices.",
                    response = DeviceList.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body"),
                            @ResponseHeader(
                                    name = "Content-Disposition",
                                    description = "The content disposition of the body"),
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
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 400,
                    message = "The incoming request has more than one selection criteria defined via the query parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "The search criteria did not match any device registered with the server.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response exportBilling(
            @ApiParam(
                    name = "tenantDomain",
                    value = "The tenant domain.",
                    required = false)
                    String tenantDomain,
            @ApiParam(
                    name = "startDate",
                    value = "The start date.",
                    required = false)
                    Timestamp startDate,
            @ApiParam(
                    name = "endDate",
                    value = "The end date.",
                    required = false)
                    Timestamp endDate,
            @ApiParam(
                    name = "generateBill",
                    value = "The generate bill boolean.",
                    required = false)
                    boolean generateBill);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices Owned by an Authenticated User",
            notes = "Provides details of devices enrolled by authenticated users.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of devices.",
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 400,
                    message = "The incoming request has more than one selection criteria defined via the query parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "The search criteria did not match any device registered with the server.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    @Path("/user-devices")
    Response getDeviceByUser(
            @ApiParam(
                    name = "requireDeviceInfo",
                    value = "Boolean flag indicating whether to include device-info (location, application list etc) \n" +
                            " to the device object.",
                    required = false)
            @QueryParam("requireDeviceInfo")
                    boolean requireDeviceInfo,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Registered Devices Owned by an Authenticated User to generate token for Traccar",
            notes = "Provides details of devices enrolled by authenticated users to generate token for Traccar.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of devices.",
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 400,
                    message = "The incoming request has more than one selection criteria defined via the query parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "The search criteria did not match any device registered with the server.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    @Path("/traccar-user-token")
    Response getTraccarUserToken();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{groupId}/location-history")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Location Details of Devices in the group",
            notes = "Get the location details of devices  in the group during a define time period.",
            response = Response.class,
            tags = "Device Management",
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
                            response = Device.class,
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
                            message = "Not Found. \n Location history details for the devices with the specified group id was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the devices location history details.",
                            response = ErrorResponse.class)
            })
    Response getDevicesGroupLocationInfo(
            @ApiParam(
                    name = "groupId",
                    value = "The group ID.",
                    required = true)
            @PathParam("groupId") int groupId,
            @ApiParam(
                    name = "from",
                    value = "Define the time to start getting the geo location history of the device in " +
                            "milliseconds.",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Define the time to finish getting the geo location history of the device in " +
                            "milliseconds.",
                    required = true)
            @QueryParam("to") long to,
            @ApiParam(
                    name = "type",
                    value = "Defines how the output should be.",
                    required = true)
            @QueryParam("type") String type,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
             @ApiParam(
                     name = "limit",
                     value = "Provide how many device details you require from the starting pagination index/offset.",
                     required = false,
                     defaultValue = "100")
             @QueryParam("limit") int limit
    );

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Device",
            notes = "Get the details of a device by specifying the device type and device identifier and optionally " +
                    "the owner.",
            tags = "Device Management",
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
                            response = Device.class,
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
    Response getDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "owner",
                    value = "The owner of the device you want ot get details.")
            @QueryParam("owner")
            @Size(max = 100)
                    String owner,
            @ApiParam(
                    name = "ownership",
                    value = "Device ownership.")
            @QueryParam("ownership")
            @Size(max = 100)
                    String ownership,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200")
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{deviceType}/{deviceId}/location-history")
    @ApiOperation(
            produces = "application/json",
            httpMethod = "GET",
            value = "Getting the Location Details of a Device",
            notes = "Get the location details of a device during a define time period.",
            response = Response.class,
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:details")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK.",
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
                    code = 400,
                    message = "Bad Request. \n Invalid Device Identifiers found.",
                    response = Response.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Error on retrieving stats",
                    response = Response.class)
    })
    Response getDeviceLocationInfo(
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @PathParam("deviceId") String deviceId,
            @ApiParam(
                    name = "from",
                    value = "Define the time to start getting the geo location history of the device in " +
                            "milliseconds.",
                    required = true)
            @QueryParam("from") long from,
            @ApiParam(
                    name = "to",
                    value = "Define the time to finish getting the geo location history of the device in " +
                            "milliseconds.",
                    required = true)
            @QueryParam("to") long to,
            @ApiParam(
                    name = "type",
                    value = "Defines how the output should be.",
                    required = true)
            @QueryParam("type") String type);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/type/any/id/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Device",
            notes = "Get the details of a device by specifying the device identifier.",
            tags = "Device Management",
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
                            response = Device.class,
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
    Response getDeviceByID(
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String timestamp,
            @ApiParam(
                    name = "requireDeviceInfo",
                    value = "Boolean flag indicating whether to include device-info \n" +
                            "(location, application list etc) to the device object.",
                    required = false)
            @QueryParam("requireDeviceInfo")
                    boolean requireDeviceInfo);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/type/any/list")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Getting Details of Devices",
            notes = "Get the details of devices by specifying the device identifiers.",
            tags = "Device Management",
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
                            response = Device.class,
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
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response getDeviceByIdList(List<String> deviceIds);


    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Device Enrollment Status",
            notes = "Get the device enrollment status and the device details of the device.",
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
    Response isEnrolled(@ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
                        @PathParam("type") String type,
                        @ApiParam(name = "id", value = "The device id.", required = true)
                        @PathParam("id") String deviceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/location")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Location Details of a Device",
            notes = "Get the location details of a device by specifying the device type and device identifier.",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the location details of the device.",
                            response = Device.class, //TODO, This should be DeviceLocation.class
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
                            message = "Not Found. \n Location data for the specified device was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response getDeviceLocation(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows, or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/info")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Information of a Device",
            notes = "Get the information of a device by specifying the device type and device identifier.",
            tags = "Device Management",
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
                            message = "OK. \n Successfully fetched the information of the device.",
                            response = DeviceInfo.class,
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
                            message = "Not Found. \n Location data for the specified device was not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the device details.",
                            response = ErrorResponse.class)
            })
    Response getDeviceInformation(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows, or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you want ot get details.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    //device rename request would looks like follows
    //POST devices/type/virtual_firealarm/id/us06ww93auzp/rename
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/type/{deviceType}/id/{deviceId}/rename")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Update the Displayed Name of the Device",
            notes = "Use this API to rename a device so that it is easy for you to identify devices.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched information of the device.",
                            response = Device.class,
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest " +
                                    "version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            })
    Response renameDevice(
            @ApiParam(
                    name = "device",
                    value = "The payload containing the new name of the device.",
                    required = true)
                    Device device,
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "device-id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("deviceId")
            @Size(max = 45)
                    String deviceId);

    //device remove request would looks like follows
    //DELETE devices/type/virtual_firealarm/id/us06ww93auzp
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/type/{deviceType}/id/{deviceId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Remove the Device Specified by the Device ID",
            notes = "Returns the status of the deleted device operation and the details of the deleted device.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the device.",
                            response = Device.class,
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest " +
                                    "version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            })
    Response deleteDevice(
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("deviceType")
            @Size(max = 45)
                    String deviceType,
            @ApiParam(
                    name = "device-id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("deviceId")
            @Size(max = 45)
                    String deviceId);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/features")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Feature Details of a Device",
            notes = "WSO2 IoTS features enable you to carry out many operations based on the device platform. " +
                    "Using this REST API you can get the features that can be carried out on a preferred device type," +
                    " such as iOS, Android or Windows.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:features")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of features.",
                            response = Feature.class,
                            responseContainer = "List",
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified device can not be found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the feature list for the device platform.",
                            response = ErrorResponse.class)
            })
    Response getFeaturesOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.\n" +
                            "INFO: Make sure to add the ID of a device that is already registered with WSO2 IoTS.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z. \n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/search-devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Advanced Search for Devices",
            notes = "Search for devices by filtering the search result through the specified search terms.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:search")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the device information.",
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
                                            description = "Date and time the resource was last modified. \n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Acceptable.\n The existing device did not match the values specified in the device search.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while getting the device details.",
                            response = ErrorResponse.class)
            })
    Response searchDevices(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit,
            @ApiParam(
                    name = "searchContext",
                    value = "The properties to advanced search devices.",
                    required = true)
                    SearchContext searchContext);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/query-devices")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Property based Search for Devices",
            notes = "Search for devices based on properties",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:search")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved the device information.",
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
                                            description = "Date and time the resource was last modified. \n" +
                                                          "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                      "Empty body because the client already has the latest version of the requested resource.\n"),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Acceptable.\n The existing device did not match the values specified in the device search.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The format of the requested entity was not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                      "Server error occurred while getting the device details.",
                            response = ErrorResponse.class)
            })
    Response queryDevicesByProperties(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit,
            @ApiParam(
                    name = "device property map",
                    value = "properties by which devices need filtered",
                    required = true)
            PropertyMap map);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/applications")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Installed Application Details of a Device",
            notes = "Get the list of applications subscribed to by a device.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:applications")
                    })

            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of applications.",
                            response = Application.class,
                            responseContainer = "List",
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
                                            description = "Date and time the resource was last modified\n" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.\n",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified device does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the list of installed application on the device.",
                            response = ErrorResponse.class)
            })
    Response getInstalledApplications(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows, or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many application details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit);


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/uninstallation")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Uninstall apps in device using apps tab",
            notes = "Check app is subscribed in store or not and then do uninstallation accordingly",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:applications")
                    })
            })
    @ApiResponses(
            value = {

            })
    Response uninstallation(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "packageName",
                    value = "The package name of the app user want to uninstall",
                    required = true)
            @QueryParam("packageName")
                    String packageName);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/operations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Device Operation Details",
            notes = "Get the details of operations carried out on a selected device.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:operations")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of operations scheduled for the device.",
                            response = Operation.class,
                            responseContainer = "List",
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
                                            description = "Date and time the resource was last modified" +
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.\n",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified device does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the operation list scheduled for the device.",
                            response = ErrorResponse.class)
            })
    Response getDeviceOperations(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you wish to get details.\n" +
                            "INFO: Make sure to add the ID of a device that is already registered with WSO2 IoTS.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200")
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit,
            @ApiParam(
                    name = "owner",
                    value = "Provides the owner of the required device.",
                    required = true,
                    defaultValue = "")
            @QueryParam("owner")
                    String owner,
            @ApiParam(
                    name = "ownership",
                    value = "Provides the ownership of the required device.")
            @QueryParam("ownership")
                    String ownership,
            @ApiParam(
                    name = "createdFrom",
                    value = "Since when user wants to filter operation logs using the created data and time")
            @QueryParam("createdFrom")
                    Long createdFrom,
            @ApiParam(
                    name = "createdTo",
                    value = "Till when user wants to filter operation logs using the created data and time")
            @QueryParam("createdTo")
                    Long createdTo,
            @ApiParam(
                    name = "updatedFrom",
                    value = "Since when user wants to filter operation logs using the received date and time")
            @QueryParam("updatedFrom")
                    Long updatedFrom,
            @ApiParam(
                    name = "updatedTo",
                    value = "Till when user wants to filter operation logs using the received date and time")
            @QueryParam("updatedTo")
                    Long updatedTo,
            @ApiParam(
                    name = "operationCode",
                    value = "Provides the operation codes to filter the operation logs via operation codes")
            @QueryParam("operationCode")
                    List<String> operationCode,
            @ApiParam(
                    name = "operationStatus",
                    value = "Provides the status codes to filter operation logs via status")
            @QueryParam("operationStatus")
                    List<String> status);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/effective-policy")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Details of a Policy Enforced on a Device",
            notes = "A policy is enforced on all the devices that register with WSO2 IoTS." +
                    "WSO2 IoTS filters the policies based on the device platform (device type)," +
                    "the device ownership type, the user role or name and finally, the policy that matches these filters will be enforced on the device.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:effective-policy")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully returned the details of the policy enforced on the device.",
                            response = Policy.class,
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n " +
                                    "The source can be retrieved from the URL specified in the location header.\n",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified device does not exist.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable. \n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the policy details that is enforced on the device.",
                            response = ErrorResponse.class)
            }
    )
    Response getEffectivePolicyOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows, or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "The device ID.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time. \n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since")
                    String ifModifiedSince);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{type}/{id}/compliance-data")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Policy Compliance Details of a Device",
            notes = "A policy is enforced on the devices that register with WSO2 IoTS. " +
                    "The server checks if the settings in the device comply with the policy that is enforced on the device using this REST API.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:compliance-data")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK",
                            response = NonComplianceData.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while getting the compliance data.",
                            response = ErrorResponse.class)
            }
    )
    Response getComplianceDataOfDevice(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android or windows.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "Device Identifier",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{type}/{id}/getstatushistory")
  @ApiOperation(
          produces = MediaType.APPLICATION_JSON,
          httpMethod = "GET",
          value = "Get Device status history",
          notes = "Get a list of status history associated with the device type and id",
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
                          message = "OK. \n Successfully fetched the status history of matching devices.",
                          response = List.class,
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
    Response getDeviceStatusHistory(
          @ApiParam(
                  name = "type",
                  value = "The device type, such as ios, android, or windows.",
                  required = true)
          @PathParam("type")
          @Size(max = 45)
                  String type,
          @ApiParam(
                  name = "id",
                  value = "Device ID.",
                  required = true)
          @PathParam("id")
          @Size(max = 45)
                  String id);
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{type}/{id}/getenrolmentstatushistory")
  @ApiOperation(
          produces = MediaType.APPLICATION_JSON,
          httpMethod = "GET",
          value = "Get Device Current Enrolment status history",
          notes = "Get a list of status history associated with the device type and id for the current enrolment",
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
                          message = "OK. \n Successfully fetched the status history of matching devices.",
                          response = List.class,
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
    Response getCurrentEnrolmentDeviceStatusHistory(
          @ApiParam(
                  name = "type",
                  value = "The device type, such as ios, android, or windows.",
                  required = true)
          @PathParam("type")
          @Size(max = 45)
                  String type,
          @ApiParam(
                  name = "id",
                  value = "Device ID.",
                  required = true)
          @PathParam("id")
          @Size(max = 45)
                  String id);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{type}/{id}/changestatus")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Changing the Status of a Device",
            notes = "Change the status of a device from one state to another.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:change-status")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully changed the device status.",
                            response = Device.class,
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest " +
                                    "version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            })
    Response changeDeviceStatus(
            @ApiParam(
                    name = "type",
                    value = "The device type, such as ios, android, or windows.",
                    required = true)
            @PathParam("type")
            @Size(max = 45)
                    String type,
            @ApiParam(
                    name = "id",
                    value = "Device ID.",
                    required = true)
            @PathParam("id")
            @Size(max = 45)
                    String id,
            @ApiParam(
                    name = "newStatus",
                    value = "The available device statuses are CREATED, ACTIVE, INACTIVE, UNREACHABLE, UNCLAIMED, SUSPENDED, BLOCKED, REMOVED, and DISENROLLMENT_REQUESTED.",
                    required = true)
            @QueryParam("newStatus")
                    EnrolmentInfo.Status newStatus);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{type}/operations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending an Operation to Specific Device Types",
            notes = "You can send an operation to a group of devices that belong to a specific device type using this API. For example, you can send a ring operation to all the enrolled Android devices.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:operations")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully added the operation.",
                            response = Activity.class,
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest " +
                                    "version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            })
    Response addOperation(@ApiParam(name = "type", value = "The device type, such as ios, android or windows... etc.", required = true)
                          @PathParam("type") String type,
                          @ApiParam(name = "deviceOperation", value = "Operation object with device ids.", required = true)
                          @Valid OperationRequest operationRequest);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/type/{type}/status/{status}/count")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Device Count with status",
            notes = "Get specified device count with type and status.",
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
                            message = "OK. \n Successfully fetched the count of matching devices.",
                            response = int.class,
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
                    String status);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/type/{type}/status/{status}/ids")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting ids of devices with specified type and status",
            notes = "Get the ids of a device by specifying the device type and status.",
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
                            message = "OK. \n Successfully fetched the details of the device.",
                            response = String[].class,
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
                    String status);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/type/{type}/status/{status}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Changing the Status of a Devices",
            notes = "Change the status of a devices from one state to another.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:change-status")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully changed the device status.",
                            response = Device.class,
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
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                          "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. Empty body because the client already has the latest " +
                                      "version of the requested resource."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No device is found under the provided type and id.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                      "Server error occurred while retrieving information requested device.",
                            response = ErrorResponse.class)
            })
    Response bulkUpdateDeviceStatus(
            @ApiParam(name = "type", value = "The device type, such as ios, android or windows.", required = true)
            @PathParam("type") String type,
            @ApiParam(name = "status", value = "The device type, such as ios, android or windows.", required = true)
            @PathParam("status") String status,
            @ApiParam(name = "deviceList", value = "The payload containing the new name of the device.", required = true)
            @Valid List<String> deviceList);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/compliance/{complianceStatus}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Policy Compliance Status of all devices",
            notes = "A policy is enforced on the devices that register with Entgra IoTS. " +
                    "The server checks if the settings in the device comply with the policy that is enforced on the device using this REST API.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:compliance-data")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK",
                            response = NonComplianceData.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while getting the compliance data.",
                            response = ErrorResponse.class)
            })
    Response getPolicyCompliance(
            @ApiParam(
                    name = "compliance-status",
                    value = "Compliance status for devices. If true, devices which are compliant with policies. " +
                            "If false, devices which are not compliant",
                    required = true)
            @PathParam("complianceStatus")
                    boolean complianceStatus,
            @ApiParam(
                    name = "policy",
                    value = "Policy ID")
            @QueryParam("policy") String policyId,
            @ApiParam(
                    name = "is-pending",
                    value = "Check for devices in pending status")
            @QueryParam("pending") boolean isPending,
            @ApiParam(
                    name = "fromDate",
                    value = "Start date of the duration")
            @QueryParam("from") String fromDate,
            @ApiParam(
                    name = "toDate",
                    value = "end date of the duration")
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
                    int limit);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/features")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Policy Compliance Status of all devices",
            notes = "A policy is enforced on the devices that register with Entgra IoTS. " +
                    "The server checks if the settings in the device comply with the policy that is enforced on the device using this REST API.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:compliance-data")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK",
                            response = NonComplianceData.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while getting the compliance data.",
                            response = ErrorResponse.class)
            })
    Response getNoneComplianceFeatures(
            @ApiParam(
                    name = "id",
                    value = "The device identifier of the device you wish to get details.\n" +
                            "INFO: Make sure to add the ID of a device that is already registered with Entgra IoTS.",
                    required = true)
            @PathParam("id")
                    int id);

    @GET
    @Path("/{deviceType}/applications")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Applications",
            notes = "Provides details of installed applications in all the devices enrolled with Entgra IoT Server.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:applications")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of applications.",
                            response = ApplicationList.class,
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
                            message = "Not Found. \n There are no applications.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while getting the application data.",
                            response = ErrorResponse.class)
            })
    Response getApplications(
            @ApiParam(
                    name = "device-type",
                    value = "Device type (platform) of the application",
                    required = true)
            @PathParam("deviceType")
                    String deviceType,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    defaultValue = "10")
            @QueryParam("limit")
                    int limit,
            @ApiParam(
                    name = "appName",
                    value = "App name to be searched")
            @QueryParam("appName")
                    String appName);

    @GET
    @Path("/application/{packageName}/versions")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting versions of a given application",
            notes = "Provides versions of a given application installed in devices of Entgra IoT Server.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:applications")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of app versions.",
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
                            message = "Error occurred while getting the version data.",
                            response = ErrorResponse.class)
            })
    Response getAppVersions(
            @ApiParam(
                    name = "package-name",
                    value = "The package name of the app.",
                    required = true)
            @PathParam("packageName")
                    String packageName);

    @PUT
    @Path("/{deviceType}/{id}/operation")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update status of a given opeation",
            notes = "Updates the status of a given operation of a given device in Entgra IoT Server.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:devices:operations")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated the operation status.",
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
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Error occurred while updating operation status.",
                            response = ErrorResponse.class)
            })
    Response updateOperationStatus(
            @ApiParam(
                    name = "device-type",
                    value = "The device type, such as ios, android, or windows.")
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "id",
                    value = "The device identifier")
            @PathParam("id") String deviceId,
            OperationStatusBean operationStatusBean);

    @GET
    @Path("/filters")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieving filters of device.",
            notes = "Provides filters in devices of Entgra IoT Server which can be used in UI for filtering.",
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
                            message = "OK. \n Successfully fetched the device filters.",
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
                            message = "Error occurred while getting the version data.",
                            response = ErrorResponse.class)
            })
    Response getDeviceFilters();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{clientId}/{clientSecret}/default-token")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the default token",
            notes = "Getting the default access token by using given client ID and the client secret value.",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:device:enroll")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully returned the default token details.",
                            response = Policy.class,
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while retrieving the default token.",
                            response = ErrorResponse.class)
            }
    )
    Response getDefaultToken(
            @ApiParam(
                    name = "client ID",
                    value = "Client Id.",
                    required = true)
            @PathParam("clientId")
                    String clientId,
            @ApiParam(
                    name = "client secret",
                    value = "Client Secret",
                    required = true)
            @PathParam("clientSecret")
                    String clientSecret,
            @QueryParam("scopes") String scopes
    );
}
