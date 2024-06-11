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

package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceData;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoCluster;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoCoordinate;
import io.entgra.device.mgt.core.device.mgt.common.geo.service.GeoQuery;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo.Status;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.TestUtils;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DevicePersistTests extends BaseDeviceManagementTest {

    DeviceDAO deviceDAO;
    DeviceTypeDAO deviceTypeDAO;

    private static final Log log = LogFactory.getLog(DevicePersistTests.class);

    @BeforeClass
    @Override
    public void init() throws Exception {
        initDataSource();
        deviceDAO = DeviceManagementDAOFactory.getDeviceDAO();
        deviceTypeDAO = DeviceManagementDAOFactory.getDeviceTypeDAO();
    }

    @Test
    public void testAddDeviceTypeTest() {
        DeviceType deviceType = TestDataHolder.generateDeviceTypeData(TestDataHolder.TEST_DEVICE_TYPE);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            deviceTypeDAO.addDeviceType(deviceType, TestDataHolder.SUPER_TENANT_ID, true);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device type '" + deviceType.getName() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction to persist device type '" +
                    deviceType.getName() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        Integer targetTypeId = null;
        try {
            targetTypeId = this.getDeviceTypeId(TestDataHolder.TEST_DEVICE_TYPE);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving target device type id";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
        Assert.assertNotNull(targetTypeId, "Device Type Id is null");
        deviceType.setId(targetTypeId);
        TestDataHolder.initialTestDeviceType = deviceType;
    }

    @Test(dependsOnMethods = {"testAddDeviceTypeTest"})
    public void testAddDeviceTest() {
        int tenantId = TestDataHolder.SUPER_TENANT_ID;
        Device device = TestDataHolder.generateDummyDeviceData(TestDataHolder.TEST_DEVICE_TYPE);
        device.setName(TestDataHolder.initialDeviceName);

        try {
            DeviceManagementDAOFactory.beginTransaction();
            int deviceId = deviceDAO.addDevice(TestDataHolder.initialTestDeviceType.getId(), device, tenantId);
            device.setId(deviceId);
            deviceDAO.addEnrollment(device, tenantId);
            DeviceManagementDAOFactory.commitTransaction();
            TestDataHolder.initialTestDevice = device;
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding '" + device.getType() + "' device with the identifier '" +
                    device.getDeviceIdentifier() + "'";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        int targetId = -1;
        try {
            targetId = this.getDeviceId(TestDataHolder.initialTestDevice.getDeviceIdentifier(),
                    TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving device id";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
        Assert.assertNotNull(targetId, "Device Id persisted in device management metadata repository upon '" +
                device.getType() + "' carrying the identifier '" + device.getDeviceIdentifier() + "', is null");
    }

    private int getDeviceId(String deviceIdentification, int tenantId) throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        int id = -1;
        try {
            Assert.assertNotNull(getDataSource(), "Data Source is not initialized properly");
            conn = getDataSource().getConnection();
            String sql = "SELECT ID FROM DM_DEVICE WHERE DEVICE_IDENTIFICATION = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentification);
            stmt.setInt(2, tenantId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("ID");
            }
            return id;
        } catch (SQLException e) {
            String msg = "Error in fetching device by device identification id";
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            TestUtils.cleanupResources(conn, stmt, rs);
        }
    }

    private int getDeviceTypeId(String deviceTypeName) throws DeviceManagementDAOException {
        int id = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        String sql = "SELECT ID, NAME FROM DM_DEVICE_TYPE WHERE NAME = ?";

        try {
            Assert.assertNotNull(getDataSource(), "Data Source is not initialized properly");
            conn = getDataSource().getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceTypeName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                id = rs.getInt("ID");
            }
            return id;
        } catch (SQLException e) {
            String msg = "Error in fetching device type by name IOS";
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            TestUtils.cleanupResources(conn, stmt, null);
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void testSetEnrolmentStatus() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceIdentifier deviceId = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            deviceDAO.setEnrolmentStatus(deviceId, device.getEnrolmentInfo().getOwner(), Status.ACTIVE,
                    TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while setting enrolment status";
            log.error(msg, e);
            Assert.fail(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            Assert.fail(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
        Status target = null;
        try {
            target = this.getEnrolmentStatus(device.getDeviceIdentifier(), device.getType(),
                    TestDataHolder.SUPER_TENANT_ID);
        } catch (DeviceManagementDAOException e) {
            String msg = "Error occurred while retrieving the target enrolment status";
            log.error(msg, e);
            Assert.fail(msg, e);
        }
        if (!isMock()) {
            Assert.assertNotNull(target, "Enrolment status retrieved for the device carrying its identifier as '" +
                    device.getDeviceIdentifier() + "' is null");
            Assert.assertEquals(target, Status.ACTIVE, "Enrolment status retrieved is not as same as what's configured");
        }
    }

    private Status getEnrolmentStatus(String identifier, String deviceType, int tenantId)
            throws DeviceManagementDAOException {

        Device device = TestDataHolder.generateDummyDeviceData("ios");
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceIdentifier deviceId = new DeviceIdentifier(identifier, deviceType);
            return deviceDAO.getEnrolmentStatus(deviceId, device.getEnrolmentInfo().getOwner(), tenantId);
        } catch (DeviceManagementDAOException | SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the current status of the " +
                    "enrolment", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDevicesByIdentifiersTest() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.openConnection();
            DeviceIdentifier deviceId = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            List<Device> retrieved = deviceDAO.getDevicesByIdentifiers(
                    Collections.singletonList(deviceId.getId()), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, retrieved.size(), "Device count is not matched to expected.");
        } catch (DeviceManagementDAOException | SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the current status of the " +
                    "enrolment", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void recordDeviceUpdateTest() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            device = deviceDAO.getDevice(device.getDeviceIdentifier(), TestDataHolder.SUPER_TENANT_ID);
            log.info("Device before update: " + device);
            DeviceIdentifier deviceId = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            boolean updated = deviceDAO.recordDeviceUpdate(deviceId, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertTrue(updated, "Device timestamp is not updated.");
            Device updatedDevice = deviceDAO.getDevice(device.getDeviceIdentifier(), TestDataHolder.SUPER_TENANT_ID);
            log.info("Device after update: " + updatedDevice);
            Assert.assertTrue(device.getLastUpdatedTimeStamp() < updatedDevice.getLastUpdatedTimeStamp(),
                    "Last updated timestamp is way older.");
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the current status of the " +
                    "enrolment", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByStatusTest() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            PaginationRequest pr = new PaginationRequest(0, 10);
            pr.setStatusList(Collections.singletonList(Status.ACTIVE.name()));
            List<Device> results = deviceDAO.getDevicesByStatus(pr, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the current status of the " +
                    "enrolment", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDevicesByTenantId() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getDevices(TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByDeviceId() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Device results = deviceDAO.getDevice(device.getDeviceIdentifier(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotNull(results.getDeviceIdentifier(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByDeviceIdentifierWithTenantId() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
            Device results = deviceDAO.getDevice(deviceIdentifier, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotNull(results.getDeviceIdentifier(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByDeviceData() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        DeviceData deviceData = TestDataHolder.generateDummyDevice(deviceIdentifier);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Device results = deviceDAO.getDevice(deviceData, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotNull(results.getDeviceIdentifier(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByOwner() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Device results = deviceDAO.getDevice(deviceIdentifier, TestDataHolder.OWNER, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotNull(results.getDeviceIdentifier(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByDateSince() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Device results = deviceDAO.getDevice(deviceIdentifier, TestDataHolder.getTimeBefore(1), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotNull(results.getDeviceIdentifier(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByDateSinceWithDeviceId() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Device results = deviceDAO.getDevice(device.getDeviceIdentifier(), TestDataHolder.getTimeBefore(1), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotNull(results.getDeviceIdentifier(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByEnrollmentStatus() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            Device results = deviceDAO.getDevice(deviceIdentifier, Status.ACTIVE, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(device.getDeviceIdentifier(), results.getDeviceIdentifier(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByDeviceIdentifier() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            SingletonMap results = deviceDAO.getDevice(deviceIdentifier);
            Assert.assertNotNull(results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceByDeviceType() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getDevices(device.getType(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getAllocatedDevices() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getAllocatedDevices(device.getType(), TestDataHolder.SUPER_TENANT_ID, 1, 0);
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDevicesOfUser() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getDevicesOfUser(TestDataHolder.OWNER, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDevicesOfUserWithDeviceType() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getDevicesOfUser(TestDataHolder.OWNER, device.getType(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDevicesOfUserWithDeviceStatus() throws DeviceManagementDAOException, TransactionManagementException {
        List<String> status = new ArrayList<>() ;
        status.add(Status.ACTIVE.name());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getDevicesOfUser(TestDataHolder.OWNER, TestDataHolder.SUPER_TENANT_ID, status);
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getCountOfDevicesInGroup() throws DeviceManagementDAOException, TransactionManagementException {
        PaginationRequest pr = new PaginationRequest(0, 10);
        pr.setGroupId(1);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getCountOfDevicesInGroup(pr, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(0, results, "No device count returned in group");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device count" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountWithOwner() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCount(TestDataHolder.OWNER, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountWithType() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCount(device.getType(), Status.ACTIVE.name(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void setEnrolmentStatusInBulk() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        List<String> devices = new ArrayList<>() ;
        devices.add(device.getDeviceIdentifier());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            boolean results = deviceDAO.setEnrolmentStatusInBulk(device.getType(), Status.ACTIVE.name(),TestDataHolder.SUPER_TENANT_ID, devices );
            Assert.assertTrue(results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCount() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCount(TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountWithPagination() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        PaginationRequest pr = new PaginationRequest(0, 10);
        pr.setDeviceName(device.getName());
        pr.setDeviceType(device.getType());
        pr.setOwner(TestDataHolder.OWNER);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCount(pr, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountByType() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCountByType(device.getType(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountByUser() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCountByUser(TestDataHolder.OWNER, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountByName() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCountByName(TestDataHolder.initialDeviceName, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountByOwnership() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCountByOwnership(EnrolmentInfo.OwnerShip.BYOD.name(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountByStatus() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCountByStatus(Status.ACTIVE.name(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceCountByStatusWithType() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getDeviceCountByStatus(device.getType(), Status.ACTIVE.name(), TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getActiveEnrolment() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(), device.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            EnrolmentInfo results = deviceDAO.getActiveEnrolment(deviceIdentifier, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertEquals(Status.ACTIVE, results.getStatus(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDeviceEnrolledTenants() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Integer> results = deviceDAO.getDeviceEnrolledTenants();
            Assert.assertEquals(1, results.size(), "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void findGeoClusters() throws DeviceManagementDAOException, TransactionManagementException {
        GeoQuery geoQuery = new GeoQuery(new GeoCoordinate(123, 123), new GeoCoordinate(123, 123), 12345);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<GeoCluster> results = deviceDAO.findGeoClusters(geoQuery, TestDataHolder.SUPER_TENANT_ID);
            Assert.assertNotNull(results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getAppNotInstalledDevices() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        PaginationRequest pr = new PaginationRequest(0, 10);
        pr.setDeviceType(device.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getAppNotInstalledDevices(pr, TestDataHolder.SUPER_TENANT_ID, "com.google.calc", "1.0.0");
            Assert.assertNotNull(results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getCountOfAppNotInstalledDevices() throws DeviceManagementDAOException, TransactionManagementException {
        Device device = TestDataHolder.initialTestDevice;
        PaginationRequest pr = new PaginationRequest(0, 10);
        pr.setDeviceType(device.getType());
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getCountOfAppNotInstalledDevices(pr, TestDataHolder.SUPER_TENANT_ID, "com.google.calc", "1.0.0");
            Assert.assertEquals(1, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getDevicesByEncryptionStatus() throws DeviceManagementDAOException, TransactionManagementException {
        PaginationRequest pr = new PaginationRequest(0, 10);
        try {
            DeviceManagementDAOFactory.beginTransaction();
            List<Device> results = deviceDAO.getDevicesByEncryptionStatus(pr, TestDataHolder.SUPER_TENANT_ID, false);
            Assert.assertNotNull(results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    @Test(dependsOnMethods = "testAddDeviceTest")
    public void getCountOfDevicesByEncryptionStatus() throws DeviceManagementDAOException, TransactionManagementException {
        try {
            DeviceManagementDAOFactory.beginTransaction();
            int results = deviceDAO.getCountOfDevicesByEncryptionStatus(TestDataHolder.SUPER_TENANT_ID, true);
            Assert.assertEquals(0, results, "No device returned");
            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the device" + e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

}
