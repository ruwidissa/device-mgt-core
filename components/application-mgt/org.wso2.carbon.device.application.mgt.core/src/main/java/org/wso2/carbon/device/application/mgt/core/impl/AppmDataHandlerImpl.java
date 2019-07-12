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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.config.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.AppmDataHandler;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

import java.io.InputStream;
import java.util.Map;

public class AppmDataHandlerImpl implements AppmDataHandler {

    private static final Log log = LogFactory.getLog(AppmDataHandlerImpl.class);
    private UIConfiguration uiConfiguration;
    private LifecycleStateManager lifecycleStateManager;



    public AppmDataHandlerImpl(UIConfiguration config) {
        this.uiConfiguration = config;
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();

    }

    @Override
    public UIConfiguration getUIConfiguration() {
        return this.uiConfiguration;
    }

    @Override
    public Map<String, LifecycleState> getLifecycleConfiguration() throws LifecycleManagementException {
        return lifecycleStateManager.getLifecycleConfig();
    }

    @Override
    public InputStream getArtifactStream(String uuid, String artifactName) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = DAOUtil.getApplicationStorageManager();
        ApplicationReleaseDAO applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
        String artifactPath;
        String appReleaseHashValue;
        try {
            ConnectionManagerUtil.openDBConnection();
            appReleaseHashValue = applicationReleaseDAO.getReleaseHashValue(uuid, tenantId);
            if (appReleaseHashValue == null) {
                String msg = "Could't find application release for UUID: " + uuid + ". Hence try with valid UUID.";
                log.error(msg);
                throw new NotFoundException(msg);
            }
            artifactPath = appReleaseHashValue + Constants.FORWARD_SLASH + artifactName;
            InputStream inputStream = applicationStorageManager.getFileStream(artifactPath);
            if (inputStream == null) {
                String msg = "Couldn't file the file in the file system. File path: " + artifactPath;
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
            return inputStream;
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "Error occurred when retrieving application release hash value for given application release UUID: "
                            + uuid;
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred when getting input stream of the " + artifactName + " file.";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

    }
}
