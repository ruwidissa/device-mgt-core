package org.wso2.carbon.device.application.mgt.core.lifecycle;

import org.wso2.carbon.device.application.mgt.core.lifecycle.config.LifecycleState;

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
        if (currentState.equalsIgnoreCase(nextState)) {
            return true;
        }

        State state = getMatchingState(currentState);
        if (state != null) {
            return getMatchingNextState(state.getProceedingStates(), nextState);
        }
        return false;
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
}
