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
package org.wso2.carbon.device.application.mgt.publisher.api.services;

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
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.publisher.api.beans.ErrorResponse;

import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
 * APIs to handle application management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Application Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "Application Management related "
                        + "APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get Application Details",
                        description = "Get application details",
                        key = "perm:app:publisher:view",
                        permissions = {"/device-mgt/application/view"}
                ),
                @Scope(
                        name = "Update an Application",
                        description = "Update an application",
                        key = "perm:app:publisher:update",
                        permissions = {"/device-mgt/application/update"}
                )
        }
)
@Path("/publisher/applications")
@Api(value = "Application Management", description = "This API carries all application management related operations " +
        "such as get all the applications, add application, etc.")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:view")
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
                            message = "Not Found. There doesn't have an application which is matched with requested " +
                                    "query."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response getApplications(
            @ApiParam(
                    name = "filter",
                    value = "Filter to get application list",
                    required = true)
            @Valid Filter filter,
            @ApiParam(
                    name = "offset",
                    value = "offset",
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "limit",
                    defaultValue = "20")
            @QueryParam("limit") int limit
    );

    @GET
    @Path("/{appType}")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:view")
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
                    name = "appType",
                    value = "Type of the application",
                    required = true)
            @PathParam("appType") String appType,
            @ApiParam(
                    name = "appName",
                    value = "Application name",
                    required = true)
            @QueryParam("isWithImages") String appName
    );

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Edit an application",
            notes = "This will edit the new application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully edited the application.",
                            response = Application.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Application updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while editing the application.",
                            response = ErrorResponse.class)
            })
    Response updateApplication(
            @ApiParam(
                    name = "application",
                    value = "The application that need to be edited.",
                    required = true)
            @Valid Application application
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application.",
                            response = Application.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Application creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createApplication(
            @ApiParam(
                    name = "application",
                    value = "The application that need to be created.",
                    required = true)
            @Valid Application application,
            @ApiParam(
                    name = "binaryFile",
                    value = "Binary file of uploading application",
                    required = true)
            @Multipart(value = "binaryFile") Attachment binaryFile,
            @ApiParam(
                    name = "icon",
                    value = "Icon of the uploading application",
                    required = true)
            @Multipart(value = "icon") Attachment iconFile,
            @ApiParam(
                    name = "banner",
                    value = "Banner of the uploading application",
                    required = true)
            @Multipart(value = "banner") Attachment bannerFile,
            @ApiParam(
                    name = "screenshot",
                    value = "Screen Shots of the uploading application",
                    required = true)
            @Multipart(value = "screenshot") List<Attachment> attachmentList
    );

    @DELETE
    @Consumes("application/json")
    @Path("/{appid}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete the application with the given UUID",
            notes = "This will delete the application with the given UUID",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully deleted the application identified by UUID.",
                            response = List.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleteing the application.",
                            response = ErrorResponse.class)
            })
    Response deleteApplication(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the Application",
                    required = true)
            @PathParam("appid") int applicationId
    );

    @POST
    @Path("/image-artifacts/{appId}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
            notes = "This will create a new application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully updated artifacts."),
                    @ApiResponse(
                            code = 403,
                            message = "FORBIDDEN. \n Can't Update the application release in PUBLISHED or DEPRECATED "
                                    + "state. Hence please demote the application and update the application release"),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Error occurred while updating the application."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationImageArtifacts(
            @ApiParam(name = "appId", value = "ID of the application", required = true)
            @PathParam("appId") int applicatioId,
            @ApiParam(name = "uuid", value = "UUID of the application", required = true)
            @PathParam("uuid") String applicationUUID,
            @Multipart(value = "icon") Attachment iconFile,
            @Multipart(value = "banner") Attachment bannerFile,
            @Multipart(value = "screenshot") List<Attachment> screenshots
    );

    @PUT
    @Path("/app-artifacts/{deviceType}/{appType}/{appId}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
            notes = "This will create a new application",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully uploaded artifacts."),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Application artifact updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationArtifact(
            @ApiParam(name = "deviceType", value = "Type of the device i.e Android, IOS etc", required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(name = "appType", value = "Type of the application i.e Mobile, WEB, WEB-CLIP etc", required = true)
            @PathParam("appType") String appType,
            @ApiParam(name = "appId", value = "Id of the application", required = true)
            @PathParam("appId") int applicationId,
            @ApiParam(name = "uuid", value = "UUID of the application", required = true)
            @PathParam("uuid") String applicationUUID,
            @Multipart("binaryFile") Attachment binaryFile
    );

    @PUT
    @Path("/{appId}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update an application release",
            notes = "This will update a new application release",
            tags = "Application Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully created an application release.",
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Application release updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationRelease(
            @ApiParam(name = "appId", value = "Identifier of the Application", required = true) @PathParam("appId") int applicationId,
            @ApiParam(name = "UUID", value = "Unique identifier of the Application Release", required = true) @PathParam("uuid") String applicationUUID,
            @Multipart(value = "applicationRelease", required = false, type = "application/json") ApplicationRelease applicationRelease,
            @Multipart(value = "binaryFile", required = false) Attachment binaryFile,
            @Multipart(value = "icon", required = false) Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart(value = "screenshot", required = false) List<Attachment> attachmentList);

    @GET
    @Path("/lifecycle/{appId}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get lifecycle states",
            notes = "Get all lifecycle states",
            tags = "Lifecycle Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved lifecycle states.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the lifecycle list.",
                            response = ErrorResponse.class)
            })
    Response getLifecycleState(@PathParam("appId") int applicationId,
                               @PathParam("uuid") String applicationUuid);

    @POST
    @Path("/lifecycle/{appId}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a lifecycle state",
            notes = "This will add a new lifecycle state",
            tags = "Lifecycle Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "OK. \n Successfully add a lifecycle state.",
                            response = Application.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred adding a lifecycle state.",
                            response = ErrorResponse.class)
            })
    Response addLifecycleState(@PathParam("appId") int applicationId,
                               @PathParam("uuid") String applicationUuid,
                               LifecycleState state);
}
