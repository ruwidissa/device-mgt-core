/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymgt.extension.internal;

import org.wso2.carbon.apimgt.keymgt.extension.service.KeyMgtService;

public class KeyMgtDataHolder {

    private static final KeyMgtDataHolder thisInstance = new KeyMgtDataHolder();
    private KeyMgtService keyMgtService;

    public static KeyMgtDataHolder getInstance() {
        return thisInstance;
    }

    public KeyMgtService getKeyMgtService() {
        return keyMgtService;
    }

    public void setKeyMgtService(KeyMgtService keyMgtService) {
        this.keyMgtService = keyMgtService;
    }

}
