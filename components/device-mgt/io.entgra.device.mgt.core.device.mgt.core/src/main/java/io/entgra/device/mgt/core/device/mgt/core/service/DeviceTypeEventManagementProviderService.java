/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEventUpdateResult;

import java.util.List;

/**
 * This interface defines the contract for managing event definitions associated with a device type.
 * It includes operations to create, update, retrieve, and delete device type event metadata.
 */
public interface DeviceTypeEventManagementProviderService {

    /**
     * Retrieves a list of event definitions associated with a given device type.
     *
     * @param deviceType The name of the device type.
     * @return A list of {@link DeviceTypeEvent} instances representing event definitions.
     * @throws DeviceManagementException If an error occurs while fetching the definitions.
     */
    List<DeviceTypeEvent> getDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException;

    /**
     * Retrieves the event definitions for a device type in JSON string format.
     *
     * @param deviceType The name of the device type.
     * @return A JSON string representing the event definitions.
     * @throws DeviceManagementException If an error occurs while generating or retrieving the JSON.
     */
    String getDeviceTypeEventDefinitionsAsJson(String deviceType) throws DeviceManagementException;

    /**
     * Checks whether metadata for the given device type already exists.
     *
     * @param deviceType The name of the device type.
     * @return {@code true} if metadata exists, {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs during the check.
     */
    boolean isDeviceTypeMetaExist(String deviceType) throws DeviceManagementException;

    /**
     * Persists metadata and event definitions for the specified device type.
     *
     * @param deviceType        The name of the device type.
     * @param deviceTypeEvents  A list of event definitions to be associated with the device type.
     * @return {@code true} if the operation is successful, {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs during the creation process.
     */
    boolean createDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents) throws DeviceManagementException;

    /**
     * Updates metadata and event definitions for the specified device type.
     *
     * @param deviceType        The name of the device type.
     * @param deviceTypeEvents  A list of updated event definitions to be associated with the device type.
     * @return {@code true} if the update is successful, {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs during the update process.
     */
    boolean updateDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementException;

    /**
     * Deletes all event definitions associated with the given device type.
     *
     * @param deviceType The name of the device type.
     * @return {@code true} if deletion is successful, {@code false} otherwise.
     * @throws DeviceManagementException If an error occurs during the deletion process.
     */
    boolean deleteDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException;

    /**
     * Computes the updated and merged device type event definitions for the given device type.
     * <p>
     * This method compares the incoming list of {@link DeviceTypeEvent} definitions with the existing ones
     * for the specified device type. It identifies:
     * <ul>
     *     <li>Events that are new or modified ({@code updatedEvents})</li>
     *     <li>Events that are unchanged or retained ({@code mergedEvents}, includes both updated and unchanged)</li>
     * </ul>
     * This is typically used to determine whether an update operation is needed and to construct the
     * final event definition set to be persisted.
     *
     * @param deviceType      the name of the device type whose events are being updated
     * @param incomingEvents  the list of incoming event definitions from the request
     * @return a {@link DeviceTypeEventUpdateResult} containing lists of updated and merged events
     * @throws DeviceManagementException if there is an error retrieving existing event definitions
     */
    DeviceTypeEventUpdateResult computeUpdatedDeviceTypeEvents(String deviceType, List<DeviceTypeEvent> incomingEvents)
            throws DeviceManagementException;
}
