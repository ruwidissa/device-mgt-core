/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceStatus;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;

import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

import static org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status.*;

public class DeviceStatusPersistenceTests extends BaseDeviceManagementTest {

    private static final Log log = LogFactory.getLog(DeviceStatusPersistenceTests.class);
    private EnrollmentDAO enrollmentDAO = DeviceManagementDAOFactory.getEnrollmentDAO();
    private DeviceStatusDAO deviceStatusDAO = DeviceManagementDAOFactory.getDeviceStatusDAO();

    /**
     * Validate that the list of statuses received match the statuses
     * @param device
     * @param receivedStatus
     * @param statuses
     */
    private void validateDeviceStatus(Device device, List<DeviceStatus> receivedStatus, EnrolmentInfo.Status[] statuses){
        Assert.assertEquals(receivedStatus.size(), statuses.length);
        for(int i = 0; i < statuses.length; i++) {
            Assert.assertEquals(receivedStatus.get(i).getDeviceId(), device.getId());
            Assert.assertEquals(receivedStatus.get(i).getStatus(), statuses[i]);
        }
    }

    /**
     * Validate the list of statuses corresponds to different enrolments and their statuses
     * @param device
     * @param receivedStatus
     * @param statuses
     */
    private void validateDeviceStatus(Device device, List<DeviceStatus> receivedStatus, EnrolmentInfo.Status[][] statuses){
        Map<Integer, List<DeviceStatus>> statusMap = new HashMap<>();
        for (DeviceStatus deviceStatus: receivedStatus) {
            Assert.assertEquals(deviceStatus.getDeviceId(), device.getId());
            if (!statusMap.containsKey(deviceStatus.getEnrolmentId())){
                statusMap.put(deviceStatus.getEnrolmentId(), new ArrayList<>());
            }
            statusMap.get(deviceStatus.getEnrolmentId()).add(deviceStatus);
        }
        Assert.assertEquals(statusMap.size(), statuses.length);
        Integer[] keys = (new TreeSet<>(statusMap.keySet())).toArray(new Integer[]{});
        for(int i = 0; i < keys.length; i++){
            validateDeviceStatus(device, statusMap.get(keys[i]), statuses[i]);
        }
    }

