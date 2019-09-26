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
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstallResponse;
import org.wso2.carbon.device.application.mgt.common.DeviceList;
import org.wso2.carbon.device.application.mgt.common.BasicUserInfo;
import org.wso2.carbon.device.application.mgt.common.BasicUserInfoList;
import org.wso2.carbon.device.application.mgt.common.RoleList;
import org.wso2.carbon.device.application.mgt.common.DeviceGroupList;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.SubsciptionType;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.store.api.services.SubscriptionManagementAPI;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.SubscriptionManager;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
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
            @Valid List<DeviceIdentifier> deviceIdentifiers) {
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            ApplicationInstallResponse response = subscriptionManager
                    .performBulkAppOperation(uuid, deviceIdentifiers, SubsciptionType.DEVICE.toString(), action);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUI: " + uuid;
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg =
                    "Error occurred while installing the application release which has UUID: " + uuid + " for devices";
            log.error(msg);
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
            @Valid List<String> subscribers) {
        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();
            ApplicationInstallResponse response = subscriptionManager
                    .performBulkAppOperation(uuid, subscribers, subType, action);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + uuid + ". Hence, verify the payload";
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found invalid payload for installing application which has UUID: " + uuid
                    + ". Hence verify the payload";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "Application release is not in the installable state. Hence you are not permitted to install "
                    + "the application.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while installing the application release which has UUID: " + uuid
                    + " for user devices";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Path("/{uuid}/devices")
    public Response getAppInstalledDevices(@PathParam("uuid") String uuid,
                                           @DefaultValue("0")
                                           @QueryParam("offset") int offset,
                                           @DefaultValue("5")
                                           @QueryParam("limit") int limit,
                                           @QueryParam("status") String status) {

        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();

            PaginationResult subscribedDeviceDetails = subscriptionManager
                    .getAppInstalledDevices(offset, limit, uuid, status);

            DeviceList devices = new DeviceList();

            devices.setList((List<Device>) subscribedDeviceDetails.getData());
            devices.setCount(subscribedDeviceDetails.getRecordsTotal());

            return Response.status(Response.Status.OK).entity(devices).build();
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
    public Response getAppInstalledCategories(@PathParam("uuid") String uuid,
                                              @PathParam("subType") String subType,
                                              @DefaultValue("0")
                                              @QueryParam("offset") int offset,
                                              @DefaultValue("5")
                                              @QueryParam("limit") int limit) {

        try {
            SubscriptionManager subscriptionManager = APIUtil.getSubscriptionManager();

            PaginationResult subscribedCategoryDetails = subscriptionManager
                    .getAppInstalledCategories(offset, limit, uuid, subType);

            if (SubsciptionType.USER.toString().equalsIgnoreCase(subType)) {
                BasicUserInfoList users = new BasicUserInfoList();

                users.setList((List<BasicUserInfo>) subscribedCategoryDetails.getData());
                users.setCount(subscribedCategoryDetails.getRecordsTotal());

                return Response.status(Response.Status.OK).entity(users).build();
            } else if (SubsciptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                RoleList roles = new RoleList();

                roles.setList(subscribedCategoryDetails.getData());
                roles.setCount(subscribedCategoryDetails.getRecordsTotal());

                return Response.status(Response.Status.OK).entity(roles).build();
            } else if (SubsciptionType.GROUP.toString().equalsIgnoreCase(subType)) {
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
}
