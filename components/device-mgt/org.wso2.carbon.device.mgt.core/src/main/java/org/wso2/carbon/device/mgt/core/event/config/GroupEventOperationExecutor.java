/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.event.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.event.config.EventMetaData;
import org.wso2.carbon.device.mgt.common.event.config.EventOperation;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFenceEventMeta;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupEventOperationExecutor implements Runnable {
    private static final Log log = LogFactory.getLog(GroupEventOperationExecutor.class);

    private final List<Integer> groupIds;
    private final String eventSource;
    private final EventMetaData eventMetaData;
    private final int tenantId;

    public GroupEventOperationExecutor(EventMetaData eventMetaData, List<Integer> groupIds, int tenantId, String eventSource) {
        this.eventMetaData = eventMetaData;
        this.groupIds = groupIds;
        this.tenantId = tenantId;
        this.eventSource = eventSource;
    }

    @Override
    public void run() {
        log.info("Starting event operation creation task for event " + eventSource + " tenant " + tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Event creation operation started for groups with IDs " + Arrays.toString(groupIds.toArray()));
        }
        ProfileOperation operation = new ProfileOperation();
        operation.setCode(OperationMgtConstants.OperationCodes.EVENT_CONFIG);
        operation.setType(Operation.Type.PROFILE);
        if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
            createGeoFenceOperation(operation);
        } //extend with another cases to handle other types of events

        if (log.isDebugEnabled()) {
            log.debug("Starting tenant flow for tenant id " + tenantId);
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
        GroupManagementProviderService groupManagementService = DeviceManagementDataHolder
                .getInstance().getGroupManagementProviderService();
        Set<Device> devices = new HashSet<>();
        for (Integer groupId : groupIds) {
            DeviceGroup group;
            try {
                group = groupManagementService.getGroup(groupId, false);
            } catch (GroupManagementException e) {
                log.error("Failed to retrieve group with group ID " + groupId, e);
                continue;
            }
            try {
                if (group != null) {
                    List<Device> allDevicesOfGroup = groupManagementService.getAllDevicesOfGroup(group.getName(), false);
                    if (allDevicesOfGroup == null || allDevicesOfGroup.isEmpty()) {
                        log.info("No devices found in group " + group.getName());
                    } else {
                        devices.addAll(allDevicesOfGroup);
                    }
                }
            } catch (GroupManagementException e) {
                log.error("Failed to retrieve devices of group with ID " + groupId + " and name " + group.getName(), e);
            }
        }
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            if (device.getType().equalsIgnoreCase("android")) { //TODO introduce a proper mechanism for event handling for each device types
                deviceIdentifiers.add(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            }
        }
        DeviceManagementProviderService deviceManagementProvider = DeviceManagementDataHolder
                .getInstance().getDeviceManagementProvider();
        try {
            if (!deviceIdentifiers.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating event operations stared for devices" + Arrays.toString(deviceIdentifiers.toArray()));
                }
                deviceManagementProvider.addOperation("android", operation, deviceIdentifiers); //TODO introduce a proper mechanism
            } else {
                log.info("Device identifiers are empty, Hence ignoring adding event operation");
            }
        } catch (OperationManagementException e) {
            log.error("Creating event operation failed with error ", e);
        } catch (InvalidDeviceException e) {
            log.error("Creating event operation failed.\n" +
                    "Could not found device/devices for the defined device identifiers.", e);
        }
        log.info("Event operation creation succeeded");
    }

    private void createGeoFenceOperation(ProfileOperation operation) {
        GeoFenceEventMeta geoFenceMeta = (GeoFenceEventMeta) eventMetaData;
        try {
            GeoLocationProviderService geoLocationProviderService = DeviceManagementDataHolder
                    .getInstance().getGeoLocationProviderService();
            List<EventConfig> eventConfigList = geoLocationProviderService.getEventsOfGeoFence(geoFenceMeta.getId());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved event records of Geo Fence " + geoFenceMeta.getId() +
                        ". events " + Arrays.toString(eventConfigList.toArray()));
            }
            EventOperation eventOperation = new EventOperation();
            eventOperation.setEventDefinition(eventMetaData);
            eventOperation.setEventSource(eventSource);
            eventOperation.setEventTriggers(eventConfigList);
            operation.setPayLoad(eventOperation.toJSON());
        } catch (GeoLocationBasedServiceException e) {
            log.error("Failed to retrieve event data of Geo fence " + geoFenceMeta.getId()
                    + " : " + geoFenceMeta.getFenceName(), e);
        }
    }
}
