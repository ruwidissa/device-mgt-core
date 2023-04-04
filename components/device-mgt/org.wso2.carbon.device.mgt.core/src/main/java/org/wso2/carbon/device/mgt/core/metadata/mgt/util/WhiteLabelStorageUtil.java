/* Copyright (c) 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.metadata.mgt.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Base64File;
import org.wso2.carbon.device.mgt.common.FileResponse;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.NotFoundException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelImage;
import org.wso2.carbon.device.mgt.core.common.exception.StorageManagementException;
import org.wso2.carbon.device.mgt.core.common.util.FileUtil;
import org.wso2.carbon.device.mgt.core.common.util.StorageManagementUtil;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.metadata.mgt.MetaDataConfiguration;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class contains the default concrete implementation of ApplicationStorage Management.
 */
public class WhiteLabelStorageUtil {
    private static final Log log = LogFactory.getLog(WhiteLabelStorageUtil.class);
    private static final MetaDataConfiguration metadataConfig;
    private static final String STORAGE_PATH;

    static {
        metadataConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getMetaDataConfiguration();
        if (metadataConfig == null) {
            throw new RuntimeException("Meta configuration is not found in cdm-config.xml");
        }
        STORAGE_PATH = metadataConfig.getWhiteLabelConfiguration().getWhiteLabelImages().getStoragePath();
    }

    /**
     * Store provided white label {@link Base64File} image
     *
     * @param image base64 image file
     * @param imageName {@link WhiteLabelImage.ImageName} (i.e FAVICON)
     */
    public static void storeWhiteLabelImage(Base64File image, WhiteLabelImage.ImageName imageName, int tenantId)
            throws MetadataManagementException {
        String storedLocation;
        try {
            String imageStoringBaseDirPath = STORAGE_PATH + File.separator + tenantId;
            StorageManagementUtil.createArtifactDirectory(imageStoringBaseDirPath);
            String storingDir = imageStoringBaseDirPath + File.separator + imageName;
            StorageManagementUtil.createArtifactDirectory(storingDir);
            storedLocation = storingDir + File.separator + image.getName();
            StorageManagementUtil.saveFile(image, storedLocation);
        } catch (IOException e) {
            String msg = "IO Exception occurred while saving whitelabel artifacts for the tenant " + tenantId;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (StorageManagementException e) {
            String msg = "Error occurred while uploading white label image artifacts";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    /**
     * Store provided white label {@link File} image
     *
     * @param image white label file
     * @param imageName {@link WhiteLabelImage.ImageName} (i.e FAVICON)
     */
    public static void storeWhiteLabelImage(File image, WhiteLabelImage.ImageName imageName, int tenantId) throws
            MetadataManagementException {
        try {
            storeWhiteLabelImage(FileUtil.fileToBase64File(image), imageName, tenantId);
        } catch (IOException e) {
            String msg = "Error occurred when converting provided File object to Base64File class";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }


    /**
     * Update white label image for provided tenant
     *
     * @param image {@link Base64File} white label file
     * @param imageName (i.e: FAVICON)
     */
    public static void updateWhiteLabelImage(Base64File image, WhiteLabelImage.ImageName imageName, int tenantId)
            throws MetadataManagementException {
            deleteWhiteLabelImageIfExists(imageName, tenantId);
            storeWhiteLabelImage(image, imageName, tenantId);
    }

    /**
     * Use to get a given {@link WhiteLabelImage.ImageName (i.e: LOGO)} white label image File
     *
     * @param image detail bean
     * @param imageName (i.e: LOGO)
     * @return white label image file {@link File}
     */
    public static File getWhiteLabelImageFile(WhiteLabelImage image, WhiteLabelImage.ImageName imageName, String tenantDomain)
            throws MetadataManagementException, DeviceManagementException {
        String fullPathToImage = getPathToImage(image, imageName, tenantDomain);
        return new File(fullPathToImage);
    }

    /**
     * Useful to get the given {@link WhiteLabelImage.ImageName (i.e: LOGO)} white label image InputStream
     *
     * @param image - white label image detail bean
     * @param imageName (i.e: LOGO)
     * @return white label image input stream
     */
    public static FileResponse getWhiteLabelImageStream(WhiteLabelImage image, WhiteLabelImage.ImageName imageName, String tenantDomain)
            throws MetadataManagementException, NotFoundException, DeviceManagementException {
        FileResponse fileResponse = new FileResponse();
        String fullPathToFile = getPathToImage(image, imageName, tenantDomain);
        try {
            InputStream imageStream = StorageManagementUtil.getInputStream(fullPathToFile);
            if (imageStream == null) {
                String msg = "Failed to get the " + imageName + " image with the file name: " + fullPathToFile;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            byte[] fileContent = IOUtils.toByteArray(imageStream);
            String fileExtension = FileUtil.extractFileExtensionFromFilePath(image.getImageLocation());
            String mimeType = FileResponse.ImageExtension.mimeTypeOf(fileExtension);
            fileResponse.setMimeType(mimeType);
            fileResponse.setFileContent(fileContent);
            return fileResponse;
        } catch (IOException e) {
            String msg = "Error occurred when accessing the file in file path: " + fullPathToFile;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    /**
     * Construct the path to white label image in the file system and return
     *
     * @param image - white label image detail bean
     * @param imageName (i.e: LOGO)
     * @return Full path to white label image in the system
     */
    private static String getPathToImage(WhiteLabelImage image, WhiteLabelImage.ImageName imageName, String tenantDomain)
            throws MetadataManagementException, DeviceManagementException {
        WhiteLabelImage.ImageLocationType imageLocationType = image.getImageLocationType();
        if (imageLocationType == WhiteLabelImage.ImageLocationType.URL) {
            String msg = "White label images of URL type is not stored, hence it doesn't have a path in file system.";
            log.error(msg);
            throw new MetadataManagementException(msg);
        }
        int tenantId = 0;
        try {
            tenantId = DeviceManagerUtil.getTenantId(tenantDomain);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting tenant details of logo";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        String fileName = image.getImageLocation();
        String filePath = String.valueOf(tenantId);
        if (imageLocationType == WhiteLabelImage.ImageLocationType.DEFAULT_FILE) {
            filePath = metadataConfig.getWhiteLabelConfiguration().getWhiteLabelImages().getDefaultImagesLocation();
        }
        return STORAGE_PATH + File.separator + filePath + File.separator + imageName + File.separator + fileName;
    }

    /***
     * This method is responsible to delete provided white label image file which if exist
     */
    public static void deleteWhiteLabelImageIfExists(WhiteLabelImage.ImageName imageName, int tenantId) throws MetadataManagementException {
        String artifactPath = STORAGE_PATH + File.separator + tenantId + File.separator + imageName;
        File artifact = new File(artifactPath);
        if (artifact.exists()) {
            try {
                StorageManagementUtil.delete(artifact);
            } catch (IOException e) {
                String msg = "Error occurred while deleting whitelabel artifacts";
                log.error(msg, e);
                throw new MetadataManagementException(msg, e);
            }
        }
    }

    /***
     * This method is responsible to delete all white label images for provided tenant
     */
    public static void deleteWhiteLabelImageForTenantIfExists(int tenantId) throws MetadataManagementException {
        String artifactPath = STORAGE_PATH + File.separator + tenantId;
        File artifact = new File(artifactPath);
        if (artifact.exists()) {
            try {
                StorageManagementUtil.delete(artifact);
            } catch (IOException e) {
                String msg = "Error occurred while deleting whitelabel artifacts";
                log.error(msg, e);
                throw new MetadataManagementException(msg, e);
            }
        }
    }
}
