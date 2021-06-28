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

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * API to handle subscription management related admin tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Subscription Management Admin Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "SubscriptionManagementAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-store/v1.0/admin/subscription"),
                        })
                }
        ),
        tags = {
                @Tag(name = "subscription_management, device_management", description = "Subscription Management "
                        + "related Admin APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "View Application Subscriptions",
                        description = "View Application Subscriptions.",
                        key = "perm:admin:app:subscription:view",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/app-mgt/store/admin/subscription/view"}
                )
        }
)
@Path("/admin/subscription")
@Api(value = "Subscription Management Admin API")
@Produces(MediaType.APPLICATION_JSON)
public interface SubscriptionManagementAdminAPI {

    String SCOPE = "scope";

    @GET
    @Path("/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get subscription details of specific application.",
            notes = "This will get the subscription details of specific application",
            tags = "Subscription Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "perm:admin:app:subscription:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved subscription details.",
                            response = List.class,
                            responseContainer = "List"),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. \n No Application found which has application release of UUID.",
                            response = ErrorResponse.class),
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
                    name = "action",
                    value = "The action, subscribed or unsubscribed.",
                    required = false)
            @Size(max = 45)
            @QueryParam("action") String action,
            @ApiParam(
                    name = "actionStatus",
                    value = "Provide the action status details")
            @QueryParam("actionStatus") String actionStatus,
            @ApiParam(
            name = "status",
            value = "Provide the device status details, such as active or inactive.")
            @QueryParam("status") List<String> status,
            @ApiParam(
                    name = "uuid",
                    value = "uuid of the application release.",
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
            @QueryParam("limit") int limit
    );
}
