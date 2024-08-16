/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl;

import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.DeviceTag;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.Tag;
import io.entgra.device.mgt.core.device.mgt.core.dao.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.entgra.device.mgt.core.device.mgt.core.dao.util.TagManagementDAOUtil.loadDeviceTagMapping;
import static io.entgra.device.mgt.core.device.mgt.core.dao.util.TagManagementDAOUtil.loadTag;
import static io.entgra.device.mgt.core.device.mgt.core.dao.util.TagManagementDAOUtil.cleanupResources;

public class TagDAOImpl implements TagDAO {

    private static final Log log = LogFactory.getLog(TagDAOImpl.class);

    protected Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    @Override
    public void addTags(List<Tag> tags, int tenantId) throws TagManagementDAOException {
        String query = "INSERT INTO DM_TAG (NAME, DESCRIPTION, TENANT_ID) " +
                "SELECT ?, ?, ? " +
                "WHERE NOT EXISTS ( " +
                "    SELECT 1 FROM DM_TAG " +
                "    WHERE NAME = ? AND TENANT_ID = ? " +
                ")";
        Connection connection;
        PreparedStatement preparedStatement = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (Tag tag : tags) {
                preparedStatement.setString(1, tag.getName());
                preparedStatement.setString(2, tag.getDescription());
                preparedStatement.setInt(3, tenantId);
                preparedStatement.setString(4, tag.getName());
                preparedStatement.setInt(5, tenantId);
                preparedStatement.addBatch();
            }
            int[] updateCounts = preparedStatement.executeBatch();
            for (int count : updateCounts) {
                if (count == PreparedStatement.EXECUTE_FAILED) {
                    String msg = "Error occurred while adding tags, adding some tags failed.";
                    log.error(msg);
                    throw new TagManagementDAOException(msg);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding tags.";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public void updateTag(Tag tag, int tenantId) throws TagManagementDAOException {
        String query = "UPDATE DM_TAG SET NAME = ?, DESCRIPTION = ? WHERE ID = ? AND TENANT_ID = ?";
        Connection connection;
        PreparedStatement preparedStatement = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, tag.getName());
            preparedStatement.setString(2, tag.getDescription());
            preparedStatement.setInt(3, tag.getId());
            preparedStatement.setInt(4, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while updating tag.";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public void deleteTag(int tagId, int tenantId) throws TagManagementDAOException {
        String query = "DELETE FROM DM_TAG WHERE ID = ? AND TENANT_ID = ?";
        Connection connection;
        PreparedStatement preparedStatement = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, tagId);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting tag.";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public Tag getTagById(int tagId, int tenantId) throws TagManagementDAOException {
        String query = "SELECT * FROM DM_TAG WHERE ID = ? AND TENANT_ID = ?";
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Tag tag = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, tagId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                tag = loadTag(resultSet);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getting a specific tag." + tagId;
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, resultSet);
        }
        return tag;
    }

    @Override
    public Tag getTagByName(String tagName, int tenantId) throws TagManagementDAOException {
        String query = "SELECT * FROM DM_TAG WHERE NAME = ? AND TENANT_ID = ?";
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Tag tag = null;

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, tagName);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                tag = loadTag(resultSet);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving tag with name: " + tagName;
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, resultSet);
        }
        return tag;
    }


    @Override
    public List<Tag> getTags(int tenantId) throws TagManagementDAOException {
        String query = "SELECT * FROM DM_TAG WHERE TENANT_ID = ?";
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Tag> tags = new ArrayList<>();

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Tag tag = loadTag(resultSet);
                tags.add(tag);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving tags";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, resultSet);
        }
        return tags;
    }

    @Override
    public void addDeviceTagMapping(List<String> deviceIdentifiers, String deviceType, List<String> tagNames, int tenantId) throws TagManagementDAOException {
        String deviceIdentifiersPlaceholders = String.join(", ", Collections.nCopies(deviceIdentifiers.size(), "?"));
        String tagNamesPlaceholders = String.join(", ", Collections.nCopies(tagNames.size(), "?"));

        String query = String.format(
                "INSERT INTO DM_DEVICE_TAG_MAPPING (ENROLMENT_ID, TAG_ID, TENANT_ID) " +
                        "SELECT e.ID, t.ID, ? " +
                        "FROM DM_ENROLMENT e " +
                        "JOIN DM_DEVICE d ON d.ID = e.DEVICE_ID " +
                        "JOIN DM_TAG t ON t.NAME IN (%s) " +
                        "WHERE d.DEVICE_IDENTIFICATION IN (%s) " +
                        "AND e.DEVICE_TYPE = ? " +
                        "AND e.STATUS != 'REMOVED' " +
                        "AND e.TENANT_ID = ? " +
                        "AND t.TENANT_ID = ? " +
                        "AND NOT EXISTS ( " +
                        "    SELECT 1 " +
                        "    FROM DM_DEVICE_TAG_MAPPING m " +
                        "    WHERE m.ENROLMENT_ID = e.ID " +
                        "    AND m.TAG_ID = t.ID " +
                        "    AND m.TENANT_ID = ? " +
                        ")",
                tagNamesPlaceholders,
                deviceIdentifiersPlaceholders
        );

        Connection connection;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            int paramIndex = 1;
            preparedStatement.setInt(paramIndex++, tenantId);
            for (String tagName : tagNames) {
                preparedStatement.setString(paramIndex++, tagName);
            }
            for (String deviceIdentifier : deviceIdentifiers) {
                preparedStatement.setString(paramIndex++, deviceIdentifier);
            }
            preparedStatement.setString(paramIndex++, deviceType);
            preparedStatement.setInt(paramIndex++, tenantId);
            preparedStatement.setInt(paramIndex++, tenantId);
            preparedStatement.setInt(paramIndex, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            String msg = "Tag is already mapped to this device";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e, true);
        } catch (SQLException e) {
            String msg = "Error occurred while adding device tag mapping";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public void deleteDeviceTagMapping(List<String> deviceIdentifiers, String deviceType, List<String> tagNames, int tenantId) throws TagManagementDAOException {
        String deviceIdentifiersPlaceholders = String.join(", ", Collections.nCopies(deviceIdentifiers.size(), "?"));
        String tagNamesPlaceholders = String.join(", ", Collections.nCopies(tagNames.size(), "?"));

        String query = String.format(
                "DELETE FROM DM_DEVICE_TAG_MAPPING " +
                        "WHERE ENROLMENT_ID IN ( " +
                        "    SELECT e.ID " +
                        "    FROM DM_ENROLMENT e " +
                        "    JOIN DM_DEVICE d ON d.ID = e.DEVICE_ID " +
                        "    WHERE d.DEVICE_IDENTIFICATION IN (%s) " +
                        "    AND e.DEVICE_TYPE = ? " +
                        "    AND e.TENANT_ID = ? " +
                        "    AND e.STATUS != 'REMOVED' " +
                        ") " +
                        "AND TAG_ID IN ( " +
                        "    SELECT t.ID " +
                        "    FROM DM_TAG t " +
                        "    WHERE t.NAME IN (%s) " +
                        "    AND t.TENANT_ID = ? " +
                        ")",
                deviceIdentifiersPlaceholders,
                tagNamesPlaceholders
        );
        Connection connection;
        PreparedStatement preparedStatement = null;
        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            int paramIndex = 1;
            for (String deviceIdentifier : deviceIdentifiers) {
                preparedStatement.setString(paramIndex++, deviceIdentifier);
            }
            preparedStatement.setString(paramIndex++, deviceType);
            preparedStatement.setInt(paramIndex++, tenantId);
            for (String tagName : tagNames) {
                preparedStatement.setString(paramIndex++, tagName);
            }
            preparedStatement.setInt(paramIndex, tenantId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting device tag mapping";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, null);
        }
    }

    @Override
    public List<DeviceTag> getTagsForDevice(int deviceId, int tenantId) throws TagManagementDAOException {
        String query = "SELECT * FROM DM_DEVICE_TAG_MAPPING WHERE DEVICE_ID = ? AND TENANT_ID = ?";
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<DeviceTag> deviceTags = new ArrayList<>();

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, deviceId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DeviceTag deviceTag = loadDeviceTagMapping(resultSet);
                deviceTags.add(deviceTag);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving device tags";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, resultSet);
        }
        return deviceTags;
    }

    @Override
    public List<DeviceTag> getDevicesForTag(int tagId, int tenantId) throws TagManagementDAOException {
        String query = "SELECT * FROM DM_DEVICE_TAG_MAPPING WHERE TAG_ID = ? AND TENANT_ID = ?";
        Connection connection;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<DeviceTag> deviceTags = new ArrayList<>();

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, tagId);
            preparedStatement.setInt(2, tenantId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                DeviceTag deviceTag = loadDeviceTagMapping(resultSet);
                deviceTags.add(deviceTag);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving devices for tag";
            log.error(msg, e);
            throw new TagManagementDAOException(msg, e);
        } finally {
            cleanupResources(preparedStatement, resultSet);
        }
        return deviceTags;
    }
}
