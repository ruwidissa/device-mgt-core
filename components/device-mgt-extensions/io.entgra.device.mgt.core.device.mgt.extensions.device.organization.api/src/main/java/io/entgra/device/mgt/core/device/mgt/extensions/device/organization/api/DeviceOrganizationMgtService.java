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
package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.api;

import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.api.util.DeviceOrgConstants;
import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.api.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceOrganization;
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

/**
 * This interface defines the RESTful web service endpoints for managing device organizations.
 */

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceOrganizationManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-org/v1.0"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_organization_management", description = "Device organization " +
                        "management related REST-API. This can be used to manipulate device organization related details.")
        }
)

@Api(value = "Device Organization Management", description = "This API carries all device organization management " +
        "related operations.")
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(scopes = {
        @Scope(
                name = "View Device Organization",
                description = "View Device Organization",
                key = "dm:device-org:view",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/device-mgt/organization/view"}
        ),
        @Scope(
                name = "Add Device Organization",
                description = "Add Device Organization",
                key = "dm:device-org:add",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/device-mgt/organization/add"}
        ),
        @Scope(
                name = "Modify Device Organization",
                description = "Modify Device Organization",
                key = "dm:device-org:modify",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/device-mgt/organization/modify"}
        ),
        @Scope(
                name = "Delete Device Organization",
                description = "Delete Device Organization",
                key = "dm:device-org:delete",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/device-mgt/organization/delete"}
        ),
}
)
public interface DeviceOrganizationMgtService {

    String SCOPE = DeviceOrgConstants.SCOPE;

