package org.wso2.carbon.device.application.mgt.core;

import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.common.State;
import org.wso2.carbon.device.application.mgt.common.config.LifecycleState;

import java.util.HashMap;
import java.util.List;


public class LifeCycleStateManagerTest extends LifecycleStateManager {

    public void initializeLifeCycleDetails(List<LifecycleState> states) {
        HashMap<String, State> lifecycleStates = new HashMap<>();
        for (LifecycleState s : states) {
            if (s.getProceedingStates() != null) {
                s.getProceedingStates().replaceAll(String::toUpperCase);
            }
            lifecycleStates.put(s.getName().toUpperCase(), new State(s.getName().toUpperCase(),
                    s.getProceedingStates(), s.getPermission(), s.isAppUpdatable(), s.isAppInstallable(),
                    s.isInitialState(), s.isEndState()));
        }
        setLifecycleStates(lifecycleStates);
    }
}
