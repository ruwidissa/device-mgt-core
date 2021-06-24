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
 *
 *   Copyright (c) 2021, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.jaxrs.beans.DeviceGroupList;
import org.wso2.carbon.device.mgt.jaxrs.service.api.admin.GroupManagementAdminService;
import org.wso2.carbon.device.mgt.jaxrs.service.impl.util.RequestValidationUtil;
import org.wso2.carbon.device.mgt.jaxrs.util.DeviceMgtAPIUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

public class GroupManagementAdminServiceImpl implements GroupManagementAdminService {

    private static final Log log = LogFactory.getLog(GroupManagementAdminServiceImpl.class);

    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = {"/permission/device-mgt/admin/groups",
            "/permission/device-mgt/user/groups"};

    @Override
    public Response getGroups(String name, String owner, int offset, int limit, String status, boolean requireGroupProps) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            GroupPaginationRequest request = new GroupPaginationRequest(offset, limit);
            if (name != null){
                request.setGroupName(name.toUpperCase());
            }
            if (owner != null) {
                request.setOwner(owner.toUpperCase());
            }
            if (status != null && !status.isEmpty()) {
                request.setStatus(status.toUpperCase());
            }
            PaginationResult deviceGroupsResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroups(request, requireGroupProps);
            DeviceGroupList deviceGroupList = new DeviceGroupList();
            if (deviceGroupsResult.getData() != null) {
                deviceGroupList.setList(deviceGroupsResult.getData());
                deviceGroupList.setCount(deviceGroupsResult.getRecordsTotal());
            } else {
                deviceGroupList.setList(new ArrayList<>());
                deviceGroupList.setCount(0);
            }
            return Response.status(Response.Status.OK).entity(deviceGroupList).build();
        } catch (GroupManagementException e) {
            String msg = "ErrorResponse occurred while retrieving all groups.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/hierarchy")
    @Override
    public Response getGroupsWithHierarchy(
            @QueryParam("name") String name,
            @QueryParam("owner") String owner,
            @QueryParam("status") String status,
            @QueryParam("requireGroupProps") boolean requireGroupProps,
            @DefaultValue("3") @QueryParam("depth") int depth,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("5") @QueryParam("limit") int limit) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            GroupPaginationRequest request = new GroupPaginationRequest(offset, limit);
            request.setGroupName(name);
            request.setOwner(owner);
            request.setStatus(status);
            request.setDepth(depth);
            PaginationResult deviceGroupsResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroupsWithHierarchy(null, request, requireGroupProps);
            DeviceGroupList deviceGroupList = new DeviceGroupList();
            deviceGroupList.setList(deviceGroupsResult.getData());
            deviceGroupList.setCount(deviceGroupsResult.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(deviceGroupList).build();
        } catch (GroupManagementException e) {
            String error = "Error occurred while retrieving groups with hierarchy.";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getGroupCount(String status) {
        try {
            int count;
            if (status == null || status.isEmpty()) {
                count = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroupCount();
            } else {
                count = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroupCountByStatus(status);
            }
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            String msg = "ErrorResponse occurred while retrieving group count.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response createGroup(DeviceGroup group) {
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        group.setStatus(DeviceGroupConstants.GroupStatus.ACTIVE);
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().createGroup(group, DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
            return Response.status(Response.Status.CREATED).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while adding new group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (GroupAlreadyExistException e) {
            String msg = "Group already exists with name " + group.getName() + ".";
            log.warn(msg);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        }
    }
}
