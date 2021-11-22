/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import com.google.gson.Gson;
import io.entgra.application.mgt.common.services.ApplicationManager;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.application.mgt.common.ApplicationInstallResponse;
import io.entgra.application.mgt.common.SubscriptionType;
import io.entgra.application.mgt.common.exception.SubscriptionManagementException;
import io.entgra.application.mgt.common.services.SubscriptionManager;
import io.entgra.application.mgt.core.util.HelperUtil;
import org.wso2.carbon.device.mgt.common.DeviceFilters;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.OperationLogFilters;
import org.wso2.carbon.device.mgt.common.MDMAppConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.device.details.DeviceData;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocationHistorySnapshotWrapper;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.UnAuthorizedException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.PolicyComplianceException;
import org.wso2.carbon.device.mgt.common.search.PropertyMap;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceDetailsMgtException;
import org.wso2.carbon.device.mgt.core.device.details.mgt.DeviceInformationManager;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ConfigOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchManagerService;
import org.wso2.carbon.device.mgt.core.search.mgt.SearchMgtException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceCompliance;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationList;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationStatusBean;
import org.wso2.carbon.device.mgt.jaxrs.beans.ComplianceDeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationRequest;
import org.wso2.carbon.device.mgt.jaxrs.beans.OperationList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ApplicationUninstallation;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.InputValidationException;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;
import org.wso2.carbon.policy.mgt.common.PolicyManagementException;
import org.wso2.carbon.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

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
            @QueryParam("status") List<String> status,
            @QueryParam("groupId") int groupId,
            @QueryParam("since") String since,
            @HeaderParam("If-Modified-Since") String ifModifiedSince,
            @QueryParam("requireDeviceInfo") boolean requireDeviceInfo,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(role)) {
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new ErrorResponse.ErrorResponseBuilder().setMessage("Request contains both name and role " +
                                "parameters. Only one is allowed " +
                                "at once.").build()).build();
            }
