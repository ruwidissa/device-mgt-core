/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.app.mgt;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.Application;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.ApplicationManager;

import java.util.List;

public interface ApplicationManagementProviderService extends ApplicationManager{

    @Deprecated
    void updateApplicationListInstalledInDevice(DeviceIdentifier deviceIdentifier, List<Application> applications)
            throws ApplicationManagementException;

    void updateApplicationListInstalledInDevice(Device device, List<Application> applications)
            throws ApplicationManagementException;

    @Deprecated
    List<Application> getApplicationListForDevice(DeviceIdentifier deviceIdentifier)
            throws ApplicationManagementException;

    List<Application> getApplicationListForDevice(Device device)
            throws ApplicationManagementException;
}
