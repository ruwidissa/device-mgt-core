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
package io.entgra.device.mgt.core.application.mgt.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.application.mgt.common.ApplicationArtifact;
import io.entgra.device.mgt.core.application.mgt.common.FileDataHolder;
import io.entgra.device.mgt.core.application.mgt.common.FileDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.LifecycleChanger;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationReleaseDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.ItuneAppDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.FileDownloaderServiceException;
import io.entgra.device.mgt.core.application.mgt.common.exception.FileTransferServiceException;
import io.entgra.device.mgt.core.application.mgt.common.exception.InvalidConfigurationException;
import io.entgra.device.mgt.core.application.mgt.common.exception.RequestValidatingException;
import io.entgra.device.mgt.core.application.mgt.common.response.Application;
import io.entgra.device.mgt.core.application.mgt.common.response.Category;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationStorageManager;
import io.entgra.device.mgt.core.application.mgt.common.services.FileTransferService;
import io.entgra.device.mgt.core.application.mgt.common.services.ReviewManager;
import io.entgra.device.mgt.core.application.mgt.common.services.SPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.services.SubscriptionManager;
import io.entgra.device.mgt.core.application.mgt.common.services.VPPApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.ApplicationWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.CustomAppReleaseWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.CustomAppWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.EntAppReleaseWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.PublicAppReleaseWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.PublicAppWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.WebAppReleaseWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.WebAppWrapper;
import io.entgra.device.mgt.core.application.mgt.core.config.ConfigurationManager;
import io.entgra.device.mgt.core.application.mgt.core.config.Extension;
import io.entgra.device.mgt.core.application.mgt.core.exception.BadRequestException;
import io.entgra.device.mgt.core.application.mgt.core.impl.FileDownloaderServiceProvider;
import io.entgra.device.mgt.core.application.mgt.core.impl.VppApplicationManagerImpl;
import io.entgra.device.mgt.core.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.device.mgt.core.device.mgt.common.Base64File;
import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.app.mgt.App;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.common.util.FileUtil;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.MetadataManagementServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public static ApplicationArtifact constructApplicationArtifact(String iconLink, List<String> screenshotLinks, String artifactLink, String bannerLink)
            throws MalformedURLException, FileDownloaderServiceException {
        ApplicationArtifact applicationArtifact = new ApplicationArtifact();
        FileDescriptor fileDescriptor;
        if (artifactLink != null) {
            URL artifactLinkUrl = new URL(artifactLink);
            fileDescriptor = FileDownloaderServiceProvider.getFileDownloaderService(artifactLinkUrl).download(artifactLinkUrl);
            applicationArtifact.setInstallerName(fileDescriptor.getFullQualifiedName());
            applicationArtifact.setInstallerStream(fileDescriptor.getFile());
            applicationArtifact.setInstallerPath(fileDescriptor.getAbsolutePath());
        }

        if (iconLink != null) {
            URL iconLinkUrl = new URL(iconLink);
            fileDescriptor = FileDownloaderServiceProvider.getFileDownloaderService(iconLinkUrl).download(iconLinkUrl);
            applicationArtifact.setIconName(fileDescriptor.getFullQualifiedName());
            applicationArtifact.setIconStream(fileDescriptor.getFile());
            applicationArtifact.setIconPath(fileDescriptor.getAbsolutePath());
        }

        if (bannerLink != null) {
            URL bannerLinkUrl = new URL(bannerLink);
            fileDescriptor = FileDownloaderServiceProvider.getFileDownloaderService(bannerLinkUrl).download(bannerLinkUrl);
            applicationArtifact.setBannerName(fileDescriptor.getFullQualifiedName());
            applicationArtifact.setBannerStream(fileDescriptor.getFile());
            applicationArtifact.setBannerPath(fileDescriptor.getAbsolutePath());
        }

        if (screenshotLinks != null) {
            Map<String, InputStream> screenshotData = new TreeMap<>();
            Map<String, String> screenshotPaths = new TreeMap<>();
            // This is to handle cases in which multiple screenshots have the same name
            Map<String, Integer> screenshotNameCount = new HashMap<>();
            URL screenshotLinkUrl;
            for (String screenshotLink : screenshotLinks) {
                screenshotLinkUrl = new URL(screenshotLink);
                fileDescriptor = FileDownloaderServiceProvider.getFileDownloaderService(screenshotLinkUrl).download(screenshotLinkUrl);
                String screenshotName = fileDescriptor.getFullQualifiedName();
                screenshotNameCount.put(screenshotName, screenshotNameCount.getOrDefault(screenshotName, 0) + 1);
                screenshotName = FileUtil.generateDuplicateFileName(screenshotName, screenshotNameCount.get(screenshotName));
                screenshotData.put(screenshotName, fileDescriptor.getFile());
                screenshotPaths.put(screenshotName, fileDescriptor.getAbsolutePath());
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

    public static VPPApplicationManager getVPPManagerInstance() {
        // TODO: implement as an extension
        return new VppApplicationManagerImpl();
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

    public static <T> boolean getRemoteStatus(T appWrapper) {
        if (!isReleaseAvailable(appWrapper)) {
            return false;
        }
        if (appWrapper instanceof ApplicationWrapper) {
            return getRemoteStatusFromWrapper(((ApplicationWrapper) appWrapper).getEntAppReleaseWrappers().get(0));
        }
        if (appWrapper instanceof PublicAppWrapper) {
            return getRemoteStatusFromWrapper(((PublicAppWrapper) appWrapper).getPublicAppReleaseWrappers().get(0));
        }
        if (appWrapper instanceof WebAppWrapper) {
            return getRemoteStatusFromWrapper(((WebAppWrapper) appWrapper).getWebAppReleaseWrappers().get(0));
        }
        if (appWrapper instanceof CustomAppWrapper) {
            return getRemoteStatusFromWrapper(((CustomAppWrapper) appWrapper).getCustomAppReleaseWrappers().get(0));
        }
        throw new IllegalArgumentException("Provided bean does not belong to an Application Wrapper");
    }

    public static <T> boolean getRemoteStatusFromWrapper(T releaseWrapper) {
        if (releaseWrapper instanceof EntAppReleaseWrapper) {
            return ((EntAppReleaseWrapper) releaseWrapper).isRemoteStatus();
        }
        if (releaseWrapper instanceof PublicAppReleaseWrapper) {
            return ((PublicAppReleaseWrapper) releaseWrapper).isRemoteStatus();
        }
        if (releaseWrapper instanceof WebAppReleaseWrapper) {
            return ((WebAppReleaseWrapper) releaseWrapper).isRemoteStatus();
        }
        if (releaseWrapper instanceof CustomAppReleaseWrapper) {
            return ((CustomAppReleaseWrapper) releaseWrapper).isRemoteStatus();
        }
        throw new IllegalArgumentException("Provided bean does not belong to an Release Wrapper");
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

    public static void persistApp(ItuneAppDTO product) throws ApplicationManagementException {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Category> categories = applicationManager.getRegisteredCategories();
        if (product != null && product.getVersion() != null) {

            List<String> packageNamesOfApps = new ArrayList<>();
            packageNamesOfApps.add(product.getPackageName());

            List<Application> existingApps = applicationManager.getApplications(packageNamesOfApps);
            PublicAppReleaseWrapper publicAppReleaseWrapper = generatePublicAppReleaseWrapper(product);

            if (existingApps != null && existingApps.size() > 0) {
                Application app = existingApps.get(0);
                if (product.getPackageName().equals(app.getPackageName())) {
                    ApplicationUpdateWrapper applicationUpdateWrapper = generatePubAppUpdateWrapper(product, categories);
                    applicationManager.updateApplication(app.getId(), applicationUpdateWrapper);

                    if (app.getSubMethod()
                            .equalsIgnoreCase(Constants.ApplicationProperties.FREE_SUB_METHOD)) {
                        publicAppReleaseWrapper.setPrice(0.0);
                    } else {
                        publicAppReleaseWrapper.setPrice(1.0);
                    }

                    publicAppReleaseWrapper.setDescription(product.getDescription());
                    publicAppReleaseWrapper.setReleaseType("ga");
                    publicAppReleaseWrapper.setVersion(product.getVersion());
                    publicAppReleaseWrapper.setSupportedOsVersions("4.0-12.3");
                    applicationManager.updatePubAppRelease(app.getApplicationReleases().get(0).getUuid(),
                            publicAppReleaseWrapper);
                    return;
                }
            } else {

                // Generate App wrapper
                PublicAppWrapper publicAppWrapper = generatePubAppWrapper(product, categories);

                publicAppWrapper.setPublicAppReleaseWrappers(
                        Arrays.asList(new PublicAppReleaseWrapper[]{publicAppReleaseWrapper}));

                Application application = applicationManager.createApplication(publicAppWrapper, false);
                if (application != null && (application.getApplicationReleases().get(0).getCurrentStatus() == null
                        || application.getApplicationReleases().get(0).getCurrentStatus().equals("CREATED"))) {
                    String uuid = application.getApplicationReleases().get(0).getUuid();
                    LifecycleChanger lifecycleChanger = new LifecycleChanger();
                    lifecycleChanger.setAction("IN-REVIEW");
                    applicationManager.changeLifecycleState(uuid, lifecycleChanger);
                    lifecycleChanger.setAction("APPROVED");
                    applicationManager.changeLifecycleState(uuid, lifecycleChanger);
                    lifecycleChanger.setAction("PUBLISHED");
                    applicationManager.changeLifecycleState(uuid, lifecycleChanger);
                }
            }
        }
    }

    private static PublicAppReleaseWrapper generatePublicAppReleaseWrapper(ItuneAppDTO product) {
        PublicAppReleaseWrapper publicAppReleaseWrapper = new PublicAppReleaseWrapper();
        publicAppReleaseWrapper.setDescription(product.getDescription());
        publicAppReleaseWrapper.setReleaseType("ga");
        publicAppReleaseWrapper.setVersion(product.getVersion());
        publicAppReleaseWrapper.setPackageName(product.getPackageName());
        publicAppReleaseWrapper.setSupportedOsVersions("4.0-12.3");
        publicAppReleaseWrapper.setIconLink(product.getIconURL());
        publicAppReleaseWrapper.setRemoteStatus(false);
        List<String> screenshotUrls = new ArrayList<>(Collections.nCopies(3, product.getIconURL()));
        publicAppReleaseWrapper.setScreenshotLinks(screenshotUrls);
        publicAppReleaseWrapper.setPrice(1.0);
        return publicAppReleaseWrapper;
    }

    private static PublicAppWrapper generatePubAppWrapper(ItuneAppDTO product, List<Category> categories) {
        PublicAppWrapper publicAppWrapper = new PublicAppWrapper();
        publicAppWrapper.setName(product.getTitle());
        publicAppWrapper.setDescription(product.getDescription());
        publicAppWrapper.setCategories(
                Collections.singletonList(Constants.ApplicationProperties.APPLE_STORE_SYNCED_APP_CATEGORY));//Default category
        for (Category category : categories) {
            if (product.getCategory() == null) {
                List<String> pubAppCategories = new ArrayList<>();
                pubAppCategories.add(Constants.ApplicationProperties.APPLE_STORE_SYNCED_APP_CATEGORY);
                publicAppWrapper.setCategories(pubAppCategories);
                break;
            } else if (product.getCategory().equalsIgnoreCase(category.getCategoryName())) {
                List<String> pubAppCategories = new ArrayList<>();
                pubAppCategories.add(category.getCategoryName());
                pubAppCategories.add(Constants.ApplicationProperties.APPLE_STORE_SYNCED_APP_CATEGORY);
                publicAppWrapper.setCategories(pubAppCategories);
                break;
            }
        }
        if (product.getPaymentMethod().equalsIgnoreCase(Constants.ApplicationProperties.FREE_SUB_METHOD)) {
            publicAppWrapper.setSubMethod(Constants.ApplicationProperties.FREE_SUB_METHOD);
        } else {
            publicAppWrapper.setSubMethod(Constants.ApplicationProperties.PAID_SUB_METHOD);
        }
        // TODO: purchase an app from app store and see how to capture the real value for price
        // field.
        publicAppWrapper.setPaymentCurrency("$");
        publicAppWrapper.setDeviceType(DeviceManagementConstants.MobileDeviceTypes.MOBILE_DEVICE_TYPE_IOS);
        return publicAppWrapper;
    }

    private static ApplicationUpdateWrapper generatePubAppUpdateWrapper(ItuneAppDTO product, List<Category> categories) {
        ApplicationUpdateWrapper applicationUpdateWrapper = new ApplicationUpdateWrapper();
        applicationUpdateWrapper.setName(product.getTitle());
        applicationUpdateWrapper.setDescription(product.getDescription());
        applicationUpdateWrapper.setCategories(
                Collections.singletonList(Constants
                        .ApplicationProperties.APPLE_STORE_SYNCED_APP_CATEGORY));//Default
        // add the default APPLE_STORE_SYNCED_APP_CATEGORY
        for (Category category : categories) {
            if (product.getCategory() == null) {
                List<String> pubAppCategories = new ArrayList<>();
                pubAppCategories.add(Constants.ApplicationProperties.APPLE_STORE_SYNCED_APP_CATEGORY);
                applicationUpdateWrapper.setCategories(pubAppCategories);
                break;
            } else if (product.getCategory().equalsIgnoreCase(category.getCategoryName())) {
                List<String> pubAppCategories = new ArrayList<>();
                pubAppCategories.add(category.getCategoryName());
                pubAppCategories.add(Constants.ApplicationProperties.APPLE_STORE_SYNCED_APP_CATEGORY);
                applicationUpdateWrapper.setCategories(pubAppCategories);
                break;
            }
        }
        if (product.getPaymentMethod().equalsIgnoreCase(Constants.ApplicationProperties.FREE_SUB_METHOD)) {
            applicationUpdateWrapper.setSubMethod(Constants.ApplicationProperties.FREE_SUB_METHOD);
        } else {
            applicationUpdateWrapper.setSubMethod(Constants.ApplicationProperties.PAID_SUB_METHOD);
        }
        // TODO: purchase an app from Playstore and see how to capture the real value for price field.
        applicationUpdateWrapper.setPaymentCurrency("$");
        return applicationUpdateWrapper;
    }

    private static ApplicationArtifact generateArtifacts(ItuneAppDTO product) throws ApplicationManagementException {
        ApplicationArtifact applicationArtifact = new ApplicationArtifact();
        String prefix = product.getPackageName();
        try {
            String iconName = prefix + "_icon";
            applicationArtifact.setIconName(iconName);
            InputStream iconInputStream = getInputStream(iconName, product.getIconURL());
            applicationArtifact.setIconStream(iconInputStream);
            Map<String, InputStream> screenshotMap = new HashMap<>();
            // TODO: look for a way to get screenshots

            for (int a = 0; a < 3; a++) {
                String screenshotName = product.getPackageName() + a;
                InputStream screenshotInputStream = getInputStream(screenshotName, product.getIconURL());
                screenshotMap.put(screenshotName, screenshotInputStream);
            }

            applicationArtifact.setScreenshots(screenshotMap);
            return applicationArtifact;
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while generating Application artifact";
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
    }

    private static InputStream getInputStream(String filename, String url) throws ApplicationManagementException {
        URL website;
        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            String msg = "Error occurred while converting the url " + url;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try {
            rbc = Channels.newChannel(website.openStream());
            fos = new FileOutputStream(System.getProperty("java.io.tmpdir")
                    + File.separator + filename);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            String msg = "Error occurred while opening stream for url " + url;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        } finally {
            try {
                fos.close();
                rbc.close();
            } catch (IOException e) {
            }
        }

        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
        InputStream targetStream;
        try {
            targetStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            String msg = "Error occurred while reading the tmp file  " + System.getProperty("java.io.tmpdir")
                    + File.separator + filename;
            log.error(msg);
            throw new ApplicationManagementException(msg, e);
        }
        file.deleteOnExit();
        return targetStream;
    }

    private static void updateImages(PublicAppReleaseWrapper appReleaseWrapper, String iconName,
                                     InputStream iconStream, Map<String, InputStream>
                                             screenshotsMaps) throws IOException {
        List<Base64File> screenshots = new ArrayList<>();
        Base64File iconFile = new Base64File(iconName,
                convertStreamToBase64(iconStream));
        appReleaseWrapper.setIcon(iconFile);
        if (screenshotsMaps.size() > 0) {
            for (Map.Entry<String, InputStream> screenshotEntry : screenshotsMaps.entrySet()) {
                Base64File screenshot = new Base64File(screenshotEntry.getKey(),
                        convertStreamToBase64(screenshotEntry.getValue()));
                screenshots.add(screenshot);
            }
            appReleaseWrapper.setScreenshots(screenshots);
        }
    }

    private static String convertStreamToBase64(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;

        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);

                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            }
        } catch (IOException e) {
            String msg = "Error while converting image to base64";
            log.error(msg);
            throw e;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public static List<Application> getAppDetails(String adamId) throws ApplicationManagementException {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<String> packageNamesOfApps = new ArrayList<>();
        packageNamesOfApps.add(adamId);
        return applicationManager.getApplications(packageNamesOfApps);
    }

    /**
     * Sanitize app names and shorten icon/screenshot file names
     *
     * @param originalName Original name of the file which is being uploaded
     * @param type Type - Name/Artifact(Icon, Screenshot, etc.)
     * @return Sanitized and shortened file name
     */
    public static String sanitizeName(String originalName, String type) {
        String sanitizedName = originalName.replaceAll(Constants.APP_NAME_REGEX, "");
        if (Constants.ApplicationProperties.NAME.equals(type) && sanitizedName.length() > Constants.MAX_APP_NAME_CHARACTERS) {
            sanitizedName = sanitizedName.substring(0, Constants.MAX_APP_NAME_CHARACTERS);
            return sanitizedName;
        } else if (Constants.ICON_NAME.equals(type) || Constants.SCREENSHOT_NAME.equals(type)) {
            // Shortening icon/screenshot names
            String fileExtension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                fileExtension = originalName.substring(dotIndex);
            }
            return type + fileExtension;
        } else {
            return sanitizedName;
        }
    }

    public static <T> List<?> deriveApplicationWithoutRelease(T app) {
        List<?> releaseWrappers = null;
        if (app instanceof ApplicationWrapper) {
            ApplicationWrapper applicationWrapper = (ApplicationWrapper) app;
            releaseWrappers = applicationWrapper.getEntAppReleaseWrappers();
            applicationWrapper.setEntAppReleaseWrappers(Collections.emptyList());
        }
        if (app instanceof CustomAppWrapper) {
            CustomAppWrapper applicationWrapper = (CustomAppWrapper) app;
            releaseWrappers = applicationWrapper.getCustomAppReleaseWrappers();
            applicationWrapper.setCustomAppReleaseWrappers(Collections.emptyList());
        }
        return releaseWrappers;
    }

    /**
     * Add installer path metadata value to windows applications
     * @param applicationReleaseDTO {@link ApplicationReleaseDTO}
     * @throws ApplicationManagementException Throws when error encountered while updating the app metadata
     */
    public static void addInstallerPathToMetadata(ApplicationReleaseDTO applicationReleaseDTO)
            throws ApplicationManagementException {
        if (applicationReleaseDTO.getMetaData() == null) return;
        Gson gson = new Gson();
        String installerPath = APIUtil.constructInstallerPath(applicationReleaseDTO.getInstallerName(), applicationReleaseDTO.getAppHashValue());
        String[] fileNameSegments = extractNameSegments(applicationReleaseDTO, installerPath);
        String extension = fileNameSegments[fileNameSegments.length - 1];
        if (!Objects.equals(extension, "appx") && !Objects.equals(extension, "msi")) {
            return;
        }

        String installerPaths = "[ {" +
                "\"key\": \"Content_Uri\", " +
                "\"value\" : \"" + installerPath + "\"" +
                "}]";

        if (Objects.equals(extension, "appx")) {
            installerPaths = "[ {" +
                    "\"key\": \"Package_Url\", " +
                    "\"value\" : \"" + installerPath + "\"" +
                    "}]";
        }

        JsonArray parsedMetadataList = gson.fromJson(applicationReleaseDTO.getMetaData(), JsonArray.class);
        JsonArray installerPathsArray = gson.fromJson(installerPaths, JsonArray.class);
        parsedMetadataList.addAll(installerPathsArray);
        applicationReleaseDTO.setMetaData(gson.toJson(parsedMetadataList));
    }

    /**
     * Extract name segments from installer path
     * @param applicationReleaseDTO {@link ApplicationReleaseDTO}
     * @param installerPath Installer path
     * @return Extracted file name segments
     * @throws ApplicationManagementException Throws when error encountered while extracting name segments from installer path
     */
    private static String[] extractNameSegments(ApplicationReleaseDTO applicationReleaseDTO, String installerPath)
            throws ApplicationManagementException {
        String []installerPathSegments = installerPath.split("/");
        if (installerPathSegments.length == 0) {
            throw new ApplicationManagementException("Received malformed url for installer path of the app : "
                    + applicationReleaseDTO.getInstallerName());
        }
        String fullQualifiedName = installerPathSegments[installerPathSegments.length - 1];
        String []fileNameSegments = fullQualifiedName.split("\\.(?=[^.]+$)");
        if (fileNameSegments.length != 2) {
            throw new ApplicationManagementException("Received malformed url for installer path of the app : "
                    + applicationReleaseDTO.getInstallerName());
        }
        return fileNameSegments;
    }

}
