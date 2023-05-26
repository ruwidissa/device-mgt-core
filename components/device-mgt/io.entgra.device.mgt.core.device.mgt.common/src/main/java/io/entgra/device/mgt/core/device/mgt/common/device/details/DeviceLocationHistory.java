/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.common.device.details;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DeviceLocationHistory implements Serializable {
    private List<List<DeviceLocationHistorySnapshot>> locationHistorySnapshots = new ArrayList<>();

    public List<List<DeviceLocationHistorySnapshot>> getLocationHistorySnapshots() {
        return locationHistorySnapshots;
    }

    public void setLocationHistorySnapshots(List<List<DeviceLocationHistorySnapshot>> locationHistorySnapshots) {
        this.locationHistorySnapshots = locationHistorySnapshots;
    }
}