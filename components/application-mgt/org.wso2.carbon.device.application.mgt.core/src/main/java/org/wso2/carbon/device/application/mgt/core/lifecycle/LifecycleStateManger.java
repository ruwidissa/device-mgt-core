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
public class LifecycleStateManger {

    private Map<String, State> lifecycleStates;
    private static Log log = LogFactory.getLog(LifecycleStateManger.class);

    public void init(List<LifecycleState> states) throws LifecycleManagementException {
        lifecycleStates = new HashMap<>();
        for (LifecycleState s : states) {
            if (s.getProceedingStates() != null) {
                s.getProceedingStates().replaceAll(String::toUpperCase);
            }
            lifecycleStates.put(s.getName().toUpperCase(), new State(s.getName().toUpperCase(),
                    s.getProceedingStates(), s.getPermission(),s.isAppUpdatable(),s.isAppInstallable(),
                    s.isInitialState(),s.isEndState()));
            try {
                PermissionUtils.putPermission(s.getPermission());
            } catch (PermissionManagementException e) {
                log.error("Error when adding permission " + s.getPermission() + "  related to the state: "
                        + s.getName(), e);
                throw new LifecycleManagementException (
                        "Error when adding permission " + s.getPermission() + "  related to the state: "
                                + s.getName(), e);
            }
        }
    }

    public Set<String> getNextLifecycleStates(String currentLifecycleState) {
        return lifecycleStates.get(currentLifecycleState.toUpperCase()).getProceedingStates();
    }

    public boolean isValidStateChange(String currentState, String nextState, String username,
                                      int tenantId) throws LifecycleManagementException {

        UserRealm userRealm = null;
        String permission = getPermissionForStateChange(nextState);
        if(permission != null) {
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
        }else{
            throw new LifecycleManagementException(
                    "Required permissions cannot be found for the state : "+nextState);
        }
    }

    private State getMatchingState(String currentState) {
        Iterator it = lifecycleStates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(pair.getKey().toString().equalsIgnoreCase(currentState)) {
                return lifecycleStates.get(pair.getKey().toString());
            }
            it.remove();
        }
        return null;
    }


    private boolean getMatchingNextState(Set<String> proceedingStates, String nextState) {
        for (String state: proceedingStates) {
            if (state.equalsIgnoreCase(nextState)) {
                return true;
            }
        }
        return false;
    }

    private String getPermissionForStateChange(String nextState){
        Iterator it = lifecycleStates.entrySet().iterator();
        State nextLifecycleState;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(pair.getKey().toString().equalsIgnoreCase(nextState)) {
                nextLifecycleState = lifecycleStates.get(nextState);
                return nextLifecycleState.getPermission();
            }
            it.remove();
        }
        return null;
    }
}
