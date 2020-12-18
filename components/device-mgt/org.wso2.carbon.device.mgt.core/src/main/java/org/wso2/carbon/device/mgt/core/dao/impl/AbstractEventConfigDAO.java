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

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EventConfigDAO;
import org.wso2.carbon.device.mgt.core.dao.EventManagementDAOException;
import org.wso2.carbon.device.mgt.common.event.config.EventTaskEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractEventConfigDAO implements EventConfigDAO {
    private static final Log log = LogFactory.getLog(AbstractEventConfigDAO.class);

    @Override
    public boolean addEventGroupMappingRecords(List<Integer> eventIds, List<Integer> groupIds) throws EventManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_EVENT_GROUP_MAPPING(" +
                    "EVENT_ID ," +
                    "GROUP_ID) " +
                    "VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer groupId : groupIds) {
                    for (Integer eventId : eventIds) {
                        stmt.setInt(1, eventId);
                        stmt.setInt(2, groupId);
                        stmt.addBatch();
                    }
                }
                return stmt.executeBatch().length > 0;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating event group mapping records";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public List<EventConfig> getEventsOfGroups(List<Integer> groupIds, int tenantId) throws EventManagementDAOException {
        try {
            List<EventConfig> eventList = new ArrayList<>();
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "E.ID AS EVENT_ID, " +
                    "EVENT_SOURCE, " +
                    "EVENT_LOGIC, " +
                    "ACTIONS " +
                    "FROM DM_DEVICE_EVENT E, DM_DEVICE_EVENT_GROUP_MAPPING G " +
                    "WHERE G.EVENT_ID = E.ID " +
                    "AND G.GROUP_ID IN (%s) " +
                    "AND E.TENANT_ID = ? " +
                    "GROUP BY E.ID";
            String inClause = String.join(", ", Collections.nCopies(groupIds.size(), "?"));
            sql = String.format(sql, inClause);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer groupId : groupIds) {
                    stmt.setInt(index++, groupId);
                }
                stmt.setInt(index, tenantId);
                ResultSet rst = stmt.executeQuery();
                while (rst.next()) {
                    EventConfig event = new EventConfig();
                    event.setEventId(rst.getInt("EVENT_ID"));
                    event.setEventSource(rst.getString("EVENT_SOURCE"));
                    event.setEventLogic(rst.getString("EVENT_LOGIC"));
                    event.setActions(rst.getString("ACTIONS"));
                    eventList.add(event);
                }
                return eventList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating event group mapping records";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public List<EventConfig> getEventsOfGroups(int groupId, int tenantId) throws EventManagementDAOException {
        try {
            List<EventConfig> eventList = new ArrayList<>();
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "E.ID AS EVENT_ID, " +
                    "EVENT_SOURCE, " +
                    "EVENT_LOGIC, " +
                    "ACTIONS " +
                    "FROM DM_DEVICE_EVENT E, DM_DEVICE_EVENT_GROUP_MAPPING G " +
                    "WHERE G.EVENT_ID = E.ID " +
                    "AND G.GROUP_ID = ? " +
                    "AND E.TENANT_ID = ? " +
                    "GROUP BY E.ID";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, groupId);
                stmt.setInt(2, tenantId);
                ResultSet rst = stmt.executeQuery();
                while (rst.next()) {
                    EventConfig event = new EventConfig();
                    event.setEventId(rst.getInt("EVENT_ID"));
                    event.setEventSource(rst.getString("EVENT_SOURCE"));
                    event.setEventLogic(rst.getString("EVENT_LOGIC"));
                    event.setActions(rst.getString("ACTIONS"));
                    eventList.add(event);
                }
                return eventList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving event records of group " + groupId
                    + " and tenant " + tenantId;
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteEventGroupMappingRecordsByEventIds(List<Integer> eventsIdsToDelete) throws EventManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_EVENT_GROUP_MAPPING WHERE EVENT_ID IN (%s)";
            String inClause = String.join(", ", Collections.nCopies(eventsIdsToDelete.size(), "?"));
            sql = String.format(sql, inClause);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer eventId : eventsIdsToDelete) {
                    stmt.setInt(index++, eventId);
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting event group mapping records";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteEventGroupMappingRecordsByGroupIds(List<Integer> groupIdsToDelete) throws EventManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_EVENT_GROUP_MAPPING WHERE GROUP_ID IN (%s)";
            String inClause = String.join(", ", Collections.nCopies(groupIdsToDelete.size(), "?"));
            sql = String.format(sql, inClause);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer groupId : groupIdsToDelete) {
                    stmt.setInt(index++, groupId);
                    stmt.addBatch();
                }
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting event group mapping records";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateEventRecords(List<EventConfig> eventsToUpdate) throws EventManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "UPDATE DM_DEVICE_EVENT SET " +
                    "ACTIONS = ? " +
                    "WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (EventConfig updatingEvent : eventsToUpdate) {
                    stmt.setString(1, updatingEvent.getActions());
                    stmt.setInt(2, updatingEvent.getEventId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating event records";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public void deleteEventRecords(List<Integer> eventsIdsToDelete) throws EventManagementDAOException {
        try {
            Connection conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_EVENT WHERE ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer eventId : eventsIdsToDelete) {
                    stmt.setInt(1, eventId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting event records of tenant";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public List<EventConfig> getEventsById(List<Integer> eventIdList) throws EventManagementDAOException {
        try {
            List<EventConfig> eventList = new ArrayList<>();
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "ID AS EVENT_ID, " +
                    "EVENT_SOURCE, " +
                    "EVENT_LOGIC, " +
                    "ACTIONS " +
                    "FROM DM_DEVICE_EVENT " +
                    "WHERE ID IN (%s) ";
            String inClause = String.join(", ", Collections.nCopies(eventIdList.size(), "?"));
            sql = String.format(sql, inClause);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer eventId : eventIdList) {
                    if (eventId != -1) {
                        stmt.setInt(index++, eventId);
                    }
                }
                ResultSet rst = stmt.executeQuery();
                while (rst.next()) {
                    EventConfig event = new EventConfig();
                    event.setEventId(rst.getInt("EVENT_ID"));
                    event.setEventSource(rst.getString("EVENT_SOURCE"));
                    event.setEventLogic(rst.getString("EVENT_LOGIC"));
                    event.setActions(rst.getString("ACTIONS"));
                    eventList.add(event);
                }
                return eventList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating event group mapping records";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getGroupsOfEvents(List<Integer> eventIdList) throws EventManagementDAOException {
        try {
            List<Integer> groupIdList = new ArrayList<>();
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "GROUP_ID " +
                    "FROM DM_DEVICE_EVENT_GROUP_MAPPING " +
                    "WHERE EVENT_ID IN (%s) " +
                    "GROUP BY GROUP_ID";
            String inClause = String.join(", ", Collections.nCopies(eventIdList.size(), "?"));
            sql = String.format(sql, inClause);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int index = 1;
                for (Integer eventId : eventIdList) {
                    if (eventId != -1) {
                        stmt.setInt(index++, eventId);
                    }
                }
                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    groupIdList.add(resultSet.getInt("GROUP_ID"));
                }
                return groupIdList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating event group mapping records";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getEventSourcesOfGroups(int groupId, int tenantId) throws EventManagementDAOException {
        try {
            List<String> eventSourceList = new ArrayList<>();
            Connection conn = this.getConnection();
            String sql = "SELECT " +
                    "EVENT_SOURCE " +
                    "FROM DM_DEVICE_EVENT E, DM_DEVICE_EVENT_GROUP_MAPPING G " +
                    "WHERE G.EVENT_ID = E.ID " +
                    "AND G.GROUP_ID = ? " +
                    "AND E.TENANT_ID = ? " +
                    "GROUP BY EVENT_SOURCE";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, groupId);
                stmt.setInt(2, tenantId);
                ResultSet rst = stmt.executeQuery();
                while (rst.next()) {
                    eventSourceList.add(rst.getString("EVENT_SOURCE"));
                }
                return eventSourceList;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving event records of group " + groupId
                    + " and tenant " + tenantId;
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }


    @Override
    public boolean createEventTaskEntry(EventTaskEntry eventTaskEntry, List<Integer> groupIds)
            throws EventManagementDAOException {
        try {
            Connection connection = this.getConnection();
            String sql = "INSERT INTO DM_EVENT_OPERATION_ASSIGNMENT_TASK(" +
                    "DEVICE_ID, " +
                    "GROUP_ID, " +
                    "OPERATION_CODE, " +
                    "EVENT_SOURCE, " +
                    "EVENT_META_ID, " +
                    "STATUS, " +
                    "TENANT_ID, " +
                    "CREATED_TIMESTAMP) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stm = connection.prepareStatement(sql)){
                for (Integer groupId : groupIds) {
                    stm.setInt(1, eventTaskEntry.getDeviceId());
                    stm.setInt(2, groupId);
                    stm.setString(3, eventTaskEntry.getOperationCode());
                    stm.setString(4, eventTaskEntry.getEventSource());
                    stm.setInt(5, eventTaskEntry.getEventMetaId());
                    stm.setString(6, eventTaskEntry.getExecutionStatus().toString());
                    stm.setInt(7, eventTaskEntry.getTenantId());
                    stm.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
                    stm.addBatch();
                }
                return stm.executeBatch().length > 0;
            }
        } catch (SQLException e) {
            String msg = "Failed while creating event task entry for event operation " + eventTaskEntry.getOperationCode()
                    + " of the tenant " + eventTaskEntry.getTenantId();
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public Map<Integer, List<EventTaskEntry>> getAvailableEventTaskEntries(String status) throws EventManagementDAOException {
        Map<Integer, List<EventTaskEntry>> eventTaskEntries = new HashMap<>();
        try {
            Connection connection = this.getConnection();
            String sql = "SELECT " +
                    "ID, "+
                    "DEVICE_ID, " +
                    "GROUP_ID, " +
                    "OPERATION_CODE, " +
                    "EVENT_SOURCE, " +
                    "EVENT_META_ID, " +
                    "TENANT_ID " +
                    "FROM DM_EVENT_OPERATION_ASSIGNMENT_TASK " +
                    "WHERE STATUS = ?";
            try (PreparedStatement stm = connection.prepareStatement(sql)){
                stm.setString(1, status);
                ResultSet resultSet = stm.executeQuery();
                while (resultSet.next()) {
                    int tenantId = resultSet.getInt("TENANT_ID");
                    List<EventTaskEntry> eventsOfTenant = eventTaskEntries.get(tenantId);
                    if (eventsOfTenant == null) {
                        eventsOfTenant = new ArrayList<>();
                    }
                    EventTaskEntry eventTaskEntry = new EventTaskEntry();
                    eventTaskEntry.setId(resultSet.getInt("ID"));
                    eventTaskEntry.setOperationCode(resultSet.getString("OPERATION_CODE"));
                    eventTaskEntry.setGroupId(resultSet.getInt("GROUP_ID"));
                    eventTaskEntry.setEventMetaId(resultSet.getInt("EVENT_META_ID"));
                    eventTaskEntry.setDeviceId(resultSet.getInt("DEVICE_ID"));
                    eventTaskEntry.setTenantId(tenantId);
                    eventTaskEntry.setEventSource(resultSet.getString("EVENT_SOURCE"));
                    eventsOfTenant.add(eventTaskEntry);
                    eventTaskEntries.put(tenantId, eventsOfTenant);
                }
                return eventTaskEntries;
            }
        } catch (SQLException e) {
            String msg = "Failed while retrieving event task entries";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean setEventTaskComplete(String code, String eventSource, List<Integer> groupIds, int tenantId)
            throws EventManagementDAOException {
        try {
            Connection connection = this.getConnection();
            String sql = "UPDATE DM_EVENT_OPERATION_ASSIGNMENT_TASK " +
                    "SET STATUS = ?, "+
                    "COMPLETED_TIMESTAMP = ? " +
                    "WHERE STATUS = ? AND OPERATION_CODE = ? AND EVENT_SOURCE = ? AND GROUP_ID = ? AND TENANT_ID = ?";
            try (PreparedStatement stm = connection.prepareStatement(sql)){
                for (Integer groupId : groupIds) {
                    stm.setString(1, EventTaskEntry.ExecutionStatus.COMPLETED.toString());
                    stm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    stm.setString(3, EventTaskEntry.ExecutionStatus.CREATED.toString());
                    stm.setString(4, code);
                    stm.setString(5, eventSource);
                    stm.setInt(6, groupId);
                    stm.setInt(7, tenantId);
                    stm.addBatch();
                }
                return stm.executeBatch().length > 0;
            }
        } catch (SQLException e) {
            String msg = "Failed while retrieving event task entries";
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}
