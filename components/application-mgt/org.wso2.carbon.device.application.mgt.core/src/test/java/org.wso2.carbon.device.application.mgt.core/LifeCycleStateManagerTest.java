package org.wso2.carbon.device.application.mgt.core;

import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.common.config.LifecycleState;

import java.util.HashMap;
import java.util.List;


class LifeCycleStateManagerTest extends LifecycleStateManager {

    void initializeLifeCycleDetails(List<LifecycleState> lifecycleStates) {
        HashMap<String, LifecycleState> lifecycleStatesMap = new HashMap<>();
        for (LifecycleState lifecycleState : lifecycleStates) {
            if (lifecycleState.getProceedingStates() != null) {
                lifecycleState.getProceedingStates().replaceAll(String::toUpperCase);
            }
            lifecycleStatesMap.put(lifecycleState.getName().toUpperCase(), lifecycleState);
        }
        setLifecycleStates(lifecycleStatesMap);
    }
}
