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
package io.entgra.device.mgt.core.device.mgt.extensions.defaultrole.manager;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.extensions.defaultrole.manager.bean.DefaultRolesConfig;
import io.entgra.device.mgt.core.device.mgt.extensions.defaultrole.manager.internal.RoleManagerDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DefaultRoleReadingTest extends BaseTest {

    private static final Log log = LogFactory.getLog(DefaultRoleReadingTest.class);

    @Override
    @BeforeClass
    public void init() throws Exception {
        RoleManagerDataHolder.getInstance().setDefaultRolesConfigManager(new DefaultRolesConfigManager());
        log.info("Test initialized");
    }

    @Test
    public void readDefaultRoles() {
        DefaultRolesConfig defaultRolesConfig = RoleManagerDataHolder.getInstance()
                .getDefaultRolesConfigManager().getDefaultRolesConfig();
        Assert.assertNotNull(defaultRolesConfig);
        log.info(new Gson().toJson(defaultRolesConfig));
    }

}
