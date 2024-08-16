/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.TagInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.TagInfoList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.TagMappingInfo;
import io.swagger.annotations.*;
import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;

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
                                @ExtensionProperty(name = "name", value = "TagManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/tags"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Getting the List of Tags",
                        description = "Getting the List of Tags",
                        key = "tm:tags:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/tags/view"}
                ),
                @Scope(
                        name = "Adding a new tag",
                        description = "Adding a new tag",
                        key = "tm:tags:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/tags/create"}
                ),
                @Scope(
                        name = "Updating a tag",
                        description = "Updating a tag",
                        key = "tm:tags:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/tags/update"}
                ),
                @Scope(
                        name = "Delete a tag",
                        description = "Delete a tag",
                        key = "tm:tags:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/tags/delete"}
                ),
                @Scope(
                        name = "Adding a device tag mapping",
                        description = "Adding a device-tag mapping",
                        key = "tm:tags:mapping:create",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/tags/mapping/create"}
                ),
                @Scope(
                        name = "Deleting a device tag mapping",
                        description = "Deleting a device-tag mapping",
                        key = "tm:tags:mapping:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/tags/mapping/delete"}
                ),
                @Scope(
                        name = "Getting the list of device tag mappings",
                        description = "Getting the list of device-tag mappings",
                        key = "tm:tags:mapping:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/tags/mapping/view"}
                )
        }
)
@Path("/tags")
@Api(value = "Tag Management", description = "Tag management related operations can be found here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface TagManagementService {

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the List of Tags",
            notes = "This endpoint is used to retrieve all tags",
            tags = "Tag Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "tm:tags:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of tags.",
                            response = TagInfoList.class,
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
                            message = "Not Modified. \n Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching list of roles.",
                            response = ErrorResponse.class)
            })
    Response getTags();

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Tag",
            notes = "This endpoint is used to add new tags",
            tags = "Tag Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "tm:tags:create")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully created the tag.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The URL to the newly added tag."),
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
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 303,
                    message = "See Other. \n The source can be retrieved from the URL specified in the location header.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The Source URL of the document.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding a new tag.",
                    response = ErrorResponse.class)
    })
    Response addTag(
            @ApiParam(
                    name = "tag",
                    value = "The properties required to add a new tag.",
                    required = true) List<TagInfo> tags);

    @PUT
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating a Tag",
            notes = "This endpoint is used to update a specific tag",
            tags = "Tag Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "tm:tags:update")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the tag.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified tag does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while updating the tag.",
                    response = ErrorResponse.class)
    })
    Response updateTag(
            @ApiParam(
                    name = "tagId",
                    value = "The ID of the tag to be updated.",
                    required = false) @QueryParam("tagId") Integer tagId,
            @ApiParam(
                    name = "tagName",
                    value = "The name of the tag to be updated.",
                    required = false) @QueryParam("tagName") String tagName,
            @ApiParam(
                    name = "tagInfo",
                    value = "The properties required to update the tag.",
                    required = true) TagInfo tagInfo);

    @DELETE
    @Path("/{tagId}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Deleting a Tag",
            notes = "This endpoint is used to delete a specific tag",
            tags = "Tag Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "tm:tags:delete")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 204,
                    message = "No Content. \n Successfully deleted the tag."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified tag does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while deleting the tag.",
                    response = ErrorResponse.class)
    })
    Response deleteTag(
            @ApiParam(
                    name = "tagId",
                    value = "The ID of the tag to be deleted.",
                    required = true) @PathParam("tagId") int tagId);

    @GET
    @Path("/{tagId}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting a Tag by ID",
            notes = "This endpoint is used to retrieve tag by id",
            tags = "Tag Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "tm:tags:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the tag.",
                    response = Tag.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")}),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified tag does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the tag.",
                    response = ErrorResponse.class)
    })
    Response getTagById(
            @ApiParam(
                    name = "tagId",
                    value = "The ID of the tag to be fetched.",
                    required = true) @PathParam("tagId") int tagId);

    @POST
    @Path("/mapping")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a Device-Tag Mapping",
            notes = "This endpoint is used to map devices with tags",
            tags = "Device-Tag Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "tm:tags:mapping:create")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 201,
                    message = "Created. \n Successfully created the device-tag mapping.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Location",
                                    description = "The URL to the newly added device-tag mapping."),
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while adding the device-tag mapping.",
                    response = ErrorResponse.class)
    })
    Response addDeviceTagMapping(
            @ApiParam(
                    name = "deviceTagInfo",
                    value = "The properties required to add a new device-tag mapping.",
                    required = true) TagMappingInfo tagMappingInfo);

    @DELETE
    @Path("/mapping")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting a Device-Tag Mapping",
            notes = "This endpoint is used to remove tag mappings from devices",
            tags = "Device-Tag Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "tm:tags:mapping:delete")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 204,
                    message = "No Content. \n Successfully deleted the device-tag mapping."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while deleting the device-tag mapping.",
                    response = ErrorResponse.class)
    })
    Response deleteDeviceTagMapping(
            @ApiParam(
                    name = "deviceTagInfo",
                    value = "The properties required to add a new device-tag mapping.",
                    required = true) TagMappingInfo tagMappingInfo);
}
