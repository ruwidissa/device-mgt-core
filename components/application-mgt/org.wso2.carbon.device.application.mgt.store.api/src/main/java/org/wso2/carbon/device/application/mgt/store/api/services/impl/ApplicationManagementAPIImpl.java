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
package org.wso2.carbon.device.application.mgt.store.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.AppLifecycleState;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.ApplicationList;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.core.exception.NotFoundException;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.ApplicationManagementAPI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Implementation of Application Management related APIs.
 */
@Produces({ "application/json" })
@Path("/store/applications")
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
            filter.setAppReleaseState(AppLifecycleState.PUBLISHED.toString());
            if (appName != null && !appName.isEmpty()) {
                filter.setAppName(appName);
            }
            if (appType != null && !appType.isEmpty()) {
                filter.setAppType(appType);
            }
//            if (appCategory != null && !appCategory.isEmpty()) {
//                filter.setAppCategories(appCategory);
//            }
            ApplicationList applications = applicationManager.getApplications(filter);
            if (applications.getApplications().isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Couldn't find any application for requested query.").build();
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
    @Path("/{uuid}")
    public Response getApplication(
            @PathParam("uuid") String uuid) {
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        try {
            ApplicationDTO application = applicationManager
                    .getApplicationByUuid(uuid, AppLifecycleState.PUBLISHED.toString());
            return Response.status(Response.Status.OK).entity(application).build();
        } catch (NotFoundException e) {
            String msg = "Application with application release UUID: " + uuid + " is not found";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting application with the application release uuid: " + uuid;
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
