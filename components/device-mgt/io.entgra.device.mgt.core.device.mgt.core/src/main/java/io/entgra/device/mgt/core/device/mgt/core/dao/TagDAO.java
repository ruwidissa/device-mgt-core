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

package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.DeviceTag;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.Tag;

import java.util.List;

/**
 * This interface represents the key operations associated with persisting tag related information.
 */
public interface  TagDAO {

    /**
     * Add a new tag.
     *
     * @param tags      to be added.
     * @param tenantId of the tag.
     * @throws TagManagementDAOException
     */
    void addTags(List<Tag> tags, int tenantId) throws TagManagementDAOException;

    /**
     * Update an existing tag.
     *
     * @param tag     to be updated.
     * @param tenantId of the tag.
     * @throws TagManagementDAOException
     */
    void updateTag(Tag tag, int tenantId) throws TagManagementDAOException;

    /**
     * Delete an existing tag.
     *
     * @param tagId   of the tag.
     * @param tenantId of the tag.
     * @throws TagManagementDAOException
     */
    void deleteTag(int tagId, int tenantId) throws TagManagementDAOException;

    /**
     * Get a tag by id.
     *
     * @param tagId   of the tag.
     * @param tenantId of the tag.
     * @return Tag object.
     * @throws TagManagementDAOException
     */
    Tag getTagById(int tagId, int tenantId) throws TagManagementDAOException;

    /**
     * Method to retrieve a tag by its name.
     *
     * @param tagName - Name of the tag to be retrieved.
     * @param tenantId - Tenant ID.
     * @return Tag object retrieved from the database.
     * @throws TagManagementDAOException if something goes wrong while retrieving the Tag.
     */
    Tag getTagByName(String tagName, int tenantId) throws TagManagementDAOException;

    /**
     * Get all tags for a tenant.
     *
     * @param tenantId of the tag.
     * @return List of all tags.
     * @throws TagManagementDAOException
     */
    List<Tag> getTags(int tenantId) throws TagManagementDAOException;

    /**
     * Add a device tag mapping.
     *
     * @param deviceIdentifiers of the mapping.
     * @param deviceType of the mapping.
     * @param tagNames of the mapping.
     * @param tenantId  of the mapping.
     * @throws TagManagementDAOException
     */
    void addDeviceTagMapping(List<String> deviceIdentifiers, String deviceType, List<String> tagNames, int tenantId)
            throws TagManagementDAOException;

    /**
     * Delete a device tag mapping.
     *
     * @param deviceIdentifiers of the mapping.
     * @param deviceType of the mapping.
     * @param tagNames of the mapping.
     * @param tenantId of the mapping.
     * @throws TagManagementDAOException
     */
    void deleteDeviceTagMapping(List<String> deviceIdentifiers, String deviceType, List<String> tagNames, int tenantId)
            throws TagManagementDAOException;

    /**
     * Get all device tags for a device.
     *
     * @param deviceId of the device.
     * @param tenantId of the mapping.
     * @return List of device tags.
     * @throws TagManagementDAOException
     */
    List<DeviceTag> getTagsForDevice(int deviceId, int tenantId) throws TagManagementDAOException;

    /**
     * Get all devices for a tag.
     *
     * @param tagId    of the tag.
     * @param tenantId of the mapping.
     * @return List of device tags.
     * @throws TagManagementDAOException
     */
    List<DeviceTag> getDevicesForTag(int tagId, int tenantId) throws TagManagementDAOException;
}
