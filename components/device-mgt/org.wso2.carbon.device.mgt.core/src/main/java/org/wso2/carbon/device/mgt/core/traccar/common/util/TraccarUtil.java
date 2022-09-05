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

package org.wso2.carbon.device.mgt.core.traccar.common.util;

import org.json.JSONObject;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarDevice;
import org.wso2.carbon.device.mgt.core.traccar.common.beans.TraccarUser;

import java.util.ArrayList;
import java.util.List;

public class TraccarUtil {
    public static JSONObject TraccarUserPayload(TraccarUser traccarUser) {
        JSONObject payload = new JSONObject();
        JSONObject attribute = new JSONObject();
        attribute.put("speedUnit", "kmh");
        payload.put("id", traccarUser.getId());
        payload.put("name", traccarUser.getName());
        payload.put("login", traccarUser.getLogin());
        payload.put("email", traccarUser.getEmail());
        payload.put("password", traccarUser.getPassword());
        payload.put("token", traccarUser.getToken());
        payload.put("administrator", traccarUser.getAdministrator());
        payload.put("deviceLimit", traccarUser.getDeviceLimit());
        payload.put("userLimit", traccarUser.getUserLimit());
        payload.put("disabled", traccarUser.getDisabled());
        payload.put("deviceReadonly", traccarUser.getDeviceReadonly());
        payload.put("readonly", traccarUser.getReadonly());
        payload.put("expirationTime", traccarUser.getExpirationTime());
        payload.put("attributes", attribute);
        return payload;
    }

    public static JSONObject TraccarDevicePayload(TraccarDevice deviceInfo) {
        JSONObject payload = new JSONObject();
        payload.put("name", deviceInfo.getDeviceName());
        payload.put("uniqueId", deviceInfo.getUniqueId());
        payload.put("status", deviceInfo.getStatus());
        payload.put("disabled", deviceInfo.getDisabled());
        payload.put("lastUpdate", deviceInfo.getLastUpdate());
        payload.put("positionId", deviceInfo.getPositionId());
        payload.put("groupId", deviceInfo.getGroupId());
        payload.put("phone", deviceInfo.getPhone());
        payload.put("model", deviceInfo.getModel());
        payload.put("contact", deviceInfo.getContact());
        payload.put("category", deviceInfo.getCategory());
        List<String> geoFenceIds = new ArrayList<>();
        payload.put("geofenceIds", geoFenceIds);
        payload.put("attributes", new JSONObject());
        return payload;
    }
}
