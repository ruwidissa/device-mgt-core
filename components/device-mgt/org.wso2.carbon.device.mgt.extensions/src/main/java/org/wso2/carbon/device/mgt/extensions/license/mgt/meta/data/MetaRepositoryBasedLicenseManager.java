/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.extensions.license.mgt.meta.data;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.extensions.device.type.template.util.DeviceTypePluginConstants;
import org.wso2.carbon.device.mgt.extensions.internal.DeviceTypeExtensionDataHolder;

public class MetaRepositoryBasedLicenseManager implements LicenseManager {

    private static final Log log = LogFactory.getLog(MetaRepositoryBasedLicenseManager.class);

    @Override
    public License getLicense(String deviceType, String languageCode) throws LicenseManagementException {
        MetadataManagementService metadataManagementService = DeviceTypeExtensionDataHolder.getInstance()
                .getMetadataManagementService();
        String licenceKey = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()
                + DeviceTypePluginConstants.UNDERSCORE + deviceType + DeviceTypePluginConstants.LICENCE_META_KEY_SUFFIX
                + languageCode;

        try {
            Metadata metadata = metadataManagementService.retrieveMetadata(licenceKey);
            if (metadata == null) {
                DeviceManagementProviderService deviceManagementProviderService = DeviceTypeExtensionDataHolder
                        .getInstance().getDeviceManagementProviderService();

                License license = deviceManagementProviderService.getLicenseConfig(deviceType);

                if (license != null && !StringUtils.isBlank(license.getLanguage()) && !StringUtils
                        .isBlank(license.getName()) && !StringUtils.isBlank(license.getText()) && !StringUtils
                        .isBlank(license.getVersion())) {
                    addLicense(deviceType, license);
                    return license;
                } else {
                    license = new License();
                    license.setName(deviceType);
                    license.setVersion("1.0.0");
                    license.setLanguage("en_US");
                    license.setText("This is license text");
                    addLicense(deviceType, license);
                    return license;
                }
            }
            Gson g = new Gson();
            return g.fromJson(metadata.getMetaValue(), License.class);
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while accessing meta data service to store licence data";
            log.error(msg, e);
            throw new LicenseManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting device details.";
            log.error(msg, e);
            throw new LicenseManagementException(msg, e);
        }
    }

    @Override
    public void addLicense(String deviceType, License license) throws LicenseManagementException {

        String languageCode = license.getLanguage();
        if (StringUtils.isBlank(languageCode)) {
            languageCode = DeviceTypePluginConstants.LANGUAGE_CODE_ENGLISH_US;
        }

        String licenceKey = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()
                + DeviceTypePluginConstants.UNDERSCORE + deviceType + DeviceTypePluginConstants.LICENCE_META_KEY_SUFFIX
                + languageCode;

        Metadata metadata = new Metadata();
        metadata.setMetaKey(licenceKey);
        metadata.setMetaValue(new Gson().toJson(license));

        MetadataManagementService metadataManagementService = DeviceTypeExtensionDataHolder.getInstance()
                .getMetadataManagementService();
        try {
            if (metadataManagementService.retrieveMetadata(licenceKey) != null) {
                metadataManagementService.updateMetadata(metadata);
            } else {
                metadataManagementService.createMetadata(metadata);
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while saving the licence value in meta data repository";
            log.error(msg, e);
            throw new LicenseManagementException(msg, e);
        } catch (MetadataKeyAlreadyExistsException e) {
            String msg =
                    "Error occurred while saving the licence key and licence key exist. Licence Key: " + licenceKey;
            log.error(msg, e);
            throw new LicenseManagementException(msg, e);
        }
    }
}
