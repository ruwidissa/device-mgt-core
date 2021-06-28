/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 *   Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.jaxrs.service.api;

import com.google.gson.JsonArray;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Info;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Extension;
import io.swagger.annotations.Tag;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitation;
import org.wso2.carbon.device.mgt.jaxrs.beans.ActivityList;
import org.wso2.carbon.device.mgt.jaxrs.beans.BasicUserInfo;
import org.wso2.carbon.device.mgt.jaxrs.beans.BasicUserInfoList;
import org.wso2.carbon.device.mgt.jaxrs.beans.Credential;
import org.wso2.carbon.device.mgt.jaxrs.beans.EnrollmentInvitation;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.OldPasswordResetWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.PermissionList;
import org.wso2.carbon.device.mgt.jaxrs.beans.RoleList;
import org.wso2.carbon.device.mgt.jaxrs.beans.UserInfo;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "UserManagement"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/users"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Adding a User",
                        description = "Adding a User",
                        key = "perm:users:add",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Getting Details of a User",
                        description = "Getting Details of a User",
                        key = "perm:users:details",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Updating Details of a User",
                        description = "Updating Details of a User",
                        key = "perm:users:update",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Deleting a User",
                        description = "Deleting a User",
                        key = "perm:users:delete",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Getting the Role Details of a User",
                        description = "Getting the Role Details of a User",
                        key = "perm:users:roles",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Getting Details of Users",
                        description = "Getting Details of Users",
                        key = "perm:users:user-details",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Getting the User Count",
                        description = "Getting the User Count",
                        key = "perm:users:count",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Getting the User existence status",
                        description = "Getting the User existence status",
                        key = "perm:users:is-exist",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Searching for a User Name",
                        description = "Searching for a User Name",
                        key = "perm:users:search",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/view"}
                ),
                @Scope(
                        name = "Changing the User Password",
                        description = "Adding a User",
                        key = "perm:users:credentials",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/login"}
                ),
                @Scope(
                        name = "Sending Enrollment Invitations to Users",
                        description = "Sending Enrollment Invitations to Users",
                        key = "perm:users:send-invitation",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/users/manage"}
                ),
                @Scope(
                        name = "Get activities",
                        description = "Get activities",
                        key = "perm:get-activity",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/device-mgt/devices/owning-device/view"}
                ),
                @Scope(
                        name = "Getting the Permissions of the User",
                        description = "Getting the Permissions of the User",
                        key = "perm:user:permission-view",
                        roles = {"Internal/devicemgt-user"},
                        permissions = {"/login"}
                )
        }
)
@Path("/users")
@Api(value = "User Management", description = "User management related operations can be found here.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserManagementService {

    @POST
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Adding a User",
            notes = "WSO2 IoTS supports user management. Add a new user to the WSO2 IoTS user management system via " +
                    "this REST API",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:add")
                })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 201,
                            message = "Created. \n Successfully created the user.",
                            responseHeaders = {
                                    @ResponseHeader(
                                            name = "Content-Location",
                                            description = "The URL of the role added."),
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
                                                    "Used by caches, or in conditional requests.")}),
                    @ApiResponse(
                            code = 400,
                            message = "Bad Request. \n Invalid request or validation error.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 409,
                            message = "Conflict. \n User already exists.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 415,
                            message = "Unsupported media type. \n The entity of the request was in a not " +
                                    "supported format.",
                            response = ErrorResponse.class),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while adding a new user.",
                            response = ErrorResponse.class)
            })
    Response addUser(
            @ApiParam(
                    name = "user",
                    value = "Provide the property details to add a new user.\n" +
                            "Double click the example value and click try out. ",
                    required = true) UserInfo user);

    @GET
    @Path("/{username}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a User",
            notes = "Get the details of a user registered with WSO2 IoTS using the REST API.",
            response = BasicUserInfo.class,
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:details")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the details of the specified user.",
                    response = BasicUserInfo.class,
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the " +
                            "requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n Server error occurred while" +
                            " fetching the ruser details.",
                    response = ErrorResponse.class)
    })
    Response getUser(
            @ApiParam(
                    name = "username",
                    value = "Provide the username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @PUT
    @Path("/{username}")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating Details of a User",
            notes = "There will be situations where you will want to update the user details. In such "
                    + "situation you can update the user details using this REST API.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:update")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the details of the specified user.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "Content type of the body"),
                            @ResponseHeader(
                                    name = "ETag",
                                    description = "Entity Tag of the response resource.\n" +
                                            "Used by caches, or in conditional requests."),
                            @ResponseHeader(
                                    name = "Last-Modified",
                                    description = "Date and time the resource was last modified.\n" +
                                            "Used by caches, or in conditional requests.")}),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the user.",
                    response = ErrorResponse.class)
    })
    Response updateUser(
            @ApiParam(
                    name = "username",
                    value = "The username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain,
            @ApiParam(
                    name = "userData",
                    value = "Update the user details.\n" +
                            "NOTE: Do not change the admin username, password and roles when trying out this API.",
                    required = true) UserInfo userData);

    @DELETE
    @Path("/{username}")
    @Consumes(MediaType.WILDCARD)
    @ApiOperation(
            httpMethod = "DELETE",
            consumes = MediaType.WILDCARD,
            value = "Deleting a User",
            notes = "When an employee leaves the organization, you can remove the user details from WSO2 IoTS using " +
                    "this REST API.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:delete")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully removed the user from WSO2 IoTS."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while removing the user.",
                    response = ErrorResponse.class
            )
    })
    Response removeUser(
            @ApiParam(
                    name = "username",
                    value = "Username of the user to be deleted.\n" +
                            "INFO: If you want to try out this API, make sure to create a new user and then remove " +
                            "that user. Do not remove the admin user.",
                    required = true,
                    defaultValue = "[Create a new user named Jim, and then try out this API.]")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain);

    @GET
    @Path("/{username}/roles")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Role Details of a User",
            notes = "A user can be assigned to one or more role in IoTS. Using this REST API you can get the " +
                    "role/roles a user is assigned to.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:roles")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of roles the specified user is assigned to.",
                    response = RoleList.class,
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the " +
                            "requested resource."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the list of roles" +
                            " assigned to the specified user.",
                    response = ErrorResponse.class)
    })
    Response getRolesOfUser(
            @ApiParam(
                    name = "username",
                    value = "The username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username,
            @ApiParam(
                    name = "domain",
                    value = "The domain name of the user store.",
                    required = false)
            @QueryParam("domain") String domain);

    @GET
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of Users",
            notes = "You are able to manage users in WSO2 IoTS by adding, updating and removing users. If you wish to" +
                    " get the list of users registered with WSO2 IoTS, you can do so "
                    + "using this REST API",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:user-details")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of users registered with WSO2 IoTS.",
                    response = BasicUserInfoList.class,
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the list of WSO2 IoTS " +
                            "users.",
                    response = ErrorResponse.class)
    })
    Response getUsers(
            @ApiParam(
                    name = "filter",
                    value = "The username of the user.",
                    required = false)
            @QueryParam("filter") String filter,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the Java Date Format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String timestamp,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many user details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit);

    @GET
    @Path(("/search"))
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Filter details of users based on the given claims",
            notes = "You are able to manage users in WSO2 IoTS by adding, updating and removing users. If you wish to" +
                    " filter and get a list of users registered with WSO2 IoTS, you can do so using this REST API",
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:user-details")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of users registered with WSO2 IoTS.",
                    response = BasicUserInfoList.class,
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of " +
                            "the requested resource.\n"),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching users.",
                    response = ErrorResponse.class)
    })
    Response getUsers(
            @ApiParam(
                    name = "username",
                    value = "Username of the user",
                    required = false
            )
            @QueryParam("username") String username,
            @ApiParam(
                    name = "firstName",
                    value = "First Name of the user",
                    required = false
            )
            @QueryParam("firstName") String firstName,
            @ApiParam(
                    name = "lastName",
                    value = "Last Name of the user",
                    required = false
            )
            @QueryParam("lastName")String lastName,
            @ApiParam(
                    name = "emailAddress",
                    value = "Email Address of the user",
                    required = false
            )
            @QueryParam("emailAddress")String emailAddress,
            @HeaderParam("If-Modified-Since") String timestamp,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many user details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit
    );

    @GET
    @Path("/count")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the User Count",
            notes = "Get the number of users in WSO2 IoTS via this REST API.",
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:count")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the user count.",
                    response = BasicUserInfoList.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the total number of " +
                            "users in WSO2 IoTS.",
                    response = ErrorResponse.class)
    })
    Response getUserCount();

    @GET
    @Path("/checkUser")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the User existence status",
            notes = "Check if the user exists in the user store.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:is-exist")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched user exist status.",
                    response = BasicUserInfoList.class,
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the " +
                            "total user exist status.",
                    response = ErrorResponse.class)
    })
    Response isUserExists(@ApiParam(
                    name = "username",
                    value = "The username of the user.",
                    required = true)
            @QueryParam("username") String userName);

    @GET
    @Path("/search/usernames")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Searching for a User Name",
            notes = "If you are unsure of the user name of a user and need to retrieve the details of a specific " +
                    "user, you can "
                    + "search for that user by giving a character or a few characters in the username. "
                    + "You will be given a list of users having the user name in the exact order of the "
                    + "characters you provided.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:search")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of users that matched the given filter.",
                    response = String.class,
                    responseContainer = "List",
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the " +
                            "requested resource."),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the list of users that " +
                            "matched the given filter.",
                    response = ErrorResponse.class)
    })
    Response getUserNames(
            @ApiParam(
                    name = "filter",
                    value = "Provide a character or a few character in the user name",
                    required = true)
            @QueryParam("filter") String filter,
            @ApiParam(
                    name = "domain",
                    value = "The user store domain which the user names should be fetched from",
                    required = false)
            @QueryParam("domain") String domain,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z\n. " +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200",
                    required = false)
            @HeaderParam("If-Modified-Since") String timestamp,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    required = false,
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many user details you require from the starting pagination index/offset.",
                    required = false,
                    defaultValue = "5")
            @QueryParam("limit") int limit);

    @PUT
    @Path("/credentials")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Changing the User Password",
            notes = "A user is able to change the password to secure their WSO2 IoTS profile via this REST API.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:credentials")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated the user credentials."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the user credentials.",
                    response = ErrorResponse.class)
    })
    Response resetPassword(
            @ApiParam(
                    name = "credentials",
                    value = "The property to change the password.\n" +
                            "The password should be within 5 to 30 characters",
                    required = true) OldPasswordResetWrapper credentials);

    @POST
    @Path("/send-invitation")
    @ApiOperation(
            consumes = MediaType.APPLICATION_JSON,
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Sending Enrollment Invitations to Users",
            notes = "Send the users a mail inviting them to enroll their devices using the REST API given below.\n" +
                    "Before running the REST API command to send the enrollment invitations to users make sure to " +
                    "configure WSO2 IoTS as explained in step 4, under the WSO2 IoTS general server configurations " +
                    "documentation.",
            tags = "User Management",
            extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:send-invitation")
                })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully sent the invitation mail."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                            "Server error occurred while updating the user credentials.",
                    response = ErrorResponse.class)
    })
    Response inviteExistingUsersToEnrollDevice(
            @ApiParam(
                    name = "users",
                    value = "List of users",
                    required = true)
            @Valid DeviceEnrollmentInvitation deviceEnrollmentInvitation);

    @POST
    @Path("/enrollment-invite")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = HTTPConstants.HEADER_POST,
            value = "Sending Enrollment Invitations to email address",
            notes = "Send the a mail inviting recipients to enroll devices.",
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:send-invitation")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully sent the invitation mail."),
            @ApiResponse(
                    code = 400,
                    message = "Bad Request. \n Invalid request or validation error.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 415,
                    message = "Unsupported media type. \n The format of the requested entity was not supported.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n " +
                              "Server error occurred while updating the user credentials.",
                    response = ErrorResponse.class)
    })
    Response inviteToEnrollDevice(
            @ApiParam(
                    name = "enrollmentInvitation",
                    value = "List of email address of recipients",
                    required = true)
            @Valid EnrollmentInvitation enrollmentInvitation);

    @POST
    @Path("/validate")
    Response validateUser(Credential credential);

    @GET
    @Path("/device/activities")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Activity Details",
            notes = "Get the details of the operations/activities executed by the server on the devices registered" +
                    " with WSO2 EMM, during a defined time period.",
            tags = "Activity Info Provider",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:get-activity")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the activity details.",
                    response = ActivityList.class,
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
                    code = 304,
                    message = "Not Modified. \n Empty body because the client already has the latest version of the" +
                            " requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized. \n Unauthorized request."),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n No activities found.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the activity data.",
                    response = ErrorResponse.class)
    })
    Response getActivities(
            @ApiParam(
                    name = "since",
                    value = "Checks if the requested variant was created since the specified date-time.\n" +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z.\n" +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200")
            @QueryParam("since") String since,
            @ApiParam(
                    name = "offset",
                    value = "The starting pagination index for the complete list of qualified items.",
                    defaultValue = "0")
            @QueryParam("offset") int offset,
            @ApiParam(
                    name = "limit",
                    value = "Provide how many activity details you require from the starting pagination index/offset.",
                    defaultValue = "5")
            @QueryParam("limit") int limit,
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Checks if the requested variant was modified, since the specified date-time\n." +
                            "Provide the value in the following format: EEE, d MMM yyyy HH:mm:ss Z\n." +
                            "Example: Mon, 05 Jan 2014 15:10:00 +0200")
            @HeaderParam("If-Modified-Since") String ifModifiedSince);

    @PUT
    @Path("/claims/{username}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Updating external device claims of user",
            notes = "Update external device claims of a user registered with Entgra IoTS using the REST API.",
            response = BasicUserInfo.class,
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:details")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully updated external device claims of user.",
                    response = BasicUserInfo.class,
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
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n Server error occurred while" +
                            " fetching the user details.",
                    response = ErrorResponse.class)
    })
    Response updateUserClaimsForDevices(
            @ApiParam(
                    name = "username",
                    value = "Provide the username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username,
            @ApiParam(
                    name = "device list",
                    value = "Array of objects with device details",
                    required = true) JsonArray deviceList);

    @GET
    @Path("/claims/{username}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting external device claims of user",
            notes = "Get external device claims of a user registered with Entgra IoTS using the REST API.",
            response = BasicUserInfo.class,
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:details")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched external device claims of user.",
                    response = BasicUserInfo.class,
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
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n Server error occurred while" +
                            " fetching the ruser details.",
                    response = ErrorResponse.class)
    })
    Response getUserClaimsForDevices(
            @ApiParam(
                    name = "username",
                    value = "Provide the username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username);

    @DELETE
    @Path("/claims/{username}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Deleting external device claims of user",
            notes = "Delete external device claims of user registered with Entgra IoTS using the REST API.",
            response = BasicUserInfo.class,
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:users:details")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully deleted external device claims of user.",
                    response = BasicUserInfo.class,
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
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server ErrorResponse. \n Server error occurred while" +
                            " fetching the ruser details.",
                    response = ErrorResponse.class)
    })
    Response deleteUserClaimsForDevices(
            @ApiParam(
                    name = "username",
                    value = "Provide the username of the user.",
                    required = true,
                    defaultValue = "admin")
            @PathParam("username") String username);

    @GET
    @Path("/current-user/permissions")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the permission details of the current user",
            notes = "A user may granted more than one permission in IoTS. Using this REST API "
                    + "you can get the permission/permission the current user has granted. ",
            tags = "User Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:user:permission-view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "OK. \n Successfully fetched the list of permissions the user "
                            + "has granted.",
                    response = PermissionList.class,
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
                    code = 404,
                    message = "Not Found. \n The specified resource does not exist.\n",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the "
                            + "list of roles assigned to the specified user.",
                    response = ErrorResponse.class)
    })
    Response getPermissionsOfUser();
}
