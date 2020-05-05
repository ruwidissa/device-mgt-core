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

package org.wso2.carbon.device.application.mgt.core.impl;

import com.dd.plist.NSDictionary;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.ApplicationInstaller;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.DeviceTypes;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.ParsingException;
import org.wso2.carbon.device.application.mgt.core.util.ArtifactsParser;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil;

import java.io.*;
import java.util.List;

import static org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil.saveFile;

/**
 * This class contains the default concrete implementation of ApplicationStorage Management.
 */
public class ApplicationStorageManagerImpl implements ApplicationStorageManager {
    private static final Log log = LogFactory.getLog(ApplicationStorageManagerImpl.class);
    private String storagePath;
    private int screenShotMaxCount;

    /**
     * Create a new ApplicationStorageManager Instance
     *
     * @param storagePath        Storage Path to save the binary and image files.
     * @param screenShotMaxCount Maximum Screen-shots count
     */
    public ApplicationStorageManagerImpl(String storagePath, String screenShotMaxCount) {
        this.storagePath = storagePath;
        this.screenShotMaxCount = Integer.parseInt(screenShotMaxCount);
    }

    @Override
    public ApplicationReleaseDTO uploadImageArtifacts(ApplicationReleaseDTO applicationReleaseDTO,
            InputStream iconFileStream, InputStream bannerFileStream, List<InputStream> screenShotStreams, int tenantId)
            throws ResourceManagementException {
        String iconStoredLocation;
        String bannerStoredLocation;
        String scStoredLocation = null;

        try {
            String artifactStoringBaseDirPath =
                    storagePath + tenantId + File.separator + applicationReleaseDTO.getAppHashValue();
            StorageManagementUtil.createArtifactDirectory(artifactStoringBaseDirPath);

            if (iconFileStream != null) {
                String iconStoringDir = artifactStoringBaseDirPath + File.separator + Constants.ICON_ARTIFACT;
                StorageManagementUtil.createArtifactDirectory(iconStoringDir);
                iconStoredLocation = iconStoringDir + File.separator + applicationReleaseDTO.getIconName();
                saveFile(iconFileStream, iconStoredLocation);
            }
            if (bannerFileStream != null) {
                String bannerStoringDir = artifactStoringBaseDirPath + File.separator + Constants.BANNER_ARTIFACT;
                StorageManagementUtil.createArtifactDirectory(bannerStoringDir);
                bannerStoredLocation = bannerStoringDir + File.separator + applicationReleaseDTO.getBannerName();
                saveFile(bannerFileStream, bannerStoredLocation);
            }
            if (!screenShotStreams.isEmpty()) {
                if (screenShotStreams.size() > screenShotMaxCount) {
                    String msg = "Maximum limit for the screen-shot exceeds. You can't upload more than three "
                            + "screenshot for an application release";
                    log.error(msg);
                    throw new ApplicationStorageManagementException(msg);
                }
                int count = 1;
                for (InputStream screenshotStream : screenShotStreams) {
                    String scStoringDir = artifactStoringBaseDirPath + File.separator + Constants.SCREENSHOT_ARTIFACT + count;
                    StorageManagementUtil.createArtifactDirectory(scStoringDir);
                    if (count == 1) {
                        scStoredLocation = scStoringDir + File.separator + applicationReleaseDTO.getScreenshotName1();
                    }
                    if (count == 2) {
                        scStoredLocation = scStoringDir + File.separator + applicationReleaseDTO.getScreenshotName2();
                    }
                    if (count == 3) {
                        scStoredLocation = scStoringDir + File.separator + applicationReleaseDTO.getScreenshotName3();
                    }
                    saveFile(screenshotStream, scStoredLocation);
                    count++;
                }
            }
            return applicationReleaseDTO;
        } catch (IOException e) {
            String msg = "IO Exception occurred while saving application artifacts for the application which has UUID "
                    + applicationReleaseDTO.getUuid();
            log.error(msg, e);
            throw new ApplicationStorageManagementException(msg, e);
        }
    }

    @Override
    public ApplicationInstaller getAppInstallerData(InputStream binaryFile, String deviceType)
            throws ApplicationStorageManagementException {
        ApplicationInstaller applicationInstaller = new ApplicationInstaller();
        try {
            if (DeviceTypes.ANDROID.toString().equalsIgnoreCase(deviceType)) {
                ApkMeta apkMeta = ArtifactsParser.readAndroidManifestFile(binaryFile);
                applicationInstaller.setVersion(apkMeta.getVersionName());
                applicationInstaller.setPackageName(apkMeta.getPackageName());
            } else if (DeviceTypes.IOS.toString().equalsIgnoreCase(deviceType)) {
                NSDictionary plistInfo = ArtifactsParser.readiOSManifestFile(binaryFile);
                applicationInstaller
                        .setVersion(plistInfo.objectForKey(ArtifactsParser.IPA_BUNDLE_VERSION_KEY).toString());
                applicationInstaller
                        .setPackageName(plistInfo.objectForKey(ArtifactsParser.IPA_BUNDLE_IDENTIFIER_KEY).toString());
            } else {
                String msg = "Application Type doesn't match with supporting application types " + deviceType;
                log.error(msg);
                throw new ApplicationStorageManagementException(msg);
            }
        } catch (ParsingException e){
            String msg = "Application Type doesn't match with supporting application types " + deviceType;
            log.error(msg, e);
            throw new ApplicationStorageManagementException(msg, e);
        }
        return applicationInstaller;
    }

