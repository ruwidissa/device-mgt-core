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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceStatus;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceStatusDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.EnrollmentDAO;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceStatusDAOImpl implements DeviceStatusDAO {

  private List<DeviceStatus> getStatus(int id, Date fromDate, Date toDate, boolean isDeviceId, boolean billingStatus) throws DeviceManagementDAOException {
    List<DeviceStatus> result = new ArrayList<>();
    Connection conn;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    EnrolmentInfo.Status status = null;
    try {
      conn = this.getConnection();
      // either we list all status values for the device using the device id or only get status values for the given enrolment id
      String idType = isDeviceId ? "DEVICE_ID" : "ENROLMENT_ID";
      String sql = "SELECT ENROLMENT_ID, DEVICE_ID, UPDATE_TIME, STATUS, CHANGED_BY FROM DM_DEVICE_STATUS WHERE " + idType + " = ?";

      // filter the data based on a date range if specified
      if (fromDate != null){
        sql += " AND UPDATE_TIME >= ?";
      }
      if (toDate != null){
        sql += " AND UPDATE_TIME <= ?";
      }

      if (billingStatus) {
        sql += " ORDER BY ID DESC";
      }

      stmt = conn.prepareStatement(sql);

      int i = 1;
      stmt.setInt(i++, id);
      if (fromDate != null){
        Timestamp fromTime = new Timestamp(fromDate.getTime());
        stmt.setTimestamp(i++, fromTime);
      }
      if (toDate != null){
        Timestamp toTime = new Timestamp(toDate.getTime());
        stmt.setTimestamp(i++, toTime);
      }
      rs = stmt.executeQuery();

      while (rs.next()) {
        DeviceStatus ds = new DeviceStatus(rs.getInt("ENROLMENT_ID"), rs.getInt("DEVICE_ID"),
                EnrolmentInfo.Status.valueOf(rs.getString("STATUS")),
                new Date(rs.getTimestamp("UPDATE_TIME").getTime()), rs.getString("CHANGED_BY"));
        result.add(ds);
      }
    } catch (SQLException e) {
      throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
    } finally {
      DeviceManagementDAOUtil.cleanupResources(stmt, rs);
    }
    return result;
  }

  @Override
  public List<DeviceStatus> getStatus(int enrolmentId, Date fromDate, Date toDate) throws DeviceManagementDAOException {
    return getStatus(enrolmentId, fromDate, toDate, false, false);
  }

  @Override
  public List<DeviceStatus> getStatus(int deviceId, int tenantId) throws DeviceManagementDAOException {
    return getStatus(deviceId, tenantId, null, null, false);
  }

  @Override
  public List<DeviceStatus> getStatus(int deviceId, int tenantId, Date fromDate, Date toDate, boolean billingStatus) throws DeviceManagementDAOException {
    return getStatus(deviceId, fromDate, toDate, true, billingStatus);
  }

  @Override
  public List<DeviceStatus> getStatus(int enrolmentId) throws DeviceManagementDAOException {
    return getStatus(enrolmentId, null, null);
  }


  private Connection getConnection() throws SQLException {
    return DeviceManagementDAOFactory.getConnection();
  }
}
