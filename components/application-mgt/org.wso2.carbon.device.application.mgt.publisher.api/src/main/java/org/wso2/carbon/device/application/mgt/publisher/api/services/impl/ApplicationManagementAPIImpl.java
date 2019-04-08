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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.dto.LifecycleStateDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;
import org.wso2.carbon.device.application.mgt.core.exception.BadRequestException;
import org.wso2.carbon.device.application.mgt.core.exception.ForbiddenException;
import org.wso2.carbon.device.application.mgt.core.exception.ValidationException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.services.ApplicationManagementAPI;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @GET
    @Override
    @Consumes("application/json")
    public Response getApplications(
            @QueryParam("device-type") String deviceType,
            @QueryParam("name") String appName,
            @QueryParam("type") String appType,
            @QueryParam("category") String appCategory,
            @QueryParam("exact-match") boolean isFullMatch,
            @QueryParam("release-state") String releaseState,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("20") @QueryParam("limit") int limit,
            @DefaultValue("ASC") @QueryParam("sort") String sortBy) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        try {
            Filter filter = APIUtil
                    .constructFilter(appName, appType, appCategory, isFullMatch, releaseState, offset,
                            limit, sortBy);
            ApplicationList applications = applicationManager.getApplications(filter, deviceType);
            if (applications.getApplications().isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Couldn't find any application for the requested query.").build();
            }
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch(BadRequestException e){
            String msg = "Couldn't found a device type for " + deviceType;
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
            @DefaultValue("PUBLISHED") @QueryParam("state") String state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            ApplicationDTO application = applicationManager.getApplicationById(appId, state);
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "ApplicationDTO with application id: " + appId + " not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application with the id " + appId;
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
            applicationManager.isValidAttachmentSet(binaryFile, iconFile, bannerFile, attachmentList,
                    applicationWrapper.getType());

            // Created new application entry
            Application application = applicationManager.createApplication(applicationWrapper,
                    constructApplicationArtifact(binaryFile, iconFile, bannerFile, attachmentList));
            if (application != null) {
                return Response.status(Response.Status.CREATED).entity(application).build();
            } else {
                String msg = "ApplicationDTO creation is failed";
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

//    @POST
//    @Consumes("multipart/mixed")
//    @Path("/{deviceType}/{appType}/{appId}")
//    public Response createRelease(
//            @PathParam("deviceType") String deviceType,
//            @PathParam("appType") String appType,
//            @PathParam("appId") int appId,
//            @Multipart("applicationRelease") ApplicationReleaseDTO applicationRelease,
//            @Multipart("binaryFile") Attachment binaryFile,
//            @Multipart("icon") Attachment iconFile,
//            @Multipart("banner") Attachment bannerFile,
//            @Multipart("screenshot1") Attachment screenshot1,
//            @Multipart("screenshot2") Attachment screenshot2,
//            @Multipart("screenshot3") Attachment screenshot3) {
//        ApplicationManager applicationManager = APIUtil.getApplicationManager();
//        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
//        InputStream iconFileStream;
//        InputStream bannerFileStream;
//        List<InputStream> attachments = new ArrayList<>();
//        List<Attachment> attachmentList = new ArrayList<>();
//        attachmentList.add(screenshot1);
//        if (screenshot2 != null) {
//            attachmentList.add(screenshot2);
//        }
//        if (screenshot3 != null) {
//            attachmentList.add(screenshot3);
//        }
//
//        try {
//            applicationManager
//                    .validateReleaseCreatingRequest(applicationRelease, appType, binaryFile, iconFile, bannerFile,
//                            attachmentList);
//
//            // The application executable artifacts such as apks are uploaded.
//            if (!ApplicationType.ENTERPRISE.toString().equals(appType)) {
//                applicationRelease = applicationStorageManager
//                        .uploadReleaseArtifact(applicationRelease, appType, deviceType, null);
//            } else {
//                applicationRelease = applicationStorageManager
//                        .uploadReleaseArtifact(applicationRelease, appType, deviceType,
//                                binaryFile.getDataHandler().getInputStream());
//                if (applicationRelease.getInstallerName() == null || applicationRelease.getAppHashValue() == null) {
//                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//                }
//            }
//
//            iconFileStream = iconFile.getDataHandler().getInputStream();
//            bannerFileStream = bannerFile.getDataHandler().getInputStream();
//
//            for (Attachment screenshot : attachmentList) {
//                attachments.add(screenshot.getDataHandler().getInputStream());
//            }
//
//            // Upload images
//            applicationRelease = applicationStorageManager
//                    .uploadImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, attachments);
//            applicationRelease.setUuid(UUID.randomUUID().toString());
//
//            // Created new application release entry
//            ApplicationReleaseDTO release = applicationManager.createRelease(appId, applicationRelease);
//            if (release != null) {
//                return Response.status(Response.Status.CREATED).entity(release).build();
//            } else {
//                log.error("ApplicationDTO Creation Failed");
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//            }
//        } catch (ApplicationManagementException e) {
//            String msg = "Error occurred while creating the application";
//            log.error(msg, e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
//        } catch (ResourceManagementException e) {
//            String msg = "Error occurred while uploading the releases artifacts of the application ID: " + appId;
//            log.error(msg, e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
//        } catch (IOException e) {
//            String msg = "Error while uploading binary file and resources for the application release of the "
//                    + "application ID: " + appId;
//            log.error(msg, e);
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
//        } catch (RequestValidatingException e) {
//            String msg = "Error occurred while handling the application creating request";
//            log.error(msg, e);
//            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
//        }
//    }

    @Override
    @PUT
    @Consumes("multipart/mixed")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/image-artifacts/{appId}/{uuid}")
    public Response updateApplicationImageArtifacts(
            @PathParam("appId") int appId,
            @PathParam("uuid") String applicationReleaseUuid,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        try {
            InputStream iconFileStream = null;
            InputStream bannerFileStream = null;
            List<InputStream> attachments = new ArrayList<>();

            if (iconFile != null) {
                iconFileStream = iconFile.getDataHandler().getInputStream();
            }
            if (bannerFile != null) {
                bannerFileStream = bannerFile.getDataHandler().getInputStream();
            }
            attachments.add(screenshot1.getDataHandler().getInputStream());
            if (screenshot2 != null) {
                attachments.add(screenshot2.getDataHandler().getInputStream());
            }
            if (screenshot3 != null) {
                attachments.add(screenshot3.getDataHandler().getInputStream());
            }
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            applicationManager
                    .updateApplicationImageArtifact(appId, applicationReleaseUuid, iconFileStream, bannerFileStream,
                            attachments);
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application " + applicationReleaseUuid).build();
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ValidationException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the application image artifacts for app id: " + appId
                    + " application release uuid: " + applicationReleaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (IOException e) {
            String msg =
                    "Exception while trying to read icon, banner files for the application " + applicationReleaseUuid;
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Consumes("multipart/mixed")
    @Path("/app-artifacts/{deviceType}/{appType}/{appId}/{uuid}")
    public Response updateApplicationArtifact(
            @PathParam("deviceType") String deviceType,
            @PathParam("appType") String appType,
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationReleaseUuid,
            @Multipart("binaryFile") Attachment binaryFile) {

        try {
            if (binaryFile == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("binary file is NULL, hence invalid request to update application release artifact for application release UUID: "
                                + applicationReleaseUuid).build();
            }
            if (!ApplicationType.ENTERPRISE.toString().equals(appType)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("If ApplicationDTO type is " + appType
                        + ", therefore you don't have application release artifact to update for application release UUID: "
                        + applicationReleaseUuid).build();
            }
            APIUtil.getApplicationManager().updateApplicationArtifact(applicationId, deviceType, applicationReleaseUuid,
                    binaryFile.getDataHandler().getInputStream());
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application release. UUID is "
                            + applicationReleaseUuid).build();
        } catch (IOException e) {
            String msg = "Error occurred while trying to read icon, banner files for the application release"
                    + applicationReleaseUuid;
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ValidationException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
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
            @Valid ApplicationDTO application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            application = applicationManager.updateApplication(applicationId, application);
        } catch (NotFoundException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (ForbiddenException e) {
            log.error(e.getMessage());
            return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while modifying the application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
        return Response.status(Response.Status.OK).entity(application).build();
    }

    @Override
    @PUT
    @Path("/{deviceType}/{appId}/{uuid}")
    public Response updateApplicationRelease(
            @PathParam("deviceType") String deviceType,
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") ApplicationReleaseDTO applicationRelease,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        InputStream iconFileStream;
        InputStream bannerFileStream;
        InputStream binaryFileStram;
        List<InputStream> attachments = new ArrayList<>();
        List<Attachment> attachmentList = new ArrayList<>();
        if (screenshot1 != null){
            attachmentList.add(screenshot1);
        }
        if (screenshot2 != null) {
            attachmentList.add(screenshot2);
        }
        if (screenshot3 != null) {
            attachmentList.add(screenshot3);
        }
        if (iconFile == null || bannerFile == null || binaryFile == null || attachmentList.isEmpty()){
            String msg = "Invalid data is received for application release updating. application id: " + applicationId
                    + " and application release UUID: " + applicationUUID;
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }

        try {
            binaryFileStram = binaryFile.getDataHandler().getInputStream();
            iconFileStream = iconFile.getDataHandler().getInputStream();
            bannerFileStream = bannerFile.getDataHandler().getInputStream();
            for (Attachment screenshot : attachmentList) {
                attachments.add(screenshot.getDataHandler().getInputStream());
            }
            boolean status = applicationManager
                    .updateRelease(applicationId, applicationUUID, deviceType, applicationRelease, binaryFileStram,
                            iconFileStream, bannerFileStream, attachments);
            if (!status){
                log.error("ApplicationDTO release updating is failed. Please contact the administrator. ApplicationDTO id: "
                        + applicationId + ", ApplicationDTO release UUID: " + applicationUUID + ", Supported device type: "
                        + deviceType);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(applicationRelease).build();
            }
            return Response.status(Response.Status.OK).entity("ApplicationDTO release is successfully updated.").build();
        } catch(BadRequestException e){
            String msg = "Invalid request to update application release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found application or application release for application id: " + applicationId
                    + " and application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to update the application release which has UUID "
                    + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        }
        catch (ApplicationManagementException e) {
            String msg = "Error while updating the application release of the application with UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (IOException e) {
            String msg = "Error while updating the release artifacts of the application with UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/{appid}")
    public Response deleteApplication(
            @PathParam("appid") int applicationId) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            List<String> storedLocations = applicationManager.deleteApplication(applicationId);
            applicationStorageManager.deleteAllApplicationReleaseArtifacts(storedLocations);
            String responseMsg = "Successfully deleted the application and application releases: " + applicationId;
            return Response.status(Response.Status.OK).entity(responseMsg).build();
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
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred while deleting the application storage: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Path("/{appid}/{uuid}")
    public Response deleteApplicationRelease(
            @PathParam("appid") int applicationId,
            @PathParam("uuid") String releaseUuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            String storedLocation = applicationManager.deleteApplicationRelease(applicationId, releaseUuid);
            applicationStorageManager.deleteApplicationReleaseArtifacts(storedLocation);
            String responseMsg = "Successfully deleted the application release of: " + applicationId + "";
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        }  catch (NotFoundException e) {
            String msg = "Couldn't found application release which is having application id: " + applicationId
                    + " and application release UUID:" + releaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg =
                    "You don't have require permission to delete the application release which has UUID " + releaseUuid
                            + " and application ID " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        }catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred while deleting the application storage: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/lifecycle/{appId}/{uuid}")
    public Response getLifecycleState(
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUuid) {
        LifecycleStateDTO lifecycleState;
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            lifecycleState = applicationManager.getLifecycleState(applicationId, applicationUuid);
            if (lifecycleState == null) {
                String msg = "Couldn't found application lifecycle details for appid: " + applicationId
                        + " and app release UUID: " + applicationUuid;
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting lifecycle state.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(lifecycleState).build();
    }

    @POST
    @Path("/lifecycle/{appId}/{uuid}")
    public Response addLifecycleState(
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUuid,
            @QueryParam("action") String action) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (action == null || action.isEmpty()) {
                String msg = "The Action is null or empty. Please check the request";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            LifecycleStateDTO state = new LifecycleStateDTO();
            state.setCurrentState(action);
            applicationManager.changeLifecycleState(applicationId, applicationUuid, state);
        } catch (NotFoundException e) {
            String msg = "Could,t find application release for application id: " + applicationId
                    + " and application release uuid: " + applicationUuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while adding lifecycle state.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.CREATED).entity("Lifecycle state added successfully.").build();
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
                String msg =
                        "Icon file name retrieving is failed.. Hence can't proceed. Please verify the " + "icon file.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            applicationArtifact.setIconName(iconFileName);
            applicationArtifact.setIconStream(iconStream);

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
            applicationArtifact.setBannername(bannerFileName);
            applicationArtifact.setBannerStream(bannerStream);

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
                            "Screenshot file name retrieving is failed for one screenshot. Hence can't proceed. Please verify the "
                                    + "screenshots.";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }
                scrrenshotData.put(screenshotrFileName, screenshotStream);
            }
            applicationArtifact.setScreenshots(scrrenshotData);
            return applicationArtifact;
        } catch (IOException e) {
            String msg = "Error occurred when reading attachment data.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        }

    }

}
