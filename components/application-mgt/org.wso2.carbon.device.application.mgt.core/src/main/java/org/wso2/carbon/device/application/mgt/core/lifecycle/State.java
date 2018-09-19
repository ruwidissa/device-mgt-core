package org.wso2.carbon.device.application.mgt.core.lifecycle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents the state of the lifecycle
 */
public class State {

    private Set<String> proceedingStates;
    private String stateName;

    public State(String stateName, List<String> states) {
        this.stateName = stateName;
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

}
