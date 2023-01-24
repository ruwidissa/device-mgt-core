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

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.LifecycleStateDevice;

import java.util.List;

/**
 * Device status relevent DAO activity
 */
public interface DeviceLifecycleDAO {

    /**
     * can change the relevent device's status
     *
     * @param enrolmentId Enrolment Id
     * @param status      changing status
     * @param tenantId    tenantId
     * @return true or false
     * @throws DeviceManagementDAOException when device no found
     */
    boolean changeStatus(int enrolmentId, EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException;

    /**
     * Add the changed status
     *
     * @param enrolmentId    Enrolment Id
     * @param currentStatus  Current Status
     * @param previousStatus Previous Status
     * @param deviceId       Id of the device
     * @return Added or not, true or false
     * @throws DeviceManagementDAOException When device not found
     */
    boolean addStatus(int enrolmentId, EnrolmentInfo.Status currentStatus, EnrolmentInfo.Status previousStatus,
                      int deviceId) throws DeviceManagementDAOException;

    /**
     * Get Device ID
     *
     * @param enrolmentId Enrolment ID
     * @return Device id
     * @throws DeviceManagementDAOException when device not found
     */
    int getDeviceId(int enrolmentId) throws DeviceManagementDAOException;

    /**
     * Get the lifecycle history of the device
     *
     * @param id id of the device
     * @return List of LifecycleStateDevice
     */
    List<LifecycleStateDevice> getDeviceLifecycle(int id) throws DeviceManagementDAOException;
}
