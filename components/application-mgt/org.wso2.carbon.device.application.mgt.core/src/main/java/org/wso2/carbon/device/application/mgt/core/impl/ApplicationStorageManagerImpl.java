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
import net.dongliu.apk.parser.bean.ApkMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.wso2.carbon.device.application.mgt.core.util.StorageManagementUtil.delete;
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
    public ApplicationReleaseDTO uploadImageArtifacts(ApplicationReleaseDTO applicationRelease, InputStream iconFileStream,
                                                   InputStream bannerFileStream, List<InputStream> screenShotStreams)
            throws ResourceManagementException {
        String artifactDirectoryPath;
        String iconStoredLocation;
        String bannerStoredLocation;
        String scStoredLocation = null;

        try {
            artifactDirectoryPath = storagePath + applicationRelease.getAppHashValue();
            StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
            iconStoredLocation = artifactDirectoryPath + File.separator + applicationRelease.getIconName();
            bannerStoredLocation = artifactDirectoryPath + File.separator + applicationRelease.getBannerName();

            if (iconFileStream != null) {
                saveFile(iconFileStream, iconStoredLocation);
            }
            if (bannerFileStream != null) {
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
                    if (count == 1) {
                        scStoredLocation = artifactDirectoryPath + File.separator + applicationRelease.getScreenshotName1();
                    }
                    if (count == 2) {
                        scStoredLocation = artifactDirectoryPath + File.separator + applicationRelease.getScreenshotName2();
                    }
                    if (count == 3) {
                        scStoredLocation = artifactDirectoryPath + File.separator + applicationRelease.getScreenshotName3();
                    }
                    saveFile(screenshotStream, scStoredLocation);
                    count++;
                }
            }
            return applicationRelease;
        } catch (IOException e) {
            throw new ApplicationStorageManagementException("IO Exception while saving the screens hots for " +
                    "the application " + applicationRelease.getUuid(), e);
        }
    }


    @Override
     public void deleteImageArtifacts(ApplicationReleaseDTO applicationReleaseDTO)
            throws ResourceManagementException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);

        try {
            String iconName = applicationReleaseDTO.getIconName();
            String bannerName = applicationReleaseDTO.getBannerName();
            String sc1 = applicationReleaseDTO.getScreenshotName1();
            String sc2 = applicationReleaseDTO.getScreenshotName2();
            String sc3 = applicationReleaseDTO.getScreenshotName3();
            String hashValue = applicationReleaseDTO.getAppHashValue();
            if (iconName != null) {
                deleteApplicationReleaseArtifacts(
                        storagePath + Constants.FORWARD_SLASH + hashValue + Constants.FORWARD_SLASH + iconName);
            }
            if (bannerName != null) {
                deleteApplicationReleaseArtifacts(
                        storagePath + Constants.FORWARD_SLASH + hashValue + Constants.FORWARD_SLASH + bannerName);
            }

            if (sc1 != null) {
                deleteApplicationReleaseArtifacts(
                        storagePath + Constants.FORWARD_SLASH + hashValue + Constants.FORWARD_SLASH + sc1);
            }
            if (sc2 != null) {
                deleteApplicationReleaseArtifacts(
                        storagePath + Constants.FORWARD_SLASH + hashValue + Constants.FORWARD_SLASH + sc2);
            }
            if (sc3 != null) {
                deleteApplicationReleaseArtifacts(
                        storagePath + Constants.FORWARD_SLASH + hashValue + Constants.FORWARD_SLASH + sc3);
            }
        } catch (ApplicationStorageManagementException e) {
            throw new ApplicationStorageManagementException("ApplicationDTO Storage exception while trying to"
                    + " update the screen-shot count for the application Release " + applicationReleaseDTO.getUuid() +
                    " for the tenant " + tenantId, e);
        }
    }

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
            log.error(msg);
            throw new ApplicationStorageManagementException(msg);
        }
        return applicationInstaller;
    }

    @Override
    public ApplicationReleaseDTO uploadReleaseArtifact(ApplicationReleaseDTO applicationReleaseDTO, String appType,
            String deviceType, InputStream binaryFile) throws ResourceManagementException {
        try {
            String artifactDirectoryPath;
            String artifactPath;
            byte [] content = IOUtils.toByteArray(binaryFile);

            artifactDirectoryPath = storagePath + applicationReleaseDTO.getAppHashValue();
            StorageManagementUtil.createArtifactDirectory(artifactDirectoryPath);
            artifactPath = artifactDirectoryPath + File.separator + applicationReleaseDTO.getInstallerName();
            saveFile(new ByteArrayInputStream(content), artifactPath);
        } catch (IOException e) {
            String msg = "IO Exception while saving the release artifacts in the server for the application UUID "
                    + applicationReleaseDTO.getUuid();
            log.error(msg);
            throw new ApplicationStorageManagementException( msg, e);
        }
        return applicationReleaseDTO;
    }

    @Override
    public void copyImageArtifactsAndDeleteInstaller(String deletingAppHashValue,
            ApplicationReleaseDTO applicationReleaseDTO) throws ApplicationStorageManagementException {

        try {
            String basePath = storagePath + Constants.FORWARD_SLASH;
            String appHashValue = applicationReleaseDTO.getAppHashValue();
            String bannerName = applicationReleaseDTO.getBannerName();
            String iconName = applicationReleaseDTO.getIconName();
            String screenshot1 = applicationReleaseDTO.getScreenshotName1();
            String screenshot2 = applicationReleaseDTO.getScreenshotName2();
            String screenshot3 = applicationReleaseDTO.getScreenshotName3();

            if (bannerName != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + bannerName, basePath + appHashValue + bannerName);
            }
            if (iconName != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + iconName, basePath + appHashValue + iconName);
            }
            if (screenshot1 != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + screenshot1, basePath + appHashValue + screenshot1);
            }
            if (screenshot2 != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + screenshot2, basePath + appHashValue + screenshot2);
            }
            if (screenshot3 != null) {
                StorageManagementUtil
                        .copy(basePath + deletingAppHashValue + screenshot3, basePath + appHashValue + screenshot3);
            }
            deleteApplicationReleaseArtifacts( basePath + deletingAppHashValue);
        } catch (IOException e) {
            String msg = "Application installer updating is failed because of I/O issue";
            log.error(msg);
            throw new ApplicationStorageManagementException(msg, e);
        }
    }

    @Override
    public void deleteApplicationReleaseArtifacts(String artifactPath) throws ApplicationStorageManagementException {
        File artifact = new File(artifactPath);

        if (artifact.exists()) {
            try {
                StorageManagementUtil.delete(artifact);
            } catch (IOException e) {
                throw new ApplicationStorageManagementException(
                        "Error occured while deleting application release artifacts", e);
            }
        } else {
            throw new ApplicationStorageManagementException(
                    "Tried to delete application release, but it doesn't exist in the system");
        }
    }

    @Override
    public void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths)
            throws ApplicationStorageManagementException {
        for (String directoryBasePath : directoryPaths) {
            deleteApplicationReleaseArtifacts(storagePath + directoryBasePath);
        }
    }

    public InputStream getFileSttream (String path) throws ApplicationStorageManagementException {
        String filePath = storagePath + path;
        try {
            return StorageManagementUtil.getInputStream(filePath);
        } catch (IOException e) {
            String msg = "Error occured when accessing the file in file path: " + filePath;
            throw new ApplicationStorageManagementException(msg, e);
        }
    }

    private synchronized Map<String, String> getIPAInfo(File ipaFile) throws ApplicationStorageManagementException {
        Map<String, String> ipaInfo = new HashMap<>();

        String ipaDirectory = null;
        try {
            String ipaAbsPath = ipaFile.getAbsolutePath();
            ipaDirectory = new File(ipaAbsPath).getParent();

            if (new File(ipaDirectory + File.separator + Constants.PAYLOAD).exists()) {
                delete(new File(ipaDirectory + File.separator + Constants.PAYLOAD));
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
                delete(new File(ipaDirectory + File.separator + Constants.PAYLOAD));
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
