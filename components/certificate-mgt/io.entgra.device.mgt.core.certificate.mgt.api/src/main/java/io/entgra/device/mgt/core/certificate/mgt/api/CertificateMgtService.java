/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.certificate.mgt.api;

import io.swagger.annotations.*;

import io.entgra.device.mgt.core.apimgt.annotations.Scope;
import io.entgra.device.mgt.core.apimgt.annotations.Scopes;
import io.entgra.device.mgt.core.certificate.mgt.api.beans.ErrorResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@SwaggerDefinition(
        info = @Info(
                version = "1.0.0",
                title = "",
                extensions = {
                        @Extension(properties = {
                                @ExtensionProperty(name = "name", value = "SCEP Management"),
                                @ExtensionProperty(name = "context", value = "/api/device-mgt/v1.0/scep"),
                        })
                }
        ),
        tags = {
                @Tag(name = "scep_management", description = "SCEP management related REST-API. " +
                                                                    "This can be used to manipulated device " +
                                                                    "certificate related details.")
        }
)
@Path("/scep")
@Api(value = "SCEP Management", description = "This API carries all device Certificate management " +
                                                            "related operations.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Scopes(scopes = {
        @Scope(
                name = "Sign CSR",
                description = "Sign CSR",
                key = "dm:sign-csr",
                roles = {"Internal/devicemgt-user"},
                permissions = {"/device-mgt/certificates/manage"}
        )
}
)
public interface CertificateMgtService {

     String SCOPE = "scope";

    /**
     * Sign the client's certificate signing request and save it in the database.
     *
     * @param binarySecurityToken Base64 encoded Certificate signing request.
     * @return X509Certificate type sign certificate.
     */
    @POST
    @Path("/sign-csr")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(
            consumes = MediaType.TEXT_PLAIN,
            produces = MediaType.TEXT_PLAIN,
            httpMethod = "POST",
            value = "Process a given CSR and return signed certificates.",
            notes = "This will return a signed certificate upon a given CSR.",
            tags = "Device Management",
            extensions = {
                    @Extension(properties = {
                            @ExtensionProperty(name = SCOPE, value = "dm:sign-csr")
                    })
            }
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            code = 200,
                            message = "OK. \n Successfully fetched the device location.",
                            response = String.class),
                    @ApiResponse(
                            code = 304,
                            message = "Not Modified. \n " +
                                    "Empty body because the client already has the latest version of the requested resource."),
                    @ApiResponse(
                            code = 500,
                            message = "Internal Server Error. \n Error occurred while retrieving signed certificate.",
                            response = ErrorResponse.class)
            })
    Response getSignedCertFromCSR(
            @ApiParam(
                    name = "If-Modified-Since",
                    value = "Validates if the requested variant has not been modified since the time specified",
                    required = false)
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            String binarySecurityToken);
}
