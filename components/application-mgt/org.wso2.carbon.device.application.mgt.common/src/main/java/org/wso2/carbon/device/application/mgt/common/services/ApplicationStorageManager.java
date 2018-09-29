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

import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;

import java.io.InputStream;
import java.util.List;

/**
 * This manages all the storage related requirements of Application.
 */
public interface ApplicationStorageManager {
    /**
     * To upload image artifacts related with an Application.
     *
     * @param applicationRelease ApplicationRelease Object
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationRelease uploadImageArtifacts(ApplicationRelease applicationRelease,
            InputStream iconFile, InputStream bannerFile, List<InputStream> screenshots) throws ResourceManagementException;

    /**
     * To upload image artifacts related with an Application.
     *
     * @param applicationRelease Release of the application
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @param screenshots   Input Streams of screenshots
     * @throws ResourceManagementException Resource Management Exception.
     */
    void updateImageArtifacts(ApplicationRelease applicationRelease, InputStream iconFile,
            InputStream bannerFile, List<InputStream> screenshots)
            throws ResourceManagementException, ApplicationManagementException;

    /**
     * To upload release artifacts for an Application.
     *
     * @param applicationRelease Application Release Object.
     * @param appType Application Type.
     * @param deviceType Compatible device tipe of the application.
     * @param binaryFile      Binary File for the release.
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationRelease uploadReleaseArtifact(ApplicationRelease applicationRelease, String appType, String deviceType,
            InputStream binaryFile) throws ResourceManagementException, RequestValidatingException;

    /**
     * To upload release artifacts for an Application.
     *
     * @param applicationRelease applicationRelease Application release of a particular application.
     * @param appType   Type of the application.
     * @param deviceType Compatible device tipe of the application.
     * @param binaryFile      Binary File for the release.
     * @throws ApplicationStorageManagementException Resource Management Exception.
     */
    ApplicationRelease updateReleaseArtifacts(ApplicationRelease applicationRelease, String appType, String deviceType,
            InputStream binaryFile) throws ApplicationStorageManagementException, RequestValidatingException;

    /**
     * To delete the artifacts related with particular Application Release.
     *
     * @param directoryPath Hash value of the application artifact.
     * @throws ApplicationStorageManagementException Not Found Exception.
     */
    void deleteApplicationReleaseArtifacts(String directoryPath) throws ApplicationStorageManagementException;

    /**
     * To delete all release artifacts related with particular Application Release.
     *
     * @param directoryPaths Hash values of the Application.
     * @throws ApplicationStorageManagementException Application Storage Management Exception
     */
    void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths) throws ApplicationStorageManagementException;

}