    /**
     * Adds a new device organization.
     *
     * @param request The request containing the device organization information.
     * @return A response indicating the success or failure of the operation.
     */
    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a new device Organization.",
            notes = "This endpoint allows you to add a new device organization.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:add")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created the device organization.",
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
                            }),
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
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response addDeviceOrganization(DeviceOrganization request);

    /**
     * Retrieves a list of child nodes of a given device node, up to a specified depth.
     *
     * @param deviceId      The ID of the parent device node.
     * @param maxDepth      The maximum depth of child nodes to retrieve.
     * @param includeDevice Indicates whether to include device information in the retrieved nodes.
     * @return A response containing a list of child device nodes.
     */
    @GET
    @Path("children")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Child Nodes of a Device Node",
            notes = "This endpoint allows you to retrieve a list of child nodes of a given device node up to a specified depth.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the child devices.",
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
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response getChildrenOfDeviceNode(
            @ApiParam(value = "The ID of the parent device node.", required = true)
            @QueryParam("deviceId") int deviceId,
            @ApiParam(value = "The maximum depth of child nodes to retrieve.", required = true)
            @QueryParam("maxDepth") int maxDepth,
            @ApiParam(value = "Indicates whether to include device information in the retrieved nodes.", required = true)
            @QueryParam("includeDevice") boolean includeDevice
    );


    /**
     * Retrieves a list of parent nodes of a given device node, up to a specified depth.
     *
     * @param deviceId      The ID of the child device node.
     * @param maxDepth      The maximum depth of parent nodes to retrieve.
     * @param includeDevice Indicates whether to include device information in the retrieved nodes.
     * @return A response containing a list of parent device nodes.
     */
    @GET
    @Path("parents")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve Parent Nodes of a Device Node",
            notes = "Get a list of parent nodes of a specified child device node, up to a specified depth.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the parent devices.",
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
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response getParentsOfDeviceNode(
            @ApiParam(value = "The ID of the child device node.", required = true)
            @QueryParam("deviceId") int deviceId,
            @ApiParam(value = "The maximum depth of parent nodes to retrieve.", required = true)
            @QueryParam("maxDepth") int maxDepth,
            @ApiParam(value = "Indicates whether to include device information in the retrieved nodes.", required = true)
            @QueryParam("includeDevice") boolean includeDevice);


    /**
     * Retrieves a list of leaf device organizations.
     *
     * @return A response containing a list of leaf device organizations.
     */
    @GET
    @Path("leafs")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve leaf Device Organizations",
            notes = "Get a list of leaf device organizations.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the all devices.",
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
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response getDeviceOrganizationLeafs(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items",
                    required = false,
                    defaultValue = "0")
            @DefaultValue("0") @QueryParam("offset")
            int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many policy details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @DefaultValue("20") @QueryParam("limit")
            int limit);

    /**
     * Retrieves a list of root device organizations.
     *
     * @return A response containing a list of root device organizations.
     */
    @GET
    @Path("roots")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve leaf Device Organizations",
            notes = "Get a list of leaf device organizations.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the all devices.",
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
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response getDeviceOrganizationRoots(
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items",
                    required = false,
                    defaultValue = "0")
            @DefaultValue("0") @QueryParam("offset")
            int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many policy details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @DefaultValue("20") @QueryParam("limit")
            int limit);

    /**
     * Retrieves a specific device organization by its organization ID.
     *
     * @param organizationId The organization ID of the device organization to retrieve.
     * @return A response containing the device organization with the specified ID.
     */
    @GET
    @Path("{organizationId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Retrieve Device Organization by ID",
            notes = "Get a specific device organization by its ID.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the device organization by ID.",
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
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response getDeviceOrganizationById(@PathParam("organizationId") int organizationId);


    /**
     * Checks if a device organization with the specified device and parent device IDs already exists.
     *
     * @param deviceId       The ID of the device.
     * @param parentDeviceId The ID of the parent device.
     * @return A response indicating whether the organization exists or not.
     */
    @GET
    @Path("exists/{deviceId}/{parentDeviceId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Check Device Organization Existence",
            notes = "Check if a device organization with the specified device and parent device IDs exists.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfull. The device organization exists.",
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
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response isDeviceOrganizationExist(
            @ApiParam(name = "deviceId", value = "The ID of the child device.", required = true)
            @PathParam("deviceId") int deviceId,
            @ApiParam(name = "parentDeviceId", value = "The ID of the parent device.")
            @PathParam("parentDeviceId") String parentDeviceId);


    /**
     * Retrieve a device organization by its unique key (deviceId and parentDeviceId).
     *
     * @param deviceId       The ID of the device.
     * @param parentDeviceId The ID of the parent device.
     * @return A response containing the retrieved DeviceOrganization object, or null if not found.
     */
    @GET
    @Path("organization/{deviceId}/{parentDeviceId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Device Organization by Unique Key",
            notes = "Retrieve a device organization by its unique key, which is a combination of deviceId and parentDeviceId.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved organization details.",
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
                            code = 404,
                            message =
                                    "Not Found. \n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            })
    Response getDeviceOrganizationByUniqueKey(
            @ApiParam(name = "deviceId", value = "The ID of the child device.", required = true)
            @PathParam("deviceId") int deviceId,
            @ApiParam(name = "parentDeviceId", value = "The ID of the parent device.")
            @PathParam("parentDeviceId") String parentDeviceId);

    /**
     * Updates a device organization.
     *
     * @param deviceOrganization The updated device organization.
     * @return A response indicating the success or failure of the operation.
     */
    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update a Device Organization",
            notes = "This endpoint allows you to update a device organization.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated device organization.",
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
    Response updateDeviceOrganization(
            @ApiParam(value = "The updated device organization.", required = true)
            DeviceOrganization deviceOrganization);


    /**
     * Deletes a device organization by its organization ID.
     *
     * @param organizationId The organization ID of the device organization to delete.
     * @return A response indicating the success or failure of the operation.
     */
    @DELETE
    @Path("{organizationId}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Delete a Device Organization",
            notes = "This endpoint allows you to delete a device organization by its organization ID.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the device organization.",
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
    Response deleteDeviceOrganizationById(
            @ApiParam(value = "The organization ID of the device organization to delete.", required = true)
            @PathParam("organizationId") int organizationId);

    /**
     * Deletes records associated with a particular device ID from the device organization table.
     * This method deletes records where the provided device ID matches either the deviceID column or
     * parentDeviceID column in the device organization table.
     *
     * @param deviceId The ID of the device for which associations should be deleted.
     * @return A response indicating the success or failure of the operation.
     */
    @DELETE
    @Path("associations/{deviceId}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Delete Device Associations",
            notes = "This endpoint allows you to delete records associated with a particular device ID from the device organization table.",
            tags = "Device Organization Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:device-org:delete")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted device organizations.",
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
    Response deleteDeviceAssociations(
            @ApiParam(name = "deviceId", value = "The ID of the device for which associations should be deleted.", required = true)
            @PathParam("deviceId") int deviceId);


}
