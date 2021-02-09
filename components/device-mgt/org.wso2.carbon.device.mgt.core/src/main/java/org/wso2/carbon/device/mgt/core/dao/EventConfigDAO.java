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

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.event.config.EventConfig;

import java.util.List;
import java.util.Map;

public interface EventConfigDAO {

    /**
     * Create event configuration entries of the db for a selected tenant
     * @param eventConfigList event list to be created
     * @param tenantId corresponding tenant id of the events
     * @return generated event ids while storing geofence data
     * @throws EventManagementDAOException error occurred while creating event records
     */
    List<Integer> storeEventRecords(List<EventConfig> eventConfigList, int tenantId) throws EventManagementDAOException;

    /**
     * Cerate even-group mapping records
     * @param eventIds event ids to be mapped with groups
     * @param groupIds group ids of the event attached with
     * @return true for the successful creation
     * @throws EventManagementDAOException error occurred while creating event-group mapping records
     */
    boolean addEventGroupMappingRecords(List<Integer> eventIds, List<Integer> groupIds) throws EventManagementDAOException;

    /**
     * Get events owned by a specific device group
     * @param groupIds group ids of the events
     * @param tenantId tenant of the events owning
     * @return list of event configuration filtered by tenant id and group ids
     * @throws EventManagementDAOException error occurred while reading event records
     */
    List<EventConfig> getEventsOfGroups(List<Integer> groupIds, int tenantId) throws EventManagementDAOException;

    /**
     * Get events of groups using group Id
     * @param groupId  id of the group
     * @param tenantId id of the tenant
     * @return EventConfig list of specific group
     * @throws EventManagementDAOException errors occur while retrieving events of groups
     */
    List<EventConfig> getEventsOfGroups(int groupId, int tenantId) throws EventManagementDAOException;

    /**
     * Delete event group mapping records using the group ids
     * @param groupIdsToDelete id of groups
     * @throws EventManagementDAOException error occurred while deleting event-group mapping records
     */
    void deleteEventGroupMappingRecordsByGroupIds(List<Integer> groupIdsToDelete) throws EventManagementDAOException;

    /**
     * Update event records of the tenant
     * @param eventsToUpdate updating event records
     * @throws EventManagementDAOException error occurred while updating events
     */
    void updateEventRecords(List<EventConfig> eventsToUpdate) throws EventManagementDAOException;

    /**
     * Delete events using event ids
     * @param eventsIdsToDelete ids of the events which should be deleted
     * @throws EventManagementDAOException error occurred while deleting event records
     */
    void deleteEventRecords(List<Integer> eventsIdsToDelete) throws EventManagementDAOException;

    /**
     * Get event records by event ids
     * @param eventIds filtering event ids
     * @return filtered event configuration list
     * @throws EventManagementDAOException error occurred while reading events
     */
    List<EventConfig> getEventsById(List<Integer> eventIds) throws EventManagementDAOException;

    /**
     * Get group ids belong to events using event ids
     * @param eventIds Ids of the events mapped with group
     * @return Group Id list
     * @throws EventManagementDAOException thrown errors while retrieving group Ids of events
     */
    List<Integer> getGroupsOfEvents(List<Integer> eventIds) throws EventManagementDAOException;

    /**
     * Delete event group mapping records using event Ids
     * @param eventIds Ids of the events
     * @throws EventManagementDAOException thrown errors while deleting event group mappings
     */
    void deleteEventGroupMappingRecordsByEventIds(List<Integer> eventIds) throws EventManagementDAOException;

    /**
     * Retrieve event sources mapped with specific groups and tenant
     * @param groupId Id of the group
     * @param tenantId Id of the tenant
     * @return Event source list belong to
     * @throws EventManagementDAOException thrown errors while retrieving event sources
     */
    List<String> getEventSourcesOfGroups(int groupId, int tenantId) throws EventManagementDAOException;
}
