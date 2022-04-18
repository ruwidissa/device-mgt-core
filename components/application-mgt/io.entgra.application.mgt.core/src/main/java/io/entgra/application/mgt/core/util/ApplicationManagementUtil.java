/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package io.entgra.application.mgt.core.util;

import io.entgra.application.mgt.common.ApplicationArtifact;
import io.entgra.application.mgt.common.Base64File;
import io.entgra.application.mgt.common.FileDataHolder;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.exception.ResourceManagementException;
import io.entgra.application.mgt.common.services.SPApplicationManager;
import io.entgra.application.mgt.common.wrapper.ApplicationWrapper;
import io.entgra.application.mgt.common.wrapper.CustomAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.CustomAppWrapper;
import io.entgra.application.mgt.common.wrapper.EntAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppWrapper;
import io.entgra.application.mgt.core.exception.BadRequestException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.exception.InvalidConfigurationException;
import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.application.mgt.common.services.ApplicationStorageManager;
import io.entgra.application.mgt.common.services.ReviewManager;
import io.entgra.application.mgt.common.services.SubscriptionManager;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.core.config.Extension;
import io.entgra.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.mgt.core.common.util.FileUtil;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This DAOUtil class is responsible for making sure single instance of each Extension Manager is used throughout for
 * all the tasks.
 */
public class ApplicationManagementUtil {

    private static Log log = LogFactory.getLog(ApplicationManagementUtil.class);

    /**
     * Construct ApplicationArtifact from given base64 artifact files
     *
     * @param iconBase64 icon of the application
     * @param screenshotsBase64 screenshots of the application
     * @param binaryFileBase64 binary file of the application
     * @param bannerFileBase64 banner of the application
     * @return ApplicationArtifact the give base64 release artifact files
     * @throws BadRequestException if any invalid payload is found
     */
    public static ApplicationArtifact constructApplicationArtifact(Base64File iconBase64, List<Base64File> screenshotsBase64,
                                                                   Base64File binaryFileBase64, Base64File bannerFileBase64)
            throws BadRequestException {
        ApplicationArtifact applicationArtifact = new ApplicationArtifact();
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        if (binaryFileBase64 != null) {
            try {
                applicationManager.validateBase64File(binaryFileBase64);
            } catch (RequestValidatingException e) {
                String msg = "Invalid base64 binary file payload found";
                log.error(msg, e);
                throw new BadRequestException(msg, e);
            }
            FileDataHolder binaryFile = base64FileToFileDataHolder(binaryFileBase64);
            applicationArtifact.setInstallerName(binaryFile.getName());
            applicationArtifact.setInstallerStream(binaryFile.getFile());
        }
        if (iconBase64 != null) {
            try {
                applicationManager.validateBase64File(iconBase64);
            } catch (RequestValidatingException e) {
                String msg = "Invalid base64 icon file payload found";
                log.error(msg, e);
                throw new BadRequestException(msg, e);
            }
            FileDataHolder iconFile = base64FileToFileDataHolder(iconBase64);
            applicationArtifact.setIconName(iconFile.getName());
            applicationArtifact.setIconStream(iconFile.getFile());
        }
        if (bannerFileBase64 != null) {
            try {
                applicationManager.validateBase64File(bannerFileBase64);
            } catch (RequestValidatingException e) {
                String msg = "Invalid base64 banner file payload found";
                log.error(msg, e);
                throw new BadRequestException(msg, e);
            }
            FileDataHolder bannerFile = base64FileToFileDataHolder(bannerFileBase64);
            applicationArtifact.setBannerName(bannerFile.getName());
            applicationArtifact.setBannerStream(bannerFile.getFile());
        }

        if (screenshotsBase64 != null) {
            Map<String, InputStream> screenshotData = new TreeMap<>();
            // This is to handle cases in which multiple screenshots have the same name
            Map<String, Integer> screenshotNameCount = new HashMap<>();
            for (Base64File screenshot : screenshotsBase64) {
                try {
                    applicationManager.validateBase64File(screenshot);
                } catch (RequestValidatingException e) {
                    String msg = "Invalid base64 screenshot file payload found";
                    log.error(msg, e);
                    throw new BadRequestException(msg, e);
                }
                FileDataHolder screenshotFile = base64FileToFileDataHolder(screenshot);
                String screenshotName = screenshotFile.getName();
                screenshotNameCount.put(screenshotName, screenshotNameCount.getOrDefault(screenshotName, 0) + 1);
                screenshotName = FileUtil.generateDuplicateFileName(screenshotName, screenshotNameCount.get(screenshotName));
                screenshotData.put(screenshotName, screenshotFile.getFile());
            }
            applicationArtifact.setScreenshots(screenshotData);
        }
        return applicationArtifact;
    }

