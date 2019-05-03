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

package org.wso2.carbon.device.application.mgt.api.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.api.services.ConfigRetrieveAPI;
import org.wso2.carbon.device.application.mgt.common.config.LifecycleStateVertex;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.common.services.AppmDataHandler;
import org.wso2.carbon.device.application.mgt.core.util.APIUtil;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of ApplicationDTO Management related APIs.
 */
@Produces({"application/json"})
@Path("/config")
public class ConfigRetrieveAPIImpl implements ConfigRetrieveAPI {

    private static Log log = LogFactory.getLog(ConfigRetrieveAPIImpl.class);

    @GET
    @Override
    @Consumes("application/json")
    @Path("/ui-config")
    public Response getUiConfig() {
        AppmDataHandler dataHandler = APIUtil.getDataHandler();
        UIConfiguration uiConfiguration = dataHandler.getUIConfiguration();
        return Response.status(Response.Status.OK).entity(uiConfiguration).build();
    }

    @GET
    @Override
    @Consumes("application/json")
    @Path("/lifecycle-config")
    public Response getLifecycleConfig() {
        AppmDataHandler dataHandler = APIUtil.getDataHandler();
        Map<String, List<LifecycleStateVertex>> verticesObject = new HashMap<>();
        Map<LifecycleStateVertex, List<LifecycleStateVertex>> vertices = null;
        try {
            vertices = dataHandler.getLifecycleConfiguration().getAdjVertices();
            for (LifecycleStateVertex vt : vertices.keySet()) {
                verticesObject.put(vt.getLabel(), vertices.get(vt));
            }
            return Response.status(Response.Status.OK).entity(verticesObject).build();
        } catch (LifecycleManagementException e) {
            String msg = "Error Occurred while accessing lifecycle manager.";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
