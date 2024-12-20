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

package io.entgra.device.mgt.core.application.mgt.core.lifecycle;

import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherServiceImpl;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherStartupHandler;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherService;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import io.entgra.device.mgt.core.application.mgt.common.config.LifecycleState;
import io.entgra.device.mgt.core.application.mgt.common.exception.LifecycleManagementException;
import io.entgra.device.mgt.core.application.mgt.core.internal.DataHolder;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermission;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionUtils;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the activities related to lifecycle management
 */
public class LifecycleStateManager {

    private Map<String, LifecycleState> lifecycleStates;
    private static final Log log = LogFactory.getLog(LifecycleStateManager.class);

    public void init(List<LifecycleState> states) throws LifecycleManagementException {
        lifecycleStates = new HashMap<>();
        APIPublisherService publisher = new APIPublisherServiceImpl();
        List<DefaultPermission> allDefaultPermissions = new ArrayList<>();
        for (LifecycleState lifecycleState : states) {
            if (lifecycleState.getProceedingStates() != null) {
                lifecycleState.getProceedingStates().replaceAll(String::toUpperCase);
            }
            lifecycleStates.put(lifecycleState.getName().toUpperCase(), lifecycleState);
            DefaultPermission defaultPermission = new DefaultPermission();
            defaultPermission.setName(PermissionUtils.ADMIN_PERMISSION_REGISTRY_PATH + lifecycleState.getPermission());
            defaultPermission.setScopeMapping(lifecycleState.getScopeMapping());
            allDefaultPermissions.add(defaultPermission);
        }
        try {
            APIPublisherStartupHandler.updateScopeMetadataEntryAndRegistryWithDefaultScopes(allDefaultPermissions);
            publisher.addDefaultScopesIfNotExist(allDefaultPermissions);
        } catch (APIManagerPublisherException e) {
            String errorMsg = "Failed to update API publisher with default permissions.";
            log.error(errorMsg, e);
            throw new LifecycleManagementException(errorMsg, e);
        }
    }

    public Map<String, LifecycleState> getLifecycleConfig() throws LifecycleManagementException {
        if (lifecycleStates == null) {
            String msg = "Lifecycle configuration in not initialized.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        return lifecycleStates;
    }

    public List<String> getNextLifecycleStates(String currentLifecycleState) {
        return lifecycleStates.get(currentLifecycleState.toUpperCase()).getProceedingStates();
    }

    public boolean isValidStateChange(String currentState, String nextState, String username, int tenantId)
            throws LifecycleManagementException {
        UserRealm userRealm;
        String permission = getPermissionForStateChange(nextState);
        if (permission != null) {
            try {
                userRealm = DataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
                if (userRealm != null && userRealm.getAuthorizationManager() != null && userRealm
                        .getAuthorizationManager()
                        .isUserAuthorized(username, PermissionUtils.getAbsolutePermissionPath(permission),
                                Constants.UI_EXECUTE)) {
                    if (currentState.equalsIgnoreCase(nextState)) {
                        return true;
                    }
                    LifecycleState matchingState = getMatchingState(currentState);
                    if (matchingState != null) {
                        return getMatchingNextState(matchingState.getProceedingStates(), nextState);
                    }
                    return false;
                }
                return false;
            } catch (UserStoreException e) {
                String msg = "UserStoreException exception from changing the state from : " + currentState + "  to: "
                        + nextState + " with username : " + username + " and tenant Id : " + tenantId;
                log.error(msg, e);
                throw new LifecycleManagementException(msg, e);
            }
        } else {
            String msg = "Required permissions cannot be found for the state : " + nextState;
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    private LifecycleState getMatchingState(String currentState) {
        for (Map.Entry<String, LifecycleState> lifecycyleState : lifecycleStates.entrySet()) {
            if (lifecycyleState.getKey().equalsIgnoreCase(currentState)) {
                return lifecycyleState.getValue();
            }
        }
        return null;
    }

    private boolean getMatchingNextState(List<String> proceedingStates, String nextState) {
        for (String stateName : proceedingStates) {
            if (stateName.equalsIgnoreCase(nextState)) {
                return true;
            }
        }
        return false;
    }

    private String getPermissionForStateChange(String nextState) {
        for (Map.Entry<String, LifecycleState> lifecycyleState : lifecycleStates.entrySet()) {
            if (lifecycyleState.getKey().equalsIgnoreCase(nextState)) {
                return lifecycyleState.getValue().getPermission();
            }
        }
        return null;
    }

    public boolean isDeletableState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isDeletableState();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isUpdatableState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isAppUpdatable();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isInstallableState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isAppInstallable();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isInitialState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isInitialState();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isEndState(String state) throws LifecycleManagementException {
        LifecycleState currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isEndState();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public String getInitialState() throws LifecycleManagementException {
        String initialState;
        for (Map.Entry<String, LifecycleState> lifecycleState : lifecycleStates.entrySet()) {
            if (lifecycleState.getValue().isInitialState()) {
                initialState = lifecycleState.getKey();
                return initialState;
            }
        }
        String msg = "Haven't defined the initial state in the application-manager.xml. Please add initial state "
                + "to the <LifecycleStates> section in the app-manager.xml";
        log.error(msg);
        throw new LifecycleManagementException(msg);
    }

    public String getEndState() throws LifecycleManagementException {
        String endState = null;
        for (Map.Entry<String, LifecycleState> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getValue().isEndState()) {
                endState = stringStateEntry.getKey();
                break;
            }
        }
        if (endState == null) {
            String msg = "Haven't defined the end state in the application-manager.xml. Please add end state "
                    + "to the <LifecycleStates> section in the app-manager.xml";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        return endState;
    }

    public String getInstallableState() throws LifecycleManagementException {
        String installableState = null;
        for (Map.Entry<String, LifecycleState> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getValue().isAppInstallable()) {
                installableState = stringStateEntry.getKey();
                break;
            }
        }
        if (installableState == null) {
            String msg = "Haven't defined the installable state in the application-manager.xml. Please add installable "
                    + "state to the <LifecycleStates> section in the app-manager.xml";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
        return installableState;
    }

    public boolean isStateExist(String currentState) {
        for (Map.Entry<String, LifecycleState> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getKey().equalsIgnoreCase(currentState)) {
                return true;
            }
        }
        return false;
    }

    public void setLifecycleStates(Map<String, LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
    }
}
