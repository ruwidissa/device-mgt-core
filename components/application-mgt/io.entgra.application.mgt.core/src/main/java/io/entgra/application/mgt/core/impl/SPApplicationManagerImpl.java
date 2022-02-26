/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.application.mgt.core.impl;

import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.IdentityServerList;
import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.dto.ApplicationDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.DBConnectionException;
import io.entgra.application.mgt.common.exception.TransactionManagementException;
import io.entgra.application.mgt.common.response.Application;
import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.application.mgt.common.services.SPApplicationManager;
import io.entgra.application.mgt.core.dao.ApplicationDAO;
import io.entgra.application.mgt.core.dao.SPApplicationDAO;
import io.entgra.application.mgt.core.dao.VisibilityDAO;
import io.entgra.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import io.entgra.application.mgt.core.exception.ApplicationManagementDAOException;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.core.internal.DataHolder;
import io.entgra.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.application.mgt.core.util.APIUtil;
import io.entgra.application.mgt.core.util.ApplicationManagementUtil;
import io.entgra.application.mgt.core.util.ConnectionManagerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import java.util.ArrayList;
import java.util.List;

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

    public void addExistingApps(int identityServerId, List<SPApplication> applications) throws ApplicationManagementException {
        for (SPApplication application : applications) {
            List<Application> existingApplications = getSPApplications(identityServerId, application.getId());
            application.setExistingApplications(existingApplications);
        }
    }

    @Override
    public IdentityServer getIdentityServer(int identityServerId) throws ApplicationManagementException {
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
    public IdentityServerList getIdentityServers() throws ApplicationManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            ConnectionManagerUtil.openDBConnection();
            IdentityServerList identityServerList = new IdentityServerList();
            identityServerList.setIdentityServers(spApplicationDAO.getIdentityServers(tenantId));
            return identityServerList;
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

    public void validateAttachAppsRequest(int identityServerId, List<Integer> appIds) throws ApplicationManagementException {
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
