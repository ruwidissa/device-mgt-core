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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * API to handle subscription management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Subscription Management Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "SubscriptionManagementService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-store/v1.0/subscription"),
                        })
                }
        ),
        tags = {
                @Tag(name = "subscription_management, device_management", description = "Subscription Management "
                        + "related APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Install an ApplicationDTO",
                        description = "Install an application",
                        key = "perm:app:subscription:install",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/store/subscription/install"}
                ),
                @Scope(
                        name = "Uninstall an Application",
                        description = "Uninstall an application",
                        key = "perm:app:subscription:uninstall",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/app-mgt/store/subscription/uninstall"}
                )
        }
)
@Path("/subscription")
@Api(value = "Subscription Management")
@Produces(MediaType.APPLICATION_JSON)
public interface SubscriptionManagementAPI {

    String SCOPE = "scope";

    @POST
    @Path("/{uuid}/devices/{action}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Install an application for devices",
            notes = "This will install an application to a given list of devices",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:install")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response performAppOperationForDevices(
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "action",
                    value = "Performing action.",
                    required = true
            )
            @PathParam("action") String action,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<DeviceIdentifier> deviceIdentifiers,
            @ApiParam(
                    name = "timestamp",
                    value = "Timestamp of scheduled install/uninstall operation"
            )
            @QueryParam("timestamp") long timestamp,
            @ApiParam(
                    name = "block-uninstall",
                    value = "App removal status of the install operation"
            )
            @QueryParam("block-uninstall") Boolean isUninstallBlocked
    );

    @POST
    @Path("/{uuid}/{subType}/{action}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Install an application for subscription type.",
            notes = "This will install an application to a given subscription type and this is bulk app installation.",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:install")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response performBulkAppOperation(
            @ApiParam(
                    name = "uuid",
                    value = "The application release UUID",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "subType",
                    value = "Subscription type of the app installing operation.",
                    required = true
            )
            @PathParam("subType") String subType,
            @ApiParam(
                    name = "action",
                    value = "Performing action.",
                    required = true
            )
            @PathParam("action") String action,
            @ApiParam(
                    name = "subscribers",
                    value = "Subscriber list of the application release.",
                    required = true
            )
            @Valid List<String> subscribers,
            @ApiParam(
                    name = "timestamp",
                    value = "Timestamp of scheduled install/uninstall operation"
            )
            @QueryParam("timestamp") long timestamp,
            @ApiParam(
                    name = "block-uninstall",
                    value = "App removal status of the install operation"
            )
            @QueryParam("block-uninstall") Boolean isUninstallBlocked
    );

    @POST
    @Path("/{uuid}/devices/ent-app-install/{action}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Install an application for devices via google enterprise app installing service",
            notes = "This will install an application to a given list of devices",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:install")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response performEntAppSubscriptionOnDevices(
            @ApiParam(
                    name = "UUID",
                    value = "The application UUID",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "action",
                    value = "Performing action.",
                    required = true
            )
            @PathParam("action") String action,
            @ApiParam(
                    name = "installationDetails",
                    value = "The  list of device identifiers",
                    required = true
            )
            @Valid List<DeviceIdentifier> deviceIdentifiers,
            @ApiParam(
                    name = "timestamp",
                    value = "Timestamp of scheduled ent. install operation"
            )
            @QueryParam("timestamp") long timestamp,
            @ApiParam(
                    name = "requiresUpdatingExternal",
                    value = "Should external system such as Google EMM APIs need to be updated."
            )
            @QueryParam("requiresUpdatingExternal") boolean requiresUpdatingExternal
    );

    @POST
    @Path("/{uuid}/{subType}/ent-app-install/{action}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Install an application for subscription type via google enterprise install.",
            notes = "This will install an application to a given subscription type and this is bulk app installation.",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:install")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response performBulkEntAppSubscription(
            @ApiParam(
                    name = "uuid",
                    value = "The application release UUID",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "subType",
                    value = "Subscription type of the app installing operation.",
                    required = true
            )
            @PathParam("subType") String subType,
            @ApiParam(
                    name = "action",
                    value = "Performing action.",
                    required = true
            )
            @PathParam("action") String action,
            @ApiParam(
                    name = "subscribers",
                    value = "Subscriber list of the application release.",
                    required = true
            )
            @Valid List<String> subscribers,
            @ApiParam(
                    name = "timestamp",
                    value = "Timestamp of scheduled ent app install operation"
            )
            @QueryParam("timestamp") long timestamp,
            @ApiParam(
                    name = "requiresUpdatingExternal",
                    value = "Should external system such as Google EMM APIs need to be updated."
            )
            @QueryParam("requiresUpdatingExternal") boolean requiresUpdatingExternal
    );

    @GET
    @Path("/{uuid}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get device details that have a given application install",
            notes = "This will get the device details that have a given application install, if exists",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:uninstall")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved device details.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No Application found which has application release of UUID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Found invalid payload with the request.",
                            response = List.class),
                    @ApiResponse(
                            code = 403,
                            message = "Forbidden. \n Don't have permission to get the details.",
                            response = List.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting data",
                            response = ErrorResponse.class)
            })
    Response getAppInstalledDevices(
            @ApiParam(
                    name = "name",
                    value = "The device name. For example, Nexus devices can have names, suhc as shamu, bullhead or angler.",
                    required = false)
            @Size(max = 45)
                    String name,
            @ApiParam(
                    name = "user",
                    value = "The username of the owner of the device.",
                    required = false)
            @QueryParam("user")
                    String user,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n" +
                            "- BYOD: Bring Your Own Device\n" +
                            "- COPE: Corporate-Owned, Personally-Enabled",
                    required = false)
            @QueryParam("ownership")
            @Size(max = 45)
                    String ownership,
            @ApiParam(
                    name="uuid",
                    value="uuid of the application release.",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "status",
                    value = "Provide the device status details, such as active or inactive.")
            @QueryParam("status") List<String> status
    );

    @GET
    @Path("/{uuid}/{subType}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get category details that have a given application install",
            notes = "This will get the category details that have a given application install, if exists",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:uninstall")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved categories details.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No Application found which has application " +
                                      "release of UUID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Found invalid payload with the request.",
                            response = List.class),
                    @ApiResponse(
                            code = 403,
                            message = "Forbidden. \n Don't have permission to get the details.",
                            response = List.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting data",
                            response = ErrorResponse.class)
            })
    Response getAppInstalledCategories(
            @ApiParam(
                    name="uuid",
                    value="uuid of the application release.",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name="subType",
                    value="Subscription type of the application release.",
                    required = true)
            @PathParam("subType") String subType,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting " +
                            "pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit") int limit
    );

    @GET
    @Path("/{uuid}/{subType}/{subTypeName}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get device details in categories that have a given application install",
            notes = "This will get the category's device details that have a given application install, if exists",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:uninstall")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved device details.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No Devices found which has application " +
                                    "release of UUID.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Found invalid payload with the request.",
                            response = List.class),
                    @ApiResponse(
                            code = 403,
                            message = "Forbidden. \n Don't have permission to get the details.",
                            response = List.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting data",
                            response = ErrorResponse.class)
            })
    Response getAppInstalledDevicesOnCategories(
            @ApiParam(
                    name="uuid",
                    value="uuid of the application release.",
                    required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name="subType",
                    value="Subscription type of the application release.",
                    required = true)
            @PathParam("subType") String subType,
            @ApiParam(
                    name="subTypeName",
                    value="Subscription type name of the application release.",
                    required = true)
            @PathParam("subTypeName") String subTypeName,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many device details you require from the starting " +
                            "pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "name",
                    value = "The device name. For example, Nexus devices can have names, such as shamu, bullhead or angler.",
                    required = false)
            @Size(max = 45)
                    String name,
            @ApiParam(
                    name = "user",
                    value = "The username of the owner of the device.",
                    required = false)
            @QueryParam("user")
                    String user,
            @ApiParam(
                    name = "ownership",
                    allowableValues = "BYOD, COPE",
                    value = "Provide the ownership status of the device. The following values can be assigned:\n" +
                            "- BYOD: Bring Your Own Device\n" +
                            "- COPE: Corporate-Owned, Personally-Enabled",
                    required = false)
            @QueryParam("ownership")
            @Size(max = 45)
                    String ownership,
            @ApiParam(
                    name = "status",
                    value = "Provide the device status details, such as active or inactive.")
            @QueryParam("status") List<String> status
    );
}
