package org.wso2.carbon.device.application.mgt.core.lifecycle;

import org.wso2.carbon.device.application.mgt.core.lifecycle.config.LifecycleState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the activities related to lifecycle management
 */
public class LifecycleStateManger {

    private Map<String, State> lifecycleStates;

    public LifecycleStateManger(List<LifecycleState> states) {
        lifecycleStates = new HashMap<>();
        for (LifecycleState s : states) {
            lifecycleStates.put(s.getName(), new State(s.getName(), s.getProceedingStates()));
        }
    }

    public Set<String> getNextLifecycleStates(String currentLifecycleState) {
        return lifecycleStates.get(currentLifecycleState).getProceedingStates();
    }

    public boolean isValidStateChange(String currentState, String nextState) {
        if (lifecycleStates.get(currentState).getProceedingStates().contains(nextState)) {
            return true;
        }
        return false;
    }
}
