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
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEventUpdateResult;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceTypeEventDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeviceTypeEventManagementProviderServiceImpl implements DeviceTypeEventManagementProviderService {

    private static final Log log = LogFactory.getLog(DeviceTypeEventManagementProviderServiceImpl.class);
    private final DeviceTypeEventDAO deviceTypeEventDAO;

    public DeviceTypeEventManagementProviderServiceImpl() {
        this.deviceTypeEventDAO = DeviceManagementDAOFactory.getDeviceTypeEventDAO();
    }

    @Override
    public List<DeviceTypeEvent> getDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceTypeEventDAO.getDeviceTypeEventDefinitions(deviceType, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to retrieve event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public String getDeviceTypeEventDefinitionsAsJson(String deviceType) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceTypeEventDAO.getDeviceTypeEventDefinitionsAsJson(deviceType, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to retrieve event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean isDeviceTypeMetaExist(String deviceType) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.openConnection();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            return deviceTypeEventDAO.isDeviceTypeMetaExist(deviceType, tenantId);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while checking for device type meta key.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source to check for device type meta key.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean createDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isCreated = deviceTypeEventDAO.createDeviceTypeMetaWithEvents(deviceType, tenantId, deviceTypeEvents);
            DeviceManagementDAOFactory.commitTransaction();
            return isCreated;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean updateDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isUpdated = deviceTypeEventDAO.updateDeviceTypeMetaWithEvents(deviceType, tenantId, deviceTypeEvents);
            DeviceManagementDAOFactory.commitTransaction();
            return isUpdated;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean deleteDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            boolean isDeleted = deviceTypeEventDAO.deleteDeviceTypeEventDefinitions(deviceType, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            return isDeleted;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public DeviceTypeEventUpdateResult computeUpdatedDeviceTypeEvents(String deviceType, List<DeviceTypeEvent> incomingEvents)
            throws DeviceManagementException {
        List<DeviceTypeEvent> existingEvents = getDeviceTypeEventDefinitions(deviceType);
        Map<String, DeviceTypeEvent> existingEventMap = mapByName(existingEvents);
        Map<String, DeviceTypeEvent> incomingEventMap = mapByName(incomingEvents);

        List<DeviceTypeEvent> updatedEvents = new ArrayList<>();
        List<DeviceTypeEvent> unchangedEvents = new ArrayList<>();

        for (DeviceTypeEvent incoming : incomingEvents) {
            DeviceTypeEvent existing = existingEventMap.get(incoming.getEventName());
            if (existing == null || !incoming.equals(existing)) {
                updatedEvents.add(incoming);
            }
        }

        for (DeviceTypeEvent existing : existingEvents) {
            DeviceTypeEvent incoming = incomingEventMap.get(existing.getEventName());
            if (incoming == null || existing.equals(incoming)) {
                unchangedEvents.add(existing);
            }
        }

        List<DeviceTypeEvent> mergedEvents = new ArrayList<>();
        mergedEvents.addAll(updatedEvents);
        mergedEvents.addAll(unchangedEvents);

        return new DeviceTypeEventUpdateResult(updatedEvents, mergedEvents);
    }

    /**
     * Creates a map of event names to {@link DeviceTypeEvent} objects from a given list.
     *
     * @param events the list of {@link DeviceTypeEvent} to map
     * @return a map where the key is the event name and the value is the event object
     */
    private Map<String, DeviceTypeEvent> mapByName(List<DeviceTypeEvent> events) {
        return events.stream().collect(Collectors.toMap(DeviceTypeEvent::getEventName, e -> e, (a, b) -> b));
    }

}
