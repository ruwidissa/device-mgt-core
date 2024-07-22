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
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public List<Device> getDevicesByDeviceIds(PaginationRequest paginationRequest, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        List<Device> devices = new ArrayList<>();
        if (deviceIds == null || deviceIds.isEmpty()) return devices;

        String deviceIdStringList = deviceIds.stream().map(id -> "?").collect(Collectors.joining(","));
        boolean isOwnerProvided = false;
        boolean isDeviceStatusProvided = false;
        boolean isDeviceNameProvided = false;
        try {
            Connection connection = getConnection();
            String sql = "SELECT e.DEVICE_ID, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "e.STATUS, " +
                    "e.OWNER, " +
                    "d.NAME AS DEVICE_NAME, " +
                    "e.DEVICE_TYPE, " +
                    "e.OWNERSHIP, " +
                    "e.DATE_OF_LAST_UPDATE " +
                    "FROM DM_DEVICE d " +
                    "INNER JOIN DM_ENROLMENT e " +
                    "ON d.ID = e.DEVICE_ID " +
                    "WHERE d.TENANT_ID = ? " +
                    "AND e.DEVICE_ID IN (" + deviceIdStringList + ") " +
                    "AND e.STATUS NOT IN ('DELETED', 'REMOVED')";
            if (paginationRequest.getOwner() != null) {
                sql += " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            }
            if (paginationRequest.getDeviceStatus() != null) {
                sql += " AND e.STATUS = ?";
                isDeviceStatusProvided = true;
            }
            if (paginationRequest.getDeviceName() != null) {
                sql += " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql += " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int parameterIdx = 1;
                preparedStatement.setInt(parameterIdx++, tenantId);
                for (Integer deviceId : deviceIds) {
                    preparedStatement.setInt(parameterIdx++, deviceId);
                }
                if (isOwnerProvided) {
                    preparedStatement.setString(parameterIdx++, "%" + paginationRequest.getOwner() + "%");
                }
                if (isDeviceStatusProvided) {
                    preparedStatement.setString(parameterIdx++, paginationRequest.getDeviceStatus());
                }
                if (isDeviceNameProvided) {
                    preparedStatement.setString(parameterIdx++, "%" + paginationRequest.getDeviceName() + "%");
                }
                preparedStatement.setInt(parameterIdx++, paginationRequest.getStartIndex());
                preparedStatement.setInt(parameterIdx, paginationRequest.getRowCount());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Device device = new Device();
                        device.setId(resultSet.getInt("DEVICE_ID"));
                        device.setDeviceIdentifier(resultSet.getString("DEVICE_IDENTIFICATION"));
                        device.setName(resultSet.getString("DEVICE_NAME"));
                        device.setType(resultSet.getString("DEVICE_TYPE"));
                        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
                        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(resultSet.getString("STATUS")));
                        enrolmentInfo.setOwner(resultSet.getString("OWNER"));
                        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(resultSet.getString("OWNERSHIP")));
                        enrolmentInfo.setDateOfLastUpdate(resultSet.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
                        device.setEnrolmentInfo(enrolmentInfo);
                        devices.add(device);
                    }
                }
            }
            return devices;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving devices for device ids in: " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDevicesInGivenIdList(PaginationRequest request, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        List<Integer> filteredDeviceIds = new ArrayList<>();
        if (deviceIds == null || deviceIds.isEmpty()) return filteredDeviceIds;

        String deviceIdStringList = deviceIds.stream().map(id -> "?").collect(Collectors.joining(","));
        try {
            Connection connection = getConnection();
            String sql = "SELECT ID AS DEVICE_ID " +
                    "FROM DM_DEVICE WHERE ID IN " +
                    "(" + deviceIdStringList + ") " +
                    "AND TENANT_ID = ? " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int paraIdx = 1;
                for (Integer deviceId : deviceIds) {
                    preparedStatement.setInt(paraIdx++, deviceId);
                }
                preparedStatement.setInt(paraIdx++, tenantId);
                preparedStatement.setInt(paraIdx++, request.getStartIndex());
                preparedStatement.setInt(paraIdx, request.getRowCount());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        filteredDeviceIds.add(resultSet.getInt("DEVICE_ID"));
                    }
                }
                return filteredDeviceIds;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device ids in: " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDevicesNotInGivenIdList(PaginationRequest request, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        List<Integer> filteredDeviceIds = new ArrayList<>();
        try {
            Connection connection = getConnection();
            String sql = "SELECT ID AS DEVICE_ID " +
                    "FROM DM_DEVICE " +
                    "WHERE TENANT_ID = ?";

            if (deviceIds != null && !deviceIds.isEmpty()) {
                sql += " AND ID NOT IN (" + deviceIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";
            }

            sql += "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int paraIdx = 1;
                preparedStatement.setInt(paraIdx++, tenantId);
                if (deviceIds != null && !deviceIds.isEmpty()) {
                    for (Integer deviceId : deviceIds) {
                        preparedStatement.setInt(paraIdx++, deviceId);
                    }
                }
                preparedStatement.setInt(paraIdx++, request.getStartIndex());
                preparedStatement.setInt(paraIdx, request.getRowCount());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        filteredDeviceIds.add(resultSet.getInt("DEVICE_ID"));
                    }
                }
                return filteredDeviceIds;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device ids not in: " + deviceIds;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }
}
