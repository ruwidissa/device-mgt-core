/*
 *  Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.metadata.mgt;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Base64File;
import org.wso2.carbon.device.mgt.common.FileResponse;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.NotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelImage;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelImageRequestPayload;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelManagementService;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelTheme;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelThemeCreateRequest;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.metadata.mgt.MetaDataConfiguration;
import org.wso2.carbon.device.mgt.core.config.metadata.mgt.whitelabel.WhiteLabelConfiguration;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.util.MetadataConstants;
import org.wso2.carbon.device.mgt.core.metadata.mgt.util.WhiteLabelStorageUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * This class implements the MetadataManagementService.
 */
public class WhiteLabelManagementServiceImpl implements WhiteLabelManagementService {

    private static final Log log = LogFactory.getLog(WhiteLabelManagementServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public WhiteLabelManagementServiceImpl() {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }

    @Override
    public FileResponse getWhiteLabelFavicon() throws MetadataManagementException, NotFoundException {
        try {
            WhiteLabelTheme whiteLabelTheme = getWhiteLabelTheme();
            return getImageFileResponse(whiteLabelTheme.getFaviconImage(), WhiteLabelImage.ImageName.FAVICON);
        } catch (IOException e) {
            String msg = "Error occurred while getting byte content of favicon";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    @Override
    public FileResponse getWhiteLabelLogo() throws MetadataManagementException, NotFoundException {
        try {
            WhiteLabelTheme whiteLabelTheme = getWhiteLabelTheme();
            return getImageFileResponse(whiteLabelTheme.getLogoImage(), WhiteLabelImage.ImageName.LOGO);
        } catch (IOException e) {
            String msg = "Error occurred while getting byte content of logo";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    /**
     * Useful to get white label image file response for provided {@link WhiteLabelImage.ImageName}
     */
    private FileResponse getImageFileResponse(WhiteLabelImage image, WhiteLabelImage.ImageName imageName) throws
            IOException, MetadataManagementException, NotFoundException {
        if (image.getImageLocationType() == WhiteLabelImage.ImageLocationType.URL) {
            return getImageFileResponseFromUrl(image.getImageLocation());
        }
        return WhiteLabelStorageUtil.getWhiteLabelImageStream(image, imageName);
    }

    /**
     * Useful to get white label image file response from provided url
     */
    private FileResponse getImageFileResponseFromUrl(String url) throws IOException, NotFoundException  {
        FileResponse fileResponse = new FileResponse();
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet imageGetRequest = new HttpGet(url);
            HttpResponse response = client.execute(imageGetRequest);
            InputStream imageStream = response.getEntity().getContent();
            if (imageStream == null) {
                String msg = "Failed to retrieve the image from url: " + url;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            byte[] fileContent = IOUtils.toByteArray(imageStream);
            fileResponse.setFileContent(fileContent);
            String mimeType = HttpUtil.getContentType(response);
            fileResponse.setMimeType(mimeType);
            return fileResponse;
        }
    }


    @Override
    public void addDefaultWhiteLabelThemeIfNotExist(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, MetadataConstants.WHITELABEL_META_KEY)) {
                WhiteLabelTheme whiteLabelTheme = getDefaultWhiteLabelTheme();
                Metadata metadata = constructWhiteLabelThemeMetadata(whiteLabelTheme);
                metadataDAO.addMetadata(tenantId, metadata);
                if (log.isDebugEnabled()) {
                    log.debug("White label metadata entry has inserted successfully");
                }
            }
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while inserting default whitelabel metadata entry.";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }

    }

    @Override
    public void resetToDefaultWhiteLabelTheme() throws MetadataManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        WhiteLabelTheme whiteLabelTheme = getDefaultWhiteLabelTheme();
        Metadata metadata = constructWhiteLabelThemeMetadata(whiteLabelTheme);
        DeviceManagementDataHolder.getInstance().getMetadataManagementService().updateMetadata(metadata);
        WhiteLabelStorageUtil.deleteWhiteLabelImageForTenantIfExists(tenantId);
    }

    /**
     * Construct and return default whitelabel detail bean {@link WhiteLabelImage}
     */
    private WhiteLabelTheme getDefaultWhiteLabelTheme() {
        String footerText = getDefaultFooterText();
        String appTitle = getDefaultAppTitle();
        WhiteLabelImage favicon = constructDefaultFaviconImage();
        WhiteLabelImage logo = constructDefaultLogoImage();
        WhiteLabelTheme defaultTheme = new WhiteLabelTheme();
        defaultTheme.setFooterText(footerText);
        defaultTheme.setAppTitle(appTitle);
        defaultTheme.setLogoImage(logo);
        defaultTheme.setFaviconImage(favicon);
        return defaultTheme;
    }

    /**
     * Get default whitelabel label page title from config
     */
    private String getDefaultAppTitle() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        return whiteLabelConfiguration.getAppTitle();
    }

    /**
     * Get default whitelabel label footer from config
     */
    private String getDefaultFooterText() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        return whiteLabelConfiguration.getFooterText();
    }

