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
import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.Rating;
import org.wso2.carbon.device.application.mgt.common.Review;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ReviewManager;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.ReviewManagementAPI;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Response;

/**
 * Review Management related jax-rs APIs.
 */
@Path("/review")
public class ReviewManagementAPIImpl implements ReviewManagementAPI {

    private static Log log = LogFactory.getLog(ReviewManagementAPIImpl.class);

    @Override
    @GET
    @Path("/{uuid}")
    public Response getAllReviews(
            @PathParam("uuid") String uuid,
            @QueryParam("offset") int offSet,
            @QueryParam("limit") int limit) {
        ReviewManager reviewManager = APIUtil.getCommentsManager();
        PaginationRequest request = new PaginationRequest(offSet, limit);
        try {
            PaginationResult paginationResult = reviewManager.getAllReviews(request, uuid);
            return Response.status(Response.Status.OK).entity(paginationResult).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while retrieving reviews for application UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Consumes("application/json")
    @Path("/{uuid}")
    public Response addReview(
            @ApiParam Review review,
            @PathParam("uuid") String uuid) {
        ReviewManager reviewManager = APIUtil.getCommentsManager();
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        Application application;
        ApplicationRelease applicationRelease;
        try {
            application = applicationManager.getApplicationByRelease(uuid);
            applicationRelease = applicationManager.getAppReleaseIfExists(application.getId(), uuid);
            boolean abc = reviewManager.addReview(review, application.getId(), applicationRelease.getId());
            if (abc) {
                return Response.status(Response.Status.CREATED).entity(review).build();
            } else {
                String msg = "Given review is not valid ";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while creating the review";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(msg).build();
        } catch (ApplicationManagementException e) {
//            todo
            log.error("");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("").build();
        }
    }

    @Override
    @PUT
    @Consumes("application/json")
    @Path("/review/{commentId}")
    public Response updateComment(
            @ApiParam Review review,
            @PathParam("commentId") int commentId) {

        ReviewManager reviewManager = APIUtil.getCommentsManager();
        try {
            if (commentId == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Review not found").build();
            } else if (review == null) {
                String msg = "Given review is not valid ";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else if (reviewManager.updateReview(review, commentId, true)) {
                return Response.status(Response.Status.OK).entity(review).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("").build();
            }
        } catch (ReviewManagementException e) {
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
            @PathParam("commentId") int commentId,
            @QueryParam("username") String username) {

        ReviewManager reviewManager = APIUtil.getCommentsManager();
        try {
            if (commentId == 0) {
                return Response.status(Response.Status.NOT_FOUND).entity("Review not found").build();
            } else {
                reviewManager.deleteReview(username, commentId);
            }
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while deleting the comment.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg)
                .build();
        }
        return Response.status(Response.Status.OK).entity("Review is deleted successfully.").build();
    }

    @Override
    @GET
    @Path("/{uuid}/rating")
    public Response getRating(
            @PathParam("uuid") String uuid) {

        ReviewManager reviewManager = APIUtil.getCommentsManager();
        Rating rating;
        try {
            rating = reviewManager.getRating(uuid);
        } catch (ReviewManagementException e) {
            log.error("Review Management Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(rating).build();
    }

}