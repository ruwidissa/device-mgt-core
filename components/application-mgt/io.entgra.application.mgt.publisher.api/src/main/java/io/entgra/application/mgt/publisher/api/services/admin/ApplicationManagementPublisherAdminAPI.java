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
package io.entgra.application.mgt.publisher.api.services.admin;

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
import io.entgra.application.mgt.common.ApplicationList;
import io.entgra.application.mgt.common.ErrorResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
 * APIs to handle application management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "ApplicationDTO Management Publisher Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationManagementPublisherAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-publisher/v1.0/admin/applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "App publisher related Admin APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Delete Application Release",
                        description = "Delete Application Release",
                        key = "perm:admin:app:publisher:update",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/app-mgt/publisher/admin/application/update"}
                )
        }
)
@Path("/admin/applications")
@Api(value = "ApplicationDTO Management")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationManagementPublisherAdminAPI {

    String SCOPE = "scope";

    @DELETE
    @Path("/release/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete application release.",
            notes = "This will delete application release for given UUID",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully delete application release.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. There doesn't have an application release for UUID" +
                                    "query."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting application release.",
                            response = ErrorResponse.class)
            }) Response deleteApplicationRelease(
            @ApiParam(
                    name = "uuid",
                    value = "application release UUID",
                    required = true)
            @PathParam("uuid") String releaseUuid);

    @DELETE
    @Path("/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete application release.",
            notes = "This will delete application release for given UUID",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully delete application release.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. There doesn't have an application release for UUID" +
                                    "query."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting application release.",
                            response = ErrorResponse.class)
            }) Response deleteApplication(
            @ApiParam(
                    name = "appId",
                    value = "application ID",
                    required = true)
            @PathParam("appId") int applicatioId);

    @DELETE
    @Path("/tags/{tagName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Delete application tag",
            notes = "This will delete application tag",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully delete  registered tag.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting registered tag.",
                            response = ErrorResponse.class)
            })
    Response deleteTag(
            @ApiParam(
                    name = "tagName",
                    value = "Tag Name",
                    required = true)
            @PathParam("tagName") String tagName
    );

    @POST
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add new application categories.",
            notes = "This will add new application categories",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully delete  registered tag.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Category list is either empty or null"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting registered tag.",
                            response = ErrorResponse.class)
            })
    Response addCategories(
            @ApiParam(
                    name = "tagName",
                    value = "Tag Name",
                    required = true) List<String> categorynames
    );

    @PUT
    @Path("/categories/rename")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update application category",
            notes = "This will update application category.",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully delete  registered category.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. There doesn't have an category for given category name.."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting registered category.",
                            response = ErrorResponse.class)
            })
    Response renameCategory(
            @ApiParam(
                    name = "oldCategoryName",
                    value = "Existing Category Name",
                    required = true)
            @QueryParam("from") String oldCategoryName,
            @ApiParam(
                    name = "newCategoryName",
                    value = "Modifying Category Name",
                    required = true)
            @QueryParam("to") String newCategoryName
    );

    @DELETE
    @Path("/categories/{categoryName}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete application category",
            notes = "This will delete application category.",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted  registered category.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting registered category.",
                            response = ErrorResponse.class)
            })
    Response deleteCategory(
            @ApiParam(
                    name = "categoryName",
                    value = "Category Name",
                    required = true)
            @PathParam("categoryName") String categoryName
    );

    @PUT
    @Consumes("application/json")
    @Path("/retire/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Retire the application with the given app Id",
            notes = "This will retire the application with the given app Id",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the application identified by app Id.",
                            response = List.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting the application.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to delete the application"),
                    @ApiResponse(
                            code = 404,
                            message = "Application not found"),
            })
    Response retireApplication(
            @ApiParam(
                    name = "appId",
                    value = "Application Id",
                    required = true)
            @PathParam("appId") int applicationId
    );
}
