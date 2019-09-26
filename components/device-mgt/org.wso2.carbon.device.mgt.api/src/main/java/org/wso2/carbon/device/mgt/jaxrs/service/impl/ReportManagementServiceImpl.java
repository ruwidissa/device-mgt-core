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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.ReportManagementException;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceList;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.ReportManagementService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
            @QueryParam("status") String status,
            @QueryParam("ownership") String ownership,
            @QueryParam("from") String fromDate,
            @QueryParam("to") String toDate,
            @DefaultValue("0")
            @QueryParam("offset") int offset,
            @DefaultValue("5")
            @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            PaginationRequest request = new PaginationRequest(offset, limit);
            PaginationResult result;
            DeviceList devices = new DeviceList();

            if (!StringUtils.isBlank(status)) {
                request.setStatus(status);
            }
            if (!StringUtils.isBlank(ownership)) {
                request.setOwnership(ownership);
            }

            result = DeviceMgtAPIUtils.getReportManagementService()
                    .getDevicesByDuration(request, fromDate, toDate);
            if (result.getData().isEmpty()) {
                String msg = "No devices have enrolled between " + fromDate + " to " + toDate +
                             " or doesn't match with" +
                             " given parameters";
                log.error(msg);
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
}
