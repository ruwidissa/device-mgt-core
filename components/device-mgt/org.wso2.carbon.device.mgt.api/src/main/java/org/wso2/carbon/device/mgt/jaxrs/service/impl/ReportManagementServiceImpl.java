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

import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceTypeNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.common.ReportFiltersList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.ReportManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * This is the service class for report generating operations
 */
@Path("/reports")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReportManagementServiceImpl implements ReportManagementService {

    private static final Log log = LogFactory.getLog(ReportManagementServiceImpl.class);

    @GET
    @Path("/devices")
    @Override
    public Response getDevicesByDuration(
            @QueryParam("status") List<String> status,
            @QueryParam("ownership") String ownership,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("10")
            @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            PaginationRequest request = new PaginationRequest(offset, limit);
            PaginationResult result;
            DeviceList devices = new DeviceList();

            if (!StringUtils.isBlank(ownership)) {
                request.setOwnership(ownership);
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

            result = DeviceMgtAPIUtils.getReportManagementService()
                    .getDevicesByDuration(request, fromDate, toDate);
            if (result.getData().isEmpty()) {
                String msg = "No devices have enrolled between " + fromDate + " to " + toDate +
                             " or doesn't match with" +
                             " given parameters";
                return Response.status(Response.Status.OK).entity(msg).build();
            } else {
                devices.setList((List<Device>) result.getData());
                devices.setCount(result.getRecordsTotal());
                return Response.status(Response.Status.OK).entity(devices).build();
            }
        } catch (ReportManagementException e) {
            String msg = "Error occurred while retrieving device list";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/devices/count")
    @Override
    public Response getDevicesByDurationCount(
            @QueryParam("status") List<String> status,
            @QueryParam("ownership") String ownership,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate) {
        int deviceCount;
        try {
            deviceCount = DeviceMgtAPIUtils.getReportManagementService()
                    .getDevicesByDurationCount(status, ownership, fromDate, toDate);
            return Response.status(Response.Status.OK).entity(deviceCount).build();
        } catch (ReportManagementException e) {
            String errorMessage = "Error while retrieving device count.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
    }

    @GET
    @Path("/count")
    @Override
    public Response getCountOfDevicesByDuration(
            @QueryParam("status") List<String> status,
            @QueryParam("ownership") String ownership,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            PaginationRequest request = new PaginationRequest(offset, limit);

            if (!StringUtils.isBlank(ownership)) {
                request.setOwnership(ownership);
            }

            JsonObject countList = DeviceMgtAPIUtils.getReportManagementService()
                    .getCountOfDevicesByDuration(request, status, fromDate, toDate);
            if (countList.isJsonNull()) {
                return Response.status(Response.Status.OK)
                        .entity("No devices have been enrolled between the given date range").build();
            } else {
                return Response.status(Response.Status.OK).entity(countList).build();
            }
        } catch (ReportManagementException e) {
            String msg = "Error occurred while retrieving device list";
            log.error(msg, e);
            return Response.serverError().entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(msg).build()).build();
        }
    }

    @GET
    @Path("/expired-devices/{deviceType}")
    @Override
    public Response getExpiredDevicesByOSVersion(@PathParam("deviceType") String deviceType,
                                                 @QueryParam("osVersion") String osVersion,
                                                 @DefaultValue("0")
                                                 @QueryParam("offset") int offset,
                                                 @DefaultValue("5")
                                                 @QueryParam("limit") int limit) {
        try {
            PaginationRequest request = new PaginationRequest(offset, limit);
            request.setDeviceType(deviceType);
            request.setProperty(Constants.OS_VERSION, osVersion);

            PaginationResult paginationResult = DeviceMgtAPIUtils
                    .getReportManagementService()
                    .getDevicesExpiredByOSVersion(request);

            DeviceList devices = new DeviceList();
            devices.setList((List<Device>) paginationResult.getData());
            devices.setCount(paginationResult.getRecordsTotal());

            return Response.status(Response.Status.OK).entity(devices).build();
        } catch (BadRequestException e) {
            String msg = "Error occurred while validating device type or the OS version.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ReportManagementException e) {
            String msg = "Error occurred while retrieving list of devices with out-dated OS versions.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/devices/{device-type}/{package-name}/not-installed")
    @Override
    public Response getAppNotInstalledDevices(
            @PathParam("device-type") String deviceType,
            @PathParam("package-name") String packageName,
            @QueryParam("app-version") String version,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("10")
            @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            PaginationRequest request = new PaginationRequest(offset, limit);
            DeviceList devices = new DeviceList();
            request.setDeviceType(deviceType);

            PaginationResult result = DeviceMgtAPIUtils.getReportManagementService()
                    .getAppNotInstalledDevices(request, packageName, version);
            if (result.getData().isEmpty()) {
                return Response.status(Response.Status.OK)
                        .entity("App with package name " + packageName +
                                " is installed in all enrolled devices").build();
            } else {
                devices.setList((List<Device>) result.getData());
                devices.setCount(result.getRecordsTotal());
                return Response.status(Response.Status.OK).entity(devices).build();
            }
        } catch (DeviceTypeNotFoundException e) {
            String msg = "Error occurred while retrieving devices list. Device type: " + deviceType +
                    "is not valid";
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ReportManagementException e) {
            String msg = "Error occurred while retrieving device list";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/encryption-status")
    @Override
    public Response getDevicesByEncryptionStatus(@QueryParam("isEncrypted") boolean isEncrypted,
                                                 @DefaultValue("0")
                                                 @QueryParam("offset") int offset,
                                                 @DefaultValue("5")
                                                 @QueryParam("limit") int limit) {
        try {
            PaginationRequest request = new PaginationRequest(offset, limit);

            PaginationResult paginationResult = DeviceMgtAPIUtils
                    .getReportManagementService()
                    .getDevicesByEncryptionStatus(request, isEncrypted);

            DeviceList deviceList = new DeviceList();
            deviceList.setList((List<Device>) paginationResult.getData());
            deviceList.setCount(paginationResult.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(deviceList).build();
        } catch (ReportManagementException e) {
            String msg = "Error occurred while retrieving devices list with provided encryption status";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/{device-type}/ungrouped-devices")
    public Response getUngroupedDevices(
            @PathParam("device-type") String deviceType,
            @QueryParam("groupNames") List<String> groupNames,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("10")
            @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            PaginationRequest request = new PaginationRequest(offset, limit);
            DeviceList deviceList = new DeviceList();
            request.setDeviceType(deviceType);
            PaginationResult paginationResult =
                    DeviceMgtAPIUtils.getReportManagementService().getDeviceNotAssignedToGroups(request, groupNames);

            if (paginationResult.getData().isEmpty()) {
                String msg = "There is no " + deviceType + "device without groups";
                return Response.status(Response.Status.NO_CONTENT).entity(msg).build();
            } else {
                deviceList.setList((List<Device>) paginationResult.getData());
                return Response.status(Response.Status.OK).entity(deviceList).build();
            }
        } catch (ReportManagementException e) {
            String msg = "Error occurred while retrieving device list that are not assigned to " +
                         "groups";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceTypeNotFoundException e) {
            String msg = "Error occurred while retrieving devices list. Device type: " + deviceType +
                         "is not valid";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        }
    }

    @GET
    @Path("/filters")
    @Override
    public Response getReportFilters() {
        try {
            List<String> operators = DeviceMgtAPIUtils.getReportManagementService().getDeviceOperators();
            List<String> agentVersions = DeviceMgtAPIUtils.getReportManagementService().getAgentVersions();
            if(operators.isEmpty() && agentVersions.isEmpty()) {
                String msg = "There are no report filters found";
                return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
            }
            else {
                ReportFiltersList reportFiltersList = new ReportFiltersList();
                reportFiltersList.setDeviceOperators(operators);
                reportFiltersList.setAgentVersions(agentVersions);
                return Response.status(Response.Status.OK).entity(reportFiltersList).build();
            }
        } catch (ReportManagementException e) {
            String msg = "Error occurred while retrieving device operators.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
