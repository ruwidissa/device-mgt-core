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
package io.entgra.device.mgt.core.application.mgt.core.dao.impl.subscription;

import io.entgra.device.mgt.core.application.mgt.common.SubscriptionMetadata;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceOperationDTO;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionEntity;
import io.entgra.device.mgt.core.application.mgt.common.dto.SubscriptionStatisticDTO;
import io.entgra.device.mgt.core.application.mgt.core.dao.SubscriptionDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.impl.AbstractDAOImpl;
import io.entgra.device.mgt.core.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.device.mgt.core.application.mgt.core.util.DAOUtil;
import io.entgra.device.mgt.core.application.mgt.core.util.HelperUtil;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.application.mgt.common.ExecutionStatus;
import io.entgra.device.mgt.core.application.mgt.common.SubAction;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionType;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ScheduledSubscriptionDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class GenericSubscriptionDAOImpl extends AbstractDAOImpl implements SubscriptionDAO {
    private static final Log log = LogFactory.getLog(GenericSubscriptionDAOImpl.class);

    @Override
    public void addDeviceSubscription(String subscribedBy, List<Integer> deviceIds,
            String subscribedFrom, String installStatus, int releaseId, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_DEVICE_SUBSCRIPTION("
                + "SUBSCRIBED_BY, "
                + "SUBSCRIBED_TIMESTAMP, "
                + "ACTION_TRIGGERED_FROM, "
                + "STATUS, "
                + "DM_DEVICE_ID, "
                + "AP_APP_RELEASE_ID,"
                + "TENANT_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (Integer deviceId : deviceIds) {
                    stmt.setString(1, subscribedBy);
                    stmt.setTimestamp(2, timestamp);
                    stmt.setString(3, subscribedFrom);
                    stmt.setString(4, installStatus);
                    stmt.setInt(5, deviceId);
                    stmt.setInt(6, releaseId);
                    stmt.setInt(7, tenantId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a device subscription for device id " + deviceId + " and application "
                                + "release which has release id" + releaseId);
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while obtaining database connection to add device subscription for application "
                    + "release which has release Id" + releaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured when processing SQL to add device subscription for application release which"
                    + " has release Id " + releaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateDeviceSubscription(String updateBy, List<Integer> deviceIds,
            String action, String actionTriggeredFrom, String installStatus, int releaseId, int tenantId)
            throws ApplicationManagementDAOException {
        boolean unsubscribed = false;
        try {
            String sql = "UPDATE AP_DEVICE_SUBSCRIPTION SET ";

            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                sql += "UNSUBSCRIBED = ?, UNSUBSCRIBED_BY = ?, UNSUBSCRIBED_TIMESTAMP = ?, ";
                unsubscribed = true;
            } else if (SubAction.INSTALL.toString().equalsIgnoreCase(action)) {
                sql += "UNSUBSCRIBED = ?, SUBSCRIBED_BY = ?, SUBSCRIBED_TIMESTAMP = ?, ";
            } else {
                String msg = "Found invalid action " + action + ". Hence can't construct the query.";
                log.error(msg);
                throw new ApplicationManagementDAOException(msg);
            }
            sql += "ACTION_TRIGGERED_FROM = ?, " +
                    "STATUS = ? " +
                    "WHERE " +
                    "DM_DEVICE_ID = ? AND " +
                    "AP_APP_RELEASE_ID = ? AND " +
                    "TENANT_ID = ?";

            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (Integer deviceId : deviceIds) {
                    stmt.setBoolean(1, unsubscribed);
                    stmt.setString(2, updateBy);
                    stmt.setTimestamp(3, timestamp);
                    stmt.setString(4, actionTriggeredFrom);
                    stmt.setString(5, installStatus);
                    stmt.setInt(6, deviceId);
                    stmt.setInt(7, releaseId);
                    stmt.setInt(8, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update device subscriptions of "
                    + "application. Updated by: " + updateBy + " and updating action triggered from "
                    + actionTriggeredFrom;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to update the device subscriptions of application. "
                    + "Updated by: " + updateBy + " and updating action triggered from " + actionTriggeredFrom;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addOperationMapping(int operationId, List<Integer> deviceSubscriptionIds, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_APP_SUB_OP_MAPPING("
                + "OPERATION_ID, "
                + "AP_DEVICE_SUBSCRIPTION_ID, "
                + "TENANT_ID) "
                + "VALUES (?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer subId : deviceSubscriptionIds) {
                    stmt.setInt(1, operationId);
                    stmt.setInt(2, subId);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a operation mapping for subscription id " + subId);
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add operation subscription mapping to DB";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add operation subscription mapping to DB. Executed "
                    + "query: " + sql;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addUserSubscriptions(int tenantId, String subscribedBy, List<String> users, int releaseId,
            String action) throws ApplicationManagementDAOException {
        try {
            boolean isUnsubscribed = false;
            String sql = "INSERT INTO AP_USER_SUBSCRIPTION(TENANT_ID, ";

            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                sql += "UNSUBSCRIBED, UNSUBSCRIBED_BY, UNSUBSCRIBED_TIMESTAMP, ";
                isUnsubscribed = true;
            } else {
                sql += "UNSUBSCRIBED, SUBSCRIBED_BY, SUBSCRIBED_TIMESTAMP, ";
            }

            sql += "USER_NAME, AP_APP_RELEASE_ID) VALUES (?, ?, ?, ?, ?,?)";

            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String user : users) {
                    stmt.setInt(1, tenantId);
                    stmt.setBoolean(2, isUnsubscribed);
                    stmt.setString(3, subscribedBy);
                    stmt.setTimestamp(4, timestamp);
                    stmt.setString(5, user);
                    stmt.setInt(6, releaseId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding an user subscription for user " + user + " and application release which "
                                + "has Id " + releaseId + " to the database.");
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add user subscription. Subscribing user "
                    + "is " + subscribedBy;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add user subscription. Subscribing user is "
                    + subscribedBy;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addRoleSubscriptions(int tenantId, String subscribedBy, List<String> roles, int releaseId, String action)
            throws ApplicationManagementDAOException {
        try {
            boolean isUnsubscribed = false;
            String sql = "INSERT INTO AP_ROLE_SUBSCRIPTION(TENANT_ID, ";

            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                sql += "UNSUBSCRIBED, UNSUBSCRIBED_BY, UNSUBSCRIBED_TIMESTAMP, ";
                isUnsubscribed = true;
            } else {
                sql += "UNSUBSCRIBED, SUBSCRIBED_BY, SUBSCRIBED_TIMESTAMP, ";
            }

            sql += "ROLE_NAME, AP_APP_RELEASE_ID) VALUES (?, ?, ?, ?, ?,?)";

            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String role : roles) {
                    stmt.setInt(1, tenantId);
                    stmt.setBoolean(2, isUnsubscribed);
                    stmt.setString(3, subscribedBy);
                    stmt.setTimestamp(4, timestamp);
                    stmt.setString(5, role);
                    stmt.setInt(6, releaseId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a role subscription for role " + role + " and application release which "
                                + "has Id " + releaseId + " to the database.");
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add role subscription. Subscribing role "
                    + "is " + subscribedBy;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add role subscription. Subscribing role is "
                    + subscribedBy;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addGroupSubscriptions(int tenantId, String subscribedBy, List<String> groups, int releaseId,
            String action) throws ApplicationManagementDAOException {
        try {
            boolean isUnsubscribed = false;
            String sql = "INSERT INTO AP_GROUP_SUBSCRIPTION(TENANT_ID, ";

            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                sql += "UNSUBSCRIBED, UNSUBSCRIBED_BY, UNSUBSCRIBED_TIMESTAMP, ";
                isUnsubscribed = true;
            } else {
                sql += "UNSUBSCRIBED, SUBSCRIBED_BY, SUBSCRIBED_TIMESTAMP, ";
            }

            sql += "GROUP_NAME, AP_APP_RELEASE_ID) VALUES (?, ?, ?, ?, ?,?)";

            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String group : groups) {
                    stmt.setInt(1, tenantId);
                    stmt.setBoolean(2, isUnsubscribed);
                    stmt.setString(3, subscribedBy);
                    stmt.setTimestamp(4, timestamp);
                    stmt.setString(5, group);
                    stmt.setInt(6, releaseId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a group subscription for role " + group + " and application release which "
                                + "has Id " + releaseId + " to the database.");
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add group subscription. Subscribing "
                    + "group is " + subscribedBy;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add group subscription. Subscribing group is "
                    + subscribedBy;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceSubscriptionDTO> getDeviceSubscriptions(int appReleaseId, int tenantId, String actionStatus, String action) throws
            ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions for the application release id " + appReleaseId
                    + " from the database");
        }
        boolean isActionStatusProvided = false;
        boolean isActionProvided = false;
        int index = 1;
        String sql = "SELECT "
                + "DS.ID AS ID, "
                + "DS.SUBSCRIBED_BY AS SUBSCRIBED_BY, "
                + "DS.SUBSCRIBED_TIMESTAMP AS SUBSCRIBED_AT, "
                + "DS.UNSUBSCRIBED AS IS_UNSUBSCRIBED, "
                + "DS.UNSUBSCRIBED_BY AS UNSUBSCRIBED_BY, "
                + "DS.UNSUBSCRIBED_TIMESTAMP AS UNSUBSCRIBED_AT, "
                + "DS.ACTION_TRIGGERED_FROM AS ACTION_TRIGGERED_FROM, "
                + "DS.STATUS AS STATUS,"
                + "DS.DM_DEVICE_ID AS DEVICE_ID "
                + "FROM AP_DEVICE_SUBSCRIPTION DS "
                + "WHERE DS.AP_APP_RELEASE_ID = ? AND DS.TENANT_ID=?";

        if (actionStatus != null && !actionStatus.isEmpty()) {
            sql += " AND DS.STATUS= ?";
            isActionStatusProvided = true;
        }
        if (action != null && !action.isEmpty()) {
            sql += " AND DS.UNSUBSCRIBED= ?";
            isActionProvided = true;
        }
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(index++, appReleaseId);
                stmt.setInt(index++, tenantId);
                if (isActionStatusProvided) {
                    stmt.setString(index++, actionStatus);
                }
                if (isActionProvided) {
                    if (action.equals("SUBSCRIBED")) {
                        stmt.setString(index, "FALSE");
                    } else {
                        stmt.setString(index, "TRUE");
                    }
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved device subscriptions for application release id "
                                + appReleaseId);
                    }
                    return DAOUtil.loadDeviceSubscriptions(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device subscription for "
                    + "application Id: " + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while while running SQL to get device subscription data for application ID: " + appReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions(List<Integer> deviceIds, int appReleaseId,
            int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get device subscriptions for given device ids.");
        }
        try {
            Map<Integer, DeviceSubscriptionDTO> deviceSubscriptionDTOHashMap = new HashMap<>();
            if (deviceIds.isEmpty()) {
                return deviceSubscriptionDTOHashMap;
            }
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "DS.ID AS ID, "
                            + "DS.SUBSCRIBED_BY AS SUBSCRIBED_BY, "
                            + "DS.SUBSCRIBED_TIMESTAMP AS SUBSCRIBED_AT, "
                            + "DS.UNSUBSCRIBED AS IS_UNSUBSCRIBED, "
                            + "DS.UNSUBSCRIBED_BY AS UNSUBSCRIBED_BY, "
                            + "DS.UNSUBSCRIBED_TIMESTAMP AS UNSUBSCRIBED_AT, "
                            + "DS.ACTION_TRIGGERED_FROM AS ACTION_TRIGGERED_FROM, "
                            + "DS.DM_DEVICE_ID AS DEVICE_ID, "
                            + "DS.STATUS AS STATUS "
                            + "FROM AP_DEVICE_SUBSCRIPTION DS "
                            + "WHERE DS.DM_DEVICE_ID IN (", ") AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?");
            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }
                ps.setInt(index++, appReleaseId);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DeviceSubscriptionDTO deviceSubscriptionDTO = DAOUtil.constructDeviceSubscriptionDTO(rs);
                        if (deviceSubscriptionDTOHashMap.containsKey(deviceSubscriptionDTO.getDeviceId())){
                            String msg = "There shouldn't be Device ids in multiple times in AP_DEVICE_SUBSCRIPTION "
                                    + "table.";
                            log.error(msg);
                            throw new ApplicationManagementDAOException(msg);
                        }
                        deviceSubscriptionDTOHashMap.put(deviceSubscriptionDTO.getDeviceId(), deviceSubscriptionDTO);
                    }
                }
            }
            return deviceSubscriptionDTOHashMap;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get device subscriptions for given device"
                    + " Ids.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting device subscriptions for given device Ids.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    public List<String> getAppSubscribedUserNames(List<String> users, int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed users for given list of user names.");
        }
        try {
            List<String> subscribedUsers = new ArrayList<>();
            if (users.isEmpty()) {
                return subscribedUsers;
            }
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT US.USER_NAME AS USER_NAME "
                            + "FROM AP_USER_SUBSCRIPTION US "
                            + "WHERE US.USER_NAME IN (", ") AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?");
            users.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String username : users) {
                    ps.setObject(index++, username);
                }
                ps.setInt(index++, appReleaseId);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("USER_NAME"));
                    }
                }
            }
            return subscribedUsers;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already subscribed users for given "
                    + "user names.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed users for given user names.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    public List<String> getAppSubscribedRoleNames(List<String> roles, int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed role names for given list of roles.");
        }
        try {
            List<String> subscribedRoles = new ArrayList<>();
            if (roles.isEmpty()) {
                return subscribedRoles;
            }
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT RS.ROLE_NAME AS ROLE "
                            + "FROM AP_ROLE_SUBSCRIPTION RS "
                            + "WHERE RS.ROLE_NAME IN (", ") AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?");
            roles.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String roleName : roles) {
                    ps.setObject(index++, roleName);
                }
                ps.setInt(index++, appReleaseId);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedRoles.add(rs.getString("ROLE"));
                    }
                }
            }
            return subscribedRoles;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to getg subscribed roles for given role "
                    + "names.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SWL Error occurred while getting subscribes roles for given role names.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedGroupNames(List<String> groups, int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed groups for given list of groups.");
        }
        try {
            List<String> subscribedGroups = new ArrayList<>();
            if (groups.isEmpty()) {
                return subscribedGroups;
            }
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT GS.GROUP_NAME AS GROUP_NAME "
                            + "FROM AP_GROUP_SUBSCRIPTION GS "
                            + "WHERE GS.GROUP_NAME IN (", ") AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?");
            groups.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String groupName : groups) {
                    ps.setObject(index++, groupName);
                }
                ps.setInt(index++, appReleaseId);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedGroups.add(rs.getString("GROUP_NAME"));
                    }
                }
            }
            return subscribedGroups;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already subscribed groups for given "
                    + "group names.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting already subscribed groups for given group names.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDeviceSubIds(List<Integer> deviceIds, int applicationReleaseId,
            int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received to DAO Layer to get already subscribed dvice Ids for given list of device Ids.");
        }
        try {
            List<Integer> subscribedDevices = new ArrayList<>();
            if (deviceIds.isEmpty()) {
                return subscribedDevices;
            }
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT DS.ID AS DEVICE_SUBSCRIPTION_ID "
                            + "FROM AP_DEVICE_SUBSCRIPTION DS "
                            + "WHERE DS.DM_DEVICE_ID IN (", ") AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?");
            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }
                ps.setInt(index++, applicationReleaseId);
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedDevices.add(rs.getInt("DEVICE_SUBSCRIPTION_ID"));
                    }
                }
            }
            return subscribedDevices;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get subscribed device Ids for given "
                    + "device Id list.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting already subscribed device ids for given device Id list.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateSubscriptions(int tenantId, String updateBy, List<String> paramList, int releaseId,
            String subType, String action) throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            boolean isUnsubscribed = false;
            String sql = "UPDATE ";
            if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                sql += "AP_USER_SUBSCRIPTION SET ";
            } else if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                sql += "AP_ROLE_SUBSCRIPTION SET ";
            } else if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                sql += "AP_GROUP_SUBSCRIPTION SET ";
            }

            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                sql += "UNSUBSCRIBED = ?, UNSUBSCRIBED_BY = ?, UNSUBSCRIBED_TIMESTAMP = ? ";
                isUnsubscribed = true;
            } else {
                sql += "UNSUBSCRIBED = ?, SUBSCRIBED_BY = ?, SUBSCRIBED_TIMESTAMP = ? ";
            }

            if (SubscriptionType.USER.toString().equalsIgnoreCase(subType)) {
                sql += "WHERE USER_NAME = ? ";
            } else if (SubscriptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                sql += "WHERE ROLE_NAME = ? ";
            } else if (SubscriptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                sql += "WHERE GROUP_NAME = ? ";
            }

            sql += "AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String username : paramList) {
                    stmt.setBoolean(1, isUnsubscribed);
                    stmt.setString(2, updateBy);
                    stmt.setTimestamp(3, timestamp);
                    stmt.setString(4, username);
                    stmt.setInt(5, releaseId);
                    stmt.setInt(6, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update the user/role/group subscriptions "
                    + "of application.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while processing SQL to update the user/role/group subscriptions of "
                    + "application.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getDeviceIdForSubId(int subId, int tenantId) throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT DM_DEVICE_ID "
                    + "FROM AP_DEVICE_SUBSCRIPTION "
                    + "WHERE ID = ? AND "
                    + "TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, subId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("DM_DEVICE_ID");
                    }
                }
                return -1;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get app operation ids for given "
                    + "subscription id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to get operation ids for given subscription id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getOperationIdsForSubId(int subId, int tenantId) throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            List<Integer> operationIds = new ArrayList<>();
            String sql = "SELECT OPERATION_ID "
                    + "FROM AP_APP_SUB_OP_MAPPING "
                    + "WHERE AP_DEVICE_SUBSCRIPTION_ID = ? AND "
                    + "TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, subId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        operationIds.add(rs.getInt("OPERATION_ID"));
                    }
                }
                return operationIds;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get app operation ids for given "
                    + "subscription id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to get operation ids for given subscription id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }


    @Override
    public List<Integer> getDeviceSubIdsForOperation(int operationId, int deviceId, int tenantId)
            throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            List<Integer> deviceSubIds = new ArrayList<>();
            String sql = "SELECT AP_APP_SUB_OP_MAPPING.AP_DEVICE_SUBSCRIPTION_ID "
                    + "FROM "
                    + "AP_APP_SUB_OP_MAPPING INNER JOIN AP_DEVICE_SUBSCRIPTION "
                    + "ON AP_APP_SUB_OP_MAPPING.AP_DEVICE_SUBSCRIPTION_ID = AP_DEVICE_SUBSCRIPTION.ID "
                    + "WHERE AP_APP_SUB_OP_MAPPING.OPERATION_ID = ? AND "
                    + "AP_DEVICE_SUBSCRIPTION.DM_DEVICE_ID = ? AND "
                    + "AP_APP_SUB_OP_MAPPING.TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, operationId);
                stmt.setInt(2, deviceId);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        deviceSubIds.add(rs.getInt("AP_DEVICE_SUBSCRIPTION_ID"));
                    }
                }
                return deviceSubIds;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get app device subscription ids for given "
                    + "operation.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to get app device subscription ids for given operation.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean updateDeviceSubStatus(int deviceId, List<Integer> deviceSubIds, String status, int tenantId)
            throws ApplicationManagementDAOException {
        try {
            if (deviceSubIds.isEmpty()) {
                return false;
            }
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "UPDATE AP_DEVICE_SUBSCRIPTION SET STATUS = ? "
                            + "WHERE ID IN (",
                    ") AND DM_DEVICE_ID = ? AND TENANT_ID = ?");
            deviceSubIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(index++, status);
                for (Integer deviceSubId : deviceSubIds) {
                    ps.setObject(index++, deviceSubId);
                }
                ps.setInt(index++, deviceId);
                ps.setInt(index, tenantId);
                return ps.executeUpdate() != 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update the subscription status of the "
                    + "device subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to update the subscription status of the device "
                    + "subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean createScheduledSubscription(ScheduledSubscriptionDTO subscriptionDTO)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                     + "AP_SCHEDULED_SUBSCRIPTION ("
                     + "TASK_NAME, "
                     + "APPLICATION_UUID, "
                     + "SUBSCRIBER_LIST, "
                     + "STATUS, "
                     + "SCHEDULED_AT, "
                     + "SCHEDULED_TIMESTAMP,"
                     + "SCHEDULED_BY,"
                     + "DELETED) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                stmt.setString(1, subscriptionDTO.getTaskName());
                stmt.setString(2, subscriptionDTO.getApplicationUUID());
                stmt.setString(3, subscriptionDTO.getSubscribersString());
                stmt.setString(4, ExecutionStatus.PENDING.toString());
                stmt.setLong(5, subscriptionDTO.getScheduledAt());
                stmt.setTimestamp(6, new Timestamp(calendar.getTime().getTime()));
                stmt.setString(7, subscriptionDTO.getScheduledBy());
                stmt.setBoolean(8, false);
                return stmt.executeUpdate() > 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to insert the subscription status of the "
                    + "scheduled subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to insert the " + "subscription status of the scheduled  "
                    + "subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean updateScheduledSubscription(int id, long scheduledAt, String scheduledBy)
            throws ApplicationManagementDAOException {
        String sql = "UPDATE AP_SCHEDULED_SUBSCRIPTION "
                     + "SET "
                     + "SCHEDULED_AT = ?, "
                     + "SCHEDULED_BY = ?, "
                     + "SCHEDULED_TIMESTAMP = ? "
                     + "WHERE ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                stmt.setLong(1, scheduledAt);
                stmt.setString(2, scheduledBy);
                stmt.setTimestamp(3, new Timestamp(calendar.getTime().getTime()));
                stmt.setInt(4, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (DBConnectionException e) {
            String msg =
                    "Error occurred while obtaining the DB connection to update the existing entry of the scheduled "
                            + "subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to update the existing entry of the scheduled subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean deleteScheduledSubscription(List<Integer> subscriptionIdList) throws ApplicationManagementDAOException {
        String sql = "UPDATE AP_SCHEDULED_SUBSCRIPTION "
                     + "SET DELETED = ? "
                     + "WHERE ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer id: subscriptionIdList) {
                    stmt.setBoolean(1, true);
                    stmt.setInt(2, id);
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                return Arrays.stream(results).allMatch(r -> r > 0);
            }
        } catch (DBConnectionException e) {
            String msg =
                    "Error occurred while obtaining the DB connection to delete the existing entry of the scheduled "
                            + "subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to delete the existing entry of the scheduled subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean updateScheduledSubscriptionStatus(int id, ExecutionStatus status)
            throws ApplicationManagementDAOException {
        String sql = "UPDATE AP_SCHEDULED_SUBSCRIPTION "
                     + "SET STATUS = ? "
                     + "WHERE ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.toString());
                stmt.setInt(2, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update the status of the scheduled "
                    + "subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to update the status of the scheduled subscription.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedUsers(int offsetValue, int limitValue, int appReleaseId,
                                              int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed users for " +
                      "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedUsers = new ArrayList<>();
            String sql = "SELECT "
                         + "US.USER_NAME AS USER_NAME "
                         + "FROM AP_USER_SUBSCRIPTION US "
                         + "WHERE "
                         + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? LIMIT ? OFFSET ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);
                stmt.setInt(3, limitValue);
                stmt.setInt(4, offsetValue);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("USER_NAME"));
                    }
                }
                return subscribedUsers;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                         "subscribed users for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed users for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getSubscribedUserCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed users for " +
                      "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT "
                         + "COUNT(US.USER_NAME) AS USER_NAME "
                         + "FROM AP_USER_SUBSCRIPTION US "
                         + "WHERE "
                         + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("USER_NAME");
                    }
                }
                return 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                         "subscribed users count for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed users for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public String getUUID(int id, String packageName) throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT " +
                    "AP_APP_RELEASE.UUID " +
                    "FROM  AP_DEVICE_SUBSCRIPTION " +
                    "JOIN AP_APP_RELEASE " +
                    "ON " +
                    "AP_DEVICE_SUBSCRIPTION.AP_APP_RELEASE_ID = AP_APP_RELEASE.AP_APP_ID " +
                    "WHERE PACKAGE_NAME = ? " +
                    "AND DM_DEVICE_ID = ? " +
                    "AND UNSUBSCRIBED = 'FALSE' " +
                    "AND STATUS = 'COMPLETED';";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, packageName);
                stmt.setInt(2, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("UUID");
                    }
                    return null;
                }
            }
        } catch (DBConnectionException e) {
            String msg =
                    "Error occurred while obtaining the DB connection to check an application is subscribed ";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to check an application is subscribed";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ScheduledSubscriptionDTO> getScheduledSubscriptionByStatus(ExecutionStatus status, boolean deleted)
            throws ApplicationManagementDAOException {
        String sql = "SELECT "
                     + "ID, "
                     + "TASK_NAME, "
                     + "APPLICATION_UUID, "
                     + "SUBSCRIBER_LIST, "
                     + "STATUS, "
                     + "SCHEDULED_AT, "
                     + "SCHEDULED_BY, "
                     + "DELETED "
                     + "FROM AP_SCHEDULED_SUBSCRIPTION "
                     + "WHERE STATUS = ? AND DELETED = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, status.toString());
                stmt.setBoolean(2, deleted);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadScheduledSubscriptions(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to retrieve" + status.toString()
                    + " subscriptions";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve" + status.toString() + " subscriptions";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<ScheduledSubscriptionDTO> getNonExecutedSubscriptions() throws ApplicationManagementDAOException {
        String sql = "SELECT "
                     + "ID, "
                     + "TASK_NAME, "
                     + "APPLICATION_UUID, "
                     + "SUBSCRIBER_LIST, "
                     + "STATUS, "
                     + "SCHEDULED_AT, "
                     + "SCHEDULED_BY, "
                     + "DELETED "
                     + "FROM AP_SCHEDULED_SUBSCRIPTION "
                     + "WHERE STATUS = ? AND DELETED = ? AND SCHEDULED_AT < ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, ExecutionStatus.PENDING.toString());
                stmt.setBoolean(2, false);
                stmt.setLong(3, Calendar.getInstance().getTime().getTime() / 1000);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadScheduledSubscriptions(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to retrieve missed subscriptions";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve missed subscriptions.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedRoles(int offsetValue, int limitValue, int appReleaseId,
                                              int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed roles for " +
                      "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedRoles = new ArrayList<>();
            String sql = "SELECT "
                         + "RS.ROLE_NAME AS ROLE "
                         + "FROM AP_ROLE_SUBSCRIPTION RS "
                         + "WHERE "
                         + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                ps.setInt(3, limitValue);
                ps.setInt(4, offsetValue);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedRoles.add(rs.getString("ROLE"));
                    }
                }
                return subscribedRoles;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                         "subscribed roles for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed roles for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getSubscribedRoleCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed roles for " +
                      "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT "
                         + "COUNT(RS.ROLE_NAME) AS ROLE_NAME "
                         + "FROM AP_ROLE_SUBSCRIPTION RS "
                         + "WHERE "
                         + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("ROLE_NAME");
                    }
                }
                return 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                         "subscribed roles count for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed roles for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public ScheduledSubscriptionDTO getPendingScheduledSubscriptionByTaskName(String taskName)
            throws ApplicationManagementDAOException {
        String sql = "SELECT "
                     + "ID, "
                     + "TASK_NAME, "
                     + "APPLICATION_UUID, "
                     + "SUBSCRIBER_LIST, "
                     + "STATUS, "
                     + "SCHEDULED_AT, "
                     + "SCHEDULED_BY, "
                     + "DELETED "
                     + "FROM AP_SCHEDULED_SUBSCRIPTION "
                     + "WHERE TASK_NAME = ? AND STATUS = ? AND DELETED = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, taskName);
                stmt.setString(2, ExecutionStatus.PENDING.toString());
                stmt.setBoolean(3, false);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadScheduledSubscription(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg =
                    "Error occurred while obtaining the DB connection to retrieve pending subscriptions of " + taskName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve pending subscriptions of " + taskName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (UnexpectedServerErrorException e) {
            String msg = "More than one pending subscriptions exist for " + taskName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getAppSubscribedGroups(int offsetValue, int limitValue, int appReleaseId,
                                               int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed groups for " +
                      "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<String> subscribedGroups = new ArrayList<>();
            String sql = "SELECT "
                         + "GS.GROUP_NAME AS APP_GROUPS "
                         + "FROM AP_GROUP_SUBSCRIPTION GS "
                         + "WHERE "
                         + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                ps.setInt(3, limitValue);
                ps.setInt(4, offsetValue);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedGroups.add(rs.getString("APP_GROUPS"));
                    }
                }
                return subscribedGroups;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                         "subscribed groups for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed groups for given " +
                         "app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getSubscribedGroupCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed groups for " +
                      "given app release id.");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT "
                         + "COUNT(GS.GROUP_NAME) AS APP_GROUPS_COUNT "
                         + "FROM AP_GROUP_SUBSCRIPTION GS "
                         + "WHERE "
                         + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("APP_GROUPS_COUNT");
                    }
                }
                return 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                         "subscribed groups count for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed groups for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getAppSubscribedDevicesForGroups(int appReleaseId, String subType, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed devices for " +
                    "given app release id.");
        }
        // retrieve all device list by action triggered type and app release id
        try {
            Connection conn = this.getDBConnection();
            List<Integer> subscribedGroupDevices = new ArrayList<>();
            String sql = "SELECT "
                    + "AP_DEVICE_SUBSCRIPTION.DM_DEVICE_ID AS DEVICES "
                    + "FROM AP_DEVICE_SUBSCRIPTION "
                    + "WHERE "
                    + "AP_APP_RELEASE_ID = ? AND ACTION_TRIGGERED_FROM=? AND "
                    + "UNSUBSCRIBED=FALSE AND TENANT_ID = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setString(2, subType.toLowerCase());;
                ps.setInt(3, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedGroupDevices.add(rs.getInt("DEVICES"));
                    }
                }
                return subscribedGroupDevices;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already " +
                    "subscribed groups for given app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed devices for given " +
                    "app release id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public Map<Integer,String> getCurrentInstalledAppVersion(int appId, List<Integer> deviceIdList, String installedVersion ) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get current installed version of the app for " +
                    "given app release id.");
        }
        try {

            Map<Integer,String> installedVersionsMap = new HashMap<>();
            Connection conn = this.getDBConnection();
            int index = 1;
            boolean isInstalledVersionAvailable = false;
            StringJoiner joiner = new StringJoiner(",",
                    " SELECT DM_DEVICE_ID AS DEVICE,VERSION FROM " +
                            " (SELECT AP_APP.ID, VERSION FROM AP_APP_RELEASE AP_APP " +
                            "   WHERE ID IN (SELECT ID FROM AP_APP_RELEASE " +
                            "       WHERE AP_APP_ID = ?) " +
                            " ) AP_APP_V" +
                            " INNER JOIN " +
                            " (SELECT AP_APP_RELEASE_ID, DM_DEVICE_ID FROM AP_DEVICE_SUBSCRIPTION AP_DEV_1 " +
                            "   INNER JOIN (" +
                            "       SELECT  MAX(ID) AS ID FROM AP_DEVICE_SUBSCRIPTION " +
                            "           WHERE STATUS = 'COMPLETED' AND DM_DEVICE_ID IN (",
                      ") GROUP BY DM_DEVICE_ID " +
                            ") AP_DEV_2 " +
                            "ON AP_DEV_2.ID = AP_DEV_1.ID ) AP_APP_R " +
                            "ON AP_APP_R.AP_APP_RELEASE_ID = AP_APP_V.ID");
            deviceIdList.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            if(installedVersion != null && !installedVersion.isEmpty()){
                query += " WHERE VERSION = ? ";
                isInstalledVersionAvailable = true;
            }
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(index++, appId);
                for (int deviceId : deviceIdList) {
                    ps.setInt(index++, deviceId);
                }
                if(isInstalledVersionAvailable){
                    ps.setString(index++, installedVersion);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        installedVersionsMap.put(rs.getInt("DEVICE"),rs.getString("VERSION"));
                    }
                }
                return installedVersionsMap;
            }

        }catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get current installed version of the app for " +
                    "given app id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting current installed version of the app for given " +
                    "app id.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    public Activity getOperationAppDetails(int operationId, int tenantId) throws ApplicationManagementDAOException {
        try {
        String sql = "SELECT "
                + "AP.NAME, "
                + "AP.TYPE, "
                + "AR.PACKAGE_NAME, "
                + "AR.VERSION, "
                + "DS.SUBSCRIBED_BY, "
                + "DS.ACTION_TRIGGERED_FROM "
                + "FROM AP_APP_SUB_OP_MAPPING SOP "
                + "JOIN AP_DEVICE_SUBSCRIPTION DS ON SOP.AP_DEVICE_SUBSCRIPTION_ID = DS.ID "
                + "JOIN AP_APP_RELEASE AR ON DS.AP_APP_RELEASE_ID = AR.ID "
                + "JOIN AP_APP AP ON AP.ID = AR.AP_APP_ID "
                + "WHERE SOP.OPERATION_ID = ? AND SOP.TENANT_ID = ? "
                + "LIMIT 1";

            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, operationId);
                stmt.setInt(2,tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return DAOUtil.loadOperationActivity(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg =
                    "Error occurred while getting the app details from the database related to operation " + operationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to retrieve app details of operation" + operationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }  catch (UnexpectedServerErrorException e) {
            String msg = "More than one app for operation " + operationId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteOperationMappingByTenant(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete operation mapping of the tenant of id: " + tenantId);
        }
        String sql = "DELETE FROM AP_APP_SUB_OP_MAPPING " +
                "WHERE TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete operation mapping of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete operation mapping of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteRoleSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete role subscription of the tenant of id: " + tenantId);
        }
        String sql = "DELETE FROM AP_ROLE_SUBSCRIPTION " +
                "WHERE TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete role subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete role subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteUserSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete user subscription of the tenant of id: " + tenantId);
        }
        String sql = "DELETE FROM AP_USER_SUBSCRIPTION " +
                "WHERE TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete user subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete user subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }

    }

    @Override
    public void deleteGroupSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete user subscription of the tenant of id: " + tenantId);
        }
        String sql = "DELETE FROM AP_GROUP_SUBSCRIPTION " +
                "WHERE TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete group subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete group subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteScheduledSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete scheduled subscription of the tenant of id: " + tenantId);
        }
        String sql = "DELETE FROM AP_SCHEDULED_SUBSCRIPTION " +
                "WHERE APPLICATION_UUID IN " +
                "(SELECT UUID FROM AP_APP_RELEASE WHERE TENANT_ID = ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete scheduled subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete scheduled subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteDeviceSubscriptionByTenant(int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete device subscription of the tenant of id: " + tenantId);
        }
        String sql = "DELETE FROM AP_DEVICE_SUBSCRIPTION " +
                "WHERE TENANT_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete device subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete device subscription of tenant of id "
                    + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<SubscriptionEntity> getGroupsSubscriptionDetailsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId, int offset, int limit)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get groups related to the given AppReleaseID.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();

            String subscriptionStatusTime = unsubscribe ? "GS.UNSUBSCRIBED_TIMESTAMP" : "GS.SUBSCRIBED_TIMESTAMP";
            String sql = "SELECT GS.GROUP_NAME, " +
                    "GS.SUBSCRIBED_BY, " +
                    "GS.SUBSCRIBED_TIMESTAMP, " +
                    "GS.UNSUBSCRIBED, " +
                    "GS.UNSUBSCRIBED_BY, " +
                    "GS.UNSUBSCRIBED_TIMESTAMP, " +
                    "GS.AP_APP_RELEASE_ID " +
                    "FROM AP_GROUP_SUBSCRIPTION GS " +
                    "WHERE GS.AP_APP_RELEASE_ID = ? " +
                    "AND GS.UNSUBSCRIBED = ? " +
                    "AND GS.TENANT_ID = ? " +
                    "ORDER BY " + subscriptionStatusTime + " DESC ";

            // Append limit and offset only if limit is not -1
            if (limit != -1) {
                sql = sql + " LIMIT ? OFFSET ?";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setBoolean(2, unsubscribe);
                ps.setInt(3, tenantId);

                // Set limit and offset parameters only if limit is not -1
                if (limit != -1) {
                    ps.setInt(4, limit);
                    ps.setInt(5, offset);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    SubscriptionEntity subscriptionEntity;
                    while (rs.next()) {
                        subscriptionEntity = new SubscriptionEntity();
                        subscriptionEntity.setIdentity(rs.getString("GROUP_NAME"));
                        subscriptionEntity.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
                        subscriptionEntity.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_TIMESTAMP"));
                        subscriptionEntity.setUnsubscribed(rs.getBoolean("UNSUBSCRIBED"));
                        subscriptionEntity.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
                        subscriptionEntity.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_TIMESTAMP"));
                        subscriptionEntity.setApplicationReleaseId(rs.getInt("AP_APP_RELEASE_ID"));

                        subscriptionEntities.add(subscriptionEntity);
                    }
                }
                return subscriptionEntities;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get groups for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting groups for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<SubscriptionEntity> getUserSubscriptionsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId,
                                                                     int offset, int limit) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get user subscriptions related to the given UUID.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();

            String subscriptionStatusTime = unsubscribe ? "US.UNSUBSCRIBED_TIMESTAMP" : "US.SUBSCRIBED_TIMESTAMP";
            String sql = "SELECT US.USER_NAME, " +
                    "US.SUBSCRIBED_BY, " +
                    "US.SUBSCRIBED_TIMESTAMP, " +
                    "US.UNSUBSCRIBED, " +
                    "US.UNSUBSCRIBED_BY, " +
                    "US.UNSUBSCRIBED_TIMESTAMP, " +
                    "US.AP_APP_RELEASE_ID " +
                    "FROM AP_USER_SUBSCRIPTION US " +
                    "WHERE US.AP_APP_RELEASE_ID = ? " +
                    "AND US.UNSUBSCRIBED = ? " +
                    "AND US.TENANT_ID = ? " +
                    "ORDER BY " + subscriptionStatusTime + " DESC " +
                    "LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setBoolean(2, unsubscribe);
                ps.setInt(3, tenantId);
                ps.setInt(4, limit);
                ps.setInt(5, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    SubscriptionEntity subscriptionEntity;
                    while (rs.next()) {
                        subscriptionEntity = new SubscriptionEntity();
                        subscriptionEntity.setIdentity(rs.getString("USER_NAME"));
                        subscriptionEntity.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
                        subscriptionEntity.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_TIMESTAMP"));
                        subscriptionEntity.setUnsubscribed(rs.getBoolean("UNSUBSCRIBED"));
                        subscriptionEntity.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
                        subscriptionEntity.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_TIMESTAMP"));
                        subscriptionEntity.setApplicationReleaseId(rs.getInt("AP_APP_RELEASE_ID"));

                        subscriptionEntities.add(subscriptionEntity);
                    }
                }
                return subscriptionEntities;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get user subscriptions for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting user subscriptions for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<SubscriptionEntity> getRoleSubscriptionsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId, int offset,
                                                                       int limit) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get role subscriptions related to the given AppReleaseID.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<SubscriptionEntity> subscriptionEntities = new ArrayList<>();

            String subscriptionStatusTime = unsubscribe ? "ARS.UNSUBSCRIBED_TIMESTAMP" : "ARS.SUBSCRIBED_TIMESTAMP";
            String sql = "SELECT ARS.ROLE_NAME, " +
                    "ARS.SUBSCRIBED_BY, " +
                    "ARS.SUBSCRIBED_TIMESTAMP, " +
                    "ARS.UNSUBSCRIBED, " +
                    "ARS.UNSUBSCRIBED_BY, " +
                    "ARS.UNSUBSCRIBED_TIMESTAMP, " +
                    "ARS.AP_APP_RELEASE_ID " +
                    "FROM AP_ROLE_SUBSCRIPTION ARS " +
                    "WHERE ARS.AP_APP_RELEASE_ID = ? " +
                    "AND ARS.UNSUBSCRIBED = ? " +
                    "AND ARS.TENANT_ID = ? " +
                    "ORDER BY " + subscriptionStatusTime + " DESC " +
                    "LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setBoolean(2, unsubscribe);
                ps.setInt(3, tenantId);
                ps.setInt(4, limit);
                ps.setInt(5, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    SubscriptionEntity subscriptionEntity;
                    while (rs.next()) {
                        subscriptionEntity = new SubscriptionEntity();
                        subscriptionEntity.setIdentity(rs.getString("ROLE_NAME"));
                        subscriptionEntity.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
                        subscriptionEntity.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_TIMESTAMP"));
                        subscriptionEntity.setUnsubscribed(rs.getBoolean("UNSUBSCRIBED"));
                        subscriptionEntity.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
                        subscriptionEntity.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_TIMESTAMP"));
                        subscriptionEntity.setApplicationReleaseId(rs.getInt("AP_APP_RELEASE_ID"));

                        subscriptionEntities.add(subscriptionEntity);
                    }
                }
                return subscriptionEntities;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get role subscriptions for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting role subscriptions for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceSubscriptionDTO> getDeviceSubscriptionsByAppReleaseID(int appReleaseId, boolean unsubscribe, int tenantId, int offset, int limit)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get device subscriptions related to the given AppReleaseID.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<DeviceSubscriptionDTO>  deviceSubscriptions = new ArrayList<>();

            String subscriptionStatusTime = unsubscribe ? "DS.UNSUBSCRIBED_TIMESTAMP" : "DS.SUBSCRIBED_TIMESTAMP";
            String sql = "SELECT DS.DM_DEVICE_ID, " +
                    "DS.SUBSCRIBED_BY, " +
                    "DS.SUBSCRIBED_TIMESTAMP, " +
                    "DS.STATUS, " +
                    "DS.UNSUBSCRIBED, " +
                    "DS.UNSUBSCRIBED_BY, " +
                    "DS.UNSUBSCRIBED_TIMESTAMP, " +
                    "DS.AP_APP_RELEASE_ID " +
                    "FROM AP_DEVICE_SUBSCRIPTION DS " +
                    "WHERE DS.AP_APP_RELEASE_ID = ? " +
                    "AND DS.UNSUBSCRIBED = ? " +
                    "AND DS.TENANT_ID = ?  " +
                    "AND DS.ACTION_TRIGGERED_FROM = 'DEVICE' " +
                    "ORDER BY " + subscriptionStatusTime + " DESC " +
                    "LIMIT ? OFFSET ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setBoolean(2, unsubscribe);
                ps.setInt(3, tenantId);
                ps.setInt(4, limit);
                ps.setInt(5, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    DeviceSubscriptionDTO deviceSubscription;
                    while (rs.next()) {
                        deviceSubscription = new DeviceSubscriptionDTO();
                        deviceSubscription.setDeviceId(rs.getInt("DM_DEVICE_ID"));
                        deviceSubscription.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
                        deviceSubscription.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_TIMESTAMP"));
                        deviceSubscription.setStatus(rs.getString("STATUS"));
                        deviceSubscription.setUnsubscribed(rs.getBoolean("UNSUBSCRIBED"));
                        deviceSubscription.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
                        deviceSubscription.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_TIMESTAMP"));
                        deviceSubscription.setAppReleaseId(rs.getInt("AP_APP_RELEASE_ID"));

                        deviceSubscriptions.add(deviceSubscription);
                    }
                }
                return deviceSubscriptions;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get device subscriptions for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting device subscriptions for the given UUID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceOperationDTO> getSubscriptionOperationsByAppReleaseIDAndDeviceID(
            int appReleaseId, int deviceId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get device subscriptions related to the given AppReleaseID and DeviceID.");
        }
        try {
            Connection conn = this.getDBConnection();
            List<DeviceOperationDTO> deviceSubscriptions = new ArrayList<>();
            String sql = "SELECT " +
                    "  ads.DM_DEVICE_ID, " +
                    "  aasom.OPERATION_ID, " +
                    "  ads.STATUS, " +
                    "  ads.ACTION_TRIGGERED_FROM, " +
                    "  ads.SUBSCRIBED_TIMESTAMP AS ACTION_TRIGGERED_AT, " +
                    "  ads.AP_APP_RELEASE_ID " +
                    "FROM AP_APP_SUB_OP_MAPPING aasom " +
                    "JOIN AP_DEVICE_SUBSCRIPTION ads " +
                    "ON aasom.AP_DEVICE_SUBSCRIPTION_ID = ads.ID " +
                    "WHERE ads.AP_APP_RELEASE_ID = ? " +
                    "AND ads.DM_DEVICE_ID = ? " +
                    "AND ads.TENANT_ID = ? " +
                    "ORDER BY aasom.OPERATION_ID DESC " +
                    "LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, deviceId);
                ps.setInt(3, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    DeviceOperationDTO deviceSubscription;
                    while (rs.next()) {
                        deviceSubscription = new DeviceOperationDTO();
                        deviceSubscription.setDeviceId(rs.getInt("DM_DEVICE_ID"));
                        deviceSubscription.setStatus(rs.getString("STATUS"));
                        deviceSubscription.setOperationId(rs.getInt("OPERATION_ID"));
                        deviceSubscription.setActionTriggeredFrom(rs.getString("ACTION_TRIGGERED_FROM"));
                        deviceSubscription.setActionTriggeredAt(rs.getTimestamp("ACTION_TRIGGERED_AT"));
                        deviceSubscription.setAppReleaseId(rs.getInt("AP_APP_RELEASE_ID"));

                        deviceSubscriptions.add(deviceSubscription);
                    }
                }
            }
            return deviceSubscriptions;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get device subscriptions for the given AppReleaseID and DeviceID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting device subscriptions for the given AppReleaseID and DeviceID.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    // passed the required list for the action status
    @Override
    public List<DeviceSubscriptionDTO> getSubscriptionDetailsByDeviceIds(int appReleaseId, boolean unsubscribe, int tenantId,
                                                                         List<Integer> deviceIds, List<String> actionStatus, String actionType,
                                                                         String actionTriggeredBy, int limit, int offset) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions for the application release id " + appReleaseId
                    + " and device ids " + deviceIds + " from the database");
        }

        if (deviceIds == null || deviceIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            Connection conn = this.getDBConnection();
            String subscriptionStatusTime = unsubscribe ? "DS.UNSUBSCRIBED_TIMESTAMP" : "DS.SUBSCRIBED_TIMESTAMP";
            StringBuilder sql = new StringBuilder("SELECT "
                    + "DS.ID AS ID, "
                    + "DS.SUBSCRIBED_BY AS SUBSCRIBED_BY, "
                    + "DS.SUBSCRIBED_TIMESTAMP AS SUBSCRIBED_AT, "
                    + "DS.UNSUBSCRIBED AS IS_UNSUBSCRIBED, "
                    + "DS.UNSUBSCRIBED_BY AS UNSUBSCRIBED_BY, "
                    + "DS.UNSUBSCRIBED_TIMESTAMP AS UNSUBSCRIBED_AT, "
                    + "DS.ACTION_TRIGGERED_FROM AS ACTION_TRIGGERED_FROM, "
                    + "DS.STATUS AS STATUS, "
                    + "DS.DM_DEVICE_ID AS DEVICE_ID "
                    + "FROM AP_DEVICE_SUBSCRIPTION DS "
                    + "WHERE DS.AP_APP_RELEASE_ID = ? "
                    + "AND DS.UNSUBSCRIBED = ? "
                    + "AND DS.TENANT_ID = ? "
                    + "AND DS.DM_DEVICE_ID IN ("
                    + deviceIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") ");

            if (actionStatus != null && !actionStatus.isEmpty()) {
                sql.append(" AND DS.STATUS IN (").
                        append(actionStatus.stream().map(status -> "?").collect(Collectors.joining(","))).append(") ");
            }
            if (actionType != null && !actionType.isEmpty()) {
                sql.append(" AND DS.ACTION_TRIGGERED_FROM = ? ");
            }
            if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
                sql.append(" AND DS.SUBSCRIBED_BY LIKE ? ");
            }

            sql.append("ORDER BY ").append(subscriptionStatusTime).
                    append(" DESC ");

            if (offset >= 0 && limit >= 0) {
                sql.append("LIMIT ? OFFSET ?");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                ps.setInt(paramIdx++, appReleaseId);
                ps.setBoolean(paramIdx++, unsubscribe);
                ps.setInt(paramIdx++, tenantId);
                for (Integer deviceId : deviceIds) {
                    ps.setInt(paramIdx++, deviceId);
                }

                if (actionStatus != null && !actionStatus.isEmpty()) {
                    for (String status : actionStatus) {
                        ps.setString(paramIdx++, status);
                    }
                }

                if (actionType != null && !actionType.isEmpty()) {
                    ps.setString(paramIdx++, actionType);
                }

                if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
                    ps.setString(paramIdx++, "%" + actionTriggeredBy + "%");
                }

                if (offset >= 0 && limit >= 0) {
                    ps.setInt(paramIdx++, limit);
                    ps.setInt(paramIdx, offset);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved device subscriptions for application release id "
                                + appReleaseId + " and device ids " + deviceIds);
                    }
                    List<DeviceSubscriptionDTO> subscriptions = new ArrayList<>();
                    while (rs.next()) {
                        DeviceSubscriptionDTO subscription = new DeviceSubscriptionDTO();
                        subscription.setId(rs.getInt("ID"));
                        subscription.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
                        subscription.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_AT"));
                        subscription.setUnsubscribed(rs.getBoolean("IS_UNSUBSCRIBED"));
                        subscription.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
                        subscription.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_AT"));
                        subscription.setActionTriggeredFrom(rs.getString("ACTION_TRIGGERED_FROM"));
                        subscription.setStatus(rs.getString("STATUS"));
                        subscription.setDeviceId(rs.getInt("DEVICE_ID"));
                        subscriptions.add(subscription);
                    }
                    return subscriptions;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get device subscription data for application ID: " + appReleaseId
                        + " and device ids: " + deviceIds + ".";
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device subscriptions for "
                    + "application Id: " + appReleaseId + " and device ids: " + deviceIds + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }

    }

    @Override
    public int getDeviceSubscriptionCount(int appReleaseId, boolean unsubscribe, int tenantId,
                                          List<Integer> deviceIds, List<String> actionStatus, String actionType,
                                          String actionTriggeredBy) throws ApplicationManagementDAOException {
        int deviceCount = 0;

        if (deviceIds == null || deviceIds.isEmpty()) return deviceCount;

        try {
            Connection conn = this.getDBConnection();
            StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT DS.DM_DEVICE_ID) AS COUNT "
                    + "FROM AP_DEVICE_SUBSCRIPTION DS "
                    + "WHERE DS.AP_APP_RELEASE_ID = ? "
                    + "AND DS.UNSUBSCRIBED = ? "
                    + "AND DS.TENANT_ID = ? "
                    + "AND DS.DM_DEVICE_ID IN ("
                    + deviceIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") ");

            if (actionStatus != null && !actionStatus.isEmpty()) {
                sql.append(" AND DS.STATUS IN (").
                        append(actionStatus.stream().map(status -> "?").collect(Collectors.joining(","))).append(") ");
            }

            if (actionType != null && !actionType.isEmpty()) {
                sql.append(" AND DS.ACTION_TRIGGERED_FROM = ? ");
            }
            if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
                sql.append(" AND DS.SUBSCRIBED_BY LIKE ?");
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                ps.setInt(paramIdx++, appReleaseId);
                ps.setBoolean(paramIdx++, unsubscribe);
                ps.setInt(paramIdx++, tenantId);
                for (Integer deviceId : deviceIds) {
                    ps.setInt(paramIdx++, deviceId);
                }

                if (actionStatus != null && !actionStatus.isEmpty()) {
                    for (String status : actionStatus) {
                        ps.setString(paramIdx++, status);
                    }
                }

                if (actionType != null && !actionType.isEmpty()) {
                    ps.setString(paramIdx++, actionType);
                }

                if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
                    ps.setString(paramIdx, "%" + actionTriggeredBy + "%");
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved device subscriptions for application release id "
                                + appReleaseId + " and device ids " + deviceIds);
                    }
                    if (rs.next()) {
                        deviceCount = rs.getInt("COUNT");
                    }
                    return deviceCount;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get device subscription data for application ID: " + appReleaseId
                        + " and device ids: " + deviceIds + ".";
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device subscriptions for "
                    + "application Id: " + appReleaseId + " and device ids: " + deviceIds + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

