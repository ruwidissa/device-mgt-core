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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationException;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationProviderService;
import org.wso2.carbon.device.mgt.common.event.config.EventOperation;
import org.wso2.carbon.device.mgt.common.event.config.EventRevokeOperation;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFenceEventMeta;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.geo.task.EventCreateCallback;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Event create/revoke operation creation task.
 * Use at the time of devices assign into group, remove from group.
 */
public class GroupAssignmentEventOperationExecutor implements Runnable {
    private static final Log log = LogFactory.getLog(GroupAssignmentEventOperationExecutor.class);

    private final int groupId;
    private final List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
    private final int tenantId;
    private final String operationCode;
    private final GeoLocationProviderService geoLocationProviderService;
    private final EventConfigurationProviderService eventConfigurationService;
    private EventCreateCallback callback;
    private List<String> eventSources;

    private List<GeofenceData> geoFencesOfGroup;

    public GroupAssignmentEventOperationExecutor(int groupId, List<DeviceIdentifier> deviceIdentifiers, int tenantId, String operationCode) {
        this.groupId = groupId;
        for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
            if (deviceIdentifier.getType().equalsIgnoreCase("android")) {
                this.deviceIdentifiers.add(deviceIdentifier);
            }
        }
        this.tenantId = tenantId;
        this.operationCode = operationCode;
        this.geoLocationProviderService = DeviceManagementDataHolder.getInstance().getGeoLocationProviderService();
        this.eventConfigurationService = DeviceManagementDataHolder.getInstance().getEventConfigurationService();
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Event creation operation started for devices with IDs " + Arrays.toString(deviceIdentifiers.toArray()));
            log.debug("Starting tenant flow for tenant with ID : " + tenantId);
        }

        try {
            this.eventSources = eventConfigurationService.getEventsSourcesOfGroup(groupId, tenantId);
            if (this.eventSources == null || this.eventSources.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No configured events for the queried group with ID " + groupId);
                }
                return;
            }
        } catch (EventConfigurationException e) {
            log.error("Failed while retrieving event records of group " + groupId + "of the tenant " + tenantId, e);
            return;
        }

        if (operationCode != null) {
            ProfileOperation operation = new ProfileOperation();
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            try {
                initEventMeta(); // Initialize all event based meta data of the group
                operation.setCode(operationCode);
                operation.setType(Operation.Type.PROFILE);
                if (operationCode.equalsIgnoreCase(OperationMgtConstants.OperationCodes.EVENT_CONFIG)) {
                    buildEventConfigOperationObject(operation);
                } else if (operationCode.equalsIgnoreCase(OperationMgtConstants.OperationCodes.EVENT_REVOKE)) {
                    buildEventRevokeOperation(operation);
                }
            } catch (EventConfigurationException e) {
                log.error("Failed to retrieve event sources of group " + groupId + ". Event creation operation failed.", e);
                return;
            }

            DeviceManagementProviderService deviceManagementProvider = DeviceManagementDataHolder
                    .getInstance().getDeviceManagementProvider();
            try {
                if (!deviceIdentifiers.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating event operations stared");
                    }
                    deviceManagementProvider.addOperation("android", operation, deviceIdentifiers); //TODO introduce a proper mechanism
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Device identifiers are empty, Hence ignoring adding event operation");
                    }
                }
            } catch (OperationManagementException e) {
                log.error("Creating event operation failed with error ", e);
            } catch (InvalidDeviceException e) {
                log.error("Creating event operation failed.\n" +
                        "Could not found device/devices for the defined device identifiers.", e);
            }
            if (callback != null) {
                callback.onCompleteEventOperation(null);
            }
        }

    }

    /**
     * This method is using for retrieve all types of events mapped with the specific group
     * @throws EventConfigurationException when fails to retrieve event data of the group
     */
    private void initEventMeta() throws EventConfigurationException {
        this.geoFencesOfGroup = getGeoFencesOfGroup();
        //Need to be added other event sources which will be declared in future
    }

    /**
     * Build EVENT_REVOKE operation attaching event payload
     * @param operation operation object to build
     * @throws EventConfigurationException if not events found for the specific group
     */
    private void buildEventRevokeOperation(ProfileOperation operation) throws EventConfigurationException {
        for (String eventSource : this.eventSources) {
            if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
                setGeoFenceRevokeOperationContent(operation);
            } //add other cases to handle other types of events
        }
    }

    /**
     * Build EVENT_CONFIG operation attaching event payload
     * @param operation operation object to build
     * @throws EventConfigurationException if not events found for the specific group
     */
    private void buildEventConfigOperationObject(ProfileOperation operation) throws EventConfigurationException {
        for (String eventSource : this.eventSources) {
            if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
                setGeoFenceConfigOperationContent(operation);
            } //add other cases to handle other types of events
        }
    }

    /**
     * Set operation payload GeoFence for EVENT_CONFIG operation
     * @param operation operation object to attach payload
     */
    private void setGeoFenceConfigOperationContent(ProfileOperation operation) {
        List<EventOperation> eventOperationList = new ArrayList<>();
        for (GeofenceData geofenceData : this.geoFencesOfGroup) {
            GeoFenceEventMeta geoFenceEventMeta = new GeoFenceEventMeta(geofenceData);
            EventOperation eventOperation = new EventOperation();
            eventOperation.setEventDefinition(geoFenceEventMeta);
            eventOperation.setEventSource(DeviceManagementConstants.EventServices.GEOFENCE);
            eventOperation.setEventTriggers(geofenceData.getEventConfig());
            eventOperationList.add(eventOperation);
        }
        operation.setPayLoad(new Gson().toJson(eventOperationList));
    }

    /**
     * Get geo fence list of the group
     * @return {@link GeofenceData} geofence data list of the geo fence
     * @throws EventConfigurationException error occurred while querying geo fences of group
     */
    private List<GeofenceData> getGeoFencesOfGroup() throws EventConfigurationException {
        List<GeofenceData> geoFencesOfGroup;
        try {
            geoFencesOfGroup = geoLocationProviderService.getGeoFencesOfGroup(groupId, tenantId, true);
        } catch (GeoLocationBasedServiceException e) {
            String msg = "Failed to get geo fences of the group";
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved " + geoFencesOfGroup.size() + " geo fences defined for the group " + groupId);
        }
        return geoFencesOfGroup;
    }

    /**
     * Set operation payload GeoFence for EVENT_REVOKE operation
     * @param operation operation object to attach payload
     */
    private void setGeoFenceRevokeOperationContent(ProfileOperation operation){
        List<EventRevokeOperation> revokeOperationList = new ArrayList<>();
        for (GeofenceData geofenceData : this.geoFencesOfGroup) {
            EventRevokeOperation eventRevokeOperation = new EventRevokeOperation();
            eventRevokeOperation.setEventSource(DeviceManagementConstants.EventServices.GEOFENCE);
            eventRevokeOperation.setId(geofenceData.getId());
            revokeOperationList.add(eventRevokeOperation);
        }
        operation.setPayLoad(new Gson().toJson(revokeOperationList));
    }

    /**
     * Can be used to set the task callback to call after the task completed successfully
     * @param callback Event callback object implemented inside the task starting class
     */
    public void setCallback(EventCreateCallback callback) {
        this.callback = callback;
    }
}
