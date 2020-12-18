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
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.event.config.EventOperation;
import org.wso2.carbon.device.mgt.common.event.config.EventRevokeOperation;
import org.wso2.carbon.device.mgt.common.event.config.EventTaskEntry;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFenceEventMeta;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EventConfigDAO;
import org.wso2.carbon.device.mgt.core.dao.EventManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.GeofenceDAO;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.impl.RandomlyAssignedScheduleTask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Event create/revoke operation creation task.
 * Use at the time of single event create, update, delete
 */
public class GroupEventOperationCreationTask extends RandomlyAssignedScheduleTask {
    private static final Log log = LogFactory.getLog(GroupEventOperationCreationTask.class);
    private static final String TASK_NAME = "GROUP_EVENT_CREATE_TASK";

    private GroupManagementProviderService groupManagementService;
    private EventConfigDAO eventConfigDAO;
    private GeofenceDAO geofenceDAO;
    private int tenantId;

    @Override
    protected void setup() {
        if (this.groupManagementService == null) {
            this.groupManagementService = DeviceManagementDataHolder.getInstance().getGroupManagementProviderService();
        }
        if (this.eventConfigDAO == null) {
            this.eventConfigDAO = DeviceManagementDAOFactory.getEventConfigDAO();
        }
        if (this.geofenceDAO == null) {
            this.geofenceDAO = DeviceManagementDAOFactory.getGeofenceDAO();
        }
    }

