/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.task.mgt.core.dao.impl;

import io.entgra.device.mgt.core.task.mgt.common.bean.DynamicTask;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementDAOException;
import io.entgra.device.mgt.core.task.mgt.core.dao.DynamicTaskDAO;
import io.entgra.device.mgt.core.task.mgt.core.dao.common.TaskManagementDAOFactory;
import io.entgra.device.mgt.core.task.mgt.core.dao.util.TaskManagementDAOUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public class DynamicTaskDAOImpl implements DynamicTaskDAO {
    private static final Log log = LogFactory.getLog(DynamicTaskDAOImpl.class);

    @Override
    public int addTask(DynamicTask dynamicTask, int tenantId) throws TaskManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs;
        int taskId = -1;
        try {
            Connection conn = TaskManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DYNAMIC_TASK(INTERVALS, CRON, NAME, IS_ENABLED, TASK_CLASS_NAME, TENANT_ID) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(sql, new String[]{"DYNAMIC_TASK_ID"});
            stmt.setLong(1, dynamicTask.getIntervalMillis());
            stmt.setString(2, dynamicTask.getCronExpression());
            stmt.setString(3, dynamicTask.getName());
            stmt.setBoolean(4, dynamicTask.isEnabled());
            stmt.setString(5, dynamicTask.getTaskClassName());
            stmt.setInt(6, tenantId);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                taskId = rs.getInt(1);
            }
            dynamicTask.setDynamicTaskId(taskId);
            return taskId;
        } catch (SQLException e) {
            String msg = "Error occurred while inserting task '" + dynamicTask.getName() + "'";
            log.error(msg, e);
            throw new TaskManagementDAOException(msg, e);
        } finally {
            TaskManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean updateDynamicTask(DynamicTask dynamicTask, int tenantId) throws TaskManagementDAOException {
        PreparedStatement stmt = null;
        int rows;
        try {
            Connection conn = TaskManagementDAOFactory.getConnection();
            String sql = "UPDATE DYNAMIC_TASK SET INTERVALS = ?, CRON = ?,IS_ENABLED = ? WHERE DYNAMIC_TASK_ID = ? " +
                    "AND TENANT_ID = ?";

            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, dynamicTask.getIntervalMillis());
            stmt.setString(2, dynamicTask.getCronExpression());
            stmt.setBoolean(3, dynamicTask.isEnabled());
            stmt.setInt(4, dynamicTask.getDynamicTaskId());
            stmt.setInt(5, tenantId);
            rows = stmt.executeUpdate();
            return (rows > 0);
        } catch (SQLException e) {
            String msg = "Error occurred while updating dynamic task '" + dynamicTask.getDynamicTaskId() + "'";
            log.error(msg, e);
            throw new TaskManagementDAOException(msg, e);
        } finally {
            TaskManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDynamicTask(int dynamicTaskId, int tenantId) throws TaskManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete dynamic task with the id: " + dynamicTaskId);
        }
        String sql = "DELETE FROM DYNAMIC_TASK WHERE DYNAMIC_TASK_ID = ? AND TENANT_ID = ?";
        try {
            Connection conn = TaskManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, dynamicTaskId);
                stmt.setInt(2, tenantId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to delete a dynamic task which has the id " +
                    dynamicTaskId;
            log.error(msg, e);
            throw new TaskManagementDAOException(msg, e);
        }
    }

    @Override
    public DynamicTask getDynamicTask(int dynamicTaskId, int tenantId) throws TaskManagementDAOException {
        DynamicTask dynamicTask = null;
        try {
            Connection conn = TaskManagementDAOFactory.getConnection();
            String sql = "SELECT * FROM DYNAMIC_TASK WHERE DYNAMIC_TASK_ID= ? AND TENANT_ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, dynamicTaskId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        dynamicTask = TaskManagementDAOUtil.loadDynamicTask(rs);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting dynamic task data for task identifier '" +
                    dynamicTask + "'";
            log.error(msg, e);
            throw new TaskManagementDAOException(msg, e);
        }
        return dynamicTask;
    }

    @Override
    public List<DynamicTask> getAllDynamicTasks() throws TaskManagementDAOException {
        List<DynamicTask> dynamicTasks = null;
        try {
            Connection conn = TaskManagementDAOFactory.getConnection();
            String sql = "SELECT * FROM DYNAMIC_TASK";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    dynamicTasks = TaskManagementDAOUtil.loadDynamicTasks(rs);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting all dynamic task data ";
            log.error(msg, e);
            throw new TaskManagementDAOException(msg, e);
        }
        return dynamicTasks;
    }

    @Override
    public List<DynamicTask> getAllDynamicTasks(int tenantId) throws TaskManagementDAOException {
        List<DynamicTask> dynamicTasks;
        try {
            Connection conn = TaskManagementDAOFactory.getConnection();
            String sql = "SELECT * FROM DYNAMIC_TASK WHERE TENANT_ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    dynamicTasks = TaskManagementDAOUtil.loadDynamicTasks(rs);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting all dynamic task data ";
            log.error(msg, e);
            throw new TaskManagementDAOException(msg, e);
        }
        return dynamicTasks;
    }

    @Override
    public List<DynamicTask> getActiveDynamicTasks(int tenantId) throws TaskManagementDAOException {
        List<DynamicTask> dynamicTasks;
        try {
            Connection conn = TaskManagementDAOFactory.getConnection();
            String sql = "SELECT * FROM DYNAMIC_TASK WHERE IS_ENABLED = 'true' AND TENANT_ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    dynamicTasks = TaskManagementDAOUtil.loadDynamicTasks(rs);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting all dynamic task data ";
            log.error(msg, e);
            throw new TaskManagementDAOException(msg, e);
        }
        return dynamicTasks;
    }

}
