/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubscriptionMetadata {
    public static final String SUBSCRIPTION_STATUS_UNSUBSCRIBED = "unsubscribed";
    public static final class DeviceSubscriptionStatus {
        public static final String NEW = "NEW";
        public static final String PENDING = "PENDING";
        public static final String COMPLETED = "COMPLETED";
        public static final String ERROR = "ERROR";
        public static final String INVALID = "INVALID";
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String REPEATED = "REPEATED";
    }

    public static final class SubscriptionTypes {
        public static final String ROLE = "role";
        public static final String DEVICE = "device";
        public static final String GROUP = "group";
        public static final String USER = "user";
    }

    public static final class DBSubscriptionStatus {
        public static final List<String> COMPLETED_STATUS_LIST =
                Collections.singletonList(DeviceSubscriptionStatus.COMPLETED);
        public static final List<String> ERROR_STATUS_LIST =
                Arrays.asList(DeviceSubscriptionStatus.ERROR, DeviceSubscriptionStatus.INVALID, DeviceSubscriptionStatus.UNAUTHORIZED);
        public static final List<String> PENDING_STATUS_LIST =
                Arrays.asList(DeviceSubscriptionStatus.PENDING, DeviceSubscriptionStatus.IN_PROGRESS, DeviceSubscriptionStatus.REPEATED);
    }

    public static Map<String, List<String>> deviceSubscriptionStatusToDBSubscriptionStatusMap;
    static {
        Map<String, List<String>> statusMap = new HashMap<>();
        statusMap.put(DeviceSubscriptionStatus.COMPLETED, DBSubscriptionStatus.COMPLETED_STATUS_LIST);
        statusMap.put(DeviceSubscriptionStatus.PENDING, DBSubscriptionStatus.PENDING_STATUS_LIST);
        statusMap.put(DeviceSubscriptionStatus.IN_PROGRESS, Collections.singletonList(DeviceSubscriptionStatus.IN_PROGRESS));
        statusMap.put(DeviceSubscriptionStatus.REPEATED, Collections.singletonList(DeviceSubscriptionStatus.REPEATED));
        statusMap.put(DeviceSubscriptionStatus.ERROR, DBSubscriptionStatus.ERROR_STATUS_LIST);
        statusMap.put(DeviceSubscriptionStatus.INVALID,Collections.singletonList(DeviceSubscriptionStatus.INVALID));
        statusMap.put(DeviceSubscriptionStatus.UNAUTHORIZED,Collections.singletonList(DeviceSubscriptionStatus.UNAUTHORIZED));
        deviceSubscriptionStatusToDBSubscriptionStatusMap = Collections.unmodifiableMap(statusMap);
    }
}
