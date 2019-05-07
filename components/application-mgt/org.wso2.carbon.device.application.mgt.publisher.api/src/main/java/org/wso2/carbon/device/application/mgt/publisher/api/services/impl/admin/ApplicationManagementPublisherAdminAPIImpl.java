/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.publisher.api.services.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.services.admin.ApplicationManagementPublisherAdminAPI;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/applications")
public class ApplicationManagementPublisherAdminAPIImpl implements ApplicationManagementPublisherAdminAPI {

    private static Log log = LogFactory.getLog(ApplicationManagementPublisherAdminAPIImpl.class);

        @DELETE
        @Path("/release/{uuid}")
        public Response deleteApplicationRelease(
                @PathParam("uuid") String releaseUuid) {
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            try {
                applicationManager.deleteApplicationRelease(releaseUuid);
                String responseMsg = "Successfully deleted the application release for uuid: " + releaseUuid + "";
                return Response.status(Response.Status.OK).entity(responseMsg).build();
            } catch (NotFoundException e) {
                String msg =
                        "Couldn't found application release which is having application release UUID:" + releaseUuid;
                log.error(msg, e);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            } catch (ForbiddenException e) {
                String msg = "You don't have require permission to delete the application release which has UUID "
                        + releaseUuid;
                log.error(msg, e);
                return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
            } catch (ApplicationManagementException e) {
                String msg = "Error occurred while deleting the application release for application release UUID:: "
                        + releaseUuid;
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        }

    @DELETE
    @Path("/{appId}")
    public Response deleteApplication(
            @PathParam("appId") int applicatioId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteApplication(applicatioId);
            String responseMsg = "Successfully deleted the application which has ID: " + applicatioId + "";
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        } catch (NotFoundException e) {
            String msg =
                    "Couldn't found application release which is having the ID:" + applicatioId;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to delete the application which has ID: " + applicatioId;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application which has application ID:: " + applicatioId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

}