    @Test
    public void testSingleDeviceOneEnrolmentOneStatus(){
        try {
            this.initDataSource();
            Device device = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);
            addDevice(device);

            EnrolmentInfo.Status[] statuses = {ACTIVE};
            int enrolmentId = createNewEnrolmentAddStatuses(device, "admin", statuses);

//            DeviceManagementDAOFactory.openConnection();
            validateDeviceStatus(device, deviceStatusDAO.getStatus(enrolmentId), statuses);
            validateDeviceStatus(device, deviceStatusDAO.getStatus(device.getId(), TestDataHolder.SUPER_TENANT_ID),
                    new EnrolmentInfo.Status[][]{statuses});
        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while getting enrolment status", e);
        } catch (Exception e) {
            log.error("Error occurred while initializing datasource", e);
//        } finally{
//            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test
    public void testSingleDeviceOneEnrolmentMultipleStatus(){
        try {
            this.initDataSource();
            Device device = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);
            addDevice(device);
            EnrolmentInfo.Status[] statuses = {ACTIVE, ASSIGNED, CONFIGURED, READY_TO_CONNECT};
            int enrolmentId = createNewEnrolmentAddStatuses(device, "admin", statuses);
//            DeviceManagementDAOFactory.openConnection();
            validateDeviceStatus(device, deviceStatusDAO.getStatus(enrolmentId),statuses);
            validateDeviceStatus(device, deviceStatusDAO.getStatus(device.getId(), TestDataHolder.SUPER_TENANT_ID),
                    new EnrolmentInfo.Status[][]{statuses});
        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while getting enrolment status", e);
        } catch (Exception e) {
            log.error("Error occurred while initializing datasource", e);
//        } finally{
//            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test
    public void testSingleDeviceMultipleEnrolmentMultipleStatus(){
        try {
            this.initDataSource();
            Device device = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);
            addDevice(device);

            EnrolmentInfo.Status[] statuses1 = {ACTIVE, ASSIGNED, CONFIGURED, READY_TO_CONNECT};
            int enrolmentId1 = createNewEnrolmentAddStatuses(device, "admin1", statuses1);

            EnrolmentInfo.Status[] statuses2 = {CREATED, SUSPENDED, BLOCKED, DEFECTIVE, REMOVED, WARRANTY_REPLACED, BLOCKED};
            int enrolmentId2 = createNewEnrolmentAddStatuses(device, "admin2", statuses2);

            EnrolmentInfo.Status[] statuses3 = {READY_TO_CONNECT, ASSIGNED};
            addStatus(enrolmentId1, statuses3);

            EnrolmentInfo.Status[] statuses1_3 = Stream.concat(Arrays.stream(statuses1), Arrays.stream(statuses3)).toArray(EnrolmentInfo.Status[]::new);

            // introducing a delay so that data is committed to the database before we try to retrieve for validation
//            Thread.sleep(5000);

//            DeviceManagementDAOFactory.openConnection();
            validateDeviceStatus(device, deviceStatusDAO.getStatus(enrolmentId1), statuses1_3);
            validateDeviceStatus(device, deviceStatusDAO.getStatus(enrolmentId2), statuses2);

            validateDeviceStatus(device, deviceStatusDAO.getStatus(device.getId(), TestDataHolder.SUPER_TENANT_ID),
                    new EnrolmentInfo.Status[][]{statuses1_3, statuses2});
        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while getting enrolment status", e);
        } catch (Exception e) {
            log.error("Error occurred while initializing data source", e);
//        } finally{
//            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test
    public void testSettingAllDeviceStatusOfSingleUser(){
        try {
            this.initDataSource();
            Device device1 = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);
            addDevice(device1);

            Device device2 = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);
            addDevice(device2);

            EnrolmentInfo.Status[] statuses1 = {ACTIVE, ASSIGNED, CONFIGURED, READY_TO_CONNECT};
            int enrolmentId1 = createNewEnrolmentAddStatuses(device1, "admin1", statuses1);

            EnrolmentInfo.Status[] statuses2 = {CREATED, SUSPENDED, BLOCKED, DEFECTIVE, REMOVED, WARRANTY_REPLACED, BLOCKED};
            int enrolmentId2 = createNewEnrolmentAddStatuses(device1, "admin2", statuses2);

            EnrolmentInfo.Status[] statuses3 = {READY_TO_CONNECT, ASSIGNED};
            int enrolmentId3 = createNewEnrolmentAddStatuses(device2, "admin1", statuses3);

            enrollmentDAO.setStatusAllDevices("admin1", REMOVED, TestDataHolder.SUPER_TENANT_ID);

            EnrolmentInfo.Status[] statuses1_ = Stream.concat(Arrays.stream(statuses1), Arrays.stream(new EnrolmentInfo.Status[] {REMOVED})).toArray(EnrolmentInfo.Status[]::new);
            EnrolmentInfo.Status[] statuses3_ = Stream.concat(Arrays.stream(statuses3), Arrays.stream(new EnrolmentInfo.Status[] {REMOVED})).toArray(EnrolmentInfo.Status[]::new);

            validateDeviceStatus(device1, deviceStatusDAO.getStatus(enrolmentId1), statuses1_);
            validateDeviceStatus(device2, deviceStatusDAO.getStatus(enrolmentId3), statuses3_);

        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while getting enrolment status", e);
        } catch (Exception e) {
            log.error("Error occurred while initializing data source", e);
        }
    }
    private int addDevice(Device device) throws DeviceManagementDAOException {
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceDAO deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
            int deviceId = deviceDAO.addDevice(TestDataHolder.initialTestDeviceType.getId(), device, TestDataHolder.SUPER_TENANT_ID);
            device.setId(deviceId);
            return  deviceId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error while adding device", e);
        }finally{
            DeviceManagementDAOFactory.closeConnection();
        }

    }
    private int addEnrolment(Device device, String owner, int tenantId, EnrolmentInfo.Status initialStatus) {
        int deviceId = device.getId();
        EnrolmentInfo source = new EnrolmentInfo(owner, EnrolmentInfo.OwnerShip.BYOD, initialStatus);
        try {
            DeviceManagementDAOFactory.openConnection();
            EnrolmentInfo config = enrollmentDAO.addEnrollment(deviceId, source, tenantId);
            device.setEnrolmentInfo(config);
            return config.getId();
        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while adding enrollment", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return -1;
    }

    private int createNewEnrolmentAddStatuses(Device device, String user, EnrolmentInfo.Status[] statuses) {
        int enrolmentId = this.addEnrolment(device, user, TestDataHolder.SUPER_TENANT_ID, statuses[0]);
        Assert.assertNotEquals(enrolmentId, -1);
        addStatus(enrolmentId, Arrays.copyOfRange(statuses, 1, statuses.length));
        return enrolmentId;
    }

    private void addStatus(int enrolmentId, EnrolmentInfo.Status[] statuses) {
        for(int i = 0; i < statuses.length; i++) {
            Assert.assertTrue(addStatus(enrolmentId, statuses[i], TestDataHolder.SUPER_TENANT_ID));
        }
    }

    private boolean addStatus(int enrolmentId, EnrolmentInfo.Status status, int tenentId){
        try {
            DeviceManagementDAOFactory.openConnection();
            return enrollmentDAO.setStatus(enrolmentId, status, tenentId);
        } catch (DeviceManagementDAOException | SQLException e) {
            log.error("Error occurred while setting status", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        return false;
    }

    @BeforeClass
    @Override
    public void init() throws Exception {

    }
}
