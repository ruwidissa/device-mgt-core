/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2021, Entgra (pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.TrackerGroupInfo;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupNotExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GroupDAO;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.TrackerDAO;
import org.wso2.carbon.device.mgt.core.event.config.GroupAssignmentEventOperationExecutor;
import org.wso2.carbon.device.mgt.core.geo.task.GeoFenceEventOperationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.device.mgt.core.util.HttpReportingUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class GroupManagementProviderServiceImpl implements GroupManagementProviderService {

    private static final Log log = LogFactory.getLog(GroupManagementProviderServiceImpl.class);

    private final GroupDAO groupDAO;
    private final DeviceDAO deviceDAO;
    private final TrackerDAO trackerDAO;

    /**
     * Set groupDAO from GroupManagementDAOFactory when class instantiate.
     */
    public GroupManagementProviderServiceImpl() {
        this.groupDAO = GroupManagementDAOFactory.getGroupDAO();
        this.deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        this.trackerDAO = DeviceManagementDAOFactory.getTrackerDAO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createGroup(DeviceGroup deviceGroup, String defaultRole, String[] defaultPermissions)
            throws GroupManagementException, GroupAlreadyExistException {
        if (deviceGroup == null) {
            String msg = "Received incomplete data for createGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating group '" + deviceGroup.getName() + "'");
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            GroupManagementDAOFactory.beginTransaction();
            DeviceGroup existingGroup = this.groupDAO.getGroup(deviceGroup.getName(), tenantId);
            if (existingGroup == null) {
                if (deviceGroup.getParentGroupId() == 0) {
                    deviceGroup.setParentPath(DeviceGroupConstants.HierarchicalGroup.SEPERATOR);
                } else {
                    DeviceGroup immediateParentGroup = groupDAO.getGroup(deviceGroup.getParentGroupId(), tenantId);
                    if (immediateParentGroup == null) {
                        String msg = "Parent group with group ID '" + deviceGroup.getParentGroupId()
                                + "' does not exist.  Hence creating of group '" + deviceGroup.getName()
                                + "' was not success";
                        log.error(msg);
                        throw new GroupManagementException(msg);
                    }
                    String parentPath = DeviceManagerUtil.createParentPath(immediateParentGroup);
                    deviceGroup.setParentPath(parentPath);
                }
                int updatedGroupID = this.groupDAO.addGroup(deviceGroup, tenantId);
                if (deviceGroup.getGroupProperties() != null && deviceGroup.getGroupProperties().size() > 0) {
                    this.groupDAO.addGroupProperties(deviceGroup, updatedGroupID, tenantId);
                }
                GroupManagementDAOFactory.commitTransaction();

                //add new group in traccar
                try {
                    if (HttpReportingUtil.isTrackerEnabled()) {
                        DeviceManagementDataHolder.getInstance().getDeviceAPIClientService()
                                .addGroup(deviceGroup, updatedGroupID, tenantId);
                    }
                } catch (TraccarConfigurationException e) {
                    log.error("Error while adding a group to Traccar " + e);
                }
                //add new group in traccar
            } else {
                // add a group if not exist in traccar starts
                existingGroup = this.groupDAO.getGroup(deviceGroup.getName(), tenantId);
                int groupId = existingGroup.getGroupId();
                TrackerGroupInfo res = trackerDAO.getTraccarGroup(groupId, tenantId);
                if(res==null){
                    try {
                        DeviceManagementDataHolder.getInstance().getDeviceAPIClientService()
                                .addGroup(deviceGroup, groupId, tenantId);
                    } catch (TraccarConfigurationException e) {
                        log.error("Error while adding a existing group to Traccar " + e);
                    }
                }
                // add a group if not exist in traccar starts

                throw new GroupAlreadyExistException("Group exist with name " + deviceGroup.getName());
            }
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding deviceGroup '" + deviceGroup.getName() + "' to database.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupAlreadyExistException ex) {
            throw ex;
        } catch (Exception e) {
            String msg = "Error occurred in creating group '" + deviceGroup.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }

        if (log.isDebugEnabled()) {
            log.debug("DeviceGroup added: " + deviceGroup.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroup(DeviceGroup deviceGroup, int groupId)
            throws GroupManagementException, GroupNotExistException {
        if (deviceGroup == null) {
            String msg = "Received incomplete data for updateGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("update group '" + deviceGroup.getName() + "'");
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            DeviceGroup existingGroup = this.groupDAO.getGroup(groupId, tenantId);
            if (existingGroup != null) {
                List<DeviceGroup> groupsToUpdate = new ArrayList<>();
                String immediateParentID = StringUtils.substringAfterLast(existingGroup.getParentPath(), DeviceGroupConstants.HierarchicalGroup.SEPERATOR);
                String parentPath = "";
                if (deviceGroup.getParentGroupId() == 0) {
                    deviceGroup.setParentPath(DeviceGroupConstants.HierarchicalGroup.SEPERATOR);
                } else {
                    DeviceGroup immediateParentGroup = groupDAO.getGroup(deviceGroup.getParentGroupId(), tenantId);
                    if (immediateParentGroup == null) {
                        String msg = "Parent group with group ID '" + deviceGroup.getParentGroupId()
                                + "' does not exist.  Hence updating of group '" + groupId
                                + "' was not success";
                        log.error(msg);
                        throw new GroupManagementException(msg);
                    }
                    parentPath = DeviceManagerUtil.createParentPath(immediateParentGroup);
                    deviceGroup.setParentPath(parentPath);
                }
                deviceGroup.setGroupId(groupId);
                groupsToUpdate.add(deviceGroup);
                if (StringUtils.isNotBlank(immediateParentID)) {
                    List<DeviceGroup> childrenGroups = groupDAO.getChildrenGroups(
                            DeviceManagerUtil.createParentPath(existingGroup), tenantId);
                    for (DeviceGroup childrenGroup : childrenGroups) {
                        childrenGroup.setParentPath(childrenGroup.getParentPath()
                                .replace(existingGroup.getParentPath(), parentPath));
                        groupsToUpdate.add(childrenGroup);
                    }
                }
                this.groupDAO.updateGroups(groupsToUpdate, tenantId);
                if (deviceGroup.getGroupProperties() != null && deviceGroup.getGroupProperties().size() > 0) {
                    this.groupDAO.updateGroupProperties(deviceGroup, groupId, tenantId);
                }

                //procees to update a group in traccar starts
                if (HttpReportingUtil.isTrackerEnabled()) {
                    TrackerGroupInfo res = trackerDAO.getTraccarGroup(groupId, tenantId);
                    JSONObject obj = new JSONObject(res);
                    try {
                        DeviceManagementDataHolder.getInstance().getDeviceAPIClientService()
                                .updateGroup(deviceGroup, obj.getInt("traccarGroupId"), groupId, tenantId);
                    } catch (TraccarConfigurationException e) {
                        log.error("Error while updating the group in Traccar " + e);
                    }
                }
                //procees to update a group in traccar starts

                GroupManagementDAOFactory.commitTransaction();
            } else {
                throw new GroupNotExistException("Group with ID - '" + groupId + "' doesn't exists!");
            }
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while modifying device group with ID - '" + groupId + "'.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupNotExistException ex) {
            throw ex;
        } catch (Exception e) {
            String msg = "Error occurred in updating the device group with ID - '" + groupId + "'.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteGroup(int groupId, boolean isDeleteChildren) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Delete group: " + groupId);
        }
        DeviceGroup deviceGroup = getGroup(groupId, false);
        if (deviceGroup == null) {
            return false;
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            GroupManagementDAOFactory.beginTransaction();
            List<DeviceGroup> childrenGroups = new ArrayList<>();
            List<Integer> groupIdsToDelete = new ArrayList<>();
            if (deviceGroup.getChildrenGroups() != null && !deviceGroup.getChildrenGroups().isEmpty()) {
                String parentPath = DeviceManagerUtil.createParentPath(deviceGroup);
                childrenGroups = groupDAO.getChildrenGroups(parentPath, tenantId);
                if (isDeleteChildren) {
                    groupIdsToDelete = childrenGroups.stream().map(DeviceGroup::getGroupId)
                            .collect(Collectors.toList());
                } else {
                    for (DeviceGroup childrenGroup : childrenGroups) {
                        String newParentPath = childrenGroup.getParentPath()
                                .replace(DeviceGroupConstants.HierarchicalGroup.SEPERATOR + deviceGroup.getGroupId(), "");
                        if (StringUtils.isEmpty(newParentPath)) {
                            newParentPath = DeviceGroupConstants.HierarchicalGroup.SEPERATOR;
                        }
                        childrenGroup.setParentPath(newParentPath);
                    }
                }
            }

            //procees to delete a group from traccar starts
            if (HttpReportingUtil.isTrackerEnabled()) {
                TrackerGroupInfo res = trackerDAO.getTraccarGroup(groupId, tenantId);
                JSONObject obj = new JSONObject(res);
                trackerDAO.removeTraccarGroup(obj.getInt("id"));
                try {
                    DeviceManagementDataHolder.getInstance().getDeviceAPIClientService()
                            .deleteGroup(obj.getInt("traccarGroupId"), tenantId);
                } catch (TraccarConfigurationException e) {
                    log.error("Error while disenrolling a device from Traccar " + e);
                }
            }
            //procees to delete a group from traccar ends

            if (isDeleteChildren) {
                groupIdsToDelete.add(groupId);
                groupDAO.deleteGroupsMapping(groupIdsToDelete, tenantId);
                groupDAO.deleteGroups(groupIdsToDelete, tenantId);
                groupDAO.deleteAllGroupsProperties(groupIdsToDelete, tenantId);
            } else {
                groupDAO.deleteGroup(groupId, tenantId);
                groupDAO.deleteAllGroupProperties(groupId, tenantId);
                if (!childrenGroups.isEmpty()) {
                    groupDAO.updateGroups(childrenGroups, tenantId);
                }
            }
            GroupManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("DeviceGroup " + deviceGroup.getName() + " removed.");
            }

            return true;
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while removing group data.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in deleting group: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(int groupId, boolean requireGroupProps) throws GroupManagementException {
        return getGroup(groupId, requireGroupProps, 1);
    }

    @Override
    public DeviceGroup getGroup(int groupId, boolean requireGroupProps, int depth) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get group by id: " + groupId);
        }
        DeviceGroup deviceGroup;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            GroupManagementDAOFactory.openConnection();
            deviceGroup = this.groupDAO.getGroup(groupId, tenantId);
            if (deviceGroup != null) {
                String parentPath = DeviceManagerUtil.createParentPath(deviceGroup);
                List<DeviceGroup> childrenGroups = groupDAO.getChildrenGroups(parentPath, tenantId);
                createGroupWithChildren(deviceGroup, childrenGroups, requireGroupProps, tenantId, depth, 0);
                if (requireGroupProps) {
                    populateGroupProperties(deviceGroup, tenantId);
                }
            }
            return deviceGroup;
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while obtaining group '" + groupId + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to retrieve the group '"
                    + groupId + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(String groupName, boolean requireGroupProps) throws GroupManagementException {
        return getGroup(groupName, requireGroupProps, 1);
    }

    @Override
    public DeviceGroup getGroup(String groupName, boolean requireGroupProps, int depth) throws GroupManagementException {
        if (groupName == null) {
            String msg = "Received empty groupName for getGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get group by name '" + groupName + "'");
        }
        DeviceGroup deviceGroup;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            GroupManagementDAOFactory.openConnection();
            deviceGroup = this.groupDAO.getGroup(groupName, tenantId);
            if (deviceGroup != null) {
                String parentPath = DeviceManagerUtil.createParentPath(deviceGroup);
                List<DeviceGroup> childrenGroups = groupDAO.getChildrenGroups(parentPath, tenantId);
                createGroupWithChildren(deviceGroup, childrenGroups, requireGroupProps, tenantId, depth , 0);
                if (requireGroupProps) {
                    populateGroupProperties(deviceGroup, tenantId);
                }
            }
            return deviceGroup;
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while obtaining group with name: '" + groupName + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to retrieve group with name '"
                    + groupName + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceGroup> getGroups(boolean requireGroupProps) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups");
        }
        List<DeviceGroup> deviceGroups;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = groupDAO.getRootGroups(tenantId);
            for (DeviceGroup deviceGroup : deviceGroups) {
                if (requireGroupProps) {
                    populateGroupProperties(deviceGroup, tenantId);
                }
            }
            return deviceGroups;
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult getGroups(GroupPaginationRequest request, boolean requireGroupProps)
            throws GroupManagementException {
        if (request == null) {
            String msg = "Received incomplete data for getGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups with pagination " + request.toString());
        }
        request = DeviceManagerUtil.validateGroupListPageSize(request);
        List<DeviceGroup> rootGroups;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            request.setParentPath(DeviceGroupConstants.HierarchicalGroup.SEPERATOR);
            rootGroups = this.groupDAO.getGroups(request, tenantId);
            for (DeviceGroup rootGroup : rootGroups) {
                if (requireGroupProps) {
                    populateGroupProperties(rootGroup, tenantId);
                }
            }
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        PaginationResult groupResult = new PaginationResult();
        groupResult.setData(rootGroups);
        groupResult.setRecordsTotal(getGroupCount(request));
        return groupResult;
    }

    @Override
    public PaginationResult getGroupsWithHierarchy(String username, GroupPaginationRequest request,
            boolean requireGroupProps) throws GroupManagementException {
        if (request == null) {
            String msg = "Received incomplete data for retrieve groups with hierarchy";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups with hierarchy " + request.toString());
        }
        DeviceManagerUtil.validateGroupListPageSize(request);
        List<DeviceGroup> rootGroups;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            request.setParentPath(DeviceGroupConstants.HierarchicalGroup.SEPERATOR);
            if (StringUtils.isBlank(username)) {
                GroupManagementDAOFactory.openConnection();
                rootGroups = groupDAO.getGroups(request, tenantId);
            } else {
                List<Integer> allDeviceGroupIdsOfUser = getGroupIds(username);
                GroupManagementDAOFactory.openConnection();
                rootGroups = this.groupDAO.getGroups(request, allDeviceGroupIdsOfUser, tenantId);
            }
            String parentPath;
            List<DeviceGroup> childrenGroups;
            for (DeviceGroup rootGroup : rootGroups) {
                parentPath = DeviceManagerUtil.createParentPath(rootGroup);
                childrenGroups = groupDAO.getChildrenGroups(parentPath, tenantId);
                createGroupWithChildren(
                        rootGroup, childrenGroups, requireGroupProps, tenantId, request.getDepth(), 0);
                if (requireGroupProps) {
                    populateGroupProperties(rootGroup, tenantId);
                }
            }
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups with hierarchy";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to retrieve all groups "
                    + "with hierarchy";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        PaginationResult groupResult = new PaginationResult();
        groupResult.setData(rootGroups);
        if (StringUtils.isBlank(username)) {
            groupResult.setRecordsTotal(getGroupCount(request));
        } else {
            groupResult.setRecordsTotal(getGroupCount(username, request.getParentPath()));
        }
        return groupResult;
    }

    @Override
    public List<DeviceGroup> getGroups(String username, boolean requireGroupProps) throws GroupManagementException {
        if (username == null || username.isEmpty()) {
            String msg = "Received null user name for getGroups";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups of owner '" + username + "'");
        }
        Map<Integer, DeviceGroup> groups = new HashMap<>();
        List<DeviceGroup> mergedGroups = new ArrayList<>();
        UserStoreManager userStoreManager;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            GroupManagementDAOFactory.openConnection();
            List<DeviceGroup> deviceGroups = this.groupDAO.getOwnGroups(username, tenantId);
            for (DeviceGroup deviceGroup : deviceGroups) {
                groups.put(deviceGroup.getGroupId(), deviceGroup);
            }
            deviceGroups = this.groupDAO.getGroups(roleList, tenantId);
            for (DeviceGroup deviceGroup : deviceGroups) {
                groups.put(deviceGroup.getGroupId(), deviceGroup);
            }
            if (requireGroupProps) {
                for (DeviceGroup deviceGroup : groups.values()) {
                    populateGroupProperties(deviceGroup, tenantId);
                    mergedGroups.add(deviceGroup);
                }
            } else {
                mergedGroups.addAll(groups.values());
            }
        } catch (UserStoreException | SQLException | GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups accessible to user.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups for " + username;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return mergedGroups;
    }

    private List<Integer> getGroupIds(String username) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups Ids of owner '" + username + "'");
        }
        UserStoreManager userStoreManager;
        List<Integer> deviceGroupIds;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            GroupManagementDAOFactory.openConnection();
            deviceGroupIds = this.groupDAO.getOwnGroupIds(username, tenantId);
            deviceGroupIds.addAll(this.groupDAO.getGroupIds(roleList, tenantId));
        } catch (UserStoreException | SQLException | GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups accessible to user.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups for username '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return deviceGroupIds;
    }

    @Override
    public PaginationResult getGroups(String currentUser, GroupPaginationRequest request, boolean requireGroupProps)
            throws GroupManagementException {
        if (currentUser == null || request == null) {
            String msg = "Received incomplete date for getGroups";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get all groups of user '" + currentUser + "' pagination request " + request.toString());
        }
        request = DeviceManagerUtil.validateGroupListPageSize(request);
        List<Integer> allDeviceGroupIdsOfUser = getGroupIds(currentUser);
        List<DeviceGroup> rootGroups;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            request.setParentPath(DeviceGroupConstants.HierarchicalGroup.SEPERATOR);
            rootGroups = this.groupDAO.getGroups(request, allDeviceGroupIdsOfUser, tenantId);
            for (DeviceGroup rootGroup : rootGroups) {
                if (requireGroupProps) {
                    populateGroupProperties(rootGroup, tenantId);
                }
            }
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        PaginationResult groupResult = new PaginationResult();
        groupResult.setData(rootGroups);
        groupResult.setRecordsTotal(getGroupCount(currentUser, request.getParentPath()));
        return groupResult;
    }

    @Override
    public int getGroupCount() throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups count");
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroupCount(tenantId, null);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public int getGroupCountByStatus(String status) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups count by Status");
        }
        int tenantId = -1;
        try {
            tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroupCount(tenantId, status);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant " + tenantId
                    + " by status : " + status;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    private int getGroupCount(GroupPaginationRequest request) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups count, pagination request " + request.toString());
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroupCount(request, tenantId);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroupCount";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount(String username, String parentPath) throws GroupManagementException {
        if (username == null || username.isEmpty()) {
            String msg = "Received empty user name for getGroupCount";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups count of '" + username + "'");
        }
        UserStoreManager userStoreManager;
        int count;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            GroupManagementDAOFactory.openConnection();
            count = groupDAO.getOwnGroupsCount(username, tenantId, parentPath);
            count += groupDAO.getGroupsCount(roleList, tenantId, parentPath);
            return count;
        } catch (UserStoreException e) {
            String msg = "Error occurred while retrieving role list of user '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening db connection to get group count.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving group count of user '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void manageGroupSharing(int groupId, List<String> newRoles)
            throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Manage group sharing for group: " + groupId);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserStoreManager userStoreManager;
        try {
            userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            tenantId).getUserStoreManager();
        } catch (UserStoreException e) {
            String msg = "User store error in updating sharing roles.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        }
        List<String> currentUserRoles = getRoles(groupId);
        try {

            GroupManagementDAOFactory.beginTransaction();
            if (newRoles != null) {
                for (String role : newRoles) {
                    if (!userStoreManager.isExistingRole(role)) {
                        throw new RoleDoesNotExistException("Role '" + role + "' does not exists in the user store.");
                    }
                    // Removing role from current user roles of the group will return true if role exist.
                    // So we don't need to add it to the db again.
                    if (!currentUserRoles.remove(role)) {
                        // If group doesn't have the role, it is adding to the db.
                        groupDAO.addRole(groupId, role, tenantId);
                    }
                }
            }
            for (String role : currentUserRoles) {
                // Removing old roles from db which are not available in the new roles list.
                groupDAO.removeRole(groupId, role, tenantId);
            }
            GroupManagementDAOFactory.commitTransaction();
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            log.error(e);
            throw new GroupManagementException(e);
        } catch (TransactionManagementException e) {
            log.error(e);
            throw new GroupManagementException(e);
        } catch (Exception e) {
            String msg = "Error occurred in manageGroupSharing for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(int groupId) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Group roles for group: " + groupId);
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getRoles(groupId, tenantId);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getRoles for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Device> getDevices(int groupId, int startIndex, int rowCount, boolean requireDeviceProps)
            throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Group devices of group: " + groupId + " start index " + startIndex + " row count " + rowCount);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devices;
        try {
            rowCount = DeviceManagerUtil.validateDeviceListPageSize(rowCount);
            GroupManagementDAOFactory.openConnection();
            devices = this.groupDAO.getDevices(groupId, startIndex, rowCount, tenantId);
            if (requireDeviceProps) {
                DeviceManagementDAOFactory.openConnection();
                for (Device device : devices) {
                    Device retrievedDevice = deviceDAO.getDeviceProps(device.getDeviceIdentifier(), tenantId);
                    if (retrievedDevice != null && !retrievedDevice.getProperties().isEmpty()) {
                        device.setProperties(retrievedDevice.getProperties());
                    }
                }
            }
        } catch (GroupManagementDAOException | SQLException | DeviceManagementException e) {
            String msg = "Error occurred while getting devices in group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevices for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
            if (requireDeviceProps) {
                DeviceManagementDAOFactory.closeConnection();
            }
        }
        return devices;
    }

    @Override
    public List<Device> getAllDevicesOfGroup(String groupName, boolean requireDeviceProps)
            throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Group devices of group: " + groupName);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devices;
        try {
            GroupManagementDAOFactory.openConnection();
            devices = this.groupDAO.getAllDevicesOfGroup(groupName, tenantId);
            if (requireDeviceProps) {
                return loadDeviceProperties(devices);
            }
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while getting devices in group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevices for group name: " + groupName;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return devices;
    }

    @Override
    public List<Device> getAllDevicesOfGroup(String groupName, List<String> deviceStatuses, boolean requireDeviceProps)
            throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Group devices of group: " + groupName);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devices;
        try {
            GroupManagementDAOFactory.openConnection();
            devices = this.groupDAO.getAllDevicesOfGroup(groupName, deviceStatuses, tenantId);
            if (requireDeviceProps) {
                return loadDeviceProperties(devices);
            }
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while getting devices in group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevices for group name: " + groupName;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return devices;
    }

    /**
     * Load Dice properties of given list of devices
     *
     * @param devices list of devices
     * @return list of devices which contains device properties
     * @throws GroupManagementException if error occurred while loading device properties of devices which are in a
     *                                  particular device group
     */
    private List<Device> loadDeviceProperties(List<Device> devices) throws GroupManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.openConnection();
            for (Device device : devices) {
                Device retrievedDevice = deviceDAO.getDeviceProps(device.getDeviceIdentifier(), tenantId);
                if (retrievedDevice != null && !retrievedDevice.getProperties().isEmpty()) {
                    device.setProperties(retrievedDevice.getProperties());
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while opening the connection for loading device properties of group devices.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while loading device properties of group devices.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return devices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDeviceCount(int groupId) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Group devices count of group: " + groupId);
        }
        try {
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getDeviceCount(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceCount for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDevices(int groupId, List<DeviceIdentifier> deviceIdentifiers)
            throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Group devices to the group: " + groupId);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devicesList = null;
        try {

            List<String> deviceIdentifierList = deviceIdentifiers.stream()
                    .map(DeviceIdentifier::getId)
                    .collect(Collectors.toCollection(ArrayList::new));
            devicesList = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                    .getDeviceByIdList(deviceIdentifierList);
            if (devicesList == null || devicesList.isEmpty()) {
                throw new DeviceNotFoundException("Couldn't find any devices for the given deviceIdentifiers '" + deviceIdentifiers + "'");
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving devices using device identifiers";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in addDevices for groupId " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        }

        try {
            GroupManagementDAOFactory.beginTransaction();
            for (Device device : devicesList) {
                if (!this.groupDAO.isDeviceMappedToGroup(groupId, device.getId(), tenantId)) {
                    this.groupDAO.addDevice(groupId, device.getId(), tenantId);
                }
            }
            GroupManagementDAOFactory.commitTransaction();
            createEventTask(OperationMgtConstants.OperationCodes.EVENT_CONFIG, groupId, deviceIdentifiers, tenantId);
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device to group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in addDevices for groupId " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDevice(int groupId, List<DeviceIdentifier> deviceIdentifiers)
            throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Remove devices from the group: " + groupId);
        }
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                        getDevice(deviceIdentifier, false);
                if (device == null) {
                    throw new DeviceNotFoundException("Device not found for id '" + deviceIdentifier.getId() + "'");
                }
                this.groupDAO.removeDevice(groupId, device.getId(), tenantId);
            }
            GroupManagementDAOFactory.commitTransaction();
            createEventTask(OperationMgtConstants.OperationCodes.EVENT_REVOKE, groupId, deviceIdentifiers, tenantId);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device to group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in removeDevice for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> getGroups(String username, String permission, boolean requireGroupProps)
            throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups of user '" + username + "'");
        }
        List<DeviceGroup> deviceGroups = getGroups(username, false);
        Map<Integer, DeviceGroup> permittedDeviceGroups = new HashMap<>();
        UserRealm userRealm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            for (DeviceGroup deviceGroup : deviceGroups) {
                List<String> roles = getRoles(deviceGroup.getGroupId());
                for (String roleName : roles) {
                    if (userRealm.getAuthorizationManager().
                            isRoleAuthorized(roleName, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                        if (requireGroupProps) {
                            populateGroupProperties(deviceGroup, tenantId);
                        }
                        permittedDeviceGroups.put(deviceGroup.getGroupId(), deviceGroup);
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while getting user realm.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups for username '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        }
        return new ArrayList<>(permittedDeviceGroups.values());
    }

    @Override
    public List<DeviceGroup> getGroups(DeviceIdentifier deviceIdentifier, boolean requireGroupProps)
            throws GroupManagementException {
        if (deviceIdentifier == null) {
            String msg = "Received empty device identifier for getGroups";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups of device " + deviceIdentifier.getId());
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        DeviceManagementProviderService managementProviderService = DeviceManagementDataHolder
                .getInstance().getDeviceManagementProvider();
        Device device;
        try {
            device = managementProviderService.getDevice(deviceIdentifier, false);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device groups.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        }
        return getDeviceGroups(requireGroupProps, tenantId, device);
    }

    private List<DeviceGroup> getDeviceGroups(boolean requireGroupProps, int tenantId, Device device) throws GroupManagementException {
        try {
            GroupManagementDAOFactory.openConnection();
            List<DeviceGroup> deviceGroups = groupDAO.getGroups(device.getId(), tenantId);
            if (requireGroupProps) {
                if (deviceGroups != null && !deviceGroups.isEmpty()) {
                    for (DeviceGroup group : deviceGroups) {
                        populateGroupProperties(group, tenantId);
                    }
                }
            }
            return deviceGroups;
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving device groups.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<DeviceGroup> getGroups(Device device, boolean requireGroupProps)
            throws GroupManagementException {
        if (device.getDeviceIdentifier() == null) {
            String msg = "Received empty device identifier for getGroups";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups of device " + device.getDeviceIdentifier());
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        return getDeviceGroups(requireGroupProps, tenantId, device);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup createDefaultGroup(String groupName) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Create default group " + groupName);
        }
        DeviceGroup defaultGroup = this.getGroup(groupName, false);
        if (defaultGroup == null) {
            defaultGroup = new DeviceGroup(groupName);
            defaultGroup.setStatus(DeviceGroupConstants.GroupStatus.ACTIVE);
            // Setting system level user (wso2.system.user) as the owner
            defaultGroup.setOwner(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            defaultGroup.setDescription("Default system group for devices with " + groupName + " ownership.");
            try {
                this.createGroup(defaultGroup, DeviceGroupConstants.Roles.DEFAULT_ADMIN_ROLE,
                        DeviceGroupConstants.Permissions.DEFAULT_ADMIN_PERMISSIONS);
            } catch (GroupAlreadyExistException e) {
                String msg = "Default group: " + defaultGroup.getName() + " already exists. Skipping group creation.";
                log.error(msg, e);
                throw new GroupManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred in createDefaultGroup for groupName '" + groupName + "'";
                log.error(msg, e);
                throw new GroupManagementException(msg, e);
            }
            return this.getGroup(groupName, false);
        } else {
            return defaultGroup;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceMappedToGroup(int groupId, DeviceIdentifier deviceIdentifier)
            throws GroupManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    getDevice(deviceIdentifier, false);
            if (device == null) {
                throw new GroupManagementException("Device not found for id '" + deviceIdentifier.getId() +
                        "' type '" + deviceIdentifier.getType() + "'");
            }
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Device management exception occurred when retrieving device. " +
                    e.getMessage(), e);
        }

        try {
            GroupManagementDAOFactory.openConnection();
            return this.groupDAO.isDeviceMappedToGroup(groupId, device.getId(), tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred when checking device, group mapping between device id '" +
                    deviceIdentifier.getId() + "' and group id '" + groupId + "'", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred when opening db connection to check device, group " +
                    "mapping between device id '" + deviceIdentifier.getId() +
                    "' and group id '" + groupId + "'", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * This is populating group properties in to the group pass as an argument. Call should be manage the transactions.
     *
     * @param deviceGroup which needs to populate with properties
     * @param tenantId of the caller
     * @throws GroupManagementDAOException on DAO exceptions
     */
    private void populateGroupProperties(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException {
        if (deviceGroup != null && deviceGroup.getGroupId() > 0) {
            deviceGroup.setGroupProperties(this.groupDAO.getAllGroupProperties(deviceGroup.getGroupId(),
                    tenantId));
        }
    }

    /**
     * Create event config/revoke operation at the time of device removing from a group/assigning into group
     * @param eventOperationCode code of the event operation(config/revoke)
     * @param groupId Id of the device removing/assigning group
     * @param deviceIdentifiers devices assigning to/removing from group
     * @param tenantId tenant of the group
     */
    private void createEventTask(String eventOperationCode, int groupId, List<DeviceIdentifier> deviceIdentifiers,
                                 int tenantId) {
        GeoFenceEventOperationManager eventManager = new GeoFenceEventOperationManager(eventOperationCode, tenantId, null);
        GroupAssignmentEventOperationExecutor eventOperationExecutor = eventManager
                .getGroupAssignmentEventExecutor(groupId, deviceIdentifiers);
        if (eventOperationExecutor != null) {
            ExecutorService eventConfigExecutors = DeviceManagementDataHolder.getInstance().getEventConfigExecutors();
            if (eventConfigExecutors != null) {
                eventConfigExecutors.submit(eventOperationExecutor);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Ignoring event creation since not enabled. Tenant id: " + tenantId + " Group Id: " + groupId);
            }
        }
    }

    /**
     * Recursive method to create group with children based on params to provide hierarchical grouping.
     * @param parentGroup to which children group should be set.
     * @param childrenGroups which are descendants of parent group.
     * @param requireGroupProps to include device properties.
     * @param tenantId of the group.
     * @param depth of children groups set and when reaches recursive call returns to callee.
     * @param counter to track the recursive calls and to stop when reaches the depth.
     * @throws GroupManagementDAOException on error during population of group properties.
     */
    private void createGroupWithChildren(DeviceGroup parentGroup, List<DeviceGroup> childrenGroups,
            boolean requireGroupProps, int tenantId, int depth, int counter) throws GroupManagementDAOException {
        if (childrenGroups.isEmpty() || depth == counter) {
            return;
        }
        List<DeviceGroup> immediateChildrenGroups = new ArrayList<>();
        Iterator<DeviceGroup> iterator = childrenGroups.iterator();
        while (iterator.hasNext()) {
            DeviceGroup childGroup = iterator.next();
            int immediateParentID = Integer.parseInt(StringUtils.substringAfterLast(
                    childGroup.getParentPath(), DeviceGroupConstants.HierarchicalGroup.SEPERATOR));
            if (immediateParentID == parentGroup.getGroupId()) {
                if (requireGroupProps) {
                    populateGroupProperties(childGroup, tenantId);
                }
                immediateChildrenGroups.add(childGroup);
                iterator.remove();
            }
        }
        parentGroup.setChildrenGroups(immediateChildrenGroups);
        counter++;
        for (DeviceGroup nextParentGroup : immediateChildrenGroups) {
            createGroupWithChildren(nextParentGroup, childrenGroups, requireGroupProps, tenantId, depth, counter);
        }
    }
}
