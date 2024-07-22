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

import io.entgra.device.mgt.core.application.mgt.common.SubscriptionEntity;
import io.entgra.device.mgt.core.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This handles Application subscribing operations which are specific to Oracle.
 */
public class OracleSubscriptionDAOImpl extends GenericSubscriptionDAOImpl {

    private static Log log = LogFactory.getLog(OracleSubscriptionDAOImpl.class);

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
                    + "US.USER_NAME AS USER "
                    + "FROM AP_USER_SUBSCRIPTION US "
                    + "WHERE "
                    + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? ORDER BY US.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);
                stmt.setInt(3, offsetValue);
                stmt.setInt(4, limitValue);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("USER"));
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
                    + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? ORDER BY RS.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                ps.setInt(3, offsetValue);
                ps.setInt(4, limitValue);
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
                    + "AP_APP_RELEASE_ID = ? AND TENANT_ID = ? ORDER BY GS.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setInt(2, tenantId);
                ps.setInt(3, offsetValue);
                ps.setInt(4, limitValue);
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
                sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
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
                    ps.setInt(paramIdx++, offset);
                    ps.setInt(paramIdx, limit);
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
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setBoolean(2, unsubscribe);
                ps.setInt(3, tenantId);
                ps.setInt(4, offset);
                ps.setInt(5, limit);
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
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setBoolean(2, unsubscribe);
                ps.setInt(3, tenantId);
                ps.setInt(4, offset);
                ps.setInt(5, limit);
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
                    "AND GS.UNSUBSCRIBED = ? AND GS.TENANT_ID = ? " +
                    "ORDER BY " + subscriptionStatusTime + " DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, appReleaseId);
                ps.setBoolean(2, unsubscribe);
                ps.setInt(3, tenantId);
                ps.setInt(4, offset);
                ps.setInt(5, limit);

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
    public List<DeviceSubscriptionDTO> getAllSubscriptionsDetails(int appReleaseId, boolean unsubscribe, int tenantId,
                                                                  List<String> actionStatus, String actionType, String actionTriggeredBy,
                                                                  int offset, int limit) throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions for the application release id " + appReleaseId + " from the database");
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
            sql.append(" AND DS.STATUS IN (")
                    .append(actionStatus.stream().map(status -> "?").collect(Collectors.joining(","))).append(") ");
        }
        if (actionType != null && !actionType.isEmpty()) {
            sql.append(" AND DS.ACTION_TRIGGERED_FROM = ? ");
        }
        if (actionTriggeredBy != null && !actionTriggeredBy.isEmpty()) {
            sql.append(" AND ").append(actionTriggeredColumn).append(" LIKE ? ");
        }
        sql.append("ORDER BY ").append(subscriptionStatusTime).append(" DESC ");
        if (limit >= 0 && offset >= 0) {
            sql.append("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
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
                    ps.setInt(paramIdx++, offset);
                    ps.setInt(paramIdx, limit);
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
            String msg = "Error occurred while obtaining the DB connection for getting device subscription for application Id: "
                    + appReleaseId + ".";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while running SQL to get device subscription data for application ID: " + appReleaseId;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
