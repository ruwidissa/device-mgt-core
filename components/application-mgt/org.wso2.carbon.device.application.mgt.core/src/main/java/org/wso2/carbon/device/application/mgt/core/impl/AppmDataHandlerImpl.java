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

package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.config.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.AppmDataHandler;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

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

    @Override public InputStream getArtifactStream(int tenantId, String uuid, String folderName, String artifactName)
            throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        ApplicationReleaseDAO applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
        String appReleaseHashValue;
        try {
            ConnectionManagerUtil.openDBConnection();
            appReleaseHashValue = applicationReleaseDAO.getReleaseHashValue(uuid, tenantId);
            if (appReleaseHashValue == null) {
                String msg = "Could't find application release for UUID: " + uuid + ". Hence try with valid UUID.";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            InputStream inputStream = applicationStorageManager
                    .getFileStream(appReleaseHashValue, folderName, artifactName, tenantId);
            if (inputStream == null) {
                String msg = "Couldn't find the file in the file system.";
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
            return inputStream;
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred when retrieving application release hash value for given application release "
                    + "UUID: " + uuid;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when getting input stream of the " + artifactName + " file.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public InputStream getAgentStream(int tenantId, String deviceType)
            throws ApplicationManagementException {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            InputStream inputStream = applicationStorageManager
                    .getFileStream(deviceType, tenantId);
            if (inputStream == null) {
                String msg = "Couldn't find the file in the file system for device type: " + deviceType;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            return inputStream;
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when getting input stream of the " + deviceType + " agent.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (RequestValidatingException e) {
            String msg = "Error invalid request received with device type: " + deviceType;
            log.error(msg, e);
            throw new BadRequestException(msg, e);
        }
    }
}
