/* Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Base64File;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.NotFoundException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelImage;
import org.wso2.carbon.device.mgt.core.common.exception.StorageManagementException;
import org.wso2.carbon.device.mgt.core.common.util.FileUtil;
import org.wso2.carbon.device.mgt.core.common.util.StorageManagementUtil;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.tenant.whitelabel.MetaDataConfiguration;
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

    public static void storeWhiteLabelImage(File file, WhiteLabelImage.ImageName imageName, int tenantId) throws
            MetadataManagementException {
        try {
            storeWhiteLabelImage(FileUtil.fileToBase64File(file), imageName, tenantId);
        } catch (IOException e) {
            String msg = "Error occurred when converting provided File object to Base64File class";
            log.error(msg);
            throw new MetadataManagementException(msg, e);
        }
    }


    public static void updateWhiteLabelImage(Base64File image, WhiteLabelImage.ImageName imageName, int tenantId)
            throws MetadataManagementException {
            deleteWhiteLabelImageIfExists(imageName, tenantId);
            storeWhiteLabelImage(image, imageName, tenantId);
    }

    public static File getWhiteLabelImageFile(WhiteLabelImage image, WhiteLabelImage.ImageName imageName)
            throws MetadataManagementException, NotFoundException {
        String fullPathToImage = getPathToImage(image, imageName);
        return new File(fullPathToImage);
    }

    public static InputStream getWhiteLabelImageStream(WhiteLabelImage image, WhiteLabelImage.ImageName imageName)
            throws MetadataManagementException, NotFoundException {
        String fullPathToFile = getPathToImage(image, imageName);
        try {
            InputStream imageStream = StorageManagementUtil.getInputStream(fullPathToFile);
            if (imageStream == null) {
                String msg = "Failed to get the " + imageName + " image with the file name: " + fullPathToFile;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return imageStream;
        } catch (IOException e) {
            String msg = "Error occurred when accessing the file in file path: " + fullPathToFile;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    private static String getPathToImage(WhiteLabelImage image, WhiteLabelImage.ImageName imageName)
            throws MetadataManagementException {
        WhiteLabelImage.ImageLocationType imageLocationType = image.getImageLocationType();
        if (imageLocationType == WhiteLabelImage.ImageLocationType.URL) {
            String msg = "White label images of URL type is not stored, hence it doesn't have a path in file system.";
            log.error(msg);
            throw new MetadataManagementException(msg);
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String fileName = image.getImageLocation();
        String filePath = String.valueOf(tenantId);
        if (imageLocationType == WhiteLabelImage.ImageLocationType.DEFAULT_FILE) {
            filePath = metadataConfig.getWhiteLabelConfiguration().getWhiteLabelImages().getDefaultImagesLocation();
        }
        return STORAGE_PATH + File.separator + filePath + File.separator + imageName + File.separator + fileName;
    }

    /***
     * This method is responsible to  delete artifact file which is located in the artifact path.
     */
    public static void deleteWhiteLabelImageIfExists(WhiteLabelImage.ImageName imageName, int tenantId) throws MetadataManagementException {
        String artifactPath = STORAGE_PATH + File.separator + tenantId + File.separator + imageName;
        File artifact = new File(artifactPath);
        if (artifact.exists()) {
            try {
                StorageManagementUtil.delete(artifact);
            } catch (IOException e) {
                throw new MetadataManagementException("Error occurred while deleting whitelabel artifacts", e);
            }
        }
    }

    /***
     * This method is responsible to  delete artifact file which is located in the artifact path.
     */
    public static void deleteWhiteLabelImageForTenantIfExists(int tenantId) throws MetadataManagementException {
        String artifactPath = STORAGE_PATH + File.separator + tenantId;
        File artifact = new File(artifactPath);
        if (artifact.exists()) {
            try {
                StorageManagementUtil.delete(artifact);
            } catch (IOException e) {
                throw new MetadataManagementException("Error occurred while deleting whitelabel artifacts", e);
            }
        }
    }
}
