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

package io.entgra.device.mgt.core.policy.mgt.core.dao.impl.policy;

import io.entgra.device.mgt.core.device.mgt.common.PolicyPaginationRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.policy.mgt.core.dao.PolicyManagementDAOFactory;
import io.entgra.device.mgt.core.policy.mgt.core.dao.PolicyManagerDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLServerPolicyDAOImpl extends AbstractPolicyDAOImpl {
    private static final Log log = LogFactory.getLog(SQLServerPolicyDAOImpl.class);

    private Connection getConnection() {
        return PolicyManagementDAOFactory.getConnection();
    }

    @Override
    public List<Policy> getAllPolicies(PolicyPaginationRequest request) throws PolicyManagerDAOException {
        Connection conn;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String name = request.getName();
        String type = request.getType();
        String status = request.getStatus();
        String deviceType = request.getDeviceType();
        int statusValue = 0;
        boolean isPolicyNameProvided = false;
        boolean isPolicyTypeProvided = false;
        boolean isPolicyStatusProvided = false;
        boolean isDeviceTypeProvided = false;

        try {
            conn = this.getConnection();
            String query = "SELECT * " +
                    "FROM DM_POLICY P " +
                    "LEFT JOIN DM_PROFILE PR ON P.PROFILE_ID = PR.ID " +
                    "WHERE P.TENANT_ID = ? ";

            if (name != null && !name.isEmpty()) {
                query += "AND P.NAME LIKE ? " ;
                isPolicyNameProvided = true;
            }

            if (type != null && !type.isEmpty()) {
                query += "AND P.POLICY_TYPE = ? " ;
                isPolicyTypeProvided = true;
            }

            if (status != null && !status.isEmpty()) {
                if (status.equals("ACTIVE")) {
                    statusValue = 1;
                }
                query += "AND P.ACTIVE = ? " ;
                isPolicyStatusProvided = true;
            }

            if (deviceType != null && !deviceType.isEmpty()) {
                query += "AND PR.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }

            query += "ORDER BY P.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, tenantId);
                if (isPolicyNameProvided) {
                    stmt.setString(paramIdx++, "%" + name + "%");
                }
                if (isPolicyTypeProvided) {
                    stmt.setString(paramIdx++, type);
                }
                if (isPolicyStatusProvided) {
                    stmt.setInt(paramIdx++, statusValue);
                }
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                stmt.setInt(paramIdx++, request.getStartIndex());
                stmt.setInt(paramIdx++, request.getRowCount());
                try (ResultSet resultSet = stmt.executeQuery()) {
                    return this.extractPolicyListWithProfileFromDbResult(resultSet, tenantId);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        }
    }
}
