/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.admin.SubscriptionManagementAdminAPI;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Implementation of Subscription Management related APIs.
 */
@Produces({"application/json"})
@Path("/admin/subscription")
public class SubscriptionManagementAdminAPIImpl implements SubscriptionManagementAdminAPI {

    private static final Log log = LogFactory.getLog(SubscriptionManagementAdminAPIImpl.class);

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/{uuid}")
    public Response getAppInstalledDevices(
            @QueryParam("name") String name,
            @QueryParam("user") String user,
            @QueryParam("actionStatus") String actionStatus,
            @QueryParam("status") List<String> status,
            @PathParam("uuid") String uuid,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("5")
            @QueryParam("limit") int limit) {

        try {
            PaginationRequest request = new PaginationRequest(offset, limit);
            if (name != null && !name.isEmpty()) {
                request.setDeviceName(name);
            }
            if (user != null && !user.isEmpty()) {
                request.setOwner(user);
            }
//            if (status != null && !status.isEmpty()) {
//                boolean isStatusEmpty = true;
//                for (String statusString : status){
//                    if (StringUtils.isNotBlank(statusString)){
//                        isStatusEmpty = false;
//                        break;
//                    }
//                }
//                if (!isStatusEmpty) {
//                    for (String status_ : status) {
//                        switch (status_) {
//                            case "ACTIVE":
//                            case "INACTIVE":
//                            case "UNCLAIMED":
//                            case "UNREACHABLE":
//                            case "SUSPENDED":
//                            case "DISENROLLMENT_REQUESTED":
//                            case "REMOVED":
//                            case "BLOCKED":
//                            case "CREATED":
//                                break;
//                            default:
//                                String msg = "Invalid enrollment status type: " + status_ + ". \nValid status types are " +
//                                        "ACTIVE | INACTIVE | UNCLAIMED | UNREACHABLE | SUSPENDED | " +
//                                        "DISENROLLMENT_REQUESTED | REMOVED | BLOCKED | CREATED";
//                                log.error(msg);
//                                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
//                        }
//                    }
//                    request.setStatusList(status);
//                }
//            }
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            PaginationResult subscriptionData = subscriptionManager
                    .getAppSubscriptionDetails(request, uuid, status, actionStatus);
            return Response.status(Response.Status.OK).entity(subscriptionData).build();
        } catch (NotFoundException e) {
            String msg = "Application with application release UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for getting application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting app installed devices which has application release UUID of: "
                         + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
