/* Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.core.impl;

import io.entgra.application.mgt.common.IdentityServerResponse;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.common.dto.IdentityServiceProviderDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.common.exception.TransactionManagementException;
import io.entgra.application.mgt.common.response.Application;
import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.application.mgt.common.services.SPApplicationManager;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.core.config.IdentityServiceProvider;
import io.entgra.application.mgt.core.dao.ApplicationDAO;
import io.entgra.application.mgt.core.dao.SPApplicationDAO;
import io.entgra.application.mgt.core.dao.VisibilityDAO;
import io.entgra.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.exception.NotFoundException;
import io.entgra.application.mgt.core.identityserver.serviceprovider.ISServiceProviderApplicationService;
import io.entgra.application.mgt.core.internal.DataHolder;
import io.entgra.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.core.util.ApplicationManagementUtil;
import io.entgra.application.mgt.core.util.ConnectionManagerUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SPApplicationManagerImpl implements SPApplicationManager {

    private static final Log log = LogFactory.getLog(SPApplicationManagerImpl.class);
    private ApplicationDAO applicationDAO;
    private SPApplicationDAO spApplicationDAO;
    private VisibilityDAO visibilityDAO;
    private final LifecycleStateManager lifecycleStateManager;

    public SPApplicationManagerImpl() {
        initDataAccessObjects();
        lifecycleStateManager = DataHolder.getInstance().getLifecycleStateManager();
    }

    private void initDataAccessObjects() {
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.visibilityDAO = ApplicationManagementDAOFactory.getVisibilityDAO();
        this.spApplicationDAO = ApplicationManagementDAOFactory.getSPApplicationDAO();
    }

    @Override
    public IdentityServerResponse getIdentityServerResponse(int identityServerId) throws ApplicationManagementException {
        IdentityServerDTO identityServerDTO = getIdentityServer(identityServerId);
        return APIUtil.identityServerDtoToIdentityServerResponse(identityServerDTO);
    }

    private IdentityServerDTO getIdentityServer(int identityServerId) throws ApplicationManagementException {
        IdentityServerDTO identityServerDTO = getIdentityServerFromDB(identityServerId);
        if (identityServerDTO == null) {
            String msg = "Identity server with the id: " + identityServerId + " does not exist";
            log.error(msg);
            throw new NotFoundException(msg);
        }
        return identityServerDTO;
    }

    private IdentityServerDTO getIdentityServerFromDB(int identityServerId) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            return spApplicationDAO.getIdentityServerById(identityServerId, tenantId);
        } catch (DBConnectionException e) {
            String msg = "Error occurred when getting database connection to get identity server with the id: " + identityServerId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "DAO exception while getting identity server with the id " + identityServerId ;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<IdentityServerResponse> getIdentityServers() throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            return spApplicationDAO.getIdentityServers(tenantId).stream().
                    map(APIUtil::identityServerDtoToIdentityServerResponse).collect(Collectors.toList());
        } catch (DBConnectionException e) {
            String msg = "Error occurred when getting database connection to get identity servers";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "DAO exception while getting identity servers";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public IdentityServerResponse createIdentityServer(IdentityServerDTO identityServerDTO) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        validateIdentityServerCreateRequest(identityServerDTO);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            int id = spApplicationDAO.createIdentityServer(identityServerDTO, tenantId);
            identityServerDTO.setId(id);
            ConnectionManagerUtil.commitDBTransaction();
            return APIUtil.identityServerDtoToIdentityServerResponse(identityServerDTO);
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while creating identity server " + identityServerDTO.getName();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public IdentityServerResponse updateIdentityServer(IdentityServerDTO updateIdentityServerDTO, int id)
            throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        IdentityServerDTO existingIdentityServerDTO = getIdentityServer(id);
        validateIdentityServerUpdateRequest(updateIdentityServerDTO, existingIdentityServerDTO);
        Map<String, String> updatedApiParams = constructUpdatedApiParams(updateIdentityServerDTO, existingIdentityServerDTO);
        updateIdentityServerDTO.setApiParams(updatedApiParams);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            spApplicationDAO.updateIdentityServer(updateIdentityServerDTO, tenantId, id);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while creating identity server " + updateIdentityServerDTO.getName();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return getIdentityServerResponse(id);
    }

    @Override
    public void deleteIdentityServer(int id) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        validateIdentityServerDeleteRequest(id);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            spApplicationDAO.deleteIdentityServer(id, tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while creating identity server with the id " + id;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    private void validateIdentityServerDeleteRequest(int identityServerId) throws ApplicationManagementException {
        IdentityServerDTO identityServerDTO = getIdentityServerFromDB(identityServerId);
        if (identityServerDTO == null) {
            String msg = "Identity server with the id: " + identityServerId + " does not exist to delete";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    private Map<String, String> constructUpdatedApiParams(IdentityServerDTO updatedIdentityServerDTO,
                                                          IdentityServerDTO existingIdentityServerDTO) {
        Map<String, String> updatedApiParams = updatedIdentityServerDTO.getApiParams();
        Map<String, String> existingApiParams = existingIdentityServerDTO.getApiParams();
        if (updatedIdentityServerDTO.getProviderName().equals(existingIdentityServerDTO.getProviderName())) {
            existingApiParams.putAll(updatedApiParams);
            return existingApiParams;
        }
        return updatedApiParams;
    }

    /**
     * Validate the identity server update request payload
     *
     * @param updateIdentityServerDTO of identity server update request
     * @throws BadRequestException if any invalid payload found
     */
    private void validateIdentityServerUpdateRequest(IdentityServerDTO updateIdentityServerDTO,
                                                     IdentityServerDTO existingIdentityServerDTO) throws ApplicationManagementException {
        if (updateIdentityServerDTO.getProviderName() != null &&
                isIdentityServiceProviderNotConfigured(updateIdentityServerDTO.getProviderName())) {
            String msg = "No such providers configured. Provider name: " + updateIdentityServerDTO.getProviderName();
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (updateIdentityServerDTO.getName() != null) {
            if (!updateIdentityServerDTO.getName().equalsIgnoreCase(existingIdentityServerDTO.getName())
                    && isIdentityServerNameExist(updateIdentityServerDTO.getName())) {
                String msg = "Identity server already exist with the given name. Identity server name: " + updateIdentityServerDTO.getName();
                log.error(msg);
                throw new BadRequestException(msg);
            }
        }
        if (updateIdentityServerDTO.getUrl() != null) {
            validateIdentityServerUrl(updateIdentityServerDTO.getUrl());
            if(!updateIdentityServerDTO.getUrl().equalsIgnoreCase(existingIdentityServerDTO.getUrl()) &&
                    isIdentityServerUrlExist(updateIdentityServerDTO.getUrl())) {
                String msg = "Identity server already exist with the given url. Identity server url: " + updateIdentityServerDTO.getUrl();
                log.error(msg);
                throw new BadRequestException(msg);
            }
        }
        validateUpdateIdentityServerRequestApiParam(updateIdentityServerDTO, existingIdentityServerDTO);
    }


    /**
     * Validate the identity server create request payload
     *
     * @param identityServerDTO of identity server create request
     * @throws BadRequestException if any invalid payload found
     */
    private void validateIdentityServerCreateRequest(IdentityServerDTO identityServerDTO) throws ApplicationManagementException {
        if (identityServerDTO.getUsername() == null) {
            String msg = "Identity server username can not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (identityServerDTO.getPassword() == null) {
            String msg = "Identity server password can not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (identityServerDTO.getName() == null) {
            String msg = "Identity server name can not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (identityServerDTO.getUrl() == null) {
            String msg = "Identity server url can not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (isIdentityServiceProviderNotConfigured(identityServerDTO.getProviderName())) {
            String msg = "No such providers configured. Provider name: " + identityServerDTO.getProviderName();
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (isIdentityServerNameExist(identityServerDTO.getName())) {
            String msg = "Identity server already exist with the given name. Identity server name: " + identityServerDTO.getName();
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (isIdentityServerUrlExist(identityServerDTO.getUrl())) {
            String msg = "Identity server already exist with the given url. Identity server url: " + identityServerDTO.getUrl();
            log.error(msg);
            throw new BadRequestException(msg);
        }
        validateCreateIdentityServerRequestApiParams(identityServerDTO);
        validateIdentityServerUrl(identityServerDTO.getUrl());
    }

    private void validateIdentityServerUrl(String url) throws BadRequestException {
        String[] schemes = {"http","https"};
        UrlValidator urlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
        if (!urlValidator.isValid(url)) {
            String msg = "Identity server url is not a valid url";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    private void validateUpdateIdentityServerRequestApiParam(IdentityServerDTO identityServerUpdateDTO,
                                                             IdentityServerDTO existingIdentityServerDTO) throws ApplicationManagementException {
        ISServiceProviderApplicationService serviceProviderApplicationService =
                ISServiceProviderApplicationService.of(existingIdentityServerDTO.getProviderName());
        List<String> requiredApiParams = serviceProviderApplicationService.getRequiredApiParams();
        if (!identityServerUpdateDTO.getProviderName().equals(existingIdentityServerDTO.getProviderName())) {
            validateAllRequiredParamsExists(identityServerUpdateDTO, requiredApiParams);
        }
        validateIfAnyInvalidParamExists(identityServerUpdateDTO, requiredApiParams);
    }

    private void validateCreateIdentityServerRequestApiParams(IdentityServerDTO identityServerDTO) throws ApplicationManagementException {
        ISServiceProviderApplicationService serviceProviderApplicationService =
                ISServiceProviderApplicationService.of(identityServerDTO.getProviderName());
        List<String> requiredApiParams = serviceProviderApplicationService.getRequiredApiParams();
        validateAllRequiredParamsExists(identityServerDTO, requiredApiParams);
        validateIfAnyInvalidParamExists(identityServerDTO, requiredApiParams);
    }

    private void validateAllRequiredParamsExists(IdentityServerDTO identityServerDTO, List<String> requiredApiParams)
            throws BadRequestException {
        for (String param : requiredApiParams) {
            if (identityServerDTO.getApiParams().get(param) == null) {
                String msg = param + " api parameter is required for " + identityServerDTO.getProviderName() + ". " +
                        "Required api parameters: " + StringUtils.join(requiredApiParams, ",");
                log.error(msg);
                throw new BadRequestException(msg);
            }
        }
    }

    private void validateIfAnyInvalidParamExists(IdentityServerDTO identityServerDTO, List<String> requiredApiParams)
            throws BadRequestException {
        for (String param : identityServerDTO.getApiParamKeys()) {
            if (!requiredApiParams.contains(param)) {
                String msg = "Invalid api parameter. " + param + " is not required for " + identityServerDTO.getProviderName() + ". " +
                        "Required api parameters: " + StringUtils.join(requiredApiParams, ",");
                throw new BadRequestException(msg);
            }
        }
    }

    private boolean isIdentityServiceProviderNotConfigured(String providerName) {
        List<IdentityServiceProvider> identityServiceProviders = ConfigurationManager.getInstance().getIdentityServerConfiguration().
                getIdentityServiceProviders();
        return identityServiceProviders.stream().noneMatch(provider -> provider.getProviderName().equals(providerName));
    }

    @Override
    public boolean isIdentityServerNameExist(String name) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            return spApplicationDAO.isExistingIdentityServerName(name, tenantId);
        } catch (ApplicationManagementDAOException | DBConnectionException e) {
            String msg = "Error occurred while checking if identity server with the name " + name + " exists.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean isIdentityServerUrlExist(String url) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            return spApplicationDAO.isExistingIdentityServerUrl(url, tenantId);
        } catch (ApplicationManagementDAOException | DBConnectionException e) {
            String msg = "Error occurred while checking if identity server with the url " + url + " exists.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public SPApplicationListResponse retrieveSPApplicationFromIdentityServer(int identityServerId, Integer offset, Integer limit)
            throws ApplicationManagementException {
        IdentityServerDTO identityServer = getIdentityServer(identityServerId);
        ISServiceProviderApplicationService serviceProviderApplicationService = ISServiceProviderApplicationService.of(identityServer.getProviderName());
        SPApplicationListResponse spApplicationListResponse = serviceProviderApplicationService.retrieveSPApplications(identityServer, offset, limit);
        addExistingApps(identityServerId, spApplicationListResponse.getApplications());
        return spApplicationListResponse;
    }

    /**
     * This method adds existing consumer applications of service providers to the SPApplication bean
     *
     * @param identityServerId identity  server id of the service provider
     * @param spApplications Service providers list to which the existing applications should be added
     * @throws ApplicationManagementException if error occurred while adding existing applications
     */
    private void addExistingApps(int identityServerId, List<SPApplication> spApplications) throws ApplicationManagementException {
        for (SPApplication spApplication : spApplications) {
            List<Application> existingApplications = getSPApplications(identityServerId, spApplication.getId());
            spApplication.setExistingApplications(existingApplications);
        }
    }

    @Override
    public List<Application> getSPApplications(int identityServerId, String spUID) throws
            ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ApplicationManager applicationManager = APIUtil.getApplicationManager();
        List<Application> applications = new ArrayList<>();

        try {
            ConnectionManagerUtil.openDBConnection();
            List<ApplicationDTO> appDTOs = spApplicationDAO.getSPApplications(identityServerId, spUID, tenantId);
            for (ApplicationDTO applicationDTO : appDTOs) {
                if (lifecycleStateManager.getEndState().equals(applicationDTO.getStatus())) {
                    continue;
                }
                boolean isHideableApp = applicationManager.isHideableApp(applicationDTO.getApplicationReleaseDTOs());
                boolean isDeletableApp = applicationManager.isDeletableApp(applicationDTO.getApplicationReleaseDTOs());

                //Set application categories, tags and unrestricted roles to the application DTO.
                applicationDTO
                        .setUnrestrictedRoles(visibilityDAO.getUnrestrictedRoles(applicationDTO.getId(), tenantId));
                applicationDTO.setAppCategories(applicationDAO.getAppCategories(applicationDTO.getId(), tenantId));
                applicationDTO.setTags(applicationDAO.getAppTags(applicationDTO.getId(), tenantId));

                applicationDTO.setApplicationReleaseDTOs(applicationDTO.getApplicationReleaseDTOs());
                Application application = APIUtil.appDtoToAppResponse(applicationDTO);
                application.setDeletableApp(isDeletableApp);
                application.setHideableApp(isHideableApp);
                applications.add(application);
            }

            return applications;
        } catch (DBConnectionException e) {
            String msg = "Error occurred when getting database connection to get applications by filtering from "
                    + "requested filter.";
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementDAOException e) {
            String msg =
                    "DAO exception while getting applications of tenant " + tenantId ;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public void validateAttachAppsRequest(int identityServerId, String serviceProviderId, List<Integer> appIds) throws ApplicationManagementException {
        validateServiceProviderUID(identityServerId, serviceProviderId);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            for (int appId : appIds) {
                try {
                    ApplicationDTO appDTO = applicationDAO.getApplication(appId, tenantId);
                    if (appDTO == null) {
                        String msg = "Payload contains invalid an app id. " + "No app exist with the appId: " + appId + ".";
                        throw new BadRequestException(msg);
                    }
                } catch (ApplicationManagementDAOException e) {
                    String msg = "Error occurred while trying to retrieve application with the id:" + appId;
                    log.error(msg, e);
                    throw new ApplicationManagementException(msg, e);
                }
            }
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public void validateDetachAppsRequest(int identityServerId, String spId, List<Integer> appIds) throws ApplicationManagementException {
        validateServiceProviderUID(identityServerId, spId);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            ConnectionManagerUtil.openDBConnection();
            for (int id : appIds) {
                try {
                    boolean isSPAppExist = spApplicationDAO.isSPApplicationExist(identityServerId, spId, id, tenantId);
                    if (!isSPAppExist) {
                        String msg = "No service provider app exist with the appId: " + id + " for service provider with the " +
                                "UID " + spId;
                        throw new ApplicationManagementException(msg);
                    }
                } catch (ApplicationManagementDAOException e) {
                    String msg = "Error occurred while checking if application exists with the id:" + id;
                    log.error(msg, e);
                    throw new ApplicationManagementException(msg, e);
                }
            }
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public List<IdentityServiceProviderDTO> getIdentityServiceProviders() throws ApplicationManagementException {
        List<IdentityServiceProvider> identityServiceProviders = ConfigurationManager.getInstance().
                getIdentityServerConfiguration().getIdentityServiceProviders();
        List<IdentityServiceProviderDTO> identityServiceProviderDTOS = new ArrayList<>();
        for (IdentityServiceProvider identityServiceProvider : identityServiceProviders) {
            try {
                identityServiceProviderDTOS.add(APIUtil.identityServiceProviderToDTO(identityServiceProvider));
            } catch (ApplicationManagementException e) {
                String msg = "Identity service provider configuration file is invalid. Hence failed to proceed.";
                log.error(msg);
                throw new ApplicationManagementException(msg);
            }
        }
        return identityServiceProviderDTOS;
    }

    /**
     * Responsible for validating service provider in requests
     *
     * @param identityServerId identity server id of the service provider
     * @param spUID uid of the service provider
     * @throws ApplicationManagementException if invalid service provider
     */
    private void validateServiceProviderUID(int identityServerId, String spUID) throws
            ApplicationManagementException {
        IdentityServerDTO identityServer = getIdentityServer(identityServerId);
        ISServiceProviderApplicationService serviceProviderApplicationService = ISServiceProviderApplicationService.of(identityServer.getProviderName());
        try {
            boolean isSPAppExists = serviceProviderApplicationService.
                    isSPApplicationExist(identityServer, spUID);
            if (!isSPAppExists) {
                String errMsg = "Service provider with the uid " + spUID + " does not exist.";
                log.error(errMsg);
                throw new BadRequestException(errMsg);
            }
        } catch (ApplicationManagementException e) {
            String errMsg = "Error occurred while trying to validate service provider uid";
            log.error(errMsg, e);
            throw new ApplicationManagementException(errMsg, e);
        }
    }


    public void attachSPApplications(int identityServerId, String spUID, List<Integer> appIds) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            for (int appId : appIds) {
                spApplicationDAO.attachSPApplication(identityServerId, spUID, appId, tenantId);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e){
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "DAO exception while getting applications of tenant " + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public void detachSPApplications(int identityServerId, String spUID, List<Integer> appIds)  throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            for (int id : appIds) {
                spApplicationDAO.detachSPApplication(identityServerId, spUID, id, tenantId);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (ApplicationManagementDAOException e){
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg =
                    "DAO exception while getting applications of tenant " + tenantId;
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public <T> Application createSPApplication(T app, int identityServerId, String spId) throws ApplicationManagementException {
        validateServiceProviderUID(identityServerId, spId);
        ApplicationManager applicationManager = ApplicationManagementUtil.getApplicationManagerInstance();
        ApplicationDTO applicationDTO = applicationManager.uploadReleaseArtifactIfExist(app);
        if (log.isDebugEnabled()) {
            log.debug("Application release create request is received. Application name: " + applicationDTO.getName()
                    + " Device type ID: " + applicationDTO.getDeviceTypeId());
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Application createdApp = applicationManager.addAppDataIntoDB(applicationDTO);
            attachCreatedSPApplication(createdApp, identityServerId,  spId);
            ConnectionManagerUtil.commitDBTransaction();
            return createdApp;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection.";
            log.error(msg, e);
            ApplicationManagementUtil.deleteArtifactIfExist(applicationDTO);
            throw new ApplicationManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while disabling AutoCommit.";
            log.error(msg, e);
            ApplicationManagementUtil.deleteArtifactIfExist(applicationDTO);
            throw new ApplicationManagementException(msg, e);
        } catch (ApplicationManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while creating and attaching application with the name " + applicationDTO.getName() ;
            log.error(msg, e);
            ApplicationManagementUtil.deleteArtifactIfExist(applicationDTO);
            throw new ApplicationManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    public void attachCreatedSPApplication(Application createdApp, int identityServerId, String spUID) throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            spApplicationDAO.attachSPApplication(identityServerId, spUID, createdApp.getId(), tenantId);
        } catch (ApplicationManagementDAOException e) {
            String msg = "Error occurred while attaching application with the id " + createdApp.getId();
            log.error(msg, e);
            throw new ApplicationManagementException(msg, e);
        }
    }

}
