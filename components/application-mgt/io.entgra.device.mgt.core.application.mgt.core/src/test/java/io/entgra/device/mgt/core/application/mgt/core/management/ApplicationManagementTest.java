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
package io.entgra.device.mgt.core.application.mgt.core.management;

import io.entgra.device.mgt.core.application.mgt.common.ApplicationArtifact;
import io.entgra.device.mgt.core.application.mgt.common.ApplicationList;
import io.entgra.device.mgt.core.application.mgt.common.ChunkDescriptor;
import io.entgra.device.mgt.core.application.mgt.common.FileMetaEntry;
import io.entgra.device.mgt.core.application.mgt.common.Filter;
import io.entgra.device.mgt.core.application.mgt.common.LifecycleState;
import io.entgra.device.mgt.core.application.mgt.common.TransferLink;
import io.entgra.device.mgt.core.application.mgt.common.services.FileTransferService;
import io.entgra.device.mgt.core.application.mgt.core.impl.FileTransferServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import io.entgra.device.mgt.core.application.mgt.common.dto.ApplicationDTO;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.exception.RequestValidatingException;
import io.entgra.device.mgt.core.application.mgt.common.response.Application;
import io.entgra.device.mgt.core.application.mgt.common.response.ApplicationRelease;
import io.entgra.device.mgt.core.application.mgt.common.response.Category;
import io.entgra.device.mgt.core.application.mgt.common.response.Tag;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.ApplicationWrapper;
import io.entgra.device.mgt.core.application.mgt.common.wrapper.EntAppReleaseWrapper;
import io.entgra.device.mgt.core.application.mgt.core.BaseTestCase;
import io.entgra.device.mgt.core.application.mgt.core.dao.ApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.device.mgt.core.application.mgt.core.dto.ApplicationsDTO;
import io.entgra.device.mgt.core.application.mgt.core.impl.ApplicationManagerImpl;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.application.mgt.core.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.common.Base64File;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.core.common.util.FileUtil;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceType;
import io.entgra.device.mgt.core.device.mgt.core.dto.DeviceTypeVersion;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplicationManagementTest extends BaseTestCase {

    private static final Log log = LogFactory.getLog(ApplicationManagementTest.class);

    @Test
    public void testAddApplication() throws Exception {

        ApplicationDAO applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        DataHolder.getInstance().setDeviceManagementService(new DeviceManagementProviderServiceImpl());
        ConnectionManagerUtil.beginDBTransaction();
        applicationDAO.createApplication(ApplicationsDTO.getApp1(), -1234);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
    }

    @Test(dependsOnMethods = "addApplicationCategories")
    public void createApplication() throws Exception {

        log.debug("Creating the first application ....!");

        ApplicationWrapper applicationWrapper = new ApplicationWrapper();

        List<String> categories = new ArrayList<>();
        categories.add("Test Category");
        applicationWrapper.setCategories(categories);

        applicationWrapper.setDescription("Test Description");
        applicationWrapper.setDeviceType("android");
        applicationWrapper.setName("Test Application");
        applicationWrapper.setSubMethod("Test Sub type");

        List<String> tags = new ArrayList<>();
        tags.add("abc");
        tags.add("pqr");
        tags.add("xyz");
        applicationWrapper.setTags(tags);
        applicationWrapper.setPaymentCurrency("USD");

        List<EntAppReleaseWrapper> entAppReleaseWrappers = new ArrayList<>();
        EntAppReleaseWrapper releaseWrapper = new EntAppReleaseWrapper();
        releaseWrapper.setDescription("First release");
        releaseWrapper.setIsSharedWithAllTenants(false);
        releaseWrapper.setMetaData("[{\"key\": \"Just a metadata\"}]");
        releaseWrapper.setReleaseType("free");
        releaseWrapper.setPrice(5.7);
        releaseWrapper.setSupportedOsVersions("4.0-7.0");


        FileTransferService fileTransferService = FileTransferServiceImpl.getInstance();
        DataHolder.getInstance().setFileTransferService(fileTransferService);

        FileMetaEntry metaEntry = new FileMetaEntry();
        TransferLink transferLink;
        String []segments;
        ChunkDescriptor chunkDescriptor;

        metaEntry.setFileName("banner1");
        metaEntry.setExtension("jpg");
        metaEntry.setSize(179761);
        transferLink = fileTransferService.generateUploadLink(metaEntry);
        segments = transferLink.getRelativeTransferLink().split("/");
        chunkDescriptor = fileTransferService.
                resolve(segments[segments.length-1], Files.newInputStream(
                        Paths.get("src/test/resources/samples/app1/banner1.jpg")));
        fileTransferService.writeChunk(chunkDescriptor);
        releaseWrapper.setBannerLink(transferLink.getDirectTransferLink() + "/banner1.jpg");

        metaEntry.setFileName("icon");
        metaEntry.setExtension("png");
        metaEntry.setSize(41236);
        transferLink = fileTransferService.generateUploadLink(metaEntry);
        segments = transferLink.getRelativeTransferLink().split("/");
        chunkDescriptor = fileTransferService.
                resolve(segments[segments.length-1], Files.newInputStream(
                        Paths.get("src/test/resources/samples/app1/icon.png")));
        fileTransferService.writeChunk(chunkDescriptor);
        releaseWrapper.setIconLink(transferLink.getDirectTransferLink() + "/icon.png");

        List<String> screenshotPaths = Arrays.asList("src/test/resources/samples/app1/shot1.png",
                "src/test/resources/samples/app1/shot2.png", "src/test/resources/samples/app1/shot3.png");
        List<String> screenshotLinks = new ArrayList<>();
        String []pathSegments;
        for (String path: screenshotPaths) {
            pathSegments = path.split("/");
            String fullQualifiedName = pathSegments[pathSegments.length - 1];
            String []nameSegments = fullQualifiedName.split("\\.(?=[^.]+$)");
            metaEntry.setFileName(nameSegments[0]);
            metaEntry.setExtension(nameSegments[1]);
            metaEntry.setSize(41236);
            transferLink = fileTransferService.generateUploadLink(metaEntry);
            segments = transferLink.getRelativeTransferLink().split("/");
            chunkDescriptor = fileTransferService.
                    resolve(segments[segments.length-1], Files.newInputStream(Paths.get(path)));
            fileTransferService.writeChunk(chunkDescriptor);
            screenshotLinks.add(transferLink.getDirectTransferLink() + "/" + fullQualifiedName);
        }
        releaseWrapper.setScreenshotLinks(screenshotLinks);

        metaEntry.setFileName("sample");
        metaEntry.setExtension("apk");
        metaEntry.setSize(6259412);
        TransferLink apkTransferLink = fileTransferService.generateUploadLink(metaEntry);
        segments = apkTransferLink.getRelativeTransferLink().split("/");
        chunkDescriptor = fileTransferService.
                resolve(segments[segments.length-1], Files.newInputStream(Paths.get("src/test/resources/samples/app1/sample.apk")));
        fileTransferService.writeChunk(chunkDescriptor);
        releaseWrapper.setArtifactLink(apkTransferLink.getDirectTransferLink() + "/sample.apk");
        releaseWrapper.setRemoteStatus(false);

        entAppReleaseWrappers.add(releaseWrapper);
        applicationWrapper.setEntAppReleaseWrappers(entAppReleaseWrappers);

        // https://roadmap.entgra.net/issues/12367
        // TODO: Uncomment the line below after fixing the BaseTestCase setupDataSource test issue.
        // ApplicationManager manager = new ApplicationManagerImpl();
        // manager.createApplication(applicationWrapper, false);
    }

    @DataProvider(name = "applicationIdDataProvider")
    public static Object[][] applicationId() {
        return new Object[][] {{-1}};
    }

    @DataProvider(name = "updateApplicationDataProvider")
    public static Object[][] updateApplicationDataProvider() {
        return new Object[][] {{-1, new ApplicationUpdateWrapper()}};
    }

    @DataProvider(name = "uuidDataProvider")
    public static Object[][] uuidDataProvider() {
        return new Object[][] {{"TEST_APP_UUID"}};
    }
    
    @Test(enabled = false)
    public void createApplicationAndPublish(ApplicationWrapper applicationWrapper, ApplicationArtifact applicationArtifact, boolean isPublish) throws ApplicationManagementException {
    
    }
    
    @Test(enabled = false)
    public void updateApplication(int applicationId, ApplicationUpdateWrapper applicationUpdateWrapper) throws ApplicationManagementException {

    }

    @Test(enabled = false)
    public void deleteApplication(int applicationId) throws ApplicationManagementException {

    }

    @Test(enabled = false)
    public void retireApplication(int applicationId) throws ApplicationManagementException {

    }

    @Test(enabled = false)
    public void deleteApplicationRelease(String releaseUuid) throws ApplicationManagementException {

    }

    @Test(enabled = false)
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public Application getApplicationById(int id, String state) throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public ApplicationRelease getApplicationReleaseByUUID(String uuid) throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public ApplicationDTO getApplicationByUuid(String uuid, String state) throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public ApplicationDTO getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public List<LifecycleState> getLifecycleStateChangeFlow(String releaseUuid) throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public void changeLifecycleState(String releaseUuid, String stateName) throws ApplicationManagementException {

    }

    @Test(enabled = false)
    public void updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {

    }

    @Test(enabled = false)
    public void updateApplicationArtifact(String deviceType, String appType, String uuid, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {

    }

    @Test(enabled = false)
    public ApplicationRelease createRelease(int applicationId, EntAppReleaseWrapper entAppReleaseWrapper, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public boolean updateRelease(String deviceType, String applicationType, String releaseUuid, EntAppReleaseWrapper entAppReleaseWrapper, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        return false;
    }

    @Test(enabled = false)
    public void validateAppCreatingRequest(ApplicationWrapper applicationWrapper) throws RequestValidatingException {

    }

    @Test(enabled = false)
    public void validateReleaseCreatingRequest(EntAppReleaseWrapper entAppReleaseWrapper, String applicationType) throws RequestValidatingException {

    }

    @Test(enabled = false)
    public void validateImageArtifacts(Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList) throws RequestValidatingException {

    }

    @Test(enabled = false)
    public void validateBinaryArtifact(Attachment binaryFile, String applicationType) throws RequestValidatingException {

    }

    @Test(dependsOnMethods = "addDeviceVersions")
    public void addApplicationCategories() throws ApplicationManagementException {
        List<String> categories = new ArrayList<>();
        categories.add("Test Category");
        categories.add("Test Category2");
        ApplicationManager manager = new ApplicationManagerImpl();
        manager.addApplicationCategories(categories);

    }

    @Test
    public void addDeviceVersions() throws ApplicationManagementException {
        List<DeviceTypeVersion> deviceTypeVersions = new ArrayList<>();
        List<String> supportingVersions = new ArrayList<>();

        //add supporting versions
        supportingVersions.add("4.0");
        supportingVersions.add("5.0");
        supportingVersions.add("6.0");
        supportingVersions.add("7.0");
        supportingVersions.add("8.0");

        DeviceManagementProviderServiceImpl deviceManagementProviderService = new DeviceManagementProviderServiceImpl();
        try {
            List<DeviceType> deviceTypes = deviceManagementProviderService.getDeviceTypes();

            for (DeviceType deviceType: deviceTypes){
                for (String version : supportingVersions){
                    DeviceTypeVersion deviceTypeVersion = new DeviceTypeVersion();
                    deviceTypeVersion.setDeviceTypeId(deviceType.getId());
                    deviceTypeVersion.setVersionName(version);
                    deviceTypeVersions.add(deviceTypeVersion);
                }
            }

            for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions){
                deviceManagementProviderService.addDeviceTypeVersion(deviceTypeVersion);
            }
        } catch (DeviceManagementException e) {
            String msg = "Error Occured while adding device type versions";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        }
    }

    @Test(enabled = false)
    public List<Tag> getRegisteredTags() throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public List<Category> getRegisteredCategories() throws ApplicationManagementException {
        return null;
    }

    @Test(enabled = false)
    public void deleteTagMapping(int appId, String tagName) throws ApplicationManagementException {

    }
}
