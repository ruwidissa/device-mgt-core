/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;

import java.io.InputStream;
import java.util.List;

/**
 * This manages all the storage related requirements of ApplicationDTO.
 */
public interface ApplicationStorageManager {
    /**
     * To upload image artifacts related with an ApplicationDTO.
     *
     * @param applicationRelease ApplicationReleaseDTO Object
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationReleaseDTO uploadImageArtifacts(ApplicationReleaseDTO applicationRelease,
            InputStream iconFile, InputStream bannerFile, List<InputStream> screenshots) throws ResourceManagementException;

    /**
     * To upload image artifacts related with an ApplicationDTO.
     *
     * @param applicationRelease Release of the application
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @param screenshots   Input Streams of screenshots
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationReleaseDTO updateImageArtifacts(ApplicationReleaseDTO applicationRelease, InputStream iconFile,
            InputStream bannerFile, List<InputStream> screenshots) throws ResourceManagementException;

    /**
     * To upload release artifacts for an ApplicationDTO.
     *
     * @param applicationRelease ApplicationDTO Release Object.
     * @param appType ApplicationDTO Type.
     * @param deviceType Compatible device tipe of the application.
     * @param binaryFile      Binary File for the release.
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationReleaseDTO uploadReleaseArtifact(ApplicationReleaseDTO applicationRelease, String appType, String deviceType,
            InputStream binaryFile) throws ResourceManagementException;

    /**
     * To upload release artifacts for an ApplicationDTO.
     *
     * @param applicationRelease applicationRelease ApplicationDTO release of a particular application.
     * @param appType   Type of the application.
     * @param deviceType Compatible device tipe of the application.
     * @param binaryFile      Binary File for the release.
     * @throws ApplicationStorageManagementException Resource Management Exception.
     */
    ApplicationReleaseDTO updateReleaseArtifacts(ApplicationReleaseDTO applicationRelease, String appType, String deviceType,
            InputStream binaryFile) throws ApplicationStorageManagementException, RequestValidatingException;

    /**
     * To delete the artifacts related with particular ApplicationDTO Release.
     *
     * @param directoryPath Hash value of the application artifact.
     * @throws ApplicationStorageManagementException Not Found Exception.
     */
    void deleteApplicationReleaseArtifacts(String directoryPath) throws ApplicationStorageManagementException;

    /**
     * To delete all release artifacts related with particular ApplicationDTO Release.
     *
     * @param directoryPaths Hash values of the ApplicationDTO.
     * @throws ApplicationStorageManagementException ApplicationDTO Storage Management Exception
     */
    void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths) throws ApplicationStorageManagementException;

    /***
     * Get the InputStream of the file which is located in filePath
     * @param path file path
     * @return {@link InputStream}
     * @throws ApplicationStorageManagementException throws if an error occurs when accessing the file.
     */
    InputStream getFileSttream (String path) throws ApplicationStorageManagementException;


    }
