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
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupConstants;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupRoleWrapper;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceTypesOfGroups;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupAlreadyExistException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupManagementException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.GroupNotExistException;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.RoleDoesNotExistException;
import io.entgra.device.mgt.core.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.device.mgt.core.notification.logger.GroupMgtLogContext;
import io.entgra.device.mgt.core.notification.logger.impl.EntgraGroupMgtLoggerImpl;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.GroupPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceNotFoundException;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.GroupManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceGroupList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.DeviceToGroupsAssignment;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.RoleList;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.GroupManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyAdministratorPoint;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
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
import java.util.List;

public class GroupManagementServiceImpl implements GroupManagementService {

    GroupMgtLogContext.Builder groupMgtContextBuilder = new GroupMgtLogContext.Builder();
    private static final EntgraLogger log = new EntgraGroupMgtLoggerImpl(GroupManagementServiceImpl.class);

    private static final String DEFAULT_ADMIN_ROLE = "admin";
    private static final String[] DEFAULT_ADMIN_PERMISSIONS = {"/permission/device-mgt/admin/groups",
                                                               "/permission/device-mgt/user/groups"};

    @Override
    public Response getGroups(String name, String owner, int offset, int limit, boolean requireGroupProps) {
        try {
            RequestValidationUtil.validatePaginationParameters(offset, limit);
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            GroupPaginationRequest request = new GroupPaginationRequest(offset, limit);
            request.setGroupName(name);
            request.setOwner(owner);
            PaginationResult deviceGroupsResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroups(currentUser, request, requireGroupProps);
            DeviceGroupList deviceGroupList = new DeviceGroupList();
            if (deviceGroupsResult.getData() != null && deviceGroupsResult.getRecordsTotal() > 0) {
                deviceGroupList.setList(deviceGroupsResult.getData());
                deviceGroupList.setCount(deviceGroupsResult.getRecordsTotal());
            } else {
                deviceGroupList.setList(new ArrayList<>());
                deviceGroupList.setCount(0);
            }
            return Response.status(Response.Status.OK).entity(deviceGroupList).build();
        } catch (GroupManagementException e) {
            String error = "Error occurred while getting the groups.";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @GET
    @Path("/hierarchy")
    @Override
    public Response getGroupsWithHierarchy(
            @QueryParam("name") String name,
            @QueryParam("owner") String owner,
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
            request.setDepth(depth);
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserRealm realmService = DeviceMgtAPIUtils.getRealmService().getTenantUserRealm(tenantId);
            String[] roles = realmService.getUserStoreManager().getRoleListOfUser(currentUser);
            boolean hasAdminRole = Arrays.asList(roles).contains(DEFAULT_ADMIN_ROLE);
            PaginationResult deviceGroupsResult;
            if (hasAdminRole) {
                deviceGroupsResult = DeviceMgtAPIUtils.getGroupManagementProviderService()
                        .getGroupsWithHierarchy(null, request, requireGroupProps);
            } else{
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
    public Response getHierarchicalGroupCount() {
        try {
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getHierarchicalGroupCount(currentUser, null);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while retrieving hierarchical group count.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getGroupCount() {
        try {
            String currentUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroupCount(currentUser, null);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while retrieving group count.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response createGroup(DeviceGroup group) {
        String tenantId = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        String tenantDomain = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (group == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        group.setOwner(owner);
        group.setStatus(DeviceGroupConstants.GroupStatus.ACTIVE);
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().createGroup(group, DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
            log.info(
                    "Group " + group.getName() + " created",
                    groupMgtContextBuilder
                            .setActionTag("ADD_GROUP")
                            .setGroupId(String.valueOf(group.getGroupId()))
                            .setName(group.getName())
                            .setOwner(group.getOwner())
                            .setTenantID(tenantId)
                            .setTenantDomain(tenantDomain)
                            .setUserName(owner)
                            .build()
            );
            return Response.status(Response.Status.CREATED).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while adding new group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (GroupAlreadyExistException e) {
            String msg = "Group already exists with name : " + group.getName() + ".";
            log.warn(msg);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        }
    }

    @Override
    public Response getGroup(int groupId, boolean requireGroupProps, int depth, boolean allowed) {
        try {
            GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
            DeviceGroup deviceGroup = allowed ? service.getUserOwnGroup(groupId, requireGroupProps, depth):
                service.getGroup(groupId, requireGroupProps, depth);
            if (deviceGroup != null) {
                return Response.status(Response.Status.OK).entity(deviceGroup).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Group not found.").build();
            }
        } catch (GroupManagementException e) {
            String error = "Error occurred while getting the group.";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response getGroup(String groupName, boolean requireGroupProps, int depth) {
        try {
            GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
            DeviceGroup deviceGroup = service.getGroup(groupName, requireGroupProps);
            if (deviceGroup != null) {
                return Response.status(Response.Status.OK).entity(deviceGroup).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Group not found.").build();
            }
        } catch (GroupManagementException e) {
            String error = "Error occurred while getting the group.";
            log.error(error, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

    @Override
    public Response updateGroup(int groupId, DeviceGroup deviceGroup) {
        String tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());
        String tenantDomain = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (deviceGroup == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().updateGroup(deviceGroup, groupId);
            int deviceCount = DeviceMgtAPIUtils.getGroupManagementProviderService().getDeviceCount(groupId);
            List<Device> devices = DeviceMgtAPIUtils.getGroupManagementProviderService().getAllDevicesOfGroup(deviceGroup.getName(), false);
            List<String> deviceIdentifiers = new ArrayList<>();
            for(Device device : devices) {
                deviceIdentifiers.add(device.getDeviceIdentifier());
            }
            String stringDeviceIdentifiers = new Gson().toJson(deviceIdentifiers);
            log.info(
                    "Group " + deviceGroup.getName() + " updated",
                    groupMgtContextBuilder
                            .setActionTag("UPDATE_GROUP")
                            .setGroupId(String.valueOf(deviceGroup.getGroupId()))
                            .setName(deviceGroup.getName())
                            .setOwner(deviceGroup.getOwner())
                            .setDeviceCount(String.valueOf(deviceCount))
                            .setDeviceIdentifiers(stringDeviceIdentifiers)
                            .setTenantID(tenantId)
                            .setTenantDomain(tenantDomain)
                            .setUserName(username)
                            .build()
            );
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while updating group. ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (GroupNotExistException e) {
            String msg = "Group does not exist.";
            log.warn(msg);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        } catch (GroupAlreadyExistException e) {
            String msg = "Group already exists with name : '" + deviceGroup.getName() + "'.";
            log.warn(msg);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        }
    }

    @Override
    public Response deleteGroup(int groupId, boolean isDeleteChildren) {
        try {
            String tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            String tenantDomain = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (DeviceMgtAPIUtils.getGroupManagementProviderService().deleteGroup(groupId, isDeleteChildren)) {
                int deviceCount = DeviceMgtAPIUtils.getGroupManagementProviderService().getDeviceCount(groupId);
                log.info(
                        "Group with group id " + groupId + " deleted",
                        groupMgtContextBuilder
                                .setActionTag("DELETE_GROUP")
                                .setGroupId(String.valueOf(groupId))
                                .setDeviceCount(String.valueOf(deviceCount))
                                .setTenantID(tenantId)
                                .setTenantDomain(tenantDomain)
                                .setUserName(username)
                                .build()
                );
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Group not found.").build();
            }
        } catch (GroupManagementException e) {
            String msg = "Error occurred while deleting the group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response manageGroupSharing(int groupId, List<String> userRoles) {
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .manageGroupSharing(groupId, userRoles);
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while managing group share. ";
            if (e.getErrorMessage() != null){
                msg += e.getErrorMessage();
            }
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RoleDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @Override
    public Response getRolesOfGroup(int groupId) {
        try {
            List<String> groupRoles = DeviceMgtAPIUtils.getGroupManagementProviderService().getRoles(groupId);
            RoleList deviceGroupRolesList = new RoleList();
            if(groupRoles != null) {
                deviceGroupRolesList.setList(groupRoles);
                deviceGroupRolesList.setCount(groupRoles.size());
            } else {
                deviceGroupRolesList.setList(new ArrayList<>());
                deviceGroupRolesList.setCount(0);
            }
            return Response.status(Response.Status.OK).entity(deviceGroupRolesList).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting roles of the group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getDevicesOfGroup(int groupId, int offset, int limit, boolean requireDeviceProps) {
        try {
            GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
            List<Device> deviceList = service.getDevices(groupId, offset, limit, requireDeviceProps);
            int deviceCount = service.getDeviceCount(groupId);
            DeviceList deviceListWrapper = new DeviceList();
            if (deviceList != null) {
                deviceListWrapper.setList(deviceList);
            } else {
                deviceListWrapper.setList(new ArrayList<>());
            }
            deviceListWrapper.setCount(deviceCount);
            return Response.status(Response.Status.OK).entity(deviceListWrapper).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting devices the group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response getDeviceCountOfGroup(int groupId) {
        try {
            int count = DeviceMgtAPIUtils.getGroupManagementProviderService().getDeviceCount(groupId);
            return Response.status(Response.Status.OK).entity(count).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting device count of the group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response addDevicesToGroup(int groupId, List<DeviceIdentifier> deviceIdentifiers) {
        try {
            String tenantId = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId());
            String tenantDomain = String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
            String username = CarbonContext.getThreadLocalCarbonContext().getUsername();
            DeviceMgtAPIUtils.getGroupManagementProviderService().addDevices(groupId, deviceIdentifiers);
            PolicyAdministratorPoint pap = DeviceMgtAPIUtils.getPolicyManagementService().getPAP();
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            for(DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                Device device = dms.getDevice(deviceIdentifier, false);
                if(!device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.REMOVED)) {
                    pap.removePolicyUsed(deviceIdentifier);
                    DeviceMgtAPIUtils.getPolicyManagementService().getEffectivePolicy(deviceIdentifier);
                }
            }
            pap.publishChanges();
            int deviceCount = DeviceMgtAPIUtils.getGroupManagementProviderService().getDeviceCount(groupId);
            List<String> deviceIdentifiersList = new ArrayList<>();
            for(DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                deviceIdentifiersList.add(deviceIdentifier.getId());
            }
            String stringDeviceIdentifiers = new Gson().toJson(deviceIdentifiersList);
            log.info(
                    "Devices added for group id " + groupId,
                    groupMgtContextBuilder
                            .setActionTag("ADD_DEVICES")
                            .setGroupId(String.valueOf(groupId))
                            .setDeviceCount(String.valueOf(deviceCount))
                            .setDeviceIdentifiers(stringDeviceIdentifiers)
                            .setTenantID(tenantId)
                            .setTenantDomain(tenantDomain)
                            .setUserName(username)
                            .build()
            );
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while adding devices to group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (PolicyManagementException e) {
            log.error("Error occurred while adding policies against device(s).", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        } catch (PolicyEvaluationException e) {
//            log.error("Error occurred while retrieving policies against device(s).", e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DeviceManagementException e) {
            log.error("Error occurred while retrieving device information.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public Response removeDevicesFromGroup(int groupId, List<DeviceIdentifier> deviceIdentifiers) {
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().removeDevice(groupId, deviceIdentifiers);
            PolicyAdministratorPoint pap = DeviceMgtAPIUtils.getPolicyManagementService().getPAP();
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            for(DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                Device device = dms.getDevice(deviceIdentifier, false);
                if(!device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.REMOVED)) {
                    pap.removePolicyUsed(deviceIdentifier);
                    DeviceMgtAPIUtils.getPolicyManagementService().getEffectivePolicy(deviceIdentifier);
                }
            }
            pap.publishChanges();
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while removing devices from group.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }catch (PolicyManagementException e) {
            log.error("Error occurred while adding policies against device(s).", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }catch (DeviceManagementException e) {
            log.error("Error occurred while retrieving device information.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public Response updateDeviceAssigningToGroups(DeviceToGroupsAssignment deviceToGroupsAssignment) {
        try {
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(deviceToGroupsAssignment.getDeviceIdentifier());
            GroupManagementProviderService service = DeviceMgtAPIUtils.getGroupManagementProviderService();
            List<DeviceGroup> deviceGroups = service.getGroups(deviceToGroupsAssignment.getDeviceIdentifier(), false);
            PolicyAdministratorPoint pap = DeviceMgtAPIUtils.getPolicyManagementService().getPAP();
            DeviceManagementProviderService dms = DeviceMgtAPIUtils.getDeviceManagementService();
            for (DeviceGroup group : deviceGroups) {
                Integer groupId = group.getGroupId();
                if (deviceToGroupsAssignment.getDeviceGroupIds().contains(groupId)) {
                    deviceToGroupsAssignment.getDeviceGroupIds().remove(groupId);
                } else if (!CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(group.getOwner())) {
                    DeviceMgtAPIUtils.getGroupManagementProviderService().removeDevice(groupId, deviceIdentifiers);
                }
            }
            for (int groupId : deviceToGroupsAssignment.getDeviceGroupIds()) {
                DeviceMgtAPIUtils.getGroupManagementProviderService().addDevices(groupId, deviceIdentifiers);
                for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                    pap.removePolicyUsed(deviceIdentifier);
                    DeviceMgtAPIUtils.getPolicyManagementService().getEffectivePolicy(deviceIdentifier);
                }
            }
            pap.publishChanges();
            return Response.status(Response.Status.OK).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while assigning device to groups.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (DeviceNotFoundException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (PolicyManagementException e) {
            log.error("Failed to add policies for device assigned to group.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public Response getGroups(String deviceId, String deviceType, boolean requireGroupProps) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(deviceId, deviceType);
            List<DeviceGroup> deviceGroups = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getGroups(deviceIdentifier, requireGroupProps);
            return Response.status(Response.Status.OK).entity(deviceGroups).build();
        } catch (GroupManagementException e) {
            String msg = "Error occurred while getting groups of device.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Path("/device-types")
    @Override
    public Response getGroupHasDeviceTypes(List<String> identifiers) {
        try {
            DeviceTypesOfGroups deviceTypesOfGroups = DeviceMgtAPIUtils.getGroupManagementProviderService()
                    .getDeviceTypesOfGroups(identifiers);

            return Response.status(Response.Status.OK).entity(deviceTypesOfGroups).build();
        } catch (GroupManagementException e) {
            String msg = "Only numbers can exists in a group ID or Invalid Group ID provided.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Path("/roles/share")
    @Override
    public Response createGroupWithRoles(DeviceGroupRoleWrapper groups) {
        String tenantId = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        String tenantDomain = String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (groups == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        groups.setOwner(PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername());
        groups.setStatus(DeviceGroupConstants.GroupStatus.ACTIVE);
        try {
            DeviceMgtAPIUtils.getGroupManagementProviderService().createGroupWithRoles(groups, DEFAULT_ADMIN_ROLE, DEFAULT_ADMIN_PERMISSIONS);
            DeviceGroup group = DeviceMgtAPIUtils.getGroupManagementProviderService().getGroup(groups.getName(),
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername().isEmpty());
            if (group != null) {
                DeviceMgtAPIUtils.getGroupManagementProviderService().manageGroupSharing(group.getGroupId(), groups.getUserRoles());
                String stringRoles = "";
                if(groups.getUserRoles() != null){
                    stringRoles = new Gson().toJson(groups.getUserRoles());
                }
                log.info(
                        "Group " + group.getName() + " created",
                        groupMgtContextBuilder
                                .setActionTag("ADD_GROUP")
                                .setGroupId(String.valueOf(group.getGroupId()))
                                .setName(group.getName())
                                .setRoles(stringRoles)
                                .setOwner(group.getOwner())
                                .setTenantID(tenantId)
                                .setTenantDomain(tenantDomain)
                                .setUserName(username)
                                .build()
                );
                return Response.status(Response.Status.CREATED).entity(group.getGroupId()).build();
            } else {
                String msg = "Error occurred while retrieving newly created group.";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (GroupManagementException e) {
            String msg = "Error occurred while adding " + groups.getName() + " group";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (GroupAlreadyExistException e) {
            String msg = "Group already exists with name : " + groups.getName() + " Try with another group name.";
            log.error(msg, e);
            return Response.status(Response.Status.CONFLICT).entity(msg).build();
        } catch (RoleDoesNotExistException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}