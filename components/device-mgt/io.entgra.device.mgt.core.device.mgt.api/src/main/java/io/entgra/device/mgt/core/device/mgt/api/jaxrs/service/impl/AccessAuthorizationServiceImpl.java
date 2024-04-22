/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.AccessAuthorizationService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.authorization.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class AccessAuthorizationServiceImpl implements AccessAuthorizationService {

    private static final Log log = LogFactory.getLog(AccessAuthorizationServiceImpl.class);
    @Override
    public Response checkDeviceAccess(DeviceAuthorizationRequest deviceAuthorizationRequest) {

        if (StringUtils.isEmpty(deviceAuthorizationRequest.getType())) {
            String msg = "device type not specified";
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (deviceAuthorizationRequest.getDeviceIds().isEmpty()) {
            String msg = "device ids not specified";
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (deviceAuthorizationRequest.getPermissions().isEmpty()) {
            String msg = "permissions not specified";
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for(String id : deviceAuthorizationRequest.getDeviceIds()) {
            DeviceIdentifier identifier = new DeviceIdentifier(id, deviceAuthorizationRequest.getType());
            deviceIdentifiers.add(identifier);
        }
        try {
            DeviceAuthorizationResult result = DeviceMgtAPIUtils.getDeviceAccessAuthorizationService()
                    .isUserAuthorized(deviceIdentifiers, deviceAuthorizationRequest.getUsername(),
                            deviceAuthorizationRequest.getPermissions().toArray(new String[0]));
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (DeviceAccessAuthorizationException e) {
            String msg = "Error occurred while checking access info";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response checkGroupAccess(GroupAuthorizationRequest request) {

        if (request.getGroupIds().isEmpty()) {
            String msg = "group ids not specified";
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        if (request.getPermissions().isEmpty()) {
            String msg = "permissions not specified";
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            GroupAuthorizationResult result = DeviceMgtAPIUtils.getGroupAccessAuthorizationService()
                    .isUserAuthorized(request.getGroupIds(), request.getUsername(),
                            request.getPermissions().toArray(new String[0]));
            return Response.status(Response.Status.OK).entity(result).build();
        } catch (GroupAccessAuthorizationException e) {
            String msg = "Error occurred while checking access info";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
