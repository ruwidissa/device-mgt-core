/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.traccar.api.service;

import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;


public interface DeviceAPIClientService {

    /**
     * Create device Traccar configuration records
     *
     * @param deviceInfo to be added
     * @throws TraccarConfigurationException errors thrown while creating a device traccar configuration
     */
    void addDevice(TraccarDevice deviceInfo) throws TraccarConfigurationException;

    /**
     * Add GPS location of a device Traccar configuration records
     *
     * @param deviceInfo to be added to update location of the device
     * @throws TraccarConfigurationException errors thrown while inserting location of a device traccar configuration
     */
    void updateLocation(TraccarPosition deviceInfo) throws TraccarConfigurationException;

    /**
     * Delete a device Traccar configuration records
     *
     * @param deviceInfo to be delete a device
     * @throws TraccarConfigurationException errors thrown while deleting a device traccar configuration
     */
    void disDevice(TraccarDevice deviceInfo) throws TraccarConfigurationException;

    //String addGroup(TraccarGroups groupInfo) throws IOException;
}
