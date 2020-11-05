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
import org.wso2.carbon.device.mgt.common.event.config.EventAction;
import org.wso2.carbon.device.mgt.common.event.config.EventConfig;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationException;
import org.wso2.carbon.device.mgt.common.event.config.EventConfigurationProviderService;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EventConfigDAO;
import org.wso2.carbon.device.mgt.core.dao.EventManagementDAOException;

import java.util.List;

public class EventConfigurationProviderServiceImpl implements EventConfigurationProviderService {
    private static final Log log = LogFactory.getLog(EventConfigurationProviderServiceImpl.class);
    private final EventConfigDAO eventConfigDAO;

    public EventConfigurationProviderServiceImpl() {
        eventConfigDAO = DeviceManagementDAOFactory.getEventConfigDAO();
    }

    @Override
    public boolean createEventOfDeviceGroup(List<EventConfig> eventConfigList, List<Integer> groupIds, int tenantId)
            throws EventConfigurationException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int[] generatedEventIds = eventConfigDAO.storeEventRecords(eventConfigList, tenantId);
            boolean isRecordsCreated = eventConfigDAO.addEventGroupMappingRecords(generatedEventIds, groupIds);
            DeviceManagementDAOFactory.commitTransaction();
            return isRecordsCreated;
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
}
