/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.TagInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.TagMappingInfo;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.TagManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.Tag;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.TagManagementException;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.TagNotFoundException;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraRoleMgtLoggerImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagManagementServiceImpl implements TagManagementService {

    private static final EntgraLogger log = new EntgraRoleMgtLoggerImpl(TagManagementServiceImpl.class);

    @GET
    @Override
    public Response getTags() {
        try {
            List<Tag> tags = DeviceMgtAPIUtils.getTagManagementService().getAllTags();
            return Response.status(Response.Status.OK).entity(tags).build();
        }  catch (TagManagementException e) {
            String msg = "Error occurred while getting tags.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Override
    public Response addTag(List<TagInfo> tagInfoList) {
        RequestValidationUtil.validateTagListDetails(tagInfoList);
        try {
            List<Tag> tags = new ArrayList<>();
            for (TagInfo tagInfo : tagInfoList) {
                Tag tag = new Tag(tagInfo.getName(), tagInfo.getDescription());
                tags.add(tag);
            }
            DeviceMgtAPIUtils.getTagManagementService().addTags(tags);
            return Response.status(Response.Status.CREATED).entity(tagInfoList).build();
        } catch (TagManagementException e) {
            String msg = "Error occurred while adding tags." ;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity
                    (new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (BadRequestException e) {
            String msg = "Error occurred while adding tags. Please check the request" ;
            if(log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        }
    }

    @PUT
    @Override
    public Response updateTag(@QueryParam("tagId") Integer tagId, @QueryParam("tagName") String tagName, TagInfo tagInfo) {
        RequestValidationUtil.validateTagDetails(tagId, tagName, tagInfo);
        try {
            Tag tag;
            if (tagId != null) {
                tag = DeviceMgtAPIUtils.getTagManagementService().getTagById(tagId);
            } else {
                tag = DeviceMgtAPIUtils.getTagManagementService().getTagByName(tagName);
            }
            if (tag == null) {
                String msg = (tagId != null) ? "Tag with ID " + tagId + " is not found."
                        : "Tag with name " + tagName + " is not found.";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                        setMessage(msg).build()).build();
            }
            tag.setName(tagInfo.getName());
            tag.setDescription(tagInfo.getDescription());
            DeviceMgtAPIUtils.getTagManagementService().updateTag(tag);
            return Response.status(Response.Status.OK).entity(tag).build();
        } catch (TagManagementException e) {
            String msg = (tagId != null) ? "Error occurred while updating tag with ID " + tagId + "."
                    : "Error occurred while updating tag with name " + tagName + ".";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse.
                    ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (TagNotFoundException e) {
            String msg = (tagId != null) ? "Tag with ID " + tagId + " is not found."
                    : "Tag with name " + tagName + " is not found.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                    setMessage(msg).build()).build();
        } catch (BadRequestException e) {
            String msg = (tagId != null) ? "Error occurred while updating tag with ID " + tagId
                    : "Error occurred while updating tag with name " + tagName;
            if(log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse.ErrorResponseBuilder().
                    setMessage(msg).build()).build();
        }
    }

    @DELETE
    @Path("/{tagId}")
    @Override
    public Response deleteTag(@PathParam("tagId") int tagId) {
        try {
            DeviceMgtAPIUtils.getTagManagementService().deleteTag(tagId);
            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (TagManagementException e) {
            String msg = "Error occurred while deleting tag with ID " + tagId + ".";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse.ErrorResponseBuilder().
                    setMessage(msg).build()).build();
        } catch (TagNotFoundException e) {
            String msg = "Tag with ID " + tagId + " is not found.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.ErrorResponseBuilder().
                    setMessage(e.getMessage()).build()).build();
        }
    }

    @GET
    @Path("/{tagId}")
    @Override
    public Response getTagById(@PathParam("tagId") int tagId) {
        try {
            Tag tag = DeviceMgtAPIUtils.getTagManagementService().getTagById(tagId);
            return Response.status(Response.Status.OK).entity(tag).build();
        } catch (TagManagementException e) {
            String msg = "Error occurred while getting tag with ID " + tagId + ".";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse.
                    ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (TagNotFoundException e) {
            String msg = "Tag with ID " + tagId + " is not found.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorResponse.
                    ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        }
    }

    @POST
    @Path("/mapping")
    public Response addDeviceTagMapping(TagMappingInfo tagMappingInfo) {
        RequestValidationUtil.validateTagMappingDetails(tagMappingInfo);
        try {
            DeviceMgtAPIUtils.getTagManagementService().addDeviceTagMapping(tagMappingInfo.getDeviceIdentifiers(),
                    tagMappingInfo.getDeviceType(), tagMappingInfo.getTags());
            return Response.status(Response.Status.CREATED).entity(tagMappingInfo).build();
        } catch (TagManagementException e) {
            String msg = "Error occurred while adding device-tag mapping.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse.
                    ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (BadRequestException e) {
            String msg = "Error occurred while adding tag mappings.";
            if(log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse.
                    ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        }
    }

    @DELETE
    @Path("/mapping")
    public Response deleteDeviceTagMapping(TagMappingInfo tagMappingInfo) {
        RequestValidationUtil.validateTagMappingDetails(tagMappingInfo);
        try {
            DeviceMgtAPIUtils.getTagManagementService().deleteDeviceTagMapping(tagMappingInfo.getDeviceIdentifiers(),
                    tagMappingInfo.getDeviceType(), tagMappingInfo.getTags());
            return Response.status(Response.Status.NO_CONTENT).entity(tagMappingInfo).build();
        } catch (TagManagementException e) {
            String msg = "Error occurred while deleting tag mappings.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new ErrorResponse.
                    ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (BadRequestException e) {
            String msg = "Error occurred while deleting tag mappings.";
            if(log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorResponse.
                    ErrorResponseBuilder().setMessage(e.getMessage()).build()).build();
        }
    }
}
