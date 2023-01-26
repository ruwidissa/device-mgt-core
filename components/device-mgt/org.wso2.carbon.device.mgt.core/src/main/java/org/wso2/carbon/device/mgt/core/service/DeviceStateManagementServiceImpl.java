/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.LifecycleStateDevice;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceStatusException;
import org.wso2.carbon.device.mgt.common.exceptions.IllegalTransactionStateException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidStatusException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceLifecycleDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.lifeCycle.DeviceLifecycleStateManager;

import java.sql.SQLException;
import java.util.List;

public class DeviceStateManagementServiceImpl implements DeviceStateManagementService {

    private static final Log log = LogFactory.getLog(DeviceStateManagementServiceImpl.class);
    private final DeviceLifecycleDAO deviceLifecycleDAO;
    private final DeviceLifecycleStateManager deviceLifecycleStateManager;

    public DeviceStateManagementServiceImpl() {
        this.deviceLifecycleDAO = DeviceManagementDAOFactory.getDeviceLifecycleDAO();
        deviceLifecycleStateManager = DeviceManagementDataHolder.getInstance().getDeviceLifecycleStateManager();
    }

    @Override
    public LifecycleStateDevice changeDeviceStatus(EnrolmentInfo enrolmentInfo, EnrolmentInfo.Status nextStatus) throws
            InvalidStatusException, DeviceStatusException {
        LifecycleStateDevice lifecycleStateDevice = new LifecycleStateDevice();
        EnrolmentInfo.Status currentStatus = enrolmentInfo.getStatus();
        if (deviceLifecycleStateManager.isValidState(nextStatus.toString())){
            if (deviceLifecycleStateManager.isValidStateChange(currentStatus.toString(), nextStatus.toString())) {
                lifecycleStateDevice.setCurrentStatus(nextStatus.toString());
                lifecycleStateDevice.setPreviousStatus(currentStatus.toString());
            } else {
                String msg ="'" + currentStatus + "' to '" + nextStatus + "' is not a valid Status change. Enter " +
                        "valid Status";
                log.error(msg);
                throw new InvalidStatusException(msg);
            }
        } else {
            String msg = "'" + nextStatus +"' is not a valid Status. Check the Status";
            log.error(msg);
            throw new InvalidStatusException(msg);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int enrolmentId = enrolmentInfo.getId();
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int deviceId = deviceLifecycleDAO.getDeviceId(enrolmentId);
            deviceLifecycleDAO.changeStatus(enrolmentId, EnrolmentInfo.Status.valueOf(
                    lifecycleStateDevice.getCurrentStatus()), tenantId);
            deviceLifecycleDAO.addStatus(enrolmentId,
                    EnrolmentInfo.Status.valueOf(lifecycleStateDevice.getCurrentStatus()),
                    EnrolmentInfo.Status.valueOf(lifecycleStateDevice.getPreviousStatus()), deviceId);
            DeviceManagementDAOFactory.commitTransaction();
            return lifecycleStateDevice;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred in updating status  or storing device status";
            log.error(msg, e);
            throw new DeviceStatusException(msg, e);
        } catch (IllegalTransactionStateException e) {
            String msg = "Error occurred while updating and storing(Transaction Error) device status";
            log.error(msg, e);
            throw new DeviceStatusException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred in DeviceManagementDAOFactory";
            log.error(msg, e);
            throw new InvalidStatusException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<LifecycleStateDevice> getDeviceLifecycleHistory(Device device) throws DeviceStatusException {
        int id = device.getId();
        try {
            DeviceManagementDAOFactory.openConnection();
            List<LifecycleStateDevice> listLifecycle = deviceLifecycleDAO.getDeviceLifecycle(id);
            return listLifecycle;
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while getting lifrcycle history";
            log.error(msg, e);
            throw new DeviceStatusException(msg, e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
