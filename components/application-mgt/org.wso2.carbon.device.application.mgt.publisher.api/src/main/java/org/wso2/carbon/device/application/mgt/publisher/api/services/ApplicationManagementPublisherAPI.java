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
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.response.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;

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
                title = "ApplicationDTO Management Publisher Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationManagementPublisherService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-publisher/v1.0/applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "App publisher related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Get ApplicationDTO Details",
                        description = "Get application details",
                        key = "perm:app:publisher:view",
                        permissions = {"/app-mgt/publisher/application/view"}
                ),
                @Scope(
                        name = "Update an ApplicationDTO",
                        description = "Update an application",
                        key = "perm:app:publisher:update",
                        permissions = {"/app-mgt/publisher/application/update"}
                )
        }
)
@Path("/applications")
@Api(value = "ApplicationDTO Management")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationManagementPublisherAPI {

    String SCOPE = "scope";

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get all applications",
            notes = "This will get all applications",
            tags = "ApplicationDTO Management",
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
            }) Response getApplications(
                    @ApiParam(
                            name = "Filter",
                            value = "Get application filter",
                            required = true)
                    @Valid Filter filter);

    @GET
    @Path("/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "get the application of requesting application id and  state",
            notes = "This will get the application identified by the application id and state, if exists",
            tags = "ApplicationDTO Management",
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
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to access the application"),
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
                    name = "appId",
                    value = "application Id",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "state",
                    value = "state")
            @QueryParam("state") String state
    );

    @PUT
    @Path("/{appId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Edit an application",
            notes = "This will edit the new application",
            tags = "ApplicationDTO Management",
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
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while editing the application.",
                            response = ErrorResponse.class)
            })
    Response updateApplication(
            @ApiParam(
                    name = "appId",
                    value = "application Id",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "application",
                    value = "Application data that need to be edited.",
                    required = true)
            @Valid ApplicationUpdateWrapper applicationUpdateWrapper
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("multipart/mixed")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
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
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
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
            @Multipart("application") ApplicationWrapper application,
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
                    name = "screenshot1",
                    value = "Screen Shots of the uploading application",
                    required = true)
            @Multipart(value = "screenshot1") Attachment screenshot1,
            @ApiParam(
                    name = "screenshot2",
                    value = "Screen Shots of the uploading application",
                    required = false)
            @Multipart(value = "screenshot2") Attachment screenshot2,
            @ApiParam(
                    name = "screenshot3",
                    value = "Screen Shots of the uploading application",
                    required = false)
            @Multipart(value = "screenshot3") Attachment screenshot3
    );

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("multipart/mixed")
    @Path("/{appType}/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Create an application",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
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
                            response = ApplicationRelease.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO creating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while creating the application.",
                            response = ErrorResponse.class)
            })
    Response createRelease(
            @ApiParam(
                    name = "appType",
                    value = "Application Type.",
                    required = true)
            @PathParam("appType") String appType,
            @ApiParam(
                    name = "appId",
                    value = "Id of the application.",
                    required = true)
            @PathParam("appId") int appId,
            @ApiParam(
                    name = "applicationRelease",
                    value = "The application release that need to be created.",
                    required = true)
            @Multipart("applicationRelease") ApplicationReleaseWrapper applicationReleaseWrapper,
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
                    name = "screenshot1",
                    value = "Screen Shots of the uploading application",
                    required = true)
            @Multipart(value = "screenshot1") Attachment screenshot1,
            @ApiParam(
                    name = "screenshot2",
                    value = "Screen Shots of the uploading application",
                    required = false)
            @Multipart(value = "screenshot2") Attachment screenshot2,
            @ApiParam(
                    name = "screenshot3",
                    value = "Screen Shots of the uploading application",
                    required = false)
            @Multipart(value = "screenshot3") Attachment screenshot3
    );

    @DELETE
    @Consumes("application/json")
    @Path("/{appId}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Delete the application with the given UUID",
            notes = "This will delete the application with the given UUID",
            tags = "ApplicationDTO Management",
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
                            message = "Internal Server Error. \n Error occurred while deleting the application.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to delete the application"),
                    @ApiResponse(
                            code = 404,
                            message = "Application not found"),
            })
    //todo add new scope and permission
    Response deleteApplication(
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the ApplicationDTO",
                    required = true)
            @PathParam("appId") int applicationId
    );

    @PUT
    @Path("/image-artifacts/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("multipart/mixed")
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
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
                            code = 400,
                            message = "Bad Request. \n Requesting to update image artifacts with invalid application "
                                    + "or application release data."),
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
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
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
                    name = "screenshot1",
                    value = "Screen Shots of the uploading application",
                    required = true)
            @Multipart(value = "screenshot1") Attachment screenshot1,
            @ApiParam(
                    name = "screenshot2",
                    value = "Screen Shots of the uploading application",
                    required = false)
            @Multipart(value = "screenshot2") Attachment screenshot2,
            @ApiParam(
                    name = "screenshot3",
                    value = "Screen Shots of the uploading application",
                    required = false)
            @Multipart(value = "screenshot3") Attachment screenshot3
    );

    @PUT
    @Path("/app-artifacts/{deviceType}/{appType}/{appId}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes("multipart/mixed")
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Upload artifacts",
            notes = "This will create a new application",
            tags = "ApplicationDTO Management",
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
                                    "ApplicationDTO artifact updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Couldn't found application/application release to update applocation release artifact."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the application list.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationArtifact(
            @ApiParam(
                    name = "deviceType",
                    value = "Type of the device i.e Android, IOS etc",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "appType",
                    value = "Type of the application i.e ENTERPRISE, PUBLIC, WEB, WEB-CLIP etc",
                    required = true)
            @PathParam("appType") String appType,
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the application",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @Multipart("binaryFile") Attachment binaryFile
    );

    @PUT
    @Path("/app-release/{deviceType}/{appType}/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            consumes = MediaType.MULTIPART_FORM_DATA,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update an application release",
            notes = "This will update a new application release",
            tags = "ApplicationDTO Management",
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
                            response = ApplicationReleaseDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "ApplicationDTO release updating payload contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while releasing the application.",
                            response = ErrorResponse.class)
            })
    Response updateApplicationRelease(
            @ApiParam(
                    name = "deviceType",
                    value = "Supported device type of the application",
                    required = true)
            @PathParam("deviceType") String deviceType,
            @ApiParam(
                    name = "appType",
                    value = "Type of the application",
                    required = true)
            @PathParam("appType") String appType,
            @ApiParam(
                    name = "UUID",
                    value = "Unique identifier of the ApplicationDTO Release",
                    required = true)
            @PathParam("uuid") String applicationUUID,
            @ApiParam(
                    name = "applicationReleaseWrapper",
                    value = "Application release wrapper which is going to update.",
                    required = true)
            @Multipart(
                    value = "applicationReleaseWrapper",
                    type = "application/json")
                    ApplicationReleaseWrapper applicationReleaseWrapper,
            @ApiParam(
                    name = "binaryFile",
                    value = "Application installer file.",
                    required = true)
            @Multipart(value = "binaryFile") Attachment binaryFile,
            @ApiParam(
                    name = "icon",
                    value = "Icon file of the application release.",
                    required = true)
            @Multipart(value = "icon") Attachment iconFile,
            @ApiParam(
                    name = "banner",
                    value = "banner file of the application release.",
                    required = true)
            @Multipart(value = "banner") Attachment bannerFile,
            @ApiParam(
                    name = "screenshot1",
                    value = "First screenshot of the uploading application",
                    required = true)
            @Multipart(value = "screenshot1") Attachment screenshot1,
            @ApiParam(
                    name = "screenshot2",
                    value = "Second screenshot 2 of the uploading application")
            @Multipart(value = "screenshot2") Attachment screenshot2,
            @ApiParam(
                    name = "screenshot3",
                    value = "Third screenshot of the uploading application")
            @Multipart(value = "screenshot3") Attachment screenshot3);

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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:publisher:update")
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
                            response = ApplicationDTO.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n " +
                                    "Lifecycle State changing request contains unacceptable or vulnerable data"),
                    @ApiResponse(
                            code = 404,
                            message = "NOT FOUND. \n Error occurred while adding new lifecycle state.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred adding a lifecycle state.",
                            response = ErrorResponse.class)
            })
    Response addLifecycleState(
            @ApiParam(
                    name = "appId",
                    value = "Identifier of the ApplicationDTO",
                    required = true)
            @PathParam("appId") int applicationId,
            @ApiParam(
                    name = "uuid",
                    value = "UUID of the ApplicationDTO Release",
                    required = true)
            @PathParam("uuid") String applicationUuid,
            @ApiParam(
                    name = "action",
                    value = "Changing lifecycle state",
                    required = true)
            @QueryParam("action") String action
    );
}
