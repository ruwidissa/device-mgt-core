/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.application.mgt.core.dao.impl.subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ExecutionStatus;
import org.wso2.carbon.device.application.mgt.common.SubAction;
import org.wso2.carbon.device.application.mgt.common.SubscriptionType;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ScheduledSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.UnexpectedServerErrorException;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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
    public List<DeviceSubscriptionDTO> getDeviceSubscriptions(int appReleaseId, int tenantId) throws
            ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions for the application release id " + appReleaseId
                    + " from the database");
        }
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
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);
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
            Connection conn = this.getDBConnection();
            int index = 1;
            Map<Integer, DeviceSubscriptionDTO> deviceSubscriptionDTOHashMap = new HashMap<>();
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
            Connection conn = this.getDBConnection();
            int index = 1;
            List<String> subscribedUsers = new ArrayList<>();
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
            Connection conn = this.getDBConnection();
            int index = 1;
            List<String> subscribedUsers = new ArrayList<>();
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
                        subscribedUsers.add(rs.getString("ROLE"));
                    }
                }
            }
            return subscribedUsers;
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
            Connection conn = this.getDBConnection();
            int index = 1;
            List<String> subscribedUsers = new ArrayList<>();
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
                        subscribedUsers.add(rs.getString("GROUP_NAME"));
                    }
                }
            }
            return subscribedUsers;
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
            Connection conn = this.getDBConnection();
            int index = 1;
            List<Integer> subscribedDevices = new ArrayList<>();
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
                stmt.setTimestamp(5, Timestamp.valueOf(subscriptionDTO.getScheduledAt()));
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
    public boolean updateScheduledSubscription(int id, LocalDateTime scheduledAt, String scheduledBy)
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
                stmt.setTimestamp(1, Timestamp.valueOf(scheduledAt));
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
                stmt.setTimestamp(3, new Timestamp(Calendar.getInstance().getTime().getTime()));
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
                         + "GS.GROUP_NAME AS GROUPS "
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
                        subscribedGroups.add(rs.getString("GROUPS"));
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
                         + "COUNT(GS.GROUP_NAME) AS GROUPS "
                         + "FROM AP_GROUP_SUBSCRIPTION GS "
                         + "WHERE "
                         + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("GROUPS");
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
}
