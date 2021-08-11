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

package io.entgra.application.mgt.store.api.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.ApplicationInstallResponse;
import io.entgra.application.mgt.common.ErrorResponse;
import io.entgra.application.mgt.common.SubAction;
import io.entgra.application.mgt.common.SubscriptionType;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.services.SubscriptionManager;
import io.entgra.application.mgt.core.exception.ApplicationOperationTaskException;
import io.entgra.application.mgt.common.DeviceList;
import io.entgra.application.mgt.common.BasicUserInfo;
import io.entgra.application.mgt.common.BasicUserInfoList;
import io.entgra.application.mgt.common.RoleList;
import io.entgra.application.mgt.common.DeviceGroupList;
import io.entgra.application.mgt.store.api.services.impl.util.RequestValidationUtil;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.exception.ForbiddenException;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.core.task.ScheduledAppSubscriptionTaskManager;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.store.api.services.SubscriptionManagementAPI;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.MDMAppConstants;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Properties;

/**
 * Implementation of Subscription Management related APIs.
 */
@Produces({"application/json"})
@Path("/subscription")
public class SubscriptionManagementAPIImpl implements SubscriptionManagementAPI{

    private static final Log log = LogFactory.getLog(SubscriptionManagementAPIImpl.class);

    @Override
    @POST
    @Path("/{uuid}/devices/{action}")
    public Response performAppOperationForDevices(
            @PathParam("uuid") String uuid,
            @PathParam("action") String action,
            @Valid List<DeviceIdentifier> deviceIdentifiers,
            @QueryParam("timestamp") long timestamp,
            @QueryParam("block-uninstall") Boolean isUninstallBlocked
    ) {
        Properties properties = new Properties();
        if(isUninstallBlocked != null) {
            properties.put(MDMAppConstants.AndroidConstants.IS_BLOCK_UNINSTALL, isUninstallBlocked);
        }
        try {
            if (0 == timestamp) {
                SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
                ApplicationInstallResponse response = subscriptionManager
                        .performBulkAppOperation(uuid, deviceIdentifiers, SubscriptionType.DEVICE.toString(), action, properties);
                return Response.status(Response.Status.OK).entity(response).build();
            } else {
                return scheduleApplicationOperationTask(uuid, deviceIdentifiers, SubscriptionType.DEVICE,
                        SubAction.valueOf(action.toUpperCase()), timestamp, properties);
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUI: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid + ". Hence verify "
                    + "the payload";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to perform "
                    + "the action on the application.";
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
            @QueryParam("timestamp") long timestamp,
            @QueryParam("block-uninstall") Boolean isUninstallBlocked
    ) {
        Properties properties = new Properties();
        if(isUninstallBlocked != null) {
            properties.put(MDMAppConstants.AndroidConstants.IS_BLOCK_UNINSTALL, isUninstallBlocked);
        }
        try {
            if (0 == timestamp) {
                SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
                ApplicationInstallResponse response = subscriptionManager
                        .performBulkAppOperation(uuid, subscribers, subType, action, properties);
                return Response.status(Response.Status.OK).entity(response).build();
            } else {
                return scheduleApplicationOperationTask(uuid, subscribers,
                        SubscriptionType.valueOf(subType.toUpperCase()), SubAction.valueOf(action.toUpperCase()),
                        timestamp, properties);
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + uuid + ". Hence, verify the payload";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid + ". Hence verify "
                    + "the payload";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to perform "
                    + "the action on the application.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while installing the application release which has UUID: " + uuid
                    + " for user devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Path("/{uuid}/devices/ent-app-install/{action}")
    public Response performEntAppSubscriptionOnDevices(
            @PathParam("uuid") String uuid,
            @PathParam("action") String action,
            @Valid List<DeviceIdentifier> deviceIdentifiers,
            @QueryParam("timestamp") long timestamp,
            @QueryParam("requiresUpdatingExternal") boolean requiresUpdatingExternal) {
        try {
            if (0 == timestamp) {
                SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
                subscriptionManager
                        .performEntAppSubscription(uuid, deviceIdentifiers, SubscriptionType.DEVICE.toString(),
                                action, requiresUpdatingExternal);
                String msg = "Application release which has UUID " + uuid + " is installed to given valid device "
                        + "identifiers.";
                return Response.status(Response.Status.OK).entity(msg).build();
            } else {
                return scheduleApplicationOperationTask(uuid, deviceIdentifiers, SubscriptionType.DEVICE,
                        SubAction.valueOf(SubAction.INSTALL.toString().toUpperCase()), timestamp, null);
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUI: " + uuid + " to perform ent app installation "
                    + "on subscriber's devices";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload when performing ent app installation on application which has UUID: "
                    + uuid + ". Hence verify the payload of the request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg =
                    "Error occurred while performing ent app installation on the application release which has UUID: "
                            + uuid + " for devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @POST
    @Path("/{uuid}/{subType}/ent-app-install/{action}")
    public Response performBulkEntAppSubscription(
            @PathParam("uuid") String uuid,
            @PathParam("subType") String subType,
            @PathParam("action") String action,
            @Valid List<String> subscribers,
            @QueryParam("timestamp") long timestamp,
            @QueryParam("requiresUpdatingExternal") boolean requiresUpdatingExternal) {
        try {
            if (0 == timestamp) {
                SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
                subscriptionManager.performEntAppSubscription(uuid, subscribers, subType, action, requiresUpdatingExternal);
                String msg = "Application release which has UUID " + uuid + " is installed to subscriber's valid device"
                        + " identifiers.";
                return Response.status(Response.Status.OK).entity(msg).build();
            } else {
                return scheduleApplicationOperationTask(uuid, subscribers,
                        SubscriptionType.valueOf(subType.toUpperCase()),
                        SubAction.valueOf(SubAction.INSTALL.toString().toUpperCase()), timestamp, null);
            }
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + uuid + ". Hence, verify the payload";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload when performing ent app installation on application which has UUID: "
                    + uuid + ". Hence verify the payload of the request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while performing ent app installation on the application release which has "
                    + "UUID: " + uuid + " for user devices";
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
     *                        {@see {@link io.entgra.application.mgt.common.SubscriptionType}}
     * @param subAction       action subscription action. E.g. <code>INSTALL/UNINSTALL</code>
     *                        {@see {@link io.entgra.application.mgt.common.SubAction}}
     * @param timestamp       timestamp to schedule the application subscription
     * @return {@link Response} of the operation
     */
    private Response scheduleApplicationOperationTask(String applicationUUID, List<?> subscribers,
            SubscriptionType subType, SubAction subAction, long timestamp, Properties payload) {
        try {
            ScheduledAppSubscriptionTaskManager subscriptionTaskManager = new ScheduledAppSubscriptionTaskManager();
            subscriptionTaskManager.scheduleAppSubscriptionTask(applicationUUID, subscribers, subType, subAction,
                    timestamp, payload);
        } catch (ApplicationOperationTaskException e) {
            String msg = "Error occurred while scheduling the application install operation";
            log.error(msg, e);
            ErrorResponse errorResponse = new ErrorResponse(msg);
            errorResponse.setDescription(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/{uuid}/devices")
    public Response getAppInstalledDevices(
            @QueryParam("name") String name,
            @QueryParam("user") String user,
            @QueryParam("ownership") String ownership,
            @PathParam("uuid") String uuid,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("5")
            @QueryParam("limit") int limit,
            @QueryParam("status") List<String> status) {
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            PaginationRequest request = new PaginationRequest(offset, limit);
            if (name != null && !name.isEmpty()) {
                request.setDeviceName(name);
            }
            if (user != null && !user.isEmpty()) {
                request.setOwner(user);
            }
            if (ownership != null && !ownership.isEmpty()) {
                RequestValidationUtil.validateOwnershipType(ownership);
                request.setOwnership(ownership);
            }
            if (status != null && !status.isEmpty()) {
                boolean isStatusEmpty = true;
                for (String statusString : status) {
                    if (StringUtils.isNotBlank(statusString)) {
                        isStatusEmpty = false;
                        break;
                    }
                }
                if (!isStatusEmpty) {
                    RequestValidationUtil.validateStatus(status);
                    request.setStatusList(status);
                }
            }
            PaginationResult subscribedDeviceDetails = subscriptionManager.getAppInstalledDevices(request, uuid);
            DeviceList devices = new DeviceList();
            devices.setList((List<Device>) subscribedDeviceDetails.getData());
            devices.setCount(subscribedDeviceDetails.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (NotFoundException e) {
            String msg = "Application with application release UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "User requested details are not valid";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state."
                    + "Hence you are not permitted to get the devices details.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application with the application release uuid: "
                    + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/{uuid}/{subType}")
    public Response getAppInstalledCategories(
            @PathParam("uuid") String uuid,
            @PathParam("subType") String subType,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("5")
            @QueryParam("limit") int limit) {
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();

            PaginationResult subscribedCategoryDetails = subscriptionManager
                    .getAppInstalledSubscribers(offset, limit, uuid, subType);

            if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                BasicUserInfoList users = new BasicUserInfoList();

                users.setList((List<BasicUserInfo>) subscribedCategoryDetails.getData());
                users.setCount(subscribedCategoryDetails.getRecordsTotal());

                return Response.status(Response.Status.OK).entity(users).build();
            } else if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                RoleList roles = new RoleList();

                roles.setList(subscribedCategoryDetails.getData());
                roles.setCount(subscribedCategoryDetails.getRecordsTotal());

                return Response.status(Response.Status.OK).entity(roles).build();
            } else if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                DeviceGroupList groups = new DeviceGroupList();

                groups.setList(subscribedCategoryDetails.getData());
                groups.setCount(subscribedCategoryDetails.getRecordsTotal());

                return Response.status(Response.Status.OK).entity(groups).build();
            } else {
                String msg = "Found invalid sub type ";
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
        } catch (NotFoundException e) {
            String msg = "Application with application release UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for getting application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state."
                    + "Hence you are not permitted to get the devices details.";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application with the application " +
                    "release uuid: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/{uuid}/{subType}/{subTypeName}/devices")
    public Response getAppInstalledDevicesOnCategories(
            @PathParam("uuid") String uuid,
            @PathParam("subType") String subType,
            @PathParam("subTypeName") String subTypeName,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("5")
            @QueryParam("limit") int limit,
            @QueryParam("name") String name,
            @QueryParam("user") String user,
            @QueryParam("ownership") String ownership,
            @QueryParam("status") List<String> status) {
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            PaginationRequest request = new PaginationRequest(offset, limit);

            if (StringUtils.isNotBlank(name)) {
                request.setDeviceName(name);
            }
            if (StringUtils.isNotBlank(user)) {
                request.setOwner(user);
            }
            if (StringUtils.isNotBlank(ownership)) {
                RequestValidationUtil.validateOwnershipType(ownership);
                request.setOwnership(ownership);
            }
            if (status != null && !status.isEmpty()) {
                boolean isStatusEmpty = true;
                for (String statusString : status) {
                    if (StringUtils.isNotBlank(statusString)) {
                        isStatusEmpty = false;
                        break;
                    }
                }
                if (!isStatusEmpty) {
                    RequestValidationUtil.validateStatus(status);
                    request.setStatusList(status);
                }
            }

            //todo need to update the API for other subscription types
            if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                PaginationResult subscribedCategoryDetails = subscriptionManager
                        .getAppInstalledSubscribeDevices(request, uuid, subType, subTypeName);
                DeviceList devices = new DeviceList();
                devices.setList((List<Device>) subscribedCategoryDetails.getData());
                devices.setCount(subscribedCategoryDetails.getRecordsTotal());
                return Response.status(Response.Status.OK).entity(devices).build();
            } else {
                String msg = "Found invalid sub type: " + subType;
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
        } catch (NotFoundException e) {
            String msg = "Application with application release UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application with the application " +
                    "release uuid: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
