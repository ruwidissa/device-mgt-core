/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.policy.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyDeviceWrapper;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAO;
import org.wso2.carbon.policy.mgt.core.dao.MonitoringDAOException;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.util.PolicyManagementDAOUtil;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagerUtil;

import java.sql.*;
import java.util.*;

public class MonitoringDAOImpl implements MonitoringDAO {

    private static final Log log = LogFactory.getLog(MonitoringDAOImpl.class);

    @Override
    public int addComplianceDetails(int deviceId, int policyId) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_COMPLIANCE_STATUS (DEVICE_ID, POLICY_ID, STATUS, " +
                    "LAST_REQUESTED_TIME, TENANT_ID) VALUES (?, ?, ?,?, ?) ";
            stmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, policyId);
            stmt.setInt(3, 1);
            stmt.setTimestamp(4, currentTimestamp);
            stmt.setInt(5, tenantId);
            stmt.executeUpdate();

            generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new MonitoringDAOException("Error occurred while adding the none compliance to the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
    }


    @Override
    public void addComplianceDetails(Map<Integer, Integer> devicePolicyMap) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Adding the compliance details for devices and policies");
            for (Map.Entry<Integer, Integer> map : devicePolicyMap.entrySet()) {
                log.debug(map.getKey() + " -- " + map.getValue());
            }
        }
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_COMPLIANCE_STATUS (DEVICE_ID, POLICY_ID, STATUS, " +
                    "LAST_REQUESTED_TIME, TENANT_ID) VALUES (?, ?, ?, ?, ?) ";
            stmt = conn.prepareStatement(query);
            for (Map.Entry<Integer, Integer> map : devicePolicyMap.entrySet()) {
                stmt.setInt(1, map.getKey());
                stmt.setInt(2, map.getValue());
                stmt.setInt(3, 1);
                stmt.setTimestamp(4, currentTimestamp);
                stmt.setInt(5, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new MonitoringDAOException("Error occurred while adding the none compliance to the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
    }

    @Override
    public void addComplianceDetails(List<PolicyDeviceWrapper> policyDeviceWrapper) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            for (PolicyDeviceWrapper wrapper : policyDeviceWrapper) {
                log.debug("Policy Id : " + wrapper.getPolicyId() + " - " + " Device Id : " + wrapper.getDeviceId());
            }
        }
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_COMPLIANCE_STATUS (DEVICE_ID, POLICY_ID, STATUS, " +
                    "LAST_REQUESTED_TIME, TENANT_ID, ENROLMENT_ID) VALUES (?, ?, ?, ?, ?, ?) ";
            stmt = conn.prepareStatement(query);
            for (PolicyDeviceWrapper wrapper : policyDeviceWrapper) {
                stmt.setInt(1, wrapper.getDeviceId());
                stmt.setInt(2, wrapper.getPolicyId());
                stmt.setInt(3, 1);
                stmt.setTimestamp(4, currentTimestamp);
                stmt.setInt(5, tenantId);
                stmt.setInt(6, wrapper.getEnrolmentId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new MonitoringDAOException("Error occurred while adding the none compliance to the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
    }

    @Override
    public void setDeviceAsNoneCompliance(int deviceId, int enrolmentId, int policyId) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY_COMPLIANCE_STATUS SET STATUS = 0, LAST_FAILED_TIME = ?, POLICY_ID = ? " +
                    "WHERE DEVICE_ID = ? AND TENANT_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setTimestamp(1, currentTimestamp);
            stmt.setInt(2, policyId);
            stmt.setInt(3, deviceId);
            stmt.setInt(4, tenantId);
            stmt.setInt(5, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new MonitoringDAOException("Error occurred while updating the none compliance to the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }

    }

    @Override
    public void setDeviceAsCompliance(int deviceId, int enrolmentId, int policyId) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;
        Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "UPDATE DM_POLICY_COMPLIANCE_STATUS SET STATUS = ?, LAST_SUCCESS_TIME = ?" +
                    " WHERE DEVICE_ID = ? AND TENANT_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, 1);
            stmt.setTimestamp(2, currentTimestamp);
            stmt.setInt(3, deviceId);
            stmt.setInt(4, tenantId);
            stmt.setInt(5, enrolmentId);
            stmt.executeUpdate();

//            generatedKeys = stmt.getGeneratedKeys();
//            if (generatedKeys.next()) {
//                return generatedKeys.getInt(1);
//            } else {
//                return 0;
//            }

        } catch (SQLException e) {
            throw new MonitoringDAOException("Error occurred while deleting the none compliance to the database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, generatedKeys);
        }
    }

    @Override
    public void addNonComplianceFeatures(int policyComplianceStatusId, int deviceId, List<ComplianceFeature>
            complianceFeatures) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "INSERT INTO DM_POLICY_COMPLIANCE_FEATURES (COMPLIANCE_STATUS_ID, FEATURE_CODE, STATUS, " +
                    "TENANT_ID) VALUES (?, ?, ?, ?) ";
            stmt = conn.prepareStatement(query);
            for (ComplianceFeature feature : complianceFeatures) {
                stmt.setInt(1, policyComplianceStatusId);
                stmt.setString(2, feature.getFeatureCode());
                if (feature.isCompliant()) {
                    stmt.setInt(3, 1);
                } else {
                    stmt.setInt(3, 0);
                }
                stmt.setInt(4, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new MonitoringDAOException("Error occurred while adding the none compliance features to the " +
                    "database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public NonComplianceData getCompliance(int deviceId, int enrolmentId) throws MonitoringDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        NonComplianceData complianceData = new NonComplianceData();

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_COMPLIANCE_STATUS WHERE DEVICE_ID = ? AND TENANT_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, enrolmentId);

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                complianceData.setId(resultSet.getInt("ID"));
                complianceData.setDeviceId(resultSet.getInt("DEVICE_ID"));
                complianceData.setEnrolmentId(resultSet.getInt("ENROLMENT_ID"));
                complianceData.setPolicyId(resultSet.getInt("POLICY_ID"));
                complianceData.setStatus(resultSet.getBoolean("STATUS"));
                complianceData.setLastRequestedTime(resultSet.getTimestamp("LAST_REQUESTED_TIME"));
                complianceData.setLastSucceededTime(resultSet.getTimestamp("LAST_SUCCESS_TIME"));
                complianceData.setLastFailedTime(resultSet.getTimestamp("LAST_FAILED_TIME"));
            }
            return complianceData;

        } catch (SQLException e) {
            throw new MonitoringDAOException("Unable to retrieve compliance data from database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<NonComplianceData> getCompliance(List<Integer> deviceIds) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<NonComplianceData> complianceDataList = new ArrayList<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_COMPLIANCE_STATUS WHERE TENANT_ID = ? AND DEVICE_ID IN (?)";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);
            stmt.setString(2, PolicyManagerUtil.makeString(deviceIds));

            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                NonComplianceData complianceData = new NonComplianceData();
                complianceData.setId(resultSet.getInt("ID"));
                complianceData.setDeviceId(resultSet.getInt("DEVICE_ID"));
                complianceData.setEnrolmentId(resultSet.getInt("ENROLMENT_ID"));
                complianceData.setPolicyId(resultSet.getInt("POLICY_ID"));
                complianceData.setStatus(resultSet.getBoolean("STATUS"));
                complianceData.setLastRequestedTime(resultSet.getTimestamp("LAST_REQUESTED_TIME"));
                complianceData.setLastSucceededTime(resultSet.getTimestamp("LAST_SUCCESS_TIME"));
                complianceData.setLastFailedTime(resultSet.getTimestamp("LAST_FAILED_TIME"));

                complianceDataList.add(complianceData);
            }
            return complianceDataList;
        } catch (SQLException e) {
            throw new MonitoringDAOException("Unable to retrieve compliance data from database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public Map<Integer, NonComplianceData> getCompliance() throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Map<Integer, NonComplianceData> complianceDataList = new HashMap<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_COMPLIANCE_STATUS WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, tenantId);

            resultSet = stmt.executeQuery();
            NonComplianceData complianceData;
            while (resultSet.next()) {
                complianceData = new NonComplianceData();
                complianceData.setId(resultSet.getInt("ID"));
                complianceData.setDeviceId(resultSet.getInt("DEVICE_ID"));
                complianceData.setEnrolmentId(resultSet.getInt("ENROLMENT_ID"));
                complianceData.setPolicyId(resultSet.getInt("POLICY_ID"));
                complianceData.setStatus(resultSet.getBoolean("STATUS"));
                complianceData.setLastRequestedTime(resultSet.getTimestamp("LAST_REQUESTED_TIME"));
                complianceData.setLastSucceededTime(resultSet.getTimestamp("LAST_SUCCESS_TIME"));
                complianceData.setLastFailedTime(resultSet.getTimestamp("LAST_FAILED_TIME"));

                complianceDataList.put(complianceData.getEnrolmentId(), complianceData);
            }
            return complianceDataList;
        } catch (SQLException e) {
            throw new MonitoringDAOException("Unable to retrieve compliance data from database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<ComplianceData> getAllComplianceDevices(
            PaginationRequest paginationRequest,
            String policyId,
            boolean complianceStatus,
            boolean isPending,
            String fromDate,
            String toDate)
            throws MonitoringDAOException {
        List<ComplianceData> complianceDataList = new ArrayList<>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String query =
                "SELECT " +
                        "DEVICE.NAME, " +
                        "DM_DEVICE_TYPE.NAME AS DEVICE_TYPE, " +
                        "ENROLLMENT.OWNER, " +
                        "DM_POLICY.NAME AS POLICY_NAME, " +
                        "POLICY.* " +
                        "FROM DM_POLICY_COMPLIANCE_STATUS AS POLICY, DM_DEVICE AS DEVICE, " +
                        "DM_ENROLMENT AS ENROLLMENT, DM_POLICY, DM_DEVICE_TYPE " +
                        "WHERE DEVICE.ID=POLICY.DEVICE_ID " +
                        "AND DEVICE.ID=ENROLLMENT.DEVICE_ID " +
                        "AND POLICY.POLICY_ID=DM_POLICY.ID " +
                        "AND DEVICE.DEVICE_TYPE_ID=DM_DEVICE_TYPE.ID " +
                        "AND POLICY.TENANT_ID = ? AND POLICY.STATUS = ?";

        if (isPending) {
            query = query + " AND POLICY.LAST_SUCCESS_TIME IS NULL " +
                    "AND POLICY.LAST_FAILED_TIME IS NULL";
        } else {
            query = query + " AND (POLICY.LAST_SUCCESS_TIME IS NOT NULL " +
                    "OR POLICY.LAST_FAILED_TIME IS NOT NULL)";
        }

        if (policyId != null) {
            query = query + " AND POLICY.POLICY_ID = ?";
        }

        if (fromDate != null && toDate != null) {
            if (!complianceStatus) {
                query = query + " AND POLICY.LAST_FAILED_TIME BETWEEN ? AND ?";
            } else {
                query = query + " AND POLICY.LAST_SUCCESS_TIME BETWEEN ? AND ?";
            }
        }

        query = query + " LIMIT ?,?";

        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);) {
            int paramIdx = 1;
            stmt.setInt(paramIdx++, tenantId);
            stmt.setBoolean(paramIdx++, complianceStatus);
            if (policyId != null) {
                stmt.setInt(paramIdx++, Integer.parseInt(policyId));
            }
            if (fromDate != null && toDate != null) {
                stmt.setString(paramIdx++, fromDate);
                stmt.setString(paramIdx++, toDate);
            }
            stmt.setInt(paramIdx++, paginationRequest.getStartIndex());
            stmt.setInt(paramIdx, paginationRequest.getRowCount());

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    ComplianceData complianceData = new ComplianceData();
                    complianceData.setId(resultSet.getInt("ID"));
                    complianceData.setDeviceId(resultSet.getInt("DEVICE_ID"));
                    complianceData.setDeviceName(resultSet.getString("NAME"));
                    complianceData.setDeviceType(resultSet.getString("DEVICE_TYPE"));
                    complianceData.setOwner(resultSet.getString("OWNER"));
                    complianceData.setEnrolmentId(resultSet.getInt("ENROLMENT_ID"));
                    complianceData.setPolicyId(resultSet.getInt("POLICY_ID"));
                    complianceData.setPolicyName(resultSet.getString("POLICY_NAME"));
                    complianceData.setStatus(resultSet.getBoolean("STATUS"));
                    complianceData.setAttempts(resultSet.getInt("ATTEMPTS"));
                    complianceData.setLastRequestedTime(resultSet.getTimestamp("LAST_REQUESTED_TIME"));
                    complianceData.setLastFailedTime(resultSet.getTimestamp("LAST_FAILED_TIME"));
                    complianceData.setLastSucceededTime(resultSet.getTimestamp("LAST_SUCCESS_TIME"));
                    complianceDataList.add(complianceData);
                }
            }
        } catch (SQLException e) {
            String msg = "Unable to retrieve compliance data from database.";
            log.error(msg, e);
            throw new MonitoringDAOException(msg, e);
        }
        return complianceDataList;
    }

    @Override
    public List<ComplianceFeature> getNoneComplianceFeatures(int policyComplianceStatusId) throws
            MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<ComplianceFeature> complianceFeatures = new ArrayList<ComplianceFeature>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_POLICY_COMPLIANCE_FEATURES WHERE COMPLIANCE_STATUS_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyComplianceStatusId);
            stmt.setInt(2, tenantId);

            resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                ComplianceFeature feature = new ComplianceFeature();
                feature.setFeatureCode(resultSet.getString("FEATURE_CODE"));
                feature.setMessage(resultSet.getString("STATUS"));
                complianceFeatures.add(feature);
            }
            return complianceFeatures;
        } catch (SQLException e) {
            throw new MonitoringDAOException("Unable to retrieve compliance features data from database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public void deleteNoneComplianceData(int policyComplianceStatusId) throws MonitoringDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_POLICY_COMPLIANCE_FEATURES WHERE COMPLIANCE_STATUS_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, policyComplianceStatusId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new MonitoringDAOException("Unable to delete compliance  data from database.", e);
        } finally {
            PolicyManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    private Connection getConnection() throws MonitoringDAOException {
        return PolicyManagementDAOFactory.getConnection();
    }

}