    /**
     *
     * @param base64File Base64File that should be converted to FileDataHolder bean
     * @return FileDataHolder bean which contains input stream and name of the file
     */
    public static FileDataHolder base64FileToFileDataHolder(Base64File base64File) {
        InputStream stream = FileUtil.base64ToInputStream(base64File.getBase64String());
        return new FileDataHolder(base64File.getName(), stream);
    }

    public static SPApplicationManager getSPApplicationManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.SPApplicationManager);
        return getInstance(extension, SPApplicationManager.class);
    }

    public static ApplicationManager getApplicationManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ApplicationManager);
        return getInstance(extension, ApplicationManager.class);
    }

    public static ReviewManager getReviewManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ReviewManager);
        return getInstance(extension, ReviewManager.class);
    }

    public static SubscriptionManager getSubscriptionManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.SubscriptionManager);
        return getInstance(extension, SubscriptionManager.class);
    }

    public static ApplicationStorageManager getApplicationStorageManagerInstance() throws
            InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ApplicationStorageManager);
        return getInstance(extension, ApplicationStorageManager.class);
    }

    public static LifecycleStateManager getLifecycleStateMangerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.LifecycleStateManager);
        return getInstance(extension, LifecycleStateManager.class);
    }

    /**
     * This is useful to delete application artifacts if any error occurred while creating release/application
     * after uploading the artifacts
     *
     * @param app ApplicationDTO of the application of which the artifacts should be deleted
     * @throws ApplicationManagementException if error occurred while deleting artifacts
     */
    public static <T> void deleteArtifactIfExist(ApplicationDTO app) throws ApplicationManagementException {
        ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManagerInstance();
        if (!app.getApplicationReleaseDTOs().isEmpty()) {
            applicationManager.deleteApplicationArtifacts(Collections.singletonList(app.getApplicationReleaseDTOs().get(0).getAppHashValue()));
        }
    }

    /**
     * Check if application release available for a given application wrapper. This is useful since
     * if a release is available for an application that needs to handled separately
     *
     * @param appWrapper Application wrapper bean of the application
     * @return if release is available or not
     */
    public static <T> boolean isReleaseAvailable(T appWrapper) {
        if (appWrapper instanceof ApplicationWrapper) {
            List<EntAppReleaseWrapper> entAppReleaseWrappers = ((ApplicationWrapper) appWrapper).getEntAppReleaseWrappers();
            return entAppReleaseWrappers != null && !entAppReleaseWrappers.isEmpty();
        }
        if (appWrapper instanceof PublicAppWrapper) {
            List<PublicAppReleaseWrapper> publicAppReleaseWrappers = ((PublicAppWrapper) appWrapper).getPublicAppReleaseWrappers();
            return publicAppReleaseWrappers != null && !publicAppReleaseWrappers.isEmpty();
        }
        if (appWrapper instanceof WebAppWrapper) {
            List<WebAppReleaseWrapper> webAppReleaseWrappers = ((WebAppWrapper) appWrapper).getWebAppReleaseWrappers();
            return webAppReleaseWrappers != null && !webAppReleaseWrappers.isEmpty();
        }
        if (appWrapper instanceof CustomAppWrapper) {
            List<CustomAppReleaseWrapper> customAppReleaseWrappers = ((CustomAppWrapper) appWrapper).getCustomAppReleaseWrappers();
            return customAppReleaseWrappers != null && !((CustomAppWrapper) appWrapper).getCustomAppReleaseWrappers().isEmpty();
        }
        throw new IllegalArgumentException("Provided bean does not belong to an Application Wrapper");
    }

    public static <T> T getInstance(Extension extension, Class<T> cls) throws InvalidConfigurationException {
        try {
            Class theClass = Class.forName(extension.getClassName());
            if (extension.getParameters() != null && extension.getParameters().size() > 0) {
                Class[] types = new Class[extension.getParameters().size()];
                Object[] paramValues = new String[extension.getParameters().size()];
                for (int i = 0; i < extension.getParameters().size(); i++) {
                    types[i] = String.class;
                    paramValues[i] = extension.getParameters().get(i).getValue();
                }
                Constructor<T> constructor = theClass.getConstructor(types);
                return constructor.newInstance(paramValues);
            } else {
                Constructor<T> constructor = theClass.getConstructor();
                return constructor.newInstance();
            }
        } catch (Exception e) {
            String msg = "Unable to get instance of extension - " + extension.getName() + " , for class - " + extension
                    .getClassName();
            log.error(msg, e);
            throw new InvalidConfigurationException(msg, e);
        }
    }
}
