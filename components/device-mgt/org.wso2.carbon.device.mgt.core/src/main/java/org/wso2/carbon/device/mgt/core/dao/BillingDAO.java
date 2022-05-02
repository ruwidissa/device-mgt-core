package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.Billing;

import java.sql.Timestamp;
import java.util.List;

public interface BillingDAO {

    void addBilling(int deviceId, int tenantId, Timestamp billingStart, Timestamp billingEnd) throws DeviceManagementDAOException;

    List<Billing> getBilling(int deviceId, Timestamp billingStart, Timestamp billingEnd) throws DeviceManagementDAOException;
}
