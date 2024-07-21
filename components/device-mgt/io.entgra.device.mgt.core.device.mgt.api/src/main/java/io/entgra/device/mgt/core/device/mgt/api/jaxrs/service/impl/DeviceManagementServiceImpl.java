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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.entgra.device.mgt.core.application.mgt.common.ApplicationInstallResponse;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionType;
import io.entgra.device.mgt.core.application.mgt.common.exception.SubscriptionManagementException;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.services.SubscriptionManager;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.DisenrollRequest;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtUtil;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionManagerServiceImpl;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.DCRResponse;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.TokenRequest;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.TokenResponse;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.exception.KeyMgtException;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.service.KeyMgtService;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.service.KeyMgtServiceImpl;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.*;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.Application;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.MobileAppTypes;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import io.entgra.device.mgt.core.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceData;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceInfo;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLocation;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLocationHistorySnapshotWrapper;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.*;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.monitor.ComplianceData;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import io.entgra.device.mgt.core.device.mgt.common.search.PropertyMap;
import io.entgra.device.mgt.core.device.mgt.common.search.SearchContext;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceStatus;
import io.entgra.device.mgt.core.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.DeviceInformationManager;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.CommandOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.ConfigOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.ProfileOperation;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.SearchManagerService;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.SearchMgtException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.GroupManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.*;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.InputValidationException;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.identity.jwt.client.extension.JWTClient;
import io.entgra.device.mgt.core.identity.jwt.client.extension.dto.AccessTokenInfo;
import io.entgra.device.mgt.core.identity.jwt.client.extension.exception.JWTClientException;
import io.entgra.device.mgt.core.identity.jwt.client.extension.service.JWTClientManagerService;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Path("/devices")
public class DeviceManagementServiceImpl implements DeviceManagementService {

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
    private static final Log log = LogFactory.getLog(DeviceManagementServiceImpl.class);

