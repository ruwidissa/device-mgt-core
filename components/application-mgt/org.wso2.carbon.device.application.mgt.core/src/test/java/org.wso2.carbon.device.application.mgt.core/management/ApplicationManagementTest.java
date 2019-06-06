package org.wso2.carbon.device.application.mgt.core.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.testng.annotations.Test;
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.response.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.response.Category;
import org.wso2.carbon.device.application.mgt.common.response.Tag;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;
import org.wso2.carbon.device.application.mgt.core.BaseTestCase;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.dto.ApplicationsDTO;
import org.wso2.carbon.device.application.mgt.core.impl.ApplicationManagerImpl;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationManagementTest extends BaseTestCase {

    private static final Log log = LogFactory.getLog(ApplicationManagementTest.class);

    @Test
    public void testAddApplication() throws Exception {

        ApplicationDAO applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        ConnectionManagerUtil.beginDBTransaction();
        applicationDAO.createApplication(ApplicationsDTO.getApp1(), -1234);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
    }

    @Test(dependsOnMethods = ("addAplicationCategories"))
    public void createApplication() throws Exception {

        log.debug("Creating the first application ....!");

        ApplicationWrapper applicationWrapper = new ApplicationWrapper();

        List<String> categories = new ArrayList<>();
        categories.add("Test Category");
        applicationWrapper.setAppCategories(categories);

        applicationWrapper.setDescription("Test Description");
        applicationWrapper.setDeviceType("android");
        applicationWrapper.setName("Test Application");
        applicationWrapper.setSubType("Test Sub type");

        List<String> tags = new ArrayList<>();
        tags.add("abc");
        tags.add("pqr");
        tags.add("xyz");
        applicationWrapper.setTags(tags);
        applicationWrapper.setPaymentCurrency("USD");

        List<ApplicationReleaseWrapper> applicationReleaseWrappers  = new ArrayList<>();
        ApplicationReleaseWrapper releaseWrapper = new ApplicationReleaseWrapper();
        releaseWrapper.setDescription("First release");
        releaseWrapper.setIsSharedWithAllTenants(false);
        releaseWrapper.setMetaData("Just meta data");
        releaseWrapper.setReleaseType("free");
        releaseWrapper.setPrice(5.7);
        releaseWrapper.setSupportedOsVersions("5.7, 6.1");
        applicationReleaseWrappers.add(releaseWrapper);

        applicationWrapper.setApplicationReleaseWrappers(applicationReleaseWrappers);

        ApplicationArtifact applicationArtifact = new ApplicationArtifact();
        applicationArtifact.setBannerName("My First Banner");
        File banner = new File("src/test/resources/samples/app1/banner1.jpg");
        InputStream bannerStream = new FileInputStream(banner);
        applicationArtifact.setBannerStream(bannerStream);
        applicationArtifact.setIconName("My First Icon");
        applicationArtifact.setIconStream(new FileInputStream(new File("src/test/resources/samples/app1/icon.png")));
        applicationArtifact.setInstallerName("Test Android App");
        applicationArtifact.setInstallerStream(new FileInputStream(new File("src/test/resources/samples/app1/sample.apk")));

        Map<String, InputStream> screenshots = new HashMap<>();
        screenshots.put("shot1", new FileInputStream(new File("src/test/resources/samples/app1/shot1.png")));
        screenshots.put("shot2", new FileInputStream(new File("src/test/resources/samples/app1/shot2.png")));
        screenshots.put("shot3", new FileInputStream(new File("src/test/resources/samples/app1/shot3.png")));

        applicationArtifact.setScreenshots(screenshots);

        ApplicationManager manager = new ApplicationManagerImpl();
        manager.createApplication(applicationWrapper, applicationArtifact);
    }

    @Test
    public void updateApplication(int applicationId, ApplicationUpdateWrapper applicationUpdateWrapper) throws ApplicationManagementException {

    }

    @Test
    public void deleteApplication(int applicationId) throws ApplicationManagementException {

    }

    @Test
    public void retireApplication(int applicationId) throws ApplicationManagementException {

    }

    @Test
    public void deleteApplicationRelease(String releaseUuid) throws ApplicationManagementException {

    }

    @Test
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        return null;
    }

    @Test
    public Application getApplicationById(int id, String state) throws ApplicationManagementException {
        return null;
    }

    @Test
    public ApplicationRelease getApplicationReleaseByUUID(String uuid) throws ApplicationManagementException {
        return null;
    }

    @Test
    public ApplicationDTO getApplicationByUuid(String uuid, String state) throws ApplicationManagementException {
        return null;
    }

    @Test
    public ApplicationDTO getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        return null;
    }

    @Test
    public List<LifecycleState> getLifecycleStateChangeFlow(String releaseUuid) throws ApplicationManagementException {
        return null;
    }

    @Test
    public void changeLifecycleState(String releaseUuid, String stateName) throws ApplicationManagementException {

    }

    @Test
    public void updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {

    }

    @Test
    public void updateApplicationArtifact(String deviceType, String appType, String uuid, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {

    }

    @Test
    public ApplicationRelease createRelease(int applicationId, ApplicationReleaseWrapper applicationReleaseWrapper, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        return null;
    }

    @Test
    public boolean updateRelease(String deviceType, String applicationType, String releaseUuid, ApplicationReleaseWrapper applicationReleaseWrapper, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        return false;
    }

    @Test
    public void validateAppCreatingRequest(ApplicationWrapper applicationWrapper) throws RequestValidatingException {

    }

    @Test
    public void validateReleaseCreatingRequest(ApplicationReleaseWrapper applicationReleaseWrapper, String applicationType) throws RequestValidatingException {

    }

    @Test
    public void validateImageArtifacts(Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList) throws RequestValidatingException {

    }

    @Test
    public void validateBinaryArtifact(Attachment binaryFile, String applicationType) throws RequestValidatingException {

    }

    @Test
    public void addAplicationCategories() throws ApplicationManagementException {

        List<String> categories = new ArrayList<>();
        categories.add("Test Category");
        categories.add("Test Category2");
        ApplicationManager manager = new ApplicationManagerImpl();
        manager.addAplicationCategories(categories);

    }

    @Test
    public List<Tag> getRegisteredTags() throws ApplicationManagementException {
        return null;
    }

    @Test
    public List<Category> getRegisteredCategories() throws ApplicationManagementException {
        return null;
    }

    @Test
    public void deleteTagMapping(int appId, String tagName) throws ApplicationManagementException {

    }
}
