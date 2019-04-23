/* Copyright (c) 2018, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.ApplicationReleaseArtifactPaths;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.common.services.AppmDataHandler;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationReleaseDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.util.Constants;

import java.io.InputStream;

public class AppmDataHandlerImpl implements AppmDataHandler {

    private UIConfiguration uiConfiguration;

    public AppmDataHandlerImpl(UIConfiguration config) {
        this.uiConfiguration = config;
    }

    @Override
    public UIConfiguration getUIConfiguration() throws ApplicationManagementException {
        return this.uiConfiguration;
    }

    @Override
//    throws ApplicationManagementException
    public InputStream getArtifactStream(String uuid, String artifactName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        ApplicationStorageManager applicationStorageManager = Util.getApplicationStorageManager();
        ApplicationReleaseDAO applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();
        String artifactPath;

        String appReleaseHashValue = null;
        try {
            appReleaseHashValue = applicationReleaseDAO.getReleaseHashValue(uuid, tenantId);
            artifactPath = appReleaseHashValue + Constants.FORWARD_SLASH + artifactName;
            return applicationStorageManager.getFileSttream(artifactPath);


        } catch (ApplicationManagementDAOException e) {
//            todo throw
//            throw new ApplicationManagementException();
//            e.printStackTrace();
        }catch (ApplicationStorageManagementException e) {
            //                todo throw
            //                throw new ApplicationManagementException();
            //                e.printStackTrace();
        }


        return null;
    }
}
