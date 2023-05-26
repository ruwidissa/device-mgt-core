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


package io.entgra.device.mgt.core.device.mgt.core.privacy.dao;

import io.entgra.device.mgt.core.device.mgt.core.privacy.impl.DeviceEnrollmentMapping;

import java.util.List;

public interface PrivacyComplianceDAO {

    List<DeviceEnrollmentMapping> getDevicesOfUser(String username, int tenantId) throws PrivacyComplianceDAOException;

    void deleteDevice(int deviceId, int tenantId) throws PrivacyComplianceDAOException;

    void deleteDeviceEnrollments(int deviceId, int tenantId) throws PrivacyComplianceDAOException;

    void deleteDeviceEnrollments(int deviceId, int enrolmentId, int tenantId) throws PrivacyComplianceDAOException;

    void deleteDeviceDetails(int deviceId, int enrolmentId) throws PrivacyComplianceDAOException;

    void deleteDeviceApplications(int deviceId, int enrolmentId, int tenantId) throws PrivacyComplianceDAOException;

    void deleteDeviceProperties(int deviceId, int enrolmentId, int tenantId) throws PrivacyComplianceDAOException;

    void deleteDeviceLocation(int deviceId, int enrolmentId) throws PrivacyComplianceDAOException;

    void updateDeviceOperationResponses(int enrolmentId) throws PrivacyComplianceDAOException;

    void deleteDeviceOperationDetails(int enrolmentId) throws PrivacyComplianceDAOException;

    void deleteOperationEnrolmentMappings(int enrolmentId) throws PrivacyComplianceDAOException;

}
