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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl.device;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * This class holds the generic implementation of DeviceDAO which can be used to support ANSI db syntax.
 */
public class OracleDeviceDAOImpl extends SQLServerDeviceDAOImpl {

    private static final Log log = LogFactory.getLog(OracleDeviceDAOImpl.class);

    @Override
    public void refactorDeviceStatus(Connection conn, List<Device> validDevices) throws DeviceManagementDAOException {
        String updateQuery = "UPDATE DM_DEVICE_STATUS SET STATUS = ? WHERE ID = ?";
        String selectLastMatchingRecordQuery = "SELECT ID FROM DM_DEVICE_STATUS " +
                "WHERE ENROLMENT_ID = ? AND DEVICE_ID = ? ORDER BY ID DESC ROWNUMBER = 1";

        try (PreparedStatement selectStatement = conn.prepareStatement(selectLastMatchingRecordQuery);
             PreparedStatement updateStatement = conn.prepareStatement(updateQuery)) {

            for (Device device : validDevices) {

                selectStatement.setInt(1, device.getEnrolmentInfo().getId());
                selectStatement.setInt(2, device.getId());

                ResultSet resultSet = selectStatement.executeQuery();
                int lastRecordId = 0;
                if (resultSet.next()) {
                    lastRecordId = resultSet.getInt("ID");
                }

                updateStatement.setString(1, String.valueOf(EnrolmentInfo.Status.DELETED));
                updateStatement.setInt(2, lastRecordId);
                updateStatement.execute();
            }

        } catch (SQLException e) {
            String msg = "SQL error occurred while updating device status properties.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

}
