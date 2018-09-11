/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.ReviewManagementAPI;
import org.wso2.carbon.device.application.mgt.common.Comment;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.CommentManagementException;
import org.wso2.carbon.device.application.mgt.common.services.CommentsManager;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment Management related jax-rs APIs.
 */
@Path("/review")
public class ReviewManagementAPIImpl implements ReviewManagementAPI {

    private static Log log = LogFactory.getLog(ReviewManagementAPIImpl.class);

    @Override
    @GET
    @Path("/application/{uuid}/comments")
    public Response getAllComments(
            @PathParam("uuid") String uuid,
            @QueryParam("offset") int offSet,
            @QueryParam("limit") int limit) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        List<Comment> comments = new ArrayList<>();
        try {
            PaginationRequest request = new PaginationRequest(offSet, limit);
            if (uuid == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Comments not found").build();
            } else if (request.validatePaginationRequest(offSet, limit)) {
                commentsManager.getAllComments(request, uuid);
                return Response.status(Response.Status.OK).entity(comments).build();
            }
        } catch (CommentManagementException e) {
            String msg = "Error occurred while retrieving comments.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg)
                .build();
        }
        return Response.status(Response.Status.OK).entity(comments).build();
    }

    @Override
    @POST
    @Consumes("application/json")
    @Path("/application/{uuid}/comment")
    public Response addComment(
            @ApiParam Comment comment,
            @PathParam("uuid") String uuid) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        try {
            if (commentsManager.addComment(comment, uuid) != null) {
                return Response.status(Response.Status.CREATED).entity(comment).build();
            } else {
                String msg = "Given comment is not valid ";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (CommentManagementException e) {
            String msg = "Error occurred while creating the comment";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg).build();
        }
    }

    @Override
    @PUT
    @Consumes("application/json")
    @Path("/comment/{commentId}")
    public Response updateComment(
            @ApiParam Comment comment,
            @PathParam("commentId") int commentId) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        try {
            if (commentId == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Comment not found").build();
            } else if (comment == null) {
                String msg = "Given comment is not valid ";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else {
                comment = commentsManager.updateComment(comment, commentId);
                return Response.status(Response.Status.OK).entity(comment).build();
            }
        } catch (CommentManagementException e) {
            String msg = "Error occurred while retrieving comments.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg).build();
        }
    }

    @Override
    @DELETE
    @Path("/comment/{commentId}")
    public Response deleteComment(
            @PathParam("commentId") int commentId) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        try {
            if (commentId == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Comment not found").build();
            } else {
                commentsManager.deleteComment(commentId);
            }
        } catch (CommentManagementException e) {
            String msg = "Error occurred while deleting the comment.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg)
                .build();
        }
        return Response.status(Response.Status.OK).entity("Comment is deleted successfully.").build();
    }

    @Override
    @GET
    @Path("/application/{uuid}/rating")
    public Response getRating(
            @PathParam("uuid") String uuid) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        int stars;
        try {
            stars = commentsManager.getStars(uuid);
        } catch (CommentManagementException e) {
            log.error("Comment Management Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (ApplicationManagementException e) {
            String msg="Application Management Exception occurs";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(stars).build();
    }

    @Override
    @GET
    @Path("/application/{uuid}/total-rated-users")
    public Response getNumOfRatedUsers(
            @PathParam("uuid") String uuid) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        int ratedUsers;
        try {
            ratedUsers = commentsManager.getRatedUser(uuid);
        } catch (CommentManagementException e) {
            String msg="Comment Management Exception occurs";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg="Application Management Exception occurs";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Application with UUID" + uuid + " Internal server error occurs").build();
        }
        return Response.status(Response.Status.OK).entity(ratedUsers).build();
    }

    @Override
    @PUT
    @Consumes("application/json")
    @Path("/application/{uuid}/rating")
    public Response updateRatings(
            @ApiParam int stars,
            @PathParam("uuid") String uuid) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        int newStars;
        try {
            newStars = commentsManager.updateStars(stars, uuid);
            if (stars != 0) {
                return Response.status(Response.Status.CREATED).entity(newStars).build();
            } else {
                String msg = "Given star value is not valid ";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (ApplicationManagementException e) {
            String msg="Application Management Exception occurs";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg).build();
        }
    }
}