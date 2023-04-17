/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.policy.mgt.core.dao.impl.policy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.policy.mgt.Policy;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.wso2.carbon.policy.mgt.core.dao.PolicyManagerDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GenericPolicyDAOImpl extends AbstractPolicyDAOImpl {

    private static final Log log = LogFactory.getLog(GenericPolicyDAOImpl.class);

    private Connection getConnection() {
        return PolicyManagementDAOFactory.getConnection();
    }

    @Override
    public List<Policy> getAllPolicies(PaginationRequest request) throws PolicyManagerDAOException {
        Connection conn;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            conn = this.getConnection();
            String query = "SELECT * " +
                    "FROM DM_POLICY " +
                    "WHERE TENANT_ID = ? " +
                    "ORDER BY ID LIMIT ?,?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, tenantId);
                stmt.setInt(2, request.getStartIndex());
                stmt.setInt(3, request.getRowCount());
                try (ResultSet resultSet = stmt.executeQuery()) {
                    return this.extractPolicyListFromDbResult(resultSet, tenantId);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while reading the policies from the database.";
            log.error(msg, e);
            throw new PolicyManagerDAOException(msg, e);
        }
    }
}
