/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.admin;

import io.entgra.device.mgt.core.cea.mgt.common.bean.AccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServerUIConfiguration;
import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.GracePeriod;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.DefaultAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.EmailOutlookAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.GraceAllowedPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.POPIMAPAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.WebOutlookAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ui.CEAPolicyUIConfiguration;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ui.ServerUIConfiguration;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAManagementException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyAlreadyExistsException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyNotFoundException;
import io.entgra.device.mgt.core.cea.mgt.common.service.CEAManagementService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.AccessPolicyWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.CEAPolicyWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans.GracePeriodWrapper;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.admin.CEAManagementAdminService;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl.util.RequestValidationUtil;
import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.DeviceMgtAPIUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Path("/admin/cea-policies")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CEAManagementAdminServiceImpl implements CEAManagementAdminService {
    private static final Log log = LogFactory.getLog(CEAManagementAdminServiceImpl.class);

    @GET
    @Path("/ui")
    @Override
    public Response getCEAPolicyUI() {
        CEAManagementService ceaManagementService = DeviceMgtAPIUtils.getCEAManagementService();
        try {
            CEAPolicyUIConfiguration ceaPolicyUIConfiguration = ceaManagementService.getCEAPolicyUIConfiguration();
            if (ceaPolicyUIConfiguration == null) {
                return Response.status(HttpStatus.SC_NOT_FOUND).entity("UI configurations not found").build();
            }
            return Response.status(HttpStatus.SC_OK).entity(ceaPolicyUIConfiguration).build();
        } catch (CEAManagementException e) {
            String msg = "Error occurred while retrieving CEA ui configs";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @POST
    @Override
    public Response createCEAPolicy(CEAPolicyWrapper ceaPolicyWrapper) {
        try {
            RequestValidationUtil.validateCEAPolicy(ceaPolicyWrapper);
            CEAManagementService ceaManagementService = DeviceMgtAPIUtils.getCEAManagementService();
            CEAPolicy ceaPolicy = constructCEAPolicy(ceaPolicyWrapper);
            ceaPolicy = ceaManagementService.createCEAPolicy(ceaPolicy);
            return Response.status(HttpStatus.SC_CREATED).entity(ceaPolicy).build();
        } catch (CEAPolicyAlreadyExistsException e) {
            String msg = "CEA policy already exists for the tenant";
            log.warn(msg);
            return Response.status(HttpStatus.SC_CONFLICT).entity(msg).build();
        } catch (CEAManagementException e) {
            String msg = "Error occurred while creating CEA policy";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response retrieveCEAPolicy() {
        try {
            CEAManagementService ceaManagementService = DeviceMgtAPIUtils.getCEAManagementService();
            CEAPolicy ceaPolicy = ceaManagementService.retrieveCEAPolicy();
            if (ceaPolicy == null) {
                return Response.status(HttpStatus.SC_NOT_FOUND).entity("CEA policy isn't exists in the tenant").build();
            }
            return Response.status(HttpStatus.SC_OK).entity(ceaPolicy).build();
        } catch (CEAManagementException e) {
            String msg = "Error occurred while retrieving CEA policy";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response deleteCEAPolicy() {
        try {
            CEAManagementService ceaManagementService = DeviceMgtAPIUtils.getCEAManagementService();
            ceaManagementService.deleteCEAPolicy();
            return Response.status(HttpStatus.SC_OK).build();
        } catch (CEAPolicyNotFoundException e) {
            String msg = "CEA policy isn't exists in the tenant";
            log.warn(msg);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (CEAManagementException e) {
            String msg = "Error occurred while deleting CEA policy";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response updateCEAPolicy(CEAPolicyWrapper ceaPolicyWrapper) {
        try {
            RequestValidationUtil.validateCEAPolicy(ceaPolicyWrapper);
            CEAManagementService ceaManagementService = DeviceMgtAPIUtils.getCEAManagementService();
            CEAPolicy ceaPolicy = constructCEAPolicy(ceaPolicyWrapper);
            ceaPolicy = ceaManagementService.updateCEAPolicy(ceaPolicy);
            return Response.status(HttpStatus.SC_CREATED).entity(ceaPolicy).build();
        } catch (CEAPolicyNotFoundException e) {
            String msg = "CEA policy isn't exists in the tenant";
            log.warn(msg);
            return Response.status(HttpStatus.SC_NOT_FOUND).entity(msg).build();
        } catch (CEAManagementException e) {
            String msg = "Error occurred while updating CEA policy";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @GET
    @Path("/sync-now")
    @Override
    public Response sync() {
        CEAManagementService ceaManagementService = DeviceMgtAPIUtils.getCEAManagementService();
        try {
            ceaManagementService.syncNow();
            return Response.status(HttpStatus.SC_OK).build();
        } catch (CEAManagementException e) {
            String msg = "Error occurred while trigger syncing";
            log.error(msg, e);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    /**
     * Construct {@link CEAPolicy} from {@link CEAPolicyWrapper}
     * @param ceaPolicyWrapper {@link CEAPolicyWrapper}
     * @return {@link CEAPolicy}
     */
    private CEAPolicy constructCEAPolicy(CEAPolicyWrapper ceaPolicyWrapper) {
        AccessPolicyWrapper accessPolicyWrapper = ceaPolicyWrapper.getConditionalAccessPolicyEntries();
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setDefaultAccessPolicy(Enum.valueOf(DefaultAccessPolicy.class,
                accessPolicyWrapper.getDefaultAccessPolicy()));
        accessPolicy.setPOPIMAPAccessPolicy(Enum.valueOf(POPIMAPAccessPolicy.class,
                accessPolicyWrapper.getPOPIMAPAccessPolicy()));
        accessPolicy.setWebOutlookAccessPolicy(Enum.valueOf(WebOutlookAccessPolicy.class,
                accessPolicyWrapper.getWebOutlookAccessPolicy()));
        Set<EmailOutlookAccessPolicy> emailOutlookAccessPolicy = new HashSet<>();
        for (String value : ceaPolicyWrapper.getConditionalAccessPolicyEntries().getEmailOutlookAccessPolicy()) {
            emailOutlookAccessPolicy.add(Enum.valueOf(EmailOutlookAccessPolicy.class, value));
        }
        accessPolicy.setEmailOutlookAccessPolicy(emailOutlookAccessPolicy);
        GracePeriodWrapper gracePeriodWrapper = ceaPolicyWrapper.getGracePeriodEntries();
        GracePeriod gracePeriod = new GracePeriod();
        gracePeriod.setGracePeriod(gracePeriodWrapper.getGracePeriod());
        gracePeriod.setGraceAllowedPolicy(Enum.valueOf(GraceAllowedPolicy.class,
                gracePeriodWrapper.getGraceAllowedPolicy()));
        ActiveSyncServer activeSyncServer = ceaPolicyWrapper.getActiveSyncServerEntries();
        CEAPolicy ceaPolicy = new CEAPolicy();
        ceaPolicy.setAccessPolicy(accessPolicy);
        ceaPolicy.setGracePeriod(gracePeriod);
        ceaPolicy.setActiveSyncServer(activeSyncServer);
        return ceaPolicy;
    }
}
