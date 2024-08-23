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
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.Tag;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.TagManagementException;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.TagMappingDTO;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.TagNotFoundException;
import io.entgra.device.mgt.core.device.mgt.core.TestUtils;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TagManagementProviderServiceTest extends BaseDeviceManagementTest {

    private TagManagementProviderService tagManagementProviderService;

    public static final String DEVICE_ID_1 = "100001";
    public static final String DEVICE_ID_2 = "100002";
    private static final String DEVICE_TYPE = "RANDOM_DEVICE_TYPE";

    @BeforeClass
    @Override
    public void init() throws Exception {
        tagManagementProviderService = new TagManagementProviderServiceImpl();
        RealmService realmService = new InMemoryRealmService();
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        realmService.getTenantManager().getSuperTenantDomain();
        DeviceConfigurationManager.getInstance().initConfig();
    }

    @Test(expectedExceptions = {TagManagementException.class, BadRequestException.class})
    public void createTagsNull() throws TagManagementException, BadRequestException {
        tagManagementProviderService.addTags(null);
    }

    @Test(expectedExceptions = {TagManagementException.class, BadRequestException.class}, dependsOnMethods = "createTagsNull")
    public void createTagsNameNullError() throws TagManagementException, BadRequestException {
        tagManagementProviderService.addTags(TestUtils.createTagList1());
    }

    @Test(dependsOnMethods = "createTagsNameNullError")
    public void createTags() throws TagManagementException, BadRequestException {
        tagManagementProviderService.addTags(TestUtils.createTagList2());
    }

    @Test(expectedExceptions = {TagNotFoundException.class, BadRequestException.class, TagManagementException.class})
    public void updateTagsNotFound() throws TagNotFoundException, TagManagementException, BadRequestException {
        String updateDescString = "This tag is updated";
        tagManagementProviderService.updateTag(new Tag(10,"tag10", updateDescString));
    }

    @Test(dependsOnMethods = "updateTagsNotFound")
    public void updateTags() throws TagNotFoundException, TagManagementException, BadRequestException {
        String updateDescString = "This tag is updated";
        tagManagementProviderService.updateTag(new Tag(1,"tag1", updateDescString));
        Tag tag = tagManagementProviderService.getTagById(1);
        Assert.assertEquals(tag.getDescription(), updateDescString);
    }

    @Test(dependsOnMethods = "updateTags", expectedExceptions = {TagNotFoundException.class})
    public void getTagNotFoundById() throws TagManagementException, TagNotFoundException {
        tagManagementProviderService.getTagById(10);
    }

    @Test(dependsOnMethods = "getTagNotFoundById", expectedExceptions = {BadRequestException.class})
    public void getTagNotFoundByNameNull() throws TagManagementException, TagNotFoundException, BadRequestException {
        tagManagementProviderService.getTagByName(null);
    }

    @Test(dependsOnMethods = "getTagNotFoundByNameNull", expectedExceptions = {TagNotFoundException.class})
    public void getTagNotFoundByName() throws TagManagementException, TagNotFoundException, BadRequestException {
        tagManagementProviderService.getTagByName("tag10");
    }

    @Test(dependsOnMethods = "getTagNotFoundByName")
    public void getTagById() throws TagManagementException, TagNotFoundException {
        Tag tag = tagManagementProviderService.getTagById(2);
        Assert.assertEquals(tag.getName(), TestUtils.getTag2().getName());
    }

    @Test(dependsOnMethods = "getTagById")
    public void getTagByName() throws TagManagementException, TagNotFoundException, BadRequestException {
        Tag tag = tagManagementProviderService.getTagByName("tag2");
        Assert.assertEquals(tag.getName(), TestUtils.getTag2().getName());
    }

    @Test(dependsOnMethods = "getTagByName")
    public void getTags() throws TagManagementException {
        List<Tag> tags = tagManagementProviderService.getAllTags();
        Assert.assertEquals(tags.size(), 3);
    }

    @Test(expectedExceptions = {TagNotFoundException.class}, dependsOnMethods = "getTags")
    public void deleteTagNotExists() throws TagManagementException, TagNotFoundException {
        tagManagementProviderService.deleteTag(10);
    }

    @Test(dependsOnMethods = "deleteTagNotExists")
    public void deleteTag() throws TagManagementException, TagNotFoundException {
        tagManagementProviderService.deleteTag(1);
        List<Tag> tags = tagManagementProviderService.getAllTags();
        Assert.assertEquals(tags.size(), 2);
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "deleteTag")
    public void createTagMappingsNull() throws TagManagementException, BadRequestException {
        tagManagementProviderService.addDeviceTagMapping(null, null, null);
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "createTagMappingsNull")
    public void createTagsMappingsNullDeviceIdentifiers() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.addDeviceTagMapping(null, DEVICE_TYPE,
                new ArrayList<>(Arrays.asList("tag1", "tag2")));
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "createTagsMappingsNullDeviceIdentifiers")
    public void createTagsMappingsNullDeviceType() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.addDeviceTagMapping(new ArrayList<>(Arrays.asList(DEVICE_ID_1, DEVICE_ID_2)),
                null, new ArrayList<>(Arrays.asList("tag1", "tag2")));
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "createTagsMappingsNullDeviceType")
    public void createTagsMappingsNullTags() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.addDeviceTagMapping(new ArrayList<>(Arrays.asList(DEVICE_ID_1, DEVICE_ID_2)),
                DEVICE_TYPE, null);
    }

    @Test(dependsOnMethods = "createTagsMappingsNullTags")
    public void createTagsMappings() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.addDeviceTagMapping(new ArrayList<>(Arrays.asList(DEVICE_ID_1, DEVICE_ID_2)),
                DEVICE_TYPE, new ArrayList<>(Arrays.asList("tag1", "tag2")));
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "createTagsMappings")
    public void deleteTagMappingsNull() throws TagManagementException, BadRequestException {
        tagManagementProviderService.deleteDeviceTagMapping(null, null, null);
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "deleteTagMappingsNull")
    public void deleteTagsMappingsNullDeviceIdentifiers() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.deleteDeviceTagMapping(null, DEVICE_TYPE,
                new ArrayList<>(Arrays.asList("tag1", "tag2")));
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "deleteTagsMappingsNullDeviceIdentifiers")
    public void deleteTagsMappingsNullDeviceType() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.deleteDeviceTagMapping(new ArrayList<>(Arrays.asList(DEVICE_ID_1, DEVICE_ID_2)),
                null, new ArrayList<>(Arrays.asList("tag1", "tag2")));
    }

    @Test(expectedExceptions = {BadRequestException.class}, dependsOnMethods = "deleteTagsMappingsNullDeviceType")
    public void deleteTagsMappingsNullTags() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.deleteDeviceTagMapping(new ArrayList<>(Arrays.asList(DEVICE_ID_1, DEVICE_ID_2)),
                DEVICE_TYPE, null);
    }

    @Test(dependsOnMethods = "deleteTagsMappingsNullTags")
    public void deleteTagsMappings() throws TagManagementException, DeviceManagementException {
        tagManagementProviderService.deleteDeviceTagMapping(new ArrayList<>(Arrays.asList(DEVICE_ID_1, DEVICE_ID_2)),
                DEVICE_TYPE, new ArrayList<>(Arrays.asList("tag1", "tag2")));
    }
}

