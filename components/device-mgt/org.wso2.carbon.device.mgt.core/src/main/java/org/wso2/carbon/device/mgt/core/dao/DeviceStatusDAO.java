/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceStatus;

import java.util.Date;
import java.util.List;

public interface DeviceStatusDAO {

//    boolean updateStatus(int deviceId, Status status, int tenantId) throws DeviceManagementDAOException;

//    boolean updateStatus(int enrolmentId, Status status) throws DeviceManagementDAOException;

    List<DeviceStatus> getStatus(int deviceId, int tenantId) throws DeviceManagementDAOException;

    List<DeviceStatus> getStatus(int deviceId, int tenantId, Date fromDate, Date toDate, boolean billingStatus) throws DeviceManagementDAOException;

    List<DeviceStatus> getStatus(int enrolmentId) throws DeviceManagementDAOException;

    List<DeviceStatus> getStatus(int enrolmentId, Date fromDate, Date toDate) throws DeviceManagementDAOException;

}
