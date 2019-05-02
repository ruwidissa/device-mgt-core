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
package org.wso2.carbon.device.application.mgt.publisher.api.services.admin;

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
import java.util.List;

/**
 * APIs to handle application management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "ApplicationDTO Management Publisher Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationManagementPublisherAdminService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt-publisher/v1.0/admin/applications"),
                        })
                }
        ),
        tags = {
                @Tag(name = "application_management, device_management", description = "App publisher related Admin APIs")
        }
)
@Scopes(
        scopes = {
                @Scope(
                        name = "Delete Application Release",
                        description = "Delete Application Release",
                        key = "perm:admin:app:publisher:update",
                        permissions = {"/app-mgt/publisher/admin/application/update"}
                )
        }
)
@Path("/admin/applications")
@Api(value = "ApplicationDTO Management")
@Produces(MediaType.APPLICATION_JSON)
public interface ApplicationManagementPublisherAdminAPI {

    String SCOPE = "scope";

    @DELETE
    @Path("/{appid}/{uuid}")
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
                            message = "OK. \n Successfully delete application releaset.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. There doesn't have an application release with UUID" +
                                    "query."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while deleting application release.",
                            response = ErrorResponse.class)
            }) Response deleteApplicationRelease(
            @ApiParam(
                    name = "appId",
                    value = "application Id",
                    required = true)
            @PathParam("appid") int applicationId,
            @ApiParam(
                    name = "uuid",
                    value = "application release UUID",
                    required = true)
            @PathParam("uuid") String releaseUuid);
}
