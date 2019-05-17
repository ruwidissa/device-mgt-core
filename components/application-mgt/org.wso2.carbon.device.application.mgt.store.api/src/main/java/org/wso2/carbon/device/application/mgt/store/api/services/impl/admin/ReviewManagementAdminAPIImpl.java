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
package org.wso2.carbon.device.application.mgt.store.api.services.impl.admin;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewDoesNotExistException;
import org.wso2.carbon.device.application.mgt.common.exception.ReviewManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ReviewManager;
import org.wso2.carbon.device.application.mgt.common.wrapper.ReviewWrapper;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.admin.ReviewManagementAdminAPI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * ReviewTmp Management related jax-rs APIs.
 */
@Path("/admin/reviews")
public class ReviewManagementAdminAPIImpl implements ReviewManagementAdminAPI {

    private static Log log = LogFactory.getLog(ReviewManagementAdminAPIImpl.class);

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
            if (reviewManager.updateReview(updatingReview, reviewId, uuid, true)) {
                return Response.status(Response.Status.OK).entity(updatingReview).build();
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
            if (reviewManager.deleteReview(uuid, reviewId)) {
                return Response.status(Response.Status.OK).entity("ReviewTmp is deleted successfully.").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ReviewTmp deleting is failed.")
                        .build();
            }
        } catch (ReviewManagementException e) {
            String msg = "Error occurred while deleting the comment.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ReviewDoesNotExistException e) {
            String msg = "Couldn't find a review for review-id: " + reviewId + " to delete.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }
}