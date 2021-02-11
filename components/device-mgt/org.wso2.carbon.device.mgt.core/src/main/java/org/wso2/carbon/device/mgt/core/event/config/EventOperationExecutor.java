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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationException;
import org.wso2.carbon.device.mgt.common.event.config.EventMetaData;
import org.wso2.carbon.device.mgt.common.event.config.EventOperation;
import org.wso2.carbon.device.mgt.common.event.config.EventRevokeOperation;
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

/**
 * Event create/revoke operation creation task.
 * Use at the time of single event create, update, delete
 */
public class EventOperationExecutor implements Runnable {
    private static final Log log = LogFactory.getLog(EventOperationExecutor.class);

    private final GroupManagementProviderService groupManagementService;
    private final List<Integer> groupIds;
    private final String eventSource;
    private final EventMetaData eventMetaData;
    private final int tenantId;
    private final String operationCode;

    public EventOperationExecutor(EventMetaData eventMetaData, List<Integer> groupIds, int tenantId,
                                  String eventSource, String operationCode) {
        this.groupManagementService = DeviceManagementDataHolder.getInstance().getGroupManagementProviderService();
        this.eventMetaData = eventMetaData;
        this.groupIds = groupIds;
        this.tenantId = tenantId;
        this.eventSource = eventSource;
        this.operationCode = operationCode;
    }

    /**
     * Build operation to create EVENT_REVOKE operation.
     * @param operation Operation object to build
     */
    private void buildEventRevokeOperation(ProfileOperation operation) {
        if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
            createGeoFenceRevokeOperation(operation);
        } //extend with another cases to handle other types of events
    }

    /**
     * Build operation to create EVENT_CONFIG operation.
     * @param operation Operation object to build
     * @throws EventConfigurationException Failed while build the operation object
     */
    private void buildEventConfigOperation(ProfileOperation operation) throws EventConfigurationException {
        if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
            createGeoFenceConfigOperation(operation);
        } //extend with another cases to handle other types of events
    }

    /**
     * Create EVENT_CONFIG operation object and attach payload to configure geo fence events
     * @param operation operation object to set the payload
     * @throws EventConfigurationException Failed while retrieving event list of geo fence
     */
    private void createGeoFenceConfigOperation(ProfileOperation operation) throws EventConfigurationException {
        GeoFenceEventMeta geoFenceMeta = (GeoFenceEventMeta) eventMetaData;
        try {
            GeoLocationProviderService geoLocationProviderService = DeviceManagementDataHolder
                    .getInstance().getGeoLocationProviderService();
            List<EventConfig> eventConfigList = geoLocationProviderService.getEventsOfGeoFence(geoFenceMeta.getId());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved event records of Geo Fence " + geoFenceMeta.getId() +
                        ". events " + Arrays.toString(eventConfigList.toArray()));
            }
            List<EventOperation> eventOperations = new ArrayList<>();
            EventOperation eventOperation = new EventOperation();
            eventOperation.setEventDefinition(eventMetaData);
            eventOperation.setEventSource(eventSource);
            eventOperation.setEventTriggers(eventConfigList);
            eventOperations.add(eventOperation);
            operation.setPayLoad(new Gson().toJson(eventOperations));
        }catch (GeoLocationBasedServiceException e) {
            throw new EventConfigurationException("Failed to retrieve event data of Geo fence " + geoFenceMeta.getId()
                    + " : " + geoFenceMeta.getFenceName(), e);
        }
    }

    /**
     * Create EVENT_REVOKE operation object and attach payload to configure geo fence events
     * @param operation operation object to set the payload
     */
    private void createGeoFenceRevokeOperation(ProfileOperation operation) {
        GeoFenceEventMeta geoFenceMeta = (GeoFenceEventMeta) eventMetaData;
        EventRevokeOperation eventRevokeOperation = new EventRevokeOperation();
        eventRevokeOperation.setEventSource(eventSource);
        eventRevokeOperation.setId(geoFenceMeta.getId());
        operation.setPayLoad(new Gson().toJson(eventRevokeOperation));
    }

    @Override
    public void run() {
        if (operationCode == null || groupIds == null || groupIds.isEmpty()) {
            log.error("No valid group ids or operation code found for create operations");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Starting " + operationCode + " operation creation task for event " + eventSource
                    + " tenant " + tenantId + " group Ids "+ Arrays.toString(groupIds.toArray()));
        }

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
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
                        if (log.isDebugEnabled()) {
                            log.debug("No devices found in group " + group.getName());
                        }
                    } else {
                        devices.addAll(allDevicesOfGroup);
                    }
                }
            } catch (GroupManagementException e) {
                log.error("Failed to retrieve devices of group with ID " + groupId + " and name " + group.getName(), e);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Starting " + operationCode + " operation creation task for event " + eventSource
                    + " tenant " + tenantId + " group Ids "+ Arrays.toString(groupIds.toArray()));
        }

        ProfileOperation operation = new ProfileOperation();
        operation.setType(Operation.Type.PROFILE);
        try {
            if (operationCode.equalsIgnoreCase(OperationMgtConstants.OperationCodes.EVENT_CONFIG)) {
                operation.setCode(OperationMgtConstants.OperationCodes.EVENT_CONFIG);
                buildEventConfigOperation(operation);
            } else if (operationCode.equalsIgnoreCase(OperationMgtConstants.OperationCodes.EVENT_REVOKE)){
                operation.setCode(OperationMgtConstants.OperationCodes.EVENT_REVOKE);
                buildEventRevokeOperation(operation);
            }
        } catch (EventConfigurationException e) {
            log.error("Event creation failed with message : " + e.getMessage(), e);
            return;
        }


        if (devices.isEmpty()) {
            log.warn("No devices found for the specified groups " + Arrays.toString(groupIds.toArray()));
            return;
        }
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device : devices) {
            if (device.getType().equalsIgnoreCase("android")) {
                //TODO introduce a proper mechanism for event handling for each device types
                deviceIdentifiers.add(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
            }
        }
        DeviceManagementProviderService deviceManagementProvider = DeviceManagementDataHolder
                .getInstance().getDeviceManagementProvider();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Creating event operations stared for devices" + Arrays.toString(deviceIdentifiers.toArray()));
            }
            deviceManagementProvider.addOperation("android", operation, deviceIdentifiers);
            //TODO introduce a proper mechanism
        } catch (OperationManagementException e) {
            log.error("Creating event operation failed with error ", e);
        } catch (InvalidDeviceException e) {
            log.error("Creating event operation failed.\n" +
                    "Could not found device/devices for the defined device identifiers.", e);
        }
    }
}
