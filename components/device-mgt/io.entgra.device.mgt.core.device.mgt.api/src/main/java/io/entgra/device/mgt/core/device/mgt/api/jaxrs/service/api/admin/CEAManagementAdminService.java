/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.CEAPolicyWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = {"conditional_email_access", "device_management"})
@Path("/admin/cea-policies")
@SwaggerDefinition(
        info = @Info(
                description = "Conditional Email Access Management",
                version = "v1.0.0",
                title = "CEAManagementAdminService API",
                extensions = @Extension(properties = {
                        @ExtensionProperty(name = "name", value = "CEAManagementAdminService"),
                        @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/cea-policies"),
                })
        ),
        consumes = {MediaType.APPLICATION_JSON},
        produces = {MediaType.APPLICATION_JSON},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
        tags = {
                @Tag(name = "device_management", description = "Device management"),
                @Tag(name = "conditional_email_access", description = "Mailbox access management")
        }
)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Scopes(
        scopes = {
                @Scope(
                        name = "CEA policy ui configuration view",
                        description = "CEA policy ui configuration view",
                        key = "dm:admin:cea:view",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/cea/view"}
                ),
                @Scope(
                        name = "Add CEA policy",
                        description = "Add CEA policy",
                        key = "dm:admin:cea:add",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/cea/add"}
                ),
                @Scope(
                        name = "Update CEA policy",
                        description = "Update CEA policy",
                        key = "dm:admin:cea:update",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/cea/update"}
                ),
                @Scope(
                        name = "Delete CEA policy",
                        description = "Delete CEA policy",
                        key = "dm:admin:cea:delete",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/cea/delete"}
                ),
                @Scope(
                        name = "Sync CEA policy",
                        description = "Sync CEA policy",
                        key = "dm:admin:cea:sync",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/cea/sync"}
                )
        }
)
public interface CEAManagementAdminService {
    @GET
    @Path("/ui")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Retrieve CEA policy ui configurations",
            notes = "Returns CEA policy ui configurations for supporting mail services",
            tags = {"conditional_email_access", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:admin:cea:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200,
                            message = "OK. \n Successfully retrieve the cea ui configurations",
                            response = Integer.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n Empty body because the client has already the latest version of " +
                                    "the requested resource."),
                    @ApiResponse(
                            code = 404,
                            message = "Configurations not found",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the group count.",
                            response = ErrorResponse.class)
            }
    )
    Response getCEAPolicyUI();


    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.POST,
            value = "Add CEA policy",
            notes = "Create conditional email access policy",
            tags = {"conditional_email_access", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:admin:cea:add")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n  Successfully created the CEA policy",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist."),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n  CEA policy already exists.",
                            response = Response.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = ErrorResponse.class)
            }
    )
    Response createCEAPolicy(
            @ApiParam(
                    name = "ceaPolicy",
                    value = "Conditional email access policy details",
                    required = true
            )
            CEAPolicyWrapper ceaPolicyWrapper);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Retrieve CEA policy",
            notes = "Retrieve conditional email access policy",
            tags = {"conditional_email_access", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:admin:cea:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n  Successfully retrieved the CEA policy",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist."),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n  CEA policy already exists.",
                            response = Response.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = ErrorResponse.class)
            }
    )
    Response retrieveCEAPolicy();

    @DELETE
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.DELETE,
            value = "Delete CEA policy",
            notes = "Delete conditional email access policy",
            tags = {"conditional_email_access", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:admin:cea:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n  Successfully deleted the CEA policy",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = ErrorResponse.class)
            }
    )
    Response deleteCEAPolicy();

    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.PUT,
            value = "Update CEA policy",
            notes = "Update conditional email access policy",
            tags = {"conditional_email_access", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:admin:cea:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n  Successfully updated the CEA policy",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = ErrorResponse.class)
            }
    )
    Response updateCEAPolicy(@ApiParam(
            name = "ceaPolicy",
            value = "Conditional email access policy details",
            required = true
    ) CEAPolicyWrapper ceaPolicyWrapper);

    @GET
    @Path("/sync-now")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HttpMethod.GET,
            value = "Sync with active sync server",
            notes = "Sync and enforce conditional access policy",
            tags = {"conditional_email_access", "device_management"},
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "dm:admin:cea:sync")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n  Successfully triggered CEA policy sync",
                            response = Response.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n The specified resource does not exist."),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while creating the resource.",
                            response = ErrorResponse.class)
            }
    )
    Response sync();
}
