/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.spi;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;

public interface TraccarManagementService {

    /**
     * Add the provided device to Traccar.
     * @param device The device to be added to Traccar.
     */
    void addDevice(Device device);

    /**
     * Removes the Traccar device with the specified device ID from the logged in user.
     * @param deviceEnrollmentId The enrollment ID of the device to be removed from Traccar.
     */
    void unLinkTraccarDevice(int deviceEnrollmentId);

    /**
     * Update the provided device to Traccar.
     * @param device The device to be updated on Traccar.
     */
    void updateDevice(Device device);

    /**
     * Removes the device with the specified enrollment ID from Traccar.
     * @param deviceEnrollmentId The enrollment ID of the device to be removed from Traccar.
     */
    void removeDevice(int deviceEnrollmentId);

    /**
     * Updates the location of the provided device with the specified device location.
     * @param device The device whose location is to be updated.
     * @param deviceLocation The new location of the device.
     */
    void updateLocation(Device device, DeviceLocation deviceLocation);
}
