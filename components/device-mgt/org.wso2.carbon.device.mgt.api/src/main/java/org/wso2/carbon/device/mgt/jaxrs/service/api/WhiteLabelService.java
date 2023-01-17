/*
 *  Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelThemeCreateRequest;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Metadata related REST-API implementation.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "Whitelabel Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "WhiteLabelManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/whitelabel"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "View Whitelabel",
                        description = "View whitelabel details",
                        key = "perm:whitelabel:view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/whitelabel/view"}
                ),
                @Scope(
                        name = "Update Whitelabel",
                        description = "Updating whitelabel",
                        key = "perm:whitelabel:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/whitelabel/update"}
                ),
        }
)
@Api(value = "Whitelabel Management")
@Path("/whitelabel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface WhiteLabelService {

    @GET
    @Path("/favicon")
    @ApiOperation(
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get whitelabel favicon",
            notes = "Get whitelabel favicon for the tenant of the logged in user",
            tags = "Tenant Metadata Management"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved white label favicon.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while getting white label artifact.",
                            response = ErrorResponse.class)
            })
    Response getWhiteLabelFavicon();

    @GET
    @Path("/logo")
    @ApiOperation(
            httpMethod = HTTPConstants.HEADER_GET,
            value = "Get whitelabel logo",
            notes = "Get whitelabel logo for the tenant of the logged in user",
            tags = "Tenant Metadata Management"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully retrieved white label logo.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while getting white label artifact.",
                            response = ErrorResponse.class)
            })
    Response getWhiteLabelLogo();

    @PUT
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Create whitelabel for tenant",
            notes = "Create whitelabel for the tenant of the logged in user",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:whitelabel:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully created white label theme.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while creating white label theme.",
                            response = ErrorResponse.class)
            })
    Response updateWhiteLabelTheme(WhiteLabelThemeCreateRequest whiteLabelThemeCreateRequest);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Get whitelabel for tenant",
            notes = "Get whitelabel for the tenant of the logged in user",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:whitelabel:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched white label theme.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while fetching white label theme.",
                            response = ErrorResponse.class)
            })
    Response getWhiteLabelTheme();

    @PUT
    @Path("/reset")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Reset whitelabel for tenant",
            notes = "Reset whitelabel to default for the tenant of the logged in user",
            tags = "Tenant Metadata Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:whitelabel:update")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched white label theme.",
                            response = Metadata.class,
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Type",
                                            description = "The content type of the body"),
                                    @ResponseHeader(
                                            name = "ETag",
                                            description = "Entity Tag of the response resource.\n" +
                                                    "Used by caches, or in conditional requests."),
                                    @ResponseHeader(
                                            name = "Last-Modified",
                                            description = "Date and time the resource was last modified.\n" +
                                                    "Used by caches, or in conditional requests."),
                            }),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. " +
                                    "\n Server error occurred while deleting white label theme.",
                            response = ErrorResponse.class)
            })
    Response resetWhiteLabel();
}
