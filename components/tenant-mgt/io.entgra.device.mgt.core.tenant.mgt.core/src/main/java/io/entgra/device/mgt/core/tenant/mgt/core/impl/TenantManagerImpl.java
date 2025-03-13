/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.tenant.mgt.core.impl;

import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.common.services.ApplicationManager;
import io.entgra.device.mgt.core.application.mgt.core.config.ConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.roles.config.Role;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceManagementConfig;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.DeviceManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.TenantDAO;
import io.entgra.device.mgt.core.tenant.mgt.core.TenantManager;
import io.entgra.device.mgt.core.tenant.mgt.common.exception.TenantMgtException;
import io.entgra.device.mgt.core.tenant.mgt.core.internal.TenantMgtDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.PublisherRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.dto.APIInfo.Scope;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

public class TenantManagerImpl implements TenantManager {
    private static final Log log = LogFactory.getLog(TenantManagerImpl.class);
    private static final String PERMISSION_ACTION = "ui.execute";
    TenantDAO tenantDao;

    public TenantManagerImpl() {
        this.tenantDao = DeviceManagementDAOFactory.getTenantDAO();
    }

    @Override
    public void addDefaultRoles(TenantInfoBean tenantInfoBean) throws TenantMgtException {
        initTenantFlow(tenantInfoBean);
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        if (config.getDefaultRoles().isEnabled()) {
            Map<String, List<Permission>> roleMap = getValidRoleMap(config);
            try {
                UserStoreManager userStoreManager = TenantMgtDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantInfoBean.getTenantId()).getUserStoreManager();

                roleMap.forEach((key, value) -> {
                    try {
                        userStoreManager.addRole(key, null, value.toArray(new Permission[0]));
                    } catch (UserStoreException e) {
                        log.error("Error occurred while adding default roles into user store", e);
                    }
                });
            } catch (UserStoreException e) {
                String msg = "Error occurred while getting user store manager";
                log.error(msg, e);
                throw new TenantMgtException(msg, e);
            }
        }
        try {
            TenantMgtDataHolder.getInstance().getWhiteLabelManagementService().
                    addDefaultWhiteLabelThemeIfNotExist(tenantInfoBean.getTenantId());
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while adding default white label theme to created tenant - id "+tenantInfoBean.getTenantId();
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public void addDefaultAppCategories(TenantInfoBean tenantInfoBean) throws TenantMgtException {
        initTenantFlow(tenantInfoBean);
        try {
            ApplicationManager applicationManager = TenantMgtDataHolder.getInstance().getApplicationManager();
            applicationManager
                    .addApplicationCategories(ConfigurationManager.getInstance().getConfiguration().getAppCategories());
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while getting default application categories";
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public void addDefaultDeviceStatusFilters(TenantInfoBean tenantInfoBean) throws TenantMgtException {
        initTenantFlow(tenantInfoBean);
        try {
            TenantMgtDataHolder.getInstance().getDeviceStatusManagementService().
                    addDefaultDeviceStatusFilterIfNotExist(tenantInfoBean.getTenantId());
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while adding default device status filter";
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public void deleteTenantApplicationData(int tenantId) throws TenantMgtException {
        try {
            TenantMgtDataHolder.getInstance().getApplicationManager().
                    deleteApplicationDataOfTenant(tenantId);
        } catch (ApplicationManagementException e) {
            String msg = "Error occurred while deleting Application related data of Tenant of " +
                    "tenant Id" + tenantId;
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        }
    }

    @Override
    public void deleteTenantDeviceData(int tenantId) throws TenantMgtException {
        if (log.isDebugEnabled()) {
            log.debug("Request is received to delete Device related data of tenant with ID: " + tenantId);
        }
        try {
            DeviceManagementDAOFactory.beginTransaction();

            tenantDao.deleteExternalPermissionMapping(tenantId);
            tenantDao.deleteExternalDeviceMappingByTenantId(tenantId);
            tenantDao.deleteExternalGroupMappingByTenantId(tenantId);
            // TODO: Check whether deleting DM_DEVICE_ORGANIZATION table data is necessary
//            tenantDao.deleteDeviceOrganizationByTenantId(tenantId);
            tenantDao.deleteDeviceHistoryLastSevenDaysByTenantId(tenantId);
            tenantDao.deleteDeviceDetailByTenantId(tenantId);
            tenantDao.deleteDeviceLocationByTenantId(tenantId);
            tenantDao.deleteDeviceInfoByTenantId(tenantId);
            tenantDao.deleteNotificationByTenantId(tenantId);
            tenantDao.deleteAppIconsByTenantId(tenantId);
            tenantDao.deleteTraccarUnsyncedDevicesByTenantId(tenantId);
            tenantDao.deleteDeviceEventGroupMappingByTenantId(tenantId);
            tenantDao.deleteGeofenceEventMappingByTenantId(tenantId);
            tenantDao.deleteDeviceEventByTenantId(tenantId);
            tenantDao.deleteGeofenceGroupMappingByTenantId(tenantId);
            tenantDao.deleteGeofenceByTenantId(tenantId);
            tenantDao.deleteDeviceGroupPolicyByTenantId(tenantId);
            tenantDao.deleteDynamicTaskPropertiesByTenantId(tenantId);
            tenantDao.deleteDynamicTaskByTenantId(tenantId);
            tenantDao.deleteMetadataByTenantId(tenantId);
            tenantDao.deleteOTPDataByTenantId(tenantId);
            tenantDao.deleteSubOperationTemplate(tenantId);
            tenantDao.deleteDeviceSubTypeByTenantId(tenantId);
            tenantDao.deleteCEAPoliciesByTenantId(tenantId);

            tenantDao.deleteApplicationByTenantId(tenantId);
            tenantDao.deletePolicyCriteriaPropertiesByTenantId(tenantId);
            tenantDao.deletePolicyCriteriaByTenantId(tenantId);
            tenantDao.deleteCriteriaByTenantId(tenantId);
            tenantDao.deletePolicyChangeManagementByTenantId(tenantId);
            tenantDao.deletePolicyComplianceFeaturesByTenantId(tenantId);
            tenantDao.deletePolicyComplianceStatusByTenantId(tenantId);
            tenantDao.deleteRolePolicyByTenantId(tenantId);
            tenantDao.deleteUserPolicyByTenantId(tenantId);
            tenantDao.deleteDeviceTypePolicyByTenantId(tenantId);
            tenantDao.deleteDevicePolicyAppliedByTenantId(tenantId);
            tenantDao.deleteDevicePolicyByTenantId(tenantId);
            tenantDao.deletePolicyCorrectiveActionByTenantId(tenantId);
            tenantDao.deletePolicyByTenantId(tenantId);
            tenantDao.deleteProfileFeaturesByTenantId(tenantId);
            tenantDao.deleteProfileByTenantId(tenantId);

            tenantDao.deleteDeviceOperationResponseLargeByTenantId(tenantId);
            tenantDao.deleteDeviceOperationResponseByTenantId(tenantId);
            tenantDao.deleteEnrolmentOpMappingByTenantId(tenantId);
            tenantDao.deleteDeviceStatusByTenantId(tenantId);
            tenantDao.deleteEnrolmentByTenantId(tenantId);
            tenantDao.deleteOperationByTenantId(tenantId);
            tenantDao.deleteDeviceGroupMapByTenantId(tenantId);
            tenantDao.deleteGroupPropertiesByTenantId(tenantId);
            tenantDao.deleteDevicePropertiesByTenantId(tenantId);
            tenantDao.deleteDeviceByTenantId(tenantId);
            tenantDao.deleteRoleGroupMapByTenantId(tenantId);
            tenantDao.deleteGroupByTenantId(tenantId);
            tenantDao.deleteDeviceCertificateByTenantId(tenantId);

            DeviceManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementDAOException e) {
            DeviceManagementDAOFactory.rollbackTransaction();
            String msg = "Error deleting data of tenant of ID: '" + tenantId + "'";
            log.error(msg);
            throw new TenantMgtException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error initializing transaction when trying to delete tenant info of '" + tenantId + "'";
            log.error(msg);
            throw new TenantMgtException(msg, e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

    }

    private void initTenantFlow(TenantInfoBean tenantInfoBean) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCarbonContext.setTenantId(tenantInfoBean.getTenantId());
        privilegedCarbonContext.setTenantDomain(tenantInfoBean.getTenantDomain());
    }

    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    private Map<String, List<Permission>> getValidRoleMap(DeviceManagementConfig config) {
        Map<String, List<Permission>> roleMap = new HashMap<>();
        try {
            for (Role role : config.getDefaultRoles().getRoles()) {
                List<Permission> permissionList = new ArrayList<>();
                for (String permissionPath : role.getPermissions()) {
                    if (PermissionUtils.checkResourceExists(permissionPath)) {
                        Permission permission = new Permission(permissionPath, PERMISSION_ACTION);

                        permissionList.add(permission);
                    } else {
                        log.warn("Permission  " + permissionPath + " does not exist. Hence it will not add to role "
                                + role.getName());
                    }
                }
                roleMap.put(role.getName(), permissionList);
            }
        } catch (PermissionManagementException | RegistryException e) {
            log.error("Error occurred while checking permission existence.", e);
        }
        return roleMap;
    }


    /**
     * This method will create OAuth application under the given tenant domain and generate an access token against the
     * client credentials. Once this access token is generated it will then be used to retrieve all the scopes that are already
     * published to that tenant space. The scopes of the super tenant will also be retrieved in order to compare which scopes were added
     * or removed. (A temporary admin user will be created in the sub tenant space to publish the scopes and will be deleted once
     * the scope publishing task is done)
     * @param tenantDomain tenant domain that the scopes will be published to.
     * @throws TenantMgtException if there are any errors when publishing scopes to a tenant
     */
    @Override
    public void publishScopesToTenant(String tenantDomain) throws TenantMgtException {
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PublisherRESTAPIServices publisherRESTAPIServices = TenantMgtDataHolder.getInstance().getPublisherRESTAPIServices();
                Scope[] superTenantScopes = getAllScopesFromSuperTenant(publisherRESTAPIServices);

                if (superTenantScopes != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Number of super tenant scopes already published - " + superTenantScopes.length);
                    }

                    Scope[] subTenantScopes = publisherRESTAPIServices.getScopes();

                    if (subTenantScopes.length > 0) {
                        // If there is already existing scopes on the sub tenant space then do a comparison with the
                        // super tenant scopes to add those new scopes to sub tenant space or to delete them from
                        // sub tenant space if it is not existing on the super tenant scope list.

                        if (log.isDebugEnabled()) {
                            log.debug("Number of sub tenant scopes already published - " + subTenantScopes.length);
                        }

                        List<Scope> missingScopes = new ArrayList<>();
                        List<Scope> deletedScopes = new ArrayList<>();

                        for (Scope superTenantScope : superTenantScopes) {
                            boolean isMatchingScope = false;
                            for (Scope subTenantScope : subTenantScopes) {
                                if (superTenantScope.getName().equals(subTenantScope.getName())) {
                                    isMatchingScope = true;
                                    break;
                                }
                            }
                            if (!isMatchingScope) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Missing scope found in sub tenant space - " +
                                            superTenantScope.getName());
                                }
                                missingScopes.add(superTenantScope);
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Total number of missing scopes found in sub tenant space - " +
                                    missingScopes.size());
                        }

                        if (missingScopes.size() > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Starting to add new/updated shared scopes to the tenant: '" + tenantDomain + "'.");
                            }
                            publishSharedScopes(missingScopes, publisherRESTAPIServices);
                        }

                        for (Scope subTenantScope : subTenantScopes) {
                            boolean isMatchingScope = false;
                            for (Scope superTenantScope : superTenantScopes) {
                                if (superTenantScope.getName().equals(subTenantScope.getName())) {
                                    isMatchingScope = true;
                                    break;
                                }
                            }
                            if (!isMatchingScope) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Deleted scope found in sub tenant space - " +
                                            subTenantScope.getName());
                                }
                                deletedScopes.add(subTenantScope);
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("Total number of deleted scopes found in sub tenant space - " +
                                    deletedScopes.size());
                        }

                        if (deletedScopes.size() > 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("Starting to delete shared scopes from the tenant: '" + tenantDomain + "'.");
                            }
                            for (Scope deletedScope : deletedScopes) {
                                if (publisherRESTAPIServices.isSharedScopeNameExists(deletedScope.getName())) {
                                    Scope scope = createScopeObject(deletedScope);
                                    publisherRESTAPIServices.deleteSharedScope(scope);
                                }
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Starting to publish shared scopes to newly created tenant: '" + tenantDomain + "'.");
                        }

                        publishSharedScopes(Arrays.asList(superTenantScopes), publisherRESTAPIServices);
                    }
                } else {
                    String msg = "Unable to publish scopes to sub tenants due to super tenant scopes list being empty.";
                    log.error(msg);
                    throw new TenantMgtException(msg);
                }
            } catch (BadRequestException e) {
                String msg = "Invalid request sent when publishing scopes to '" + tenantDomain + "' tenant space.";
                log.error(msg, e);
                throw new TenantMgtException(msg, e);
            } catch (UnexpectedResponseException e) {
                String msg = "Unexpected response received when publishing scopes to '" + tenantDomain + "' tenant space.";
                log.error(msg, e);
                throw new TenantMgtException(msg, e);
            } catch (APIServicesException e) {
                String msg = "Error occurred while publishing scopes to '" + tenantDomain + "' tenant space.";
                log.error(msg, e);
                throw new TenantMgtException(msg, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Get all the scopes from the super tenant space
     * @param publisherRESTAPIServices {@link PublisherRESTAPIServices} is used to get all scopes under a given tenant using client credentials
     * @return array of {@link Scope}
     * @throws BadRequestException if an invalid request is sent to the API Manager Publisher REST API Service
     * @throws UnexpectedResponseException if an unexpected response is received from the API Manager Publisher REST API Service
     * @throws TenantMgtException if an error occurred while processing the request sent to API Manager Publisher REST API Service
     */
    private Scope[] getAllScopesFromSuperTenant(PublisherRESTAPIServices publisherRESTAPIServices) throws BadRequestException,
            UnexpectedResponseException, TenantMgtException {

        try {
            // Get all scopes of super tenant to compare later with the sub tenant scopes. This is done
            // in order to see if any new scopes were added or deleted
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            return publisherRESTAPIServices.getScopes();
        } catch (APIServicesException e) {
            String msg = "Error occurred while retrieving access token from super tenant";
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Add shared scopes to the tenant space.
     * @param scopeList {@link List} of {@link Scope}
     * @param publisherRESTAPIServices {@link PublisherRESTAPIServices} is used to add shared scopes to a given tenant using client credentials
     * @throws BadRequestException if an invalid request is sent to the API Manager Publisher REST API Service
     * @throws UnexpectedResponseException if an unexpected response is received from the API Manager Publisher REST API Service
     * @throws APIServicesException if an error occurred while processing the request sent to API Manager Publisher REST API Service
     */
    private void publishSharedScopes (List<Scope> scopeList, PublisherRESTAPIServices publisherRESTAPIServices)
            throws BadRequestException, UnexpectedResponseException, APIServicesException {

        for (Scope tenantScope : scopeList) {
            if (!publisherRESTAPIServices.isSharedScopeNameExists(tenantScope.getName())) {
                Scope scope = createScopeObject(tenantScope);
                publisherRESTAPIServices.addNewSharedScope(scope);
            }
        }
    }

    /**
     * Creates a new scope object from the passed scope which includes the id, display name, description, name and bindings.
     * @param tenantScope existing {@link Scope} from a tenant
     * @return {@link Scope}
     */
    private Scope createScopeObject (Scope tenantScope) {
        Scope scope = new Scope();
        scope.setId(tenantScope.getId());
        scope.setDisplayName(tenantScope.getDisplayName());
        scope.setDescription(tenantScope.getDescription());
        scope.setName(tenantScope.getName());
        List<String> existingBindings = tenantScope.getBindings();
        List<String> bindings = new ArrayList<>();
        if (existingBindings != null &&
                existingBindings.contains(DeviceManagementConstants.User.DEFAULT_UI_EXECUTER)) {
            bindings.add(DeviceManagementConstants.User.DEFAULT_UI_EXECUTER);
        } else {
            bindings.add(Constants.ADMIN_ROLE_KEY);
        }
        scope.setBindings(bindings);
        return scope;
    }

    /**
     * Retrieves the tenant domain associated with the given tenant ID.
     *
     * @param tenantId The ID of the tenant.
     * @return The domain name of the tenant.
     * @throws TenantMgtException If there is an issue retrieving the tenant domain,
     *         such as an uninitialized RealmService or UserStoreException.
     */
    @Override
    public String getTenantDomain(int tenantId) throws TenantMgtException {
        try {
            return TenantMgtDataHolder.getInstance()
                    .getRealmService()
                    .getTenantManager()
                    .getDomain(tenantId);
        } catch (UserStoreException e) {
            String msg = "User store not initialized";
            log.error(msg);
            throw new TenantMgtException(msg, e);
        }
    }
}
