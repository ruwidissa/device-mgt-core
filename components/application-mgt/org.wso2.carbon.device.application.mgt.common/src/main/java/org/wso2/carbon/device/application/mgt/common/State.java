package org.wso2.carbon.device.application.mgt.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents the state of the lifecycle
 */
public class State {

    private Set<String> proceedingStates;
    private String stateName;
    private String permission;
    private boolean isAppUpdatable;
    private boolean isAppInstallable;
    private boolean isInitialState;
    private boolean isEndState;

    public State(String stateName, List<String> states, String permission, boolean isAppUpdatable,
                 boolean isAppInstallable, boolean isInitialState, boolean isEndState) {
        this.stateName = stateName;
        this.permission = permission;
        this.isAppUpdatable=isAppUpdatable;
        this.isAppInstallable=isAppInstallable;
        this.isInitialState=isInitialState;
        this.isEndState=isEndState;
        if (states != null && !states.isEmpty()) {
            proceedingStates = new HashSet<>(states);
        }
    }

    public String getState() {
        return stateName;
    }

    public Set<String> getProceedingStates() {
        return proceedingStates;
    }

    public String getPermission(){ return permission;}

    public boolean isAppUpdatable(){ return isAppUpdatable;}

    public boolean isAppInstallable(){ return isAppInstallable;}

    public boolean isInitialState(){ return isInitialState;}

    public boolean isEndState(){ return isEndState;}

}
