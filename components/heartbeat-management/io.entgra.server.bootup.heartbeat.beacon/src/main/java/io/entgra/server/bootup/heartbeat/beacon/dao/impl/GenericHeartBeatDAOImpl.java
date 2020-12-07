/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.server.bootup.heartbeat.beacon.dao.impl;

import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatBeaconDAOFactory;
import io.entgra.server.bootup.heartbeat.beacon.dao.HeartBeatDAO;
import io.entgra.server.bootup.heartbeat.beacon.dao.exception.HeartBeatDAOException;
import io.entgra.server.bootup.heartbeat.beacon.dao.util.HeartBeatBeaconDAOUtil;
import io.entgra.server.bootup.heartbeat.beacon.dto.ElectedCandidate;
import io.entgra.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.server.bootup.heartbeat.beacon.dto.ServerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This class represents implementation of HeartBeatDAO
 */
public class GenericHeartBeatDAOImpl implements HeartBeatDAO {

    private static final Log log = LogFactory.getLog(GenericHeartBeatDAOImpl.class);

    @Override
    public String recordServerCtx(ServerContext ctx) throws HeartBeatDAOException {
        String uuid = null;
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String serverUUID = UUID.randomUUID().toString();

            String sql;
            sql = "INSERT INTO SERVER_HEART_BEAT_EVENTS(HOST_NAME, UUID, SERVER_PORT) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, ctx.getHostName());
                stmt.setString(2, serverUUID);
                stmt.setInt(3, ctx.getCarbonServerPort());

                if (stmt.executeUpdate() > 0) {
                    uuid = serverUUID;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while persisting server context for : '" +
                         "port '" + ctx.getCarbonServerPort() + "' " +
                         "hostname : '" + ctx.getHostName() + "'";
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
        return uuid;
    }

    @Override
    public boolean recordElectedCandidate(String serverUUID) throws HeartBeatDAOException {
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql;
            sql = "INSERT INTO ELECTED_LEADER_META_INFO(UUID) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, serverUUID);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while persisting UUID of chosen " +
                         "elected dynamic task execution candidate : " + serverUUID;
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
    }

    @Override
    public void purgeCandidates() throws HeartBeatDAOException {
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            conn.setAutoCommit(false);
            String sql = "TRUNCATE TABLE ELECTED_LEADER_META_INFO";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                conn.commit();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while truncating ELECTED_LEADER_META_INFO table.";
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
    }

    @Override
    public ElectedCandidate retrieveCandidate() throws HeartBeatDAOException {
        ElectedCandidate candidate = null;
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql = "SELECT * from ELECTED_LEADER_META_INFO";

            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(sql)) {
                    while (resultSet.next()) {
                        candidate = HeartBeatBeaconDAOUtil.populateCandidate(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving meta information of elected candidate";
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
        return candidate;
    }

    @Override
    public boolean acknowledgeTask(String uuid, List<String> taskList)
            throws HeartBeatDAOException {
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql;
            sql = "UPDATE ELECTED_LEADER_META_INFO SET ACKNOWLEDGED_TASK_LIST = ? WHERE UUID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"UUID"})) {
                stmt.setString(1, String.join(",", taskList));
                stmt.setString(2, uuid);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating task list of elected server : '" +
                         uuid + "'  and task list " + taskList;
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
    }

    @Override
    public boolean recordHeatBeat(HeartBeatEvent event) throws HeartBeatDAOException {
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql;
            sql = "UPDATE SERVER_HEART_BEAT_EVENTS SET LAST_UPDATED_TIMESTAMP = ? WHERE UUID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
                stmt.setTimestamp(1, event.getTime());
                stmt.setString(2, event.getServerUUID());
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating heartbeat event against server with UUID : '" +
                         event.getServerUUID() + "'  and timestamp " + event.getTime();
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
    }

    @Override
    public boolean checkUUIDValidity(String uuid) throws HeartBeatDAOException {
        boolean result = false;
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql = "SELECT ID FROM SERVER_HEART_BEAT_EVENTS WHERE UUID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        result = true;
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred checking existense of UUID" + uuid +
                         " amongst heartbeat meta info.";
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
        return result;
    }

    @Override
    public String retrieveExistingServerCtx(ServerContext ctx) throws HeartBeatDAOException {
        String uuid = null;
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql = "SELECT UUID FROM SERVER_HEART_BEAT_EVENTS WHERE HOST_NAME = ? AND SERVER_PORT = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"UUID"})) {
                stmt.setString(1, ctx.getHostName());
                stmt.setInt(2, ctx.getCarbonServerPort());
                try (ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        uuid = resultSet.getString("UUID");
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving meta information for heart beat event from " +
                         "port '" + ctx.getCarbonServerPort() + "' " +
                         "hostname : '" + ctx.getHostName() + "'";
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
        return uuid;
    }

    @Override
    public Map<String, ServerContext> getActiveServerDetails(int elapsedTimeInSeconds)
            throws HeartBeatDAOException {
        Map<String, ServerContext> ctxList = new HashMap<>();
        try {
            Connection conn = HeartBeatBeaconDAOFactory.getConnection();
            String sql = "SELECT (@row_number:=@row_number + 1) AS IDX, UUID, HOST_NAME, SERVER_PORT from " +
                         "SERVER_HEART_BEAT_EVENTS, (SELECT @row_number:=-1) AS TEMP " +
                         "WHERE LAST_UPDATED_TIMESTAMP > ? " +
                         "ORDER BY UUID";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(elapsedTimeInSeconds)));
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        ctxList.put(resultSet.getString("UUID"), HeartBeatBeaconDAOUtil.populateContext(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving acting server count with " +
                         "heartbeat updates within " + elapsedTimeInSeconds + " seconds.";
            log.error(msg, e);
            throw new HeartBeatDAOException(msg, e);
        }
        return ctxList;
    }

}
