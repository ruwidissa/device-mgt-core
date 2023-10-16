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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.apache.axis2.transport.http.HTTPConstants;
import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceGroupList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupRoleWrapper;
import javax.validation.Valid;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
                                @ExtensionProperty(name = "name", value = "GroupManagementAdmin"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/groups"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Path("/admin/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(value = "Group Management Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and do a selected number of operations. " +
        "Further, this is strictly restricted to admin users only ")
@Scopes(
        scopes = {
                @Scope(
                        name = "View groups",
                        description = "",
                        key = "gm:admin:groups:view",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/groups/view"}
                ),
                @Scope(
                        name = "Count groups",
                        description = "",
                        key = "gm:admin:groups:count",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/groups/count"}
                ),
                @Scope(
                        name = "Add groups",
                        description = "",
                        key = "gm:admin:groups:add",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/groups/add"}
                )
        }
)
public interface GroupManagementAdminService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the list of groups.",
            notes = "Returns all groups enrolled with the system.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:admin:groups:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of device groups.",
                    response = DeviceGroupList.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the groups list.",
                    response = ErrorResponse.class)
    })
    Response getGroups(@ApiParam(
                               name = "name",
                               value = "Name of the group.")
                       @QueryParam("name")
                               String name,
                       @ApiParam(
                               name = "owner",
                               value = "Owner of the group.")
                       @QueryParam("owner")
                               String owner,
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
                               int limit,
                       @ApiParam(
                               name = "status",
                               value = "status of group to be retrieve.")
                       @QueryParam("status")
                               String status,
                       @ApiParam(
                               name = "requireGroupProps",
                               value = "Request group properties to include in the response",
                               defaultValue = "false")
                       @QueryParam("requireGroupProps")
                               boolean requireGroupProps);

    @GET
    @Path("hierarchy")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting the List of Hierarchical Groups",
            notes = "Returns all groups enrolled with the system hierarchically.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:admin:groups:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of device groups hierarchically.",
                    response = DeviceGroupList.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the groups list.",
                    response = ErrorResponse.class)
    })
    Response getGroupsWithHierarchy(@ApiParam(
            name = "name",
            value = "Name of the group.")
            @QueryParam("name")
                    String name,
            @ApiParam(
                    name = "owner",
                    value = "Owner of the group.")
            @QueryParam("owner")
                    String owner,
            @ApiParam(
                    name = "status",
                    value = "status of group to be retrieve.")
            @QueryParam("status")
                    String status,
            @ApiParam(
                    name = "requireGroupProps",
                    value = "Request group properties to include in the response",
                    defaultValue = "false")
            @QueryParam("requireGroupProps")
                    boolean requireGroupProps,
            @ApiParam(
                    name = "depth",
                    value = "Depth of the group hierarchy.")
            @DefaultValue("3")
            @QueryParam("depth")
                    int depth,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @DefaultValue("0")
            @QueryParam("offset")
                    int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many records require from the starting pagination index/offset.",
                    defaultValue = "5")
            @DefaultValue("5")
            @QueryParam("limit")
                    int limit);

    @Path("/hierarchy/count")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the count of all hierarchical groups belongs to current admin user.",
            notes = "Returns count of all permitted hierarchical groups enrolled with the system.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:admin:groups:count")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the hierarchical device group count.",
                    response = Integer.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group count.",
                    response = ErrorResponse.class)
    })
    Response getHierarchicalGroupCount(@ApiParam(
            name = "status",
            value = "status of hierarchical groups of which count should be retrieved")
                           @QueryParam("status")
                           String status);

    @Path("/count")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get the count of groups belongs to current user.",
            notes = "Returns count of all permitted groups enrolled with the system.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:admin:groups:count")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device group count.",
                    response = Integer.class,
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
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                              "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "No groups found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group count.",
                    response = ErrorResponse.class)
    })
    Response getGroupCount(@ApiParam(
                    name = "status",
                    value = "status of groups of which count should be retrieved")
            @QueryParam("status")
                    String status);

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Adding a New Device Group",
            notes = "Add device group with the current user as the owner.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:admin:groups:add")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Device group has successfully been created",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the added group."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time" +
                                                    ".\n" + "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location " +
                                    "header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 401,
                            message = "Unauthorized. \n Current logged in user is not authorized to add device groups.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not supported " +
                                    "format."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while adding a new device group.",
                            response = ErrorResponse.class)
            })
    Response createGroup(@ApiParam(
            name = "group",
            value = "Define the group object with data.",
            required = true)
                         @Valid DeviceGroup group);



    @POST
    @Path("/roles/share")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Consolidated API for Creating a Device Group, Adding Devices, and Sharing",
            notes = "This API can be used to create a new device group, add devices to the group, and share the group with user roles.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:add")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Device group has successfully been created.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the created group."),
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body."),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource has been modified the last time.\n" +
                                                    "Used by caches, or in conditional requests.")
                            }
                    ),
                    @ApiResponse(
                            code = 303,
                            message = "See Other. \n Source can be retrieved from the URL specified at the Location " +
                                    "header.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The Source URL of the document.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 401,
                            message = "Unauthorized. \n Current logged in user is not authorized to perform the operation.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 404,
                            message = "Group not found.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n " +
                                    "Server error occurred while creating the group or adding devices or sharing the group.",
                            response = ErrorResponse.class)
            })
    Response createGroupWithRoles(@ApiParam(
            name = "group",
            value = "Define the group object with data.",
            required = true)
                                  @Valid DeviceGroupRoleWrapper group

    );

}
