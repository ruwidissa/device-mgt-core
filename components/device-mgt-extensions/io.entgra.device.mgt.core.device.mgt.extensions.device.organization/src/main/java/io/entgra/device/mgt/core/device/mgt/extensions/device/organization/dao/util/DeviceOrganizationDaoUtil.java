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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dao.util;


import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceNode;
import io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto.DeviceOrganization;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class includes the utility methods required by DeviceOrganizationMgt functionalities.
 */
public class DeviceOrganizationDaoUtil {

    private static final Log log = LogFactory.getLog(DeviceOrganizationDaoUtil.class);

    /**
     * Helper method to create a Device Organization object from a ResultSet
     *
     * @param rs The ResultSet containing the organization data.
     * @return A DeviceOrganization object.
     * @throws SQLException If there's an issue reading data from the ResultSet.
     */
    public static DeviceOrganization loadDeviceOrganization(ResultSet rs) throws SQLException {
        DeviceOrganization deviceOrganization = new DeviceOrganization();
        deviceOrganization.setOrganizationId(rs.getInt("ORGANIZATION_ID"));
        deviceOrganization.setTenantID(rs.getInt("TENANT_ID"));
        deviceOrganization.setDeviceId(rs.getInt("DEVICE_ID"));
        if (rs.getInt("PARENT_DEVICE_ID") != 0) {
            deviceOrganization.setParentDeviceId(rs.getInt("PARENT_DEVICE_ID"));
        } else {
            deviceOrganization.setParentDeviceId(null);
        }
        deviceOrganization.setDeviceOrganizationMeta(rs.getString("DEVICE_ORGANIZATION_META"));
        deviceOrganization.setUpdateTime(rs.getDate("LAST_UPDATED_TIMESTAMP"));
        return deviceOrganization;
    }

    public static DeviceOrganization loadDeviceOrganizationWithDeviceDetails(ResultSet rs) throws SQLException {
        DeviceOrganization deviceOrganization = new DeviceOrganization();
        deviceOrganization.setOrganizationId(rs.getInt("ORGANIZATION_ID"));
        deviceOrganization.setTenantID(rs.getInt("TENANT_ID"));
        deviceOrganization.setDeviceId(rs.getInt("DEVICE_ID"));
        if (rs.getInt("PARENT_DEVICE_ID") != 0) {
            deviceOrganization.setParentDeviceId(rs.getInt("PARENT_DEVICE_ID"));
        } else {
            deviceOrganization.setParentDeviceId(null);
        }
        deviceOrganization.setDeviceOrganizationMeta(rs.getString("DEVICE_ORGANIZATION_META"));
        deviceOrganization.setUpdateTime(rs.getDate("LAST_UPDATED_TIMESTAMP"));
        deviceOrganization.setDevice(getDeviceDetails(rs));
        return deviceOrganization;
    }

    /**
     * Helper method to create a DeviceNode object from a ResultSet
     *
     * @param rs The ResultSet containing device data.
     * @return A DeviceNode object.
     * @throws SQLException If there's an issue reading data from the ResultSet.
     */
    public static DeviceNode getDeviceFromResultSet(ResultSet rs) throws SQLException {
        DeviceNode node = new DeviceNode();
        node.setDeviceId(rs.getInt("ID"));
        node.setDevice(getDeviceDetails(rs));
        return node;
    }

    public static Device getDeviceDetails(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("ID"));
        device.setDescription(rs.getString("DESCRIPTION"));
        device.setName(rs.getString("NAME"));
        device.setType(rs.getString("DEVICE_TYPE_NAME"));
        device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
        return device;
    }

}
