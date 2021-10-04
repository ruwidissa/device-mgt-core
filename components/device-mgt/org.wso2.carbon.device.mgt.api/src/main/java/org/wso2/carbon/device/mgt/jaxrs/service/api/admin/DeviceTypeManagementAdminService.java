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
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.mgt.jaxrs.service.api.admin;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.wso2.carbon.apimgt.annotations.api.Scope;
import org.wso2.carbon.apimgt.annotations.api.Scopes;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceTypeList;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceTypeVersionWrapper;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.validation.constraints.Size;
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

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "DeviceTypeManagementAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/admin/device-types"),
                        })
                }
        ),
        tags = {
                @Tag(name = "device_management", description = "")
        }
)
@Path("/admin/device-types")
@Api(value = "Device Type Management Administrative Service", description = "This an  API intended to be used by " +
        "'internal' components to log in as an admin user and do a selected number of operations. " +
        "Further, this is strictly restricted to admin users only ")
@Scopes(
        scopes = {
                @Scope(
                        name = "Manage a Device Type",
                        description = "Add, Edit or View a Device Type",
                        key = "perm:admin:device-type",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/device-type"}
                ),
                @Scope(
                        name = "Getting Details of a Device Type",
                        description = "Getting Details of a Device Type",
                        key = "perm:admin:device-type:view",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/device-type/view"}
                ),
                @Scope(
                        name = "Add Device Type Config",
                        description = "Add Platform Config of a Device Type",
                        key = "perm:admin:device-type:configs",
                        roles = {"Internal/devicemgt-admin"},
                        permissions = {"/device-mgt/admin/device-type/config"}
                )
        }
)
public interface DeviceTypeManagementAdminService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting the Supported Device Type with Meta Definition",
            notes = "Get the list of device types supported by WSO2 IoT.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type:view")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the list of supported device types.",
                            response = DeviceTypeList.class,
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
                                            description =
                                                    "Date and time the resource was last modified.\n" +
                                                            "Used by caches, or in conditional requests."),
                            }
                    ),
                    @ApiResponse(
                            code = 304,
                            message =
                                    "Not Modified. \n Empty body because the client already has the latest version " +
                                            "of the requested resource.\n"),
                    @ApiResponse(
                            code = 406,
                            message = "Not Acceptable.\n The requested media type is not supported"),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Server error occurred while fetching the " +
                                    "list of supported device types.",
                            response = ErrorResponse.class)
            }
    )
    Response getDeviceTypes();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{type}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Getting Details of a Device Type",
            notes = "Get the details of a device by searching via the device type and the tenant domain.",
            response = DeviceType.class,
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type:view")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device type.",
                    response = DeviceType.class,
                    responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response getDeviceTypeByName(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(min = 2, max = 45)
                    String type);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add a Device Type",
            notes = "Add the details of a device type.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully added the device type.",
                         responseHeaders = {
                                 @ResponseHeader(
                                         name = "Content-Type",
                                         description = "The content type of the body")
                         }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 403,
                    message = "Forbidden.\n The resource is unavailable for current tenant.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response addDeviceType(@ApiParam(
            name = "type",
            value = "The device type such as ios, android, windows or fire-alarm.",
            required = true)DeviceType deviceType);

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{type}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Device Type",
            notes = "Update the details of a device type.",
            response = DeviceType.class,
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully updated the device type.",
                         responseHeaders = {
                                 @ResponseHeader(
                                         name = "Content-Type",
                                         description = "The content type of the body")
                         }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response updateDeviceType(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(min = 2, max = 45)
                    String name,
            @ApiParam(
            name = "deviceType",
            value = "The device type such as ios, android, windows or fire-alarm.",
            required = true) DeviceType deviceType);

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{type}/configs")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add Configuration Details",
            notes = "Add Configuration Details of a Device Type.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type:configs")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully added the device type config.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response addDeviceTypePlatformConfig(
            @ApiParam(
                    name = "type",
                    value = "The device type name, such as ios, android, windows or fire-alarm.",
                    required = true)
            @PathParam("type")
            @Size(min = 2, max = 45)
                    String type,
            @ApiParam(
                    name = "config",
                    value = "Platform configuration of specified device type.",
                    required = true)
                    PlatformConfiguration config);


    @POST
    @Path("/versions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "POST",
            value = "Add Device Type Version",
            notes = "Add a new device type version.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully updated the device type version.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response addDeviceTypeVersion(
            @ApiParam(
                    name = "deviceTypeName",
                    value = "Device type name.",
                    required = true)
            @PathParam("deviceTypeName")
                    String deviceTypeName,
            @ApiParam(
                    name = "deviceTypeVersion",
                    value = "The device type version details.",
                    required = true) DeviceTypeVersionWrapper deviceTypeVersion);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{deviceTypeName}/versions")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "GET",
            value = "Get Device Type Version",
            notes = "Get a new device type version.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully fetched the device type version.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response getDeviceTypeVersion(
            @ApiParam(
                    name = "deviceTypeName",
                    value = "Device type name.",
                    required = true)
            @PathParam("deviceTypeName")
                    String deviceTypeName);


    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{deviceTypeName}/versions")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "PUT",
            value = "Update Device Type Version",
            notes = "Update a new device type version.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully updated the device type version.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response updateDeviceTypeVersion(
            @ApiParam(
                    name = "deviceTypeName",
                    value = "Device type name.",
                    required = true)
            @PathParam("deviceTypeName")
                    String deviceTypeName,
            @ApiParam(
                    name = "deviceTypeVersion",
                    value = "The device type version details.",
                    required = true) DeviceTypeVersionWrapper deviceTypeVersion);


    @DELETE
    @Path("/{deviceTypeName}/versions/{version}")
    @ApiOperation(
            produces = MediaType.APPLICATION_JSON,
            httpMethod = "DELETE",
            value = "Move Device Type Version to removed state",
            notes = "Move Device Type Version to removed state.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. \n Successfully moved the device type version to removed.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 304,
                    message = "Not Modified. Empty body because the client already has the latest version of the " +
                            "requested resource.\n"),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found.\n The specified device does not exist",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable.\n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while fetching the device list.",
                    response = ErrorResponse.class)
    })
    Response deleteDeviceTypeVersion(@ApiParam(
            name = "deviceTypeName",
            value = "Device type name.",
            required = true)
                                     @PathParam("deviceTypeName") String deviceTypeName,
                                     @ApiParam(
                                             name = "version",
                                             value = "Device type version.",
                                             required = true)
                                     @PathParam("version")String version);


    @DELETE
    @Path("/{deviceTypeName}")
    @ApiOperation(
            httpMethod = "DELETE",
            value = "Delete device type.",
            notes = "This api will permanently delete an existing device type with everything " +
                    "it's related with.",
            tags = "Device Type Management Administrative Service",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = Constants.SCOPE, value = "perm:admin:device-type")
                    })
            }
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "ACCEPTED. \n Successfully deleted the device type.",
                    responseHeaders = {
                            @ResponseHeader(
                                    name = "Content-Type",
                                    description = "The content type of the body")
                    }),
            @ApiResponse(
                    code = 401,
                    message = "Unauthorized.\n The unauthorized access to the requested resource.",
                    response = ErrorResponse.class),
            @ApiResponse(
                    code = 404,
                    message = "Not Found. \n Device type trying to delete does not exist"),
            @ApiResponse(
                    code = 406,
                    message = "Not Acceptable. \n The requested media type is not supported"),
            @ApiResponse(
                    code = 500,
                    message = "Internal Server Error. \n Server error occurred while deleting device type.",
                    response = ErrorResponse.class)
    })
    Response deleteDeviceType(
            @ApiParam(
                    name = "deviceTypeName",
                    value = "Device type name.",
                    required = true)
            @PathParam("deviceTypeName") String deviceTypeName);
}
