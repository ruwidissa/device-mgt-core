/*
 *  Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.NotFoundException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelTheme;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelThemeCreateRequest;
import org.wso2.carbon.device.mgt.jaxrs.beans.MetadataList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.MetadataService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the service class for metadata management.
 */
@Path("/metadata")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MetadataServiceImpl implements MetadataService {

    private static final Log log = LogFactory.getLog(MetadataServiceImpl.class);

    @GET
    @Override
    public Response getAllMetadataEntries(
            @QueryParam("offset") int offset,
            @DefaultValue("5")
            @QueryParam("limit") int limit) {
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        MetadataList metadataList = new MetadataList();
        try {
            MetadataManagementService metadataManagementService = DeviceMgtAPIUtils.getMetadataManagementService();
            PaginationResult result = metadataManagementService.retrieveAllMetadata(request);
            metadataList.setCount(result.getRecordsTotal());
            metadataList.setMetadataList((List<Metadata>) result.getData());
            return Response.status(Response.Status.OK).entity(metadataList).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving metadata list for given parameters [offset:" +
                    offset + ", limit:" + limit + " ]";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Path("/{metaKey}")
    public Response getMetadataEntry(
            @PathParam("metaKey") String metaKey) {
        Metadata metadata;
        try {
            metadata = DeviceMgtAPIUtils.getMetadataManagementService().retrieveMetadata(metaKey);
            return Response.status(Response.Status.OK).entity(metadata).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while getting the metadata entry for metaKey:" + metaKey;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    public Response createMetadataEntry(Metadata metadata) {
        RequestValidationUtil.validateMetadata(metadata);
        try {
            Metadata createdMetadata = DeviceMgtAPIUtils.getMetadataManagementService().createMetadata(metadata);
            return Response.status(Response.Status.CREATED).entity(createdMetadata).build();
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg = "Metadata entry metaKey:" + metadata.getMetaKey() + " is already exist.";
            log.error(msg, e);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while creating the metadata entry for metaKey:" + metadata.getMetaKey();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    public Response updateMetadataEntry(Metadata metadata) {
        RequestValidationUtil.validateMetadata(metadata);
        try {
            Metadata updatedMetadata = DeviceMgtAPIUtils.getMetadataManagementService().updateMetadata(metadata);
            return Response.status(Response.Status.OK).entity(updatedMetadata).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while updating the metadata entry for metaKey:" + metadata.getMetaKey();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Path("/{metaKey}")
    public Response deleteMetadataEntry(
            @PathParam("metaKey") String metaKey) {
        try {
            DeviceMgtAPIUtils.getMetadataManagementService().deleteMetadata(metaKey);
            return Response.status(Response.Status.OK).entity("Metadata entry is deleted successfully.").build();
        } catch (MetadataKeyNotFoundException e) {
            String msg = "Metadata entry metaKey:" + metaKey + " is not found.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while deleting the metadata entry for metaKey:" + metaKey;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Useful to send files as application/octet-stream responses
     */
    private Response sendFileStream(byte[] content) throws IOException {
        try (ByteArrayInputStream binaryDuplicate = new ByteArrayInputStream(content)) {
            Response.ResponseBuilder response = Response
                    .ok(binaryDuplicate, MediaType.APPLICATION_OCTET_STREAM);
            response.status(Response.Status.OK);
//            response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.header("Content-Length", content.length);
            return response.build();
        } catch (IOException e) {
            String msg = "Error occurred while creating input stream from buffer array. ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
