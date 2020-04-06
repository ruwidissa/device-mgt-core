/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.carbon.device.mgt.config.jaxrs.service;

import org.wso2.carbon.device.mgt.common.DeviceTransferRequest;
import io.entgra.carbon.device.mgt.config.jaxrs.beans.ErrorResponse;
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
import org.wso2.carbon.device.mgt.common.configuration.mgt.DeviceConfiguration;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceManagementConfiguration"),
                                @ExtensionProperty(name = "context",
                                        value = "/api/device-mgt-config/v1.0/configurations"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Path("/configurations")
@Api(value = "Device Management Configuration")
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(scopes = {
        @Scope(
                name = "View configurations",
                description = "",
                key = "perm:view-configuration",
                permissions = {"/device-mgt/platform-configurations/view"}
        ),
        @Scope(
                name = "Manage configurations",
                description = "",
                key = "perm:manage-configuration",
                permissions = {"/device-mgt/platform-configurations/manage"}
        )
}
)
public interface DeviceManagementConfigService {

    @GET
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
                    String properties);

    @PUT
    @Path("/transfer")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Transfer device to another tenant from super tenant",
            notes = "This API is responsible for transfer device from super tenant to another tenant",
            tags = "Device Management Configuration",
            extensions = {
            @Extension(properties = {
                    @ExtensionProperty(name = "scope", value = "perm:manage-configuration")
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
    @Path("/ui-config")
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
}
