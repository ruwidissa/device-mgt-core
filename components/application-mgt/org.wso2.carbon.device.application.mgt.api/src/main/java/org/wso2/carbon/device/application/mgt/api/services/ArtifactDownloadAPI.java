/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.application.mgt.api.services;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.ErrorResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * APIs to handle application management related tasks.
 */
@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "ApplicationDTO Management Common Service",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "ApplicationManagementArtifactDownloadService"),
                                @ExtensionProperty(name = "context", value = "/api/application-mgt/v1.0/artifact"),
                        })
                }
        )
)
@Path("/artifact")
@Api(value = "ApplicationDTO Management Artifact Downloading Service")
@Produces(MediaType.APPLICATION_JSON)
public interface ArtifactDownloadAPI {

    @GET
    @Path("/{uuid}/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(
            produces = MediaType.APPLICATION_OCTET_STREAM,
            httpMethod = "GET",
            value = "get application management UI configuration",
            notes = "This will get all UI configuration of application management"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully got UI config.",
                            response = ApplicationList.class),
                    @ApiResponse(
                            code = 404,
                            message = "Not Found. There doesn't have an defined UI config." +
                                    "query."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while getting the UI config.",
                            response = ErrorResponse.class)
            })
    Response getArtifact(
            @ApiParam(
            name = "uuid",
            value = "UUID of the application release.",
            required = true)
            @PathParam("uuid") String uuid,
            @ApiParam(
                    name = "fileName",
                    value = "Name of the artifact",
                    required = true)
            @PathParam("fileName") String fileName);

}
