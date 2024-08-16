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

package io.entgra.device.mgt.core.device.mgt.core.service;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.BadRequestException;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.*;

import java.util.List;

/**
 * Defines the contract of TagManagementService.
 */
public interface TagManagementProviderService {

    /**
     * Method to add a tag to the database.
     *
     * @param tag - Tag to be added to the database.
     * @throws TagManagementException if something goes wrong while adding the Tag.
     */
    void addTags(List<Tag> tag) throws TagManagementException, BadRequestException;

    /**
     * Method to fetch all tags.
     *
     * @return List of all Tags.
     * @throws TagManagementException if something goes wrong while fetching the Tags.
     */
    List<Tag> getAllTags() throws TagManagementException;

    /**
     * Method to update a tag in the database.
     *
     * @param tag - Tag to be updated in the database.
     * @throws TagManagementException if something goes wrong while updating the Tag.
     */
    void updateTag(Tag tag) throws TagManagementException, TagNotFoundException, BadRequestException;

    /**
     * Method to delete a tag from the database.
     *
     * @param tagId - ID of the tag to be deleted.
     * @throws TagManagementException if something goes wrong while deleting the Tag.
     */
    void deleteTag(int tagId) throws TagManagementException, TagNotFoundException;

    /**
     * Method to retrieve a tag by its ID.
     *
     * @param tagId - ID of the tag to be retrieved.
     * @return Tag object retrieved from the database.
     * @throws TagManagementException if something goes wrong while retrieving the Tag.
     */
    Tag getTagById(int tagId) throws TagManagementException, TagNotFoundException;

    /**
     * Method to retrieve a tag by its name.
     *
     * @param tagName - Name of the tag to be retrieved.
     * @return Tag object retrieved from the database.
     * @throws TagManagementException if something goes wrong while retrieving the Tag.
     * @throws TagNotFoundException if the Tag with the given name is not found.
     */
    Tag getTagByName(String tagName) throws TagManagementException, TagNotFoundException, BadRequestException;

    /**
     * Method to add a device-tag mapping.
     *
     * @param deviceIdentifiers - List of device ids to map.
     * @param deviceType - Device Type of that specific devices.
     * @param tags - List of tags you want to attach.
     * @throws TagManagementException if something goes wrong while adding the device-tag mapping.
     */
    void addDeviceTagMapping(List<String> deviceIdentifiers, String deviceType, List<String> tags)
            throws TagManagementException, BadRequestException;

    /**
     * Method to delete a device-tag mapping.
     *
     * @param deviceIdentifiers - List of device ids to map.
     * @param deviceType - Device Type of that specific devices.
     * @param tags - List of tags you want to attach.
     * @throws TagManagementException if something goes wrong while deleting the device-tag mapping.
     */
    void deleteDeviceTagMapping(List<String> deviceIdentifiers, String deviceType, List<String> tags)
            throws TagManagementException, BadRequestException;
}
