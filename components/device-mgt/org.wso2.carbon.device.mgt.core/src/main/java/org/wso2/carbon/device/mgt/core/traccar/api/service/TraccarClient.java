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

import org.wso2.carbon.device.mgt.common.TrackerAlreadyExistException;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarGroups;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarPosition;
import org.wso2.carbon.device.mgt.core.traccar.common.config.TraccarConfigurationException;

public interface TraccarClient {

    void addDevice(TraccarDevice deviceInfo, int tenantId) throws
            TraccarConfigurationException, TrackerAlreadyExistException;

    void updateDevice(TraccarDevice deviceInfo, int tenantId) throws
            TraccarConfigurationException, TrackerAlreadyExistException;

    void updateLocation(TraccarDevice device, TraccarPosition deviceInfo, int tenantId) throws
            TraccarConfigurationException, TrackerAlreadyExistException;

    void disEndrollDevice(int traccarDeviceId, int tenantId) throws TraccarConfigurationException;

    void addGroup(TraccarGroups groupInfo, int groupId, int tenantId) throws
            TraccarConfigurationException, TrackerAlreadyExistException;

    void updateGroup(TraccarGroups groupInfo, int groupId, int tenantId)
            throws TraccarConfigurationException, TrackerAlreadyExistException;

    void deleteGroup(int traccarGroupId, int tenantId) throws TraccarConfigurationException;
}
