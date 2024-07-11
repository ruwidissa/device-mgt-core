/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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


package io.entgra.device.mgt.core.device.mgt.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.MDMAppConstants;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.App;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.windows.AppStoreApplication;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.windows.EnterpriseApplication;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.windows.HostedAppxApplication;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.windows.HostedMSIApplication;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.windows.WebClipApplication;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UnknownApplicationTypeException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.ProfileOperation;

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
        operation.setType(Operation.Type.PROFILE);
        String appType = windowsAppType(application.getName());
        String metaData = application.getMetaData();
        JsonArray metaJsonArray = jsonStringToArray(metaData);

        switch (application.getType()) {
            case ENTERPRISE:
                EnterpriseApplication enterpriseApplication = new EnterpriseApplication();
                createEnterpriseAppPayload(appType, metaJsonArray, enterpriseApplication);
                operation.setCode(MDMAppConstants.WindowsConstants.INSTALL_ENTERPRISE_APPLICATION);
                operation.setPayLoad(enterpriseApplication.toJSON());
                break;
            case PUBLIC:
                AppStoreApplication appStoreApplication = new AppStoreApplication();
                appStoreApplication.setType(application.getType().toString());
                appStoreApplication.setPackageIdentifier(application.getIdentifier());
                operation.setCode(MDMAppConstants.WindowsConstants.INSTALL_STORE_APPLICATION);
                operation.setPayLoad(appStoreApplication.toJSON());
                break;
            case WEB_CLIP:
                WebClipApplication webClipApplication = new WebClipApplication();
                webClipApplication.setUrl(application.getLocation());
                webClipApplication.setName(application.getName());
                webClipApplication.setIcon(application.getIconImage());
                webClipApplication.setProperties(application.getProperties());
                webClipApplication.setType(application.getType().toString());
                operation.setCode(MDMAppConstants.WindowsConstants.INSTALL_WEB_CLIP_APPLICATION);
                operation.setPayLoad(webClipApplication.toJSON());
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
        operation.setType(Operation.Type.PROFILE);
        String appType = windowsAppType(application.getName());
        String metaData = application.getMetaData();
        JsonArray metaJsonArray = jsonStringToArray(metaData);

        switch (application.getType()) {
            case ENTERPRISE:
                EnterpriseApplication enterpriseApplication = new EnterpriseApplication();
                createEnterpriseAppPayload(appType, metaJsonArray, enterpriseApplication);
                operation.setCode(MDMAppConstants.WindowsConstants.UNINSTALL_ENTERPRISE_APPLICATION);
                operation.setPayLoad(enterpriseApplication.toJSON());
                break;
            case PUBLIC:
                AppStoreApplication appStoreApplication = new AppStoreApplication();
                appStoreApplication.setType(application.getType().toString());
                appStoreApplication.setPackageIdentifier(application.getIdentifier());
                operation.setCode(MDMAppConstants.WindowsConstants.UNINSTALL_STORE_APPLICATION);
                operation.setPayLoad(appStoreApplication.toJSON());
                break;
            case WEB_CLIP:
                WebClipApplication webClipApplication = new WebClipApplication();
                webClipApplication.setUrl(application.getLocation());
                webClipApplication.setName(application.getName());
                webClipApplication.setIcon(application.getIconImage());
                webClipApplication.setProperties(application.getProperties());
                webClipApplication.setType(application.getType().toString());
                operation.setCode(MDMAppConstants.WindowsConstants.UNINSTALL_WEB_CLIP_APPLICATION);
                operation.setPayLoad(webClipApplication.toJSON());
            default:
                String msg = "Application type " + application.getType() + " is not supported";
                log.error(msg);
                throw new UnknownApplicationTypeException(msg);
        }

        return operation;
    }

    /**
     * Helper method to create enterprise APPX and MSI app payloads for both install and uninstall operations
     * @param appType contains whether the app type is APPX or MSI
     * @param metaJsonArray JSON array containing metadata of APPX and MSI apps
     * @param enterpriseApplication {@link EnterpriseApplication} contains operation payload content that will be sent to the device
     */
    private static void createEnterpriseAppPayload(String appType, JsonArray metaJsonArray, EnterpriseApplication enterpriseApplication) {

        JsonElement metaElement;
        JsonObject metaObject;
        if (MDMAppConstants.WindowsConstants.APPX.equalsIgnoreCase(appType)) {
            HostedAppxApplication hostedAppxApplication = new HostedAppxApplication();
            List<String> dependencyPackageList = new ArrayList<>();

            for (int i = 0; i < metaJsonArray.size(); i++) {
                metaElement = metaJsonArray.get(i);
                metaObject = metaElement.getAsJsonObject();

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

        } else if (MDMAppConstants.WindowsConstants.MSI.equalsIgnoreCase(appType)) {
            HostedMSIApplication hostedMSIApplication = new HostedMSIApplication();

            for (int i = 0; i < metaJsonArray.size(); i++) {
                metaElement = metaJsonArray.get(i);
                metaObject = metaElement.getAsJsonObject();

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
    }

    /**
     * Method to get the installer file extension type for windows type apps(either appx or msi)
     *
     * @param installerName of the app type
     * @return string extension of the windows app types(either appx or msi)
     */
    public static String windowsAppType(String installerName) {
        return installerName.substring(installerName.lastIndexOf(".") + 1);
    }

    /**
     * Method to convert Json String to Json Array
     *
     * @param metaData metaData string array object containing the windows app type specific parameters
     * @return the metaData Json String as Json Array
     */
    public static JsonArray jsonStringToArray(String metaData) {
        return new JsonParser().parse(metaData).getAsJsonArray();
    }


}
