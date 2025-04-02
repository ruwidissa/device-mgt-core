/*
 *   Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.core.internal;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.service.RealmService;

public class DynamicTaskManagementExtensionServiceDataHolder {
    private static final Log log = LogFactory.getLog(DynamicTaskManagementExtensionServiceDataHolder.class);
    private static final Gson gson = new Gson();
    private DynamicTaskPlatformConfigurations defaultConfigurableDynamicTaskPlatformConfigurations;
    private TaskManagementService taskManagementService;
    private MetadataManagementService metadataManagementService;
    private DeviceManagementProviderService deviceManagementProviderService;
    private RealmService realmService;

    DynamicTaskManagementExtensionServiceDataHolder() {
    }

    public static DynamicTaskManagementExtensionServiceDataHolder getInstance() {
        return ReferenceHolder.INSTANCE;
    }

    public static Gson getGson() {
        if (gson == null) {
            String msg = "Gson library is not initialized properly.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return gson;
    }

    public DynamicTaskPlatformConfigurations getDefaultConfigurableDynamicTaskPlatformConfigurations() {
        return defaultConfigurableDynamicTaskPlatformConfigurations;
    }

    public void setDefaultConfigurableDynamicTaskPlatformConfigurations(DynamicTaskPlatformConfigurations defaultConfigurableDynamicTaskPlatformConfigurations) {
        this.defaultConfigurableDynamicTaskPlatformConfigurations =
                defaultConfigurableDynamicTaskPlatformConfigurations;
    }

    public TaskManagementService getTaskManagementService() {
        if (taskManagementService == null) {
            String msg = "Task management service is not initialized properly.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return taskManagementService;
    }

    public void setTaskManagementService(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    public MetadataManagementService getMetadataManagementService() {
        if (metadataManagementService == null) {
            String msg = "Metadata management service is not initialized properly.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return metadataManagementService;
    }

    public void setMetadataManagementService(MetadataManagementService metadataManagementService) {
        this.metadataManagementService = metadataManagementService;
    }

    public DeviceManagementProviderService getDeviceManagementProviderService() {
        if (deviceManagementProviderService == null) {
            String msg = "Device management provider service is not initialized properly.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

    public void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        this.deviceManagementProviderService = deviceManagementProviderService;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            String msg = "Realm service is not initialized properly.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    private static class ReferenceHolder {
        private static final DynamicTaskManagementExtensionServiceDataHolder INSTANCE =
                new DynamicTaskManagementExtensionServiceDataHolder();
    }
}
