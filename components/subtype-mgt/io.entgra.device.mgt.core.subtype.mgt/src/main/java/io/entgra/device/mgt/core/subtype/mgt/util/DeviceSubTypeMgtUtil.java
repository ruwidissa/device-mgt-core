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

package io.entgra.device.mgt.core.subtype.mgt.util;

import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubTypeCacheKey;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;

public class DeviceSubTypeMgtUtil {
    public static String setDeviceSubTypeCacheKey(int tenantId, String subTypeId, String deviceType) {
        return tenantId + "|" + subTypeId + "|" + deviceType.toString();
    }

    public static DeviceSubTypeCacheKey getDeviceSubTypeCacheKey(String key) {
        String[] keys = key.split("\\|");
        int tenantId = Integer.parseInt(keys[0]);
        String subTypeId = keys[1];
        String deviceType = keys[2];

        DeviceSubTypeCacheKey deviceSubTypesCacheKey = new DeviceSubTypeCacheKey();
        deviceSubTypesCacheKey.setTenantId(tenantId);
        deviceSubTypesCacheKey.setSubTypeId(subTypeId);
        deviceSubTypesCacheKey.setDeviceType(deviceType);
        return deviceSubTypesCacheKey;
    }
}
