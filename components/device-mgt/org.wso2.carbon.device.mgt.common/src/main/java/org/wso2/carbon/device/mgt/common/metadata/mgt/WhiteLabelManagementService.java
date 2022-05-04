/*
 *  Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.NotFoundException;

import java.io.IOException;
import java.util.List;

/**
 * Defines the contract of MetadataManagementService.
 */
public interface WhiteLabelManagementService {

    byte[] getWhiteLabelFavicon() throws
            MetadataManagementException, NotFoundException, IOException;
    byte[] getWhiteLabelLogo() throws
            MetadataManagementException, NotFoundException, IOException;

    void addDefaultWhiteLabelThemeIfNotExist() throws MetadataManagementException;

    void resetToDefaultWhiteLabelTheme() throws MetadataManagementException;

    WhiteLabelTheme updateWhiteLabelTheme(WhiteLabelThemeCreateRequest createWhiteLabelTheme)
            throws MetadataManagementException;

    WhiteLabelTheme getWhiteLabelTheme() throws MetadataManagementException, NotFoundException;
}
