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

import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationException;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationProviderService;
import org.wso2.carbon.device.mgt.common.event.config.EventMetaData;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EventConfigDAO;
import org.wso2.carbon.device.mgt.core.dao.EventManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.geo.task.GeoFenceEventOperationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventConfigurationProviderServiceImpl implements EventConfigurationProviderService {
    private static final Log log = LogFactory.getLog(EventConfigurationProviderServiceImpl.class);
    private final EventConfigDAO eventConfigDAO;

    public EventConfigurationProviderServiceImpl() {
        eventConfigDAO = DeviceManagementDAOFactory.getEventConfigDAO();
    }

    @Override
    public List<Integer> createEventsOfDeviceGroup(List<EventConfig> eventConfigList, List<Integer> groupIds)
            throws EventConfigurationException {
        int tenantId;
        try {
            tenantId = DeviceManagementDAOUtil.getTenantId();
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving tenant Id";
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        }

        try {
            DeviceManagementDAOFactory.beginTransaction();
            if (log.isDebugEnabled()) {
                log.debug("Creating event records of tenant " + tenantId);
            }
            List<Integer> generatedEventIds = eventConfigDAO.storeEventRecords(eventConfigList, tenantId);
            if (log.isDebugEnabled()) {
                log.debug("Created events with event ids : " + generatedEventIds.toString());
                log.debug("Creating event group mapping for created events with group ids : " + groupIds.toString());
            }
            eventConfigDAO.addEventGroupMappingRecords(generatedEventIds, groupIds);
            DeviceManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("Event configuration added successfully for the tenant " + tenantId);
            }
            return generatedEventIds;
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to store device event configurations";
            throw new EventConfigurationException(msg, e);
        } catch (EventManagementDAOException e) {
            String msg = "Error occurred while saving event records";
            log.error(msg, e);
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new EventConfigurationException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Integer> updateEventsOfDeviceGroup(List<EventConfig> newEventList, List<Integer> removedEventIdList,
                                                   List<Integer> groupIds) throws EventConfigurationException {
        //todo when concerning about other event types, all of this steps might not necessary.
        // so divide them into separate service methods
        if (log.isDebugEnabled()) {
            log.debug("Updating event configurations of tenant");
        }
        List<EventConfig> eventsToAdd;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            eventsToAdd = new ArrayList<>();
            List<EventConfig> eventsToUpdate = new ArrayList<>();
            List<Integer> updateEventIdList = new ArrayList<>();
            for (EventConfig newEvent : newEventList) {
                if (newEvent.getEventId() == -1) {
                    eventsToAdd.add(newEvent);
                    continue;
                }
                eventsToUpdate.add(newEvent);
                updateEventIdList.add(newEvent.getEventId());
            }
            List<Integer> savedGroups = eventConfigDAO.getGroupsOfEvents(updateEventIdList);
            List<Integer> groupIdsToAdd = new ArrayList<>();
            List<Integer> groupIdsToDelete = new ArrayList<>();
            for (Integer savedGroup : savedGroups) {
                if (!groupIds.contains(savedGroup)) {
                    groupIdsToDelete.add(savedGroup);
                }
            }

            for (Integer newGroupId : groupIds) {
                if (!savedGroups.contains(newGroupId)) {
                    groupIdsToAdd.add(newGroupId);
                }
            }

            if (!eventsToUpdate.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Updating event records ");
                }
                eventConfigDAO.updateEventRecords(eventsToUpdate);
            }

            if (!groupIdsToDelete.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting event group mapping records of groups");
                }
                eventConfigDAO.deleteEventGroupMappingRecordsByGroupIds(groupIdsToDelete);
            }

            if (!groupIdsToAdd.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating event group mapping records for updated events");
                }
                eventConfigDAO.addEventGroupMappingRecords(updateEventIdList, groupIdsToAdd);
            }

            if (!removedEventIdList.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("Deleting event group mapping records of removing events");
                }
                eventConfigDAO.deleteEventGroupMappingRecordsByEventIds(removedEventIdList);

                if (log.isDebugEnabled()) {
                    log.debug("Deleting removed event records");
                }
                eventConfigDAO.deleteEventRecords(removedEventIdList);
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to store device event configurations";
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        } catch (EventManagementDAOException e) {
            String msg = "Error occurred while saving event records";
            log.error(msg, e);
            DeviceManagementDAOFactory.rollbackTransaction();
            throw new EventConfigurationException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        if (log.isDebugEnabled()) {
            log.debug("Adding new events while updating event");
        }
        return createEventsOfDeviceGroup(eventsToAdd, groupIds);
    }

    @Override
    public List<EventConfig> getEvents(List<Integer> createdEventIds) throws EventConfigurationException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return eventConfigDAO.getEventsById(createdEventIds);
        } catch (EventManagementDAOException e) {
            String msg = "Error occurred while retrieving event by IDs : " + Arrays.toString(createdEventIds.toArray());
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        } catch (SQLException e) {
            String msg = "Failed to open connection while retrieving event by IDs";
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<String> getEventsSourcesOfGroup(int groupId, int tenantId) throws EventConfigurationException {
        try {
            DeviceManagementDAOFactory.openConnection();
            return eventConfigDAO.getEventSourcesOfGroups(groupId, tenantId);
        } catch (EventManagementDAOException e) {
            String msg = "Error occurred while retrieving events of group " + groupId + " and tenant " + tenantId;
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        } catch (SQLException e) {
            String msg = "Failed to open connection while retrieving event by IDs";
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteEvents(List<EventConfig> events) throws EventConfigurationException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Set<Integer> eventIdSet = new HashSet<>();
            for (EventConfig eventConfig : events) {
                eventIdSet.add(eventConfig.getEventId());
            }
            if (!eventIdSet.isEmpty()) {
                eventConfigDAO.deleteEventGroupMappingRecordsByEventIds(Lists.newArrayList(eventIdSet));
                eventConfigDAO.deleteEventRecords(Lists.newArrayList(eventIdSet));
            }
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Failed to start/open transaction to delete device event configurations";
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        } catch (EventManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting event records";
            log.error(msg, e);
            throw new EventConfigurationException(msg, e);
        }
    }

    @Override
    public void createEventOperationTask(String eventType, String eventCode, EventMetaData eventMeta, int tenantId,
                                         List<Integer> groupIds) {
        GeoFenceEventOperationManager geoFenceEventOperationManager = new GeoFenceEventOperationManager(eventType, tenantId, null);
        EventOperationExecutor executor = geoFenceEventOperationManager.getEventOperationExecutor(groupIds, eventMeta);
        DeviceManagementDataHolder.getInstance().getEventConfigExecutors().submit(executor);
    }
}
