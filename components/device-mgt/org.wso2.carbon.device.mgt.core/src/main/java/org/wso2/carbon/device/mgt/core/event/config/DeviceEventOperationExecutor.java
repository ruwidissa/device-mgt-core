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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationException;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationProviderService;
import org.wso2.carbon.device.mgt.common.event.config.EventOperation;
import org.wso2.carbon.device.mgt.common.exceptions.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoFenceEventMeta;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationBasedServiceException;
import org.wso2.carbon.device.mgt.common.geo.service.GeoLocationProviderService;
import org.wso2.carbon.device.mgt.common.geo.service.GeofenceData;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationMgtConstants;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import java.util.Arrays;
import java.util.List;

public class DeviceEventOperationExecutor implements Runnable {
    private static final Log log = LogFactory.getLog(DeviceEventOperationExecutor.class);

    private final int groupId;
    private final List<DeviceIdentifier> deviceIdentifiers;
    private final int tenantId;

    public DeviceEventOperationExecutor(int groupId, List<DeviceIdentifier> deviceIdentifiers, int tenantId) {
        this.groupId = groupId;
        this.deviceIdentifiers = deviceIdentifiers;
        this.tenantId = tenantId;
    }

    @Override
    public void run() {
        log.info("Starting event operation creation task for devices in group " + groupId + " tenant " + tenantId);
        if (log.isDebugEnabled()) {
            log.debug("Event creation operation started for devices with IDs " + Arrays.toString(deviceIdentifiers.toArray()));
        }
        ProfileOperation operation = new ProfileOperation();
        operation.setCode(OperationMgtConstants.OperationCodes.EVENT_CONFIG);
        operation.setType(Operation.Type.PROFILE);
        EventConfigurationProviderService eventConfigurationService = DeviceManagementDataHolder.getInstance().getEventConfigurationService();
        try {
            List<String> eventSources = eventConfigurationService.getEventsSourcesOfGroup(groupId, tenantId);
            if (eventSources == null || eventSources.isEmpty()) {
                log.info("No events applied for queried group with ID " + groupId);
            }
            for (String eventSource : eventSources) {
                if (eventSource.equalsIgnoreCase(DeviceManagementConstants.EventServices.GEOFENCE)) {
                    setGeoFenceOperationContent(operation);
                } //extend with another cases to handle other types of events
            }
        } catch (EventConfigurationException e) {
            log.error("Failed to retrieve event sources of group " + groupId, e);
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
    }

    private void setGeoFenceOperationContent(ProfileOperation operation) {
        log.info("Geo fence events found attached with group " + groupId + ", Started retrieving geo fences");
        GeoLocationProviderService geoLocationProviderService = DeviceManagementDataHolder.getInstance().getGeoLocationProviderService();
        try {
            List<GeofenceData> geoFencesOfGroup = geoLocationProviderService.getGeoFencesOfGroup(groupId, tenantId, true);
            if (log.isDebugEnabled()) {
                log.debug("Retrieved " + geoFencesOfGroup.size() + " geo fences defined for the group " + groupId);
            }
            for (GeofenceData geofenceData : geoFencesOfGroup) {
                GeoFenceEventMeta geoFenceEventMeta = new GeoFenceEventMeta(geofenceData);
                EventOperation eventOperation = new EventOperation();
                eventOperation.setEventDefinition(geoFenceEventMeta);
                eventOperation.setEventSource(DeviceManagementConstants.EventServices.GEOFENCE);
                eventOperation.setEventTriggers(geofenceData.getEventConfig());
                if (operation.getPayLoad() != null) {
                    operation.setPayLoad(operation.getPayLoad().toString().concat(eventOperation.toJSON()));
                } else {
                    operation.setPayLoad(eventOperation.toJSON());
                }
            }
        } catch (GeoLocationBasedServiceException e) {
            log.error("Failed to retrieve geo fences for group " + groupId);
        }
    }
}