//    @Override
//    public List<DeviceSubscriptionDTO> getSubscriptionDetailsByDeviceIds(int appReleaseId, boolean unsubscribe, int tenantId,
//                                                                         List<Integer> deviceIds, String actionStatus, String actionType,
//                                                                         String actionTriggeredBy, String tabActionStatus) throws ApplicationManagementDAOException {
//        if (log.isDebugEnabled()) {
//            log.debug("Getting device subscriptions for the application release id " + appReleaseId
//                    + " and device ids " + deviceIds + " from the database");
//        }
//        try {
//            Connection conn = this.getDBConnection();
//            String subscriptionStatusTime = unsubscribe ? "DS.UNSUBSCRIBED_TIMESTAMP" : "DS.SUBSCRIBED_TIMESTAMP";
//            StringBuilder sql = new StringBuilder("SELECT "
//                    + "DS.ID AS ID, "
//                    + "DS.SUBSCRIBED_BY AS SUBSCRIBED_BY, "
//                    + "DS.SUBSCRIBED_TIMESTAMP AS SUBSCRIBED_AT, "
//                    + "DS.UNSUBSCRIBED AS IS_UNSUBSCRIBED, "
//                    + "DS.UNSUBSCRIBED_BY AS UNSUBSCRIBED_BY, "
//                    + "DS.UNSUBSCRIBED_TIMESTAMP AS UNSUBSCRIBED_AT, "
//                    + "DS.ACTION_TRIGGERED_FROM AS ACTION_TRIGGERED_FROM, "
//                    + "DS.STATUS AS STATUS, "
//                    + "DS.DM_DEVICE_ID AS DEVICE_ID "
//                    + "FROM AP_DEVICE_SUBSCRIPTION DS "
//                    + "WHERE DS.AP_APP_RELEASE_ID = ? AND DS.UNSUBSCRIBED = ? AND DS.TENANT_ID = ? AND DS.DM_DEVICE_ID IN (" +
//                    deviceIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") ");
//
//            if (actionStatus != null && !actionStatus.isEmpty()) {
//                sql.append(" AND DS.STATUS = ? ");
//            }
//            if (actionType != null && !actionType.isEmpty()) {
//                sql.append(" AND DS.ACTION_TRIGGERED_FROM = ? ");
//            }
//            if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
//                sql.append(" AND DS.SUBSCRIBED_BY LIKE ? ");
//            }
//
//            sql.append("ORDER BY ").append(subscriptionStatusTime).append(" DESC");
//
//            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
//                int paramIdx = 1;
//                ps.setInt(paramIdx++, appReleaseId);
//                ps.setBoolean(paramIdx++, unsubscribe);
//                ps.setInt(paramIdx++, tenantId);
//                for (int i = 0; i < deviceIds.size(); i++) {
//                    ps.setInt(paramIdx++, deviceIds.get(i));
//                }
//
//                if (actionStatus != null && !actionStatus.isEmpty()) {
//                    ps.setString(paramIdx++, actionStatus);
//                }
//                if (actionType != null && !actionType.isEmpty()) {
//                    ps.setString(paramIdx++, actionType);
//                }
//                if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
//                    ps.setString(paramIdx++, "%" + actionTriggeredBy + "%");
//                }
//
//                try (ResultSet rs = ps.executeQuery()) {
//                    if (log.isDebugEnabled()) {
//                        log.debug("Successfully retrieved device subscriptions for application release id "
//                                + appReleaseId + " and device ids " + deviceIds);
//                    }
//                    List<DeviceSubscriptionDTO> subscriptions = new ArrayList<>();
//                    while (rs.next()) {
//                        DeviceSubscriptionDTO subscription = new DeviceSubscriptionDTO();
//                        subscription.setId(rs.getInt("ID"));
//                        subscription.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
//                        subscription.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_AT"));
//                        subscription.setUnsubscribed(rs.getBoolean("IS_UNSUBSCRIBED"));
//                        subscription.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
//                        subscription.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_AT"));
//                        subscription.setActionTriggeredFrom(rs.getString("ACTION_TRIGGERED_FROM"));
//                        subscription.setStatus(rs.getString("STATUS"));
//                        subscription.setDeviceId(rs.getInt("DEVICE_ID"));
//                        subscriptions.add(subscription);
//                    }
//                    return subscriptions;
//                }
//            } catch (SQLException e) {
//                String msg = "Error occurred while running SQL to get device subscription data for application ID: " + appReleaseId
//                        + " and device ids: " + deviceIds + ".";
//                log.error(msg, e);
//                throw new ApplicationManagementDAOException(msg, e);
//            }
//        } catch (DBConnectionException e) {
//            String msg = "Error occurred while obtaining the DB connection for getting device subscriptions for "
//                    + "application Id: " + appReleaseId + " and device ids: " + deviceIds + ".";
//            log.error(msg, e);
//            throw new ApplicationManagementDAOException(msg, e);
//        }
//
//    }

    @Override
    public List<DeviceSubscriptionDTO> getAllSubscriptionsDetails(int appReleaseId, boolean unsubscribe, int tenantId,
                                                                  List<String> actionStatus, String actionType, String actionTriggeredBy,
                                                                  int offset, int limit) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions for the application release id " + appReleaseId
                    + " from the database");
        }

        String subscriptionStatusTime = unsubscribe ? "DS.UNSUBSCRIBED_TIMESTAMP" : "DS.SUBSCRIBED_TIMESTAMP";
        String actionTriggeredColumn = unsubscribe ? "DS.UNSUBSCRIBED_BY" : "DS.SUBSCRIBED_BY";
        StringBuilder sql = new StringBuilder("SELECT "
                + "DS.ID AS ID, "
                + "DS.SUBSCRIBED_BY AS SUBSCRIBED_BY, "
                + "DS.SUBSCRIBED_TIMESTAMP AS SUBSCRIBED_AT, "
                + "DS.UNSUBSCRIBED AS IS_UNSUBSCRIBED, "
                + "DS.UNSUBSCRIBED_BY AS UNSUBSCRIBED_BY, "
                + "DS.UNSUBSCRIBED_TIMESTAMP AS UNSUBSCRIBED_AT, "
                + "DS.ACTION_TRIGGERED_FROM AS ACTION_TRIGGERED_FROM, "
                + "DS.STATUS AS STATUS, "
                + "DS.DM_DEVICE_ID AS DEVICE_ID "
                + "FROM AP_DEVICE_SUBSCRIPTION DS "
                + "WHERE DS.AP_APP_RELEASE_ID = ? "
                + "AND DS.UNSUBSCRIBED = ? "
                + "AND DS.TENANT_ID = ? ");

        if (actionStatus != null && !actionStatus.isEmpty()) {
            sql.append(" AND DS.STATUS IN (").
                    append(actionStatus.stream().map(status -> "?").collect(Collectors.joining(","))).append(") ");
        }

        if (actionType != null && !actionType.isEmpty()) {
            sql.append(" AND DS.ACTION_TRIGGERED_FROM = ? ");
        }
        if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
            sql.append(" AND ").append(actionTriggeredColumn).append(" LIKE ? ");
        }

        sql.append("ORDER BY ").append(subscriptionStatusTime).append(" DESC ");

        if (limit >= 0 && offset >= 0) {
            sql.append("LIMIT ? OFFSET ?");
        }

        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                ps.setInt(paramIdx++, appReleaseId);
                ps.setBoolean(paramIdx++, unsubscribe);
                ps.setInt(paramIdx++, tenantId);

                if (actionStatus != null && !actionStatus.isEmpty()) {
                    for (String status : actionStatus) {
                        ps.setString(paramIdx++, status);
                    }
                }

                if (actionType != null && !actionType.isEmpty()) {
                    ps.setString(paramIdx++, actionType);
                }

                if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
                    ps.setString(paramIdx++, "%" + actionTriggeredBy + "%");
                }

                if (limit >= 0 && offset >= 0) {
                    ps.setInt(paramIdx++, limit);
                    ps.setInt(paramIdx, offset);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved device subscriptions for application release id "
                                + appReleaseId);
                    }
                    List<DeviceSubscriptionDTO> deviceSubscriptions = new ArrayList<>();

                    while (rs.next()) {
                        DeviceSubscriptionDTO subscription = new DeviceSubscriptionDTO();
                        subscription.setId(rs.getInt("ID"));
                        subscription.setSubscribedBy(rs.getString("SUBSCRIBED_BY"));
                        subscription.setSubscribedTimestamp(rs.getTimestamp("SUBSCRIBED_AT"));
                        subscription.setUnsubscribed(rs.getBoolean("IS_UNSUBSCRIBED"));
                        subscription.setUnsubscribedBy(rs.getString("UNSUBSCRIBED_BY"));
                        subscription.setUnsubscribedTimestamp(rs.getTimestamp("UNSUBSCRIBED_AT"));
                        subscription.setActionTriggeredFrom(rs.getString("ACTION_TRIGGERED_FROM"));
                        subscription.setStatus(rs.getString("STATUS"));
                        subscription.setDeviceId(rs.getInt("DEVICE_ID"));

                        deviceSubscriptions.add(subscription);
                    }
                    return deviceSubscriptions;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device subscription for "
                    + "application Id: " + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while running SQL to get device subscription data for application ID: " + appReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getAllSubscriptionsCount(int appReleaseId, boolean unsubscribe, int tenantId,
                                        List<String> actionStatus, String actionType, String actionTriggeredBy)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions for the application release id " + appReleaseId
                    + " from the database");
        }
        String actionTriggeredColumn = unsubscribe ? "DS.UNSUBSCRIBED_BY" : "DS.SUBSCRIBED_BY";
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT DS.DM_DEVICE_ID) AS COUNT "
                + "FROM AP_DEVICE_SUBSCRIPTION DS "
                + "WHERE DS.AP_APP_RELEASE_ID = ? "
                + "AND DS.UNSUBSCRIBED = ? "
                + "AND DS.TENANT_ID = ? ");

        if (actionStatus != null && !actionStatus.isEmpty()) {
            sql.append(" AND DS.STATUS IN (").
                    append(actionStatus.stream().map(status -> "?").collect(Collectors.joining(","))).append(") ");
        }
        if (actionType != null && !actionType.isEmpty()) {
            sql.append(" AND DS.ACTION_TRIGGERED_FROM = ? ");
        }
        if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
            sql.append(" AND ").append(actionTriggeredColumn).append(" LIKE ?");
        }
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                ps.setInt(paramIdx++, appReleaseId);
                ps.setBoolean(paramIdx++, unsubscribe);
                ps.setInt(paramIdx++, tenantId);

                if (actionStatus != null && !actionStatus.isEmpty()) {
                    for (String status : actionStatus) {
                        ps.setString(paramIdx++, status);
                    }
                }
                if (actionType != null && !actionType.isEmpty()) {
                    ps.setString(paramIdx++, actionType);
                }
                if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
                    ps.setString(paramIdx++, "%" + actionTriggeredBy + "%");
                }
                try (ResultSet rs = ps.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved device subscriptions for application release id "
                                + appReleaseId);
                    }
                    return rs.next() ? rs.getInt("COUNT") : 0;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device subscription for "
                    + "application Id: " + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while running SQL to get device subscription data for application ID: " + appReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getAllSubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting all subscriptions count for the application appReleaseId " + appReleaseId + " from the database");
        }
        List<String> allowingDeviceStatuses = new ArrayList<>();
        allowingDeviceStatuses.add(EnrolmentInfo.Status.ACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.INACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.UNREACHABLE.toString());

        DeviceManagementProviderService deviceManagementProviderService = HelperUtil.getDeviceManagementProviderService();
        try {
            Connection conn = this.getDBConnection();
            List<Integer> deviceIds = deviceManagementProviderService.getDeviceIdsByStatus(allowingDeviceStatuses);
            if (deviceIds.isEmpty()) {
                return 0;
            }
            StringBuilder idList = new StringBuilder();
            for (int i = 0; i < deviceIds.size(); i++) {
                idList.append("?");
                if (i < deviceIds.size() - 1) {
                    idList.append(",");
                }
            }
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_DEVICE_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = FALSE " +
                    "AND DM_DEVICE_ID IN (" + idList.toString() + ")";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                for (int i = 0; i < deviceIds.size(); i++) {
                    ps.setInt(3 + i, deviceIds.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get all subscriptions count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException | DeviceManagementException e) {
            String msg = "Error occurred while obtaining the DB connection for getting all subscriptions count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getAllUnsubscriptionCount(int appReleaseId, int tenantId) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting all unsubscription count for the application appReleaseId " + appReleaseId + " from the database");
        }
        List<String> allowingDeviceStatuses = new ArrayList<>();
        allowingDeviceStatuses.add(EnrolmentInfo.Status.ACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.INACTIVE.toString());
        allowingDeviceStatuses.add(EnrolmentInfo.Status.UNREACHABLE.toString());

        DeviceManagementProviderService deviceManagementProviderService = HelperUtil.getDeviceManagementProviderService();
        try {
            Connection conn = this.getDBConnection();
            List<Integer> deviceIds = deviceManagementProviderService.getDeviceIdsByStatus(allowingDeviceStatuses);
            if (deviceIds.isEmpty()) {
                return 0;
            }
            StringBuilder idList = new StringBuilder();
            for (int i = 0; i < deviceIds.size(); i++) {
                idList.append("?");
                if (i < deviceIds.size() - 1) {
                    idList.append(",");
                }
            }
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_DEVICE_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = TRUE " +
                    "AND DM_DEVICE_ID IN (" + idList.toString() + ")";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                for (int i = 0; i < deviceIds.size(); i++) {
                    ps.setInt(3 + i, deviceIds.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get all unsubscription count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException | DeviceManagementException e) {
            String msg = "Error occurred while obtaining the DB connection for getting all unsubscription count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getDeviceSubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_DEVICE_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = FALSE " +
                    "AND ACTION_TRIGGERED_FROM = 'DEVICE'";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get device subscriptions count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device subscriptions count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getDeviceUnsubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device unsubscriptions count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_DEVICE_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = TRUE " +
                    "AND ACTION_TRIGGERED_FROM = 'DEVICE'";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get device unsubscription count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device unsubscription count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getGroupSubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting group subscriptions count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_GROUP_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = FALSE";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get group subscriptions count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting group subscriptions count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getGroupUnsubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting group unsubscriptions count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_GROUP_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = TRUE";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get group unsubscription count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting group unsubscription count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getRoleSubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting role subscriptions count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_ROLE_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = FALSE";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get role subscriptions count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting role subscriptions count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getRoleUnsubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting role unsubscription count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_ROLE_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = TRUE";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get role unsubscription count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting role unsubscription count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getUserSubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting user subscriptions count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_USER_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = FALSE";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get user subscriptions count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting user subscriptions count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public int getUserUnsubscriptionCount(int appReleaseId, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting user unsubscription count for the application appReleaseId " + appReleaseId
                    + " from the database");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) AS count " +
                    "FROM AP_USER_SUBSCRIPTION " +
                    "WHERE AP_APP_RELEASE_ID = ? " +
                    "AND TENANT_ID = ? " +
                    "AND UNSUBSCRIBED = TRUE";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count");
                    }
                    return 0;
                }
            } catch (SQLException e) {
                String msg = "Error occurred while running SQL to get user unsubscription count for application appReleaseId: "
                        + appReleaseId;
                log.error(msg, e);
                throw new ApplicationManagementDAOException(msg, e);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting user unsubscription count for appReleaseId: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public SubscriptionStatisticDTO getSubscriptionStatistic(List<Integer> deviceIds, boolean isUnsubscribed, int tenantId, int appReleaseId)
            throws ApplicationManagementDAOException {
        SubscriptionStatisticDTO subscriptionStatisticDTO = new SubscriptionStatisticDTO();
        if (deviceIds == null || deviceIds.isEmpty()) return subscriptionStatisticDTO;

        try {
            Connection connection = getDBConnection();
            String sql = "SELECT COUNT(DISTINCT ID) AS COUNT, " +
                    "STATUS FROM AP_DEVICE_SUBSCRIPTION " +
                    "WHERE TENANT_ID = ? " +
                    "AND AP_APP_RELEASE_ID = ? " +
                    "AND UNSUBSCRIBED = ? " +
                    "AND DM_DEVICE_ID IN (" +
                    deviceIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ") " +
                    "GROUP BY (STATUS)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int idx = 1;

                preparedStatement.setInt(idx++, tenantId);
                preparedStatement.setInt(idx++, appReleaseId);
                preparedStatement.setBoolean(idx++, isUnsubscribed);
                for (Integer deviceId : deviceIds) {
                    preparedStatement.setInt(idx++, deviceId);
                }

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        // add the error and in progress
                        int count = resultSet.getInt("COUNT");
                        String status = resultSet.getString("STATUS");

                        if (SubscriptionMetadata.DBSubscriptionStatus.COMPLETED_STATUS_LIST.contains(status)) {
                            subscriptionStatisticDTO.addToComplete(count);
                        }

                        if (SubscriptionMetadata.DBSubscriptionStatus.PENDING_STATUS_LIST.contains(status)) {
                            subscriptionStatisticDTO.addToPending(count);
                        }
                        if (SubscriptionMetadata.DBSubscriptionStatus.ERROR_STATUS_LIST.contains(status)) {
                            subscriptionStatisticDTO.addToFailed(count);
                        }
                    }
                }
            }
            return subscriptionStatisticDTO;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting subscription statistics";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while running SQL for getting subscription statistics";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    public int countSubscriptionsByStatus(int appReleaseId, int tenantId, String actionStatus, String actionTriggeredFrom) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to count device subscriptions by status and actionTriggeredFrom for the given AppReleaseID.");
        }
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT COUNT(*) FROM AP_DEVICE_SUBSCRIPTION " +
             "WHERE AP_APP_RELEASE_ID = ? " +
             "AND TENANT_ID = ? " +
             "AND STATUS = ?" +
             " AND ACTION_TRIGGERED_FROM = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                ps.setString(3, actionStatus);
                ps.setString(4, actionTriggeredFrom);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to count device subscriptions by status and action trigger.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while counting device subscriptions by status and action trigger.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
        return 0;
    }

}
