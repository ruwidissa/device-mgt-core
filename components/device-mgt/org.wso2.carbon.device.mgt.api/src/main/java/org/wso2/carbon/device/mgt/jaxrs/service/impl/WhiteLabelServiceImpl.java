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

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.NotFoundException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelTheme;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelThemeCreateRequest;
import org.wso2.carbon.device.mgt.jaxrs.service.api.WhiteLabelService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This is the service class for metadata management.
 */
@Path("/whitelabel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WhiteLabelServiceImpl implements WhiteLabelService {

    private static final Log log = LogFactory.getLog(WhiteLabelServiceImpl.class);

    @GET
    @Override
    @Path("/favicon")
    public Response getWhiteLabelFavicon() {
        try {
            byte[] fileContent = DeviceMgtAPIUtils.getWhiteLabelManagementService().getWhiteLabelFavicon();
            return sendFileStream(fileContent);
        } catch (NotFoundException e) {
            String msg = "Favicon not found this tenant.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting favicon";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/logo")
    public Response getWhiteLabelLogo() {
        try {
            byte[] fileContent = DeviceMgtAPIUtils.getWhiteLabelManagementService().getWhiteLabelLogo();
            return sendFileStream(fileContent);
        } catch (NotFoundException e) {
            String msg = "Logo not found for this tenant.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting logo";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    public Response createWhiteLabelTheme(WhiteLabelThemeCreateRequest whiteLabelThemeCreateRequest) {
        RequestValidationUtil.validateWhiteLabelTheme(whiteLabelThemeCreateRequest);
        try {
            WhiteLabelTheme createdWhiteLabelTheme = DeviceMgtAPIUtils.getWhiteLabelManagementService().updateWhiteLabelTheme(whiteLabelThemeCreateRequest);
            return Response.status(Response.Status.CREATED).entity(createdWhiteLabelTheme).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while creating whitelabel for tenant";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    public Response getWhiteLabelTheme() {
        try {
            WhiteLabelTheme whiteLabelTheme = DeviceMgtAPIUtils.getWhiteLabelManagementService().getWhiteLabelTheme();
            return Response.status(Response.Status.CREATED).entity(whiteLabelTheme).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while deleting whitelabel for tenant";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Not white label theme configured for this tenant";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @PUT
    @Override
    @Path("/reset")
    public Response resetWhiteLabel() {
        try {
            DeviceMgtAPIUtils.getWhiteLabelManagementService().resetToDefaultWhiteLabelTheme();
            return Response.status(Response.Status.CREATED).entity("White label theme deleted successfully.").build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while resetting whitelabel for tenant";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Useful to send files as application/octet-stream responses
     */
    private Response sendFileStream(byte[] content) {
        try (ByteArrayInputStream binaryDuplicate = new ByteArrayInputStream(content)) {
            Response.ResponseBuilder response = Response
                    .ok(binaryDuplicate, MediaType.APPLICATION_OCTET_STREAM);
            response.status(Response.Status.OK);
            response.header("Content-Length", content.length);
            return response.build();
        } catch (IOException e) {
            String msg = "Error occurred while creating input stream from buffer array. ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
