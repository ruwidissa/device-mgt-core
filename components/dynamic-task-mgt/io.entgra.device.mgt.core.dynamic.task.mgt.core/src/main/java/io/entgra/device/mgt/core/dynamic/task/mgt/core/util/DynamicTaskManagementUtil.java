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

package io.entgra.device.mgt.core.dynamic.task.mgt.core.util;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.OperationCode;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.NotFoundException;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.constant.Constants;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.internal.DynamicTaskManagementExtensionServiceDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicTaskManagementUtil {
    public static final Map<Integer, Map<String, Map<String, Long>>> tenantedDeviceTypeToOperationExecutedTimeMap =
            new ConcurrentHashMap<>();
    private static final Log log = LogFactory.getLog(DynamicTaskManagementUtil.class);
    private static final MetadataManagementService metadataManagementService =
            DynamicTaskManagementExtensionServiceDataHolder.getInstance().getMetadataManagementService();

    /**
     * Add dynamic task platform configuration to metadata registry.
     *
     * @param tenantDomain Tenant domain to add configurable categorized dynamic task configurations.
     * @throws DynamicTaskManagementException Throws when error encountered while adding configurations.
     */
    private static void addCategorizedDynamicTaskConfigurations(String tenantDomain) throws DynamicTaskManagementException {
        try {
            DynamicTaskPlatformConfigurations defaultConfigurableDynamicTaskPlatformConfigurations =
                    getDefaultConfigurableDynamicTaskPlatformConfigurations();

            Metadata configurableCategorizedDynamicTaskEntry = new Metadata();
            configurableCategorizedDynamicTaskEntry
                    .setMetaKey(tenantDomain + Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX);
            configurableCategorizedDynamicTaskEntry
                    .setMetaValue(DynamicTaskManagementExtensionServiceDataHolder
                            .getGson().toJson(defaultConfigurableDynamicTaskPlatformConfigurations));

            metadataManagementService.createMetadata(configurableCategorizedDynamicTaskEntry);
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg =
                    "Metadata entry already exists for [" + tenantDomain +
                            Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX + "].";
            log.error(msg, e);
            throw new DynamicTaskManagementException(msg, e);
        } catch (MetadataManagementException e) {
            String msg = "Error encountered while adding default categorized dynamic task configurations.";
            log.error(msg, e);
            throw new DynamicTaskManagementException(msg, e);
        }
    }

    /**
     * Add configurable dynamic task configuration entry.
     *
     * @throws DynamicTaskManagementException Throws when error encountered while adding configuration entry.
     */
    public static void addConfigurableDefaultPlatformConfigurationEntryToTenant() throws DynamicTaskManagementException {
        // Do not remove the tenant safety check, since the util procedure can be invoked by tenanted thread, even
        // though the API level restriction is ensured.
        String hostDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (Objects.equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            addCategorizedDynamicTaskConfigurations(hostDomain);
        } else {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                addCategorizedDynamicTaskConfigurations(hostDomain);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Get default dynamic task configurations.
     *
     * @return {@link DynamicTaskPlatformConfigurations}
     * @throws DynamicTaskManagementException Throws when error encountered while getting default configurations.
     */
    public static DynamicTaskPlatformConfigurations getDefaultConfigurableDynamicTaskPlatformConfigurations()
            throws DynamicTaskManagementException {

        if (DynamicTaskManagementExtensionServiceDataHolder.getInstance()
                .getDefaultConfigurableDynamicTaskPlatformConfigurations() != null) {
            return DynamicTaskManagementExtensionServiceDataHolder.getInstance()
                    .getDefaultConfigurableDynamicTaskPlatformConfigurations();
        }

        try {
            Metadata defaultCategorizedDynamicTaskEntry;
            // Do not remove the tenant safety check, since the util procedure can be invoked by tenanted thread, even
            // though the API level restriction is ensured.
            if (Objects.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain())) {
                defaultCategorizedDynamicTaskEntry =
                        metadataManagementService.retrieveMetadata(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME +
                                Constants.CONFIG_PREFIX.DEFAULT_CATEGORIZED_DYNAMIC_TASKS_TEMPLATE_PREFIX);
            } else {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    defaultCategorizedDynamicTaskEntry =
                            metadataManagementService.retrieveMetadata(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME +
                                    Constants.CONFIG_PREFIX.DEFAULT_CATEGORIZED_DYNAMIC_TASKS_TEMPLATE_PREFIX);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            if (defaultCategorizedDynamicTaskEntry == null) {
                throw new IllegalStateException("Default categorized dynamic task management configs cannot be found.");
            }

            DynamicTaskPlatformConfigurations dynamicTaskPlatformConfigurations =
                    DynamicTaskManagementExtensionServiceDataHolder.getGson()
                            .fromJson(defaultCategorizedDynamicTaskEntry.getMetaValue(),
                                    DynamicTaskPlatformConfigurations.class);
            for (CategorizedDynamicTask categorizedDynamicTask :
                    dynamicTaskPlatformConfigurations.getCategorizedDynamicTasks()) {
                categorizedDynamicTask.setDeviceTypes(categorizedDynamicTask.getConfigurableDeviceTypes());
            }
            DynamicTaskManagementExtensionServiceDataHolder.getInstance().setDefaultConfigurableDynamicTaskPlatformConfigurations(dynamicTaskPlatformConfigurations);
            return dynamicTaskPlatformConfigurations;
        } catch (MetadataManagementException e) {
            String msg =
                    "Error encountered while retrieving metadata entry for key [" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME +
                            Constants.CONFIG_PREFIX.DEFAULT_CATEGORIZED_DYNAMIC_TASKS_TEMPLATE_PREFIX + "].";
            log.error(msg, e);
            throw new DynamicTaskManagementException(msg, e);
        }
    }

    /**
     * Get dynamic task platform configurations for specified tenant.
     *
     * @param tenantDomain Tenant domain to retrieve dynamic task platform configurations.
     * @return {@link DynamicTaskPlatformConfigurations}
     * @throws NotFoundException              Throws when dynamic task platform configurations entry is not found for
     *                                        the
     *                                        specified tenant.
     * @throws DynamicTaskManagementException Throws when error encountered while retrieving platform configurations.
     */
    public static DynamicTaskPlatformConfigurations getDynamicTaskPlatformConfigurations(String tenantDomain)
            throws NotFoundException, DynamicTaskManagementException {
        try {
            Metadata configurableDynamicTaskPlatformConfigurationEntry;
            // Do not remove the tenant safety check, since the util procedure can be invoked by tenanted thread, even
            // though the API level restriction is ensured.
            if (Objects.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME,
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain())) {
                configurableDynamicTaskPlatformConfigurationEntry =
                        metadataManagementService
                                .retrieveMetadata(tenantDomain + Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX);
            } else {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                    configurableDynamicTaskPlatformConfigurationEntry =
                            metadataManagementService
                                    .retrieveMetadata(tenantDomain + Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }

            if (configurableDynamicTaskPlatformConfigurationEntry == null) {
                String msg =
                        "Cannot find a configuration entry for key [" + tenantDomain +
                                Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX + "].";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return DynamicTaskManagementExtensionServiceDataHolder.getGson().fromJson(configurableDynamicTaskPlatformConfigurationEntry.getMetaValue(),
                    DynamicTaskPlatformConfigurations.class);
        } catch (MetadataManagementException e) {
            String msg =
                    "Error encountered while retrieving metadata entry for key [" + tenantDomain +
                            Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX + "].";
            log.error(msg, e);
            throw new DynamicTaskManagementException(msg, e);
        }
    }

    /**
     * Generate tenant aware NTask name.
     *
     * @param tenantDomain Tenant domain.
     * @param taskName     Task name.
     * @return Tenant aware NTask name.
     */
    public static String generateTenantAwareNTaskName(String tenantDomain, String taskName) {
        return tenantDomain + Constants.TASK_NAME_SEPARATOR + taskName + Constants.TASK_PREFIX;
    }

    /**
     * Get operation to operation executed timestamp map.
     *
     * @param tenantId   Tenant ID.
     * @param deviceType Device Type.
     * @return Map of operation codes to their executed timestamp map.
     */
    public static Map<String, Long> getOperationExecutedTimeMap(int tenantId, String deviceType) {
        tenantedDeviceTypeToOperationExecutedTimeMap.computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>());
        tenantedDeviceTypeToOperationExecutedTimeMap.get(tenantId).computeIfAbsent(deviceType, k -> new ConcurrentHashMap<>());
        return tenantedDeviceTypeToOperationExecutedTimeMap.get(tenantId).get(deviceType);
    }

    /**
     * Populate configurable device types of operations.
     *
     * @param dynamicTaskPlatformConfigurations {@link DynamicTaskPlatformConfigurations}
     */
    public static void populateConfigurableDeviceTypes(DynamicTaskPlatformConfigurations dynamicTaskPlatformConfigurations) {
        Set<String> configurableDeviceTypes;
        for (CategorizedDynamicTask categorizedDynamicTask :
                dynamicTaskPlatformConfigurations.getCategorizedDynamicTasks()) {
            configurableDeviceTypes = new HashSet<>();
            for (OperationCode operationCode : categorizedDynamicTask.getOperationCodes()) {
                configurableDeviceTypes.addAll(operationCode.getSupportingDeviceTypes());
            }
            categorizedDynamicTask.setConfigurableDeviceTypes(configurableDeviceTypes);
        }
    }
}