//            RequestValidationUtil.validateSelectionCriteria(type, user, roleName, ownership, status);
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
                for (String statusString : status){
                    if (StringUtils.isNotBlank(statusString)){
                        isStatusEmpty = false;
                        break;
                    }
                }
                if (!isStatusEmpty) {
                    RequestValidationUtil.validateStatus(status);
                    request.setStatusList(status);
                }
            }
            // this is the user who initiates the request
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
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build()).build();
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
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'since' filter").build()).build();
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
     * @param from time to start getting DeviceLocationHistorySnapshotWrapper in milliseconds
     * @param to time to end getting DeviceLocationHistorySnapshotWrapper in milliseconds
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
                                                @DefaultValue("100") @QueryParam("limit") int limit){
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
                } catch (UserStoreException e){
                    String msg = "Error occurred while retrieving role list of user '" + authorizedUser + "'";
                    log.error(msg);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
                }

            PaginationResult result = dms.getAllDevices(request, false);

            if(!result.getData().isEmpty()){
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
            String msg = "Invalid type, use either 'path' or 'full'";
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
            String msg = "Error encountered while deleting device of type : " + deviceType + " and " +
                    "ID : " + deviceId;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()
            ).build();
        }
    }

    @POST
    @Override
    @Path("/type/{deviceType}/id/{deviceId}/rename")
    public Response renameDevice(Device device, @PathParam("deviceType") String deviceType,
                                 @PathParam("deviceId") String deviceId) {
        DeviceManagementProviderService deviceManagementProviderService = DeviceMgtAPIUtils.getDeviceManagementService();
        try {
            Device persistedDevice = deviceManagementProviderService.getDevice(new DeviceIdentifier
                    (deviceId, deviceType), true);
            persistedDevice.setName(device.getName());
            boolean response = deviceManagementProviderService.modifyEnrollment(persistedDevice);
            return Response.status(Response.Status.CREATED).entity(response).build();

        } catch (DeviceManagementException e) {
            log.error("Error encountered while updating device of type : " + deviceType + " and " +
                    "ID : " + deviceId);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage("Error while updating " +
                            "device of type " + deviceType + " and ID : " + deviceId).build()).build();
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
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser)) {
                String msg = "User '" + authorizedUser + "' is not authorized to retrieve the given device id '" + id + "'";
                log.error(msg);
                return Response.status(Response.Status.UNAUTHORIZED).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_UNAUTHORIZED).setMessage(msg).build()).build();
            }

            DeviceData deviceData = new DeviceData();
            deviceData.setDeviceIdentifier(deviceIdentifier);

            if (!StringUtils.isBlank(ifModifiedSince)){
                SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                try {
                    sinceDate = format.parse(ifModifiedSince);
                    deviceData.setLastModifiedDate(sinceDate);
                } catch (ParseException e) {
                    String msg = "Invalid date string is provided in 'If-Modified-Since' header";
                    log.error(msg, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
                }
            }

            if (!StringUtils.isBlank(owner)){
                deviceData.setDeviceOwner(owner);
            }
            if (!StringUtils.isBlank(ownership)){
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
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ErrorResponse.ErrorResponseBuilder().setCode(HttpStatus.SC_NOT_FOUND).setMessage("Requested device of type '" +
                            type + "', which carries id '" + id + "' does not exist").build()).build();
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
            String msg = "Invalid type, use either 'path' or 'full'";
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
                    String message = "Error occurred while parse the since date.Invalid date string is provided in " +
                                 "'If-Modified-Since' header";
                    log.error(message, e);
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            new ErrorResponse.ErrorResponseBuilder().setMessage("Invalid date " +
                                    "string is provided in 'If-Modified-Since' header").build()).build();
                }
            }
            if (sinceDate != null) {
                device = dms.getDevice(id, sinceDate, requireDeviceInfo);
                if (device == null) {
                    String message = "No device is modified after the timestamp provided in 'If-Modified-Since' header";
                    log.error(message);
                    return Response.status(Response.Status.NOT_MODIFIED).entity("No device is modified " +
                     "after the timestamp provided in 'If-Modified-Since' header").build();
                }
            } else {
                device = dms.getDevice(id, requireDeviceInfo);
            }
            if (device == null) {
                String message = "Device does not exist with id '" + id + "'";
                log.error(message);
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder().setCode(404l).setMessage(message).build()).build();
            }
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(id, device.getType());
            // check whether the user is authorized
            if (!deviceAccessAuthorizationService.isUserAuthorized(deviceIdentifier, authorizedUser)) {
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
    @Path("/type/any/list")
    @Override
    public Response getDeviceByIdList(List<String> deviceIds) {
        DeviceManagementProviderService deviceManagementProviderService =
                DeviceMgtAPIUtils.getDeviceManagementService();
        if (deviceIds == null || deviceIds.isEmpty()) {
            String msg = "Required values of device identifiers are not set..";
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
    @Path("/{type}/{id}/features")
    @Override
    public Response getFeaturesOfDevice(
            @PathParam("type") @Size(max = 45) String type,
            @PathParam("id") @Size(max = 45) String id,
            @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        List<Feature> features = new ArrayList<>();
        DeviceManagementProviderService dms;
        try {
            RequestValidationUtil.validateDeviceIdentifier(type, id);
            dms = DeviceMgtAPIUtils.getDeviceManagementService();
            FeatureManager fm;
            try {
                fm = dms.getFeatureManager(type);
            } catch (DeviceTypeNotFoundException e) {
                return Response.status(Response.Status.NOT_FOUND).entity(
                        new ErrorResponse.ErrorResponseBuilder()
                                .setMessage("No device type found with name '" + type + "'").build()).build();
            }
            if (fm != null) {
                features = fm.getFeatures();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of features of '" + type + "' device, which " +
                    "carries the id '" + id + "'";
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
            if(map.getProperties().isEmpty()){
                if (log.isDebugEnabled()) {
                    log.debug("No search criteria defined when querying devices.");
                }
                return Response.status(Response.Status.BAD_REQUEST).entity("No search criteria defined.").build();
            }
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            devices = dms.getDevicesBasedOnProperties(map.getProperties());
            if(devices == null || devices.isEmpty()){
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
            @QueryParam("packageName") String packageName) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        Operation operation = new Operation();
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
                    ApplicationUninstallation applicationUninstallation = new ApplicationUninstallation(packageName, "PUBLIC");
                    Gson gson = new Gson();
                    operation.setCode(MDMAppConstants.AndroidConstants.UNMANAGED_APP_UNINSTALL);
                    operation.setType(Operation.Type.PROFILE);
                    operation.setPayLoad(gson.toJson(applicationUninstallation));
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
                io.entgra.application.mgt.common.exception.ApplicationManagementException
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
        RequestValidationUtil.validateOwnerParameter(owner);
        RequestValidationUtil.validatePaginationParameters(offset, limit);
        PaginationRequest request = new PaginationRequest(offset, limit);
        request.setOwner(owner);
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
            String msg = "Error occurred while fetching the operations for the '" + type + "' device, which " +
                    "carries the id '" + id + "'";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving the list of [" + type + "] features with params " +
                    "{featureType: operation, hidden: true}";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DeviceTypeNotFoundException e) {
            String msg = "No device type found with name '" + type + "'";
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
            String msg = "Error occurred while changing device status of type : " + type + " and " +
                    "device id : " + id;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
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
            String errorMessage = "Issue in retrieving deivce management service instance";
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
    @Path("/{deviceType}/applications")
    public Response getApplications(
            @PathParam("deviceType") String deviceType,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("10")
            @QueryParam("limit") int limit) {
        PaginationRequest request = new PaginationRequest(offset, limit);
        ApplicationList applicationList = new ApplicationList();
        request.setDeviceType(deviceType);
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
                String msg = "Error, device of type: " + deviceType + " does not exist";
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
                applicationManager.updateSubsStatus(device.getId(), operation.getId(),operation.getStatus().toString());
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
        } catch (io.entgra.application.mgt.common.exception.ApplicationManagementException e) {
            String msg = "Error occurred when updating the application subscription status of the operation. " +
                    "The device identifier is: " + deviceIdentifier;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();        }
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
            @PathParam("clientSecret") String clientSecret) {
        JWTClientManagerService jwtClientManagerService = DeviceMgtAPIUtils.getJWTClientManagerService();
        try {
            JWTClient jwtClient = jwtClientManagerService.getJWTClient();
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (!"carbon.super".equals(tenantDomain)) {
                username += "@" + tenantDomain;
            }
            AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(clientId, clientSecret, username, "default");
            return Response.status(Response.Status.OK).entity(accessTokenInfo).build();
        } catch (JWTClientException e) {
            String msg = "Error occurred while getting default access token by using given client Id and client secret.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
