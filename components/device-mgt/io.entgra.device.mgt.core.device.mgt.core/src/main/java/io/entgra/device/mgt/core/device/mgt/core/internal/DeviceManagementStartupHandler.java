/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.core.internal;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.operation.change.status.task.dto.OperationConfig;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Arrays;

public class DeviceManagementStartupHandler implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(DeviceManagementStartupHandler.class);
    private static final Gson gson = new Gson();
    private static final String OPERATION_CONFIG = "OPERATION_CONFIG";
    private static final String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        userRoleCreateObserver();
        operationStatusChangeObserver();
    }

    private void userRoleCreateObserver() {
        try {
            UserStoreManager userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            MultitenantConstants.SUPER_TENANT_ID).getUserStoreManager();
            String tenantAdminName =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            MultitenantConstants.SUPER_TENANT_ID).getRealmConfiguration().getAdminUserName();
            AuthorizationManager authorizationManager = DeviceManagementDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getAuthorizationManager();

            if (!userStoreManager.isExistingRole(DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN)) {
                userStoreManager.addRole(
                        DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                        null,
                        DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_ADMIN);
            } else {
                for (Permission permission : DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_ADMIN) {
                    authorizationManager.authorizeRole(DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                            permission.getResourceId(), permission.getAction());
                }
            }
            if (!userStoreManager.isExistingRole(DeviceManagementConstants.User.DEFAULT_DEVICE_USER)) {
                userStoreManager.addRole(
                        DeviceManagementConstants.User.DEFAULT_DEVICE_USER,
                        null,
                        DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_USER);
            } else {
                for (Permission permission : DeviceManagementConstants.User.PERMISSIONS_FOR_DEVICE_USER) {
                    authorizationManager.authorizeRole(DeviceManagementConstants.User.DEFAULT_DEVICE_USER,
                            permission.getResourceId(), permission.getAction());
                }
            }
            userStoreManager.updateRoleListOfUser(tenantAdminName, null,
                    new String[]{DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN,
                            DeviceManagementConstants.User.DEFAULT_DEVICE_USER});

            if (log.isDebugEnabled()) {
                log.debug("Device management roles: " + DeviceManagementConstants.User.DEFAULT_DEVICE_USER + ", " +
                        DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN + " created for the tenant:" + tenantDomain + "."
                );
                log.debug("Tenant administrator: " + tenantAdminName + "@" + tenantDomain +
                        " is assigned to the role:" + DeviceManagementConstants.User.DEFAULT_DEVICE_ADMIN + "."
                );
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while creating roles for the tenant: " + tenantDomain + ".");
        }
    }

    private void operationStatusChangeObserver () {
        MetadataManagementService metadataManagementService = DeviceManagementDataHolder
                .getInstance().getMetadataManagementService();
        OperationDAO operationDAO = OperationManagementDAOFactory.getOperationDAO();
        Metadata metadata;
        int numOfRecordsUpdated;
        try {
            metadata = metadataManagementService.retrieveMetadata(OPERATION_CONFIG);
            if (metadata != null) {
                OperationConfig operationConfiguration = gson.fromJson(metadata.getMetaValue(), OperationConfig.class);
                String[] deviceTypes = operationConfiguration.getDeviceTypes();
                String initialOperationStatus = operationConfiguration.getInitialOperationStatus();
                String requiredStatusChange = operationConfiguration.getRequiredStatusChange();

                for (String deviceType : deviceTypes) {
                    try {
                        OperationManagementDAOFactory.beginTransaction();
                        try {
                            numOfRecordsUpdated = operationDAO.updateOperationByDeviceTypeAndInitialStatus(deviceType,
                                    initialOperationStatus, requiredStatusChange);
                            log.info(numOfRecordsUpdated + " operations updated successfully for the" + deviceType);
                            OperationManagementDAOFactory.commitTransaction();
                        } catch (OperationManagementDAOException e) {
                            OperationManagementDAOFactory.rollbackTransaction();
                            String msg = "Error occurred while updating operation status. DeviceType : " + deviceType + ", " +
                                    "Initial operation status: " + initialOperationStatus + ", Required status:" + requiredStatusChange;
                            log.error(msg, e);
                        }
                    } catch (TransactionManagementException e) {
                        String msg = "Transactional error occurred while updating the operation status";
                        log.error(msg, e);
                    } finally {
                        OperationManagementDAOFactory.closeConnection();
                    }
                }
            } else {
                log.info("Operation configuration not provided");
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving the operation configuration";
            log.error(msg, e);
        }
    }
}
