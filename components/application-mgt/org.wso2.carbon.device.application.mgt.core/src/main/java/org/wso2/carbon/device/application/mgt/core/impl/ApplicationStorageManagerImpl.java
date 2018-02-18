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

package org.wso2.carbon.device.application.mgt.core.impl;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.ApplicationType;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    public ApplicationRelease uploadImageArtifacts(ApplicationRelease applicationRelease, InputStream iconFileStream,
                                                   InputStream bannerFileStream, List<InputStream> screenShotStreams)
            throws ResourceManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String artifactDirectoryPath;
        String iconStoredLocation;
        String bannerStoredLocation;
        String scStoredLocation;

        try {
            artifactDirectoryPath = storagePath + applicationRelease.getAppHashValue();
            StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
            iconStoredLocation = artifactDirectoryPath + File.separator + Constants.IMAGE_ARTIFACTS[0];
            bannerStoredLocation = artifactDirectoryPath + File.separator + Constants.IMAGE_ARTIFACTS[1];

            if (iconFileStream != null) {
                saveFile(iconFileStream, iconStoredLocation);
                applicationRelease.setIconLoc(iconStoredLocation);
            }
            if (bannerFileStream != null) {
                saveFile(bannerFileStream, bannerStoredLocation);
                applicationRelease.setBannerLoc(bannerStoredLocation);
            }
            if (screenShotStreams.size() > screenShotMaxCount) {
                throw new ApplicationStorageManagementException("Maximum limit for the screen-shot exceeds");
            } else if (!screenShotStreams.isEmpty() && screenShotStreams.size() <= screenShotMaxCount) {
                int count = 1;
                for (InputStream screenshotStream : screenShotStreams) {
                    scStoredLocation = artifactDirectoryPath + File.separator + Constants.IMAGE_ARTIFACTS[2] + count;
                    if (count == 1) {
                        applicationRelease.setScreenshotLoc1(scStoredLocation);
                    }
                    if (count == 2) {
                        applicationRelease.setScreenshotLoc2(scStoredLocation);
                    }
                    if (count == 3) {
                        applicationRelease.setScreenshotLoc3(scStoredLocation);
                    }
                    saveFile(screenshotStream, scStoredLocation);
                    count++;
                }
            }

            return applicationRelease;
        } catch (IOException e) {
            throw new ApplicationStorageManagementException("IO Exception while saving the screens hots for " +
                    "the application " + applicationRelease.getUuid(), e);
        } catch (ApplicationStorageManagementException e) {
            throw new ApplicationStorageManagementException("Application Management DAO exception while trying to "
                    + "update the screen-shot count for the application " + applicationRelease.getUuid() +
                    " for the tenant id " + tenantId, e);
        }
    }

    @Override
    public ApplicationRelease updateImageArtifacts(ApplicationRelease applicationRelease, InputStream iconFileStream,
                                                   InputStream bannerFileStream, List<InputStream> screenShotStreams)
            throws ResourceManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        try {
            if (iconFileStream != null) {
                deleteApplicationReleaseArtifacts(applicationRelease.getIconLoc());
            }
            if (bannerFileStream != null) {
                deleteApplicationReleaseArtifacts(applicationRelease.getBannerLoc());
            }
            if (screenShotStreams.size() > screenShotMaxCount) {
                throw new ApplicationStorageManagementException("Maximum limit for the screen-shot exceeds");
            } else if (!screenShotStreams.isEmpty() && screenShotStreams.size() <= screenShotMaxCount) {
                int count = 1;
                while (count < screenShotStreams.size()) {
                    if (count == 1) {
                        deleteApplicationReleaseArtifacts(applicationRelease.getScreenshotLoc1());
                    }
                    if (count == 2) {
                        deleteApplicationReleaseArtifacts(applicationRelease.getScreenshotLoc2());
                    }
                    if (count == 3) {
                        deleteApplicationReleaseArtifacts(applicationRelease.getScreenshotLoc3());
                    }
                    count++;
                }
            }
            applicationRelease = uploadImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, screenShotStreams);
            return applicationRelease;
        } catch (ApplicationStorageManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationStorageManagementException("Application Storage exception while trying to"
                    + " update the screen-shot count for the application Release " + applicationRelease.getUuid() +
                    " for the tenant " + tenantId, e);
        }
    }

    @Override
    public ApplicationRelease uploadReleaseArtifact(ApplicationRelease applicationRelease, String appType, InputStream binaryFile)
            throws ResourceManagementException {

        String artifactDirectoryPath;
        String md5OfApp;
        md5OfApp = getMD5(binaryFile);

        try {

            if (ApplicationType.ANDROID.toString().equals(appType)){
                String prefix = "stream2file";
                String suffix = ".apk";
                Boolean isTempDelete;

                File tempFile = File.createTempFile(prefix, suffix);
                FileOutputStream out = new FileOutputStream(tempFile);
                IOUtils.copy(binaryFile, out);
                ApkFile apkFile = new ApkFile(tempFile);
                ApkMeta apkMeta = apkFile.getApkMeta();
                applicationRelease.setVersion(apkMeta.getVersionName());
                isTempDelete = tempFile.delete();
                if (!isTempDelete) {
                    log.error("Temporary created APK file deletion failed");
                }
            }else if (ApplicationType.iOS.toString().equals(appType)){
             //todo iOS ipa validate
            }else if (ApplicationType.WEB_CLIP.toString().equals(appType)){
             //todo Web Clip validate
            }else{
                throw new ApplicationStorageManagementException("Application Type doesn't match with supporting " +
                        "application types " + applicationRelease.getUuid());
            }




            if (md5OfApp != null) {
                artifactDirectoryPath = storagePath + md5OfApp;
                StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
                if (log.isDebugEnabled()) {
                    log.debug("Artifact Directory Path for saving the application release related artifacts related with "
                            + "application UUID " + applicationRelease.getUuid() + " is " + artifactDirectoryPath);
                }

                String artifactPath = artifactDirectoryPath + Constants.RELEASE_ARTIFACT;
                saveFile(binaryFile, artifactPath);
                applicationRelease.setAppStoredLoc(artifactPath);
                applicationRelease.setAppHashValue(md5OfApp);
            } else {
                throw new ApplicationStorageManagementException("Error occurred while md5sum value retrieving process: " +
                        "application UUID " + applicationRelease.getUuid());
            }
        } catch (IOException e) {
            throw new ApplicationStorageManagementException(
                    "IO Exception while saving the release artifacts in the server for the application UUID "
                            + applicationRelease.getUuid(), e);
        }


        return applicationRelease;
    }

    @Override
    public ApplicationRelease updateReleaseArtifacts(ApplicationRelease applicationRelease, String appType,
                                                     InputStream binaryFile) throws ApplicationStorageManagementException {

        if (binaryFile != null) {
            try {
                deleteApplicationReleaseArtifacts(applicationRelease.getAppStoredLoc());
                applicationRelease = uploadReleaseArtifact(applicationRelease, appType, binaryFile);
            } catch (ApplicationStorageManagementException e) {
                throw new ApplicationStorageManagementException("Application Artifact doesn't contains in the System", e);
            } catch (ResourceManagementException e) {
                throw new ApplicationStorageManagementException("Application Artifact Updating failed", e);
            }
        }

        return applicationRelease;

    }


    @Override
    public void deleteApplicationReleaseArtifacts(String directoryPath) throws ApplicationStorageManagementException {
        String artifactPath = storagePath + directoryPath;
        File artifact = new File(artifactPath);

        if (artifact.exists()) {
            StorageManagementUtil.deleteDir(artifact);
        } else {
            throw new ApplicationStorageManagementException("Tried to delete application release, but it doesn't exist " +
                    "in the system");
        }
    }

    @Override
    public void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths) throws ApplicationStorageManagementException {
        for (String directoryBasePath : directoryPaths) {
            deleteApplicationReleaseArtifacts(directoryBasePath);
        }
    }

    private String getMD5(InputStream binaryFile) throws ApplicationStorageManagementException {
        String md5;
        try {
            md5 = DigestUtils.md5Hex(IOUtils.toByteArray(binaryFile));
        } catch (IOException e) {
            throw new ApplicationStorageManagementException
                    ("IO Exception while trying to get the md5sum value of application");
        }
        return md5;
    }
}
