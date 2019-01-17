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
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceCount;
import org.wso2.carbon.device.mgt.common.DeviceIDList;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.service.api.DeviceStatusManagementService;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/device-status")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceStatusManagementServiceImpl implements DeviceStatusManagementService {

    private static final Log log = LogFactory.getLog(DeviceStatusManagementServiceImpl.class);

    @GET
    @Override
    @Path("/count/{type}/{status}")
    public Response getDeviceCountByStatus(@PathParam("type") String type, @PathParam("status") String status,
                                           @HeaderParam("If-Modified-Since") String ifModifiedSince) {
        DeviceCount deviceCount = new DeviceCount();
        try {
            deviceCount.setCount(DeviceMgtAPIUtils.getDeviceManagementService().getDeviceCountByStatus(type, status));
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while retrieving device count.";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceCount).build();
    }

    @GET
    @Override
    @Path("/ids/{type}/{status}")
    public Response getDeviceIdentifiersByStatus(@PathParam("type") String type, @PathParam("status") String status, String ifModifiedSince) {
        DeviceIDList deviceList = new DeviceIDList();
        try {
            deviceList.setIds(DeviceMgtAPIUtils.getDeviceManagementService().getDeviceIdentifiersByStatus(status, type));
        } catch (DeviceManagementException e) {
            String errorMessage = "Error while obtaining list of devices";
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    new ErrorResponse.ErrorResponseBuilder().setMessage(errorMessage).build()).build();
        }
        return Response.status(Response.Status.OK).entity(deviceList).build();
    }

    @PUT
    @Override
    @Path("/update/{type}/{status}")
    public Response bulkUpdateDeviceStatus(@PathParam("type") String type, @PathParam("status") String status, List<String> deviceList) {
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
}
