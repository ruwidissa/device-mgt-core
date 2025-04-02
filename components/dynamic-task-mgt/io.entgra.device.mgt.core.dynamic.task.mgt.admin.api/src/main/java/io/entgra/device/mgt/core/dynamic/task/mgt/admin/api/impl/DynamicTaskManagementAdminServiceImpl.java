/*
 *   Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.admin.api.impl;

import io.entgra.device.mgt.core.dynamic.task.mgt.admin.api.spi.DynamicTaskManagementAdminService;
import io.entgra.device.mgt.core.dynamic.task.mgt.admin.api.util.APIUtils;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.BadRequestException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.ForbiddenException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DynamicTaskManagementAdminServiceImpl implements DynamicTaskManagementAdminService {
    private static final Log log = LogFactory.getLog(DynamicTaskManagementAdminServiceImpl.class);

    /**
     * Validate incoming configuration payload against the existing configurations.
     *
     * @param incomingConfigurations List of {@link CategorizedDynamicTask} containing updated configurations.
     * @param existingConfigurations List of {@link CategorizedDynamicTask} existing configurations.
     * @throws BadRequestException Throws when contains any invalid configuration settings.
     */
    private static void validatePayload(List<CategorizedDynamicTask> incomingConfigurations,
                                        List<CategorizedDynamicTask> existingConfigurations) throws BadRequestException {
        for (CategorizedDynamicTask incomingCategorizedDynamicTask : incomingConfigurations) {

            if (StringUtils.isBlank(incomingCategorizedDynamicTask.getCategoryCode())) {
                String msg = "Encountered an invalid setting for categorized dynamic task frequency. " +
                        "categoryCode can not be [" + incomingCategorizedDynamicTask.getFrequency() + "].";
                log.error(msg);
                throw new BadRequestException(msg);
            }

            if (existingConfigurations.contains(incomingCategorizedDynamicTask)) {
                if (incomingCategorizedDynamicTask.getDeviceTypes() == null) {
                    String msg = "Encountered an invalid setting for categorized dynamic task frequency. " +
                            "deviceTypes can not be [" + incomingCategorizedDynamicTask.getFrequency() + "].";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }

                if (incomingCategorizedDynamicTask.getFrequency() < 0) {
                    String msg = "Encountered an invalid setting for categorized dynamic task frequency. " +
                            "Frequency can not be [" + incomingCategorizedDynamicTask.getFrequency() + "].";
                    log.error(msg);
                    throw new BadRequestException(msg);
                }

                Set<String> configurableDeviceTypes =
                        existingConfigurations.get(existingConfigurations.indexOf(incomingCategorizedDynamicTask)).getConfigurableDeviceTypes();
                for (String incomingDeviceType : incomingCategorizedDynamicTask.getDeviceTypes()) {
                    if (!configurableDeviceTypes.contains(incomingDeviceType)) {
                        String msg =
                                "Encountered an invalid device type [" + incomingDeviceType + "] in " +
                                        "categorized dynamic task [" + incomingCategorizedDynamicTask.getCategoryCode() + "].";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                }

            } else {
                String msg =
                        "Encountered an invalid categorized dynamic task [" + incomingCategorizedDynamicTask.getCategoryCode() +
                                "].";
                log.error(msg);
                throw new BadRequestException(msg);

            }
        }
    }

    /**
     * Validate incoming configuration payload against the existing configurations.
     *
     * @param incomingDynamicTaskPlatformConfigurations Instance of {@link DynamicTaskPlatformConfigurations}
     *                                                  containing updated configurations.
     * @param existingDynamicTaskPlatformConfigurations Instance of {@link DynamicTaskPlatformConfigurations}
     *                                                  containing existing configurations.
     * @throws BadRequestException contains any invalid configuration settings.
     */
    private static void validatePayload(DynamicTaskPlatformConfigurations incomingDynamicTaskPlatformConfigurations,
                                        DynamicTaskPlatformConfigurations existingDynamicTaskPlatformConfigurations) throws BadRequestException {
        if (incomingDynamicTaskPlatformConfigurations == null) {
            String msg = "Encountered an invalid configuration object. Incoming configuration resource can not be " +
                    "null.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        validatePayload(new ArrayList<>(incomingDynamicTaskPlatformConfigurations.getCategorizedDynamicTasks()),
                new ArrayList<>(existingDynamicTaskPlatformConfigurations.getCategorizedDynamicTasks()));
    }

    /**
     * Filter and get only updated categorized dynamic task entries from incoming updated configuration payload.
     *
     * @param incomingCategorizedDynamicTasks List of {@link CategorizedDynamicTask} containing updated configurations.
     * @param existingCategorizedDynamicTasks List of {@link CategorizedDynamicTask} existing configurations.
     * @return Set of {@link CategorizedDynamicTask} without duplicates.
     */
    private static Set<CategorizedDynamicTask> getUpdatedCategorizedDynamicTasks(List<CategorizedDynamicTask> incomingCategorizedDynamicTasks, List<CategorizedDynamicTask> existingCategorizedDynamicTasks) {
        Set<CategorizedDynamicTask> updatedCategorizedDynamicTasks = new HashSet<>();
        for (CategorizedDynamicTask incomingCategorizedDynamicTask : incomingCategorizedDynamicTasks) {
            CategorizedDynamicTask existingCategorizedDynamicTask =
                    existingCategorizedDynamicTasks.get(existingCategorizedDynamicTasks.indexOf(incomingCategorizedDynamicTask));
            if (!existingCategorizedDynamicTask.isContentEquals(incomingCategorizedDynamicTask)) {
                updatedCategorizedDynamicTasks.add(incomingCategorizedDynamicTask);
            }
        }
        return updatedCategorizedDynamicTasks;
    }

    /**
     * Validate if a request is come from super tenant or not.
     *
     * @throws ForbiddenException Throws when a request is come a from sub tenant.
     */
    private static void checkDomainValidity() throws ForbiddenException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (!Objects.equals(tenantDomain, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            String msg =
                    "Forbidden request received for dynamic task management admin API from tenant domain [ " + tenantDomain + "]";
            log.error(msg);
            throw new ForbiddenException(msg);
        }
    }

    @Override
    public Response getDynamicTaskPlatformConfigurations(String tenantDomain) {
        try {
            checkDomainValidity();
            return Response.ok().entity(APIUtils.getDynamicTaskConfigurationManagementService().getDynamicTaskPlatformConfigurations(tenantDomain)).build();
        } catch (ForbiddenException e) {
            String msg = "Forbidden request received for get dynamic task platform configurations.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Can not find a dynamic task configuration under tenant domain [" + tenantDomain + "].";
            log.error(msg, e);
            return Response.status(Response.Status.NOT_FOUND).entity(msg).build();
        } catch (DynamicTaskManagementException e) {
            String msg =
                    "Error encountered while retrieving dynamic task configurations for tenant domain [" + tenantDomain + "].";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response updateDynamicTaskPlatformConfigurations(String tenantDomain,
                                                            DynamicTaskPlatformConfigurations dynamicTaskPlatformConfigurations) {
        try {
            checkDomainValidity();
            DynamicTaskPlatformConfigurations existingDynamicTaskPlatformConfigurations =
                    APIUtils.getDynamicTaskConfigurationManagementService().getDynamicTaskPlatformConfigurations(tenantDomain);
            validatePayload(dynamicTaskPlatformConfigurations, existingDynamicTaskPlatformConfigurations);
            DynamicTaskPlatformConfigurations updatedDynamicTaskPlatformConfigurations =
                    APIUtils.getDynamicTaskConfigurationManagementService().updateCategorizedDynamicTasks(tenantDomain,
                            getUpdatedCategorizedDynamicTasks(new ArrayList<>(dynamicTaskPlatformConfigurations.getCategorizedDynamicTasks()),
                                    new ArrayList<>(existingDynamicTaskPlatformConfigurations.getCategorizedDynamicTasks())));
            return Response.ok().entity(updatedDynamicTaskPlatformConfigurations).build();
        } catch (ForbiddenException e) {
            String msg = "Forbidden request received for update dynamic task platform configurations.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (NotFoundException e) {
            String msg = "Failed to locate dynamic task configuration for tenant domain [" + tenantDomain + "].";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (BadRequestException e) {
            String msg = "Encountered an malformed configuration settings while processing the dynamic task updating " +
                    "request for tenant domain [" + tenantDomain + "]";
            log.error(msg);
            return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
        } catch (DynamicTaskManagementException e) {
            String msg =
                    "Error encountered while updating dynamic task configurations in tenant domain [" + tenantDomain +
                            "].";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }

    @Override
    public Response resetDynamicTaskPlatformConfigurations(String tenantDomain) {
        try {
            checkDomainValidity();
            return Response.ok().entity(APIUtils.getDynamicTaskConfigurationManagementService().resetToDefault(tenantDomain)).build();
        } catch (ForbiddenException e) {
            String msg = "Forbidden request received for reset dynamic task platform configurations.";
            log.error(msg);
            return Response.status(Response.Status.FORBIDDEN).entity(msg).build();
        } catch (DynamicTaskManagementException e) {
            String msg =
                    "Error encountered while resetting dynamic task configurations in tenant domain [" + tenantDomain +
                            "].";
            log.error(msg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
