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
import org.wso2.carbon.device.application.mgt.publisher.api.APIUtil;
import org.wso2.carbon.device.application.mgt.publisher.api.services.ApplicationManagementAPI;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationStorageManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/publisher/applications")
public class ApplicationManagementAPIImpl implements ApplicationManagementAPI {

    private static Log log = LogFactory.getLog(ApplicationManagementAPIImpl.class);

    @GET
    @Override
    @Consumes("application/json")
    public Response getApplications(@Valid Filter filter) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();

        try {
            ApplicationList applications = applicationManager.getApplications(filter);
            if (applications.getApplications().isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity
                        ("Couldn't find any application for requested query.").build();
            }
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting the application list for publisher ";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Consumes("application/json")
    @Path("/{appType}")
    public Response getApplication(
            @PathParam("appType") String appType,
            @QueryParam("appName") String appName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application application = applicationManager.getApplication(appType, appName);
            if (application == null) {
                return Response.status(Response.Status.NOT_FOUND).entity
                        ("Application with application type: " + appType + " not found").build();
            }

            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while getting application with the uuid " + appType, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Consumes("application/json")
    public Response createApplication(
            @Valid Application application,
            @Valid ApplicationRelease applicationRelease,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        InputStream iconFileStream;
        InputStream bannerFileStream;
        List<InputStream> attachments = new ArrayList<>();
        List<ApplicationRelease> applicationReleases = new ArrayList<>();
        try {
            if (iconFile == null) {
                log.error("Icon file is not uploaded for the application release of " + application.getName() +
                        " of application type " + application.getType());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (bannerFile == null) {
                log.error("Banner file is not uploaded for the application release of " + application.getName() +
                        " of application type " + application.getType());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (attachmentList == null || attachmentList.isEmpty()) {
                log.error("Screenshots are not uploaded for the application release of " + application.getName() +
                        " of application type " + application.getType());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (binaryFile == null) {
                log.error("Binary file is not uploaded for the application release of " + application.getName() +
                        " of application type " + application.getType());
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            iconFileStream = iconFile.getDataHandler().getInputStream();
            bannerFileStream = bannerFile.getDataHandler().getInputStream();

            for (Attachment screenshot : attachmentList) {
                attachments.add(screenshot.getDataHandler().getInputStream());
            }

            applicationRelease = applicationStorageManager.uploadReleaseArtifact(applicationRelease, application.getType(),
                    binaryFile.getDataHandler().getInputStream());

            if (applicationRelease.getAppStoredLoc() == null || applicationRelease.getAppHashValue() == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
            applicationRelease = applicationStorageManager.uploadImageArtifacts(applicationRelease, iconFileStream,
                    bannerFileStream, attachments);

            applicationRelease.setUuid(UUID.randomUUID().toString());
            applicationReleases.add(applicationRelease);
            application.setApplicationReleases(applicationReleases);
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
        }
    }

    @Override
    @PUT
    @Path("/image-artifacts/{appId}/{uuid}")
    public Response updateApplicationImageArtifacts(
            @PathParam("appId") int appId,
            @PathParam("uuid") String applicationUuid,
            @Multipart("icon") Attachment iconFile,
            @Multipart("banner") Attachment bannerFile,
            @Multipart("screenshot") List<Attachment> attachmentList) {

        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationRelease applicationRelease;

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
            if (attachmentList != null && !attachmentList.isEmpty()) {
                for (Attachment screenshot : attachmentList) {
                    attachments.add(screenshot.getDataHandler().getInputStream());
                }
            }
            applicationRelease = applicationManager.validateApplicationRelease(applicationUuid);
            LifecycleState lifecycleState = applicationManager.getLifecycleState(appId, applicationRelease.getUuid());
            if (AppLifecycleState.PUBLISHED.toString().equals(lifecycleState.getCurrentState()) ||
                    AppLifecycleState.DEPRECATED.toString().equals(lifecycleState.getCurrentState())) {
                return Response.status(Response.Status.FORBIDDEN).entity("Can't Update the application release in " +
                        "PUBLISHED or DEPRECATED state. Hence please demote the application and update the application " +
                        "release").build();
            }
            applicationRelease = applicationStorageManager
                    .updateImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, attachments);
            //todo need to implement updateRelease method
            applicationManager.updateRelease(appId, applicationRelease);
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application " + applicationUuid).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found application release details and storage details";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.NOT_FOUND);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the application";
            log.error(msg, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("Exception while trying to read icon, banner files for the application " + applicationUuid);
            return APIUtil.getResponse(new ApplicationManagementException(
                            "Exception while trying to read icon, " + "banner files for the application " + applicationUuid, e),
                    Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                    + applicationUuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @PUT
    @Path("/app-artifacts/{appType}/{appId}/{uuid}")
    public Response updateApplicationArtifact(
            @PathParam("appType") String appType,
            @PathParam("appId") int applicationId,
            @PathParam("uuid") String applicationUuuid,
            @Multipart("binaryFile") Attachment binaryFile) {
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationRelease applicationRelease;

        try {

            if (binaryFile == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Uploading artifacts for the application is failed " + applicationUuuid).build();
            }
            applicationRelease = applicationManager.validateApplicationRelease(applicationUuuid);
            applicationRelease = applicationStorageManager.updateReleaseArtifacts(applicationRelease, appType,
                    binaryFile.getDataHandler().getInputStream());
            //todo
            applicationManager.updateRelease(applicationId, applicationRelease);
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application " + applicationUuuid).build();
        } catch (IOException e) {
            log.error("Exception while trying to read icon, banner files for the application " + applicationUuuid);
            return APIUtil.getResponse(new ApplicationManagementException(
                            "Exception while trying to read icon, banner files for the application " + applicationUuuid, e),
                    Response.Status.BAD_REQUEST);
        } catch (ResourceManagementException e) {
            log.error("Error occurred while uploading the image artifacts of the application with the uuid "
                    + applicationUuuid, e);
            return APIUtil.getResponse(e, Response.Status.INTERNAL_SERVER_ERROR);
        } catch (ApplicationManagementException e) {
            log.error("Error occurred while updating the image artifacts of the application with the uuid "
                    + applicationUuuid, e);
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
            Application application = applicationManager.validateApplication(applicationId);

            if (binaryFile != null) {
                applicationRelease = applicationStorageManager.updateReleaseArtifacts(applicationRelease,
                        application.getType(), binaryFile.getDataHandler().getInputStream());
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

            applicationRelease = applicationStorageManager
                    .updateImageArtifacts(applicationRelease, iconFileStream, bannerFileStream, attachments);

            //todo
            applicationRelease = applicationManager.updateRelease(applicationId, applicationRelease);

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
        }
    }

    @DELETE
    @Path("/{appid}")
    public Response deleteApplication(@PathParam("appid") int applicationId) {
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
    public Response deleteApplicationRelease(@PathParam("appid") int applicationId, @PathParam("uuid") String releaseUuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        ApplicationStorageManager applicationStorageManager = APIUtil.getApplicationStorageManager();
        try {
            String storedLocation = applicationManager.deleteApplicationRelease(applicationId, releaseUuid);
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
            LifecycleState state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.addLifecycleState(applicationId, applicationUuid, state);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while adding lifecycle state.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.CREATED).entity("Lifecycle state added successfully.").build();
    }

}
