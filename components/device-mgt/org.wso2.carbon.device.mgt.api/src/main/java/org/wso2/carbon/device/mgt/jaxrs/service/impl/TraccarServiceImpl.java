/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.jaxrs.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.device.mgt.core.traccar.api.service.impl.DeviceAPIClientServiceImpl;
import org.wso2.carbon.device.mgt.core.util.HttpReportingUtil;
import org.wso2.carbon.device.mgt.jaxrs.service.api.TraccarService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/traccar")
@Produces(MediaType.APPLICATION_JSON)
public class TraccarServiceImpl implements TraccarService {
    private static final Log log = LogFactory.getLog(TraccarServiceImpl.class);

    @GET
    @Path("/generate-token")
    @Override
    public Response getUser(@QueryParam("userName") String userName) {
        if (HttpReportingUtil.isTrackerEnabled()) {
            JSONObject obj = new JSONObject(DeviceAPIClientServiceImpl.returnUser(userName));

            log.info("=================");
            log.info(obj.toString());
            log.info("==================");
            if(obj.has("error")){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(obj.getString("error")).build();
            }else{
                return Response.status(Response.Status.OK).entity(obj.getString("token")).build();
            }
        }else{
            return Response.status(Response.Status.BAD_REQUEST).entity("Traccar is not enabled").build();
        }
    }
}
