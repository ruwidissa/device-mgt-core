/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.common.ErrorResponse;
import org.wso2.carbon.device.application.mgt.common.SubAction;
import org.wso2.carbon.device.application.mgt.common.SubscriptionType;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationOperationTaskException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.task.ScheduledAppSubscriptionTaskManager;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.SubscriptionManagementAPI;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of Subscription Management related APIs.
 */
@Produces({"application/json"})
@Path("/subscription")
public class SubscriptionManagementAPIImpl implements SubscriptionManagementAPI{

    private static Log log = LogFactory.getLog(SubscriptionManagementAPIImpl.class);

    @Override
    @POST
    @Path("/{uuid}/devices/{action}")
    public Response performAppOperationForDevices(
            @PathParam("uuid") String uuid,
            @PathParam("action") String action,
            @Valid List<DeviceIdentifier> deviceIdentifiers,
            @QueryParam("timestamp") String timestamp) {
        try {
            if (StringUtils.isEmpty(timestamp)) {
                SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
                ApplicationInstallResponse response = subscriptionManager
                        .performBulkAppOperation(uuid, deviceIdentifiers, SubscriptionType.DEVICE.toString(), action);
                return Response.status(Response.Status.OK).entity(response).build();
            } else {
                return scheduleApplicationOperationTask(uuid, deviceIdentifiers, SubscriptionType.DEVICE,
                        SubAction.valueOf(action.toUpperCase()), timestamp);
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUI: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg =
                    "Error occurred while installing the application release which has UUID: " + uuid + " for devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Path("/{uuid}/{subType}/{action}")
    public Response performBulkAppOperation(
            @PathParam("uuid") String uuid,
            @PathParam("subType") String subType,
            @PathParam("action") String action,
            @Valid List<String> subscribers,
            @QueryParam("timestamp") String timestamp) {
        try {
            if (StringUtils.isEmpty(timestamp)) {
                SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
                ApplicationInstallResponse response = subscriptionManager
                        .performBulkAppOperation(uuid, subscribers, subType, action);
                return Response.status(Response.Status.OK).entity(response).build();
            } else {
                return scheduleApplicationOperationTask(uuid, subscribers,
                        SubscriptionType.valueOf(subType.toUpperCase()), SubAction.valueOf(action.toUpperCase()),
                        timestamp);
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + uuid + ". Hence, verify the payload";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while installing the application release which has UUID: " + uuid
                    + " for user devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Schedule the application subscription for the given timestamp
     *
     * @param applicationUUID UUID of the application to install
     * @param subscribers     list of subscribers. This list can be of
     *                        either {@link org.wso2.carbon.device.mgt.common.DeviceIdentifier} if {@param subType} is
     *                        equal to DEVICE or {@link String} if {@param subType} is USER, ROLE or GROUP
     * @param subType         subscription type. E.g. <code>DEVICE, USER, ROLE, GROUP</code>
     *                        {@see {@link org.wso2.carbon.device.application.mgt.common.SubscriptionType}}
     * @param subAction       action subscription action. E.g. <code>INSTALL/UNINSTALL</code>
     *                        {@see {@link org.wso2.carbon.device.application.mgt.common.SubAction}}
     * @param timestamp       timestamp to schedule the application subscription
     * @return {@link Response} of the operation
     */
    private Response scheduleApplicationOperationTask(String applicationUUID, List<?> subscribers,
            SubscriptionType subType, SubAction subAction, String timestamp) {
        try {
            ScheduledAppSubscriptionTaskManager subscriptionTaskManager = new ScheduledAppSubscriptionTaskManager();
            subscriptionTaskManager.scheduleAppSubscriptionTask(applicationUUID, subscribers, subType, subAction,
                    LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } catch (ApplicationOperationTaskException e) {
            String msg = "Error occurred while scheduling the application install operation";
            log.error(msg, e);
            ErrorResponse errorResponse = new ErrorResponse(msg);
            errorResponse.setDescription(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
        return Response.status(Response.Status.CREATED).build();
    }
}
