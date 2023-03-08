/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.subtype.mgt.util;

import io.entgra.device.mgt.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.subtype.mgt.dto.DeviceSubTypeCacheKey;

public class DeviceSubTypeMgtUtil {
    public static String setDeviceSubTypeCacheKey(int tenantId, int subTypeId, DeviceSubType.DeviceType deviceType) {
        return tenantId + "|" + subTypeId + "|" + deviceType.toString();
    }

    public static DeviceSubTypeCacheKey getDeviceSubTypeCacheKey(String key) {
        String[] keys = key.split("\\|");
        int tenantId = Integer.parseInt(keys[0]);
        int subTypeId = Integer.parseInt(keys[1]);
        DeviceSubType.DeviceType deviceType = DeviceSubType.DeviceType.valueOf(keys[2]);

        DeviceSubTypeCacheKey deviceSubTypesCacheKey = new DeviceSubTypeCacheKey();
        deviceSubTypesCacheKey.setTenantId(tenantId);
        deviceSubTypesCacheKey.setSubTypeId(subTypeId);
        deviceSubTypesCacheKey.setDeviceType(deviceType);
        return deviceSubTypesCacheKey;
    }
}
