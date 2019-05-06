/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.publisher.api.services.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.response.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.services.AppmDataHandler;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.services.ApplicationManagementPublisherAPI;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataHandler;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/applications")
public class ApplicationManagementPublisherAPIImpl implements ApplicationManagementPublisherAPI {

    private static Log log = LogFactory.getLog(ApplicationManagementPublisherAPIImpl.class);

    @POST
    @Override
    @Consumes("application/json")
    public Response getApplications(
            @Valid Filter filter ){
        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        try {
            ApplicationList applications = applicationManager.getApplications(filter);
            if (applications.getApplications().isEmpty()) {
                return Response.status(Response.Status.OK)
                        .entity("Couldn't find any application for the requested query.").build();
            }
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch(BadRequestException e){
            String msg = "Incompatible request payload is found. Please try with valid request payload.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application list for publisher ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/{appId}")
    public Response getApplication(
            @PathParam("appId") int appId,
            @QueryParam("state") String state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application application = applicationManager.getApplicationById(appId, state);
            if (application == null){
                String msg = "Could not found an application release which is in " + state + " state.";
                log.error(msg);
                return Response.status(Response.Status.OK).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "ApplicationDTO with application id: " + appId + " not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch(ForbiddenException e){
            String msg = "You don't have permission to access the application. application id: " + appId;
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        }
        catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application with the id " + appId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/release/{uuid}")
    public Response getApplicationRelease(
            @PathParam("uuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            ApplicationRelease applicationRelease = applicationManager.getApplicationReleaseByUUID(uuid);
            if (applicationRelease == null){
                String msg = "Application release is in the end state of the application lifecycle flow.";
                log.error(msg);
                return Response.status(Response.Status.OK).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (NotFoundException e) {
            String msg = "Application Release with UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch(ForbiddenException e){
            String msg = "You don't have permission to access the application release. application release UUID: : "
                    + uuid;
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        }
        catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Consumes("multipart/mixed")
    public Response createApplication(
            @Multipart("application") ApplicationWrapper applicationWrapper,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> attachmentList = new ArrayList<>();

        if (screenshot1 != null) {
            attachmentList.add(screenshot1);
        }
        if (screenshot2 != null) {
            attachmentList.add(screenshot2);
        }
        if (screenshot3 != null) {
            attachmentList.add(screenshot3);
        }

        try {
            applicationManager.validateAppCreatingRequest(applicationWrapper);
            applicationManager.validateReleaseCreatingRequest(applicationWrapper.getApplicationReleaseWrappers().get(0),
                    applicationWrapper.getType());
            applicationManager.validateBinaryArtifact(binaryFile, applicationWrapper.getType());
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachmentList);

            // Created new application entry
            Application application = applicationManager.createApplication(applicationWrapper,
                    constructApplicationArtifact(binaryFile, iconFile, bannerFile, attachmentList));
            if (application != null) {
                return Response.status(Response.Status.CREATED).entity(application).build();
            } else {
                String msg = "Application creation is failed";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Error occurred while handling the application creating request";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Consumes("multipart/mixed")
    @Path("/{appType}/{appId}")
    public Response createRelease(
            @PathParam("appType") String appType,
            @PathParam("appId") int appId,
            @Multipart("applicationRelease") ApplicationReleaseWrapper applicationReleaseWrapper,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> attachmentList = new ArrayList<>();

        if (screenshot1 != null) {
            attachmentList.add(screenshot1);
        }
        if (screenshot2 != null) {
            attachmentList.add(screenshot2);
        }
        if (screenshot3 != null) {
            attachmentList.add(screenshot3);
        }

        try {
            applicationManager.validateReleaseCreatingRequest(applicationReleaseWrapper, appType);
            applicationManager.validateBinaryArtifact(binaryFile, appType);
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachmentList);

            // Created new application release
            ApplicationRelease release = applicationManager.createRelease(appId, applicationReleaseWrapper,
                    constructApplicationArtifact(binaryFile, iconFile, bannerFile, attachmentList));
            if (release != null) {
                return Response.status(Response.Status.CREATED).entity(release).build();
            } else {
                log.error("ApplicationDTO Creation Failed");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Error occurred while handling the application creating request";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Consumes("multipart/mixed")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/image-artifacts/{uuid}")
    public Response updateApplicationImageArtifacts(
            @PathParam("uuid") String applicationReleaseUuid,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        try {
            List<Attachment> attachments = new ArrayList<>();

            if (screenshot1 != null) {
                attachments.add(screenshot1);
            }
            if (screenshot2 != null) {
                attachments.add(screenshot2);
            }
            if (screenshot3 != null) {
                attachments.add(screenshot3);
            }
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachments);
            applicationManager.updateApplicationImageArtifact(applicationReleaseUuid,
                    constructApplicationArtifact(null, iconFile, bannerFile, attachments));
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application " + applicationReleaseUuid).build();
        } catch (RequestValidatingException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the application image artifacts for application release uuid: "
                    + applicationReleaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Consumes("multipart/mixed")
    @Path("/app-artifact/{deviceType}/{appType}/{uuid}")
    public Response updateApplicationArtifact(
            @PathParam("deviceType") String deviceType,
            @PathParam("appType") String appType,
            @PathParam("uuid") String applicationReleaseUuid,
            @Multipart("binaryFile") Attachment binaryFile) {

        try {
            if (!ApplicationType.ENTERPRISE.toString().equals(appType)) {
                String msg = "If ApplicationDTO type is " + appType
                        + ", therefore you don't have application release artifact to update for application release UUID: "
                        + applicationReleaseUuid;
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            applicationManager.validateBinaryArtifact(binaryFile, appType);
            applicationManager.updateApplicationArtifact(deviceType, appType, applicationReleaseUuid,
                    constructApplicationArtifact(binaryFile, null, null, null));
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application release. UUID is "
                            + applicationReleaseUuid).build();
        } catch (RequestValidatingException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while updating the image artifacts of the application with the uuid "
                    + applicationReleaseUuid, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Consumes("application/json")
    @Path("/{appId}")
    public Response updateApplication(
            @PathParam("appId") int applicationId,
            @Valid ApplicationUpdateWrapper applicationUpdateWrapper) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.updateApplication(applicationId, applicationUpdateWrapper);
            return Response.status(Response.Status.OK)
                    .entity("Application was updated successfully for ApplicationID: " + applicationId).build();
        } catch (NotFoundException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }  catch (BadRequestException e) {
            String msg = "Error occurred while modifying the application. Found bad request payload for updating the "
                    + "application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        catch (ApplicationManagementException e) {
            String msg = "Internal Error occurred while modifying the application.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("/app-release/{deviceType}/{appType}/{uuid}")
    public Response updateApplicationRelease(
            @PathParam("deviceType") String deviceType,
            @PathParam("appType") String appType,
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") ApplicationReleaseWrapper applicationReleaseWrapper,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> screenshots = new ArrayList<>();
        if (screenshot1 != null){
            screenshots.add(screenshot1);
        }
        if (screenshot2 != null) {
            screenshots.add(screenshot2);
        }
        if (screenshot3 != null) {
            screenshots.add(screenshot3);
        }
        try {
            applicationManager.validateBinaryArtifact(binaryFile, appType);
            applicationManager.validateImageArtifacts(iconFile, bannerFile, screenshots);
            if (!applicationManager.updateRelease(deviceType, appType, applicationUUID, applicationReleaseWrapper,
                    constructApplicationArtifact(binaryFile, iconFile, bannerFile, screenshots))) {
                String msg ="Application release updating is failed. Please contact the administrator. "
                        + "ApplicationDTO release UUID: " + applicationUUID + ", Supported device type: " + deviceType;
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity("Application release is successfully updated.").build();
        } catch (BadRequestException e) {
            String msg =
                    "Invalid request to update application release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg =
                    "Couldn't found application or application release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to update the application release which has UUID "
                    + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error while updating the application release of the application with UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Error occurred while updating the application release in the file system";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }


    @PUT
    @Path("/retire/{appId}")
    public Response retireApplication(
            @PathParam("appId") int applicationId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.retireApplication(applicationId);
            return Response.status(Response.Status.OK)
                    .entity("Successfully deleted the application for application ID: " + applicationId).build();
        } catch (NotFoundException e) {
            String msg =
                    "Couldn't found application for application id: " + applicationId + " to delete the application";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to delete the application which has ID " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/life-cycle/state-changes/{uuid}")
    public Response getLifecycleStates(
            @PathParam("uuid") String releaseUuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            List<LifecycleState> lifecycleStates = applicationManager.getLifecycleStateChangeFlow(releaseUuid);
            return Response.status(Response.Status.OK).entity(lifecycleStates).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release for UUID: " + releaseUuid;
            log.error(msg);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg =
                    "Error occurred while getting lifecycle states for application release UUID: " + releaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/life-cycle/{uuid}")
    public Response addLifecycleState(
            @PathParam("uuid") String applicationUuid,
            @QueryParam("action") String action) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (StringUtils.isEmpty(action)) {
                String msg = "The Action is null or empty. Please verify the request.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            applicationManager.changeLifecycleState( applicationUuid, action);
        } catch (NotFoundException e) {
            String msg = "Could,t find application release for application release uuid: " + applicationUuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while adding lifecycle state.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.CREATED).entity("Lifecycle state added successfully.").build();
    }

    @GET
    @Override
    @Consumes("application/json")
    @Path("/lifecycle-config")
    public Response getLifecycleConfig() {
        AppmDataHandler dataHandler = APIUtil.getDataHandler();
        try {
            return Response.status(Response.Status.OK).entity(dataHandler.getLifecycleConfiguration()).build();
        } catch (LifecycleManagementException e) {
            String msg = "Error Occurred while accessing lifecycle manager.";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /***
     *
     * @param binaryFile binary file of the application release
     * @param iconFile icon file of the application release
     * @param bannerFile banner file of the application release
     * @param attachmentList list of screenshot of the application release
     * @return {@link ApplicationArtifact}
     * @throws ApplicationManagementException if an error occurs when reading the attached data.
     */
    private ApplicationArtifact constructApplicationArtifact(Attachment binaryFile, Attachment iconFile,
            Attachment bannerFile, List<Attachment> attachmentList) throws ApplicationManagementException {
        try {
            ApplicationArtifact applicationArtifact = new ApplicationArtifact();
            DataHandler dataHandler;
            if (binaryFile != null) {
                dataHandler = binaryFile.getDataHandler();
                InputStream installerStream = dataHandler.getInputStream();
                String installerFileName = dataHandler.getName();
                if (installerStream == null) {
                    String msg = "Stream of the application release installer is null. Hence can't proceed. Please "
                            + "verify the installer file.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                if (installerFileName == null) {
                    String msg = "Installer file name retrieving is failed.. Hence can't proceed. Please verify the "
                            + "installer file.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                applicationArtifact.setInstallerName(installerFileName);
                applicationArtifact.setInstallerStream(installerStream);
            }

            if (iconFile != null) {
                dataHandler = iconFile.getDataHandler();
                String iconFileName = dataHandler.getName();
                InputStream iconStream = dataHandler.getInputStream();

                if (iconStream == null) {
                    String msg = "Stream of the application release icon is null. Hence can't proceed. Please "
                            + "verify the uploaded icon file.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                if (iconFileName == null) {
                    String msg = "Icon file name retrieving is failed.. Hence can't proceed. Please verify the "
                            + "icon file.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                applicationArtifact.setIconName(iconFileName);
                applicationArtifact.setIconStream(iconStream);
            }

            if (bannerFile != null) {
                dataHandler = bannerFile.getDataHandler();
                String bannerFileName = dataHandler.getName();
                InputStream bannerStream = dataHandler.getInputStream();
                if (bannerStream == null) {
                    String msg = "Stream of the application release banner is null. Hence can't proceed. Please "
                            + "verify the uploaded banner file.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                if (bannerFileName == null) {
                    String msg = "Banner file name retrieving is failed.. Hence can't proceed. Please verify the "
                            + "banner file.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                applicationArtifact.setBannerName(bannerFileName);
                applicationArtifact.setBannerStream(bannerStream);
            }

            if (attachmentList != null) {
                Map<String, InputStream> scrrenshotData = new HashMap<>();
                for (Attachment sc : attachmentList) {
                    dataHandler = sc.getDataHandler();
                    String screenshotrFileName = dataHandler.getName();
                    InputStream screenshotStream = dataHandler.getInputStream();
                    if (screenshotStream == null) {
                        String msg =
                                "Stream of one of the application release screenshot is null. Hence can't proceed. Please "
                                        + "verify the uploaded screenshots.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    if (screenshotrFileName == null) {
                        String msg =
                                "Screenshot file name retrieving is failed for one screenshot. Hence can't proceed. "
                                        + "Please verify the screenshots.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    scrrenshotData.put(screenshotrFileName, screenshotStream);
                }
                applicationArtifact.setScreenshots(scrrenshotData);
            }
            return applicationArtifact;
        } catch (IOException e) {
            String msg = "Error occurred when reading attachment data.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        }

    }

}
