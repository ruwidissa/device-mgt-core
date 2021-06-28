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
package org.wso2.carbon.device.application.mgt.publisher.api.services.admin;

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
import org.wso2.carbon.device.application.mgt.common.PaginationResult;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
* APIs to handle admin review management related tasks in publisher.
*/

@SwaggerDefinition(
info = @Info(
        version = "1.0.0",
        title = "Publisher Review Management Admin Service",
        extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = "name", value = "PublisherReviewManagementAdminService"),
                        @ExtensionProperty(name = "context", value = "/api/application-mgt-publisher/v1.0/admin/reviews"),
                })
        }
),
tags = {
        @Tag(name = "review_management", description = "Publisher Review Management related Admin APIs")
}
)
@Scopes(
scopes = {
        @Scope(
                name = "Update a Review",
                description = "Update a Review of application.",
                key = "perm:admin:app:review:update",
                roles = {"Internal/devicemgt-admin"},
                permissions = {"/app-mgt/publisher/admin/review/update"}
        ),
        @Scope(
                name = "Get Review Details",
                description = "Get review details of application.",
                key = "perm:admin:app:review:view",
                roles = {"Internal/devicemgt-admin"},
                permissions = {"/app-mgt/publisher/admin/review/view"}
        )
}
)

@Path("/admin/reviews")
@Api(value = "Publisher Review Management Admin API")
@Produces(MediaType.APPLICATION_JSON)
public interface ReviewManagementPublisherAdminAPI {
String SCOPE = "scope";

    @GET
    @Path("/release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get app release reviews",
            notes = "Get all app release reviews",
            tags = "Review Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:review:view")
                    })
            }
    )

    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved app release reviews.",
                            response = PaginationResult.class,
                            responseContainer = "PaginationResult"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Not found an application release for requested UUID."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the review list.",
                            response = ErrorResponse.class)
            })

    Response getAllReleaseReviews(
            @ApiParam(
                    name = "uuid",
                    value = "uuid of the application release.",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "offset",
                    value = "Starting review number.",
                    defaultValue = "0")
            @QueryParam("offSet") int offSet,
            @ApiParam(
                    name = "limit",
                    value = "Limit of paginated reviews",
                    defaultValue = "20")
            @QueryParam("limit") int limit);

    @GET
    @Path("/{uuid}/release-rating")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get ratings",
            notes = "Get all ratings",
            tags = "Review Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:review:view")
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
                            message = "Not Found. \n No Application release found for application release UUID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting ratings",
                            response = ErrorResponse.class)
            })

    Response getAppReleaseRating(
            @ApiParam(
                    name = "uuid",
                    value = "uuid of the application release",
                    required = true)
            @PathParam("uuid") String uuid);

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
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:review:view")
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
