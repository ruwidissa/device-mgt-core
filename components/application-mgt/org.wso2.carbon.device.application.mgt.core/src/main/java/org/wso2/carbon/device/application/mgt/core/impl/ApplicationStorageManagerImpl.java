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

import com.dd.plist.NSDictionary;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
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
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;
import org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil;
import org.apache.commons.validator.routines.UrlValidator;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import static org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil.deleteDir;
import static org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil.saveFile;

/**
 * This class contains the default concrete implementation of ApplicationStorage Management.
 */
public class ApplicationStorageManagerImpl implements ApplicationStorageManager {
    private static final Log log = LogFactory.getLog(ApplicationStorageManagerImpl.class);
    private String storagePath;
    private int screenShotMaxCount;
    private static final int BUFFER_SIZE = 4096;

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
            if (!screenShotStreams.isEmpty()) {
                if (screenShotStreams.size() > screenShotMaxCount) {
                    throw new ApplicationStorageManagementException("Maximum limit for the screen-shot exceeds");
                }
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
            if (!screenShotStreams.isEmpty()) {
                if (screenShotStreams.size() > screenShotMaxCount) {
                    throw new ApplicationStorageManagementException("Maximum limit for the screen-shot exceeds");
                }
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
            return uploadImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, screenShotStreams);
        } catch (ApplicationStorageManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new ApplicationStorageManagementException("Application Storage exception while trying to"
                    + " update the screen-shot count for the application Release " + applicationRelease.getUuid() +
                    " for the tenant " + tenantId, e);
        }
    }

    @Override
    public ApplicationRelease uploadReleaseArtifact(ApplicationRelease applicationRelease, String appType,
            String deviceType, InputStream binaryFile) throws ResourceManagementException, RequestValidatingException {

        try {
            if (ApplicationType.WEB_CLIP.toString().equals(appType)) {
                applicationRelease.setVersion(Constants.DEFAULT_VERSION);
                UrlValidator urlValidator = new UrlValidator();
                if (applicationRelease.getUrl() == null || !urlValidator.isValid(applicationRelease.getUrl())) {
                    throw new RequestValidatingException("Request payload doesn't contains Web Clip URL " +
                                                                            "with application release object or Web " +
                                                                            "Clip URL is invalid");
                }
                applicationRelease.setAppStoredLoc(applicationRelease.getUrl());
                applicationRelease.setAppHashValue(null);
                return applicationRelease;
            }

            String artifactDirectoryPath;
            String md5OfApp;
            md5OfApp = getMD5(binaryFile);

            if (md5OfApp == null) {
                throw new ApplicationStorageManagementException(
                        "Error occurred while md5sum value retrieving process: " +
                                "application UUID " + applicationRelease.getUuid());
            }

            if (ApplicationType.ANDROID.toString().equals(deviceType)) {
                String prefix = "stream2file";
                String suffix = ".apk";
                File tempFile = File.createTempFile(prefix, suffix);
                FileOutputStream out = new FileOutputStream(tempFile);
                IOUtils.copy(binaryFile, out);
                try (ApkFile apkFile = new ApkFile(tempFile)){
                    ApkMeta apkMeta = apkFile.getApkMeta();
                    applicationRelease.setVersion(apkMeta.getVersionName());
                    Files.delete(tempFile.toPath());
                }

            } else if (ApplicationType.IOS.toString().equals(deviceType)) {
                String prefix = "stream2file";
                String suffix = ".ipa";

                File tempFile = File.createTempFile(prefix, suffix);
                FileOutputStream out = new FileOutputStream(tempFile);
                IOUtils.copy(binaryFile, out);
                Map<String, String> plistInfo = getIPAInfo(tempFile);
                applicationRelease.setVersion(plistInfo.get("CFBundleVersion"));
                Files.delete(tempFile.toPath());
            } else {
                throw new ApplicationStorageManagementException("Application Type doesn't match with supporting " +
                        "application types " + applicationRelease.getUuid());
            }

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

        } catch (IOException e) {
            throw new ApplicationStorageManagementException(
                    "IO Exception while saving the release artifacts in the server for the application UUID "
                            + applicationRelease.getUuid(), e);
        }

        return applicationRelease;
    }

    @Override
    public ApplicationRelease updateReleaseArtifacts(ApplicationRelease applicationRelease, String appType,
            String deviceType, InputStream binaryFile) throws ApplicationStorageManagementException,
            RequestValidatingException {

        try {
            deleteApplicationReleaseArtifacts(applicationRelease.getAppStoredLoc());
            applicationRelease = uploadReleaseArtifact(applicationRelease, appType, deviceType, binaryFile);
        } catch (ApplicationStorageManagementException e) {
            throw new ApplicationStorageManagementException("Application Artifact doesn't contains in the System", e);
        } catch (ResourceManagementException e) {
            throw new ApplicationStorageManagementException("Application Artifact Updating failed", e);
        }

        return applicationRelease;

    }


    @Override
    public void deleteApplicationReleaseArtifacts(String directoryPath) throws ApplicationStorageManagementException {
        String artifactPath = storagePath + directoryPath;
        File artifact = new File(artifactPath);

        if (artifact.exists()) {
            try {
                StorageManagementUtil.deleteDir(artifact);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "Error occured while deleting application release artifacts", e);
            }
        } else {
            throw new ApplicationStorageManagementException(
                    "Tried to delete application release, but it doesn't exist in the system");
        }
    }

    @Override public void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths)
            throws ApplicationStorageManagementException {
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

    private synchronized Map<String, String> getIPAInfo(File ipaFile) throws ApplicationStorageManagementException {
        Map<String, String> ipaInfo = new HashMap<>();

        String ipaDirectory = null;
        try {
            String ipaAbsPath = ipaFile.getAbsolutePath();
            ipaDirectory = new File(ipaAbsPath).getParent();

            if (new File(ipaDirectory + File.separator + Constants.PAYLOAD).exists()) {
                deleteDir(new File(ipaDirectory + File.separator + Constants.PAYLOAD));
            }

            // unzip ipa zip file
            unzip(ipaAbsPath, ipaDirectory);

            // fetch app file name, after unzip ipa
            String appFileName = "";
            for (File file : Objects.requireNonNull(
                    new File(ipaDirectory + File.separator + Constants.PAYLOAD).listFiles()
            )) {
                if (file.toString().endsWith(Constants.APP_EXTENSION)) {
                    appFileName = new File(file.toString()).getAbsolutePath();
                    break;
                }
            }

            String plistFilePath = appFileName + File.separator + Constants.PLIST_NAME;

            // parse info.plist
            File plistFile = new File(plistFilePath);
            NSDictionary rootDict;
            rootDict = (NSDictionary) PropertyListParser.parse(plistFile);

            // get version
            NSString parameter = (NSString) rootDict.objectForKey(Constants.CF_BUNDLE_VERSION);
            ipaInfo.put(Constants.CF_BUNDLE_VERSION, parameter.toString());

            if (ipaDirectory != null) {
                // remove unzip folder
                deleteDir(new File(ipaDirectory + File.separator + Constants.PAYLOAD));
            }

        } catch (ParseException e) {
            String msg = "Error occurred while parsing the plist data";
            log.error(msg);
            throw new ApplicationStorageManagementException(msg, e);
        } catch (IOException e) {
            String msg = "Error occurred while accessing the ipa file";
            log.error(msg);
            throw new ApplicationStorageManagementException(msg, e);
        } catch (SAXException | ParserConfigurationException | PropertyListFormatException e) {
            log.error(e);
            throw new ApplicationStorageManagementException(e.getMessage(), e);
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred while unzipping the ipa file";
            log.error(msg);
            throw new ApplicationStorageManagementException(msg, e);
        }
        return ipaInfo;
    }

    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @param zipFilePath   file path of the zip
     * @param destDirectory destination directory path
     */
    private void unzip(String zipFilePath, String destDirectory)
            throws IOException, ApplicationStorageManagementException {
        File destDir = new File(destDirectory);
        boolean isDirCreated;

        if (!destDir.exists()) {
            isDirCreated = destDir.mkdir();
            if (!isDirCreated) {
                throw new ApplicationStorageManagementException("Directory Creation Is Failed while iOS app vertion " +
                                                                        "retrieval");
            }
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {

            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();

                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    isDirCreated = dir.mkdir();
                    if (!isDirCreated) {
                        throw new ApplicationStorageManagementException(
                                "Directory Creation Is Failed while iOS app vertion " + "retrieval");
                    }

                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn    zip input stream
     * @param filePath file path
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}
