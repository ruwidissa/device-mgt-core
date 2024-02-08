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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.api.util;


import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.spi.DeviceOrganizationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * DeviceOrgAPIUtils class provides utility function used by Device Organization REST - API classes.
 */
public class DeviceOrgAPIUtils {
    private static DeviceOrganizationService deviceOrganizationService;

    private static final Log log = LogFactory.getLog(DeviceOrgAPIUtils.class);

    /**
     * Initializing and accessing method for DeviceOrganizationService.
     *
     * @return DeviceOrganizationService instance
     * @throws IllegalStateException if deviceOrganizationService cannot be initialized
     */
    public static DeviceOrganizationService getDeviceOrganizationService() {
        if (deviceOrganizationService == null) {
            synchronized (DeviceOrgAPIUtils.class) {
                if (deviceOrganizationService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    deviceOrganizationService = (DeviceOrganizationService) ctx.getOSGiService(
                            DeviceOrganizationService.class, null);
                    if (deviceOrganizationService == null) {
                        throw new IllegalStateException("Device Organization Management service not initialized.");
                    }
                }
            }
        }
        return deviceOrganizationService;
    }


}
