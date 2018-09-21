/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.util;

import org.wso2.carbon.device.application.mgt.common.AppOperation;
import org.wso2.carbon.device.application.mgt.common.EnterpriseApplication;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;

public class AndroidApplicationOperationUtil {
//    public static Operation installApp(AppOperation appOperation) {
//        ProfileOperation operation = new ProfileOperation();
//        operation.setCode(MDMAppConstants.AndroidConstants.OPCODE_INSTALL_APPLICATION);
//        operation.setType(Operation.Type.PROFILE);
//        switch (appOperation.getApplication().getType()) {
//            case "ENTERPRISE"://TODO: fix with ENUM
//                EnterpriseApplication enterpriseApplication = new EnterpriseApplication();
//                enterpriseApplication.setType(appOperation.getApplication().toString());
//                enterpriseApplication.setUrl(application.getLocation());
//                enterpriseApplication.setSchedule(appOperation.getScheduledDateTime());
//                enterpriseApplication.setPackageName(appOperation.getApplication().get);
//                operation.setPayLoad(enterpriseApplication.toJSON());
//                break;
//            case "PUBLIC":
//                setOperationForPublicApp(operation, application);
//                break;
//            case "WEBAPP":
//                setOperationForWebApp(operation, application);
//                break;
//            default:
//                String errorMessage = "Invalid application type.";
//                throw new DeviceApplicationException(errorMessage);
//        }
//        return operation;
//    }

}
