/*
 *   Copyright (c) 2019, Entgra (Pvt) Ltd. (https://entgra.io) All Rights Reserved.
 *
 *   Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.extensions.spi;

import org.wso2.carbon.device.mgt.extensions.device.type.template.dao.DeviceTypePluginDAOManager;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypePluginExtensionException;

/**
 * This represents the device type plugin extension service which can be used by any device type plugin implementation
 * intended to use the same plugin DAO instances to be used with its plugin level DAO components
 */
public interface DeviceTypePluginExtensionService {

    /**
     * Save device type specific DeviceTypePluginDAOManager in a HashMap againast tenant ID and device type
     * @param deviceType - Type of the device (i.e; android, ios, windows)
     * @param pluginDAOManager - Device type plugin DAO manager instance to be saved against device type
     * @throws DeviceTypePluginExtensionException when pluginDAOManager is null
     */
    void addPluginDAOManager(String deviceType, DeviceTypePluginDAOManager pluginDAOManager)
            throws DeviceTypePluginExtensionException;

    /**
     * Retrieve the DeviceTypePluginDAOManager instance against tenant ID and given device type
     * @param deviceType - Type of the device (i.e; android, ios, windows)
     * @return an Instance of {@link DeviceTypePluginDAOManager}
     * @throws DeviceTypePluginExtensionException when pluginDAOManager cannot be found
     */
    DeviceTypePluginDAOManager getPluginDAOManager(String deviceType) throws DeviceTypePluginExtensionException;
}
