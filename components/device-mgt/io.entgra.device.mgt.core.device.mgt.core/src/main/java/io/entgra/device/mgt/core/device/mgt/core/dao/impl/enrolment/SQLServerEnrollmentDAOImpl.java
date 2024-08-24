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
package io.entgra.device.mgt.core.device.mgt.core.dao.impl.enrolment;

import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.AbstractEnrollmentDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class SQLServerEnrollmentDAOImpl extends AbstractEnrollmentDAOImpl {

    public boolean addDeviceStatus(int enrolmentId, EnrolmentInfo.Status status) throws DeviceManagementDAOException {
        Connection conn;
        String changedBy = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (changedBy == null){
            changedBy = DeviceManagementConstants.MaintenanceProperties.MAINTENANCE_USER;
        }
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            // get the device id and last updated status from the device status table
            String sql = "SELECT TOP 1 DEVICE_ID, STATUS FROM DM_DEVICE_STATUS WHERE ENROLMENT_ID = ? ORDER BY UPDATE_TIME DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            ResultSet rs = stmt.executeQuery();
            int deviceId = -1;
            EnrolmentInfo.Status previousStatus = null;
            if (rs.next()) {
                // if there is a record corresponding to the enrolment we save the status and the device id
                previousStatus = EnrolmentInfo.Status.valueOf(rs.getString("STATUS"));
                deviceId = rs.getInt("DEVICE_ID");
            }
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
            // if there was no record for the enrolment or the previous status is not the same as the current status
            // we'll add a record
            if (previousStatus == null || previousStatus != status){
                if (deviceId == -1) {
                    // we need the device id in order to add a new record, therefore we get it from the enrolment table
                    sql = "SELECT DEVICE_ID FROM DM_ENROLMENT WHERE ID = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, enrolmentId);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        deviceId = rs.getInt("DEVICE_ID");
                    } else {
                        // if there were no records corresponding to the enrolment id this is a problem. i.e. enrolment
                        // id is invalid
                        throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment: no record for enrolment id " + enrolmentId);
                    }
                    DeviceManagementDAOUtil.cleanupResources(stmt, null);
                }

                sql = "INSERT INTO DM_DEVICE_STATUS (ENROLMENT_ID, DEVICE_ID, STATUS, UPDATE_TIME, CHANGED_BY) VALUES(?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                Timestamp updateTime = new Timestamp(new Date().getTime());
                stmt.setInt(1, enrolmentId);
                stmt.setInt(2, deviceId);
                stmt.setString(3, status.toString());
                stmt.setTimestamp(4, updateTime);
                stmt.setString(5, changedBy);
                stmt.execute();
            } else {
                // no need to update status since the last recorded status is the same as the current status
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

}
