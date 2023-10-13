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
import org.apache.axis2.transport.http.HTTPConstants;
import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceGroupList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceToGroupsAssignment;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.RoleList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupRoleWrapper;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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

/**
 * Device group related REST-API. This can be used to manipulated device group related details.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "GroupManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/groups"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "Device group related REST-API. " +
                        "This can be used to manipulated device group related " +
                        "details.")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get the list of groups belongs to current user.",
                        description = "Get the list of groups belongs to current user.",
                        key = "gm:groups:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/view"}
                ),
                @Scope(
                        name = "Get the count of groups belongs to current user.",
                        description = "Get the count of groups belongs to current user.",
                        key = "gm:groups:count",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/count"}
                ),
                @Scope(
                        name = "Add new device group to the system.",
                        description = "Add new device group to the system.",
                        key = "gm:groups:add",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/add"}
                ),
                @Scope(
                        name = "View group specified",
                        description = "View group specified",
                        key = "gm:groups:groups-view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/specified-groups/view"}
                ),
                @Scope(
                        name = "Update a group",
                        description = "Update a group",
                        key = "gm:groups:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/update"}
                ),
                @Scope(
                        name = "Delete a group",
                        description = "Delete a group",
                        key = "gm:groups:remove",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/remove"}
                ),
                @Scope(
                        name = "Manage group sharing with a user",
                        description = "Manage group sharing with a user",
                        key = "gm:roles:share",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/share"}
                ),
                @Scope(
                        name = "View list of roles of a device group",
                        description = "View list of roles of a device group",
                        key = "gm:roles:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/roles/view"}
                ),
                @Scope(
                        name = "View list of devices in the device group",
                        description = "View list of devices in the device group",
                        key = "gm:devices:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/devices/view"}
                ),
                @Scope(
                        name = "View list of device count in the device group",
                        description = "View list of device count in the device group",
                        key = "gm:devices:count",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/devices/count"}
                ),
                @Scope(
                        name = "Add devices to group",
                        description = "Add devices to group",
                        key = "gm:devices:add",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/devices/add"}
                ),
                @Scope(
                        name = "Remove devices from group",
                        description = "Remove devices from group",
                        key = "gm:devices:remove",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/devices/remove"}
                ),
                @Scope(
                        name = "Assign devices to groups",
                        description = "Assign devices to groups",
                        key = "gm:devices:assign",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/devices/assign"}
                ),
                @Scope(
                        name = "List of groups that have the device",
                        description = "List of groups that have the device",
                        key = "gm:groups:device:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/device-groups/view"}
                ),
                @Scope(
                        name = "View whether the groups has relevant device types",
                        description = "View whether the groups has relevant device types",
                        key = "gm:devices-types:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/groups/device-types/view"}
                )
        }
)
@Path("/groups")
@Api(value = "Device Group Management", description = "This API carries all device group management related " +
        "operations such as get all the available groups, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GroupManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting the List of Groups",
            notes = "Returns all groups enrolled with the system.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:view")
                    })
            },
            nickname = "getGroupsWithFilter"
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
    Response getGroups(
            @ApiParam(
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
                    value = "Provide how many device details you require from the starting pagination " +
                            "index/offset.",
                    defaultValue = "5")
            @QueryParam("limit")
                    int limit,
            @ApiParam(
                    name = "requireGroupProps",
                    value = "Request group properties to include in the response",
                    defaultValue = "false")
            @QueryParam("requireGroupProps")
                    boolean requireGroupProps);


    @GET
    @Path("/hierarchy")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting the List of Hierarchical Groups",
            notes = "Returns all groups enrolled with the system hierarchically.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:view")
                    })
            },
            nickname = "getGroupsWithHierarchyNonAdmin"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the list of device hierarchical groups.",
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
    Response getGroupsWithHierarchy(
            @ApiParam(
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
            value = "Getting the Number of Hirarchical Device Groups",
            notes = "Get the number of hierarchical device groups in the server that the current signed in user can access.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:count")
                    })
            },
            nickname = "getGroupCountNonAdmin"

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the hierarchical device group count.",
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
                    message = "Internal Server Error. \n Server error occurred while fetching the group count.",
                    response = ErrorResponse.class)
    })
    Response getHierarchicalGroupCount();

    @Path("/count")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting the Number of Device Groups",
            notes = "Get the number of device groups in the server that the current signed in user can access.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:count")
                    })
            },
            nickname = "getGroupCountNonAdmin"

    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device group count.",
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
                    message = "Internal Server Error. \n Server error occurred while fetching the group count.",
                    response = ErrorResponse.class)
    })
    Response getGroupCount();

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Adding a New Device Group",
            notes = "Add device group with the current user as the owner.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:add")
                    })
            },
            nickname = "createGroupByGroupObject"
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

    @Path("/id/{groupId}")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting Details of a Specific Device Group",
            notes = "Get the details of a specific device group.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:groups-view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device group.",
                    response = DeviceGroup.class,
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
                    message = "Group found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group details.",
                    response = ErrorResponse.class)
    })
    Response getGroup(
            @ApiParam(
                    name = "groupId",
                    value = "The ID of the group.",
                    required = true)
            @PathParam("groupId") int groupId,
            @ApiParam(
                    name = "requireGroupProps",
                    value = "Request group properties to include in the response",
                    defaultValue = "false")
            @QueryParam("requireGroupProps")
                    boolean requireGroupProps,
            @ApiParam(
                    name = "depth",
                    value = "Depth of the group hierarchy.",
                    defaultValue = "1")
            @DefaultValue("1")
            @QueryParam("depth")
                    int depth,
            @ApiParam(
                    name = "allowed",
                    value = "Whether to return allowed group",
                    defaultValue = "false")
            @QueryParam("allowed")
            @DefaultValue("false")
            boolean allowed);

    @Path("/name/{groupName}")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting Details of a Specific Device Group",
            notes = "Get the details of a specific device group.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:groups-view")
                    })
            },
            nickname = "getGroupByGroupNameFilter"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device group.",
                    response = DeviceGroup.class,
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
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group details.",
                    response = ErrorResponse.class)
    })
    Response getGroup(
            @ApiParam(
                    name = "groupName",
                    value = "Name of the group.",
                    required = true)
            @PathParam("groupName") String groupName,
            @ApiParam(
                    name = "requireGroupProps",
                    value = "Request group properties to include in the response",
                    defaultValue = "false")
            @QueryParam("requireGroupProps")
                    boolean requireGroupProps,
            @ApiParam(
                    name = "depth",
                    value = "Depth of the group hierarchy.",
                    defaultValue = "1")
            @DefaultValue("1")
            @QueryParam("depth")
                    int depth);

    @Path("/id/{groupId}")
    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_PUT,
            value = "Updating a Device Group",
            notes = "If you wish to make changes to an existing group, that can be done by updating the group using this API",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:update")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Group has been updated successfully.",
                    responseHeaders = {
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
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while updating the group.",
                    response = ErrorResponse.class)
    })
    Response updateGroup(@ApiParam(
            name = "groupId",
            value = "ID of the group to be updated.",
            required = true)
                         @PathParam("groupId") int groupId,
                         @ApiParam(
                                 name = "group",
                                 value = "Update the content of the group object.",
                                 required = true)
                         @Valid DeviceGroup deviceGroup);

    @Path("/id/{groupId}")
    @DELETE
    @Consumes(MediaType.WILDCARD)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            consumes = MediaType.WILDCARD,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Deleting a Group",
            notes = "If you wish to remove an existing group, that can be done by updating the group using this API.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:remove")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Group has been deleted successfully.",
                    responseHeaders = {
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
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while removing the group.",
                    response = ErrorResponse.class)
    })
    Response deleteGroup(@ApiParam(
            name = "groupId",
            value = "ID of the group to be deleted.",
            required = true)
                         @PathParam("groupId") int groupId,
                        @ApiParam(
            name = "isDeleteChildren",
            value = "Is the children groups needs to be deleted.",
            required = true)
                        @QueryParam("isDeleteChildren") boolean isDeleteChildren);

    @Path("/id/{groupId}/share")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Sharing a Group",
            notes = "A device group can be shared with different user-roles. The users that belong to that role can " +
                    "then view the groups and the devices in it. Use this API to share a group among user roles.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:roles:share")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Sharing has been updated successfully.",
                    responseHeaders = {
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
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while sharing the group.",
                    response = ErrorResponse.class)
    })
    Response manageGroupSharing(@ApiParam(
            name = "groupId",
            value = "Name of the group to be shared or unshared.",
            required = true)
                                @PathParam("groupId") int groupId,
                                @ApiParam(
                                        name = "userRoles",
                                        value = "User roles to share group with.",
                                        required = true)
                                @Valid List<String> userRoles);

    @Path("/id/{groupId}/roles")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting the List of Roles the Group is Shared With",
            notes = "A device group can be shared with different user-roles. The users that belong to that role can" +
                    "then view the groups and the devices in it. Using this API you get the list of roles the device group is shared with.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:roles:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the users.",
                    response = RoleList.class,
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
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the roles.",
                    response = ErrorResponse.class)
    })
    Response getRolesOfGroup(@ApiParam(
            name = "groupId",
            value = "ID of the group.",
            required = true)
                             @PathParam("groupId") int groupId);

    @Path("/id/{groupId}/devices")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting the List of Devices in a Group",
            notes = "Returns the list of devices in a device group.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:devices:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the devices.",
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
                                    description = "Date and time the resource has been modified the last time.\n" +
                                            "Used by caches, or in conditional requests."),
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. \n Empty body because the client has already the latest version of " +
                            "the requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Group not found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the devices.",
                    response = ErrorResponse.class)
    })
    Response getDevicesOfGroup(@ApiParam(
            name = "groupId",
            value = "ID of the group.",
            required = true)
                               @PathParam("groupId")
                                       int groupId,
                               @ApiParam(
                                       name = "offset",
                                       value = "The starting pagination index for the complete list of qualified items.",
                                       defaultValue = "0")
                               @QueryParam("offset")
                                       int offset,
                               @ApiParam(
                                       name = "limit",
                                       value = "Provide how many group details you require from the starting " +
                                               "pagination index/offset.",
                                       defaultValue = "5")
                               @QueryParam("limit")
                                       int limit,

                               @ApiParam(
                                       name = "requireDeviceProps",
                                       value = "Boolean flag indicating whether to include device properties \n" +
                                               " to the device object.",
                                       required = false)
                               @QueryParam("requireDeviceProps")
                                       boolean requireDeviceProps);

    @Path("/id/{groupId}/devices/count")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting the Number of Devices in a Group",
            notes = "Get the number of devices in a group using this API.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:devices:count")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device count.",
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
                    message = "Internal Server Error. \n Server error occurred while fetching device count.",
                    response = ErrorResponse.class)
    })
    Response getDeviceCountOfGroup(
            @ApiParam(
                    name = "groupId",
                    value = "ID of the group.",
                    required = true)
            @PathParam("groupId") int groupId);

    @Path("/id/{groupId}/devices/add")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Adding Devices to a Group",
            notes = "Add the enrolled devices to a group.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:devices:add")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully add devices to the group.",
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
                    message = "Internal Server Error. \n Server error occurred while adding devices to the group.",
                    response = ErrorResponse.class)
    })
    Response addDevicesToGroup(@ApiParam(
            name = "groupId",
            value = "ID of the group.",
            required = true)
                               @PathParam("groupId") int groupId,
                               @ApiParam(
                                       name = "deviceIdentifiers",
                                       value = "Device identifiers of the devices which needed be added.",
                                       required = true)
                               @Valid List<DeviceIdentifier> deviceIdentifiers);

    @Path("/id/{groupId}/devices/remove")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_DELETE,
            value = "Removing Devices from a Group",
            notes = "Remove a device from a group using this API.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:devices:remove")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully removed devices from the group.",
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
                    message = "Internal Server Error. \n Server error occurred while removing devices from the group.",
                    response = ErrorResponse.class)
    })
    Response removeDevicesFromGroup(@ApiParam(
            name = "groupId",
            value = "ID of the group.",
            required = true)
                                    @PathParam("groupId") int groupId,
                                    @ApiParam(
                                            name = "deviceIdentifiers",
                                            value = "The device identifiers of the devices that needed to be removed."+
                                                    "  You can define many device IDs as comma separated values.",
                                            required = true)
                                    @Valid List<DeviceIdentifier> deviceIdentifiers);

    @Path("/device/assign")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Adding a Device to Many Groups",
            notes = "Add an already enrolled device to many groups, using this API.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:devices:assign")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully assign the device to groups.",
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
                    message = "Internal Server Error. \n Server error occurred while adding devices to the group.",
                    response = ErrorResponse.class)
    })
    Response updateDeviceAssigningToGroups(
            @ApiParam(
                    name = "deviceToGroupsAssignment",
                    value = "In the payload, define the group IDs that you need to add the device to as comma " +
                            "separated values, and the device identifier and type of the device, such as android, " +
                            "ios, and windows, that needs to be added to the groups.",
                    required = true)
            @Valid DeviceToGroupsAssignment deviceToGroupsAssignment);

    @Path("/device")
    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting Device Groups that Include the Specific Device",
            notes = " device can be added to one or many groups. This API gives you the list of groups the device " +
                    "has been added to.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:groups:device:view")
                    })
            },
            nickname = "getGroupsNonAdmin"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK.",
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
                    message = "Internal Server Error. \n Server error occurred.",
                    response = ErrorResponse.class)
    })
    Response getGroups(
            @ApiParam(
                    name = "deviceId",
                    value = "The device ID.",
                    required = true)
            @QueryParam("deviceId")
                    String deviceId,
            @ApiParam(
                    name = "deviceType",
                    value = "The type of the device, such as android, ios, or windows.",
                    required = true)
            @QueryParam("deviceType")
                    String deviceType,
            @ApiParam(
                    name = "requireGroupProps",
                    value = "Request group properties to include in the response",
                    defaultValue = "false")
            @QueryParam("requireGroupProps")
                    boolean requireGroupProps);

    @Path("/device-types")
    @POST
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Getting Details whether the groups has relevant device type or not",
            notes = "Getting Details whether the groups has relevant device type or not.",
            tags = "Device Group Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "gm:devices-types:view")
                    })
            },
            nickname = "getGroupByGroupNameFilter"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device types of groups.",
                    response = DeviceGroup.class,
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
                    message = "Error occurred",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported."),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the group details.",
                    response = ErrorResponse.class)
    })
    Response getGroupHasDeviceTypes(
            @ApiParam(
                    name = "identifiers",
                    value = "GET list of identifiers.",
                    required = true)
            List<String> identifiers);


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
