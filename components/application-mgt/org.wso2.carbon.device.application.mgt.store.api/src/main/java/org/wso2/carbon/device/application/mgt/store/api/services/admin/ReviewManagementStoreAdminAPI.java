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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
* APIs to handle admin review management related tasks in store.
*/

@SwaggerDefinition(
info = @Info(
        version = "1.0.0",
        title = "Store Review Management Admin Service",
        extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = "name", value = "StoreReviewManagementAdminService"),
                        @ExtensionProperty(name = "context", value = "/api/application-mgt-store/v1.0/admin/reviews"),
                })
        }
),
tags = {
        @Tag(name = "review_management", description = "Store Review Management related Admin APIs")
}
)
@Scopes(
scopes = {
        @Scope(
                name = "Update a Review",
                description = "Update a Review of applications.",
                key = "perm:admin:app:review:update",
                roles = {"Internal/devicemgt-admin"},
                permissions = {"/app-mgt/store/admin/review/update"}
        )
}
)

@Path("/admin/reviews")
@Api(value = "Store Review Management Admin API")
public interface ReviewManagementStoreAdminAPI {
String SCOPE = "scope";

    @DELETE
    @Path("/{uuid}/{reviewId}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.WILDCARD)
    @ApiOperation(
        consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON,
        httpMethod = "DELETE",
        value = "Remove review",
        notes = "Remove review",
        tags = "Review Management",
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
            @ApiParam(
                    name = "reviewId",
                    value = "Id of the review.",
                    required = true)
            @PathParam("reviewId") int reviewId);
}
