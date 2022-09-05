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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.exceptions.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.dao.TrackerManagementDAOException;
import java.util.concurrent.ExecutionException;

public interface DeviceAPIClientService {

    /**
     * Add GPS location of a device Traccar configuration records
     *
     * @param device to be added to update location of the device
     * @param deviceLocation to be added to update location of the device
     * @throws TrackerManagementDAOException errors thrown while inserting location of a traccar device
     */
    void updateLocation(Device device, DeviceLocation deviceLocation, int tenantId) throws
            ExecutionException, InterruptedException;

    /**
     * Create device Traccar configuration records
     *
     * @param device to be added
     * @throws TrackerManagementDAOException errors thrown while creating a traccar device
     */
    void addDevice(Device device, int tenantId) throws ExecutionException, InterruptedException;

    /**
     * Delete a device Traccar configuration records
     *
     * @param deviceId to be delete a device
     * @throws TrackerManagementDAOException errors thrown while deleting a traccar device
     */
    void disEnrollDevice(int deviceId, int tenantId) throws ExecutionException, InterruptedException;

    /**
     * Delete a device Traccar configuration records
     *
     * @param group to be add a group
     * @throws TrackerManagementDAOException errors thrown while adding a traccar group
     */
    void addGroup(DeviceGroup group, int groupID, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException;

    /**
     * Delete a device Traccar configuration records
     *
     * @param group to be update the group
     * @throws TrackerManagementDAOException errors thrown while adding a traccar group
     */
    void updateGroup(DeviceGroup group, int groupID, int tenantId) throws
            TrackerManagementDAOException, TrackerAlreadyExistException, ExecutionException, InterruptedException;

    /**
     * Delete a device Traccar configuration records
     *
     * @param groupId to delete a group
     * @param tenantId to delete a group
     * @throws TrackerManagementDAOException errors thrown while adding a traccar group
     */
    void deleteGroup(int groupId, int tenantId) throws
            TrackerManagementDAOException, ExecutionException, InterruptedException;
}
