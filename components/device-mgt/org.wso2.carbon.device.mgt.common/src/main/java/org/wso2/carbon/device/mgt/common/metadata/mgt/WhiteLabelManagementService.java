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

package org.wso2.carbon.device.mgt.common.metadata.mgt;

import org.wso2.carbon.device.mgt.common.FileResponse;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.NotFoundException;

/**
 * Defines the contract of WhiteLabelManagementService.
 */
public interface WhiteLabelManagementService {

    /**
     * Use to get byte content of favicon whitelabel image
     * @return byte content of favicon
     * @throws MetadataManagementException if error occurred while retrieving favicon
     * @throws NotFoundException if favicon is not found
     */
    FileResponse getWhiteLabelFavicon() throws
            MetadataManagementException, NotFoundException;

    /**
     * Use to get byte content of logo whitelabel image
     * @return byte content of logo
     * @throws MetadataManagementException if error occurred while retrieving logo
     * @throws NotFoundException if logo is not found
     */
    FileResponse getWhiteLabelLogo() throws
            MetadataManagementException, NotFoundException;

    /**
     * This method is useful to create & persist default white label theme for provided tenant if
     * it doesn't exist already
     * @throws MetadataManagementException if error while adding default white label theme
     */
    void addDefaultWhiteLabelThemeIfNotExist(int tenantId) throws MetadataManagementException;

    /**
     * This method is useful to reset existing white label to default whitelabel
     * @throws MetadataManagementException if error while resetting default white label theme
     */
    void resetToDefaultWhiteLabelTheme() throws MetadataManagementException;

    /**
     * This method is useful to update existing white label theme
     * @throws MetadataManagementException if error while updating existing white label theme
     */
    WhiteLabelTheme updateWhiteLabelTheme(WhiteLabelThemeCreateRequest createWhiteLabelTheme)
            throws MetadataManagementException;

    /**
     * This method is useful to get existing white label theme
     * @throws MetadataManagementException if error while getting existing white label theme
     */
    WhiteLabelTheme getWhiteLabelTheme() throws MetadataManagementException, NotFoundException;
}
