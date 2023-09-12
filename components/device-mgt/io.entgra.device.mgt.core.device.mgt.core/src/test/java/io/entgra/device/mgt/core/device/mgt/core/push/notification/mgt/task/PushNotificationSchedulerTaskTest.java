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
package io.entgra.device.mgt.core.device.mgt.core.push.notification.mgt.task;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementServiceComponent;
import io.entgra.device.mgt.core.device.mgt.core.operation.TestNotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationMapping;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * This class contains unit tests to test {@link PushNotificationSchedulerTask} class.
 */
public class PushNotificationSchedulerTaskTest extends BaseDeviceManagementTest {
    private static final Log log = LogFactory.getLog(PushNotificationSchedulerTask.class);
    private DeviceManagementProviderService deviceMgtProviderService;
    private PushNotificationSchedulerTask pushNotificationSchedulerTask;
    private OperationDAO operationDAO;

    private HeartBeatManagementService heartBeatManagementService;
    @BeforeClass
    public void init() throws DeviceManagementException, RegistryException {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing Push Notification Scheduler Test Class");
        DeviceManagementServiceComponent.notifyStartupListeners();
        this.deviceMgtProviderService = Mockito.mock(DeviceManagementProviderServiceImpl.class, Mockito.CALLS_REAL_METHODS);
        this.heartBeatManagementService = Mockito.mock(HeartBeatManagementService.class, Mockito.CALLS_REAL_METHODS);
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(this.deviceMgtProviderService);
        DeviceManagementDataHolder.getInstance().setHeartBeatService(this.heartBeatManagementService);
        this.operationDAO = OperationManagementDAOFactory.getOperationDAO();
        this.pushNotificationSchedulerTask = new PushNotificationSchedulerTask();
    }

    @Test(description = "Tests the push notification scheduling for devices")
    public void testPushNotificationScheduler()
            throws DeviceManagementException, OperationManagementException, SQLException,
            OperationManagementDAOException {
        try {
            log.info("Attempting to execute push notification task scheduler");
            Mockito.when(this.heartBeatManagementService.isTaskPartitioningEnabled()).thenReturn(false);
            Mockito.doReturn(new TestNotificationStrategy()).when(this.deviceMgtProviderService)
                    .getNotificationStrategyByDeviceType(Mockito.anyString());
            Mockito.doReturn(new io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation())
                    .when(this.deviceMgtProviderService).getOperation(Mockito.anyString(), Mockito.anyInt());
            this.pushNotificationSchedulerTask.run();
            OperationManagementDAOFactory.openConnection();
            Map<Integer, List<OperationMapping>> operationMappingsTenantMap = operationDAO
                    .getOperationMappingsByStatus(Operation.Status.PENDING, Operation.PushNotificationStatus.SCHEDULED,
                            DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                                    .getPushNotificationConfiguration().getSchedulerBatchSize());
            Assert.assertEquals(operationMappingsTenantMap.size(), 0);
            log.info("Push notification task execution complete.");
        } catch (HeartBeatManagementException e) {
            throw new RuntimeException(e);
        } finally {
            OperationManagementDAOFactory.closeConnection();
        }
    }
}
