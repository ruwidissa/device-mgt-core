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
package io.entgra.application.mgt.publisher.api.services.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.PaginationRequest;
import io.entgra.application.mgt.common.PaginationResult;
import io.entgra.application.mgt.common.Rating;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.ReviewManagementException;
import io.entgra.application.mgt.common.services.ReviewManager;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.publisher.api.services.admin.ReviewManagementPublisherAdminAPI;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Review Management related jax-rs APIs.
 */
@Path("/admin/reviews")
public class ReviewManagementPublisherAdminAPIImpl implements ReviewManagementPublisherAdminAPI {

    private static final Log log = LogFactory.getLog(ReviewManagementPublisherAdminAPIImpl.class);

    @Override
    @GET
    @Path("/release/{uuid}")
    public Response getAllReleaseReviews(
            @PathParam("uuid") String uuid,
            @DefaultValue("0") @QueryParam("offset") int offSet,
            @DefaultValue("20") @QueryParam("limit") int limit) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        PaginationRequest request = new PaginationRequest(offSet, limit);
        try {
            PaginationResult paginationResult = reviewManager.getAllReleaseReviews(request, uuid);
            return Response.status(Response.Status.OK).entity(paginationResult).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while retrieving reviews for application UUID: " + uuid;
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
    @Path("/{uuid}/release-rating")
    public Response getAppReleaseRating(
            @PathParam("uuid") String uuid) {
        ReviewManager reviewManager = APIUtil.getReviewManager();
        Rating rating;
        try {
            rating = reviewManager.getAppReleaseRating(uuid);
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ReviewManagementException | ApplicationManagementException e) {
            String msg = "Error occured while getting review data for application release UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(rating).build();
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