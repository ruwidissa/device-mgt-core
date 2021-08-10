/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.store.api.services.impl;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.PaginationResult;
import io.entgra.application.mgt.common.Rating;
import io.entgra.application.mgt.common.response.Review;
import io.entgra.application.mgt.common.services.ReviewManager;
import io.entgra.application.mgt.common.wrapper.ReviewWrapper;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.exception.ForbiddenException;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.store.api.services.ReviewManagementAPI;
import io.entgra.application.mgt.common.PaginationRequest;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.ReviewManagementException;
import io.entgra.application.mgt.core.util.APIUtil;

import javax.ws.rs.DefaultValue;
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
@Path("/reviews")
public class ReviewManagementAPIImpl implements ReviewManagementAPI {

    private static final Log log = LogFactory.getLog(ReviewManagementAPIImpl.class);

    @Override
    @GET
    @Path("/app/user/{uuid}")
    public Response getUserReviews(
            @PathParam("uuid") String uuid,
            @DefaultValue("0") @QueryParam("offset") int offSet,
            @DefaultValue("20") @QueryParam("limit") int limit) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        PaginationRequest request = new PaginationRequest(offSet, limit);
        try {
            PaginationResult paginationResult = reviewManager.getAllAppReviewsOfUser(request, uuid);
            return Response.status(Response.Status.OK).entity(paginationResult).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application which has application release of UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while retrieving reviews for application which has application release for "
                    + "UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while retrieving application release details for application UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @GET
    @Path("/app/{uuid}")
    public Response getAllAppReviews(
            @PathParam("uuid") String uuid,
            @DefaultValue("0") @QueryParam("offset") int offSet,
            @DefaultValue("20") @QueryParam("limit") int limit) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        PaginationRequest request = new PaginationRequest(offSet, limit);
        try {
            PaginationResult paginationResult = reviewManager.getAllAppReviews(request, uuid);
            return Response.status(Response.Status.OK).entity(paginationResult).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application which has application release of UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while retrieving reviews for application which has application release for "
                    + "UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while retrieving application release details for application UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Consumes("application/json")
    @Path("/{uuid}")
    public Response addReview(
            @ApiParam ReviewWrapper reviewWrapper,
            @PathParam("uuid") String uuid) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            Review review = reviewManager.addReview(reviewWrapper, uuid, false);
            if (review != null) {
                return Response.status(Response.Status.CREATED).entity(reviewWrapper).build();
            } else {
                String msg = "Review adding is failed. Please contact the administrator.";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload data with the request. Hence, please verify the request payload.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You have already reviewed the application. Hence you are not permitted to review the "
                    + "application again.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while creating the reviewTmp";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occured while accessing application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Consumes("application/json")
    @Path("/{uuid}/{parentReviewId}")
    public Response addReplyComment(
            @ApiParam ReviewWrapper reviewWrapper,
            @PathParam("uuid") String uuid,
            @PathParam("parentReviewId") int parentReviewId) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            boolean isRepliedForReview = reviewManager.addReplyComment(reviewWrapper, uuid, parentReviewId);
            if (isRepliedForReview) {
                return Response.status(Response.Status.CREATED).entity(reviewWrapper).build();
            } else {
                String msg = "Error occured when adding reply comment for the review. Please contact the administrator..";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload data with the request to add reply comment. Hence, please verify the "
                    + "request payload.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }catch (ReviewManagementException e) {
            String msg = "Error occurred while creating the reviewTmp";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occured while accessing application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Consumes("application/json")
    @Path("/{uuid}/{reviewId}")
    public Response updateReview(
            @ApiParam ReviewWrapper updatingReview,
            @PathParam("uuid") String uuid,
            @PathParam("reviewId") int reviewId) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            Review updatedReview = reviewManager.updateReview(updatingReview, reviewId, uuid, false);
            if (updatedReview != null) {
                return Response.status(Response.Status.OK).entity(updatedReview).build();
            } else {
                String msg = "Review updating failed. Please contact the administrator";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while retrieving comments.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found application release data for UUID " + uuid + " or Review for review ID: " + reviewId;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You dont have permission to update application release review.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred when getting application release data for application release UUID:." + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();        }
    }

    @Override
    @DELETE
    @Path("/{uuid}/{reviewId}")
    public Response deleteReview(
            @PathParam("uuid") String uuid,
            @PathParam("reviewId") int reviewId) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            reviewManager.deleteReview(uuid, reviewId, false);
            return Response.status(Response.Status.OK).entity("Review is deleted successfully.").build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application review to delete which match with the request.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You are not permitted to delete the review.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while deleting the comment.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application release data.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @GET
    @Path("/{uuid}/app-rating")
    public Response getAppRating(
            @PathParam("uuid") String uuid) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        Rating rating;
        try {
            rating = reviewManager.getAppRating(uuid);
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application for application release UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ReviewManagementException | ApplicationManagementException e) {
            String msg = "Error occured while getting review data for application release UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(rating).build();
    }

}