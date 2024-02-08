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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization;

import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAO;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.DeviceOrganizationDAOFactory;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.mock.BaseDeviceOrganizationTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;

public class DAONegativeTest extends BaseDeviceOrganizationTest {

    private static final Log log = LogFactory.getLog(DAONegativeTest.class);

    private DeviceOrganizationDAO deviceOrganizationDAO;

    @BeforeClass
    public void init() {
        deviceOrganizationDAO = DeviceOrganizationDAOFactory.getDeviceOrganizationDAO();
        log.info("DAO test initialized");
    }

}
