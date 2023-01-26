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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.LifecycleStateDevice;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceStatusException;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidStatusException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;

import java.util.List;

/**
 * This interface manages the device lifecycle, such as statuschange and add status to table
 */
public interface DeviceStateManagementService {

   /**
    * This method change the device status and store it in a table
    *
    * @param enrolmentInfo Enrollment Information about the device
    * @param nextStatus Next status of the device
    * @return LifecycleStateDevice which contain current and previoius status
    * @throws InvalidStatusException If there is a invalid status or invalid status change
    * @throws DeviceManagementDAOException if the device cannot be found
    */
   LifecycleStateDevice changeDeviceStatus(EnrolmentInfo enrolmentInfo, EnrolmentInfo.Status nextStatus)
           throws InvalidStatusException, DeviceStatusException;

   /**
    * Get the lifecycle history of the relevent device
    *
    * @param device Device
    * @return List of LifecycleStateDevice
    */
   List<LifecycleStateDevice> getDeviceLifecycleHistory(Device device) throws DeviceStatusException;
}
