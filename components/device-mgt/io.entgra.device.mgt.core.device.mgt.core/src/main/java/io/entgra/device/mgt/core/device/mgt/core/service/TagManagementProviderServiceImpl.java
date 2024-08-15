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
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.*;
import io.entgra.device.mgt.core.device.mgt.core.dao.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TagManagementProviderServiceImpl implements TagManagementProviderService {
    private static final Log log = LogFactory.getLog(TagManagementProviderServiceImpl.class);

    private final TagDAO tagDAO;

    public TagManagementProviderServiceImpl() {
        this.tagDAO = DeviceManagementDAOFactory.getTagDAO();
    }

    @Override
    public void addTags(List<Tag> tags) throws TagManagementException, BadRequestException {
        if (tags == null || tags.isEmpty()) {
            String msg = "Received incomplete data for tags";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            for (Tag tag : tags) {
                if (tag.getName() == null) {
                    throw new BadRequestException("Tag name cannot be null");
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Starting creating Tags.");
            }
            tagDAO.addTags(tags, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding tags to database.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in creating tags.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Tag> getAllTags() throws TagManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            return tagDAO.getTags(tenantId);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while retrieving tags.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in retrieving tags.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Tag getTagById(int tagId) throws TagManagementException, TagNotFoundException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            Tag tag = tagDAO.getTagById(tagId, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            if (tag == null) {
                String msg = "Tag with ID " + tagId + " not found.";
                throw new TagNotFoundException(msg);
            }
            return tag;
        } catch (TransactionManagementException | TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while retrieving the tag with ID: " + tagId;
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Tag getTagByName(String tagName) throws TagManagementException, TagNotFoundException, BadRequestException {
        if (tagName == null || tagName.trim().isEmpty()) {
            String msg = "Tag name cannot be null or empty.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.beginTransaction();
            Tag tag = tagDAO.getTagByName(tagName, tenantId);
            DeviceManagementDAOFactory.commitTransaction();

            if (tag == null) {
                String msg = "Tag with name " + tagName + " not found.";
                throw new TagNotFoundException(msg);
            }

            return tag;
        } catch (TransactionManagementException | TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while retrieving the tag with name: " + tagName;
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }


    @Override
    public void updateTag(Tag tag) throws TagManagementException, TagNotFoundException, BadRequestException {
        if (tag == null || tag.getName() == null) {
            String msg = "Received incomplete data for tag update";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.openConnection();
            Tag existingTag = tagDAO.getTagById(tag.getId(), tenantId);
            if (existingTag == null) {
                String msg = "Tag with ID: " + tag.getId() + " does not exist.";
                log.error(msg);
                throw new TagNotFoundException(msg);
            }
            tagDAO.updateTag(tag, tenantId);
        } catch (TagManagementDAOException | SQLException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating the tag with ID: " + tag.getId();
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }


    @Override
    public void deleteTag(int tagId) throws TagManagementException, TagNotFoundException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.openConnection();
            Tag existingTag = tagDAO.getTagById(tagId, tenantId);
            if (existingTag == null) {
                String msg = "Tag with ID: " + tagId + " does not exist.";
                log.error(msg);
                throw new TagNotFoundException(msg);
            }
            tagDAO.deleteTag(tagId, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TagManagementDAOException | SQLException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting the tag with ID: " + tagId;
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void addDeviceTagMapping(TagMappingDTO tagMappingDto) throws TagManagementException, BadRequestException {
        if (tagMappingDto == null || tagMappingDto.getDeviceIdentifiers() == null || tagMappingDto.getTags() == null
                || tagMappingDto.getDeviceType() == null) {
            String msg = "Received incomplete data for device tag mapping.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.openConnection();

            List<Tag> tagList = new ArrayList<>();
            for (String tagName : tagMappingDto.getTags()) {
                Tag tag = new Tag();
                tag.setName(tagName);
                tagList.add(tag);
            }

            tagDAO.addTags(tagList, tenantId);
            tagDAO.addDeviceTagMapping(tagMappingDto.getDeviceIdentifiers(), tagMappingDto.getDeviceType(),
                    tagMappingDto.getTags(), tenantId);

        } catch (TagManagementDAOException e) {
            if (e.isUniqueConstraintViolation()) {
                String msg = "Tag is already mapped to this device.";
                log.info(msg, e);
                throw new BadRequestException(msg);
            } else {
                DeviceManagementDAOFactory.rollbackTransaction();
                String msg = "Error occurred while adding device tag mapping.";
                log.error(msg, e);
                throw new TagManagementException(msg, e);
            }
        } catch (SQLException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device tag mapping.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public void deleteDeviceTagMapping(TagMappingDTO tagMappingDto) throws TagManagementException, BadRequestException {
        if (tagMappingDto == null || tagMappingDto.getDeviceIdentifiers() == null || tagMappingDto.getTags() == null
                || tagMappingDto.getDeviceType() == null) {
            String msg = "Received incomplete data for device tag mapping.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            DeviceManagementDAOFactory.openConnection();
            tagDAO.deleteDeviceTagMapping(tagMappingDto.getDeviceIdentifiers(), tagMappingDto.getDeviceType(),
                    tagMappingDto.getTags(), tenantId);
            DeviceManagementDAOFactory.commitTransaction();
        } catch (TagManagementDAOException | SQLException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting device tag mappings.";
            log.error(msg, e);
            throw new TagManagementException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
