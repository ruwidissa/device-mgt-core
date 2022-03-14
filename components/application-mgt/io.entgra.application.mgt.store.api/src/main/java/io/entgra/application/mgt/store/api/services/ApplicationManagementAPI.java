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
package io.entgra.application.mgt.store.api.services;

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
import io.entgra.application.mgt.common.ErrorResponse;
import io.entgra.application.mgt.common.Filter;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.common.ApplicationList;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * APIs to handle application storage management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Application Storage Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationStorageManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-store/v1.0/applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "ApplicationDTO Storage Management "
                        + "related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Application Details",
                        description = "Get application details",
                        key = "perm:app:store:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/store/application/view"}
                ),
                @Scope(
                        name = "Modify Application",
                        description = "Modify application state",
                        key = "perm:app:store:modify",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/store/application/modify"}
                )
        }
)
@Path("/applications")
@Api(value = "Application Management", description = "This API carries all app store management related operations such"
        + " as get all the applications etc.")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationManagementAPI {

    String SCOPE = "scope";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/favourite/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "add application to favourites",
            notes = "This will add application to favourites",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:store:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully added application to favourites.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while adding the application to favourites.",
                            response = ErrorResponse.class)
            })
    Response addAppToFavourite(
            @ApiParam(
                    name = "appId",
                    value = "id of the application",
                    required = true)
            @PathParam("appId") int appId);

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/favourite/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "remove application from favourites",
            notes = "This will removing an application from favourites",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:store:modify")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully removed application from favourites.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while removing the application from favourites.",
                            response = ErrorResponse.class)
            })
    Response removeAppFromFavourite(
            @ApiParam(
                    name = "appId",
                    value = "id of the application",
                    required = true)
            @PathParam("appId") int appId);

    @POST
    @Path("/favourite")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all favourite applications",
            notes = "This will get all favourite applications",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:store:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got application list.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Application retrieving request payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response getFavouriteApplications(
            @ApiParam(
                    name = "filter",
                    value = "Application filtering data",
                    required = true)
            @Valid Filter filter);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all applications",
            notes = "This will get all applications",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:store:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got application list.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Application retrieving request payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response getApplications(
            @ApiParam(
                    name = "filter",
                    value = "Application filtering data",
                    required = true)
            @Valid Filter filter
    );

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get the application of requesting application type",
            notes = "This will get the application identified by the application type and name, if exists",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:store:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved relevant application.",
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 404,
                            message = "Application not found"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting relevant application.",
                            response = ErrorResponse.class)
            })
    Response getApplication(
            @ApiParam(
                    name = "uuid",
                    value = "Type of the application",
                    required = true)
            @PathParam("uuid") String uuid
    );


}
