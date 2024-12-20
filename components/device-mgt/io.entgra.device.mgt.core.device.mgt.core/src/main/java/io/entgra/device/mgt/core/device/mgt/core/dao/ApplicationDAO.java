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

    /**
     * This method is used to save application icon information.
     * @param iconPath Icon path of the application
     * @param packageName Package name of the application
     * @param version version of the application
     * @throws DeviceManagementDAOException If any database error occurred
     */
    void saveApplicationIcon(String iconPath, String packageName, String version, int tenantId) throws DeviceManagementDAOException;

    /**
     * This method is used to check the package existence.
     * @param packageName Package name of the application
     * @throws DeviceManagementDAOException If any database error occurred
     */
    int getApplicationPackageCount(String packageName) throws DeviceManagementDAOException;

    /**
     * This method is used to update application icon information.
     * @param iconPath Icon path of the application
     * @param oldPackageName Old package name of the application
     * @param newPackageName New package name of the application
     * @param version Version of the application
     * @throws DeviceManagementDAOException If any database error occurred
     */
    void updateApplicationIcon(String iconPath, String oldPackageName, String newPackageName, String version) throws DeviceManagementDAOException;

    /**
     * This method is used to delete application icon information.
     * @param packageName Package name of the application
     * @throws DeviceManagementDAOException If any database error occurred
     */
    void deleteApplicationIcon(String packageName) throws DeviceManagementDAOException;

    /**
     * This method is used to get the installed application list of a specific device
     * @param deviceId ID of the device
     * @param enrolmentId Enrolment ID of the device
     * @param tenantId tenant ID
     * @throws DeviceManagementDAOException If any database error occurred
     */
    List<Application> getInstalledApplicationListOnDevice(int deviceId, int enrolmentId, int offset, int limit, int tenantId, int isSystemApp)
            throws DeviceManagementDAOException;

    /**
     * This method is used to retrieve the icon info of an installed app in device.
     * @param applicationIdentifier application identifier.
     * @return returns the application icon path.
     * @throws DeviceManagementDAOException
     */
    String getIconPath(String applicationIdentifier) throws DeviceManagementDAOException;

    /**
     * This method is used to get the installed application list of a specific device
     * @param deviceId ID of the device
     * @param enrolmentId Enrolment ID of the device
     * @param tenantId tenant ID
     * @throws DeviceManagementDAOException If any database error occurred
     */
    List<Application> getInstalledApplicationListOnDevice(int deviceId, int enrolmentId, int tenantId)
            throws DeviceManagementDAOException;
}
