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

package org.wso2.carbon.device.mgt.common.event.config;

import java.util.List;

public interface EventConfigurationProviderService {
    /**
     * Create event configuration records
     *
     * @param eventConfigList event list to be added
     * @param groupIds        group ids of the events are mapped
     * @return generated event ids
     * @throws EventConfigurationException errors thrown while creating event configuration
     */
    List<Integer> createEventsOfDeviceGroup(List<EventConfig> eventConfigList, List<Integer> groupIds)
            throws EventConfigurationException;

    /**
     * Update event configuration records
     *
     * @param eventConfig        updated event configuration list. event ids should be present for
     *                           the updating events and event ids should be -1 for the newly creating events
     * @param removedEventIdList event ids of removed while updating the event configuration
     * @param groupIds           group ids to be mapped with updated events
     * @return Newly created event Ids
     * @throws EventConfigurationException
     */
    List<Integer> updateEventsOfDeviceGroup(List<EventConfig> eventConfig, List<Integer> removedEventIdList,
                                            List<Integer> groupIds) throws EventConfigurationException;

    /**
     * Retrieve event list using event IDs
     *
     * @param createdEventIds event ids
     * @return {@link EventConfig} List of events of defined IDs
     * @throws EventConfigurationException
     */
    List<EventConfig> getEvents(List<Integer> createdEventIds) throws EventConfigurationException;

    /**
     * Get event sources attached to a specific group
     *
     * @param groupId  mapped group id of events
     * @param tenantId event owning tenant
     * @return Event sources of the group
     * @throws EventConfigurationException error thrown while retrieving event sources
     */
    List<String> getEventsSourcesOfGroup(int groupId, int tenantId) throws EventConfigurationException;

    /**
     * Delete events by event Ids
     *
     * @param eventList event list to be deleted
     * @throws EventConfigurationException error thrown while deleting event records
     */
    void deleteEvents(List<EventConfig> eventList) throws EventConfigurationException;

    /**
     * Create event operation and add them into the corresponding devices
     * @param eventType EVENT_REVOKE / EVENT_CONFIG
     * @param eventCode unique code of the event
     * @param eventMeta event metadata
     * @param tenantId tenant id
     * @param groupIds group ids of the corresponding event
     */
    void createEventOperationTask(String eventType, String eventCode, EventMetaData eventMeta, int tenantId,
                                  List<Integer> groupIds) throws EventConfigurationException;
}
