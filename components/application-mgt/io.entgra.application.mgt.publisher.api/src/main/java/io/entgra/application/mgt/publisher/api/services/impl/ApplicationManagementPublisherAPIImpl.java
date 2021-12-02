/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.publisher.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import io.entgra.application.mgt.common.*;
import io.entgra.application.mgt.common.LifecycleState;
import io.entgra.application.mgt.common.exception.LifecycleManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.response.Application;
import io.entgra.application.mgt.common.response.ApplicationRelease;
import io.entgra.application.mgt.common.response.Category;
import io.entgra.application.mgt.common.response.Tag;
import io.entgra.application.mgt.common.services.AppmDataHandler;
import io.entgra.application.mgt.common.wrapper.CustomAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.CustomAppWrapper;
import io.entgra.application.mgt.common.wrapper.EntAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import io.entgra.application.mgt.common.wrapper.ApplicationWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.PublicAppWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppReleaseWrapper;
import io.entgra.application.mgt.common.wrapper.WebAppWrapper;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.exception.ForbiddenException;
import io.entgra.application.mgt.core.exception.UnexpectedServerErrorException;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.core.util.Constants;
import io.entgra.application.mgt.publisher.api.services.ApplicationManagementPublisherAPI;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.application.mgt.core.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.activation.DataHandler;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({"application/json"})
@Path("/applications")
public class ApplicationManagementPublisherAPIImpl implements ApplicationManagementPublisherAPI {

    private static final Log log = LogFactory.getLog(ApplicationManagementPublisherAPIImpl.class);

