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

package org.wso2.carbon.device.mgt.core.geo.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.common.event.config.EventMetaData;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.event.config.EventOperationExecutor;
import org.wso2.carbon.device.mgt.core.event.config.GroupAssignmentEventOperationExecutor;

import java.util.List;

/**
 * Responsible for Event operation task creation management.
 * Wrap event operation executor creation
 */
public class GeoFenceEventOperationManager {
    private static final Log log = LogFactory.getLog(GeoFenceEventOperationManager.class);

    private final int tenantId;
    private final String eventOperationCode;
    private final EventCreateCallback callback;
    private final boolean isEventEnabled;

    public GeoFenceEventOperationManager(String eventOperationCode, int tenantId, EventCreateCallback callback) {
        this.eventOperationCode = eventOperationCode;
        this.tenantId = tenantId;
        this.callback = callback;
        isEventEnabled = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                .getEventOperationTaskConfiguration().isEnabled();
    }

    /**
     * Get executor for create EVENT_CONFIG / EVENT_REVOKE operations at the time of a device/s
     * assigned into a group or removed from a group
     *
     * @param groupId           Id of the assigned / removed group
     * @param deviceIdentifiers Device identifiers assigned to / removed from the group
     * @return {@link GroupAssignmentEventOperationExecutor} Created executor to create operations
     */
    public GroupAssignmentEventOperationExecutor getGroupAssignmentEventExecutor(int groupId,
                                                                                 List<DeviceIdentifier> deviceIdentifiers) {
        if (this.isEventEnabled) {
            GroupAssignmentEventOperationExecutor executor = new GroupAssignmentEventOperationExecutor(groupId,
                    deviceIdentifiers, tenantId, eventOperationCode);
            executor.setCallback(callback);
            return executor;
        }
        if (log.isDebugEnabled()) {
            log.debug("Ignoring geo fence event creation since not enabled from configurations");
        }
        return null;
    }

    /**
     * Get executor for create EVENT_CONFIG / EVENT_REVOKE operations at the time of a event is created
     *
     * @param groupIds      list of group ids to apply the created event
     * @param eventMetaData contains all the data of the related event
     * @return {@link EventOperationExecutor} The created event executor object
     */
    public EventOperationExecutor getEventOperationExecutor(List<Integer> groupIds, EventMetaData eventMetaData) {
        if (this.isEventEnabled) {
            EventOperationExecutor executor = new EventOperationExecutor(eventMetaData, groupIds,
                    this.tenantId, DeviceManagementConstants.EventServices.GEOFENCE, this.eventOperationCode);
            return executor;
        }
        if (log.isDebugEnabled()) {
            log.debug("Ignoring geo fence event creation since not enabled from configurations");
        }
        return null;
    }
}
