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

package org.wso2.carbon.device.application.mgt.core.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.lifecycle.config.LifecycleState;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.device.mgt.core.search.mgt.Constants;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the activities related to lifecycle management
 */
public class LifecycleStateManager {

    private Map<String, State> lifecycleStates;
    private static Log log = LogFactory.getLog(LifecycleStateManager.class);

    public void init(List<LifecycleState> states) throws LifecycleManagementException {
        lifecycleStates = new HashMap<>();
        for (LifecycleState s : states) {
            if (s.getProceedingStates() != null) {
                s.getProceedingStates().replaceAll(String::toUpperCase);
            }
            lifecycleStates.put(s.getName().toUpperCase(), new State(s.getName().toUpperCase(),
                    s.getProceedingStates(), s.getPermission(), s.isAppUpdatable(), s.isAppInstallable(),
                    s.isInitialState(), s.isEndState()));
            try {
                PermissionUtils.putPermission(s.getPermission());
            } catch (PermissionManagementException e) {
                String msg = "Error when adding permission " + s.getPermission() + "  related to the state: "
                        + s.getName();
                log.error(msg, e);
                throw new LifecycleManagementException(msg, e);
            }
        }
    }


    public Set<String> getNextLifecycleStates(String currentLifecycleState) {
        return lifecycleStates.get(currentLifecycleState.toUpperCase()).getProceedingStates();
    }

    public boolean isValidStateChange(String currentState, String nextState, String username, int tenantId) throws
            LifecycleManagementException {

        UserRealm userRealm;
        String permission = getPermissionForStateChange(nextState);
        if (permission != null) {
            try {
                userRealm = DataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
                if (userRealm != null && userRealm.getAuthorizationManager() != null &&
                        userRealm.getAuthorizationManager().isUserAuthorized(username,
                                PermissionUtils.getAbsolutePermissionPath(permission),
                                Constants.UI_EXECUTE)) {
                    if (currentState.equalsIgnoreCase(nextState)) {
                        return true;
                    }
                    State state = getMatchingState(currentState);
                    if (state != null) {
                        return getMatchingNextState(state.getProceedingStates(), nextState);
                    }
                    return false;
                }
                return false;
            } catch (UserStoreException e) {
                throw new LifecycleManagementException(
                        "UserStoreException exception from changing the state from : " + currentState + "  to: "
                                + nextState + " with username : " + username + " and tenant Id : " + tenantId, e);
            }
        } else {
            throw new LifecycleManagementException(
                    "Required permissions cannot be found for the state : " + nextState);
        }
    }

    private State getMatchingState(String currentState) {
        for (Map.Entry<String, State> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getKey().equalsIgnoreCase(currentState)) {
                return lifecycleStates.get(stringStateEntry.getKey());
            }
        }
        return null;
    }


    private boolean getMatchingNextState(Set<String> proceedingStates, String nextState) {
        for (String state : proceedingStates) {
            if (state.equalsIgnoreCase(nextState)) {
                return true;
            }
        }
        return false;
    }

    private String getPermissionForStateChange(String nextState) {
        Iterator it = lifecycleStates.entrySet().iterator();
        State nextLifecycleState;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (pair.getKey().toString().equalsIgnoreCase(nextState)) {
                nextLifecycleState = lifecycleStates.get(nextState);
                return nextLifecycleState.getPermission();
            }
            it.remove();
        }
        return null;
    }

    public boolean isUpdatableState(String state) throws LifecycleManagementException {
        State currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isAppUpdatable();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public boolean isInstallableState(String state) throws LifecycleManagementException {
        State currentState = getMatchingState(state);
        if (currentState != null) {
            return currentState.isAppInstallable();
        } else {
            String msg = "Couldn't find a lifecycle state that matches with " + state + " state.";
            log.error(msg);
            throw new LifecycleManagementException(msg);
        }
    }

    public String getInitialState() throws LifecycleManagementException {
        String initialState = null;
        for (Map.Entry<String, State> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getValue().isInitialState()) {
                initialState = stringStateEntry.getKey();
                break;
            }
        }
        if (initialState == null){
            String msg = "Haven't defined the initial state in the application-manager.xml. Please add initial state "
                    + "to the <LifecycleStates> section in the app-manager.xml";
            log.error(msg);
            throw  new LifecycleManagementException(msg);
        }
        return initialState;
    }

    public String getEndState() throws LifecycleManagementException {
        String endState = null;
        for (Map.Entry<String, State> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getValue().isEndState()) {
                endState = stringStateEntry.getKey();
                break;
            }
        }
        if (endState == null){
            String msg = "Haven't defined the end state in the application-manager.xml. Please add end state "
                    + "to the <LifecycleStates> section in the app-manager.xml";
            log.error(msg);
            throw  new LifecycleManagementException(msg);
        }
        return endState;
    }

    public boolean isStateExist(String currentState) {
        for (Map.Entry<String, State> stringStateEntry : lifecycleStates.entrySet()) {
            if (stringStateEntry.getKey().equalsIgnoreCase(currentState)) {
                return true;
            }
        }
        return false;
    }

    public void setLifecycleStates(Map<String, State> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
    }
}
