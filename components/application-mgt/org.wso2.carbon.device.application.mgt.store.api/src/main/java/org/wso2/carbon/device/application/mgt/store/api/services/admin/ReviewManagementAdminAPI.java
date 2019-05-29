/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.store.api.services.admin;

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
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.common.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.response.Review;
import org.wso2.carbon.device.application.mgt.common.wrapper.ReviewWrapper;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
* APIs to handle review management related tasks.
*/

@SwaggerDefinition(
info = @Info(
        version = "1.0.0",
        title = "Admin Review Management Admin Service",
        extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = "name", value = "ReviewManagementAdminService"),
                        @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/admin/review"),
                })
        }
),
tags = {
        @Tag(name = "review_management", description = "Review Management related Admin APIs")
}
)
@Scopes(
scopes = {
        @Scope(
                name = "Update a Review",
                description = "Update a Review from the application store.",
                key = "perm:admin:app:review:update",
                permissions = {"/app-mgt/store/admin/review/update"}
        )
}
)

@Path("/admin/reviews")
@Api(value = "Review Management Admin API")
@Produces(MediaType.APPLICATION_JSON)
public interface ReviewManagementAdminAPI {
String SCOPE = "scope";

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
                    @ExtensionProperty(name = SCOPE, value = "perm:admin:app:review:update")
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
                name = "uuid",
                value = "uuid of the application release",
                required = true)
        @PathParam("uuid") String uuid,
        @ApiParam(
                name = "reviewId",
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
                    @ExtensionProperty(name = SCOPE, value = "perm:admin:app:review:update")
            })
    }
)

@ApiResponses(
    value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully deleted the review"),
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
                name = "uuid",
                value = "UUID of the application release.",
                required = true)
        @PathParam("uuid") String uuid,
        @ApiParam(name = "reviewId",
                value = "Id of the review.",
                required = true)
        @PathParam("reviewId") int reviewId);


}
