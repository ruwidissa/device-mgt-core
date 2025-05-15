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
package io.entgra.device.mgt.core.device.mgt.core;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.GroupPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.common.tag.mgt.Tag;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.Attribute;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.AttributeType;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.EventAttributeList;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.TransportType;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {


    private static final Log log = LogFactory.getLog(TestUtils.class);

    public static void cleanupResources(Connection conn, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing database connection", e);
            }
        }
    }

    public static DeviceGroup createDeviceGroup1() {
        DeviceGroup group = new DeviceGroup();
        group.setName("TEST_GROUP_01");
        group.setDescription("TEST_GROUP_01 - Description");
        group.setOwner("admin");
        return group;
    }


    public static DeviceGroup createDeviceGroup2() {
        DeviceGroup group = new DeviceGroup();
        group.setName("TEST_GROUP_02");
        group.setDescription("TEST_GROUP_02 - Description");
        group.setOwner("admin");
        return group;
    }

    public static DeviceGroup createDeviceGroup3() {
        DeviceGroup group = new DeviceGroup();
        group.setName("TEST_GROUP_03");
        group.setDescription("TEST_GROUP_03 - Description");
        group.setOwner("admin");
        return group;
    }

    public static DeviceGroup createDeviceGroup4() {
        DeviceGroup group = new DeviceGroup();
        group.setName("TEST_GROUP_04");
        group.setDescription("TEST_GROUP_04 - Description");
        group.setOwner("admin");
        return group;
    }

    public static Tag getTag1() {
        return new Tag(1, "tag1", "This is tag1");
    }

    public static Tag getTag2() {
        return new Tag(2, "tag2", "This is tag2");
    }

    public static Tag getTag1Dao() {
        return new Tag("tag1", "This is tag1");
    }

    public static Tag getTag2Dao() {
        return new Tag("tag2", "This is tag2");
    }

    public static Tag getTag3() {
        return new Tag("tag3", "This is tag3");
    }

    public static List<Tag> createTagList1() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag(null, "This is tag1"));
        return tagList;
    }

    public static List<Tag> createTagList2() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(getTag1());
        tagList.add(getTag2());
        return tagList;
    }

    public static List<Tag> createTagList3() {
        List<Tag> tagList = new ArrayList<>();
        tagList.add(getTag1Dao());
        tagList.add(getTag2Dao());
        return tagList;
    }

    public static GroupPaginationRequest createPaginationRequest() {
        GroupPaginationRequest request = new GroupPaginationRequest(0, 5);
        return request;
    }

    public static List<DeviceIdentifier> getDeviceIdentifiersList() {
        DeviceIdentifier identifier = new DeviceIdentifier();
        identifier.setId("12345");
        identifier.setType(TestDataHolder.TEST_DEVICE_TYPE);

        List<DeviceIdentifier> list = new ArrayList<>();
        list.add(identifier);

        return list;
    }

    public static RegistryService getRegistryService(Class clazz) throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = clazz.getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    public static List<DeviceTypeEvent> getDeviceTypeEvents() {
        List<DeviceTypeEvent> events = new ArrayList<>();

        // Create and populate the first event
        DeviceTypeEvent event1 = new DeviceTypeEvent();
        event1.setEventName("event1");
        event1.setEventTopicStructure("topic1/structure");
        EventAttributeList eventAttributeList = new EventAttributeList();
        Attribute attributes1 = new Attribute();
        attributes1.setName("attr1");
        attributes1.setType(AttributeType.INT);
        eventAttributeList.setList(new ArrayList<Attribute>() {{
            add(attributes1);
        }});
        event1.setEventAttributeList(eventAttributeList);
        event1.setTransportType(TransportType.MQTT);

        events.add(event1);
        return events;
    }
}
