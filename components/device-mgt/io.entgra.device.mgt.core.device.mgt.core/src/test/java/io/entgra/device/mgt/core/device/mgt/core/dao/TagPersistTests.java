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

import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.Tag;
import io.entgra.device.mgt.core.device.mgt.core.TestUtils;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

public class TagPersistTests extends BaseDeviceManagementTest {

    private TagDAO tagDAO;
    private static final Log log = LogFactory.getLog(TagPersistTests.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        tagDAO = DeviceManagementDAOFactory.getTagDAO();
    }

    @Test
    public void addTag() {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            tagDAO.addTags(TestUtils.createTagList3(), TestDataHolder.ALTERNATE_TENANT_ID);
            log.debug("Tags added to the database");
            Tag tag = tagDAO.getTagByName("tag1", TestDataHolder.ALTERNATE_TENANT_ID);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertNotNull(tag, "Tag should be added and retrieved.");
            Assert.assertEquals(tag.getName(), "tag1", "Tag name mismatch.");
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding tag list type.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "addTag")
    public void updateTag() {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            String updatedDescription = "Updated Description";
            Tag tag = tagDAO.getTagByName("tag1", TestDataHolder.ALTERNATE_TENANT_ID);
            Tag tagToUpdate = new Tag(tag.getId(), tag.getName(), updatedDescription);
            tagDAO.updateTag(tagToUpdate, TestDataHolder.ALTERNATE_TENANT_ID);
            log.debug("Tag updated in the database");
            tag = tagDAO.getTagByName("tag1", TestDataHolder.ALTERNATE_TENANT_ID);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(tag.getDescription(), updatedDescription, "Tag description mismatch.");
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating tag.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "updateTag")
    public void deleteTag() {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            tagDAO.deleteTag(1, TestDataHolder.ALTERNATE_TENANT_ID);
            log.debug("Tag deleted from the database");
            Tag deletedTag = tagDAO.getTagById(1, TestDataHolder.ALTERNATE_TENANT_ID);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertNull(deletedTag, "Tag should be deleted.");
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting tag.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = {"deleteTag"})
    public void getTags() {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Tag> tags = tagDAO.getTags(TestDataHolder.ALTERNATE_TENANT_ID);
            DeviceManagementDAOFactory.commitTransaction();
            log.debug("Tags retrieved successfully.");
            Assert.assertEquals(tags.size(), 2);
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while retrieving tags.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = {"getTags"})
    public void getTagByName() {
        try {
            String tagName = "tag2";
            DeviceManagementDAOFactory.beginTransaction();
            Tag tag = tagDAO.getTagByName(tagName, TestDataHolder.ALTERNATE_TENANT_ID);
            DeviceManagementDAOFactory.commitTransaction();
            log.debug("Tag " + tagName +  " retrieved successfully.");
            Assert.assertEquals(tag.getName(), "tag2");
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while retrieving tag by Id.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "getTagByName")
    public void addTagsForAlternateTenant() {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            //Here, adding a same tag name for a separate tenant is tested.
            tagDAO.addTags(TestUtils.createTagList3(), TestDataHolder.ALTERNATE_TENANT_ID_1);
            log.debug("Tags added for a alternate tenant");
            List<Tag> tagList = tagDAO.getTags(TestDataHolder.ALTERNATE_TENANT_ID_1);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(tagList.size(), 2, "Tag count mismatch.");
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding tags for a different tenant.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "addTagsForAlternateTenant")
    public void getTagsOfAlternateTenant() {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Tag> tagList = tagDAO.getTags(TestDataHolder.ALTERNATE_TENANT_ID_1);
            log.debug("Tags retrieved for a alternate tenant " + TestDataHolder.ALTERNATE_TENANT_ID_1);
            DeviceManagementDAOFactory.commitTransaction();
            Assert.assertEquals(tagList.size(), 2, "Tag count mismatch.");
        } catch (TagManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding tags for a different tenant.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }
}
