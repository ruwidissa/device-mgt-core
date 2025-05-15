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

package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;

import java.sql.SQLException;
import java.util.List;

/**
 * This class represents the key dao operations associated with persisting and retrieving
 * device type event related information.
 */
public interface DeviceTypeEventDAO {

    /**
     * Retrieves a list of event definitions associated with a specific device type and tenant.
     *
     * @param deviceType The device type for which events are to be retrieved.
     * @param tenantId   The tenant ID.
     * @return A list of {@link DeviceTypeEvent} objects.
     * @throws DeviceManagementDAOException If an error occurs while accessing the data store.
     */
    List<DeviceTypeEvent> getDeviceTypeEventDefinitions(String deviceType, int tenantId) throws DeviceManagementDAOException;

    /**
     * Persists the device type metadata along with associated event definitions.
     *
     * @param deviceType       The device type to be saved.
     * @param tenantId         The tenant ID.
     * @param deviceTypeEvents The list of event definitions to be associated with the device type.
     * @return {@code true} if the operation is successful, {@code false} otherwise.
     * @throws DeviceManagementDAOException If an error occurs while persisting the data.
     */
    boolean createDeviceTypeMetaWithEvents(String deviceType, int tenantId,
                                           List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementDAOException;

    /**
     * Updates the metadata and event definitions of an existing device type.
     *
     * @param deviceType       The device type to be updated.
     * @param tenantId         The tenant ID.
     * @param deviceTypeEvents The updated list of event definitions.
     * @return {@code true} if the update is successful, {@code false} otherwise.
     * @throws DeviceManagementDAOException If an error occurs during the update.
     */
    boolean updateDeviceTypeMetaWithEvents(String deviceType, int tenantId, List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementDAOException;

    /**
     * Deletes all event definitions associated with a given device type.
     *
     * @param deviceType The device type whose events should be deleted.
     * @param tenantId   The tenant ID.
     * @return {@code true} if the deletion is successful, {@code false} otherwise.
     * @throws DeviceManagementDAOException If an error occurs during deletion.
     */
    boolean deleteDeviceTypeEventDefinitions(String deviceType, int tenantId) throws DeviceManagementDAOException;

    /**
     * Retrieves the event definitions of a device type in JSON format.
     *
     * @param deviceType The device type for which event definitions are retrieved.
     * @param tenantId   The tenant ID.
     * @return A JSON string representation of the event definitions.
     * @throws DeviceManagementDAOException If a DAO-level error occurs.
     * @throws SQLException                 If a database access error occurs.
     */
    String getDeviceTypeEventDefinitionsAsJson(String deviceType, int tenantId) throws DeviceManagementDAOException, SQLException;

    /**
     * Checks if metadata for a given device type exists.
     *
     * @param deviceType The device type to check.
     * @param tenantId   The tenant ID.
     * @return {@code true} if metadata exists, {@code false} otherwise.
     * @throws DeviceManagementDAOException If an error occurs during the check.
     */
    boolean isDeviceTypeMetaExist(String deviceType, int tenantId) throws DeviceManagementDAOException;
}
