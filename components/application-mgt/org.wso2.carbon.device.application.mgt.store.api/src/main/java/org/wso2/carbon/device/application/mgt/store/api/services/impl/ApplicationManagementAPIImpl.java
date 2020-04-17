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
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.exception.UnexpectedServerErrorException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.ApplicationManagementAPI;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management STORE APIs.
 */
@Produces({ "application/json" })
@Path("/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static final Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @POST
    @Override
    @Consumes("application/json")
    public Response getApplications(@Valid Filter filter) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (filter == null) {
                String msg = "Request Payload is null";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            filter.setAppReleaseState(applicationManager.getInstallableLifecycleState());
            ApplicationList applications = applicationManager.getApplications(filter);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnexpectedServerErrorException e) {
            String msg = e.getMessage();
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = e.getMessage();
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
}
