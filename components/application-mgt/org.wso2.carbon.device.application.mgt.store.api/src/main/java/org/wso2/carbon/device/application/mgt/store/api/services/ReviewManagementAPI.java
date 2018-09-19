/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.store.api.services;

import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Tag;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.publisher.api.beans.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Review;
import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

        /**
        * APIs to handle comment management related tasks.
        */

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Store Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "CommentManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/comments"),
                        })
                }
        ),
        tags = {
                @Tag(name = "store_management", description = "Review Management related "
                        + "APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Comments Details",
                        description = "Get comments details",
                        key = "perm:comment:get",
                        permissions = {"/device-mgt/comment/get"}
                ),
                @Scope(
                        name = "Add a Review",
                        description = "Add a comment",
                        key = "perm:comment:add",
                        permissions = {"/device-mgt/comment/add"}
                ),
                @Scope(
                        name = "Update a Review",
                        description = "Update a Review",
                        key = "perm:comment:update",
                        permissions = {"/device-mgt/comment/update"}
                ),

                @Scope(
                        name = "Delete a Review",
                        description = "Delete a comment",
                        key = "perm:comment:delete",
                        permissions = {"/device-mgt/comment/delete"}
                ),
        }
)

@Path("/review")
@Api(value = "Comments Management", description = "This API carries all comments management related operations " +
        "such as get all the comments, add comment, etc.")
@Produces(MediaType.APPLICATION_JSON)

public interface ReviewManagementAPI {
    String SCOPE = "scope";

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get comments",
            notes = "Get all comments",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:store:get")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved comments.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No activity found with the given ID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the comment list.",
                            response = ErrorResponse.class)
            })

    Response getAllReviews(
            @ApiParam(
                    name="uuid",
                    value="uuid of the released version of application.",
                    required = true)
            @PathParam("uuid")
                    String uuid,
            @ApiParam(
                    name="offSet",
                    value="Starting comment number.",defaultValue = "1",
                    required = false)
            @QueryParam("offSet")
                    int offSet,
            @ApiParam(
                    name="limit",
                    value = "Limit of paginated comments",defaultValue = "20",
                    required = false)
            @QueryParam("limit")
                    int limit);

    @POST
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a review",
            notes = "This will add a new review",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:store:add")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully add a review.",
                            response = Review.class),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred adding a review.",
                            response = ErrorResponse.class)
            })

    Response addReview(
            @ApiParam(
                    name = "review",
                    value = "Review details",
                    required = true) Review review,
            @ApiParam(
                    name="uuid",
                    value="uuid of the release version of the application",
                    required=true)
            @PathParam("uuid")
                    String uuid);

    @PUT
    @Path("/{CommentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Edit a review",
            notes = "This will edit the review",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:store:edit")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully updated review.",
                            response = Review.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No activity found with the given ID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while updating the new review.",
                            response = ErrorResponse.class)
            })
    Response updateComment(
            @ApiParam(
                    name = "review",
                    value = "The review that need to be updated.",
                    required = true)
            @Valid Review review,
            @ApiParam(
                    name="commentId",
                    value = "review id of the updating review.",
                    required = true)
            @QueryParam("commentId")
            int commentId);

    @DELETE
    @Path("/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Remove comment",
            notes = "Remove comment",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:store:remove")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the comment"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No activity found with the given ID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting the comment.",
                            response = ErrorResponse.class)
            })

    Response deleteComment(
                    @ApiParam(
                            name="commentId",
                            value="Id of the comment.",
                            required = true)
                    @PathParam("commentId")
                            int commentId,
            @ApiParam(
                    name="username",
                    value="logged in username",
                    required = true)
            @QueryParam("username") String username);

    @GET
    @Path("/{uuid}/{stars}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get stars",
            notes = "Get all stars",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:stars:get")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved stars.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the stars",
                            response = ErrorResponse.class)
            })

    Response getRating(
            @ApiParam(
                    name = "uuid",
                    value = "uuid of the application release",
                    required = true)
            @PathParam("uuid")
                    String uuid);
}
