/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.geo.task;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFenceEventMeta;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.core.event.config.DeviceEventOperationExecutor;
import org.wso2.carbon.device.mgt.core.event.config.GroupEventOperationExecutor;

import java.util.List;

public class GeoFenceEventOperationManager {
    private final int tenantId;
    private final String eventOperationCode;
    private final EventCreateCallback callback;
    public GeoFenceEventOperationManager(String eventOperationCode, int tenantId, EventCreateCallback callback) {
        this.eventOperationCode = eventOperationCode;
        this.tenantId = tenantId;
        this.callback = callback;
    }

    public GroupEventOperationExecutor getGroupEventOperationExecutor(GeofenceData geofenceData) {
        GeoFenceEventMeta geoFenceEventMeta = new GeoFenceEventMeta(geofenceData);
        GroupEventOperationExecutor executor = new GroupEventOperationExecutor(geoFenceEventMeta, geofenceData.getGroupIds(),
                tenantId, DeviceManagementConstants.EventServices.GEOFENCE, eventOperationCode);
        executor.setCallback(callback);
        return executor;
    }

    public DeviceEventOperationExecutor getDeviceEventOperationExecutor(int groupId, List<DeviceIdentifier> deviceIdentifiers) {
        DeviceEventOperationExecutor executor = new DeviceEventOperationExecutor(groupId, deviceIdentifiers, tenantId, eventOperationCode);
        executor.setCallback(callback);
        return executor;
    }
}
