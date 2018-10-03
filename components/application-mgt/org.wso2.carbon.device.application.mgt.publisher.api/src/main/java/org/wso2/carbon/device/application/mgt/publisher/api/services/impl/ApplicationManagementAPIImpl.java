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
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.services.ApplicationManagementAPI;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
            Filter filter = new Filter();
            filter.setOffset(offset);
            filter.setLimit(limit);
            filter.setSortBy(sortBy);
            filter.setFullMatch(isFullMatch);
            if (appName != null && !appName.isEmpty()) {
                filter.setAppName(appName);
            }
            if (appType != null && !appType.isEmpty()) {
                filter.setAppType(appType);
            }
            if (appCategory != null && !appCategory.isEmpty()) {
                filter.setAppCategory(appCategory);
            }
            if (releaseState != null && !releaseState.isEmpty()) {
                filter.setCurrentAppReleaseState(releaseState);
            }
            ApplicationList applications = applicationManager.getApplications(filter);
            if (applications.getApplications().isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity
                        ("Couldn't find any application for requested query.").build();
            }
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (ApplicationManagementException e) {
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
            Application application = applicationManager.getApplicationById(appId, state);
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "Application with application id: " + appId + " not found";
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
            @Multipart("application") Application application,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        InputStream iconFileStream;
        InputStream bannerFileStream;
        List<InputStream> attachments = new ArrayList<>();
        List<ApplicationRelease> applicationReleases = new ArrayList<>();
        ApplicationRelease applicationRelease;
        List<Attachment> attachmentList = new ArrayList<>();
        attachmentList.add(screenshot1);
        if(screenshot2 != null) {
            attachmentList.add(screenshot2);
        }
        if(screenshot3 != null) {
            attachmentList.add(screenshot3);
        }

        try {
            if (!isValidAppCreatingRequest(binaryFile, iconFile, bannerFile, attachmentList, application)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            // The application executable artifacts such as apks are uploaded.
            if (!ApplicationType.ENTERPRISE.toString().equals(application.getType())) {
                applicationRelease = application.getApplicationReleases().get(0);
                applicationRelease = applicationStorageManager
                        .uploadReleaseArtifact(applicationRelease, application.getType(), application.getDeviceType(),
                                null);
            } else {
                applicationRelease = application.getApplicationReleases().get(0);
                applicationRelease = applicationStorageManager
                        .uploadReleaseArtifact(applicationRelease, application.getType(), application.getDeviceType(),
                                binaryFile.getDataHandler().getInputStream());
                if (applicationRelease.getAppStoredLoc() == null || applicationRelease.getAppHashValue() == null) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }

            iconFileStream = iconFile.getDataHandler().getInputStream();
            bannerFileStream = bannerFile.getDataHandler().getInputStream();

            for (Attachment screenshot : attachmentList) {
                attachments.add(screenshot.getDataHandler().getInputStream());
            }

            // Upload images
            applicationRelease = applicationStorageManager.uploadImageArtifacts(applicationRelease, iconFileStream,
                    bannerFileStream, attachments);
            applicationRelease.setUuid(UUID.randomUUID().toString());
            applicationReleases.add(applicationRelease);
            application.setApplicationReleases(applicationReleases);

            // Created new application entry
            Application createdApplication = applicationManager.createApplication(application);
            if (createdApplication != null) {
                return Response.status(Response.Status.CREATED).entity(createdApplication).build();
            } else {
                log.error("Application Creation Failed");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the application";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the releases artifacts of the application "
                    + application.getName(), e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            String errorMessage =
                    "Error while uploading binary file and resources for the application release of the application "
                            + application.getName();
            log.error(errorMessage, e);
            return APIUtil.getResponse(new ApplicationManagementException(errorMessage, e),
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (RequestValidatingException e) {
            log.error("Error occurred while handling the application creating request");
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    @PUT
    @Consumes("multipart/mixed")
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/image-artifacts/{appId}/{uuid}")
    public Response updateApplicationImageArtifacts(
            @PathParam("appId") int appId,
            @PathParam("uuid") String applicationUuid,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {

        try {
            InputStream iconFileStream = null;
            InputStream bannerFileStream = null;
            List<InputStream> attachments = new ArrayList<>();;

            if (iconFile != null) {
                iconFileStream = iconFile.getDataHandler().getInputStream();
            }
            if (bannerFile != null) {
                bannerFileStream = bannerFile.getDataHandler().getInputStream();
            }

            attachments.add(screenshot1.getDataHandler().getInputStream());
            if(screenshot2 != null) {
                attachments.add(screenshot2.getDataHandler().getInputStream());
            }
            if(screenshot3 != null) {
                attachments.add(screenshot3.getDataHandler().getInputStream());
            }
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            applicationManager.updateApplicationImageArtifact(appId,
                    applicationUuid, iconFileStream, bannerFileStream, attachments);

            return Response.status(Response.Status.OK).entity("Successfully uploaded artifacts for the application "
                    + applicationUuid).build();
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the application.";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            String msg = "Exception while trying to read icon, banner files for the application " + applicationUuid;
            log.error(msg);
            return APIUtil.getResponse(new ApplicationManagementException(msg, e),
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                    + applicationUuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
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
            @PathParam("uuid") String applicationUuid,
            @Multipart("binaryFile") Attachment binaryFile) {

        try {

            if (binaryFile == null) {
                return APIUtil.getResponse("Uploading artifacts for the application is failed " + applicationUuid,
                        Response.Status.BAD_REQUEST);
            }
            APIUtil.getApplicationManager().updateApplicationArtifact(applicationId, applicationUuid,
                    binaryFile.getDataHandler().getInputStream());
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application release. UUID is " + applicationUuid).build();
        } catch (IOException e) {
            String msg =
                    "Error occurred while trying to read icon, banner files for the application release" +
                            applicationUuid;
            log.error(msg);
            return APIUtil.getResponse(new ApplicationManagementException(msg, e),
                    Response.Status.BAD_REQUEST);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                    + applicationUuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while updating the image artifacts of the application with the uuid "
                    + applicationUuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (RequestValidatingException e) {
            log.error("Error occured while handling the application artifact updating request. application release UUID:  "
                    + applicationUuid);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        } catch (DeviceManagementException e) {
            log.error("Error occurred while updating the image artifacts of the application with the uuid "
                    + applicationUuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    @PUT
    @Consumes("application/json")
    public Response updateApplication(@Valid Application application) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            application = applicationManager.updateApplication(application);
        } catch (NotFoundException e) {
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while modifying the application";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
        }
        return Response.status(Response.Status.OK).entity(application).build();
    }

    @Override
    @PUT
    @Path("/{appId}/{uuid}")
    public Response updateApplicationRelease(
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") ApplicationRelease applicationRelease,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        InputStream iconFileStream = null;
        InputStream bannerFileStream = null;
        List<InputStream> attachments = new ArrayList<>();

        try {
            Application application = applicationManager.getApplicationIfAccessible(applicationId);

            if (!applicationManager.isAcceptableAppReleaseUpdate(application.getId(),
                                                                         applicationRelease.getUuid())) {
                String msg = "Application release is in the " + applicationRelease.getLifecycleState().getCurrentState()
                        + " state. Hence updating is not acceptable when application in this state";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            if (binaryFile != null) {
                applicationRelease = applicationStorageManager
                        .updateReleaseArtifacts(applicationRelease, application.getType(), application.getDeviceType(),
                                binaryFile.getDataHandler().getInputStream());
            }
            if (iconFile != null) {
                iconFileStream = iconFile.getDataHandler().getInputStream();
            }
            if (bannerFile != null) {
                bannerFileStream = bannerFile.getDataHandler().getInputStream();
            }
            if (!attachmentList.isEmpty()) {
                for (Attachment screenshot : attachmentList) {
                    attachments.add(screenshot.getDataHandler().getInputStream());
                }
            }

//            applicationRelease = applicationStorageManager
//                    .updateImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, attachments);
//            applicationRelease = applicationManager.updateRelease(applicationId, applicationRelease);

            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (ApplicationManagementException e) {
            log.error("Error while updating the application release of the application with UUID " + applicationUUID);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("Error while updating the release artifacts of the application with UUID " + applicationUUID);
            return APIUtil.getResponse(new ApplicationManagementException(
                            "Error while updating the release artifacts of the application with UUID " + applicationUUID),
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while updating the releases artifacts of the application with the uuid "
                    + applicationUUID + " for the release " + applicationRelease.getVersion(), e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (RequestValidatingException e) {
            log.error("Error occured while handling the application release updating request. application release UUID:  "
                    + applicationUUID);
            return APIUtil.getResponse(e, Response.Status.BAD_REQUEST);
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
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + applicationId;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred while deleting the application storage: " + applicationId;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
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
            String storedLocation = applicationManager.deleteApplicationRelease(applicationId, releaseUuid, true);
            applicationStorageManager.deleteApplicationReleaseArtifacts(storedLocation);
            String responseMsg = "Successfully deleted the application release of: " + applicationId + "";
            return Response.status(Response.Status.OK).entity(responseMsg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting the application: " + applicationId;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationStorageManagementException e) {
            String msg = "Error occurred while deleting the application storage: " + applicationId;
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/lifecycle/{appId}/{uuid}")
    public Response getLifecycleState(
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUuid) {
        LifecycleState lifecycleState;
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            lifecycleState = applicationManager.getLifecycleState(applicationId, applicationUuid);
        } catch (NotFoundException e){
            String msg = "Couldn't found application lifecycle details for appid: " + applicationId
                    + " and app release UUID: " + applicationUuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        catch (ApplicationManagementException e) {
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
            LifecycleState state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.changeLifecycleState(applicationId, applicationUuid, state, true);
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

    private boolean isValidAppCreatingRequest(Attachment binaryFile, Attachment iconFile, Attachment bannerFile,
            List<Attachment> attachmentList, Application application){

        if (application.getApplicationReleases().size() > 1) {
            log.error(
                    "Invalid application creating request. Application creating request must have single application "
                            + "release.  Application name:" + application.getName() + " and type: " +
                            application.getType());
            return false;
        }

        if (iconFile == null) {
            log.error("Icon file is not found for the application release. Application name: " +
                    application.getName() + " and type: " + application.getType());
            return false;
        }

        if (bannerFile == null) {
            log.error("Banner file is not found for the application release. Application name: " +
                    application.getName() + " and application type: " + application.getType());
            return false;
        }

        if (attachmentList == null || attachmentList.isEmpty()) {
            log.error("Screenshots are not found for the application release. Application name: " +
                    application.getName() + " Application type: " + application.getType());
            return false;
        }

        if (binaryFile == null && ApplicationType.ENTERPRISE.toString().equals(application.getType())) {
            log.error("Binary file is not found for the application release. Application name: "
                    + application.getName() + " Application type: " + application.getType());
            return false;
        }
        return true;
    }
}
