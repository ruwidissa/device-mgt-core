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

package io.entgra.application.mgt.core.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.config.LifecycleState;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.ApplicationStorageManagementException;
import io.entgra.application.mgt.common.exception.LifecycleManagementException;
import io.entgra.application.mgt.common.services.ApplicationStorageManager;
import io.entgra.application.mgt.common.services.AppmDataHandler;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.core.internal.DataHolder;
import io.entgra.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.io.InputStream;
import java.util.Map;

public class AppmDataHandlerImpl implements AppmDataHandler {

    private static final Log log = LogFactory.getLog(AppmDataHandlerImpl.class);
    private LifecycleStateManager lifecycleStateManager;

    public AppmDataHandlerImpl() {
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
    }

    @Override
    public Map<String, LifecycleState> getLifecycleConfiguration() throws LifecycleManagementException {
        return lifecycleStateManager.getLifecycleConfig();
    }

    @Override
    public InputStream getArtifactStream(int tenantId, String appHashValue, String folderName, String artifactName)
            throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        validateArtifactDownloadRequest(tenantId, appHashValue, folderName, artifactName);
        try {
            InputStream inputStream = applicationStorageManager
                    .getFileStream(appHashValue, folderName, artifactName, tenantId);
            if (inputStream == null) {
                String msg = "Couldn't find the file in the file system. Tenant Id: " + tenantId + " App Has Value: "
                        + appHashValue + " Folder Name: " + folderName + " Artifact name: " + artifactName;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return inputStream;
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when getting input stream of the " + artifactName + " file.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

    /**
     * Validate the artifact downloading request
     * @param tenantId Tenat Id
     * @param appHashValue Application hash value
     * @param folderName Folder Name
     * @param artifactName Artifact name
     * @throws BadRequestException if there is an invalid data to retrieve application artifact.
     */
    private void validateArtifactDownloadRequest(int tenantId, String appHashValue, String folderName,
            String artifactName) throws BadRequestException {
        if (tenantId != -1234 && tenantId <= 0) {
            String msg = "Found invalid tenant Id to get application artifact. Tenant Id: " + tenantId;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (StringUtils.isBlank(appHashValue)) {
            String msg = "Found invalid application has value to get application artifact. Application hash value: "
                    + appHashValue;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (StringUtils.isBlank(folderName)) {
            String msg = "Found invalid folder name to get application artifact. Folder name: " + folderName;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (StringUtils.isBlank(artifactName)) {
            String msg = "Found invalid artifact name to get application artifact. Artifact name: " + artifactName;
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    @Override
    public InputStream getAgentStream(String tenantDomain, String deviceType) throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            DeviceType deviceTypeObj = DataHolder.getInstance().getDeviceManagementService().getDeviceType(deviceType);
            if (deviceTypeObj == null) {
                String msg = "Couldn't find a registered device type called " + deviceType + " in the system.";
                log.error(msg);
                throw new NotFoundException(msg);
            }

            InputStream inputStream = applicationStorageManager.getFileStream(deviceType, tenantDomain);
            if (inputStream == null) {
                String msg = "Couldn't find the device type agent in the server. Device type: " + deviceType
                        + " Tenant Domain: " + tenantDomain;
                log.error(msg);
                throw new BadRequestException(msg);
            }
            return inputStream;
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when getting input stream of the " + deviceType + " agent.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = " Error occurred when getting device type details. Device type " + deviceType;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }
}
