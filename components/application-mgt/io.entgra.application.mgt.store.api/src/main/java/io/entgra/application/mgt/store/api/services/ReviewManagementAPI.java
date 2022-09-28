/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.store.api.services;

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
import io.entgra.application.mgt.common.PaginationResult;
import io.entgra.application.mgt.common.ErrorResponse;
import io.entgra.application.mgt.common.response.Review;
import io.entgra.application.mgt.common.wrapper.ReviewWrapper;

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
* APIs to handle review management related tasks.
*/

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Review Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ReviewManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-store/v1.0/reviews"),
                        })
                }
        ),
        tags = {
                @Tag(name = "review_management", description = "Review Management related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Review Details",
                        description = "Get review details from application store.",
                        key = "perm:app:review:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/store/review/view"}
                ),
                @Scope(
                        name = "Update a Review",
                        description = "Update a Review from the application store.",
                        key = "perm:app:review:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/store/review/update"}
                ),
        }
)

@Path("/reviews")
@Api(value = "Review Management API")
@Produces(MediaType.APPLICATION_JSON)
public interface ReviewManagementAPI {
    String SCOPE = "scope";

    @GET
    @Path("/app/user/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get app reviews",
            notes = "Get all app reviews",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:review:view")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved app reviews.",
                            response = PaginationResult.class,
                            responseContainer = "PaginationResult"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Not found an application release associated with requested "
                                    + "UUID."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the review list.",
                            response = ErrorResponse.class)
            })

    Response getUserReviews(
            @ApiParam(
                    name="uuid",
                    value="uuid of the application release.",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name="offset",
                    value="Starting review number.",
                    defaultValue = "0")
            @QueryParam("offSet") int offSet,
            @ApiParam(
                    name="limit",
                    value = "Limit of paginated reviews",
                    defaultValue = "20")
            @QueryParam("limit") int limit);

    @GET
    @Path("/app/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get app reviews",
            notes = "Get all app reviews",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:review:view")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved app reviews.",
                            response = PaginationResult.class,
                            responseContainer = "PaginationResult"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Not found an application release associated with requested "
                                    + "UUID."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the review list.",
                            response = ErrorResponse.class)
            })

    Response getAllAppReviews(
            @ApiParam(
                    name="uuid",
                    value="uuid of the application release.",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name="offset",
                    value="Starting review number.",
                    defaultValue = "0")
            @QueryParam("offSet") int offSet,
            @ApiParam(
                    name="limit",
                    value = "Limit of paginated reviews",
                    defaultValue = "20")
            @QueryParam("limit") int limit);

    @POST
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a review",
            notes = "This will add a new review for application release.",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:review:update")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully add a reviewTmp.",
                            response = Review.class),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n Found invalid payload with the request."),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to add a review."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Not found an application release for requested UUID."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred adding a reviewTmp.",
                            response = ErrorResponse.class)
            })

    Response addReview(
            @ApiParam(
                    name = "reviewTmp",
                    value = "Review details",
                    required = true) ReviewWrapper reviewWrapper,
            @ApiParam(
                    name="uuid",
                    value="uuid of the application release.",
                    required=true)
            @PathParam("uuid") String uuid);

    @POST
    @Path("/{uuid}/{parentReviewId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a reply comment",
            notes = "This will add a reply comment for a comment or review.",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:review:update")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully add a reviewTmp.",
                            response = Review.class),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n Found invalid payload with the request."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Not found an application release for requested UUID."),

                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred adding a reviewTmp.",
                            response = ErrorResponse.class)
            })

    Response addReplyComment(
            @ApiParam(
                    name = "review",
                    value = "Reply comment details",
                    required = true) ReviewWrapper reviewWrapper,
            @ApiParam(
                    name="uuid",
                    value="uuid of the application release.",
                    required=true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name="parentReviewId",
                    value="uuid of the application release.",
                    required=true)
            @PathParam("parentReviewId") int parentReviewId);

    @PUT
    @Path("/{uuid}/{reviewId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Edit a reviewTmp",
            notes = "This will edit the reviewTmp",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:review:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated reviewTmp.",
                            response = Review.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while updating the new reviewTmp.",
                            response = ErrorResponse.class)
            })
    Response updateReview(
            @ApiParam(
                    name = "reviewTmp",
                    value = "The reviewTmp that need to be updated.",
                    required = true)
            @Valid ReviewWrapper updatingReview,
            @ApiParam(
                    name="uuid",
                    value = "uuid of the application release",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name="reviewId",
                    value = "reviewTmp id of the updating reviewTmp.",
                    required = true)
            @PathParam("reviewId") int reviewId);

    @DELETE
    @Path("/{uuid}/{reviewId}")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:review:update")
                    })
            },
            nickname = "deleteReviewComment"
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the review"),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to delete the review."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No activity found with the given ID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting the review.",
                            response = ErrorResponse.class)
            })

    Response deleteReview(
            @ApiParam(
                    name="uuid",
                    value="UUID of the application release.",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name="reviewId",
                    value="Id of the review.",
                    required = true)
            @PathParam("reviewId") int reviewId);

    @GET
    @Path("/{uuid}/app-rating")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get app ratings",
            notes = "Get all app ratings",
            tags = "Store Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:review:view")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved ratings.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No Application found which has application release of UUID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting ratings",
                            response = ErrorResponse.class)
            })

    Response getAppRating(
            @ApiParam(
                    name = "uuid",
                    value = "uuid of the application release",
                    required = true)
            @PathParam("uuid")
                    String uuid);
}
