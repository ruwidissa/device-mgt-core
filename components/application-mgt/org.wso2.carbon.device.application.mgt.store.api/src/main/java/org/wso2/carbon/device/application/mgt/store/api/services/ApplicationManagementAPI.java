/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/store-applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "Application Storage Management "
                        + "related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Application Details",
                        description = "Get application details",
                        key = "perm:app:store:view",
                        permissions = {"/device-mgt/application/get"}
                )
        }
)
@Path("/store/applications")
@Api(value = "Application Management", description = "This API carries all app store management related operations " +
        "such as get all the applications etc.")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationManagementAPI {

    String SCOPE = "scope";

    @GET
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
                            @ExtensionProperty(name = SCOPE, value = "perm:application:get")
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
                            code = 404,
                            message = "Not Found. Not Found Applications."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response getApplications(
            @ApiParam(
                    name = "name",
                    value = "Name of the application")
            @QueryParam("name") String appName,
            @ApiParam(
                    name = "type",
                    value = "Type of the application")
            @QueryParam("type") String appType,
            @ApiParam(
                    name = "category",
                    value = "Category of the application")
            @QueryParam("category") String appCategory,
            @ApiParam(
                    name = "exact-match",
                    value = "Is it requesting exactly matching application or partially matching application.")
            @QueryParam("exact-match") boolean isFullMatch,
            @ApiParam(
                    name = "offset",
                    value = "Provide from which position apps should return", defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many apps it should return", defaultValue = "20")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many apps it should return", defaultValue = "ASC")
            @QueryParam("sort") String sortBy

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
                            @ExtensionProperty(name = SCOPE, value = "perm:application:get")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved relevant application.",
                            response = Application.class),
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
