package org.wso2.carbon.device.mgt.core.dao.impl;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceStatus;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.DeviceStatusDAO;
import org.wso2.carbon.device.mgt.core.dao.EnrollmentDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceStatusDAOImpl implements DeviceStatusDAO {

  private List<DeviceStatus> getStatus(int id, Date fromDate, Date toDate, boolean isDeviceId) throws DeviceManagementDAOException {
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
    return getStatus(enrolmentId, fromDate, toDate, false);
  }

  @Override
  public List<DeviceStatus> getStatus(int deviceId, int tenantId) throws DeviceManagementDAOException {
    return getStatus(deviceId, tenantId, null, null);
  }

  @Override
  public List<DeviceStatus> getStatus(int deviceId, int tenantId, Date fromDate, Date toDate) throws DeviceManagementDAOException {
    return getStatus(deviceId, fromDate, toDate, true);
  }

  @Override
  public List<DeviceStatus> getStatus(int enrolmentId) throws DeviceManagementDAOException {
    return getStatus(enrolmentId, null, null);
  }


  private Connection getConnection() throws SQLException {
    return DeviceManagementDAOFactory.getConnection();
  }
}