    @Override
    public void uploadReleaseArtifact(ApplicationReleaseDTO applicationReleaseDTO,
            String deviceType, InputStream binaryFile, int tenantId) throws ResourceManagementException {
        try {
            byte [] content = IOUtils.toByteArray(binaryFile);
            String artifactDirectoryPath =
                    storagePath + tenantId + File.separator + applicationReleaseDTO.getAppHashValue() + File.separator
                            + Constants.APP_ARTIFACT;
            StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
            String artifactPath = artifactDirectoryPath + File.separator + applicationReleaseDTO.getInstallerName();
            saveFile(new ByteArrayInputStream(content), artifactPath);
        } catch (IOException e) {
            String msg = "IO Exception while saving the release artifacts in the server for the application UUID "
                    + applicationReleaseDTO.getUuid();
            log.error(msg, e);
            throw new ResourceManagementException( msg, e);
        }
    }

    @Override
    public void copyImageArtifactsAndDeleteInstaller(String deletingAppHashValue,
            ApplicationReleaseDTO applicationReleaseDTO, int tenantId) throws ApplicationStorageManagementException {

        try {
            String appHashValue = applicationReleaseDTO.getAppHashValue();
            String bannerName = applicationReleaseDTO.getBannerName();
            String iconName = applicationReleaseDTO.getIconName();
            String screenshot1 = applicationReleaseDTO.getScreenshotName1();
            String screenshot2 = applicationReleaseDTO.getScreenshotName2();
            String screenshot3 = applicationReleaseDTO.getScreenshotName3();
            String basePath = storagePath + tenantId + File.separator;

            if (bannerName != null) {
                StorageManagementUtil.copy(basePath + deletingAppHashValue + File.separator + Constants.BANNER_ARTIFACT
                                + File.separator + bannerName,
                        basePath + appHashValue + File.separator + Constants.BANNER_ARTIFACT + File.separator
                                + bannerName);
            }
            if (iconName != null) {
                StorageManagementUtil.copy(basePath + deletingAppHashValue + File.separator + Constants.ICON_ARTIFACT
                                + File.separator + iconName,
                        basePath + appHashValue + File.separator + Constants.ICON_ARTIFACT + File.separator + iconName);
            }
            if (screenshot1 != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + File.separator + Constants.SCREENSHOT_ARTIFACT + 1
                                        + File.separator + screenshot1,
                                basePath + appHashValue + File.separator + Constants.SCREENSHOT_ARTIFACT + 1
                                        + File.separator + screenshot1);
            }
            if (screenshot2 != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + File.separator + Constants.SCREENSHOT_ARTIFACT + 2
                                        + File.separator + screenshot2,
                                basePath + appHashValue + File.separator + Constants.SCREENSHOT_ARTIFACT + 2
                                        + File.separator + screenshot2);
            }
            if (screenshot3 != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + File.separator + Constants.SCREENSHOT_ARTIFACT + 3
                                        + File.separator + screenshot3,
                                basePath + appHashValue + File.separator + Constants.SCREENSHOT_ARTIFACT + 3
                                        + File.separator + screenshot3);
            }
            deleteAppReleaseArtifact( basePath + deletingAppHashValue);
        } catch (IOException e) {
            String msg = "Application installer updating is failed because of I/O issue";
            log.error(msg, e);
            throw new ApplicationStorageManagementException(msg, e);
        }
    }



    @Override
    public void deleteAppReleaseArtifact(String appReleaseHashVal, String folderName, String fileName, int tenantId)
            throws ApplicationStorageManagementException {
        String artifactPath = storagePath + tenantId + File.separator + appReleaseHashVal + File.separator + folderName
                + File.separator + fileName;
        deleteAppReleaseArtifact(artifactPath);
    }

    @Override
    public void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths, int tenantId)
            throws ApplicationStorageManagementException {
        String basePath = storagePath + tenantId + File.separator;
        for (String directoryPath : directoryPaths) {
            deleteAppReleaseArtifact(basePath + directoryPath);
        }
    }

    @Override
    public InputStream getFileStream(String hashVal, String folderName, String fileName, int tenantId)
            throws ApplicationStorageManagementException {
        String filePath =
                storagePath + tenantId + File.separator + hashVal + File.separator + folderName + File.separator
                        + fileName;
        try {
            return StorageManagementUtil.getInputStream(filePath);
        } catch (IOException e) {
            String msg = "Error occured when accessing the file in file path: " + filePath;
            log.error(msg, e);
            throw new ApplicationStorageManagementException(msg, e);
        }
    }

    @Override
    public InputStream getFileStream(String deviceType, String tenantDomain) throws ApplicationStorageManagementException {
        String fileName = Constants.AGENT_FILE_NAMES.get(deviceType);
        String filePath =
                storagePath + File.separator + "agents" + File.separator + deviceType.toLowerCase() + File.separator
                        + tenantDomain + File.separator + fileName;
        try {
            return StorageManagementUtil.getInputStream(filePath);
        } catch (IOException e) {
            String msg = "Error occured when accessing the file in file path: " + filePath;
            log.error(msg, e);
            throw new ApplicationStorageManagementException(msg, e);
        }
    }

    /***
     * This method is responsible to  delete artifact file which is located in the artifact path.
     *
     * @param artifactPath relative path of the artifact file
     * @throws ApplicationStorageManagementException when the file couldn't find in the given artifact path or if an
     * IO error occured while deleting the artifact.
     */
    private void deleteAppReleaseArtifact(String artifactPath) throws ApplicationStorageManagementException {
        File artifact = new File(artifactPath);
        if (artifact.exists()) {
            try {
                StorageManagementUtil.delete(artifact);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "Error occured while deleting application release artifacts", e);
            }
        } else {
            String msg = "Tried to delete application release, but it doesn't exist in the file system";
            log.error(msg);
            throw new ApplicationStorageManagementException(msg);
        }
    }
}
