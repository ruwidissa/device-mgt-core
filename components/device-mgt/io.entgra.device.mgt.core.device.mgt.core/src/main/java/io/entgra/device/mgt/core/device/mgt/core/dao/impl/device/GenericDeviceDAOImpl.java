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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.Count;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceInfo;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.AbstractDeviceDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class holds the generic implementation of DeviceDAO which can be used to support ANSI db syntax.
 */
public class GenericDeviceDAOImpl extends AbstractDeviceDAOImpl {

    private static final Log log = LogFactory.getLog(GenericDeviceDAOImpl.class);

    @Override
    public List<Device> getDevices(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        List<Device> devices;
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;
        String serial = request.getSerialNumber();
        boolean isSerialProvided = false;

        try {
            Connection conn = getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, " +
                    "d1.DESCRIPTION, " +
                    "d1.NAME AS DEVICE_NAME, " +
                    "e.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, " +
                    "d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.OWNER, " +
                    "e.OWNERSHIP, " +
                    "e.STATUS, " +
                    "e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, " +
                    "e.ID AS ENROLMENT_ID " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, " +
                    "d.DESCRIPTION, " +
                    "d.NAME, " +
                    "d.LAST_UPDATED_TIMESTAMP, " +
                    "d.DEVICE_IDENTIFICATION ";

            //Filter by serial number or any Custom Property in DM_DEVICE_INFO
            if ((serial != null) || (request.getCustomProperty() != null && !request.getCustomProperty().isEmpty())) {
                sql = sql +
                        "FROM DM_DEVICE d WHERE ";
                if (serial != null) {
                    sql += "EXISTS (" +
                            "SELECT VALUE_FIELD " +
                            "FROM DM_DEVICE_INFO di " +
                            "WHERE di.DEVICE_ID = d.ID " +
                            "AND di.KEY_FIELD = 'serial' " +
                            "AND di.VALUE_FIELD LIKE ? ) ";
                    isSerialProvided = true;
                }
                if (!request.getCustomProperty().isEmpty()) {
                    if (serial != null) {
                        sql += "AND ";
                    }
                    boolean firstCondition = true;
                    for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                        if (!firstCondition) {
                            sql += "AND ";
                        }
                        sql += "EXISTS (" +
                                "SELECT VALUE_FIELD " +
                                "FROM DM_DEVICE_INFO di " +
                                "WHERE di.DEVICE_ID = d.ID " +
                                "AND di.KEY_FIELD = '" + entry.getKey() + "' " +
                                "AND di.VALUE_FIELD LIKE ? ) ";
                        firstCondition = false;
                    }
                }
                sql += "AND d.TENANT_ID = ? ";
            } else {
                sql = sql + "FROM DM_DEVICE d WHERE d.TENANT_ID = ? ";
            }
            //Add query for last updated timestamp
            if (since != null) {
                sql = sql + " AND d.LAST_UPDATED_TIMESTAMP > ?";
                isSinceProvided = true;
            }
            //Add the query for device-name
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql = sql + ") d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";
            //Add the query for device-type
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND e.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }
            //Add the query for ownership
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }
            sql = sql + " LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                if (isSerialProvided) {
                    stmt.setString(paramIdx++, "%" + serial + "%");
                }
                if (request.getCustomProperty() != null && !request.getCustomProperty().isEmpty()) {
                    for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                        stmt.setString(paramIdx++, "%" + entry.getValue() + "%");
                    }
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isSinceProvided) {
                    stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
                }
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, "%" + deviceName + "%");
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, ownership);
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, "%" + owner + "%");
                } else if (isOwnerPatternProvided) {
                    stmt.setString(paramIdx++, ownerPattern + "%");
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }
                stmt.setInt(paramIdx++, request.getRowCount());
                stmt.setInt(paramIdx, request.getStartIndex());

                try (ResultSet rs = stmt.executeQuery()) {
                    devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered devices";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getNonRemovedYearlyDeviceList(int tenantId,  Timestamp startDate, Timestamp endDate)
            throws DeviceManagementDAOException {
        List<Device> devices = new ArrayList<>();
        try {
            Connection conn = getConnection();
            String sql = "SELECT d.ID AS DEVICE_ID, " +
                    "DEVICE_IDENTIFICATION, " +
                    "DESCRIPTION, " +
                    "NAME, " +
                    "DATE_OF_ENROLMENT, " +
                    "LAST_UPDATED_TIMESTAMP, " +
                    "STATUS, " +
                    "DATE_OF_LAST_UPDATE, " +
                    "TIMESTAMPDIFF(DAY, ?, DATE_OF_ENROLMENT) as DAYS_SINCE_ENROLLED " +
                    "FROM DM_DEVICE d, DM_ENROLMENT e " +
                    "WHERE " +
                    "e.TENANT_ID=? AND " +
                    "d.ID=e.DEVICE_ID AND " +
                    "STATUS NOT IN ('REMOVED', 'DELETED') AND " +
                    "(" +
                    "DATE_OF_ENROLMENT BETWEEN ? AND ? " +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.valueOf(endDate));
                stmt.setInt(2, tenantId);
                stmt.setString(3, String.valueOf(startDate));
                stmt.setString(4, String.valueOf(endDate));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    devices.add(DeviceManagementDAOUtil.loadDeviceBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of NonRemovedYearly device billing ", e);
        }
        return devices;
    }

    @Override
    public List<Device> getRemovedYearlyDeviceList(int tenantId,  Timestamp startDate, Timestamp endDate)
            throws DeviceManagementDAOException {
        Connection conn;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "select d.ID AS DEVICE_ID, " +
                    "DEVICE_IDENTIFICATION, " +
                    "DESCRIPTION, " +
                    "NAME, " +
                    "DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, " +
                    "d1.LAST_UPDATED_TIMESTAMP, " +
                    "STATUS, " +
                    "TIMESTAMPDIFF(DAY, DATE_OF_LAST_UPDATE, DATE_OF_ENROLMENT) AS DAYS_USED " +
                    "from DM_DEVICE d, DM_ENROLMENT e " +
                    "where " +
                    "e.TENANT_ID=? and d.ID=e.DEVICE_ID and " +
                    "(" +
                    "STATUS = 'REMOVED' OR STATUS = 'DELETED' " +
                    ") and "  +
                    "(" +
                    "DATE_OF_ENROLMENT between ? and ? " +
                    ") and " +
                    "(" +
                    "DATE_OF_LAST_UPDATE >= ? " +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                stmt.setString(2, String.valueOf(startDate));
                stmt.setString(3, String.valueOf(endDate));
                stmt.setString(4, String.valueOf(startDate));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    devices.add(DeviceManagementDAOUtil.loadDeviceBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of RemovedYearly device billing ", e);
        }
        return devices;
    }

    @Override
    public List<Device> getNonRemovedPriorYearsDeviceList(int tenantId,  Timestamp startDate, Timestamp endDate)
            throws DeviceManagementDAOException {
        Connection conn;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "select d.ID AS DEVICE_ID, " +
                    "DEVICE_IDENTIFICATION, " +
                    "DESCRIPTION, " +
                    "NAME, " +
                    "DATE_OF_ENROLMENT, " +
                    "LAST_UPDATED_TIMESTAMP, " +
                    "STATUS, " +
                    "DATE_OF_LAST_UPDATE, " +
                    "TIMESTAMPDIFF(DAY, ?, ?) as DAYS_SINCE_ENROLLED " +
                    "from DM_DEVICE d, DM_ENROLMENT e " +
                    "where " +
                    "e.TENANT_ID=? and " +
                    "d.ID=e.DEVICE_ID and " +
                    "STATUS NOT IN ('REMOVED', 'DELETED') and " +
                    "(" +
                    "DATE_OF_ENROLMENT < ? " +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.valueOf(endDate));
                stmt.setString(2, String.valueOf(startDate));
                stmt.setInt(3, tenantId);
                stmt.setString(4, String.valueOf(startDate));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    devices.add(DeviceManagementDAOUtil.loadDeviceBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of NonRemovedPriorYears device billing ", e);
        }
        return devices;
    }

    @Override
    public List<Device> getRemovedPriorYearsDeviceList(int tenantId,  Timestamp startDate, Timestamp endDate)
            throws DeviceManagementDAOException {
        Connection conn;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "select d.ID AS DEVICE_ID, " +
                    "DEVICE_IDENTIFICATION, " +
                    "DESCRIPTION, " +
                    "NAME, " +
                    "DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, " +
                    "LAST_UPDATED_TIMESTAMP, " +
                    "STATUS, " +
                    "TIMESTAMPDIFF(DAY, DATE_OF_LAST_UPDATE,  ?) AS DAYS_USED " +
                    "from DM_DEVICE d, DM_ENROLMENT e " +
                    "where " +
                    "e.TENANT_ID=? and d.ID=e.DEVICE_ID and " +
                    "(" +
                    "STATUS = 'REMOVED' OR STATUS = 'DELETED' " +
                    ") and "  +
                    "(" +
                    "DATE_OF_ENROLMENT < ? " +
                    ") and " +
                    "(" +
                    "DATE_OF_LAST_UPDATE >= ? " +
                    ")";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, String.valueOf(startDate));
                stmt.setInt(2, tenantId);
                stmt.setString(3, String.valueOf(startDate));
                stmt.setString(4, String.valueOf(startDate));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    devices.add(DeviceManagementDAOUtil.loadDeviceBilling(rs));
                }
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of RemovedPriorYears device billing ", e);
        }
        return devices;
    }

    //Return only not removed id list
    @Override
    public List<Device> getDeviceListWithoutPagination(int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT " +
                    "DM_DEVICE.ID AS DEVICE_ID, " +
                    "DEVICE_IDENTIFICATION, " +
                    "DESCRIPTION, " +
                    "LAST_UPDATED_TIMESTAMP, " +
                    "DM_DEVICE.NAME AS DEVICE_NAME, " +
                    "DEVICE_TYPE, " +
                    "DM_ENROLMENT.ID AS ENROLMENT_ID, " +
                    "DATE_OF_ENROLMENT, " +
                    "OWNER, " +
                    "OWNERSHIP, " +
                    "IS_TRANSFERRED, " +
                    "STATUS, " +
                    "DATE_OF_LAST_UPDATE, " +
                    "TIMESTAMPDIFF(DAY, DATE_OF_ENROLMENT, CURDATE()) as DAYS_SINCE_ENROLLED " +
                    "FROM " +
                    "DM_DEVICE " +
                    "JOIN DM_ENROLMENT ON (DM_DEVICE.ID = DM_ENROLMENT.DEVICE_ID) " +
                    "WHERE " +
                    "DM_ENROLMENT.TENANT_ID = ? ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of device billing ", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesIds(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        List<Device> devices = null;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;

        try {
            conn = getConnection();
            String sql = "SELECT " +
                    "d1.ID AS DEVICE_ID, " +
                    "d1.DEVICE_IDENTIFICATION, " +
                    "d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.STATUS, " +
                    "e.OWNER, " +
                    "e.IS_TRANSFERRED, " +
                    "e.ID AS ENROLMENT_ID " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT d.ID, " +
                    "d.LAST_UPDATED_TIMESTAMP, " +
                    "d.DEVICE_IDENTIFICATION " +
                    "FROM DM_DEVICE d WHERE d.TENANT_ID = ?) d1 " +
                    "WHERE d1.ID = e.DEVICE_ID AND e.TENANT_ID = ? ";
            //Add the query for ownership
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER = ?";
                isOwnerProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, tenantId);
                stmt.setInt(paramIdx++, tenantId);
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, ownership);
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, owner);
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDeviceIds(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered devices";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getAllocatedDevices(PaginationRequest request, int tenantId,
                                            int activeServerCount, int serverIndex)
            throws DeviceManagementDAOException {
        List<Device> devices;
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;
        boolean isPartitionedTask = false;

        try {
            Connection conn = getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, " +
                         "d1.DESCRIPTION, " +
                         "d1.NAME AS DEVICE_NAME, " +
                         "e.DEVICE_TYPE, " +
                         "d1.DEVICE_IDENTIFICATION, " +
                         "d1.LAST_UPDATED_TIMESTAMP, " +
                         "e.OWNER, " +
                         "e.OWNERSHIP, " +
                         "e.STATUS, " +
                         "e.IS_TRANSFERRED, " +
                         "e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, " +
                         "e.ID AS ENROLMENT_ID " +
                         "FROM DM_ENROLMENT e, " +
                         "(SELECT d.ID, " +
                         "d.DESCRIPTION, " +
                         "d.NAME, " +
                         "d.DEVICE_IDENTIFICATION, " +
                         "d.LAST_UPDATED_TIMESTAMP " +
                         "FROM DM_DEVICE d ";
            //Add the query to filter active devices on timestamp
            if (since != null) {
                sql = sql + ", DM_DEVICE_DETAIL dt";
                isSinceProvided = true;
            }
            sql = sql + " WHERE d.TENANT_ID = ?";
            //Add query for last updated timestamp
            if (isSinceProvided) {
                sql = sql + " AND dt.DEVICE_ID = d.ID AND dt.UPDATE_TIMESTAMP > ?";
            }
            //Add the query for device-name
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql = sql + ") d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";
            //Add the query for device-type
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND e.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }
            //Add the query for ownership
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER = ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }
            if (activeServerCount > 0){
                sql = sql + " AND MOD(d1.ID, ?) = ?";
                isPartitionedTask = true;
            }
            sql = sql + " LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, tenantId);
                if (isSinceProvided) {
                    stmt.setLong(paramIdx++, since.getTime());
                }
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, deviceName + "%");
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, ownership);
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, owner);
                } else if (isOwnerPatternProvided) {
                    stmt.setString(paramIdx++, ownerPattern + "%");
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }
                if (isPartitionedTask) {
                    stmt.setInt(paramIdx++, activeServerCount);
                    stmt.setInt(paramIdx++, serverIndex);
                }
                stmt.setInt(paramIdx++, request.getRowCount());
                stmt.setInt(paramIdx, request.getStartIndex());

                try (ResultSet rs = stmt.executeQuery()) {
                    devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                         "registered devices";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> searchDevicesInGroup(PaginationRequest request, int tenantId) throws DeviceManagementDAOException {
        List<Device> devices = null;
        int groupId = request.getGroupId();
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;
        String serial = request.getSerialNumber();
        boolean isSerialProvided = false;

        try {
            Connection conn = getConnection();
            String sql = "SELECT d1.DEVICE_ID, " +
                    "d1.DESCRIPTION, " +
                    "d1.NAME AS DEVICE_NAME, " +
                    "e.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, " +
                    "d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.OWNER, " +
                    "e.OWNERSHIP, " +
                    "e.STATUS, " +
                    "e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, " +
                    "e.ID AS ENROLMENT_ID " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT gd.DEVICE_ID, " +
                    "gd.DESCRIPTION, " +
                    "gd.NAME, " +
                    "gd.DEVICE_IDENTIFICATION, " +
                    "gd.LAST_UPDATED_TIMESTAMP " +
                    "FROM " +
                    "(SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION,  " +
                    "d.NAME, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d, " +
                    "(SELECT dgm.DEVICE_ID " +
                    "FROM DM_DEVICE_GROUP_MAP dgm " +
                    "WHERE  dgm.GROUP_ID = ?) dgm1 " +
                    "WHERE d.ID = dgm1.DEVICE_ID " +
                    "AND d.TENANT_ID = ?";
            //Add the query for device-name
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql = sql + ") gd";
            sql = sql + " WHERE 1 = 1";
            //Add query for last updated timestamp
            if (since != null) {
                sql = sql + " AND gd.LAST_UPDATED_TIMESTAMP > ?";
                isSinceProvided = true;
            }
            sql = sql + " ) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND e.TENANT_ID = ? ";
            //Add the query for device-type
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND e.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }
            //Add the query for ownership
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }
            //Filter Group with serial number or any Custom Property in DM_DEVICE_INFO
            if (serial != null || !request.getCustomProperty().isEmpty()) {
                if (serial != null) {
                    sql += "AND EXISTS (" +
                            "SELECT VALUE_FIELD " +
                            "FROM DM_DEVICE_INFO di " +
                            "WHERE di.DEVICE_ID = d1.DEVICE_ID " +
                            "AND di.KEY_FIELD = 'serial' " +
                            "AND di.VALUE_FIELD LIKE ?) ";
                    isSerialProvided = true;
                }
                if (!request.getCustomProperty().isEmpty()) {
                    for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                        sql += "AND EXISTS (" +
                                "SELECT VALUE_FIELD " +
                                "FROM DM_DEVICE_INFO di2 " +
                                "WHERE di2.DEVICE_ID = d1.DEVICE_ID " +
                                "AND di2.KEY_FIELD = '" + entry.getKey() + "' " +
                                "AND di2.VALUE_FIELD LIKE ?)";
                    }
                }
            }
            sql = sql + " LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, groupId);
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, "%" + deviceName + "%");
                }
                if (isSinceProvided) {
                    stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, ownership);
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, "%" + owner + "%");
                } else if (isOwnerPatternProvided) {
                    stmt.setString(paramIdx++, ownerPattern + "%");
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }
                if (isSerialProvided) {
                    stmt.setString(paramIdx++, "%" + serial + "%");
                }
                if (!request.getCustomProperty().isEmpty()) {
                    for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                        stmt.setString(paramIdx++, "%" + entry.getValue() + "%");
                    }
                }
                stmt.setInt(paramIdx++, request.getRowCount());
                stmt.setInt(paramIdx, request.getStartIndex());

                try (ResultSet rs = stmt.executeQuery()) {
                    devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of" +
                    " devices belonging to group : " + groupId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getDevicesOfUser(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.IS_TRANSFERRED, " +
                    "e1.DATE_OF_LAST_UPDATE, d.LAST_UPDATED_TIMESTAMP, " +
                    "e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DEVICE_IDENTIFICATION, " +
                    "e1.DEVICE_TYPE FROM DM_DEVICE d, (SELECT e.OWNER, e.OWNERSHIP, e.ID AS ENROLMENT_ID, e.DEVICE_TYPE, " +
                    "e.DEVICE_ID, e.STATUS, e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e WHERE " +
                    "e.TENANT_ID = ? AND e.OWNER = ?) e1 WHERE d.ID = e1.DEVICE_ID LIMIT ? OFFSET ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, request.getOwner());
            stmt.setInt(3, request.getRowCount());
            stmt.setInt(4, request.getStartIndex());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices belongs to '" +
                    request.getOwner() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesByName(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, e.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, d1.LAST_UPDATED_TIMESTAMP, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                    "e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.NAME, " +
                    "d.DESCRIPTION, d.LAST_UPDATED_TIMESTAMP, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d " +
                    "WHERE d.NAME LIKE ? AND d.TENANT_ID = ?) d1 " +
                    "WHERE DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? LIMIT ? OFFSET ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getDeviceName() + "%");
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            stmt.setInt(4, request.getRowCount());
            stmt.setInt(5, request.getStartIndex());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches " +
                    "'" + request.getDeviceName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesByOwnership(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, e.DEVICE_TYPE, " +
                    "d.DEVICE_IDENTIFICATION, d.LAST_UPDATED_TIMESTAMP, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                    "e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM (SELECT e.ID, e.DEVICE_ID, e.OWNER, " +
                    "e.OWNERSHIP, e.STATUS, e.IS_TRANSFERRED, e.DEVICE_TYPE, " +
                    "e.DATE_OF_ENROLMENT, e.DATE_OF_LAST_UPDATE, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e " +
                    "WHERE TENANT_ID = ? AND OWNERSHIP = ?) e, DM_DEVICE d " +
                    "WHERE DEVICE_ID = e.DEVICE_ID AND d.TENANT_ID = ? LIMIT ? OFFSET ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, request.getOwnership());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, request.getRowCount());
            stmt.setInt(5, request.getStartIndex());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches to ownership " +
                    "'" + request.getOwnership() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesByStatus(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        List<Device> devices = new ArrayList<>();
        List<String> statusList = request.getStatusList();

        try {
            Connection conn = getConnection();
            String sql = "SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION, " +
                    "d.NAME AS DEVICE_NAME, " +
                    "e.DEVICE_TYPE, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "d.LAST_UPDATED_TIMESTAMP, " +
                    "e.OWNER, " +
                    "e.OWNERSHIP, " +
                    "e.STATUS, " +
                    "e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, " +
                    "e.ID AS ENROLMENT_ID " +
                    "FROM " +
                    "(SELECT e.ID, " +
                    "e.DEVICE_ID, " +
                    "e.OWNER, " +
                    "e.OWNERSHIP, " +
                    "e.STATUS, " +
                    "e.IS_TRANSFERRED, " +
                    "e.DATE_OF_ENROLMENT, " +
                    "e.DATE_OF_LAST_UPDATE, " +
                    "e.DEVICE_TYPE, " +
                    "e.ID AS ENROLMENT_ID " +
                    "FROM DM_ENROLMENT e " +
                    "WHERE TENANT_ID = ?";
            if (statusList == null || statusList.isEmpty()) {
                String msg = "Error occurred while fetching the list of devices. Status List can't " +
                        "be null or empty";
                log.error(msg);
                throw new DeviceManagementDAOException(msg);
            }
            sql += buildStatusQuery(statusList);
            sql += ") e, " +
                    "DM_DEVICE d " +
                    "WHERE DEVICE_ID = e.DEVICE_ID " +
                    "AND d.TENANT_ID = ? " +
                    "LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, tenantId);
                for (String status : statusList) {
                    stmt.setString(paramIdx++, status);
                }
                stmt.setInt(paramIdx++, tenantId);
                stmt.setInt(paramIdx++, request.getRowCount());
                stmt.setInt(paramIdx, request.getStartIndex());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices that matches to status " +
                    request.getStatusList().toString();
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getDevicesByDuration(PaginationRequest request, int tenantId,
                                             String fromDate, String toDate)
            throws DeviceManagementDAOException {
        List<Device> devices;
        String ownership = request.getOwnership();
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;

        String sql = "SELECT " +
                "d.ID AS DEVICE_ID, " +
                "d.DESCRIPTION,d.NAME AS DEVICE_NAME, " +
                "d.LAST_UPDATED_TIMESTAMP, " +
                "e.DEVICE_TYPE, " +
                "e.DEVICE_IDENTIFICATION, " +
                "e.OWNER, " +
                "e.OWNERSHIP, " +
                "e.STATUS, " +
                "e.IS_TRANSFERRED, " +
                "e.DATE_OF_LAST_UPDATE," +
                "e.DATE_OF_ENROLMENT, " +
                "e.ID AS ENROLMENT_ID " +
                "FROM DM_DEVICE AS d, DM_ENROLMENT AS e " +
                "WHERE d.ID = e.DEVICE_ID AND " +
                "e.TENANT_ID = ? AND " +
                "e.DATE_OF_ENROLMENT BETWEEN ? AND ?";
        if (statusList != null && !statusList.isEmpty()) {
            sql += buildStatusQuery(statusList);
            isStatusProvided = true;
        }
        if (ownership != null) {
            sql = sql + " AND e.OWNERSHIP = ?";
        }
        sql = sql + " LIMIT ? OFFSET ?";

        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIdx = 1;
            stmt.setInt(paramIdx++, tenantId);
            stmt.setString(paramIdx++, fromDate);
            stmt.setString(paramIdx++, toDate);
            if (isStatusProvided) {
                for (String status : statusList) {
                    stmt.setString(paramIdx++, status);
                }
            }
            if (ownership != null) {
                stmt.setString(paramIdx++, ownership);
            }
            stmt.setInt(paramIdx++, request.getRowCount());
            stmt.setInt(paramIdx, request.getStartIndex());

            try (ResultSet rs = stmt.executeQuery()) {
                devices = new ArrayList<>();
                while (rs.next()) {
                    Device device = DeviceManagementDAOUtil.loadDevice(rs);
                    devices.add(device);
                }
                return devices;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered devices under tenant id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getDevicesByDurationCount(
            List<String> statusList, String ownership, String fromDate, String toDate, int tenantId)
            throws DeviceManagementDAOException {
        int deviceCount = 0;
        boolean isStatusProvided = false;

        String sql = "SELECT " +
                "COUNT(d.ID) AS DEVICE_COUNT " +
                "FROM DM_DEVICE AS d, " +
                "DM_ENROLMENT AS e " +
                "WHERE d.ID = e.DEVICE_ID " +
                "AND e.TENANT_ID = ? " +
                "AND e.DATE_OF_ENROLMENT BETWEEN ? AND ?";
        if (statusList != null && !statusList.isEmpty()) {
            sql += buildStatusQuery(statusList);
            isStatusProvided = true;
        }
        if (ownership != null) {
            sql = sql + " AND e.OWNERSHIP = ?";
        }

        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIdx = 1;
            stmt.setInt(paramIdx++, tenantId);
            stmt.setString(paramIdx++, fromDate);
            stmt.setString(paramIdx++, toDate);
            if (isStatusProvided) {
                for (String status : statusList) {
                    stmt.setString(paramIdx++, status);
                }
            }
            if (ownership != null) {
                stmt.setString(paramIdx, ownership);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    deviceCount = rs.getInt("DEVICE_COUNT");
                }
                return deviceCount;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered devices under tenant id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Count> getCountOfDevicesByDuration(PaginationRequest request, List<String> statusList, int tenantId,
                                                   String fromDate, String toDate)
            throws DeviceManagementDAOException {
        List<Count> countList = new ArrayList<>();
        String ownership = request.getOwnership();
        boolean isStatusProvided = false;

        String sql =
                "SELECT " +
                        "SUBSTRING(e.DATE_OF_ENROLMENT, 1, 10) AS ENROLMENT_DATE, " +
                        "COUNT(SUBSTRING(e.DATE_OF_ENROLMENT, 1, 10)) AS ENROLMENT_COUNT " +
                        "FROM DM_DEVICE AS d " +
                        "INNER JOIN DM_ENROLMENT AS e ON d.ID = e.DEVICE_ID " +
                        "AND e.TENANT_ID = ? " +
                        "AND e.DATE_OF_ENROLMENT " +
                        "BETWEEN ? AND ? ";

        //Add the query for status
        if (statusList != null && !statusList.isEmpty()) {
            sql += buildStatusQuery(statusList);
            isStatusProvided = true;
        }

        if (ownership != null) {
            sql = sql + " AND e.OWNERSHIP = ?";
        }

        sql = sql + " GROUP BY SUBSTRING(e.DATE_OF_ENROLMENT, 1, 10) LIMIT ?, ?";

        try {
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, tenantId);
                stmt.setString(paramIdx++, fromDate);
                stmt.setString(paramIdx++, toDate);
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }
                if (ownership != null) {
                    stmt.setString(paramIdx++, ownership);
                }
                stmt.setInt(paramIdx++, request.getRowCount());
                stmt.setInt(paramIdx, request.getStartIndex());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Count count = new Count(
                                rs.getString("ENROLMENT_DATE"),
                                rs.getInt("ENROLMENT_COUNT")
                        );
                        countList.add(count);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                    "registered devices under tenant id " + tenantId + " between " + fromDate + " to " + toDate;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }

        return countList;
    }

    /**
     * Get the list of devices that matches with the given device name and (or) device type.
     *
     * @param deviceName Name of the device.
     * @param tenantId   Id of the current tenant
     * @return device list
     * @throws DeviceManagementDAOException
     */
    @Override
    public List<Device> getDevicesByNameAndType(String deviceName, String type, int tenantId, int offset, int limit)
            throws DeviceManagementDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, e.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, d1.LAST_UPDATED_TIMESTAMP, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                    "e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.NAME, " +
                    "d.DESCRIPTION, d.DEVICE_IDENTIFICATION, d.LAST_UPDATED_TIMESTAMP FROM DM_DEVICE d " +
                    "WHERE d.TENANT_ID = ?";

            if (deviceName != null && !deviceName.isEmpty()) {
                sql += " AND d.NAME LIKE ? ";
            }
            sql += ") d1 WHERE d1.ID = e.DEVICE_ID";
            if (type != null && !type.isEmpty()) {
                sql += " AND e.DEVICE_TYPE = ?";
            }
            sql+=" LIMIT ? OFFSET ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);

            int i = 1;

            if (deviceName != null && !deviceName.isEmpty()) {
                stmt.setString(++i, deviceName + "%");
            }

            if (type != null && !type.isEmpty()) {
                stmt.setString(++i, type);
            }

            stmt.setInt(++i, limit);
            stmt.setInt(++i, offset);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices corresponding" +
                    "to the mentioned filtering criteria";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> getSubscribedDevices(PaginationRequest request, List<Integer> deviceIds, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        int limitValue = request.getRowCount();
        int offsetValue = request.getStartIndex();
        List<String> status = request.getStatusList();
        String name = request.getDeviceName();
        String user = request.getOwner();
        String ownership = request.getOwnership();
        String serial = request.getSerialNumber();
        String query = null;
        try {
            List<Device> devices = new ArrayList<>();
            if (deviceIds.isEmpty()) {
                return devices;
            }
            conn = this.getConnection();
            int index = 1;

            boolean isStatusProvided = false;
            boolean isDeviceNameProvided = false;
            boolean isOwnerProvided = false;
            boolean isOwnershipProvided = false;
            boolean isSerialProvided = false;

            query = "SELECT "
                    + "DM_DEVICE.ID AS DEVICE_ID, "
                    + "DM_DEVICE.NAME AS DEVICE_NAME, "
                    + "DM_DEVICE.DESCRIPTION AS DESCRIPTION, "
                    + "DM_DEVICE.LAST_UPDATED_TIMESTAMP, "
                    + "DM_DEVICE.DEVICE_IDENTIFICATION AS DEVICE_IDENTIFICATION, "
                    + "e.ID AS ENROLMENT_ID, "
                    + "e.OWNER, "
                    + "e.OWNERSHIP, "
                    + "e.DATE_OF_ENROLMENT, "
                    + "e.DATE_OF_LAST_UPDATE, "
                    + "e.STATUS, "
                    + "e.DEVICE_TYPE, "
                    + "e.IS_TRANSFERRED "
                    + "FROM DM_DEVICE "
                    + "INNER JOIN DM_ENROLMENT e ON "
                    + "DM_DEVICE.ID = e.DEVICE_ID AND "
                    + "DM_DEVICE.TENANT_ID = e.TENANT_ID ";

            if (null != serial && !serial.isEmpty()) { // Only if serial is provided, join with device info table
                query = query.concat("INNER JOIN DM_DEVICE_INFO i ON "
                        + "DM_DEVICE.ID = i.DEVICE_ID "
                        + "AND i.KEY_FIELD = 'serial' ");
                isSerialProvided = true;
            }
            query = query.concat("WHERE DM_DEVICE.ID IN (");
            StringJoiner joiner = new StringJoiner(",", query ,
                    ") AND DM_DEVICE.TENANT_ID = ? AND e.STATUS != ? AND e.STATUS != ?");
            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            query = joiner.toString();

            if (name != null && !name.isEmpty()) {
                query += " AND DM_DEVICE.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            if (ownership != null && !ownership.isEmpty()) {
                query += " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            if (isSerialProvided) {
                query += " AND i.VALUE_FIELD LIKE ?" ;
            }
            if (user != null && !user.isEmpty()) {
                query += " AND e.OWNER = ?";
                isOwnerProvided = true;
            }
            if (status != null && !status.isEmpty()) {
                query += buildStatusQuery(status);
                isStatusProvided = true;
            }
            // Loop through custom properties and add conditions
            if (!request.getCustomProperty().isEmpty()) {
                for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                    query += " AND EXISTS (" +
                            "SELECT VALUE_FIELD " +
                            "FROM DM_DEVICE_INFO di2 " +
                            "WHERE di2.DEVICE_ID = DM_DEVICE.ID " +
                            "AND di2.KEY_FIELD = '" + entry.getKey() + "' " +
                            "AND di2.VALUE_FIELD LIKE ?)";
                }
            }

            query = query + " LIMIT ? OFFSET ?";

            try (PreparedStatement ps = conn.prepareStatement(query)) {

                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }
                ps.setInt(index++, tenantId);
                ps.setString(index++, EnrolmentInfo.Status.REMOVED.toString());
                ps.setString(index++, EnrolmentInfo.Status.DELETED.toString());
                if (isDeviceNameProvided) {
                    ps.setString(index++, name + "%");
                }
                if (isOwnershipProvided) {
                    ps.setString(index++, ownership);
                }
                if (isSerialProvided) {
                    ps.setString(index++, "%" + serial + "%");
                }
                if (isOwnerProvided) {
                    ps.setString(index++, user);
                }
                if (isStatusProvided) {
                    for (String deviceStatus : status) {
                        ps.setString(index++, deviceStatus);
                    }
                }
                // Set custom property values in the loop
                if (!request.getCustomProperty().isEmpty()) {
                    for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                        ps.setString(index++, "%" + entry.getValue() + "%");
                    }
                }
                ps.setInt(index++, limitValue);
                ps.setInt(index, offsetValue);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        devices.add(DeviceManagementDAOUtil.loadDevice(rs));
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all registered devices " +
                    "according to device ids and the limit area. Executed query " + query;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getSubscribedDeviceCount(List<Integer> deviceIds, int tenantId, List<String> status)
            throws DeviceManagementDAOException {
        try {
            if (deviceIds.isEmpty()) {
                return 0;
            }
            Connection conn = this.getConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT " +
                            "COUNT(e.DEVICE_ID) AS DEVICE_ID " +
                            "FROM DM_ENROLMENT AS e, DM_DEVICE AS f " +
                            "WHERE " +
                            "e.DEVICE_ID=f.ID AND " +
                            "e.DEVICE_ID IN (", ") AND e.TENANT_ID = ?");

            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();

            if (status != null && !status.isEmpty()) {
                query += buildStatusQuery(status);
            }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }

                ps.setInt(index++, tenantId);
                if (status != null && !status.isEmpty()) {
                    for (String deviceStatus : status) {
                        ps.setString(index++, deviceStatus);
                    }
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("DEVICE_ID");
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all registered devices " +
                    "according to device ids and the limit area.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getDevicesExpiredByOSVersion(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        try {
            Long osValue = (Long) request.getProperty(Constants.OS_VALUE);
            Connection conn = getConnection();
             /* following variable is used to identify the datasource type.This is due to a
             convert function performed in the query which will depend on the datasource */
            String dataSourceType = conn.getMetaData().getDatabaseProductName();
            String sql = "SELECT " +
                    "e.DEVICE_TYPE, " +
                    "d1.DEVICE_ID, " +
                    "d1.DEVICE_NAME, " +
                    "d1.DESCRIPTION, " +
                    "d1.DEVICE_IDENTIFICATION, " +
                    "d1.LAST_UPDATED_TIMESTAMP, " +
                    "ddd.OS_VERSION, " +
                    "e.ID AS ENROLMENT_ID, " +
                    "e.OWNER, " +
                    "e.OWNERSHIP, " +
                    "e.STATUS, " +
                    "e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT " +
                    "FROM DM_DEVICE_INFO ddi," +
                    "DM_DEVICE_DETAIL ddd, DM_ENROLMENT e, DM_DEVICE d1 " +
                    "WHERE e.DEVICE_TYPE = ? " +
                    "AND e.TENANT_ID = ? " +
                    "AND d1.DEVICE_ID = e.DEVICE_ID " +
                    "AND d1.DEVICE_ID = ddi.DEVICE_ID " +
                    "AND d1.DEVICE_ID = ddd.DEVICE_ID " +
                    "AND ddi.KEY_FIELD = ? ";
            if (dataSourceType.contains(DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2)) {
                sql += "AND CAST( ddi.VALUE_FIELD AS BIGINT ) < ? ";
            } else if (dataSourceType.contains(DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL)) {
                sql += "AND CAST( ddi.VALUE_FIELD AS UNSIGNED ) < ? ";
            }
            sql += "LIMIT ? OFFSET ?";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, request.getDeviceType());
                ps.setInt(2, tenantId);
                ps.setString(3, Constants.OS_VALUE);
                ps.setLong(4, osValue);
                ps.setInt(5, request.getStartIndex());
                ps.setInt(6, request.getRowCount());

                try (ResultSet rs = ps.executeQuery()) {
                    List<Device> devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
                        device.setDeviceInfo(deviceInfo);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while building or executing queries to retrieve information " +
                    "of devices with an older OS build date";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public int getCountOfDeviceExpiredByOSVersion(String deviceType, Long osValue, int tenantId)
            throws DeviceManagementDAOException {
        try {
            Connection conn = getConnection();
            /* following variable is used to identify the datasource type.This is due to a
             convert function performed in the query which will depend on the datasource */
            String dataSourceType = conn.getMetaData().getDatabaseProductName();
            String sql = "SELECT " +
                    "COUNT(ddi.DEVICE_ID) AS DEVICE_COUNT " +
                    "FROM DM_DEVICE_INFO ddi, " +
                    "(SELECT d.ID    AS DEVICE_ID " +
                    "FROM DM_DEVICE_TYPE dt, " +
                    "DM_DEVICE d " +
                    "WHERE dt.NAME = ? " +
                    "AND PROVIDER_TENANT_ID = ? " +
                    "AND dt.ID = d.DEVICE_TYPE_ID " +
                    ") d1 " +
                    "WHERE d1.DEVICE_ID = ddi.DEVICE_ID " +
                    "AND ddi.KEY_FIELD = ? ";
            if (dataSourceType.contains(DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2)) {
                sql += "AND CAST( ddi.VALUE_FIELD AS BIGINT ) < ? ";
            } else if (dataSourceType.contains(DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL)) {
                sql += "AND CAST( ddi.VALUE_FIELD AS UNSIGNED ) < ? ";
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, deviceType);
                ps.setInt(2, tenantId);
                ps.setString(3, Constants.OS_VALUE);
                ps.setLong(4, osValue);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("DEVICE_COUNT");
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while building or executing queries to retrieve the count " +
                    "of devices with an older OS build date";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getGroupedDevicesDetails(PaginationRequest request, List<Integer> deviceIds, String groupName,
                                                 int tenantId) throws DeviceManagementDAOException {
        int limitValue = request.getRowCount();
        int offsetValue = request.getStartIndex();
        List<String> status = request.getStatusList();
        String name = request.getDeviceName();
        String user = request.getOwner();
        String ownership = request.getOwnership();
        try {
            List<Device> devices = new ArrayList<>();
            if (deviceIds.isEmpty()) {
                return devices;
            }
            Connection conn = this.getConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "DM_DEVICE.ID AS DEVICE_ID, "
                            + "DM_DEVICE.NAME AS DEVICE_NAME, "
                            + "DM_DEVICE.DESCRIPTION AS DESCRIPTION, "
                            + "DM_DEVICE.LAST_UPDATED_TIMESTAMP, "
                            + "e.DEVICE_TYPE, "
                            + "e.DEVICE_IDENTIFICATION AS DEVICE_IDENTIFICATION, "
                            + "e.ID AS ENROLMENT_ID, "
                            + "e.OWNER, "
                            + "e.OWNERSHIP, "
                            + "e.DATE_OF_ENROLMENT, "
                            + "e.DATE_OF_LAST_UPDATE, "
                            + "e.STATUS, "
                            + "e.IS_TRANSFERRED "
                            + "FROM DM_DEVICE_GROUP_MAP "
                            + "INNER JOIN DM_DEVICE ON "
                            + "DM_DEVICE_GROUP_MAP.DEVICE_ID = DM_DEVICE.ID "
                            + "INNER JOIN DM_GROUP ON "
                            + "DM_DEVICE_GROUP_MAP.GROUP_ID = DM_GROUP.ID "
                            + "INNER JOIN DM_ENROLMENT e ON "
                            + "DM_DEVICE.ID = e.DEVICE_ID AND "
                            + "DM_DEVICE.TENANT_ID = e.TENANT_ID "
                            + "WHERE DM_DEVICE.ID IN (",
                    ") AND DM_DEVICE.TENANT_ID = ?");

            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            if (StringUtils.isNotBlank(groupName)) {
                query += " AND DM_GROUP.GROUP_NAME = ?";
            }
            if (StringUtils.isNotBlank(name)) {
                query += " AND DM_DEVICE.NAME LIKE ?";
            }
            if (StringUtils.isNotBlank(user)) {
                query += " AND e.OWNER = ?";
            }
            if (StringUtils.isNotBlank(ownership)) {
                query += " AND e.OWNERSHIP = ?";
            }
            if (status != null && !status.isEmpty()) {
                query += buildStatusQuery(status);
            }

            query += "LIMIT ? OFFSET ?";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setInt(index++, deviceId);
                }
                ps.setInt(index++, tenantId);
                if (StringUtils.isNotBlank(groupName)) {
                    ps.setString(index++, groupName);
                }
                if (StringUtils.isNotBlank(name)) {
                    ps.setString(index++, name);
                }
                if (StringUtils.isNotBlank(user)) {
                    ps.setString(index++, user);
                }
                if (StringUtils.isNotBlank(ownership)) {
                    ps.setString(index++, ownership);
                }
                if (status != null && !status.isEmpty()) {
                    for (String deviceStatus : status) {
                        ps.setString(index++, deviceStatus);
                    }
                }
                ps.setInt(index++, limitValue);
                ps.setInt(index, offsetValue);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        devices.add(DeviceManagementDAOUtil.loadDevice(rs));
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all registered devices " +
                    "according to device ids and the limit area.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /***
     * This method updates the status of a given list of devices to DELETED state in the DM_DEVICE_STATUS table
     * @param conn Connection object
     * @param validDevices list of devices
     * @throws DeviceManagementDAOException if updating fails
     */
    public void refactorDeviceStatus(Connection conn, List<Device> validDevices) throws DeviceManagementDAOException {
        String updateQuery = "UPDATE DM_DEVICE_STATUS SET STATUS = ? WHERE ID = ?";
        String selectLastMatchingRecordQuery = "SELECT ID FROM DM_DEVICE_STATUS WHERE ENROLMENT_ID = ? " +
                "AND DEVICE_ID = ? ORDER BY ID DESC LIMIT 1";

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
    public List<Device> searchDevicesNotInGroup(PaginationRequest request, int tenantId) throws DeviceManagementDAOException {
        List<Device> devices = null;
        int groupId = request.getGroupId();
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        List<String> statusList = request.getStatusList();
        boolean isStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;
        String serial = request.getSerialNumber();
        boolean isSerialProvided = false;

        try {
            Connection conn = getConnection();
            String sql = "SELECT d1.DEVICE_ID, " +
                    "d1.DESCRIPTION, " +
                    "d1.NAME AS DEVICE_NAME, " +
                    "e.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, " +
                    "d1.LAST_UPDATED_TIMESTAMP, " +
                    "e.OWNER, " +
                    "e.OWNERSHIP, " +
                    "e.STATUS, " +
                    "e.IS_TRANSFERRED, " +
                    "e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, " +
                    "e.ID AS ENROLMENT_ID " +
                    "FROM DM_ENROLMENT e, " +
                    "(SELECT gd.DEVICE_ID, " +
                    "gd.DESCRIPTION, " +
                    "gd.NAME, " +
                    "gd.DEVICE_IDENTIFICATION, " +
                    "gd.LAST_UPDATED_TIMESTAMP " +
                    "FROM " +
                    "(SELECT d.ID AS DEVICE_ID, " +
                    "d.DESCRIPTION,  " +
                    "d.NAME, " +
                    "d.DEVICE_IDENTIFICATION, " +
                    "d.LAST_UPDATED_TIMESTAMP " +
                    "FROM DM_DEVICE d " +
                    "WHERE d.ID NOT IN " +
                    "(SELECT dgm.DEVICE_ID " +
                    "FROM DM_DEVICE_GROUP_MAP dgm " +
                    "WHERE  dgm.GROUP_ID = ?) " +
                    "AND d.TENANT_ID = ?";

            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }
            sql = sql + ") gd";
            sql = sql + " WHERE 1 = 1";

            if (since != null) {
                sql = sql + " AND gd.LAST_UPDATED_TIMESTAMP > ?";
                isSinceProvided = true;
            }
            sql = sql + " ) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND e.TENANT_ID = ? ";

            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND e.DEVICE_TYPE = ?";
                isDeviceTypeProvided = true;
            }

            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }

            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            if (statusList != null && !statusList.isEmpty()) {
                sql += buildStatusQuery(statusList);
                isStatusProvided = true;
            }

            if (serial != null || !request.getCustomProperty().isEmpty()) {
                if (serial != null) {
                    sql += "AND EXISTS (" +
                            "SELECT VALUE_FIELD " +
                            "FROM DM_DEVICE_INFO di " +
                            "WHERE di.DEVICE_ID = d1.DEVICE_ID " +
                            "AND di.KEY_FIELD = 'serial' " +
                            "AND di.VALUE_FIELD LIKE ?) ";
                    isSerialProvided = true;
                }
                if (!request.getCustomProperty().isEmpty()) {
                    for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                        sql += "AND EXISTS (" +
                                "SELECT VALUE_FIELD " +
                                "FROM DM_DEVICE_INFO di2 " +
                                "WHERE di2.DEVICE_ID = d1.DEVICE_ID " +
                                "AND di2.KEY_FIELD = '" + entry.getKey() + "' " +
                                "AND di2.VALUE_FIELD LIKE ?)";
                    }
                }
            }
            sql = sql + " LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, groupId);
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceNameProvided) {
                    stmt.setString(paramIdx++, "%" + deviceName + "%");
                }
                if (isSinceProvided) {
                    stmt.setTimestamp(paramIdx++, new Timestamp(since.getTime()));
                }
                stmt.setInt(paramIdx++, tenantId);
                if (isDeviceTypeProvided) {
                    stmt.setString(paramIdx++, deviceType);
                }
                if (isOwnershipProvided) {
                    stmt.setString(paramIdx++, ownership);
                }
                if (isOwnerProvided) {
                    stmt.setString(paramIdx++, "%" + owner + "%");
                } else if (isOwnerPatternProvided) {
                    stmt.setString(paramIdx++, "%" + ownerPattern + "%");
                }
                if (isStatusProvided) {
                    for (String status : statusList) {
                        stmt.setString(paramIdx++, status);
                    }
                }
                if (isSerialProvided) {
                    stmt.setString(paramIdx++, "%" + serial + "%");
                }
                if (!request.getCustomProperty().isEmpty()) {
                    for (Map.Entry<String, String> entry : request.getCustomProperty().entrySet()) {
                        stmt.setString(paramIdx++, "%" + entry.getValue() + "%");
                    }
                }
                stmt.setInt(paramIdx++, request.getRowCount());
                stmt.setInt(paramIdx, request.getStartIndex());

                try (ResultSet rs = stmt.executeQuery()) {
                    devices = new ArrayList<>();
                    while (rs.next()) {
                        Device device = DeviceManagementDAOUtil.loadDevice(rs);
                        devices.add(device);
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of" +
                    " devices not belonging to group : " + groupId;
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
        boolean isDeviceTypeIdProvided = false;

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
                    "AND e.DEVICE_ID IN (" + deviceIdStringList+ ") " +
                    "AND e.STATUS NOT IN ('DELETED', 'REMOVED')";

            if (paginationRequest.getOwner() != null) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerProvided = true;
            }

            if (paginationRequest.getDeviceStatus() != null) {
                sql = sql + " AND e.STATUS = ?";
                isDeviceStatusProvided = true;
            }

            if (paginationRequest.getDeviceName() != null) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }

            if (paginationRequest.getDeviceTypeId() > 0) {
                sql = sql + " AND d.DEVICE_TYPE_ID = ?";
                isDeviceTypeIdProvided = true;
            }

            sql = sql + " LIMIT ? OFFSET ?";

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
                if (isDeviceTypeIdProvided) {
                    preparedStatement.setInt(parameterIdx++, paginationRequest.getDeviceTypeId());
                }

                preparedStatement.setInt(parameterIdx++, paginationRequest.getRowCount());
                preparedStatement.setInt(parameterIdx, paginationRequest.getStartIndex());

                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    Device device;
                    while(resultSet.next()) {
                        device = new Device();
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
}