    @POST
    @Override
    @Consumes("application/json")
    public Response getApplications(
            @Valid Filter filter) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            if (filter == null) {
                String msg = "Request Payload is null";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            ApplicationList applications = applicationManager.getApplications(filter);
            return Response.status(Response.Status.OK).entity(applications).build();
        } catch (BadRequestException e) {
            String msg = "Incompatible request payload is found. Please try with valid request payload.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (UnexpectedServerErrorException e) {
            String msg = "Error Occured when getting supported device types by Entgra IoTS";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
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
            @QueryParam("state") String state) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application application = applicationManager.getApplicationById(appId, state);
            if (application == null){
                String msg = "Could not found an active application which has Id: " + appId;
                log.error(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "ApplicationDTO with application id: " + appId + " not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch(ForbiddenException e){
            String msg = "You don't have permission to access the application. application id: " + appId;
            log.error(msg, e);
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
    public Response getApplicationByUUID(
            @PathParam("uuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            Application application = applicationManager.getApplicationByUuid(uuid);
            if (application == null){
                String msg = "Application release is in the end state of the application lifecycle flow.";
                log.error(msg);
                return Response.status(Response.Status.CONFLICT).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "Application Release with UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch(ForbiddenException e){
            String msg = "You don't have permission to access the application release. application release UUID: : "
                    + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        }
        catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application release for UUID: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Consumes({"multipart/mixed", MediaType.MULTIPART_FORM_DATA})
    @Path("/ent-app")
    public Response createEntApp(
            @Multipart("application") ApplicationWrapper applicationWrapper,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> attachmentList = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            applicationManager.validateAppCreatingRequest(applicationWrapper);
            applicationManager.validateReleaseCreatingRequest(applicationWrapper.getEntAppReleaseWrappers().get(0),
                    applicationWrapper.getDeviceType());
            applicationManager.validateBinaryArtifact(binaryFile);
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachmentList);

            // Created new Ent App
            Application application = applicationManager.createEntApp(applicationWrapper,
                    constructApplicationArtifact(binaryFile, iconFile, bannerFile, attachmentList));
            if (application != null) {
                return Response.status(Response.Status.CREATED).entity(application).build();
            } else {
                String msg = "Application creation is failed";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (BadRequestException e) {
            String msg = "Found incompatible payload with ent. app creating request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the ent. application";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Couldn't find the required artifacts to create new ent. application with the request";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Consumes({"multipart/mixed", MediaType.MULTIPART_FORM_DATA})
    @Path("/web-app")
    public Response createWebApp(
            @Multipart("webapp") WebAppWrapper webAppWrapper,
            @Multipart("icon") Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> attachmentList = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            applicationManager.validateAppCreatingRequest(webAppWrapper);
            applicationManager
                    .validateReleaseCreatingRequest(webAppWrapper.getWebAppReleaseWrappers().get(0), Constants.ANY);
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachmentList);

            // Created new Web App
            Application application = applicationManager.createWebClip(webAppWrapper,
                    constructApplicationArtifact(null, iconFile, bannerFile, attachmentList));
            if (application != null) {
                return Response.status(Response.Status.CREATED).entity(application).build();
            } else {
                String msg = "Web app creation is failed";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (BadRequestException e) {
            String msg = "Found incompatible payload with web app creating request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the web application";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Couldn't find the required artifacts to create new web application with the request";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Consumes({"multipart/mixed", MediaType.MULTIPART_FORM_DATA})
    @Path("/public-app")
    public Response createPubApp(
            @Multipart("public-app") PublicAppWrapper publicAppWrapper,
            @Multipart("icon") Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> attachmentList = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            applicationManager.validateAppCreatingRequest(publicAppWrapper);
            applicationManager.validateReleaseCreatingRequest(publicAppWrapper.getPublicAppReleaseWrappers().get(0),
                    publicAppWrapper.getDeviceType());
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachmentList);

            // Created new Public App
            Application application = applicationManager.createPublicApp(publicAppWrapper,
                    constructApplicationArtifact(null, iconFile, bannerFile, attachmentList));
            if (application != null) {
                return Response.status(Response.Status.CREATED).entity(application).build();
            } else {
                String msg = "Web app creation is failed";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (BadRequestException e) {
            String msg = "Found incompatible payload with pub app creating request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating the public app.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Couldn't find the required artifacts to create new public application with the request";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Consumes({"multipart/mixed", MediaType.MULTIPART_FORM_DATA})
    @Path("/custom-app")
    public Response createCustomApp(
            @Multipart("application") CustomAppWrapper customAppWrapper,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> attachmentList = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            applicationManager.validateAppCreatingRequest(customAppWrapper);
            applicationManager.validateReleaseCreatingRequest(customAppWrapper.getCustomAppReleaseWrappers().get(0),
                    customAppWrapper.getDeviceType());
            applicationManager.validateBinaryArtifact(binaryFile);
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachmentList);

            // Created new Custom App
            Application application = applicationManager.createCustomApp(customAppWrapper,
                    constructApplicationArtifact(binaryFile, iconFile, bannerFile, attachmentList));
            if (application != null) {
                return Response.status(Response.Status.CREATED).entity(application).build();
            } else {
                String msg = "Custom app creation is failed";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
        } catch (BadRequestException e) {
            String msg = "Found incompatible payload with custom app creating request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while creating a custom application";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (RequestValidatingException e) {
            String msg = "Couldn't find the required artifacts to create new custom application with the request";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        }
    }

    @POST
    @Consumes({"multipart/mixed", MediaType.MULTIPART_FORM_DATA})
    @Path("/{deviceType}/ent-app/{appId}")
    public Response createEntAppRelease(
            @PathParam("deviceType") String deviceType,
            @PathParam("appId") int appId,
            @Multipart("applicationRelease") EntAppReleaseWrapper entAppReleaseWrapper,
            @Multipart("binaryFile") Attachment binaryFile,
            @Multipart("icon") Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart("screenshot1") Attachment screenshot1,
            @Multipart("screenshot2") Attachment screenshot2,
            @Multipart("screenshot3") Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> attachmentList = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            applicationManager.validateReleaseCreatingRequest(entAppReleaseWrapper, deviceType);
            applicationManager.validateBinaryArtifact(binaryFile);
            applicationManager.validateImageArtifacts(iconFile, bannerFile, attachmentList);

            // Created new Ent App release
            ApplicationRelease release = applicationManager.createEntAppRelease(appId, entAppReleaseWrapper,
                    constructApplicationArtifact(binaryFile, iconFile, bannerFile, attachmentList));
            if (release != null) {
                return Response.status(Response.Status.CREATED).entity(release).build();
            } else {
                log.error("ApplicationDTO Creation Failed");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (BadRequestException e) {
            String msg = "Found incompatible payload with enterprise app release creating request.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
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
    @GET
    @Path("/device-type/{deviceType}/app-name")
    public Response isExistingApplication(
            @PathParam("deviceType") String deviceType,
            @QueryParam("appName") String appName){
        try {
            if (appName == null) {
                String msg = "Invalid app name, appName query param cannot be empty/null.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            if (applicationManager.isExistingAppName(appName, deviceType)) {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.status(Response.Status.OK).build();
        } catch (BadRequestException e) {
            log.error("Found invalid device type to check application existence.", e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (ApplicationManagementException e) {
            log.error("Internal Error occurred while checking the application existence.", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @PUT
    @Consumes({"multipart/mixed", MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/image-artifacts/{uuid}")
    public Response updateApplicationImageArtifacts(
            @PathParam("uuid") String applicationReleaseUuid,
            @Multipart(value = "icon", required = false) Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart(value = "screenshot1", required = false) Attachment screenshot1,
            @Multipart(value = "screenshot2", required = false) Attachment screenshot2,
            @Multipart(value = "screenshot3", required = false) Attachment screenshot3) {
        try {
            List<Attachment> attachments = constructAttachmentList(screenshot1, screenshot2, screenshot3);
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            applicationManager.updateApplicationImageArtifact(applicationReleaseUuid,
                    constructApplicationArtifact(null, iconFile, bannerFile, attachments));
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application " + applicationReleaseUuid).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found an application release which has application release UUID "
                    + applicationReleaseUuid + ". Hence please verify the application release UUID again and execute "
                    + "the operation";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the application image artifacts for application release uuid: "
                    + applicationReleaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Consumes({"multipart/mixed", MediaType.MULTIPART_FORM_DATA})
    @Path("/ent-app-artifact/{deviceType}/{uuid}")
    public Response updateApplicationArtifact(
            @PathParam("deviceType") String deviceType,
            @PathParam("uuid") String applicationReleaseUuid,
            @Multipart("binaryFile") Attachment binaryFile) {

        try {
            ApplicationManager applicationManager = APIUtil.getApplicationManager();
            applicationManager.validateBinaryArtifact(binaryFile);
            applicationManager.updateApplicationArtifact(deviceType, applicationReleaseUuid,
                    constructApplicationArtifact(binaryFile, null, null, null));
            return Response.status(Response.Status.OK)
                    .entity("Successfully uploaded artifacts for the application release. UUID is "
                            + applicationReleaseUuid).build();
        } catch (RequestValidatingException e) {
            String msg =
                    "Couldn't find the binary file with the request. Hence invoke the API with updating application"
                            + " artifact";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application which has application release UUID: " + applicationReleaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Found an invalid device type: " + deviceType + " with the request";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the image artifacts of the application with the uuid "
                    + applicationReleaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
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
            Application application = applicationManager.updateApplication(applicationId, applicationUpdateWrapper);
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't find an application for application id: " + applicationId;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Error occurred while modifying the application. Found bad request payload for updating the "
                    + "application";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Internal Error occurred while modifying the application.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("/ent-app-release/{uuid}")
    public Response updateEntAppRelease(
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") EntAppReleaseWrapper entAppReleaseWrapper,
            @Multipart(value = "binaryFile", required = false) Attachment binaryFile,
            @Multipart(value = "icon", required = false) Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart(value = "screenshot1", required = false) Attachment screenshot1,
            @Multipart(value = "screenshot2", required = false) Attachment screenshot2,
            @Multipart(value = "screenshot3", required = false) Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> screenshots = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            ApplicationRelease applicationRelease = applicationManager
                    .updateEntAppRelease(applicationUUID, entAppReleaseWrapper,
                            constructApplicationArtifact(binaryFile, iconFile, bannerFile, screenshots));
            if (applicationRelease == null) {
                String msg ="Ent app release updating is failed. Please contact the administrator. Application release "
                        + "UUID: " + applicationUUID;
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (BadRequestException e) {
            String msg =
                    "Invalid request to update ent app release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg =
                    "Couldn't found an ent app or ent app release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to update the ent app release which has UUID "
                    + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the ent app release which has UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("/public-app-release/{uuid}")
    public Response updatePubAppRelease(
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") PublicAppReleaseWrapper publicAppReleaseWrapper,
            @Multipart(value = "icon", required = false) Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart(value = "screenshot1", required = false) Attachment screenshot1,
            @Multipart(value = "screenshot2", required = false) Attachment screenshot2,
            @Multipart(value = "screenshot3", required = false) Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> screenshots = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            ApplicationRelease applicationRelease = applicationManager
                    .updatePubAppRelease(applicationUUID, publicAppReleaseWrapper,
                            constructApplicationArtifact(null, iconFile, bannerFile, screenshots));
            if (applicationRelease == null) {
                String msg ="Public app release updating is failed. Please contact the administrator. "
                        + "Application release UUID: " + applicationUUID + ", Supported device type:";
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (BadRequestException e) {
            String msg = "Invalid request to update public app release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found public app or public app release for application release UUID "
                    + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to update the public app release which has UUID "
                    + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the public app release which has UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("/web-app-release/{uuid}")
    public Response updateWebAppRelease(
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") WebAppReleaseWrapper webAppReleaseWrapper,
            @Multipart(value = "icon", required = false) Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart(value = "screenshot1", required = false) Attachment screenshot1,
            @Multipart(value = "screenshot2", required = false) Attachment screenshot2,
            @Multipart(value = "screenshot3", required = false) Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> screenshots = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            ApplicationRelease applicationRelease = applicationManager
                    .updateWebAppRelease(applicationUUID, webAppReleaseWrapper,
                            constructApplicationArtifact(null, iconFile, bannerFile, screenshots));
            if (applicationRelease == null) {
                String msg ="web app  release updating is failed. Please contact the administrator. Application "
                        + "release UUID: " + applicationUUID;
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (BadRequestException e) {
            String msg = "Invalid request to update web app release for web app release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Couldn't found web app or web app release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to update the web app release which has UUID "
                    + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the web app release which has UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    @PUT
    @Path("/custom-app-release/{uuid}")
    public Response updateCustomAppRelease(
            @PathParam("uuid") String applicationUUID,
            @Multipart("applicationRelease") CustomAppReleaseWrapper customAppReleaseWrapper,
            @Multipart(value = "binaryFile", required = false) Attachment binaryFile,
            @Multipart(value = "icon", required = false) Attachment iconFile,
            @Multipart(value = "banner", required = false) Attachment bannerFile,
            @Multipart(value = "screenshot1", required = false) Attachment screenshot1,
            @Multipart(value = "screenshot2", required = false) Attachment screenshot2,
            @Multipart(value = "screenshot3", required = false) Attachment screenshot3) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Attachment> screenshots = constructAttachmentList(screenshot1, screenshot2, screenshot3);
        try {
            ApplicationRelease applicationRelease = applicationManager
                    .updateCustomAppRelease(applicationUUID, customAppReleaseWrapper,
                            constructApplicationArtifact(binaryFile, iconFile, bannerFile, screenshots));
            if (applicationRelease == null) {
                String msg ="Custom app release updating is failed. Please contact the administrator. Application "
                        + "release UUID: " + applicationUUID;
                log.error(msg);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
            }
            return Response.status(Response.Status.OK).entity(applicationRelease).build();
        } catch (BadRequestException e) {
            String msg =
                    "Invalid request to update ent app release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg =
                    "Couldn't found an ent app or ent app release for application release UUID " + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = "You don't have require permission to update the ent app release which has UUID "
                    + applicationUUID;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while updating the ent app release which has UUID " + applicationUUID;
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
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting lifecycle states for application release UUID: " + releaseUuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/life-cycle/{uuid}")
    public Response addLifecycleState(
            @PathParam("uuid") String applicationUuid,
            @Valid LifecycleChanger lifecycleChanger) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            ApplicationRelease applicationRelease = applicationManager
                    .changeLifecycleState(applicationUuid, lifecycleChanger);
            return Response.status(Response.Status.CREATED).entity(applicationRelease).build();
        } catch (BadRequestException e) {
            String msg = "Request payload contains invalid data, hence verify the request payload.";
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (ForbiddenException e) {
            String msg = "You are trying to move the application release into  incompatible state for application "
                    + "which has application ID: " + applicationUuid;
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).build();
        } catch (NotFoundException e) {
            String msg = "Could,t find application release for application release uuid: " + applicationUuid;
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while adding lifecycle state.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Consumes("application/json")
    @Path("/tags")
    public Response getTags() {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            List<Tag> tags = applicationManager.getRegisteredTags();
            return Response.status(Response.Status.OK).entity(tags).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while getting registered tags.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Consumes(MediaType.WILDCARD)
    @Path("/{appId}/tags/{tagName}")
    public Response deleteApplicationTag(
            @PathParam("appId") int appId,
            @PathParam("tagName") String tagName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteApplicationTag(appId, tagName);
            String msg = "Tag " + tagName + " is deleted successfully.";
            return Response.status(Response.Status.OK).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while deleting registered tag.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @DELETE
    @Override
    @Consumes(MediaType.WILDCARD)
    @Path("/tags/{tagName}")
    public Response deleteUnusedTag(
            @PathParam("tagName") String tagName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.deleteUnusedTag(tagName);
            String msg = "Tag " + tagName + " is deleted successfully.";
            return Response.status(Response.Status.OK).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ForbiddenException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while deleting unused tag.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @PUT
    @Override
    @Consumes("application/json")
    @Path("/tags/rename")
    public Response modifyTagName(
            @QueryParam("from") String oldTagName,
            @QueryParam("to") String newTagName) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            applicationManager.updateTag(oldTagName, newTagName);
            String msg = "Tag " + oldTagName + " is updated to " + newTagName + " successfully.";
            return Response.status(Response.Status.OK).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while updating registered tag.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    @Consumes("application/json")
    @Path("/tags")
    public Response addTags(
            List<String> tagNames) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            List<String> tags = applicationManager.addTags(tagNames);
            return Response.status(Response.Status.OK).entity(tags).build();
        } catch (BadRequestException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while adding new tag.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    @Consumes("application/json")
    @Path("/{appId}/tags")
    public Response addApplicationTags(
            @PathParam("appId") int appId,
            List<String> tagNames) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            List<String> applicationTags = applicationManager.addApplicationTags(appId, tagNames);
            return Response.status(Response.Status.OK).entity(applicationTags).build();
        } catch (NotFoundException e) {
            String msg = e.getMessage();
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while adding new tags for application which has application ID: " + appId + ".";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Override
    @Consumes("application/json")
    @Path("/categories")
    public Response getCategories() {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            List<Category> categories = applicationManager.getRegisteredCategories();
            return Response.status(Response.Status.OK).entity(categories).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error Occurred while getting registered categories.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /***
     * Construct the screenshot list by evaluating the availability of each screenshot.
     *
     * @param screenshot1 First Screenshot
     * @param screenshot2 Second Screenshot
     * @param screenshot3 Third Screenshot
     * @return List of {@link Attachment}
     */
    private List<Attachment> constructAttachmentList(Attachment screenshot1, Attachment screenshot2,
            Attachment screenshot3) {
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
        return attachments;
    }

    /***
     * This method can be used to construct {@link ApplicationArtifact}
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
                applicationArtifact.setInstallerName(installerFileName.replaceAll("\\s", ""));
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

            if (attachmentList != null && !attachmentList.isEmpty()) {
                Map<String, InputStream> screenshotData = new TreeMap<>();
                for (Attachment sc : attachmentList) {
                    dataHandler = sc.getDataHandler();
                    String screenshotFileName = dataHandler.getName();
                    InputStream screenshotStream = dataHandler.getInputStream();
                    if (screenshotStream == null) {
                        String msg =
                                "Stream of one of the application release screenshot is null. Hence can't proceed. Please "
                                        + "verify the uploaded screenshots.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    if (screenshotFileName == null) {
                        String msg =
                                "Screenshot file name retrieving is failed for one screenshot. Hence can't proceed. "
                                        + "Please verify the screenshots.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    screenshotData.put(screenshotFileName, screenshotStream);
                }
                applicationArtifact.setScreenshots(screenshotData);
            }
            return applicationArtifact;
        } catch (IOException e) {
            String msg = "Error occurred when reading attachment data.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg);
        }
    }
}
