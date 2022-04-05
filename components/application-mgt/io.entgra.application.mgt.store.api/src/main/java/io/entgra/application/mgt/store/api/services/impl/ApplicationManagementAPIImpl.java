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
package io.entgra.application.mgt.store.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.ApplicationList;
import io.entgra.application.mgt.common.Filter;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.response.Application;
import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.store.api.services.ApplicationManagementAPI;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management STORE APIs.
 */
@Produces({ "application/json" })
@Path("/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static final Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @POST
    @Path("/favourite/{appId}")
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addAppToFavourite(@PathParam("appId") int appId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.addAppToFavourites(appId);
            return Response.status(Response.Status.OK).build();
        } catch (BadRequestException e) {
            String msg = "Invalid payload found in the request. Hence verify the request payload.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while adding application to favourites";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/favourite/{appId}")
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAppFromFavourite(@PathParam("appId") int appId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.removeAppFromFavourites(appId);
            return Response.status(Response.Status.OK).build();
        } catch (BadRequestException e) {
            String msg = "Invalid payload found in the request. Hence verify the request payload.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while removing application from favourites";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/favourite")
    @Override
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getFavouriteApplications(@Valid Filter filter) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            validateFilter(filter);
            ApplicationList applications = applicationManager.getFavouriteApplications(filter);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (BadRequestException e) {
            String msg = "Invalid filter payload found in the request. Hence verify the filter payload.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while retrieving favourite applications";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    @Consumes("application/json")
    public Response getApplications(@Valid Filter filter) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            validateFilter(filter);
            filter.setAppReleaseState(applicationManager.getInstallableLifecycleState());
            ApplicationList applications = applicationManager.getApplications(filter);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (BadRequestException e) {
            String msg = "Invalid request payload found in the request. Hence verify the payload.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnexpectedServerErrorException e) {
            String msg = "Unexpected Error occurred while retrieving applications";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while retrieving applications";
            log.error(msg);
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/{uuid}")
    public Response getApplication(@PathParam("uuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application application = applicationManager
                    .getApplicationByUuid(uuid, applicationManager.getInstallableLifecycleState());
            if (application == null) {
                String msg = "Could not found an application release which is in " + applicationManager
                        .getInstallableLifecycleState() + " state.";
                log.error(msg);
                return Response.status(Response.Status.OK).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "Application with application release UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application with the application release uuid: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * This method can be used to check & validate if {@link Filter} object exist.
     *
     * @param filter {@link Filter}
     * @throws BadRequestException if filter object doesn't exist
     */
    private void validateFilter(Filter filter) throws BadRequestException {
        if (filter == null) {
            String msg = "Request Payload is null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }
}