    @Override
    protected void executeRandomlyAssignedTask() {
        if (log.isDebugEnabled()) {
            log.debug("Starting event operation creation task");
        }
        try {
            DeviceManagementDAOFactory.openConnection();
            Map<Integer, List<EventTaskEntry>> eventTaskEntries = eventConfigDAO
                    .getAvailableEventTaskEntries(EventTaskEntry.ExecutionStatus.CREATED.toString());
            DeviceManagementDAOFactory.closeConnection();
            for (Map.Entry<Integer, List<EventTaskEntry>> eventTaskEntry : eventTaskEntries.entrySet()) {
                this.tenantId = eventTaskEntry.getKey();
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(this.tenantId, true);
                this.processEventsOfTenant(eventTaskEntry.getValue());
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (EventManagementDAOException e) {
            log.error("Failed while retrieving event task entries", e);
        } catch (SQLException e) {
            log.error("Failed while opening connection", e);
        }
    }

    /**
     * Process event entries of a particular tenant
     * @param eventTaskEntries list of geo fence task entries
     */
    private void processEventsOfTenant(List<EventTaskEntry> eventTaskEntries) {
        Map<String, List<EventTaskEntry>> eventSourceTaskMap = new HashMap<>();
        for (EventTaskEntry eventTaskEntry : eventTaskEntries) {
            List<EventTaskEntry> eventsOfSource = eventSourceTaskMap.get(eventTaskEntry.getEventSource());
            if (eventsOfSource == null) {
                eventsOfSource = new ArrayList<>();
            }
            eventsOfSource.add(eventTaskEntry);
            eventSourceTaskMap.put(eventTaskEntry.getEventSource(), eventsOfSource);
        }
        for (Map.Entry<String, List<EventTaskEntry>> eventEntry : eventSourceTaskMap.entrySet()) {
            switch (eventEntry.getKey()) {
                case DeviceManagementConstants.EventServices.GEOFENCE :
                    processGeoFenceEventList(eventEntry.getValue());
                    break;
                default:
                    log.error("Invalid event source" + eventEntry.getKey());
            }
        }
    }

    /**
     * Devide event task entries into configuration entries and revoke entries
     * @param geoFenceTaskList list of geo fence task entries
     */
    private void processGeoFenceEventList(List<EventTaskEntry> geoFenceTaskList) {
        Map<Integer, List<Integer>> geoFenceConfigEventMap = new HashMap<>();
        Map<Integer, List<Integer>> geoFenceRevokeEventMap = new HashMap<>();
        for (EventTaskEntry geoFenceTask : geoFenceTaskList) {
            if (geoFenceTask.getOperationCode().equals(OperationMgtConstants.OperationCodes.EVENT_CONFIG)) {
                List<Integer> groupIdList = geoFenceConfigEventMap.get(geoFenceTask.getEventMetaId());
                if (groupIdList == null) {
                    groupIdList = new ArrayList<>();
                }
                groupIdList.add(geoFenceTask.getGroupId());
                geoFenceConfigEventMap.put(geoFenceTask.getEventMetaId(), groupIdList);
            } else if (geoFenceTask.getOperationCode().equals(OperationMgtConstants.OperationCodes.EVENT_REVOKE)) {
                List<Integer> groupIdList = geoFenceRevokeEventMap.get(geoFenceTask.getEventMetaId());
                if (groupIdList == null) {
                    groupIdList = new ArrayList<>();
                }
                groupIdList.add(geoFenceTask.getGroupId());
                geoFenceRevokeEventMap.put(geoFenceTask.getEventMetaId(), groupIdList);
            }
        }
        if (!geoFenceConfigEventMap.isEmpty()) {
            buildGeoFenceConfigOperation(geoFenceConfigEventMap);
        }
        if (!geoFenceRevokeEventMap.isEmpty()) {
            buildGeoFenceRevokeOperation(geoFenceRevokeEventMap);
        }
    }

    /**
     * Build EVENT_REVOKE operation for geo fence event operation
     * @param geoFenceRevokeEventMap group Ids mapped with geofence ID
     */
    private void buildGeoFenceRevokeOperation(Map<Integer, List<Integer>> geoFenceRevokeEventMap) {
        try {
            DeviceManagementDAOFactory.openConnection();
            for (Map.Entry<Integer, List<Integer>> eventGroupEntry : geoFenceRevokeEventMap.entrySet()) {
                ProfileOperation revokeOperation = new ProfileOperation();
                revokeOperation.setType(Operation.Type.PROFILE);
                revokeOperation.setCode(OperationMgtConstants.OperationCodes.EVENT_REVOKE);

                GeofenceData geofence = geofenceDAO.getGeofence(eventGroupEntry.getKey());
                EventRevokeOperation eventRevokeOperation = new EventRevokeOperation();
                eventRevokeOperation.setEventSource(DeviceManagementConstants.EventServices.GEOFENCE);
                eventRevokeOperation.setId(geofence.getId());

                JSONArray payloadArray = new JSONArray();
                payloadArray.put(new JSONObject(eventRevokeOperation));
                revokeOperation.setPayLoad(payloadArray.toString());
                createOperationEntryForGroup(revokeOperation, DeviceManagementConstants.EventServices.GEOFENCE,
                        eventGroupEntry.getValue());
            }
            DeviceManagementDAOFactory.closeConnection();
        } catch (SQLException e) {
            log.error("Failed while opening connection", e);
        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while retrieving geofence data", e);
        }
    }

    /**
     * Build EVENT_CONFIG operation for geo fence event operation
     * @param geoFenceConfigEventMap group Ids mapped with geofence ID
     */
    private void buildGeoFenceConfigOperation(Map<Integer, List<Integer>> geoFenceConfigEventMap) {
        try {
            DeviceManagementDAOFactory.openConnection();
            for (Map.Entry<Integer, List<Integer>> eventGroupEntry : geoFenceConfigEventMap.entrySet()) {
                ProfileOperation configOperation = new ProfileOperation();
                configOperation.setType(Operation.Type.PROFILE);
                configOperation.setCode(OperationMgtConstants.OperationCodes.EVENT_CONFIG);

                GeofenceData geofence = geofenceDAO.getGeofence(eventGroupEntry.getKey());
                List<EventConfig> eventsOfGeoFence = geofenceDAO.getEventsOfGeoFence(geofence.getId());
                EventOperation eventOperation = new EventOperation();
                eventOperation.setEventDefinition(new GeoFenceEventMeta(geofence));
                eventOperation.setEventSource(DeviceManagementConstants.EventServices.GEOFENCE);
                eventOperation.setEventTriggers(eventsOfGeoFence);
                JSONArray payloadArray = new JSONArray();
                payloadArray.put(new JSONObject(eventOperation));
                configOperation.setPayLoad(payloadArray.toString());
                createOperationEntryForGroup(configOperation, DeviceManagementConstants.EventServices.GEOFENCE,
                        eventGroupEntry.getValue());
            }
            DeviceManagementDAOFactory.closeConnection();
        } catch (SQLException e) {
            log.error("Failed while opening connection", e);
        } catch (DeviceManagementDAOException e) {
            log.error("Error occurred while retrieving geofence data", e);
        }
    }

    /**
     * Create event operation of the groups
     * @param operation creating operation
     * @param eventSource event source type
     * @param groupIds group Id list
     */
    private void createOperationEntryForGroup(ProfileOperation operation, String eventSource, List<Integer> groupIds) {
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
                        log.warn("No devices found in group " + group.getName());
                    } else {
                        devices.addAll(allDevicesOfGroup);
                    }
                }
            } catch (GroupManagementException e) {
                log.error("Failed to retrieve devices of group with ID " + groupId + " and name " + group.getName(), e);
            }
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
            return;
        } catch (InvalidDeviceException e) {
            log.error("Creating event operation failed.\n" +
                    "Could not found device/devices for the defined device identifiers.", e);
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Event operation creation task completed");
        }
        try {
            eventConfigDAO.setEventTaskComplete(operation.getCode(), eventSource, groupIds, this.tenantId);
        } catch (EventManagementDAOException e) {
            log.error("Failed while updating event task records", e);
        }
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public void setProperties(Map<String, String> map) {

    }
}
