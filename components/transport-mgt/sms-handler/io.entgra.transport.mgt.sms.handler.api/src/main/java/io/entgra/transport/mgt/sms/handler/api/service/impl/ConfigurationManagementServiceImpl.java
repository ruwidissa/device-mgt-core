/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.transport.mgt.sms.handler.api.service.impl;

import io.entgra.transport.mgt.sms.handler.api.service.ConfigurationManagementService;
import io.entgra.transport.mgt.sms.handler.core.config.SMSConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/configuration")
public class ConfigurationManagementServiceImpl implements ConfigurationManagementService {

    private static final Log log = LogFactory.getLog(ConfigurationManagementServiceImpl.class);

    @Path("/reload")
    @GET
    @Override
    public Response reloadConfiguration() {
        if (log.isDebugEnabled()) {
            log.debug("Reloading SMS Configuration in file sms-config.xml");
        }
        try {
            SMSConfigurationManager.getInstance().initConfig();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while reloading SMS Configuration";
            log.error(msg, e);
            return Response.serverError().entity(msg).build();
        }
        return Response.ok().entity("Successfully reloaded SMS Configuration in file sms-config.xml ").build();
    }
}
