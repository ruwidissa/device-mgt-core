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

package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.services.ArtifactDownloadAPI;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.AppmDataHandler;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * Implementation of ApplicationDTO Management related APIs.
 */
@Produces({"application/json"})
@Path("/artifact")
public class ArtifactDownloadAPIImpl implements ArtifactDownloadAPI {

    private static Log log = LogFactory.getLog(ArtifactDownloadAPIImpl.class);

    @GET
    @Override
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{uuid}/{fileName}")
    public Response getArtifact(@PathParam("uuid") String uuid,
            @PathParam("fileName") String fileName) {
        AppmDataHandler dataHandler = APIUtil.getDataHandler();
        try {
            InputStream fileInputStream = dataHandler.getArtifactStream(uuid, fileName);
            Response.ResponseBuilder response = Response
                    .ok(fileInputStream, MediaType.APPLICATION_OCTET_STREAM);
            response.status(Response.Status.OK);
            response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            return response.build();
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application release for UUID: " + uuid + " and file name:  " + fileName;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Invalid data is used with the request to get input stream of the application release. UUID: "
                    + uuid + " and file name: " + fileName;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application release artifact file. ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Produces(MediaType.TEXT_XML)
    @Path("/plist/{uuid}")
    public Response getPlistArtifact(@PathParam("uuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            String plistContent = applicationManager.getPlistArtifact(uuid);
            return Response.status(Response.Status.OK).entity(plistContent).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application plist artifact file.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
