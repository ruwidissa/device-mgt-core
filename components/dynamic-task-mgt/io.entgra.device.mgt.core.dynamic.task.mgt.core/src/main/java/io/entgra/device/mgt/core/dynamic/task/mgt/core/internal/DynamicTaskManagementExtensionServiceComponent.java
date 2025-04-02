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

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.DynamicTaskConfigurationManagementService;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.DynamicTaskConfigurationManagementServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import java.util.Dictionary;
import java.util.Hashtable;

@Component(
        name = "io.entgra.device.mgt.core.dynamic.task.mgt.core.internal" +
                ".DynamicTaskManagementExtensionServiceComponent",
        immediate = true
)
@SuppressWarnings("unused")
public class DynamicTaskManagementExtensionServiceComponent {
    private static final Log log = LogFactory.getLog(DynamicTaskManagementExtensionServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.info(DynamicTaskManagementExtensionServiceComponent.class.getName() + " initialization got start.");
        }
        try {
            componentContext.getBundleContext().registerService(DynamicTaskConfigurationManagementService.class,
                    DynamicTaskConfigurationManagementServiceImpl.getInstance(), null);
            initiateObserverRegistration(componentContext);
        } catch (Throwable t) {
            log.error(DynamicTaskManagementExtensionServiceComponent.class.getName() + " initialization is failed.");
        }
    }

    private void initiateObserverRegistration(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.info("Starting to register server life cycle observers.");
        }
        componentContext.getBundleContext().registerService(ServerStartupObserver.class,
                new DynamicTaskManagementServiceServerStartupObserver(), getStartupObserverServiceDictionary());
        componentContext.getBundleContext().registerService(TenantMgtListener.class,
                new TenantCreateObserver(), null);
    }

    @Reference(
            name = "io.entgra.task.mgt.service",
            service = io.entgra.device.mgt.core.task.mgt.common.spi.TaskManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTaskManagementService"
    )
    protected void setTaskManagementService(TaskManagementService taskManagementService) {
        DynamicTaskManagementExtensionServiceDataHolder.getInstance().setTaskManagementService(taskManagementService);
        if (log.isDebugEnabled()) {
            log.info(TaskManagementService.class.getName() + " initialized successfully.");
        }
    }

    protected void unsetTaskManagementService(TaskManagementService taskManagementService) {
        DynamicTaskManagementExtensionServiceDataHolder.getInstance().setTaskManagementService(null);
        if (log.isDebugEnabled()) {
            log.info(TaskManagementService.class.getName() + " uninitialized successfully.");
        }
    }

    @Reference(
            name = "io.entgra.meta.mgt.service",
            service = io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetadataManagementService"
    )
    protected void setMetadataManagementService(MetadataManagementService metadataManagementService) {
        DynamicTaskManagementExtensionServiceDataHolder.getInstance().setMetadataManagementService(metadataManagementService);
        if (log.isDebugEnabled()) {
            log.info(MetadataManagementService.class.getName() + " initialized successfully.");
        }
    }

    protected void unsetMetadataManagementService(MetadataManagementService metadataManagementService) {
        DynamicTaskManagementExtensionServiceDataHolder.getInstance().setMetadataManagementService(null);
        if (log.isDebugEnabled()) {
            log.info(MetadataManagementService.class.getName() + " uninitialized successfully.");
        }
    }

    @Reference(
            name = "io.entgra.device.mgt.provider.service",
            service = io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDeviceManagementProviderService"
    )
    protected void setDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        DynamicTaskManagementExtensionServiceDataHolder.getInstance().setDeviceManagementProviderService(deviceManagementProviderService);
        if (log.isDebugEnabled()) {
            log.info(DeviceManagementProviderService.class.getName() + " initialized successfully.");
        }
    }

    protected void unsetDeviceManagementProviderService(DeviceManagementProviderService deviceManagementProviderService) {
        DynamicTaskManagementExtensionServiceDataHolder.getInstance().setDeviceManagementProviderService(null);
        if (log.isDebugEnabled()) {
            log.info(DeviceManagementProviderService.class.getName() + " uninitialized successfully.");
        }
    }

    private Dictionary<String, Object> getStartupObserverServiceDictionary() {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        // This property will ensure that the server startup listener service will trigger after of the heartbeat
        // service's server startup listener.
        dictionary.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
        return dictionary;
    }
}
