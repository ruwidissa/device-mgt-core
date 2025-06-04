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

package io.entgra.device.mgt.core.device.mgt.config.api.service;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.common.DeviceTransferRequest;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.DeviceConfiguration;
import io.entgra.device.mgt.core.device.mgt.common.general.TenantDetail;
import io.entgra.device.mgt.core.device.mgt.config.api.beans.ErrorResponse;
import io.swagger.annotations.*;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.dto.OperationConfig;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceManagementConfiguration"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/device-mgt-config/v1.0"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "Device management configuration service")
        }
)
@Api(value = "Device Management Configuration")
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(scopes = {
        @Scope(
                name = "View configurations",
                description = "",
                key = "dm:conf:view",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/device-mgt/platform-configurations/view"}
        ),
        @Scope(
                name = "Manage configurations",
                description = "",
                key = "dm:conf:manage",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/device-mgt/platform-configurations/manage"}
        ),
        @Scope(
                name = "Getting Details of Device tenants",
                description = "Getting Details of Device tenants",
                key = "admin:tenant:view",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/tenants/view"}
        ),
        @Scope(
                name = "Add a permission to the permission tree",
                description = "Add a permission to the permission tree",
                key = "admin:permissions:add",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/permissions/add"}
        ),
        @Scope(
                name = "Manage operation configuration",
                description = "Add or update operation configuration",
                key = "admin:operation_config:manage",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/operation-configuration/manage"}
        )
}
)
public interface DeviceManagementConfigService {

    @GET
    @Path("/configurations")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting General device Configurations",
            notes = "This API is responsible for send device configuration data to an IOT device when the " +
                    "device starts provisioning",
            tags = "Device Management Configuration"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the device configurations.",
                            response = DeviceConfiguration.class,
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
                                            description = "Date and time the resource has been modified " +
                                                          "the last time.Used by caches, or in " +
                                                          "conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad request.\n The request contains invalid parameters"),
                    @ApiResponse(
                            code = 401,
                            message = "Unauthorized.\n The requested is not authorized"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while " +
                                      "fetching device configurations.",
                            response = ErrorResponse.class)
            })
    @Produces(MediaType.APPLICATION_JSON)
    Response getConfiguration(
            @ApiParam(
                    name = "token",
                    value = "value for identify an already enrolled and authorized device",
                    required = true)
            @HeaderParam("token")
                    String token,
            @ApiParam(
                    name = "properties",
                    value = "The properties list using for query a device",
                    required = true)
            @QueryParam("properties")
                    String properties,
            @ApiParam(
                    name = "withAccessToken",
                    value = "Whether to use access token or otp token for device configuration")
            @QueryParam("withAccessToken")
                    boolean withAccessToken,
            @ApiParam(
                    name = "withGateways",
                    value = "Whether to retrieve gateway properties or not")
            @QueryParam("withGateways")
                    boolean withGateways);


    @PUT
    @Path("/device/transfer")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Transfer device to another tenant from super tenant",
            notes = "This API is responsible for transfer device from super tenant to another tenant",
            tags = "Device Management Configuration",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = "scope", value = "dm:conf:manage")
            })
    }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully transferred the device.",
                            response = DeviceConfiguration.class,
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
                                            description = "Date and time the resource has been modified " +
                                                    "the last time.Used by caches, or in " +
                                                    "conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 400,
                            message = "Bad request.\n The request contains invalid parameters"),
                    @ApiResponse(
                            code = 401,
                            message = "Unauthorized.\n The requested is not authorized"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while " +
                                    "fetching device configurations.",
                            response = ErrorResponse.class)
            })
    @Produces(MediaType.APPLICATION_JSON)
    Response transferDevices(
            @ApiParam(
                    name = "Device Transfer Request",
                    value = "The device transfer request",
                    required = true)
                    DeviceTransferRequest deviceTransferRequest);

    @GET
    @Path("/configurations/ui-config")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get application management UI configuration",
            notes = "This will get all UI configuration of application management"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got UI config."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. There doesn't have an defined UI config." +
                                    "query."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the UI config.",
                            response = ErrorResponse.class)
            })
    Response getUiConfig();

    @GET
    @Path("/tenants")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of tenants",
            notes = "Get the details of tenants.",
            response = TenantDetail.class,
            responseContainer = "List",
            tags = "Device Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "scope", value ="admin:tenant:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of tenants.",
                    response = TenantDetail.class,
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
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the" +
                            " tenant list.",
                    response = ErrorResponse.class)
    })
    Response getTenants();

    @POST
    @Path("/permissions")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add permission to the tree",
            notes = "Add permission to the tree.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "scope", value =
                                    "admin:permissions:add")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully added the permissions.",
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
                    message = "The incoming request has more than one selection criteria defined via the query parameters.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching " +
                            "adding permission to the tree.",
                    response = ErrorResponse.class)
    })
    @Produces(MediaType.APPLICATION_JSON)
    Response addPermission(List<String> permissions);

    @GET
    @Path("/operation-configuration")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Getting operation configuration",
            notes = "Retrieve the operation configuration",
            tags = "Device Management Configuration",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "scope", value = "admin:operation_config:manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the operation configuration.",
                    response = OperationConfig.class,
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
                    message = "Bad Request.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized operation! Only admin role can perform this operation."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n No operation found",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding operation configuration.",
                    response = ErrorResponse.class)
    })
    Response getOperationConfiguration();

    @POST
    @Path("/operation-configuration")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.POST,
            value = "Add operation configuration",
            notes = "Add operation configuration.",
            tags = "Device Management Configuration",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "scope", value =
                                    "admin:operation_config:manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully added the operation configuration.",
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
                    message = "The incoming request has wrong operation configuration.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding operation configuration",
                    response = ErrorResponse.class)
    })
    @Produces(MediaType.APPLICATION_JSON)
    Response addOperationConfiguration(OperationConfig config);

    @PUT
    @Path("/operation-configuration")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.PUT,
            value = "Update operation configuration",
            notes = "Update operation configuration.",
            tags = "Device Management Configuration",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "scope", value = "admin:operation_config:manage")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully Update the operation configuration.",
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
                    message = "The incoming request has wrong operation configuration.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding operation configuration.",
                    response = ErrorResponse.class)
    })
    @Produces(MediaType.APPLICATION_JSON)
    Response updateOperationConfiguration(OperationConfig config);

    @DELETE
    @Path("/operation-configuration")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.DELETE,
            value = "Delete operation configuration",
            notes = "Delete operation configuration",
            tags = {"Device Management Configuration"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = "scope", value = "admin:operation_config:manage")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n  Successfully deleted the operation configuration",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Operation configuration not provided",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while deleting the operation configuration.",
                            response = Response.class)
            }
    )
    Response deleteOperationConfiguration();
}
