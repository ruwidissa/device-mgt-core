/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.application.mgt.common.exception.SubscriptionManagementException;
import io.entgra.device.mgt.core.application.mgt.common.services.SubscriptionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.ActivityPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ActivityList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceActivityList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.ErrorResponse;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.common.ActivityIdList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.ActivityInfoProviderService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Path("/activities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ActivityProviderServiceImpl implements ActivityInfoProviderService {

    private static final Log log = LogFactory.getLog(ActivityProviderServiceImpl.class);

    @GET
    @Override
    @Path("/{id}")
    public Response getActivity(@PathParam("id")
                                @Size(max = 45) String id,
                                @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Activity activity;
        DeviceManagementProviderService dmService;
        Response response = validateAdminPermission();
        if (response == null) {
            try {
                RequestValidationUtil.validateActivityId(id);

                dmService = DeviceMgtAPIUtils.getDeviceManagementService();
                activity = dmService.getOperationByActivityId(id);
                if (activity == null) {
                    String msg = "No activity can be " +
                            "found upon the provided activity id '" + id + "'";
                    return Response.status(404).entity(msg).build();
                }
                return Response.status(Response.Status.OK).entity(activity).build();
            } catch (OperationManagementException e) {
                String msg = "ErrorResponse occurred while fetching the activity for the supplied id.";
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        } else {
            return response;
        }
    }

    @GET
    @Override
    @Path("/ids")
    public Response getActivities(@QueryParam("ids") ActivityIdList activityIdList) {

        List<String> idList;
        idList = activityIdList.getIdList();
        if (idList == null || idList.isEmpty()) {
            String msg = "Activities should not be empty";
            log.error(msg);
            return Response.status(400).entity(msg).build();
        }
        Response validationFailedResponse = validateAdminPermission();
        if (validationFailedResponse == null) {
            List<Activity> activities;
            ActivityList activityList = new ActivityList();
            DeviceManagementProviderService dmService;
            try {
                for (String id : idList) {
                    RequestValidationUtil.validateActivityId(id);
                }
                dmService = DeviceMgtAPIUtils.getDeviceManagementService();
                activities = dmService.getOperationByActivityIds(idList);
                if (!activities.isEmpty()) {
                    activityList.setList(activities);
                    int count = activities.size();
                    if (log.isDebugEnabled()) {
                        log.debug("Number of activities : " + count);
                    }
                    activityList.setCount(count);
                    return Response.ok().entity(activityList).build();
                } else {
                    String msg = "No activity found with the given IDs.";
                    log.error(msg);
                    return Response.status(404).entity(msg).build();
                }
            } catch (OperationManagementException e) {
                String msg = "ErrorResponse occurred while fetching the activity list for the supplied ids.";
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        } else {
            return validationFailedResponse;
        }
    }


    @GET
    @Override
    @Path("/{id}/{devicetype}/{deviceid}")
    public Response getActivityByDevice(@PathParam("id")
                                        @Size(max = 45) String id,
                                        @PathParam("devicetype")
                                        @Size(max = 45) String devicetype,
                                        @PathParam("deviceid")
                                        @Size(max = 45) String deviceid,
                                        @HeaderParam("If-Modified-Since") String ifModifiedSince,
                                        @QueryParam("response") Boolean response,
                                        @QueryParam("appInstall") Boolean appInstall) {
        Activity activity = new Activity();
        Activity appActivity = null;
        DeviceManagementProviderService dmService;
        try {
            RequestValidationUtil.validateActivityId(id);

            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceid);
            deviceIdentifier.setType(devicetype);

            dmService = DeviceMgtAPIUtils.getDeviceManagementService();

            if (appInstall != null && appInstall)  {
                if (response != null && response) {
                    activity = dmService.getOperationByActivityIdAndDevice(id, deviceIdentifier);
                }
                SubscriptionManager subscriptionManager = DeviceMgtAPIUtils.getSubscriptionManager();
                appActivity = subscriptionManager.getOperationAppDetails(id);
                if (appActivity != null) {
                    activity.setUsername(appActivity.getUsername());
                    activity.setPackageName(appActivity.getPackageName());
                    activity.setAppName(appActivity.getAppName());
                    activity.setAppType(appActivity.getAppType());
                    activity.setVersion(appActivity.getVersion());
                    activity.setTriggeredBy(appActivity.getTriggeredBy());
                } else {
                    String msg = "Cannot find the app details related to the operation ";
                    log.error(msg);
                    Response.status(404).entity(msg).build();
                }
            }   else {
                activity = dmService.getOperationByActivityIdAndDevice(id, deviceIdentifier);
            }
            if (activity == null) {
                String msg = "No activity can be " +
                        "found upon the provided activity id '" + id + "'";
                return Response.status(404).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(activity).build();
        } catch (OperationManagementException e) {
            String msg = "ErrorResponse occurred while fetching the activity for the supplied id.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (SubscriptionManagementException e) {
            String msg = "ErrorResponse occurred while fetching the app details for the supplied id.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    @Path("/type/{operationCode}")
    public Response getActivities(@PathParam("operationCode") String operationCode,
                                  @QueryParam("offset") int offset, @QueryParam("limit") int limit){
        if (log.isDebugEnabled()) {
            log.debug("getActivities -> Operation Code : " +operationCode+ "offset " + offset + " limit: " + limit );
        }
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        Response response = validateAdminPermission();
        if(response == null){
            List<Activity> activities;
            ActivityList activityList = new ActivityList();
            DeviceManagementProviderService dmService;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Calling database to get activities for the operation code :" +operationCode);
                }
                dmService = DeviceMgtAPIUtils.getDeviceManagementService();
                activities = dmService.getFilteredActivities(operationCode, limit, offset);
                activityList.setList(activities);
                if (log.isDebugEnabled()) {
                    log.debug("Calling database to get activity count.");
                }
                int count = dmService.getTotalCountOfFilteredActivities(operationCode);
                if (log.isDebugEnabled()) {
                    log.debug("Activity count: " + count);
                }
                activityList.setCount(count);
                return Response.ok().entity(activityList).build();
            } catch (OperationManagementException e) {
                String msg
                        = "ErrorResponse occurred while fetching the activities for the given operation code.";
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        } else {
            return response;
        }
    }

    @GET
    @Path("/devices")
    @Override
    public Response getDeviceActivities(@DefaultValue("0") @QueryParam("offset") int offset,
                                        @DefaultValue("20") @QueryParam("limit") int limit,
                                        @QueryParam("since") String since,
                                        @QueryParam("initiatedBy") String initiatedBy,
                                        @QueryParam("operationCode") String operationCode,
                                        @QueryParam("operationId") int operationId,
                                        @QueryParam("deviceType") String deviceType,
                                        @QueryParam("deviceId") List<String> deviceIds,
                                        @QueryParam("type") String type,
                                        @QueryParam("status") List<String> statuses,
                                        @HeaderParam("If-Modified-Since") String ifModifiedSince,
                                        @QueryParam("startTimestamp") long startTimestamp,
                                        @QueryParam("endTimestamp") long endTimestamp) {

        long ifModifiedSinceTimestamp;
        long sinceTimestamp;
        long timestamp = 0;
        boolean isTimeDurationProvided = false;
        if (log.isDebugEnabled()) {
            log.debug("getDeviceActivities since: " + since + " , offset: " + offset + " ,limit: " + limit + " ," +
                    "ifModifiedSince: " + ifModifiedSince);
        }
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            Date ifSinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try {
                ifSinceDate = format.parse(ifModifiedSince);
            } catch (ParseException e) {
                return Response.status(400).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "Invalid date string is provided in 'If-Modified-Since' header").build()).build();
            }
            ifModifiedSinceTimestamp = ifSinceDate.getTime();
            timestamp = ifModifiedSinceTimestamp / 1000;
        } else if (since != null && !since.isEmpty()) {
            Date sinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try {
                sinceDate = format.parse(since);
            } catch (ParseException e) {
                return Response.status(400).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(
                                "Invalid date string is provided in 'since' filter").build()).build();
            }
            sinceTimestamp = sinceDate.getTime();
            timestamp = sinceTimestamp / 1000;
        } else if (startTimestamp > 0 && endTimestamp > 0) {
            RequestValidationUtil.validateTimeDuration(startTimestamp, endTimestamp);
            isTimeDurationProvided = true;
        }

        if (timestamp == 0 && !isTimeDurationProvided) {
            //If timestamp is not sent by the user, a default value is set, that is equal to current time-12 hours.
            long time = System.currentTimeMillis() / 1000;
            timestamp = time - 42300;
        }
        if (log.isDebugEnabled()) {
            log.debug("getDeviceActivities final timestamp " + timestamp);
        }
        DeviceActivityList deviceActivityList = new DeviceActivityList();
        DeviceManagementProviderService dmService;
        ActivityPaginationRequest activityPaginationRequest = new ActivityPaginationRequest(offset, limit);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Calling database to get device activities.");
            }
            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            if (initiatedBy != null && !initiatedBy.isEmpty()) {
                activityPaginationRequest.setInitiatedBy(initiatedBy);
            }
            if (operationCode != null && !operationCode.isEmpty()) {
                activityPaginationRequest.setOperationCode(operationCode);
            }
            if (operationId > 0) {
                activityPaginationRequest.setOperationId(operationId);
            }
            if (deviceType != null && !deviceType.isEmpty()) {
                activityPaginationRequest.setDeviceType(deviceType);
            }
            if (deviceIds != null && !deviceIds.isEmpty()) {
                activityPaginationRequest.setDeviceIds(deviceIds);
            }
            if (type != null && !type.isEmpty()) {
                activityPaginationRequest.setType(Operation.Type.valueOf(type.toUpperCase()));
            }
            if (statuses != null && !statuses.isEmpty()) {
                List<Operation.Status> statusEnums = new ArrayList<>();
                for (String status : statuses) {
                    statusEnums.add(Operation.Status.valueOf(status.toUpperCase()));
                }
                activityPaginationRequest.setStatuses(statusEnums);
            }
            if (timestamp > 0) {
                activityPaginationRequest.setSince(timestamp);
            } else {
                activityPaginationRequest.setStartTimestamp(startTimestamp);
                activityPaginationRequest.setEndTimestamp(endTimestamp);
            }
            if (log.isDebugEnabled()) {
                log.debug("Device Activity request: " + new Gson().toJson(activityPaginationRequest));
            }
            int count = dmService.getDeviceActivitiesCount(activityPaginationRequest);
            if (log.isDebugEnabled()) {
                log.debug("Filtered Device Activity count: " + count);
            }
            if (count > 0) {
                deviceActivityList.setList(dmService.getDeviceActivities(activityPaginationRequest));
                if (log.isDebugEnabled()) {
                    log.debug("Fetched Device Activity count: " + deviceActivityList.getList().size());
                }
            } else if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                return Response.notModified().build();
            }
            deviceActivityList.setCount(count);
            return Response.ok().entity(deviceActivityList).build();
        } catch (OperationManagementException e) {
            String msg
                    = "ErrorResponse occurred while fetching the device activities updated after given time stamp.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    public Response getActivities(@DefaultValue("0") @QueryParam("offset") int offset,
                                  @DefaultValue("20") @QueryParam("limit") int limit,
                                  @QueryParam("since") String since,
                                  @QueryParam("initiatedBy") String initiatedBy,
                                  @QueryParam("operationCode") String operationCode,
                                  @QueryParam("operationId") int operationId,
                                  @QueryParam("deviceType") String deviceType,
                                  @QueryParam("deviceId") List<String> deviceIds,
                                  @QueryParam("type") String type,
                                  @QueryParam("status") List<String> statuses,
                                  @HeaderParam("If-Modified-Since") String ifModifiedSince,
                                  @QueryParam("startTimestamp") long startTimestamp,
                                  @QueryParam("endTimestamp") long endTimestamp) {

        long ifModifiedSinceTimestamp;
        long sinceTimestamp;
        long timestamp = 0;
        boolean isTimeDurationProvided = false;
        if (log.isDebugEnabled()) {
            log.debug("getActivities since: " + since + " , offset: " + offset + " ,limit: " + limit + " ," +
                    "ifModifiedSince: " + ifModifiedSince);
        }
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            Date ifSinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try {
                ifSinceDate = format.parse(ifModifiedSince);
            } catch (ParseException e) {
                String msg = "Invalid date string is provided in [If-Modified-Since] header.";
                return Response.status(400).entity(msg).build();
            }
            ifModifiedSinceTimestamp = ifSinceDate.getTime();
            timestamp = ifModifiedSinceTimestamp / 1000;
        } else if (since != null && !since.isEmpty()) {
            Date sinceDate;
            SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
            try {
                sinceDate = format.parse(since);
            } catch (ParseException e) {
                String msg = "Invalid date string is provided in [since] filter.";
                return Response.status(400).entity(msg).build();
            }
            sinceTimestamp = sinceDate.getTime();
            timestamp = sinceTimestamp / 1000;
        } else if (startTimestamp > 0 && endTimestamp > 0) {
            RequestValidationUtil.validateTimeDuration(startTimestamp, endTimestamp);
            isTimeDurationProvided = true;
        }

        if (timestamp == 0 && !isTimeDurationProvided) {
            //If timestamp is not sent by the user, a default value is set, that is equal to current time-12 hours.
            long time = System.currentTimeMillis() / 1000;
            timestamp = time - 42300;
        }
        if (log.isDebugEnabled()) {
            log.debug("getActivities final timestamp " + timestamp);
        }
        ActivityList activityList = new ActivityList();
        DeviceManagementProviderService dmService;
        ActivityPaginationRequest activityPaginationRequest = new ActivityPaginationRequest(offset, limit);
        try {
            if (log.isDebugEnabled()) {
                log.debug("Calling database to get activities.");
            }
            dmService = DeviceMgtAPIUtils.getDeviceManagementService();
            if (initiatedBy != null && !initiatedBy.isEmpty()) {
                activityPaginationRequest.setInitiatedBy(initiatedBy);
            }
            if (operationCode != null && !operationCode.isEmpty()) {
                activityPaginationRequest.setOperationCode(operationCode);
            }
            if (operationId > 0) {
                activityPaginationRequest.setOperationId(operationId);
            }
            if (deviceType != null && !deviceType.isEmpty()) {
                activityPaginationRequest.setDeviceType(deviceType);
            }
            if (deviceIds != null && !deviceIds.isEmpty()) {
                activityPaginationRequest.setDeviceIds(deviceIds);
            }
            if (type != null && !type.isEmpty()) {
                activityPaginationRequest.setType(Operation.Type.valueOf(type.toUpperCase()));
            }
            if (statuses != null && !statuses.isEmpty()) {
                List<Operation.Status> statusEnums = new ArrayList<>();
                for (String status : statuses) {
                    statusEnums.add(Operation.Status.valueOf(status.toUpperCase()));
                }
                activityPaginationRequest.setStatuses(statusEnums);
            }
            if (timestamp > 0) {
                activityPaginationRequest.setSince(timestamp);
            } else {
                activityPaginationRequest.setStartTimestamp(startTimestamp);
                activityPaginationRequest.setEndTimestamp(endTimestamp);
            }
            if (log.isDebugEnabled()) {
                log.debug("Activity request: " + new Gson().toJson(activityPaginationRequest));
            }
            int count = dmService.getActivitiesCount(activityPaginationRequest);
            if (log.isDebugEnabled()) {
                log.debug("Filtered Activity count: " + count);
            }
            if (count > 0) {
                activityList.setList(dmService.getActivities(activityPaginationRequest));
                if (log.isDebugEnabled()) {
                    log.debug("Fetched Activity count: " + activityList.getList().size());
                }
            } else if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                return Response.notModified().build();
            }
            activityList.setCount(count);
            return Response.ok().entity(activityList).build();
        } catch (OperationManagementException e) {
            String msg
                    = "ErrorResponse occurred while fetching the activities updated after given time stamp.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    private Response validateAdminPermission() {
        //TODO: also check initiated by field to check current user has added the operation, if so allow access.
        try {
            if (!DeviceMgtAPIUtils.isAdminUser()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Unauthorized operation! Only users with CDM ADMIN PERMISSION " +
                                "can perform this operation.").build();
            }
            return null;
        } catch (UserStoreException e) {
            String msg = "Error occurred while validating the user have admin permission!";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

}
