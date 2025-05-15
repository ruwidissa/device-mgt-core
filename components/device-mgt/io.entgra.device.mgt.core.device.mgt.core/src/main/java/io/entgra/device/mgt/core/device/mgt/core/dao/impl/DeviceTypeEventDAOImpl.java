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
package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceTypeEventDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.ATTRIBUTES;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.EVENT_ATTRIBUTES;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.EVENT_DEFINITIONS;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.EVENT_NAME;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.EVENT_TOPIC_STRUCTURE;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.NAME;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.TRANSPORT;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.TYPE;

public class DeviceTypeEventDAOImpl implements DeviceTypeEventDAO {

    private static Log log = LogFactory.getLog(DeviceTypeEventDAOImpl.class);

    @Override
    public List<DeviceTypeEvent> getDeviceTypeEventDefinitions(String deviceType, int tenantId) throws DeviceManagementDAOException {
        String selectSQL = "SELECT m.META_VALUE " +
                "FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d " +
                "ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? " +
                "AND d.PROVIDER_TENANT_ID = ? " +
                "AND d.NAME = ? " +
                "AND m.META_KEY = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, EVENT_DEFINITIONS);
            List<DeviceTypeEvent> eventDefinitions = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String eventDefinitionsJson = rs.getString("META_VALUE");
                    if (eventDefinitionsJson != null && !eventDefinitionsJson.isEmpty()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                        // Deserialize the JSON string into a List<DeviceTypeEvent>
                        eventDefinitions = objectMapper.readValue(
                                eventDefinitionsJson,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, DeviceTypeEvent.class)
                        );
                        return eventDefinitions;
                    }
                }
            }
            return eventDefinitions;
        } catch (SQLException e) {
            String msg = "SQL error while retrieving EVENT_DEFINITIONS for deviceType: " + deviceType +
                    ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } catch (IOException e) {
            String msg = "I/O error while processing EVENT_DEFINITIONS JSON for deviceType: " + deviceType +
                    ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }


    @Override
    public boolean createDeviceTypeMetaWithEvents(String deviceType, int tenantId,
                                                  List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementDAOException {
        try {
            // Initialize ObjectMapper for Jackson processing
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> eventDefinitions = addNewEventDefinitions(deviceTypeEvents);
            // Serialize event definitions
            String updatedEventDefinitionsJson = objectMapper.writeValueAsString(eventDefinitions);
            return createEventDefinitionsInDB(deviceType, tenantId, updatedEventDefinitionsJson);
        } catch (IOException e) {
            String msg = "Failed to process JSON while creating EVENT_DEFINITIONS for device type: " +
                    deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean updateDeviceTypeMetaWithEvents(String deviceType, int tenantId,
                                                  List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementDAOException {
        try {
            // Initialize ObjectMapper for Jackson processing
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> eventDefinitions = addNewEventDefinitions(deviceTypeEvents);
            // Serialize event definitions
            String updatedEventDefinitionsJson = objectMapper.writeValueAsString(eventDefinitions);
            // Update the database with the new event definitions
            return updateEventDefinitionsInDB(deviceType, tenantId, updatedEventDefinitionsJson);
        } catch (IOException e) {
            String msg = "Failed to process JSON while updating EVENT_DEFINITIONS for device type: " +
                    deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean deleteDeviceTypeEventDefinitions(String deviceType, int tenantId) throws DeviceManagementDAOException {
        try {
            String deleteSQL = "DELETE m " +
                    "FROM DM_DEVICE_TYPE_META m " +
                    "JOIN DM_DEVICE_TYPE d " +
                    "ON m.DEVICE_TYPE_ID = d.ID " +
                    "WHERE m.TENANT_ID = ? " +
                    "AND d.PROVIDER_TENANT_ID = ? " +
                    "AND d.NAME = ? " +
                    "AND m.META_KEY = ?";
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(deleteSQL)) {
                stmt.setInt(1, tenantId);
                stmt.setInt(2, tenantId);
                stmt.setString(3, deviceType);
                stmt.setString(4, EVENT_DEFINITIONS);
                // Execute the update
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Failed to delete event definitions for device type: " + deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }


    @Override
    public String getDeviceTypeEventDefinitionsAsJson(String deviceType, int tenantId) throws DeviceManagementDAOException {
        String selectSQL = "SELECT m.META_VALUE " +
                "FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d " +
                "ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? " +
                "AND d.PROVIDER_TENANT_ID = ? " +
                "AND d.NAME = ? " +
                "AND m.META_KEY = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, EVENT_DEFINITIONS);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("META_VALUE");
                }
            }
        } catch (SQLException e) {
            String msg = "Failed to retrieve EVENT_DEFINITIONS JSON for device type: " +
                    deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return null;  // Return null if no result is found
    }


    @Override
    public boolean isDeviceTypeMetaExist(String deviceType, int tenantId) throws DeviceManagementDAOException {
        String selectSQL = "SELECT m.META_VALUE " +
                "FROM DM_DEVICE_TYPE_META m " +
                "JOIN DM_DEVICE_TYPE d " +
                "ON m.DEVICE_TYPE_ID = d.ID " +
                "WHERE m.TENANT_ID = ? " +
                "AND d.PROVIDER_TENANT_ID = ? " +
                "AND d.NAME = ? " +
                "AND m.META_KEY = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL)) {
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            stmt.setString(3, deviceType);
            stmt.setString(4, EVENT_DEFINITIONS);
            try (ResultSet rs = stmt.executeQuery()) {
                // Return true if the ResultSet contains at least one row, otherwise false
                return rs.next();
            }
        } catch (SQLException e) {
            String msg = "Failed to validate existence of device type meta for deviceType: " +
                    deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /**
     * Converts a list of {@link DeviceTypeEvent} objects into a list of maps, where each map
     * represents the structure of an event definition in a key-value format.
     * <p>
     * This transformation is useful for serializing event definitions into a format
     * that can be used in configuration files, REST responses, or UI representations.
     * </p>
     *
     * @param deviceTypeEvents The list of {@link DeviceTypeEvent} objects to be transformed.
     * @return A list of maps where each map contains the properties of an event such as name,
     *         transport type, attributes, and topic structure.
     */
    private List<Map<String, Object>> addNewEventDefinitions(List<DeviceTypeEvent> deviceTypeEvents) {
        // Create a new list to avoid modifying the original existingEvents list directly
        List<Map<String, Object>> updatedEvents = new ArrayList<>();
        for (DeviceTypeEvent event : deviceTypeEvents) {
            // Create a new map for each event to avoid overriding the existing attributes
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put(EVENT_NAME, event.getEventName());
            eventMap.put(TRANSPORT, event.getTransportType().name());
            Map<String, Object> eventAttributes = new HashMap<>();
            // Add attributes: a list of attribute details inside eventAttributes
            List<Map<String, String>> attributes = event.getEventAttributeList().getList().stream()
                    .map(attr -> {
                        Map<String, String> attributeMap = new HashMap<>();
                        attributeMap.put(NAME, attr.getName());
                        attributeMap.put(TYPE, attr.getType().name()); // Assuming AttributeType is an enum
                        return attributeMap;
                    })
                    .collect(Collectors.toList());
            eventAttributes.put(ATTRIBUTES, attributes); // Nested inside eventAttributes
            // Add the eventAttributes map to the eventMap
            eventMap.put(EVENT_ATTRIBUTES, eventAttributes);
            eventMap.put(EVENT_TOPIC_STRUCTURE, event.getEventTopicStructure());
            // Add the event to the updated events list
            updatedEvents.add(eventMap);
        }
        // Return the updated list of events
        return updatedEvents;
    }

    /**
     * Updates the event definitions metadata in the database for a specific device type and tenant.
     * <p>
     * This method executes an SQL update on the {@code DM_DEVICE_TYPE_META} table by joining it with
     * the {@code DM_DEVICE_TYPE} table using the device type ID. It updates the {@code META_VALUE}
     * and {@code LAST_UPDATED_TIMESTAMP} fields where the tenant and device type match.
     * </p>
     *
     * @param deviceType                The name of the device type whose event definitions are to be updated.
     * @param tenantId                  The tenant ID associated with the device type.
     * @param updatedEventDefinitionsJson The new event definitions in JSON format to be stored in the database.
     * @return {@code true} if the update was successful (i.e., at least one row was affected), {@code false} otherwise.
     * @throws DeviceManagementDAOException if an error occurs while executing the SQL update or interacting with the database.
     */
    private boolean updateEventDefinitionsInDB(String deviceType, int tenantId, String updatedEventDefinitionsJson)
            throws DeviceManagementDAOException {
        try {
            String updateSQL = "UPDATE DM_DEVICE_TYPE_META m " +
                    "JOIN DM_DEVICE_TYPE d " +
                    "ON m.DEVICE_TYPE_ID = d.ID " +
                    "SET m.META_VALUE = ?, m.LAST_UPDATED_TIMESTAMP = ? " +
                    "WHERE m.TENANT_ID = ? " +
                    "AND d.PROVIDER_TENANT_ID = ? " +
                    "AND d.NAME = ? " +
                    "AND m.META_KEY = ?";
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                stmt.setString(1, updatedEventDefinitionsJson);
                stmt.setLong(2, System.currentTimeMillis()); // Set LAST_UPDATED_TIMESTAMP as Unix time in milliseconds
                stmt.setInt(3, tenantId);
                stmt.setInt(4, tenantId);
                stmt.setString(5, deviceType);
                stmt.setString(6, EVENT_DEFINITIONS);
                // Execute the update
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Failed to update EVENT_DEFINITIONS for deviceType: " +
                    deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /**
     * Inserts new event definitions metadata into the database for a given device type and tenant.
     * <p>
     * This method populates the {@code DM_DEVICE_TYPE_META} table with a new entry corresponding to the
     * provided device type and tenant. It uses a subquery to obtain the device type ID from the {@code DM_DEVICE_TYPE} table.
     * The metadata includes a key, value (event definitions in JSON format), and a timestamp.
     * </p>
     *
     * @param deviceType                 The name of the device type for which event definitions are being created.
     * @param tenantId                   The tenant ID associated with the device type.
     * @param updatedEventDefinitionsJson The event definitions in JSON format to be stored in the database.
     * @return {@code true} if the insert was successful (i.e., at least one row was added), {@code false} otherwise.
     * @throws DeviceManagementDAOException if a database error occurs while inserting the metadata.
     */
    private boolean createEventDefinitionsInDB(String deviceType, int tenantId, String updatedEventDefinitionsJson)
            throws DeviceManagementDAOException {
        try {
            String insertSQL = "INSERT INTO DM_DEVICE_TYPE_META (META_KEY, META_VALUE, LAST_UPDATED_TIMESTAMP, TENANT_ID, DEVICE_TYPE_ID) " +
                    "SELECT ?, ?, ?, ?, d.ID " +
                    "FROM DM_DEVICE_TYPE d " +
                    "WHERE d.NAME = ? AND d.PROVIDER_TENANT_ID = ?";
            Connection conn = this.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
                stmt.setString(1, EVENT_DEFINITIONS);
                stmt.setString(2, updatedEventDefinitionsJson);
                stmt.setLong(3, System.currentTimeMillis()); // Set LAST_UPDATED_TIMESTAMP as Unix time in milliseconds
                stmt.setInt(4, tenantId);
                stmt.setString(5, deviceType);
                stmt.setInt(6, tenantId);
                // Execute the insert
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Failed to create EVENT_DEFINITIONS for deviceType: " +
                    deviceType + ", tenantId: " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    /**
     * Retrieves a database connection from the {@link DeviceManagementDAOFactory}.
     *
     * @return A {@link Connection} object for interacting with the database.
     * @throws SQLException If an error occurs while obtaining the connection.
     */
    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}
