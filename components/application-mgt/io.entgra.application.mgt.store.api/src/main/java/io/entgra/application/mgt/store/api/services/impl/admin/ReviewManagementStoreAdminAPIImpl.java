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
package io.entgra.application.mgt.store.api.services.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.ReviewManagementException;
import io.entgra.application.mgt.common.services.ReviewManager;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.store.api.services.admin.ReviewManagementStoreAdminAPI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Review Management related jax-rs APIs.
 */
@Path("/admin/reviews")
public class ReviewManagementStoreAdminAPIImpl implements ReviewManagementStoreAdminAPI {

    private static final Log log = LogFactory.getLog(ReviewManagementStoreAdminAPIImpl.class);

    @Override
    @DELETE
    @Path("/{uuid}/{reviewId}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.WILDCARD)
    public Response deleteReview(
            @PathParam("uuid") String uuid,
            @PathParam("reviewId") int reviewId) {

        ReviewManager reviewManager = APIUtil.getReviewManager();
        try {
            reviewManager.deleteReview(uuid, reviewId, true);
            return Response.status(Response.Status.OK).entity("Review is deleted successfully.").build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application review to delete which match with the request.";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
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
}