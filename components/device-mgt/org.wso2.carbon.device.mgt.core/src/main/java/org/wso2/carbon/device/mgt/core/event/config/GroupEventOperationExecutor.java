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
public class GroupEventOperationExecutor implements Runnable {
    private static final Log log = LogFactory.getLog(GroupEventOperationExecutor.class);

    private final int groupId;
    private final List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
    private final int tenantId;
    private final String operationCode;
    private final GeoLocationProviderService geoLocationProviderService;
    private final EventConfigurationProviderService eventConfigurationService;
    private EventCreateCallback callback;

    public GroupEventOperationExecutor(int groupId, List<DeviceIdentifier> deviceIdentifiers, int tenantId, String operationCode) {
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
        log.info("Starting event operation creation task for devices in group " + groupId + " tenant " + tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Event creation operation started for devices with IDs " + Arrays.toString(deviceIdentifiers.toArray()));
            log.debug("Starting tenant flow for tenant with ID : " + tenantId);
        }
        ProfileOperation operation = new ProfileOperation();
        if (operationCode != null) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            try {
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
                    log.info("Device identifiers are empty, Hence ignoring adding event operation");
                }
            } catch (OperationManagementException e) {
                log.error("Creating event operation failed with error ", e);
            } catch (InvalidDeviceException e) {
                log.error("Creating event operation failed.\n" +
                        "Could not found device/devices for the defined device identifiers.", e);
            }
            log.info("Event operation creation succeeded");
            if (callback != null) {
                callback.onCompleteEventOperation(null);
            }
        }

    }

    /**
     * Build EVENT_REVOKE operation attaching event payload
     * @param operation operation object to build
     * @throws EventConfigurationException if not events found for the specific group
     */
    private void buildEventRevokeOperation(ProfileOperation operation) throws EventConfigurationException {
        List<String> eventSources = eventConfigurationService.getEventsSourcesOfGroup(groupId, tenantId);
        if (eventSources == null || eventSources.isEmpty()) {
            String msg = "No events applied for queried group with ID " + groupId;
            log.info(msg);
            throw new EventConfigurationException(msg);
        }
        for (String eventSource : eventSources) {
            if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
                setGeoFenceRevokeOperationContent(operation);
            } //extend with another cases to handle other types of events
        }
    }

    /**
     * Build EVENT_CONFIG operation attaching event payload
     * @param operation operation object to build
     * @throws EventConfigurationException if not events found for the specific group
     */
    private void buildEventConfigOperationObject(ProfileOperation operation) throws EventConfigurationException {
        List<String> eventSources = eventConfigurationService.getEventsSourcesOfGroup(groupId, tenantId);
        if (eventSources == null || eventSources.isEmpty()) {
            String msg = "No events applied for queried group with ID " + groupId;
            log.info(msg);
            throw new EventConfigurationException(msg);
        }
        for (String eventSource : eventSources) {
            if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
                setGeoFenceConfigOperationContent(operation);
            } //extend with another cases to handle other types of events
        }
    }

    /**
     * Set operation payload GeoFence for EVENT_CONFIG operation
     * @param operation operation object to attach payload
     * @throws EventConfigurationException failed while getting events of group
     */
    private void setGeoFenceConfigOperationContent(ProfileOperation operation) throws EventConfigurationException {
        log.info("Geo fence events found attached with group " + groupId + ", Started retrieving geo fences");
        List<GeofenceData> geoFencesOfGroup = getGeoFencesOfGroup();
        List<EventOperation> eventOperationList = new ArrayList<>();
        for (GeofenceData geofenceData : geoFencesOfGroup) {
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
     * @throws EventConfigurationException failed while getting events of group
     */
    private void setGeoFenceRevokeOperationContent(ProfileOperation operation) throws EventConfigurationException {
        List<GeofenceData> geoFencesOfGroup = getGeoFencesOfGroup();
        List<EventRevokeOperation> revokeOperationList = new ArrayList<>();
        for (GeofenceData geofenceData : geoFencesOfGroup) {
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
