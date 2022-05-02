package org.wso2.carbon.device.mgt.core.dao.impl;

import org.wso2.carbon.device.mgt.common.Billing;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dao.BillingDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillingDAOImpl implements BillingDAO {

    @Override
    public void addBilling(int deviceId, int tenantId, Timestamp billingStart, Timestamp billingEnd) throws DeviceManagementDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_BILLING(DEVICE_ID, TENANT_ID, BILLING_START, BILLING_END) VALUES(?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[] {"invoice_id"});
            stmt.setInt(1, deviceId);
            stmt.setInt(2,tenantId);
            stmt.setTimestamp(3, billingStart);
            stmt.setTimestamp(4, billingEnd);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DeviceManagementDAOException("Error occurred while adding billing period", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<Billing> getBilling(int deviceId, Timestamp billingStart, Timestamp billingEnd) throws DeviceManagementDAOException {
        List<Billing> billings = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo.Status status = null;
        try {
            conn = this.getConnection();
            String sql;

            sql = "SELECT * FROM DM_BILLING WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Billing bill = new Billing(rs.getInt("INVOICE_ID"), rs.getInt("DEVICE_ID"),rs.getInt("TENANT_ID"),
                      (rs.getTimestamp("BILLING_START").getTime()), (rs.getTimestamp("BILLING_END").getTime()));
                billings.add(bill);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred getting billing periods", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return billings;
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}
