/*
 * Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.event.config.EventConfig;
import io.entgra.device.mgt.core.device.mgt.core.dao.EventManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.EventManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.AbstractEventConfigDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

public class SQLServerEventConfigDAOImpl extends AbstractEventConfigDAO {
    private static final Log log = LogFactory.getLog(SQLServerEventConfigDAOImpl.class);

    @Override
    public List<Integer> storeEventRecords(List<EventConfig> eventConfigList, int tenantId) throws EventManagementDAOException {
        try {
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
            Connection conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_EVENT(" +
                    "EVENT_SOURCE, " +
                    "EVENT_LOGIC, " +
                    "ACTIONS, "+
                    "CREATED_TIMESTAMP, " +
                    "TENANT_ID) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                List<Integer> generatedIds = new ArrayList<>();
                for (EventConfig eventConfig : eventConfigList) {
                    stmt.setString(1, eventConfig.getEventSource());
                    stmt.setString(2, eventConfig.getEventLogic());
                    stmt.setString(3, eventConfig.getActions());
                    stmt.setTimestamp(4, timestamp);
                    stmt.setInt(5, tenantId);
                    stmt.executeUpdate();
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        generatedIds.add(generatedKeys.getInt(1));
                    }
                }
                return generatedIds;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while creating event configurations for the tenant id " + tenantId;
            log.error(msg, e);
            throw new EventManagementDAOException(msg, e);
        }
    }

    private Connection getConnection() throws SQLException {
        return EventManagementDAOFactory.getConnection();
    }
}
