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

import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.Application;

import java.util.List;

public interface ApplicationDAO {

    void addApplications(List<Application> applications, int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException;

    void updateApplications(List<Application> applications, int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException;

    void removeApplications(List<Application> apps, int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException;

    Application getApplication(String identifier, int tenantId) throws DeviceManagementDAOException;

    Application getApplication(String identifier, String version,int tenantId) throws DeviceManagementDAOException;

    Application getApplication(String identifier, String version, int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException;

    List<Application> getInstalledApplications(int deviceId, int enrolmentId, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to get a list of applications installed in all enrolled devices
     *
     * @param request Request object with limit and offset
     * @param tenantId ID of the current tenant
     * @return List of {@link Application} objects
     * @throws DeviceManagementDAOException If any database error occured
     */
    List<Application> getApplications(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException;

    /**
     * This method is used to get a list of app versions when app package name is given.
     *
     * @param tenantId ID of the current tenant
     * @param packageName Package name of the application
     * @return String list of app versions
     * @throws DeviceManagementDAOException If any database error occured
     */
    List<String> getAppVersions(int tenantId, String packageName) throws DeviceManagementDAOException;
}
