/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.ApplicationInstaller;
import io.entgra.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.application.mgt.common.exception.ApplicationStorageManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.exception.ResourceManagementException;

import java.io.InputStream;
import java.util.List;

/**
 * This manages all the storage related requirements of Application.
 */
public interface ApplicationStorageManager {

    /**
     * To upload image artifacts related with an Application.
     *
     * @param applicationRelease Application Release Object
     * @param iconFile        InputStream of the icon file.
     * @param bannerFile      InputStream of the banner file.
     * @return {@link ApplicationReleaseDTO}
     * @throws ResourceManagementException if it finds an empty screenshot array or IOException throws while handling
     * input streams.
     */
    ApplicationReleaseDTO uploadImageArtifacts(ApplicationReleaseDTO applicationRelease, InputStream iconFile,
            InputStream bannerFile, List<InputStream> screenshots, int tenantId) throws ResourceManagementException;

    /**
     * To get App Installer data such as version, package name etc.
     *
     * @param binaryFile Binary file of the application.
     * @param deviceType Compatible device type of the application.
     * @return {@link ApplicationInstaller}
     * @throws ApplicationStorageManagementException if device type is incorrect or error occurred while parsing binary
     * data.
     */
    ApplicationInstaller getAppInstallerData(InputStream binaryFile, String deviceType)
            throws ApplicationStorageManagementException;

    /**
     * To upload release artifacts for an Application.
     *
     * @param applicationRelease Application Release Object.
     * @param deviceType Compatible device type of the application.
     * @param binaryFile      Binary File for the release.
     * @param tenantId  Tenant Id
     * @throws ResourceManagementException if IO Exception occured while saving the release artifacts in the server.
     */
    void uploadReleaseArtifact(ApplicationReleaseDTO applicationRelease, String deviceType, InputStream binaryFile,
            int tenantId) throws ResourceManagementException;

    /**
     * To upload release artifacts for an Application.
     *
     * @param applicationReleaseDTO application Release of a particular application.
     * @param deletingAppHashValue Hash value of the deleting application release.
     * @param tenantId Tenant Id
     * @throws ApplicationStorageManagementException if IO Exception occurs while copying image artifacts and deleting
     * application release installer file.
     */
    void copyImageArtifactsAndDeleteInstaller(String deletingAppHashValue,
            ApplicationReleaseDTO applicationReleaseDTO, int tenantId) throws ApplicationStorageManagementException;

    /**
     * To delete the artifacts related with particular Application Release.
     *
     * @param appReleaseHashVal Hash value of the application release.
     * @param folderName Folder name of the application stored.
     * @param fileName Name of the application release artifact.
     * @throws ApplicationStorageManagementException if artifact doesn't exist.
     */
    void deleteAppReleaseArtifact(String appReleaseHashVal, String folderName, String fileName, int tenantId)
            throws ApplicationStorageManagementException;

    /**
     * To delete all release artifacts related with particular Application Release.
     *
     * @param directoryPaths Hash values of the Application.
     * @param tenantId Tenant Id
     * @throws ApplicationStorageManagementException if artifact doesn't exist or IO exception occurred while deleting
     * application artifact.
     */
    void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths, int tenantId)
            throws ApplicationStorageManagementException;

    /**
     * Get the InputStream of the file which is located in filePath
     *
     * @param hashVal Hash Value of the application release.
     * @return {@link InputStream}
     * @throws ApplicationStorageManagementException throws if an error occurs when accessing the file.
     */
    InputStream getFileStream(String hashVal, String folderName, String fileName, int tenantId)
            throws ApplicationStorageManagementException;

    /**
     * Get the InputStream of the file which is located in filePath
     *
     * @param deviceType device type name
     * @param tenantDomain   tenant domain name
     * @return {@link InputStream}
     * @throws ApplicationStorageManagementException throws if an error occurs when accessing the file.
     */
    InputStream getFileStream(String deviceType, String tenantDomain) throws ApplicationStorageManagementException;
}
