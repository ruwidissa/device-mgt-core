/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.core.util;

import org.wso2.carbon.device.mgt.common.MDMAppConstants;
import org.wso2.carbon.device.mgt.common.app.mgt.ios.AppStoreApplication;
import org.wso2.carbon.device.mgt.common.app.mgt.ios.EnterpriseApplication;
import org.wso2.carbon.device.mgt.common.app.mgt.ios.RemoveApplication;
import org.wso2.carbon.device.mgt.common.app.mgt.ios.WebClip;
import org.wso2.carbon.device.mgt.common.app.mgt.App;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;

import java.util.Properties;

/**
 * This class contains the all the operations related to IOS.
 */
public class MDMIOSOperationUtil {

    /**
     * This method is used to create Install Authentication operation.
     *
     * @param application MobileApp application
     * @return operation
     */
    public static Operation createInstallAppOperation(App application) {

        ProfileOperation operation = new ProfileOperation();

        switch (application.getType()) {
            case ENTERPRISE:
                EnterpriseApplication enterpriseApplication =
                        new EnterpriseApplication();
                enterpriseApplication.setManifestURL(application.getLocation());
                Properties properties = application.getProperties();
                enterpriseApplication.setPreventBackupOfAppData((Boolean) properties.
                        get(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP));
                enterpriseApplication.setRemoveAppUponMDMProfileRemoval((Boolean) properties.
                        get(MDMAppConstants.IOSConstants.IS_REMOVE_APP));
                operation.setCode(MDMAppConstants.IOSConstants.OPCODE_INSTALL_ENTERPRISE_APPLICATION);
                operation.setPayLoad(enterpriseApplication.toJSON());
                operation.setType(Operation.Type.COMMAND);
                break;
            case PUBLIC:
                AppStoreApplication appStoreApplication =
                        new AppStoreApplication();
                appStoreApplication.setRemoveAppUponMDMProfileRemoval((Boolean) application.getProperties().
                        get(MDMAppConstants.IOSConstants.IS_REMOVE_APP));
                appStoreApplication.setIdentifier(application.getIdentifier());
                appStoreApplication.setPreventBackupOfAppData((Boolean) application.getProperties().
                        get(MDMAppConstants.IOSConstants.IS_PREVENT_BACKUP));
                appStoreApplication.setBundleId(application.getId());
                appStoreApplication.setiTunesStoreID(Integer.parseInt(application.getProperties().
                        get(MDMAppConstants.IOSConstants.I_TUNES_ID).toString()));
                operation.setCode(MDMAppConstants.IOSConstants.OPCODE_INSTALL_STORE_APPLICATION);
                operation.setType(Operation.Type.COMMAND);
                operation.setPayLoad(appStoreApplication.toJSON());
                break;
            case WEB_CLIP:
                WebClip webClip = new WebClip();
                webClip.setIcon(application.getIconImage());
                webClip.setIsRemovable(application.getProperties().
                        getProperty(MDMAppConstants.IOSConstants.IS_REMOVE_APP));
                webClip.setLabel(application.getProperties().
                        getProperty(MDMAppConstants.IOSConstants.LABEL));
                webClip.setURL(application.getProperties().getProperty(MDMAppConstants.IOSConstants.WEB_CLIP_URL));

                operation.setCode(MDMAppConstants.IOSConstants.OPCODE_INSTALL_WEB_APPLICATION);
                operation.setType(Operation.Type.PROFILE);
                operation.setPayLoad(webClip.toJSON());
                break;
        }
        return operation;
    }

    public static Operation createAppUninstallOperation(App application) {

        ProfileOperation operation = new ProfileOperation();
        operation.setCode(MDMAppConstants.IOSConstants.OPCODE_REMOVE_APPLICATION);
        operation.setType(Operation.Type.PROFILE);

        RemoveApplication removeApplication =
                new RemoveApplication();
        removeApplication.setBundleId(application.getIdentifier());
        operation.setPayLoad(removeApplication.toJSON());

        return operation;
    }

}
