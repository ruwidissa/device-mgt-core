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

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.DeviceTaskManagerServiceImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DeviceManagementRepositoryTests{

	private DeviceManagementPluginRepository repository;

	@BeforeClass
	public void init() throws Exception {
		this.repository = new DeviceManagementPluginRepository();
		DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(new DeviceTaskManagerServiceImpl());
		DeviceManagementDataHolder.getInstance().setTaskService(new TestTaskServiceImpl());
	}

	@Test
	public void testAddDeviceManagementService() {
		DeviceManagementService sourceProvider = new TestDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE,
																				 TestDataHolder.SUPER_TENANT_DOMAIN);
		try {
			this.getRepository().addDeviceManagementProvider(sourceProvider);
		} catch (DeviceManagementException e) {
			Assert.fail("Unexpected error occurred while invoking addDeviceManagementProvider functionality", e);
		}
		DeviceManagementService targetProvider =
				this.getRepository().getDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE,
																TestDataHolder.SUPER_TENANT_ID);
		Assert.assertEquals(targetProvider.getType(), sourceProvider.getType());
	}

	@Test(dependsOnMethods = "testAddDeviceManagementService")
	public void testRemoveDeviceManagementService() {
		DeviceManagementService sourceProvider = new TestDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE,
																				 TestDataHolder.SUPER_TENANT_DOMAIN);
		try {
			this.getRepository().removeDeviceManagementProvider(sourceProvider);
		} catch (DeviceManagementException e) {
			Assert.fail("Unexpected error occurred while invoking removeDeviceManagementProvider functionality", e);
		}
		DeviceManagementService targetProvider =
				this.getRepository().getDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE,
																TestDataHolder.SUPER_TENANT_ID);
		Assert.assertNull(targetProvider);
	}

	private DeviceManagementPluginRepository getRepository() {
		return repository;
	}

}
