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
package org.wso2.carbon.device.application.mgt.core.dao.impl.lifecyclestate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Concrete implementation for Lifecycle related DB operations.
 */
public class GenericLifecycleStateDAOImpl extends AbstractDAOImpl implements LifecycleStateDAO {

    private static final Log log = LogFactory.getLog(GenericLifecycleStateDAOImpl.class);

    @Override
    public LifecycleState getLatestLifecycleState(String uuid) throws LifeCycleManagementDAOException{
        String sql = "SELECT "
                + "CURRENT_STATE, "
                + "PREVIOUS_STATE, "
                + "UPDATED_AT, "
                + "UPDATED_BY "
                + "FROM "
                + "AP_APP_LIFECYCLE_STATE "
                + "WHERE "
                + "AP_APP_RELEASE_ID = (SELECT ID FROM AP_APP_RELEASE WHERE UUID = ?) ORDER BY UPDATED_AT DESC";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid);
                try (ResultSet rs = stmt.executeQuery()){
                    return constructLifecycle(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get latest lifecycle state for a specific"
                    + " application. Application release UUID: " + uuid;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to get latest lifecycle state for a specific "
                    + "application. Application release UUID: " + uuid + ". Executed Query: " + sql;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }
    }

    @Override
    public String getAppReleaseCreatedUsername(int appId, String uuid, int tenantId) throws LifeCycleManagementDAOException{
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT "
                    + "UPDATED_BY "
                    + "FROM AP_APP_LIFECYCLE_STATE "
                    + "WHERE "
                    + "AP_APP_ID = ? AND "
                    + "AP_APP_RELEASE_ID = (SELECT ID FROM AP_APP_RELEASE WHERE UUID=?) AND "
                    + "CURRENT_STATE = ? AND "
                    + "TENANT_ID = ? ORDER BY UPDATED_AT DESC LIMIT 1";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, appId);
            stmt.setString(2, uuid);
            stmt.setString(3, AppLifecycleState.CREATED.toString());
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("UPDATED_BY");
            }
            return null;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get the created user of a release which "
                    + "has APP ID " + appId + " and release UUID ." + uuid;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }  catch (SQLException e) {
            String msg = "SQL Error occurred when getting the created user of a release which has APP ID " + appId
                    + " and release UUID ." + uuid;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        } finally {
            DAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<LifecycleState> getLifecycleStates(int appReleaseId, int tenantId) throws LifeCycleManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            String sql = "SELECT "
                    + "CURRENT_STATE, "
                    + "PREVIOUS_STATE, "
                    + "UPDATED_AT, "
                    + "UPDATED_BY "
                    + "FROM AP_APP_LIFECYCLE_STATE "
                    + "WHERE AP_APP_RELEASE_ID = ?  AND "
                    + "TENANT_ID = ? "
                    + "ORDER BY UPDATED_AT ASC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1,appReleaseId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()){
                    return getLifecycleStates(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection when getting lifecycle states for an "
                    + "application which has application ID: " + appReleaseId;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while retrieving lifecycle states for application which has application "
                    + "ID: " + appReleaseId;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }
    }

    @Override
    public void addLifecycleState(LifecycleState state, int appReleaseId, int tenantId) throws LifeCycleManagementDAOException {
        String sql = "INSERT INTO AP_APP_LIFECYCLE_STATE "
                + "(CURRENT_STATE, "
                + "PREVIOUS_STATE, "
                + "TENANT_ID, "
                + "UPDATED_BY, "
                + "UPDATED_AT, "
                + "REASON, "
                + "AP_APP_RELEASE_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, state.getCurrentState().toUpperCase());
                stmt.setString(2, state.getPreviousState().toUpperCase());
                stmt.setInt(3, tenantId);
                stmt.setString(4, state.getUpdatedBy());
                stmt.setTimestamp(5, timestamp);
                stmt.setString(6, state.getReasonForChange());
                stmt.setInt(7, appReleaseId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to add lifecycle state for application "
                    + "release which has ID " + appReleaseId + ". Lifecycle state " + state.getCurrentState();
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing the query to add lifecycle state for application release which"
                    + " has ID " + appReleaseId + ". Lifecycle state " + state.getCurrentState() + ". Executed query: "
                    + sql;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteLifecycleStateByReleaseId(int releaseId) throws LifeCycleManagementDAOException {
        String sql = "DELETE "
                + "FROM AP_APP_LIFECYCLE_STATE " +
                "WHERE AP_APP_RELEASE_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, releaseId);
                stmt.executeUpdate();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to delete lifecycle states for application "
                    + "release ID: " + releaseId;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing the query to delete lifecycle states for application release"
                    + " ID: " + releaseId + ". Executed query " + sql;
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteLifecycleStates(List<Integer> appReleaseIds) throws LifeCycleManagementDAOException{
        String sql = "DELETE "
                + "FROM AP_APP_LIFECYCLE_STATE " +
                "WHERE AP_APP_RELEASE_ID = ?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer releaseId : appReleaseIds) {
                    stmt.setInt(1, releaseId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for deleting application life-cycle states "
                    + "for given application Ids.";
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to delete application life-cycle states for given "
                    + "application Ids.";
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }
    }

    /***
     * This method is capable to construct {@link LifecycleState} object by accessing given {@link ResultSet}
     * @param rs Result Set of an executed query
     * @return {@link LifecycleState}
     * @throws LifeCycleManagementDAOException if {@link SQLException} occurs when creating the {@link LifecycleState}
     * by accessing given {@link ResultSet}. In this particular method {@link SQLException} could occurs if the
     * columnLabel is not valid or if a database access error occurs or this method is called on a closed result set
     *
     */
    private LifecycleState constructLifecycle(ResultSet rs) throws LifeCycleManagementDAOException {
        LifecycleState lifecycleState = null;
        try {
            if (rs.next()) {
                lifecycleState = new LifecycleState();
                lifecycleState.setCurrentState(rs.getString("CURRENT_STATE"));
                lifecycleState.setPreviousState(rs.getString("PREVIOUS_STATE"));
                lifecycleState.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
                lifecycleState.setUpdatedBy(rs.getString("UPDATED_BY"));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while construct lifecycle state by data which is retrieved from SQL query";
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }
        return lifecycleState;
    }

    /**
     * Get the list of lifecycle states by using Result Set
     *
     * @param rs ResultSet object
     * @return List of life cycle states
     * @throws LifeCycleManagementDAOException if error occurred while getting lifecycle state data from ResultSet
     *                                         object
     */
    private List<LifecycleState> getLifecycleStates(ResultSet rs) throws LifeCycleManagementDAOException {
        List<LifecycleState> lifecycleStates = new ArrayList<>();
        try {
            while (rs.next()) {
                LifecycleState lifecycleState = new LifecycleState();
                lifecycleState.setCurrentState(rs.getString("CURRENT_STATE"));
                lifecycleState.setPreviousState(rs.getString("PREVIOUS_STATE"));
                lifecycleState.setUpdatedAt(rs.getTimestamp("UPDATED_AT"));
                lifecycleState.setUpdatedBy(rs.getString("UPDATED_BY"));
                lifecycleStates.add(lifecycleState);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while construct lifecycle state by data which is retrieved from SQL query";
            log.error(msg, e);
            throw new LifeCycleManagementDAOException(msg, e);
        }
        return lifecycleStates;
    }
}
