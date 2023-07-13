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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.admin;

import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupConstants;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupRoleWrapper;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupAlreadyExistException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.GroupPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceGroupList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin.GroupManagementAdminService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;

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
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            GroupPaginationRequest request = new GroupPaginationRequest(offset, limit);
            request.setGroupName(name);
            request.setOwner(owner);
            request.setStatus(status);
            request.setDepth(depth);
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserRealm realmService = DeviceMgtAPIUtils.getRealmService().getTenantUserRealm(tenantId);
            String[] roles = realmService.getUserStoreManager().getRoleListOfUser(currentUser);
            boolean isAdmin = DEFAULT_ADMIN_ROLE.equals(currentUser);
            boolean hasAdminRole = Arrays.asList(roles).contains(DEFAULT_ADMIN_ROLE);
            PaginationResult deviceGroupsResult;
            if (StringUtils.isBlank(currentUser) || isAdmin || hasAdminRole) {
                deviceGroupsResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                        .getGroupsWithHierarchy(null, request, requireGroupProps);
            } else {
                deviceGroupsResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                        .getGroupsWithHierarchy(currentUser, request, requireGroupProps);
            }
            DeviceGroupList deviceGroupList = new DeviceGroupList();
            deviceGroupList.setList(deviceGroupsResult.getData());
            deviceGroupList.setCount(deviceGroupsResult.getRecordsTotal());
            return Response.status(Response.Status.OK).entity(deviceGroupList).build();
        } catch (GroupManagementException e) {
            String error = "Error occurred while retrieving groups with hierarchy.";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        } catch (UserStoreException e) {
            String msg = "Error occurred while getting user realm.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
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
            String msg = "Group already exists with name : " + group.getName() + ". Try with another group name.";
            log.warn(msg);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        }
    }
    @POST
    @Path("/roles/share")
    @Override
    public Response createGroupWithRoles(DeviceGroupRoleWrapper group) {
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        group.setOwner(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
        group.setStatus(DeviceGroupConstants.GroupStatus.ACTIVE);
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().createGroupWithRoles(group, DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
            DeviceMgtAPIUtils.getGroupManagementProviderService().manageGroupSharing(group.getGroupId(), group.getUserRoles());
            return Response.status(Response.Status.CREATED).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while adding " + group.getName() + " group";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (GroupAlreadyExistException e) {
            String msg = "Group already exists with name : " + group.getName() + " Try with another group name.";
            log.error(msg, e);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        } catch (RoleDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

}

