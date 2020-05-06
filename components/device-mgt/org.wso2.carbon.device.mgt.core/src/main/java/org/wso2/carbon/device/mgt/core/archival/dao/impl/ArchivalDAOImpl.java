/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.archival.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDAO;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDAOException;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDAOUtil;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDestinationDAOFactory;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalSourceDAOFactory;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ArchivalDAOImpl implements ArchivalDAO {

    private static final Log log = LogFactory.getLog(ArchivalDAOImpl.class);

    private final int retentionPeriod;

    private static final String SOURCE_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getSourceDB();

    private static final String DESTINATION_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getDestinationDB();

    public ArchivalDAOImpl(int retentionPeriod, int batchSize) {
        this.retentionPeriod = retentionPeriod;
        if (log.isDebugEnabled()) {
            log.debug("Using batch size of " + batchSize + " with retention period " + this.retentionPeriod);
        }
    }

    public List<Integer> getNonRemovableOperationMappingIDs(Timestamp time) throws ArchivalDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> removableOperationMappingIds = new ArrayList<>();
        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_ENROLMENT_OP_MAPPING " +
                    "WHERE UPDATED_TIMESTAMP < UNIX_TIMESTAMP(DATE_SUB(?, INTERVAL ? DAY)) " +
                    "AND (STATUS != 'COMPLETED' AND STATUS != 'ERROR')";

            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, time);
            stmt.setInt(2, this.retentionPeriod);

            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;
            while (rs.next()) {
                removableOperationMappingIds.add(rs.getInt("ID"));
            }

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for getting Non Removable Operation Mapping IDs: " + difference);
                log.debug("Total Non Removable Operation Mapping IDs: " + removableOperationMappingIds.size());
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting Non Removable Operation Mapping IDs. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return removableOperationMappingIds;
    }

    public int getLargeOperationResponseCount(Timestamp time, List<Integer> nonRemovableMappings)
            throws ArchivalDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS COUNT FROM DM_DEVICE_OPERATION_RESPONSE_LARGE " +
                    "WHERE RECEIVED_TIMESTAMP < (DATE_SUB( ? , INTERVAL ? DAY))");
            if (nonRemovableMappings.size() > 0) {
                sql.append(" AND EN_OP_MAP_ID NOT IN (");
                for (int i = 0; i < nonRemovableMappings.size(); i++) {
                    sql.append(nonRemovableMappings.get(i));
                    if (i != nonRemovableMappings.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }

            stmt = conn.prepareStatement(sql.toString());
            stmt.setTimestamp(1, time);
            stmt.setInt(2, this.retentionPeriod);

            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            while (rs.next()) {
                count = rs.getInt("COUNT");
            }

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for getting Large Operation Response Count : " + difference);
                log.debug("Total Large Operation Responses for Archival : " + count);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while archiving the large operation responses. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return count;
    }

    public int getOpMappingsCount(Timestamp time) throws ArchivalDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS COUNT FROM DM_ENROLMENT_OP_MAPPING " +
                    "WHERE UPDATED_TIMESTAMP < UNIX_TIMESTAMP(DATE_SUB( ? , INTERVAL ? DAY))" +
                    "AND (STATUS = 'COMPLETED' OR STATUS = 'ERROR')";

            stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, time);
            stmt.setInt(2, this.retentionPeriod);

            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            while (rs.next()) {
                count = rs.getInt("COUNT");
            }

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for getting Op Mappings Count : " + difference);
                log.debug("Total Enrollment Operation Mappings for Archival : " + count);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while getting Op Mappings Count. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return count;
    }

    @Override
    public int getOperationResponseCount(Timestamp time, List<Integer> nonRemovableMappings)
            throws ArchivalDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();

            StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS COUNT FROM DM_DEVICE_OPERATION_RESPONSE " +
                    "WHERE RECEIVED_TIMESTAMP < (DATE_SUB( ? , INTERVAL ? DAY))");
            if (nonRemovableMappings.size() > 0) {
                sql.append(" AND EN_OP_MAP_ID NOT IN (");
                for (int i = 0; i < nonRemovableMappings.size(); i++) {
                    sql.append(nonRemovableMappings.get(i));
                    if (i != nonRemovableMappings.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }

            stmt = conn.prepareStatement(sql.toString());
            stmt.setTimestamp(1, time);
            stmt.setInt(2, this.retentionPeriod);

            long startTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            while (rs.next()) {
                count = rs.getInt("COUNT");
            }

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for getting Operation Response Count : " + difference);
                log.debug("Total Operation Responses for Archival : " + count);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while archiving the operation response count. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt, rs);
        }
        return count;
    }

    @Override
    public void transferOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings)
            throws ArchivalDAOException {
        PreparedStatement ps = null;

        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();

            StringBuilder sql = new StringBuilder("INSERT INTO " + DESTINATION_DB + ".DM_DEVICE_OPERATION_RESPONSE_ARCH " +
                    "SELECT OPR.ID, OPR.ENROLMENT_ID, OPR.OPERATION_ID, OPR.OPERATION_RESPONSE, OPR.RECEIVED_TIMESTAMP, NOW(), OPR.IS_LARGE_RESPONSE " +
                    "FROM " + SOURCE_DB + ".DM_DEVICE_OPERATION_RESPONSE OPR " +
                    "WHERE OPR.RECEIVED_TIMESTAMP < ( DATE_SUB( ? , INTERVAL ? DAY))");
            if (nonRemovableMappings.size() > 0) {
                sql.append(" AND EN_OP_MAP_ID NOT IN (");
                for (int i = 0; i < nonRemovableMappings.size(); i++) {
                    sql.append(nonRemovableMappings.get(i));
                    if (i != nonRemovableMappings.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }
            sql.append(" ORDER BY OPR.RECEIVED_TIMESTAMP LIMIT ?");

            long startTime = System.currentTimeMillis();

            ps = conn.prepareStatement(sql.toString());
            ps.setTimestamp(1, time);
            ps.setInt(2, this.retentionPeriod);
            ps.setInt(3, batchSize);

            int affected = ps.executeUpdate();

            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Transferring Operation Responses : " + difference);
                log.debug("Transfer of " + affected + " Operation Responses Completed");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving the operation responses. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(ps);
        }
    }

    @Override
    public void transferLargeOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings)
            throws ArchivalDAOException {

        PreparedStatement ps = null;

        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();

            StringBuilder sql = new StringBuilder("INSERT INTO " + DESTINATION_DB + ".DM_DEVICE_OPERATION_RESPONSE_LARGE_ARCH " +
                    "SELECT OPR.ID, OPR.OPERATION_RESPONSE, NOW() " +
                    "FROM " + SOURCE_DB + ".DM_DEVICE_OPERATION_RESPONSE_LARGE OPR " +
                    "WHERE OPR.RECEIVED_TIMESTAMP < ( DATE_SUB( ? , INTERVAL ? DAY))");
            if (nonRemovableMappings.size() > 0) {
                sql.append(" AND EN_OP_MAP_ID NOT IN (");
                for (int i = 0; i < nonRemovableMappings.size(); i++) {
                    sql.append(nonRemovableMappings.get(i));
                    if (i != nonRemovableMappings.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }
            sql.append(" ORDER BY OPR.RECEIVED_TIMESTAMP LIMIT ?");
            long startTime = System.currentTimeMillis();

            ps = conn.prepareStatement(sql.toString());
            ps.setTimestamp(1, time);
            ps.setInt(2, this.retentionPeriod);
            ps.setInt(3, batchSize);

            int affected = ps.executeUpdate();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Transferring Large Operation Responses : " + difference);
                log.debug("Transfer of " + affected + " Large Operation Responses Completed");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving large operation responses. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(ps);
        }
    }

    @Override
    public void removeLargeOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings)
            throws ArchivalDAOException {
        PreparedStatement ps = null;

        Connection conn;
        try {
            conn = ArchivalSourceDAOFactory.getConnection();

            StringBuilder sql = new StringBuilder("DELETE FROM DM_DEVICE_OPERATION_RESPONSE_LARGE " +
                    "WHERE RECEIVED_TIMESTAMP < ( DATE_SUB( ? , INTERVAL ? DAY))");
            if (nonRemovableMappings.size() > 0) {
                sql.append(" AND EN_OP_MAP_ID NOT IN (");
                for (int i = 0; i < nonRemovableMappings.size(); i++) {
                    sql.append(nonRemovableMappings.get(i));
                    if (i != nonRemovableMappings.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }
            sql.append(" ORDER BY RECEIVED_TIMESTAMP LIMIT ?");
            long startTime = System.currentTimeMillis();

            ps = conn.prepareStatement(sql.toString());
            ps.setTimestamp(1, time);
            ps.setInt(2, this.retentionPeriod);
            ps.setInt(3, batchSize);

            int affected = ps.executeUpdate();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Removing Large Operation Responses : " + difference);
                log.debug(affected + " Rows deleted from DM_DEVICE_OPERATION_RESPONSE_LARGE");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while removing the operation responses. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(ps);
        }
    }

    @Override
    public void removeOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings)
            throws ArchivalDAOException {
        PreparedStatement ps = null;

        Connection conn;
        try {
            conn = ArchivalSourceDAOFactory.getConnection();

            StringBuilder sql = new StringBuilder("DELETE FROM DM_DEVICE_OPERATION_RESPONSE " +
                    "WHERE RECEIVED_TIMESTAMP < ( DATE_SUB( ? , INTERVAL ? DAY))");
            if (nonRemovableMappings.size() > 0) {
                sql.append(" AND EN_OP_MAP_ID NOT IN (");
                for (int i = 0; i < nonRemovableMappings.size(); i++) {
                    sql.append(nonRemovableMappings.get(i));
                    if (i != nonRemovableMappings.size() - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            }
            sql.append(" ORDER BY RECEIVED_TIMESTAMP LIMIT ?");
            long startTime = System.currentTimeMillis();

            ps = conn.prepareStatement(sql.toString());
            ps.setTimestamp(1, time);
            ps.setInt(2, this.retentionPeriod);
            ps.setInt(3, batchSize);

            int affected = ps.executeUpdate();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Removing Operation Responses : " + difference);
                log.debug(affected + " Rows deleted from DM_DEVICE_OPERATION_RESPONSE");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while removing operation responses. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(ps);
        }
    }

    @Override
    public void moveNotifications(Timestamp time) throws ArchivalDAOException {
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try {
            Connection conn = ArchivalSourceDAOFactory.getConnection();
            Connection conn2 = ArchivalDestinationDAOFactory.getConnection();

            String sql = "INSERT INTO " + DESTINATION_DB + ".DM_NOTIFICATION_ARCH " +
                    "SELECT NOTIFICATION_ID, DEVICE_ID, OPERATION_ID, TENANT_ID, STATUS, DESCRIPTION, NOW() " +
                    "FROM " + SOURCE_DB + ".DM_NOTIFICATION " +
                    "WHERE LAST_UPDATED_TIMESTAMP < ( DATE_SUB( ? , INTERVAL ? DAY) )";

            ps1 = conn2.prepareStatement(sql);
            ps1.setTimestamp(1, time);
            ps1.setInt(2, this.retentionPeriod);

            long startTime = System.currentTimeMillis();
            int affected = ps1.executeUpdate();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Transfer of operations : " + difference);
                log.debug(affected + " [NOTIFICATIONS] Records copied to the archival table. Starting deletion");
            }

            sql = "DELETE FROM DM_NOTIFICATION WHERE LAST_UPDATED_TIMESTAMP < ( DATE_SUB( ? , INTERVAL ? DAY) )";

            ps2 = conn.prepareStatement(sql);
            ps2.setTimestamp(1, time);
            ps2.setInt(2, this.retentionPeriod);

            startTime = System.currentTimeMillis();
            affected = ps2.executeUpdate();
            endTime = System.currentTimeMillis();
            difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for deleting operations : " + difference);
                log.debug(affected + " Rows deleted");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting operations. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(ps1);
            ArchivalDAOUtil.cleanupResources(ps2);
        }
    }

    @Override
    public void transferEnrollmentOpMappings(int batchSize, Timestamp time) throws ArchivalDAOException {

        PreparedStatement ps = null;

        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();

            String sql = "INSERT INTO " + DESTINATION_DB + ".DM_ENROLMENT_OP_MAPPING_ARCH " +
                    "SELECT OPR.ID, OPR.ENROLMENT_ID, OPR.OPERATION_ID, OPR.STATUS, OPR.CREATED_TIMESTAMP, OPR.UPDATED_TIMESTAMP, NOW() " +
                    "FROM " + SOURCE_DB + ".DM_ENROLMENT_OP_MAPPING OPR " +
                    "WHERE OPR.UPDATED_TIMESTAMP < UNIX_TIMESTAMP( DATE_SUB( ? , INTERVAL ? DAY)) " +
                    "AND (STATUS = 'COMPLETED' OR STATUS = 'ERROR') " +
                    "ORDER BY OPR.UPDATED_TIMESTAMP LIMIT ?";

            ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, time);
            ps.setInt(2, this.retentionPeriod);
            ps.setInt(3, batchSize);

            long startTime = System.currentTimeMillis();
            int affected = ps.executeUpdate();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Transferring Enrollment Operation Mappings : " + difference);
                log.debug("Transfer of " + affected + " Enrollment Operation Mappings Completed");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving Enrollment Operation Mappings. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(ps);
        }
    }

    @Override
    public void removeEnrollmentOPMappings(int batchSize, Timestamp time) throws ArchivalDAOException {
        PreparedStatement ps = null;
        Connection conn;

        try {
            conn = ArchivalSourceDAOFactory.getConnection();

            String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING " +
                    "WHERE UPDATED_TIMESTAMP < UNIX_TIMESTAMP( DATE_SUB( ? , INTERVAL ? DAY)) " +
                    "AND (STATUS = 'COMPLETED' OR STATUS = 'ERROR') " +
                    "ORDER BY UPDATED_TIMESTAMP LIMIT ?";

            ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, time);
            ps.setInt(2, this.retentionPeriod);
            ps.setInt(3, batchSize);

            long startTime = System.currentTimeMillis();
            int affected = ps.executeUpdate();
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Removing Enrollment Operation Mappings : " + difference);
                log.debug(affected + " Rows deleted from DM_ENROLMENT_OP_MAPPING");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while removing Enrollment Operation Mappings. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(ps);
        }
    }

    @Override
    public void transferOperations() throws ArchivalDAOException {
        Statement stmt = null;
        try {
            Connection conn = ArchivalDestinationDAOFactory.getConnection();
            stmt = conn.createStatement();

            String sql = "INSERT INTO " + DESTINATION_DB + ".DM_OPERATION_ARCH " +
                    "SELECT OPR.ID, OPR.TYPE, OPR.CREATED_TIMESTAMP, OPR.RECEIVED_TIMESTAMP, " +
                    "OPR.OPERATION_CODE, OPR.INITIATED_BY, OPR.OPERATION_DETAILS, OPR.ENABLED, NOW() " +
                    "FROM   " + SOURCE_DB + ".DM_OPERATION OPR " +
                    "WHERE OPR.ID NOT IN (SELECT DISTINCT OPERATION_ID FROM " + SOURCE_DB + ".DM_ENROLMENT_OP_MAPPING)";

            long startTime = System.currentTimeMillis();
            int affected = stmt.executeUpdate(sql);
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Transferring Operations : " + difference);
                log.debug("Transfer of " + affected + " Operations Completed");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while archiving Operations. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }

    @Override
    public void removeOperations() throws ArchivalDAOException {
        Statement stmt = null;
        Connection conn;

        try {
            conn = ArchivalSourceDAOFactory.getConnection();
            stmt = conn.createStatement();

            String sql = "DELETE FROM DM_OPERATION " +
                    "WHERE ID NOT IN (SELECT DISTINCT OPERATION_ID FROM DM_ENROLMENT_OP_MAPPING)";

            long startTime = System.currentTimeMillis();
            int affected = stmt.executeUpdate(sql);
            long endTime = System.currentTimeMillis();
            long difference = endTime - startTime;

            if (log.isDebugEnabled()) {
                log.debug("Time Elapsed for Removing Operations : " + difference);
                log.debug(affected + " Rows deleted from DM_OPERATION");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while removing Operations. " + e.getMessage();
            throw new ArchivalDAOException(msg, e);
        } finally {
            ArchivalDAOUtil.cleanupResources(stmt);
        }
    }
}