    @GET
    @Path("/{type}/{id}/status")
    @Override
    public Response isEnrolled(@PathParam("type") String type, @PathParam("id") String id) {
        boolean result;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
        try {
            result = DeviceMgtAPIUtils.getDeviceManagementService().isEnrolled(deviceIdentifier);
            if (result) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while checking enrollment status of the device.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    public Response getDevices(
            @QueryParam("name") String name,
            @QueryParam("type") String type,
            @QueryParam("user") String user,
            @QueryParam("userPattern") String userPattern,
            @QueryParam("role") String role,
            @QueryParam("ownership") String ownership,
            @QueryParam("serialNumber") String serialNumber,
            @QueryParam("customProperty") String customProperty,
            @QueryParam("status") List<String> status,
            @QueryParam("groupId") int groupId,
            @QueryParam("excludeGroupId") int excludeGroupId,
            @QueryParam("since") String since,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("requireDeviceInfo") boolean requireDeviceInfo,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(role)) {
                String msg = "Request contains both name and role parameters. Only one is allowed at once.";
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
//            RequestValidationUtil.validateSelectionCriteria(type, user, roleName, ownership, status);
            final ObjectMapper objectMapper = new ObjectMapper();
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();
            PaginationRequest request = new PaginationRequest(offset, limit);
            PaginationResult result;
            DeviceList devices = new DeviceList();

            if (name != null && !name.isEmpty()) {
                request.setDeviceName(name);
            }
            if (customProperty != null && !customProperty.isEmpty()) {
                try {
                    Map<String, String> customProperties = objectMapper.readValue(customProperty, Map.class);
                    // Extract and set custom properties
                    for (Map.Entry<String, String> entry : customProperties.entrySet()) {
                        String propertyName = entry.getKey();
                        String propertyValue = entry.getValue();
                        // Add custom property to the paginationRequest object
                        request.addCustomProperty(propertyName, propertyValue);
                    }
                } catch (IOException e) {
                    String msg = "Error occurred while converting custom property string to a Java Map";
                    log.error(msg);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                }
            }
            if (type != null && !type.isEmpty()) {
                request.setDeviceType(type);
            }
            if (ownership != null && !ownership.isEmpty()) {
                RequestValidationUtil.validateOwnershipType(ownership);
                request.setOwnership(ownership);
            }
            if (StringUtils.isNotBlank(serialNumber)) {
                request.setSerialNumber(serialNumber);
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

            if (excludeGroupId != 0) {
                request.setGroupId(excludeGroupId);

                if (user != null && !user.isEmpty()) {
                    request.setOwner(MultitenantUtils.getTenantAwareUsername(user));
                } else if (userPattern != null && !userPattern.isEmpty()) {
                    request.setOwnerPattern(userPattern);
                }

                result = dms.getDevicesNotInGroup(request, requireDeviceInfo);
                devices.setList((List<Device>) result.getData());
                devices.setCount(result.getRecordsTotal());
                return Response.status(Response.Status.OK).entity(devices).build();
            }

            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

            if (groupId != 0) {
                try {
                    boolean isPermitted = DeviceMgtAPIUtils.checkPermission(groupId, authorizedUser);
                    if (isPermitted) {
                        request.setGroupId(groupId);
                    } else {
                        return Response.status(Response.Status.FORBIDDEN).entity(
                                new ErrorResponse.ErrorResponseBuilder().setMessage("Current user '" + authorizedUser
                                        + "' doesn't have enough privileges to list devices of group '"
                                        + groupId + "'").build()).build();
                    }
                } catch (GroupManagementException | UserStoreException e) {
                    throw new DeviceManagementException(e);
                }
            }
            if (role != null && !role.isEmpty()) {
                request.setOwnerRole(role);
            }
            authorizedUser = MultitenantUtils.getTenantAwareUsername(authorizedUser);
            // check whether the user is device-mgt admin
            if (deviceAccessAuthorizationService.isDeviceAdminUser() || request.getGroupId() > 0) {
                if (user != null && !user.isEmpty()) {
                    request.setOwner(MultitenantUtils.getTenantAwareUsername(user));
                } else if (userPattern != null && !userPattern.isEmpty()) {
                    request.setOwnerPattern(userPattern);
                }
            } else {
                if (user != null && !user.isEmpty()) {
                    user = MultitenantUtils.getTenantAwareUsername(user);
                    if (user.equals(authorizedUser)) {
                        request.setOwner(user);
                    } else {
                        String msg = "User '" + authorizedUser + "' is not authorized to retrieve devices of '" + user
                                + "' user";
                        log.error(msg);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(
                                new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                    }
                } else {
                    request.setOwner(authorizedUser);
                }
            }

            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    String msg = "Invalid date string is provided in [If-Modified-Since] header";
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
                request.setSince(sinceDate);
                if (requireDeviceInfo) {
                    result = dms.getAllDevices(request);
                } else {
                    result = dms.getAllDevices(request, false);
                }

                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                            "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else if (since != null && !since.isEmpty()) {
                Date sinceDate;
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(since);
                } catch (ParseException e) {
                    String msg = "Invalid date string is provided in [since] filter";
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
                request.setSince(sinceDate);
                if (requireDeviceInfo) {
                    result = dms.getAllDevices(request);
                } else {
                    result = dms.getAllDevices(request, false);
                }
                if (result == null || result.getData() == null || result.getData().size() <= 0) {
                    devices.setList(new ArrayList<Device>());
                    devices.setCount(0);
                    return Response.status(Response.Status.OK).entity(devices).build();
                }
            } else {
                if (requireDeviceInfo) {
                    result = dms.getAllDevices(request);
                } else {
                    result = dms.getAllDevices(request, false);
                }
                int resultCount = result.getRecordsTotal();
                if (resultCount == 0) {
                    Response.status(Response.Status.OK).entity(devices).build();
                }
            }

            devices.setList((List<Device>) result.getData());
            devices.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching all enrolled devices";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking device access authorization";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    @Path("/user-devices")
    public Response getDeviceByUser(@QueryParam("requireDeviceInfo") boolean requireDeviceInfo,
                                    @QueryParam("offset") int offset,
                                    @QueryParam("limit") int limit) {

        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        PaginationResult result;
        DeviceList devices = new DeviceList();

        String currentUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
        request.setOwner(currentUser);

        try {
            if (requireDeviceInfo) {
                result = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesOfUser(request);
            } else {
                result = DeviceMgtAPIUtils.getDeviceManagementService().getDevicesOfUser(request, false);
            }
            devices.setList((List<Device>) result.getData());
            devices.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching all enrolled devices";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    /**
     * Validate group Id and group Id greater than 0 and exist.
     *
     * @param groupId Group ID of the group
     * @param from    time to start getting DeviceLocationHistorySnapshotWrapper in milliseconds
     * @param to      time to end getting DeviceLocationHistorySnapshotWrapper in milliseconds
     */
    private static void validateGroupId(int groupId, long from, long to) throws GroupManagementException, BadRequestException {
        if (from == 0 || to == 0) {
            String msg = "Invalid values for from/to";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (groupId <= 0) {
            String msg = "Invalid group ID '" + groupId + "'";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
        if (service.getGroup(groupId, false) == null) {
            String msg = "Invalid group ID '" + groupId + "'";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    @GET
    @Override
    @Path("/{groupId}/location-history")
    public Response getDevicesGroupLocationInfo(@PathParam("groupId") int groupId,
                                                @QueryParam("from") long from,
                                                @QueryParam("to") long to,
                                                @QueryParam("type") String type,
                                                @DefaultValue("0") @QueryParam("offset") int offset,
                                                @DefaultValue("100") @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            PaginationRequest request = new PaginationRequest(offset, limit);
            DeviceList devices = new DeviceList();

            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            try {
                validateGroupId(groupId, from, to);
                boolean isPermitted = DeviceMgtAPIUtils.checkPermission(groupId, authorizedUser);
                if (isPermitted) {
                    request.setGroupId(groupId);
                } else {
                    String msg = "Current user '" + authorizedUser
                            + "' doesn't have enough privileges to list devices of group '"
                            + groupId + "'";
                    log.error(msg);
                    return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
                }
            } catch (GroupManagementException e) {
                String msg = "Error occurred while getting the data using '" + groupId + "'";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            } catch (UserStoreException e) {
                String msg = "Error occurred while retrieving role list of user '" + authorizedUser + "'";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }catch (BadRequestException e){
                String msg = "Error occurred while validating the device group.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }

            PaginationResult result = dms.getAllDevices(request, false);

            if (!result.getData().isEmpty()) {
                devices.setList((List<Device>) result.getData());

                for (Device device : devices.getList()) {
                    DeviceLocationHistorySnapshotWrapper snapshotWrapper = DeviceMgtAPIUtils.getDeviceHistorySnapshots(
                            device.getType(), device.getDeviceIdentifier(), authorizedUser, from, to, type,
                            dms);
                    device.setHistorySnapshot(snapshotWrapper);
                }
            }
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (BadRequestException e) {
            String msg = "Invalid type, use either [path] or [full]";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnAuthorizedException e) {
            String msg = "Current user doesn't have enough privileges to list devices of group '" + groupId + "'";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking device access authorization";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Path("/type/{deviceType}/id/{deviceId}")
    public Response deleteDevice(@PathParam("deviceType") String deviceType,
                                 @PathParam("deviceId") String deviceId) {
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier, true);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            boolean response = deviceManagementProviderService.disenrollDevice(deviceIdentifier);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while deleting requested device of type : " + deviceType ;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @PUT
    @Override
    @Path("/disenroll")
    public Response disenrollMultipleDevices(DisenrollRequest deviceTypeWithDeviceIds) {

        if (deviceTypeWithDeviceIds == null) {
            String errorMsg = "Invalid request. The request body must not be null.";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMsg).build();
        }
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();

        List<DeviceIdentifier> successfullyDisenrolledDevices = new ArrayList<>();
        List<DeviceIdentifier> failedToDisenrollDevices = new ArrayList<>();

        Map<String, List<String>> list = deviceTypeWithDeviceIds.getDeviceTypeWithDeviceIds();
        String deviceType;
        List<String> deviceIds;
        DeviceIdentifier deviceIdentifier;
        Device persistedDevice;
        boolean response;

        for (Map.Entry<String, List<String>> entry : list.entrySet()) {
            deviceType = entry.getKey();
            deviceIds = entry.getValue();

            for (String deviceId : deviceIds) {
                deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
                try {
                    persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier, true);
                    if (persistedDevice != null) {
                        response = deviceManagementProviderService.disenrollDevice(deviceIdentifier);
                        if (response) {
                            successfullyDisenrolledDevices.add(deviceIdentifier);
                        } else {
                            failedToDisenrollDevices.add(deviceIdentifier);
                        }
                    } else {
                        failedToDisenrollDevices.add(deviceIdentifier);
                        if(log.isDebugEnabled()){
                            String msg = "Error encountered while dis-enrolling device of type: " + deviceType + " with " + deviceId;
                            log.error(msg);
                        }
                    }
                } catch (DeviceManagementException e) {
                    String msg = "Error encountered while dis-enrolling device of type: " + deviceType + " with " + deviceId;
                    log.error(msg, e);
                    failedToDisenrollDevices.add(deviceIdentifier);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                }
            }
        }

        Map<String, List<DeviceIdentifier>> responseMap = new HashMap<>();
        responseMap.put("successfullyDisenrollDevices", successfullyDisenrolledDevices);
        responseMap.put("failedToDisenrollDevices", failedToDisenrollDevices);

        return Response.status(Response.Status.OK).entity(responseMap).build();
    }
    @POST
    @Override
    @Path("/type/{deviceType}/id/{deviceId}/rename")
    public Response renameDevice(Device device, @PathParam("deviceType") String deviceType,
                                 @PathParam("deviceId") String deviceId) {
        if (device == null) {
            String msg = "Required values are not set to rename device";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        if (StringUtils.isEmpty(device.getName())) {
            String msg = "Device name is not set to rename device";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            Device updatedDevice = deviceManagementProviderService.updateDeviceName(device, deviceType, deviceId);
            if (updatedDevice != null) {
                boolean notificationResponse = deviceManagementProviderService.sendDeviceNameChangedNotification(updatedDevice);
                if (notificationResponse) {
                    return Response.status(Response.Status.CREATED).entity(updatedDevice).build();
                } else {
                    String msg = "Device updated successfully, but failed to send notification.";
                    log.warn(msg);
                    return Response.status(Response.Status.CREATED).entity(updatedDevice).header("Warning", msg).build();
                }
            } else {
                String msg = "Device update failed for device of type : " + deviceType;
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (BadRequestException e) {
            String msg = "Bad request: " + e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceNotFoundException e) {
            String msg = "Device not found: " + e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while updating requested device of type : " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ConflictException e) {
            String msg = "Conflict encountered while updating requested device of type : " + deviceType;
            log.error(msg, e);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        }
    }

    @GET
    @Path("/{type}/{id}")
    @Override
    public Response getDevice(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @QueryParam("owner") @Size(max = 100) String owner,
            @QueryParam("ownership") @Size(max = 100) String ownership,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        Device device;
        Date sinceDate = null;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();

            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            // check whether the user is authorized
            String requiredPermission = PermissionManagerServiceImpl.getInstance().getRequiredPermission();
            String[] requiredPermissions = new String[] {requiredPermission};
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser, requiredPermissions)) {
                String msg = "User '" + authorizedUser + "' is not authorized to retrieve the given device id '" + id + "'";
                log.error(msg);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_UNAUTHORIZED).setMessage(msg).build()).build();
            }

            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceIdentifier(deviceIdentifier);

            if (!StringUtils.isBlank(ifModifiedSince)) {
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                    deviceData.setLastModifiedDate(sinceDate);
                } catch (ParseException e) {
                    String msg = "Invalid date string is provided in [If-Modified-Since] header";
                    log.error(msg, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
            }

            if (!StringUtils.isBlank(owner)) {
                deviceData.setDeviceOwner(owner);
            }
            if (!StringUtils.isBlank(ownership)) {
                deviceData.setDeviceOwnership(ownership);
            }
            device = dms.getDevice(deviceData, true);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking the device authorization.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        if (device == null) {
            if (sinceDate != null) {
                return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                        "after the timestamp provided in 'If-Modified-Since' header").build();
            }
            String msg = "Requested device of type " + type + " does not exist";
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(device).build();
    }

    @GET
    @Path("/{deviceType}/{deviceId}/location-history")
    public Response getDeviceLocationInfo(
            @PathParam("deviceType") String deviceType,
            @PathParam("deviceId") String deviceId,
            @QueryParam("from") long from,
            @QueryParam("to") long to,
            @QueryParam("type") String type) {
        try {
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
            DeviceLocationHistorySnapshotWrapper snapshotWrapper = DeviceMgtAPIUtils.getDeviceHistorySnapshots(
                    deviceType, deviceId, authorizedUser, from, to, type,
                    dms);
            return Response.status(Response.Status.OK).entity(snapshotWrapper).build();
        } catch (BadRequestException e) {
            String msg = "Invalid type, use either [path] or [full]";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnAuthorizedException e) {
            String msg = "Current user doesn't have enough privileges to retrieve the given device id '"
                    + deviceId + "'";
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while fetching the device information.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InputValidationException e) {
            String msg = "Invalid device Id or device type";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking device access authorization";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/type/any/id/{id}")
    @Override
    public Response getDeviceByID(
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("requireDeviceInfo") boolean requireDeviceInfo) {
        Device device;
        try {
            RequestValidationUtil.validateDeviceIdentifier("any", id);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            DeviceAccessAuthorizationService deviceAccessAuthorizationService =
                    DeviceMgtAPIUtils.getDeviceAccessAuthorizationService();

            // this is the user who initiates the request
            String authorizedUser = CarbonContext.getThreadLocalCarbonContext().getUsername();

            Date sinceDate = null;
            if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                } catch (ParseException e) {
                    String message = "Invalid date string is provided in [If-Modified-Since] header";
                    log.error(message, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(message).build();
                }
            }
            if (sinceDate != null) {
                device = dms.getDevice(id, sinceDate, requireDeviceInfo);
                if (device == null) {
                    String message = "No device is modified after the timestamp provided in 'If-Modified-Since' header";
                    log.error(message);
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                            "after the timestamp provided in [If-Modified-Since] header").build();
                }
            } else {
                device = dms.getDevice(id, requireDeviceInfo);
            }
            if (device == null) {
                String message = "Device does not exist";
                log.error(message);
                return Response.status(Response.Status.NOT_FOUND).entity(message).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, device.getType());
            // check whether the user is authorized
            String requiredPermission = PermissionManagerServiceImpl.getInstance().getRequiredPermission();
            String[] requiredPermissions = new String[] {requiredPermission};
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser, requiredPermissions)) {
                String message = "User '" + authorizedUser + "' is not authorized to retrieve the given " +
                        "device id '" + id + "'";
                log.error(message);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(401l).setMessage(message).build()).build();
            }
        } catch (DeviceManagementException e) {
            String message = "Error occurred while fetching the device information.";
            log.error(message, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(message).build()).build();
        } catch (DeviceAccessAuthorizationException e) {
            String message = "Error occurred while checking the device authorization.";
            log.error(message, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(message).build()).build();
        }
        return Response.status(Response.Status.OK).entity(device).build();
    }

    @POST
    @Path("/enrollment/guide")
    @Override
    public Response sendEnrollmentGuide(String enrolmentGuide) {
        if (log.isDebugEnabled()) {
            log.debug("Sending enrollment invitation mail to existing user.");
        }
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        if (!config.getEnrollmentGuideConfiguration().isEnabled()) {
            String msg = "Sending enrollment guide config is not enabled.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        }
        DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            dms.sendEnrolmentGuide(enrolmentGuide);
            return Response.status(Response.Status.OK).entity("Invitation mails have been sent.").build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred  sending mail to group in enrollment guide";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }


    @POST
    @Path("/type/any/list")
    @Override
    public Response getDeviceByIdList(List<String> deviceIds) {
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        if (deviceIds == null || deviceIds.isEmpty()) {
            String msg = "Required values of device identifiers are not set.";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            List<Device> devices = deviceManagementProviderService.getDeviceByIdList(deviceIds);
            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (DeviceManagementException e) {
            String msg = "Error encountered while retrieving devices";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/{type}/{id}/location")
    @Override
    public Response getDeviceLocation(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        DeviceInformationManager informationManager;
        DeviceLocation deviceLocation;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(id);
            deviceIdentifier.setType(type);
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceLocation = informationManager.getDeviceLocation(deviceIdentifier);
        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device location.";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceLocation).build();

    }


    @GET
    @Path("/{type}/{id}/info")
    @Override
    public Response getDeviceInformation(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        DeviceInformationManager informationManager;
        DeviceInfo deviceInfo;
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(id);
            deviceIdentifier.setType(type);
            informationManager = DeviceMgtAPIUtils.getDeviceInformationManagerService();
            deviceInfo = informationManager.getDeviceInfo(deviceIdentifier);

        } catch (DeviceDetailsMgtException e) {
            String msg = "Error occurred while getting the device information of id : " + id + " type : " + type;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceInfo).build();

    }
    @GET
    @Path("/{type}/{id}/config")
    @Override
    public Response getDeviceConfiguration(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {

        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.setDeviceId(id);
        deviceConfig.setType(type);

        // find token validity time
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        int validityTime = 3600;
        // add scopes for event topics
        List<String> mqttEventTopicStructure = new ArrayList<>();
        try {
            DeviceType deviceType = deviceManagementProviderService.getDeviceType(type);
            if (deviceType != null) {
                if (deviceType.getDeviceTypeMetaDefinition().isLongLivedToken()) {
                    validityTime = Integer.MAX_VALUE;
                }
                mqttEventTopicStructure = deviceType.getDeviceTypeMetaDefinition().getMqttEventTopicStructures();
            } else {
                String msg = "Device not found, device id : " +  id + ", device type : " + type;
                log.error(msg);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device, device id : " +  id + ", device type : " + type;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String applicationName = type.replace(" ", "").replace("_", "")
                + "_" + tenantDomain;

        KeyMgtService keyMgtService = new KeyMgtServiceImpl();
        try {
            DCRResponse dcrResponse = keyMgtService.dynamicClientRegistration(applicationName, username,
                    "client_credentials", null, new String[] {"device_management"}, false, validityTime);
            deviceConfig.setClientId(dcrResponse.getClientId());
            deviceConfig.setClientSecret(dcrResponse.getClientSecret());

            StringBuilder scopes = new StringBuilder("device:" + type.replace(" ", "") + ":" + id);
            for (String topic : mqttEventTopicStructure) {
                if (topic.contains("${deviceId}")) {
                    topic = topic.replace("${deviceId}", id);
                }
                topic = topic.replace("/",":");
//                scopes.append(" perm:topic:sub:".concat(topic));
                scopes.append(" perm:topic:pub:".concat(topic));
            }

            // add scopes for retrieve operation topic /tenantDomain/deviceType/deviceId/operation/#
            scopes.append(" perm:topic:sub:" + tenantDomain + ":" + type + ":" + id + ":operation");

            // add scopes for update operation /tenantDomain/deviceType/deviceId/update/operation
            scopes.append(" perm:topic:pub:" + tenantDomain + ":" + type + ":" + id + ":update:operation");

            TokenRequest tokenRequest = new TokenRequest(dcrResponse.getClientId(), dcrResponse.getClientSecret(),
                    null, scopes.toString(), "client_credentials", null,
                    null, null, null,  validityTime);
            TokenResponse tokenResponse = keyMgtService.generateAccessToken(tokenRequest);
            deviceConfig.setAccessToken(tokenResponse.getAccessToken());
            deviceConfig.setRefreshToken(tokenResponse.getRefreshToken());

            try {
                deviceConfig.setPlatformConfiguration(deviceManagementProviderService.getConfiguration(type));
            } catch (DeviceManagementException e) {
                String msg = "Error occurred while reading platform configurations token, device id : " +  id + ", device type : " + type;
                log.error(msg, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
            }

            deviceConfig.setMqttGateway("tcp://" + System.getProperty("mqtt.broker.host") + ":" + System.getProperty("mqtt.broker.port"));
            deviceConfig.setHttpGateway("http://" + System.getProperty("iot.gateway.host") + ":" + System.getProperty("iot.gateway.http.port"));
            deviceConfig.setHttpsGateway("https://" + System.getProperty("iot.gateway.host") + ":" + System.getProperty("iot.gateway.https.port"));

        } catch (KeyMgtException e) {
            String msg = "Error occurred while creating oauth application, device id : " +  id + ", device type : " + type;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (io.entgra.device.mgt.core.apimgt.keymgt.extension.exception.BadRequestException e) {
            String msg = "Error occurred while generating token, device id : " +  id + ", device type : " + type;
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceConfig).build();

    }

    @GET
    @Path("/device-type/{type}/features")
    @Override
    public Response getFeaturesOfDevice(
            @PathParam("type") @Size(max = 45) String type,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        List<Feature> features = new ArrayList<>();
        DeviceManagementProviderService dms;
        try {
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            FeatureManager fm;
            try {
                fm = dms.getFeatureManager(type);
            } catch (DeviceTypeNotFoundException e) {
                String msg = "No device type found with name : " + type ;
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            if (fm != null) {
                features = fm.getFeatures();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of features of '" + type + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        return Response.status(Response.Status.OK).entity(features).build();
    }

    @POST
    @Path("/search-devices")
    @Override
    public Response searchDevices(@QueryParam("offset") int offset,
                                  @QueryParam("limit") int limit, SearchContext searchContext) {
        SearchManagerService searchManagerService;
        List<Device> devices;
        DeviceList deviceList = new DeviceList();
        try {
            searchManagerService = DeviceMgtAPIUtils.getSearchManagerService();
            devices = searchManagerService.search(searchContext);
        } catch (SearchMgtException e) {
            String msg = "Error occurred while searching for devices that matches the provided selection criteria";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        deviceList.setList(devices);
        deviceList.setCount(devices.size());
        return Response.status(Response.Status.OK).entity(deviceList).build();
    }

    @POST
    @Path("/query-devices")
    @Override
    public Response queryDevicesByProperties(@QueryParam("offset") int offset,
                                             @QueryParam("limit") int limit, PropertyMap map) {
        List<Device> devices;
        DeviceList deviceList = new DeviceList();
        try {
            if (map.getProperties().isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No search criteria defined when querying devices.");
                }
                return Response.status(Response.Status.BAD_REQUEST).entity("No search criteria defined.").build();
            }
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            devices = dms.getDevicesBasedOnProperties(map.getProperties());
            if (devices == null || devices.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No Devices Found for criteria : " + map);
                }
                return Response.status(Response.Status.NOT_FOUND).entity("No device found matching query criteria.").build();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while searching for devices that matches the provided device properties";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        deviceList.setList(devices);
        deviceList.setCount(devices.size());
        return Response.status(Response.Status.OK).entity(deviceList).build();
    }

    @GET
    @Path("/{type}/{id}/applications")
    @Override
    public Response getInstalledApplications(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        List<Application> applications;
        ApplicationManagementProviderService amc;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(id, false);
            amc = DeviceMgtAPIUtils.getAppManagementService();
            applications = amc.getApplicationListForDevice(device);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while fetching the apps of the '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @POST
    @Path("/{type}/{id}/uninstallation")
    @Override
    public Response uninstallation(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @QueryParam("packageName") String packageName,
            @QueryParam("platform") String platform,
            @QueryParam("name") String name,
            @QueryParam("version") String version,
            @QueryParam("user") String user) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            Device device = DeviceMgtAPIUtils.getDeviceManagementService().getDevice(id, false);
            ApplicationManagementProviderService amc = DeviceMgtAPIUtils.getAppManagementService();
            List<Application> applications = amc.getApplicationListForDevice(device);
            //checking requested package names are valid or not
            RequestValidationUtil.validateApplicationIdentifier(packageName, applications);
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            deviceIdentifiers.add(deviceIdentifier);
            SubscriptionManager subscriptionManager = DeviceMgtAPIUtils.getSubscriptionManager();
            String UUID = subscriptionManager.checkAppSubscription(device.getId(), packageName);
            // UUID is available means app is subscribed in the entgra store
            if (UUID != null) {
                ApplicationInstallResponse response = subscriptionManager
                        .performBulkAppOperation(UUID, deviceIdentifiers, SubscriptionType.DEVICE.toString(),
                                "uninstall", new Properties());
                return Response.status(Response.Status.OK).entity(response).build();
                //if the applications not installed via entgra store
            } else {
                if (Constants.ANDROID.equals(type)) {
                    ApplicationUninstallation applicationUninstallation = new ApplicationUninstallation(packageName,
                            MobileAppTypes.PUBLIC.toString(), name, platform, version, user);
                    ProfileOperation operation = new ProfileOperation();
                    operation.setCode(MDMAppConstants.AndroidConstants.UNMANAGED_APP_UNINSTALL);
                    operation.setType(Operation.Type.PROFILE);
                    operation.setPayLoad(applicationUninstallation.toJson());
                    DeviceManagementProviderService deviceManagementProviderService = HelperUtil
                            .getDeviceManagementProviderService();
                    Activity activity = deviceManagementProviderService.addOperation(
                            DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_ANDROID, operation, deviceIdentifiers);
                    return Response.status(Response.Status.CREATED).entity(activity).build();
                } else {
                    String msg = "Not implemented for other device types";
                    log.error(msg);
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (SubscriptionManagementException |
                io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException
                e) {
            String msg = "Error occurred while getting the " + type + "application is of device " + id + "subscribed " +
                    "at entgra store";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while fetching the apps of the '" + type + "' device, which carries " +
                    "the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (OperationManagementException e) {
            String msg = "Issue in retrieving operation management service instance";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (InvalidDeviceException e) {
            String msg = "The list of device identifiers are invalid";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/{type}/{id}/operations")
    @Override
    public Response getDeviceOperations(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit,
            @QueryParam("owner") String owner,
            @QueryParam("ownership") String ownership,
            @QueryParam("createdFrom") Long createdFrom,
            @QueryParam("createdTo") Long createdTo,
            @QueryParam("updatedFrom") Long updatedFrom,
            @QueryParam("updatedTo") Long updatedTo,
            @QueryParam("operationCode") List<String> operationCode,
            @QueryParam("operationStatus") List<String> status) {
        OperationList operationsList = new OperationList();
        RequestValidationUtil requestValidationUtil = new RequestValidationUtil();
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        if(owner != null){
            request.setOwner(owner);
        }
        try {
            //validating the operation log filters
            OperationLogFilters olf = requestValidationUtil.validateOperationLogFilters(operationCode, createdFrom,
                    createdTo, updatedFrom, updatedTo, status, type);
            request.setOperationLogFilters(olf);
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            if (!StringUtils.isBlank(ownership)) {
                request.setOwnership(ownership);
            }
            PaginationResult result = dms.getOperations(new DeviceIdentifier(id, type), request);
            operationsList.setList((List<? extends Operation>) result.getData());
            operationsList.setCount(result.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(operationsList).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred while fetching the operations for the '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (InputValidationException e) {
            String msg = "Error occurred while fetching the operations for the type : " + type + " device";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of [" + type + "] features with params " +
                    "{featureType: operation, hidden: true}";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceTypeNotFoundException e) {
            String msg = "No device type found with name : " + type ;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    @GET
    @Path("/{type}/{id}/effective-policy")
    @Override
    public Response getEffectivePolicyOfDevice(@PathParam("type") @Size(max = 45) String type,
                                               @PathParam("id") @Size(max = 45) String id,
                                               @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            Device device = DeviceMgtAPIUtils.getDeviceManagementService()
                    .getDevice(new DeviceIdentifier(id, type), false);
            PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
            Policy policy = policyManagementService.getAppliedPolicyToDevice(device);

            return Response.status(Response.Status.OK).entity(policy).build();
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving '" + type + "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("{type}/{id}/compliance-data")
    public Response getComplianceDataOfDevice(@PathParam("type") @Size(max = 45) String type,
                                              @PathParam("id") @Size(max = 45) String id) {

        RequestValidationUtil.validateDeviceIdentifier(type, id);
        Device device;
        try {
            device = DeviceMgtAPIUtils.getDeviceManagementService()
                    .getDevice(new DeviceIdentifier(id, type), false);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving '" + type + "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
        PolicyManagerService policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
        Policy policy;
        NonComplianceData complianceData;
        DeviceCompliance deviceCompliance = new DeviceCompliance();

        try {
            policy = policyManagementService.getAppliedPolicyToDevice(device);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the current policy associated with the '" + type +
                    "' device, which carries the id '" + id + "'";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }

        if (policy == null) {
            deviceCompliance.setDeviceID(id);
            deviceCompliance.setComplianceData(null);
            return Response.status(Response.Status.OK).entity(deviceCompliance).build();
        } else {
            try {
                policyManagementService = DeviceMgtAPIUtils.getPolicyManagementService();
                complianceData = policyManagementService.getDeviceCompliance(device);
                deviceCompliance.setDeviceID(id);
                deviceCompliance.setComplianceData(complianceData);
                return Response.status(Response.Status.OK).entity(deviceCompliance).build();
            } catch (PolicyComplianceException e) {
                String error = "Error occurred while getting the compliance data.";
                log.error(error, e);
                return Response.serverError().entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage(error).build()).build();
            }
        }
    }

    /**
     * Change device status.
     *
     * @param type       Device type
     * @param id         Device id
     * @param newsStatus Device new status
     * @return {@link Response} object
     */
    @PUT
    @Path("/{type}/{id}/changestatus")
    public Response changeDeviceStatus(@PathParam("type") @Size(max = 45) String type,
                                       @PathParam("id") @Size(max = 45) String id,
                                       @QueryParam("newStatus") EnrolmentInfo.Status newsStatus) {
        RequestValidationUtil.validateDeviceIdentifier(type, id);
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier, false);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            boolean response = deviceManagementProviderService.changeDeviceStatus(deviceIdentifier, newsStatus);
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while changing device status of device type : " + type ;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    /**
     * List device status history
     *
     * @param type Device type
     * @param id   Device id
     * @return {@link Response} object
     */
    @GET
    @Path("/{type}/{id}/status-history")
    public Response getDeviceStatusHistory(@PathParam("type") @Size(max = 45) String type,
                                           @PathParam("id") @Size(max = 45) String id) {
        //TODO check authorization for this
        RequestValidationUtil.validateDeviceIdentifier(type, id);
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier, false);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<DeviceStatus> deviceStatusHistory = deviceManagementProviderService.getDeviceStatusHistory(persistedDevice);
            return Response.status(Response.Status.OK).entity(deviceStatusHistory).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device status history for device of type : " + type ;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    /**
     * List device status history for the current enrolment
     *
     * @param type Device type
     * @param id   Device id
     * @return {@link Response} object
     */
    @GET
    @Path("/{type}/{id}/enrolment-status-history")
    public Response getCurrentEnrolmentDeviceStatusHistory(@PathParam("type") @Size(max = 45) String type,
                                                           @PathParam("id") @Size(max = 45) String id) {
        //TODO check authorization for this or current enrolment should be based on for the enrolment associated with the user
        RequestValidationUtil.validateDeviceIdentifier(type, id);
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, type);
            Device persistedDevice = deviceManagementProviderService.getDevice(deviceIdentifier, false);
            if (persistedDevice == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<DeviceStatus> deviceStatusHistory = deviceManagementProviderService.getDeviceCurrentEnrolmentStatusHistory(persistedDevice);
            return Response.status(Response.Status.OK).entity(deviceStatusHistory).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device status history for device of type : " + type;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Path("/{type}/operations")
    public Response addOperation(@PathParam("type") String type, @Valid OperationRequest operationRequest) {
        try {
            if (operationRequest == null || operationRequest.getDeviceIdentifiers() == null
                    || operationRequest.getOperation() == null) {
                String errorMessage = "Operation cannot be empty";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (!DeviceMgtAPIUtils.getDeviceManagementService().getAvailableDeviceTypes().contains(type)) {
                String errorMessage = "Device Type is invalid";
                log.error(errorMessage);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            Operation.Type operationType = operationRequest.getOperation().getType();
            if (operationType == Operation.Type.COMMAND || operationType == Operation.Type.CONFIG || operationType == Operation.Type.PROFILE) {
                DeviceIdentifier deviceIdentifier;
                List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
                for (String deviceId : operationRequest.getDeviceIdentifiers()) {
                    deviceIdentifier = new DeviceIdentifier();
                    deviceIdentifier.setId(deviceId);
                    deviceIdentifier.setType(type);
                    deviceIdentifiers.add(deviceIdentifier);
                }
                Operation operation;
                if (operationType == Operation.Type.COMMAND) {
                    Operation commandOperation = operationRequest.getOperation();
                    operation = new CommandOperation();
                    operation.setType(Operation.Type.COMMAND);
                    operation.setCode(commandOperation.getCode());
                    operation.setEnabled(commandOperation.isEnabled());
                    operation.setStatus(commandOperation.getStatus());

                } else if (operationType == Operation.Type.CONFIG) {
                    Operation configOperation = operationRequest.getOperation();
                    operation = new ConfigOperation();
                    operation.setType(Operation.Type.CONFIG);
                    operation.setCode(configOperation.getCode());
                    operation.setEnabled(configOperation.isEnabled());
                    operation.setPayLoad(configOperation.getPayLoad());
                    operation.setStatus(configOperation.getStatus());

                } else {
                    Operation profileOperation = operationRequest.getOperation();
                    operation = new ProfileOperation();
                    operation.setType(Operation.Type.PROFILE);
                    operation.setCode(profileOperation.getCode());
                    operation.setEnabled(profileOperation.isEnabled());
                    operation.setPayLoad(profileOperation.getPayLoad());
                    operation.setStatus(profileOperation.getStatus());
                }
                String date = new SimpleDateFormat(DATE_FORMAT_NOW).format(new Date());
                operation.setCreatedTimeStamp(date);
                Activity activity = DeviceMgtAPIUtils.getDeviceManagementService().addOperation(type, operation,
                        deviceIdentifiers);
                return Response.status(Response.Status.CREATED).entity(activity).build();
            } else {
                String message = "Only Command and Config operation is supported through this api";
                return Response.status(Response.Status.NOT_ACCEPTABLE).entity(message).build();
            }

        } catch (InvalidDeviceException e) {
            String errorMessage = "Invalid Device Identifiers found.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (OperationManagementException e) {
            String errorMessage = "Issue in retrieving operation management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Issue in retrieving device management service instance";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        } catch (InvalidConfigurationException e) {
            log.error("failed to add operation", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Override
    @Path("/type/{type}/status/{status}/count")
    public Response getDeviceCountByStatus(@PathParam("type") String type, @PathParam("status") String status) {
        int deviceCount;
        try {
            deviceCount = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCountOfTypeByStatus(type, status);
            return Response.status(Response.Status.OK).entity(deviceCount).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while retrieving device count.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @GET
    @Override
    @Path("/type/{type}/status/{status}/ids")
    public Response getDeviceIdentifiersByStatus(@PathParam("type") String type, @PathParam("status") String status) {
        List<String> deviceIds;
        try {
            deviceIds = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceIdentifiersByStatus(type, status);
            return Response.status(Response.Status.OK).entity(deviceIds.toArray(new String[0])).build();
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while obtaining list of devices";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @PUT
    @Override
    @Path("/type/{type}/status/{status}")
    public Response bulkUpdateDeviceStatus(@PathParam("type") String type, @PathParam("status") String status,
                                           @Valid List<String> deviceList) {
        try {
            DeviceMgtAPIUtils.getDeviceManagementService().bulkUpdateDeviceStatus(type, deviceList, status);
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while updating device status in bulk.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Override
    @Path("/compliance/{complianceStatus}")
    public Response getPolicyCompliance(
            @PathParam("complianceStatus") boolean complianceStatus,
            @QueryParam("policy") String policyId,
            @DefaultValue("false")
            @QueryParam("pending") boolean isPending,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("10")
            @QueryParam("limit") int limit) {

        PaginationRequest request = new PaginationRequest(offset, limit);
        ComplianceDeviceList complianceDeviceList = new ComplianceDeviceList();
        PaginationResult paginationResult;
        try {

            PolicyManagerService policyManagerService = DeviceMgtAPIUtils.getPolicyManagementService();
            paginationResult = policyManagerService.getPolicyCompliance(request, policyId, complianceStatus, isPending, fromDate, toDate);

            if (paginationResult.getData().isEmpty()) {
                return Response.status(Response.Status.OK)
                        .entity("No policy compliance or non compliance devices are available").build();
            } else {
                complianceDeviceList.setList((List<ComplianceData>) paginationResult.getData());
                complianceDeviceList.setCount(paginationResult.getRecordsTotal());
                return Response.status(Response.Status.OK).entity(complianceDeviceList).build();
            }
        } catch (PolicyComplianceException e) {
            String msg = "Error occurred while retrieving compliance data";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    @Path("/{id}/features")
    public Response getNoneComplianceFeatures(
            @PathParam("id") int id) {
        List<ComplianceFeature> complianceFeatureList;
        try {
            PolicyManagerService policyManagerService = DeviceMgtAPIUtils.getPolicyManagementService();
            complianceFeatureList = policyManagerService.getNoneComplianceFeatures(id);

            if (complianceFeatureList.isEmpty()) {
                return Response.status(Response.Status.OK).entity("No non compliance features are available").build();
            } else {
                return Response.status(Response.Status.OK).entity(complianceFeatureList).build();
            }
        } catch (PolicyComplianceException e) {
            String msg = "Error occurred while retrieving non compliance features";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Override
    @Consumes("application/json")
    @Path("/{deviceType}/applications")
    public Response getApplications(
            @PathParam("deviceType") String deviceType,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("10")
            @QueryParam("limit") int limit,
            @QueryParam("appName") String appName,
            @QueryParam("packageName") String packageName) {
        PaginationRequest request = new PaginationRequest(offset, limit);
        ApplicationList applicationList = new ApplicationList();
        request.setDeviceType(deviceType);
        request.setFilter(DeviceMgtUtil.buildAppSearchFilter(appName, packageName));

        try {
            PaginationResult paginationResult = DeviceMgtAPIUtils
                    .getDeviceManagementService()
                    .getApplications(request);

            if (paginationResult.getData().isEmpty()) {
                return Response.status(Response.Status.OK)
                        .entity("No applications are available under " + deviceType + " platform.").build();
            } else {
                applicationList.setList((List<Application>) paginationResult.getData());
                applicationList.setCount(paginationResult.getRecordsTotal());
                return Response.status(Response.Status.OK).entity(applicationList).build();
            }
        } catch (DeviceTypeNotFoundException e) {
            String msg = "Error occurred while retrieving application list." +
                    " Device type (Application Platform): " + deviceType +
                    "is not valid";
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while retrieving application list";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/application/{packageName}/versions")
    @Override
    public Response getAppVersions(
            @PathParam("packageName") String packageName) {
        try {
            List<String> versions = DeviceMgtAPIUtils.getDeviceManagementService()
                    .getAppVersions(packageName);
            return Response.status(Response.Status.OK).entity(versions).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while retrieving version list for app with package name " + packageName;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Path("/{deviceType}/{id}/operation")
    @Override
    public Response updateOperationStatus(
            @PathParam("deviceType") String deviceType,
            @PathParam("id") String deviceId,
            OperationStatusBean operationStatusBean) {
        if (log.isDebugEnabled()) {
            log.debug("Requesting device information from " + deviceId);
        }
        if (operationStatusBean == null) {
            String errorMessage = "Request does not contain the required payload.";
            log.error(errorMessage);
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Device device = DeviceMgtAPIUtils.getDeviceManagementService()
                    .getDevice(deviceIdentifier, false);
            DeviceType deviceTypeObj = DeviceManagerUtil.getDeviceType(
                    deviceType, tenantId);
            if (deviceTypeObj == null) {
                String msg = "Device of type: " + deviceType + " does not exist";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            Operation operation = DeviceMgtAPIUtils.validateOperationStatusBean(operationStatusBean);
            operation.setId(operationStatusBean.getOperationId());
            operation.setCode(operationStatusBean.getOperationCode());
            DeviceMgtAPIUtils.getDeviceManagementService().updateOperation(device, operation);

            if (MDMAppConstants.AndroidConstants.OPCODE_INSTALL_APPLICATION.equals(operation.getCode()) ||
                    MDMAppConstants.AndroidConstants.OPCODE_UNINSTALL_APPLICATION.equals(operation.getCode())) {
                ApplicationManager applicationManager = DeviceMgtAPIUtils.getApplicationManager();
                applicationManager.updateSubsStatus(device.getId(), operation.getId(), operation.getStatus().toString());
            }
            return Response.status(Response.Status.OK).entity("OperationStatus updated successfully.").build();
        } catch (BadRequestException e) {
            String msg = "Error occurred due to invalid request";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred when fetching device " + deviceIdentifier.toString();
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (OperationManagementException e) {
            String msg = "Error occurred when updating operation of device " + deviceIdentifier;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException e) {
            String msg = "Error occurred when updating the application subscription status of the operation. " +
                    "The device identifier is: " + deviceIdentifier;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/filters")
    @Override
    public Response getDeviceFilters() {
        try {
            List<String> deviceTypeNames = new ArrayList<>();
            List<String> ownershipNames = new ArrayList<>();
            List<String> statusNames = new ArrayList<>();
            List<DeviceType> deviceTypes = DeviceMgtAPIUtils.getDeviceManagementService().getDeviceTypes();
            for (DeviceType deviceType : deviceTypes) {
                deviceTypeNames.add(deviceType.getName());
            }
            EnrolmentInfo.OwnerShip[] ownerShips = EnrolmentInfo.OwnerShip.values();
            for (EnrolmentInfo.OwnerShip ownerShip : ownerShips) {
                ownershipNames.add(ownerShip.name());
            }
            EnrolmentInfo.Status[] statuses = EnrolmentInfo.Status.values();
            for (EnrolmentInfo.Status status : statuses) {
                statusNames.add(status.name());
            }
            DeviceFilters deviceFilters = new DeviceFilters();
            deviceFilters.setDeviceTypes(deviceTypeNames);
            deviceFilters.setOwnerships(ownershipNames);
            deviceFilters.setStatuses(statusNames);
            return Response.status(Response.Status.OK).entity(deviceFilters).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred white retrieving device types to be used in device filters.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/{clientId}/{clientSecret}/default-token")
    @Override
    public Response getDefaultToken(
            @PathParam("clientId") String clientId,
            @PathParam("clientSecret") String clientSecret,
            @QueryParam("scopes") String scopes) {
        JWTClientManagerService jwtClientManagerService = DeviceMgtAPIUtils.getJWTClientManagerService();
        try {
            JWTClient jwtClient = jwtClientManagerService.getJWTClient();
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (!"carbon.super".equals(tenantDomain)) {
                username += "@" + tenantDomain;
            }
            String scopeString = "default";
            if (!StringUtils.isBlank(scopes)) {
                scopeString = scopes;
            }
            AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(clientId, clientSecret, username, scopeString);
            return Response.status(Response.Status.OK).entity(accessTokenInfo).build();
        } catch (JWTClientException e) {
            String msg = "Error occurred while getting default access token by using given client Id and client secret.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
