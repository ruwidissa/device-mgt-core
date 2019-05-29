/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.application.mgt.common.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponseTmp;
import org.wso2.carbon.device.application.mgt.common.EnterpriseInstallationDetails;
import org.wso2.carbon.device.application.mgt.common.InstallationDetails;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/subscription"),
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
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Install an ApplicationDTO",
                        description = "Install an application",
                        key = "perm:app:subscription:install",
                        permissions = {"/app-mgt/store/subscription/install"}
                ),
                @org.wso2.carbon.apimgt.annotations.api.Scope(
                        name = "Uninstall an Application",
                        description = "Uninstall an application",
                        key = "perm:app:subscription:uninstall",
                        permissions = {"/app-mgt/store/subscription/uninstall"}
                )
        }
)
@Path("/subscription")
@Api(value = "Subscription Management", description = "This API carries all subscription management related " +
        "operations " +
        "such as install application to device, uninstall application from device, etc.")
@Produces(MediaType.APPLICATION_JSON)
public interface SubscriptionManagementAPI {

    String SCOPE = "scope";

    @POST
    @Path("/install/{uuid}/devices")
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
    Response installApplicationForDevices(
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<DeviceIdentifier> deviceIdentifiers
    );

    @POST
    @Path("/install/{uuid}/{subType}/{action}")
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
    Response performBulkAppInstallation(
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
            @Valid List<String> subscribers
    );

    @POST
    @Path("/install/{uuid}/devices")
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
    Response installApplicationForUsers(
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<String> users
    );

    @POST
    @Path("/install/{uuid}/devices")
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
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully add an operation to install application for user devices..",
                            response = PaginationResult.class,
                            responseContainer = "PaginationResult"),
                    @ApiResponse(
                            code = 400,
                            message =
                                    "Bad Request. \n Found invalid payload with the request."),
                    @ApiResponse(
                            code = 403,
                            message = "Don't have permission to install application release."),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n Not found an application release for requested UUID."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while adding operation to install "
                                    + "application for users.",
                            response = ErrorResponse.class)

            })
    Response installApplicationForRoles (
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<String> roles
    );

    @POST
    @Path("/install/{uuid}/devices")
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
    Response installApplicationForGroups (
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<String> groups
    );

//    ###########################


    @POST
    @Path("/uninstall/{uuid}/devices")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:uninstall")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response uninstallApplicationForDevices(
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<DeviceIdentifier> deviceIdentifiers
    );

    @POST
    @Path("/uninstall/{uuid}/devices")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:uninstall")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response uninstallApplicationForUsers(
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<String> users
    );

    @POST
    @Path("/uninstall/{uuid}/devices")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:uninstall")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response uninstallApplicationForRoles (
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<String> roles
    );

    @POST
    @Path("/uninstall/{uuid}/devices")
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
                            @ExtensionProperty(name = SCOPE, value = "perm:app:subscription:uninstall")
                    })
            }
    )
    @ApiResponses(
            value = {

            })
    Response uninstallApplicationForGroups (
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "installationDetails",
                    value = "The application ID and list of devices/users/roles",
                    required = true
            )
            @Valid List<String> groups
    );
}