    /**
     * This is useful to construct and get the default favicon whitelabel image
     *
     * @return {@link WhiteLabelImage}
     */
    private WhiteLabelImage constructDefaultFaviconImage() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        WhiteLabelImage favicon = new WhiteLabelImage();
        favicon.setImageLocation(whiteLabelConfiguration.getWhiteLabelImages().getDefaultFaviconName());
        setDefaultWhiteLabelImageCommonProperties(favicon);
        return favicon;
    }

    /**
     * This is useful to construct and get the default logo whitelabel image
     *
     * @return {@link WhiteLabelImage}
     */
    private WhiteLabelImage constructDefaultLogoImage() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        WhiteLabelImage logo = new WhiteLabelImage();
        logo.setImageLocation(whiteLabelConfiguration.getWhiteLabelImages().getDefaultLogoName());
        setDefaultWhiteLabelImageCommonProperties(logo);
        return logo;
    }

    /**
     * This is useful to set common properties such as DEFAULT_FILE type for {@link WhiteLabelImage.ImageLocationType}
     * for default white label image bean{@link WhiteLabelImage}
     */
    private void setDefaultWhiteLabelImageCommonProperties(WhiteLabelImage image) {
        image.setImageLocationType(WhiteLabelImage.ImageLocationType.DEFAULT_FILE);
    }

    @Override
    public WhiteLabelTheme updateWhiteLabelTheme(WhiteLabelThemeCreateRequest createWhiteLabelTheme)
            throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Metadata : [" + createWhiteLabelTheme.toString() + "]");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        File existingFaviconImage = null;
        File existingLogoImage = null;
        try {
            WhiteLabelTheme theme = getWhiteLabelTheme();
            if (theme.getFaviconImage().getImageLocationType() == WhiteLabelImage.ImageLocationType.CUSTOM_FILE) {
                existingFaviconImage = WhiteLabelStorageUtil.getWhiteLabelImageFile(theme.getFaviconImage(), WhiteLabelImage.ImageName.FAVICON);
            }
            if (theme.getLogoImage().getImageLocationType() == WhiteLabelImage.ImageLocationType.CUSTOM_FILE) {
                existingLogoImage = WhiteLabelStorageUtil.getWhiteLabelImageFile(theme.getLogoImage(), WhiteLabelImage.ImageName.LOGO);
            }
            storeWhiteLabelImageIfRequired(createWhiteLabelTheme.getFavicon(), WhiteLabelImage.ImageName.FAVICON, tenantId);
            storeWhiteLabelImageIfRequired(createWhiteLabelTheme.getLogo(), WhiteLabelImage.ImageName.LOGO, tenantId);
            WhiteLabelTheme whiteLabelTheme = constructWhiteLabelTheme(createWhiteLabelTheme);
            Metadata metadataWhiteLabelTheme = constructWhiteLabelThemeMetadata(whiteLabelTheme);
            try {
                MetadataManagementDAOFactory.beginTransaction();
                metadataDAO.updateMetadata(tenantId, metadataWhiteLabelTheme);
                MetadataManagementDAOFactory.commitTransaction();
                if (log.isDebugEnabled()) {
                    log.debug("Metadata entry created successfully. " + createWhiteLabelTheme);
                }
                return whiteLabelTheme;
            } catch (MetadataManagementDAOException e) {
                MetadataManagementDAOFactory.rollbackTransaction();
                restoreWhiteLabelImages(existingFaviconImage, existingLogoImage, tenantId);
                String msg = "Error occurred while creating the metadata entry. " + createWhiteLabelTheme;
                log.error(msg, e);
                throw new MetadataManagementException(msg, e);
            } catch (TransactionManagementException e) {
                restoreWhiteLabelImages(existingFaviconImage, existingLogoImage, tenantId);
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg, e);
                throw new MetadataManagementException("Error occurred while creating metadata record", e);
            } finally {
                MetadataManagementDAOFactory.closeConnection();
            }
        } catch (NotFoundException e) {
            String msg = "Error occurred while retrieving existing white label theme";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    /**
     * This is method is useful to restore provided existing white label images (i.e: favicon/logo).
     * For example if any exception occurred white updating/deleting white label, this method can be used to
     * restore the existing images in any case. Note that the existing images should be first loaded so that
     * those can be passed to this method in order to restore.
     *
     * @param existingFavicon existing favicon image file
     * @param existingLogo existing logo image file
     */
    private void restoreWhiteLabelImages(File existingFavicon, File existingLogo, int tenantId)
            throws MetadataManagementException {
        WhiteLabelStorageUtil.deleteWhiteLabelImageForTenantIfExists(tenantId);
        if (existingFavicon != null) {
            WhiteLabelStorageUtil.storeWhiteLabelImage(existingFavicon, WhiteLabelImage.ImageName.FAVICON, tenantId);
        }
        if (existingLogo != null) {
            WhiteLabelStorageUtil.storeWhiteLabelImage(existingLogo, WhiteLabelImage.ImageName.LOGO, tenantId);
        }
    }

    /**
     * This handles storing provided white label image if required.
     * For example if the provided white label image is of URL type it doesn't need to be stored
     *
     * @param whiteLabelImage image to be stored
     * @param imageName (i.e: FAVICON)
     */
    private void storeWhiteLabelImageIfRequired(WhiteLabelImageRequestPayload whiteLabelImage,
                                                WhiteLabelImage.ImageName imageName, int tenantId)
            throws MetadataManagementException {
        if (whiteLabelImage.getImageType() == WhiteLabelImageRequestPayload.ImageType.BASE64) {
            Base64File imageBase64 = new Gson().fromJson(whiteLabelImage.getImage(), Base64File.class);
            WhiteLabelStorageUtil.updateWhiteLabelImage(imageBase64, imageName, tenantId);
        }
    }

    /**
     * Generate {@link WhiteLabelTheme} from provided {@link WhiteLabelThemeCreateRequest}
     */
    private WhiteLabelTheme constructWhiteLabelTheme(WhiteLabelThemeCreateRequest whiteLabelThemeCreateRequest) {
        WhiteLabelTheme whiteLabelTheme = new WhiteLabelTheme();
        WhiteLabelImageRequestPayload faviconPayload = whiteLabelThemeCreateRequest.getFavicon();
        WhiteLabelImageRequestPayload logoPayload = whiteLabelThemeCreateRequest.getLogo();
        WhiteLabelImage faviconImage = constructWhiteLabelImageDTO(faviconPayload);
        WhiteLabelImage logoImage = constructWhiteLabelImageDTO(logoPayload);
        whiteLabelTheme.setFaviconImage(faviconImage);
        whiteLabelTheme.setLogoImage(logoImage);
        whiteLabelTheme.setFooterText(whiteLabelThemeCreateRequest.getFooterText());
        whiteLabelTheme.setAppTitle(whiteLabelThemeCreateRequest.getAppTitle());
        return whiteLabelTheme;
    }

    /**
     * Generate {@link WhiteLabelImage} from provided {@link WhiteLabelImageRequestPayload}
     */
    private WhiteLabelImage constructWhiteLabelImageDTO(WhiteLabelImageRequestPayload image) {
        WhiteLabelImage imageResponse = new WhiteLabelImage();
        WhiteLabelImage.ImageLocationType imageLocationType = image.getImageType().getDTOImageLocationType();
        imageResponse.setImageLocationType(imageLocationType);
        String imageLocation;
        if (image.getImageType() == WhiteLabelImageRequestPayload.ImageType.BASE64) {
            Base64File imageBase64 = image.getImageAsBase64File();
            imageLocation = imageBase64.getName();
        } else {
            imageLocation = image.getImageAsUrl();
        }
        imageResponse.setImageLocation(imageLocation);
        return imageResponse;
    }

    /**
     * Generate {@link Metadata} from provided {@link WhiteLabelImage}
     */
    private Metadata constructWhiteLabelThemeMetadata(WhiteLabelTheme whiteLabelTheme) {
        String whiteLabelThemeJsonString = new Gson().toJson(whiteLabelTheme);
        Metadata metadata = new Metadata();
        metadata.setMetaKey(MetadataConstants.WHITELABEL_META_KEY);
        metadata.setMetaValue(whiteLabelThemeJsonString);
        return metadata;
    }

    @Override
    public WhiteLabelTheme getWhiteLabelTheme() throws MetadataManagementException, NotFoundException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving whitelabel theme for tenant: " + tenantId);
        }
        try {
            MetadataManagementDAOFactory.openConnection();
            Metadata metadata =  metadataDAO.getMetadata(tenantId, MetadataConstants.WHITELABEL_META_KEY);
            if (metadata == null) {
                String msg = "Whitelabel theme not found for tenant: " + tenantId;
                log.debug(msg);
                throw new NotFoundException(msg);
            }
            return new Gson().fromJson(metadata.getMetaValue(), WhiteLabelTheme.class);
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving white label theme for tenant:" + tenantId;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }
}
