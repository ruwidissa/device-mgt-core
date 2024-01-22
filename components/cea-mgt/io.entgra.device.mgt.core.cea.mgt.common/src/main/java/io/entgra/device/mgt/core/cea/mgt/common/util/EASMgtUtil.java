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

package io.entgra.device.mgt.core.cea.mgt.common.util;

import io.entgra.device.mgt.core.cea.mgt.common.bean.AndroidEASIdentifier;

public class EASMgtUtil {
    public static AndroidEASIdentifier generateAndroidEASIdentifier(String androidId) {
        AndroidEASIdentifier androidEASIdentifier = new AndroidEASIdentifier();
        androidEASIdentifier.setIdentifier((Constants.EAS_KEY + androidId).toUpperCase());
        return androidEASIdentifier;
    }

    public static boolean isManageByUEM(AndroidEASIdentifier androidEASIdentifier) {
        if (androidEASIdentifier == null)
            throw new IllegalArgumentException("Null retrieved for Android EAS Identifier");
        return androidEASIdentifier.getIdentifier().startsWith(Constants.EAS_KEY);
    }

    public static boolean isManageByUEM(String androidEASIdentifier) {
        if (androidEASIdentifier == null)
            throw new IllegalArgumentException("Null retrieved for Android EAS Identifier");
        return androidEASIdentifier.startsWith(Constants.EAS_KEY);
    }
}
