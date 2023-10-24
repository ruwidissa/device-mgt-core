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

package io.entgra.device.mgt.core.application.mgt.core.dao.impl.vpp;

import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

public class OracleVppApplicationDAOImpl extends GenericVppApplicationDAOImpl  {

    private static final Log log = LogFactory.getLog(GenericVppApplicationDAOImpl.class);

    @Override
    public int addVppUser(VppUserDTO userDTO, int tenantId)
            throws ApplicationManagementDAOException {
        int vppUserId = -1;
        String sql = "INSERT INTO "
                + "AP_VPP_USER("
                + "CLIENT_USER_ID, "
                + "DM_USERNAME, "
                + "TENANT_ID, "
                + "EMAIL, "
                + "INVITE_CODE, "
                + "STATUS,"
                + "CREATED_TIME,"
                + "LAST_UPDATED_TIME,"
                + "MANAGED_ID,"
                + "TEMP_PASSWORD) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, new String[] {"ID"})) {
                long currentTime = System.currentTimeMillis();
                stmt.setString(1, userDTO.getClientUserId());
                stmt.setString(2, userDTO.getDmUsername());
                stmt.setInt(3, tenantId);
                stmt.setString(4, userDTO.getEmail());
                stmt.setString(5, userDTO.getInviteCode());
                stmt.setString(6, userDTO.getStatus());
                stmt.setLong(7, currentTime);
                stmt.setLong(8, currentTime);
                stmt.setString(9, userDTO.getManagedId());
                stmt.setString(10, userDTO.getTmpPassword());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        vppUserId = rs.getInt(1);
                    }
                }
                return vppUserId;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining database connection when adding the vpp user";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to add  the vpp user.";
            log.error(msg, e);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}
