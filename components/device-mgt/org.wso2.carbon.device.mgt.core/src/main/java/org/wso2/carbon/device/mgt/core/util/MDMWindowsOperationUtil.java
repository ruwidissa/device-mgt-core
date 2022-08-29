/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.MDMAppConstants;
import org.wso2.carbon.device.mgt.common.app.mgt.App;
import org.wso2.carbon.device.mgt.common.app.mgt.windows.EnterpriseApplication;
import org.wso2.carbon.device.mgt.common.app.mgt.windows.HostedAppxApplication;
import org.wso2.carbon.device.mgt.common.app.mgt.windows.HostedMSIApplication;
import org.wso2.carbon.device.mgt.common.exceptions.UnknownApplicationTypeException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the all the operations related to Windows.
 */
public class MDMWindowsOperationUtil {

    private static final Log log = LogFactory.getLog(MDMWindowsOperationUtil.class);

    /**
     * This method is used to create Install Application operation.
     *
     * @param application MobileApp application
     * @return operation object
     * @throws UnknownApplicationTypeException
     */
    public static Operation createInstallAppOperation(App application) throws UnknownApplicationTypeException {

        ProfileOperation operation = new ProfileOperation();
        operation.setCode(MDMAppConstants.WindowsConstants.INSTALL_ENTERPRISE_APPLICATION);
        operation.setType(Operation.Type.PROFILE);
        String appType = windowsAppType(application.getName());
        String metaData = application.getMetaData();
        JsonArray metaJsonArray = jsonStringToArray(metaData);

        switch (application.getType()) {
            case ENTERPRISE:
                EnterpriseApplication enterpriseApplication = new EnterpriseApplication();
                if (appType.equalsIgnoreCase(MDMAppConstants.WindowsConstants.APPX)) {
                    HostedAppxApplication hostedAppxApplication = new HostedAppxApplication();
                    List<String> dependencyPackageList = new ArrayList<>();
                    for (int i = 0; i < metaJsonArray.size(); i++) {
                        JsonElement metaElement = metaJsonArray.get(i);
                        JsonObject metaObject = metaElement.getAsJsonObject();

                        if (MDMAppConstants.WindowsConstants.APPX_PACKAGE_URI.equals(metaObject.get("key").getAsString())) {
                            hostedAppxApplication.setPackageUri(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_PACKAGE_FAMILY_NAME.equals(metaObject.get("key").getAsString())) {
                            hostedAppxApplication.setPackageFamilyName(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_DEPENDENCY_PACKAGE_URL.equals(metaObject.get("key").getAsString())
                                && metaObject.has("value")) {
                            dependencyPackageList.add(metaObject.get("value").getAsString().trim());
                            hostedAppxApplication.setDependencyPackageUri(dependencyPackageList);
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_CERTIFICATE_HASH.equals(metaObject.get("key").getAsString())
                                && metaObject.has("value")) {
                            hostedAppxApplication.setCertificateHash(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_ENCODED_CERT_CONTENT.equals(metaObject.get("key").getAsString())
                                && metaObject.has("value")) {
                            hostedAppxApplication.setEncodedCertificate(metaObject.get("value").getAsString().trim());
                        }
                    }
                    enterpriseApplication.setHostedAppxApplication(hostedAppxApplication);

                } else if (appType.equalsIgnoreCase(MDMAppConstants.WindowsConstants.MSI)) {
                    HostedMSIApplication hostedMSIApplication = new HostedMSIApplication();
                    for (int i = 0; i < metaJsonArray.size(); i++) {
                        JsonElement metaElement = metaJsonArray.get(i);
                        JsonObject metaObject = metaElement.getAsJsonObject();
                        if (MDMAppConstants.WindowsConstants.MSI_PRODUCT_ID.equals(metaObject.get("key").getAsString())) {
                            hostedMSIApplication.setProductId(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.MSI_CONTENT_URI.equals(metaObject.get("key").getAsString())) {
                            hostedMSIApplication.setContentUrl(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.MSI_FILE_HASH.equals(metaObject.get("key").getAsString())) {
                            hostedMSIApplication.setFileHash(metaObject.get("value").getAsString().trim());
                        }
                    }
                    enterpriseApplication.setHostedMSIApplication(hostedMSIApplication);
                }
                operation.setPayLoad(enterpriseApplication.toJSON());
                break;
            default:
                String msg = "Application type " + application.getType() + " is not supported";
                log.error(msg);
                throw new UnknownApplicationTypeException(msg);
        }

        return operation;
    }

    /**
     * This method is used to create Uninstall Application operation.
     *
     * @param application MobileApp application
     * @return operation object
     * @throws UnknownApplicationTypeException
     */
    public static Operation createUninstallAppOperation(App application) throws UnknownApplicationTypeException {

        ProfileOperation operation = new ProfileOperation();
        operation.setCode(MDMAppConstants.WindowsConstants.UNINSTALL_ENTERPRISE_APPLICATION);
        operation.setType(Operation.Type.PROFILE);
        String appType = windowsAppType(application.getName());
        String metaData = application.getMetaData();
        JsonArray metaJsonArray = jsonStringToArray(metaData);

        switch (application.getType()) {
            case ENTERPRISE:
                EnterpriseApplication enterpriseApplication = new EnterpriseApplication();
                if (appType.equalsIgnoreCase(MDMAppConstants.WindowsConstants.APPX)) {
                    HostedAppxApplication hostedAppxApplication = new HostedAppxApplication();
                    List<String> dependencyPackageList = new ArrayList<>();
                    for (int i = 0; i < metaJsonArray.size(); i++) {
                        JsonElement metaElement = metaJsonArray.get(i);
                        JsonObject metaObject = metaElement.getAsJsonObject();

                        if (MDMAppConstants.WindowsConstants.APPX_PACKAGE_URI.equals(metaObject.get("key").getAsString())) {
                            hostedAppxApplication.setPackageUri(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_PACKAGE_FAMILY_NAME.equals(metaObject.get("key").getAsString())) {
                            hostedAppxApplication.setPackageFamilyName(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_DEPENDENCY_PACKAGE_URL.equals(metaObject.get("key").getAsString())
                                && metaObject.has("value")) {
                            dependencyPackageList.add(metaObject.get("value").getAsString().trim());
                            hostedAppxApplication.setDependencyPackageUri(dependencyPackageList);
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_CERTIFICATE_HASH.equals(metaObject.get("key").getAsString())
                                && metaObject.has("value")) {
                            hostedAppxApplication.setCertificateHash(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.APPX_ENCODED_CERT_CONTENT.equals(metaObject.get("key").getAsString())
                                && metaObject.has("value")) {
                            hostedAppxApplication.setEncodedCertificate(metaObject.get("value").getAsString().trim());
                        }
                    }
                    enterpriseApplication.setHostedAppxApplication(hostedAppxApplication);

                } else if (appType.equalsIgnoreCase(MDMAppConstants.WindowsConstants.MSI)) {
                    HostedMSIApplication hostedMSIApplication = new HostedMSIApplication();
                    for (int i = 0; i < metaJsonArray.size(); i++) {
                        JsonElement metaElement = metaJsonArray.get(i);
                        JsonObject metaObject = metaElement.getAsJsonObject();
                        if (MDMAppConstants.WindowsConstants.MSI_PRODUCT_ID.equals(metaObject.get("key").getAsString())) {
                            hostedMSIApplication.setProductId(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.MSI_CONTENT_URI.equals(metaObject.get("key").getAsString())) {
                            hostedMSIApplication.setContentUrl(metaObject.get("value").getAsString().trim());
                        }
                        else if (MDMAppConstants.WindowsConstants.MSI_FILE_HASH.equals(metaObject.get("key").getAsString())) {
                            hostedMSIApplication.setFileHash(metaObject.get("value").getAsString().trim());
                        }
                    }
                    enterpriseApplication.setHostedMSIApplication(hostedMSIApplication);
                }
                operation.setPayLoad(enterpriseApplication.toJSON());
                break;
            default:
                String msg = "Application type " + application.getType() + " is not supported";
                log.error(msg);
                throw new UnknownApplicationTypeException(msg);
        }

        return operation;
    }

    /**
     * Method to get the installer file extension type for windows type apps(either appx or msi)
     *
     * @param installerName of the app type
     * @return string extension of the windows app types(either appx or msi)
     */
    public static String windowsAppType(String installerName) {
        String extension = installerName.substring(installerName.lastIndexOf(".") + 1);
        return extension;
    }

    /**
     * Method to convert Json String to Json Array
     *
     * @param metaData metaData string array object containing the windows app type specific parameters
     * @return the metaData Json String as Json Array
     */
    public static JsonArray jsonStringToArray(String metaData) {
        JsonArray metaJsonArray = new JsonParser().parse(metaData).getAsJsonArray();
        return metaJsonArray;
    }


}
