/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.core.dao.impl.application.release;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This handles Application Release operations which are specific to MSSQL.
 */
public class SQLServerApplicationReleaseDAOImpl extends GenericApplicationReleaseDAOImpl {
    private static final Log log = LogFactory.getLog(GenericApplicationReleaseDAOImpl.class);

    public boolean isActiveReleaseExisitForPackageName(String packageName, int tenantId, String inactiveState)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Verifying application release existence for package name:" + packageName);
        }
        String sql = "SELECT AR.ID AS RELEASE_ID "
                + "FROM AP_APP_RELEASE AS AR "
                + "WHERE AR.PACKAGE_NAME = ? AND "
                + "AR.CURRENT_STATE != ? AND "
                + "AR.TENANT_ID = ? ORDER BY AR.ID OFFSET 0 ROWS FETCH NEXT 1 ROWS ONLY;";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, packageName);
                stmt.setString(2, inactiveState);
                stmt.setInt(3, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to verify the existence of package name for "
                    + "active application release. Package name: " + packageName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL error occurred while verifying the existence of package name for active application "
                    + "release. package name: " + packageName;
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
