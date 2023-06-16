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
package io.entgra.device.mgt.core.application.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.GroupManagementProviderService;

/**
 * Utility methods used in the Application Management.
 */
public class HelperUtil {

    private static final Log log = LogFactory.getLog(HelperUtil.class);

    private static DeviceManagementProviderService deviceManagementProviderService;
    private static GroupManagementProviderService groupManagementProviderService;

    public static synchronized DeviceManagementProviderService getDeviceManagementProviderService() {
        if (deviceManagementProviderService == null) {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            deviceManagementProviderService = (DeviceManagementProviderService) ctx
                    .getOSGiService(DeviceManagementProviderService.class, null);
            if (deviceManagementProviderService == null) {
                String msg = "Device management provider service has not initialized.";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
        }
        return deviceManagementProviderService;
    }

    public static synchronized GroupManagementProviderService getGroupManagementProviderService() {
        if (groupManagementProviderService == null) {
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            groupManagementProviderService = (GroupManagementProviderService) ctx
                    .getOSGiService(GroupManagementProviderService.class, null);
            if (groupManagementProviderService == null) {
                String msg = "Group management provider service has not initialized.";
                log.error(msg);
                throw new IllegalStateException(msg);
            }
        }
        return groupManagementProviderService;
    }
}
