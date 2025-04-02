/*
 *   Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.admin.api.spi;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "Dynamic Task Management Service", tags = {"device_management"})
@Path("/admin/dynamic-task-configurations")
@SwaggerDefinition(
        info = @Info(
                description = "Dynamic Task Management Admin Service",
                version = "v1.0.0",
                title = "DynamicTaskManagementAdminService API",
                extensions = @Extension(properties = {
                        @ExtensionProperty(name = "name", value = "DynamicTaskManagementAdminService"),
                        @ExtensionProperty(name = "context", value = "/api/dynamic-task-mgt/v1.0/admin/dynamic-task" +
                                "-configurations"),
                })
        ),
        consumes = {MediaType.APPLICATION_JSON},
        produces = {MediaType.APPLICATION_JSON},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {
                @Tag(name = "device_management", description = "Dynamic Task Management Service")
        }
)

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(
        scopes = {
                @Scope(
                        name = "Dynamic task management configurations view",
                        description = "Dynamic task management configurations view",
                        key = "admin:dynamic-task-configurations:view",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/dynamic-task-configurations/view"}
                ),
                @Scope(
                        name = "Dynamic task management configurations view",
                        description = "Dynamic task management configurations view",
                        key = "admin:dynamic-task-configurations:modify",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/dynamic-task-configurations/view"}
                )
        }
)
public interface DynamicTaskManagementAdminService {
    String SCOPE = "scope";

    @GET
    @Path("/tenants/{tenantDomain}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Retrieve dynamic task configurations",
            notes = "Retrieve dynamic task configurations for specified tenant ",
            tags = {"device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "admin:dynamic-task-configurations:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully retrieve the dynamic task configurations for specified " +
                                    "tenant.",
                            response = Integer.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest version " +
                                    "of " +
                                    "the requested resource.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Dynamic task configurations are not found for the specified tenant.",
                            response = Response.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while retrieving dynamic task " +
                                    "configurations.",
                            response = Response.class)
            }
    )
    Response getDynamicTaskPlatformConfigurations(
            @ApiParam(
                    name = "tenantDomain",
                    value = "Tenant domain to retrieve the configurations")
            @PathParam("tenantDomain") String tenantDomain);

    @PUT
    @Path("/tenants/{tenantDomain}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Update dynamic task configurations",
            notes = "Update dynamic task configurations for specified tenant ",
            tags = {"device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "admin:dynamic-task-configurations:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully updated the dynamic task configurations for specified " +
                                    "tenant.",
                            response = Integer.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest version " +
                                    "of " +
                                    "the requested resource.",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid resource payload received.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Dynamic task configurations are not found for the specified tenant.",
                            response = Response.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while updating dynamic task " +
                                    "configurations.",
                            response = Response.class)
            }
    )
    Response updateDynamicTaskPlatformConfigurations(
            @ApiParam(
                    name = "tenantDomain",
                    value = "Tenant domain to update the configurations")
            @PathParam("tenantDomain") String tenantDomain,
            @ApiParam(
                    name = "categorizedDynamicTask",
                    value = "Categorized dynamic task configurations"
            )
            DynamicTaskPlatformConfigurations dynamicTaskPlatformConfigurations
    );

    @PUT
    @Path("/tenants/{tenantDomain}/reset")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Reset dynamic task configurations",
            notes = "Reset dynamic task configurations to default in specified tenant ",
            tags = {"device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "admin:dynamic-task-configurations:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully reset the dynamic task configurations to default in " +
                                    "specified tenant.",
                            response = Integer.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest version " +
                                    "of " +
                                    "the requested resource.",
                            response = Response.class),
                    @ApiResponse(
                            code = 404,
                            message = "Dynamic task configurations are not found for the specified tenant.",
                            response = Response.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported.",
                            response = Response.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while resetting dynamic task " +
                                    "configurations.",
                            response = Response.class)
            }
    )
    Response resetDynamicTaskPlatformConfigurations(
            @ApiParam(
                    name = "tenantDomain",
                    value = "Tenant domain to reset the configurations")
            @PathParam("tenantDomain") String tenantDomain
    );
}
