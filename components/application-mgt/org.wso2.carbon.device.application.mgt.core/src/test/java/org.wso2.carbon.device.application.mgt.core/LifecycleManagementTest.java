package org.wso2.carbon.device.application.mgt.core;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.application.mgt.core.config.Configuration;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManger;
import org.wso2.carbon.device.application.mgt.core.lifecycle.config.LifecycleState;

import java.util.List;
import java.util.Set;

public class LifecycleManagementTest {

    private List<LifecycleState> lifecycleStates;
    private LifecycleStateManger lifecycleStateManger;

    private final String CURRENT_STATE = "Approved";
    private final String NEXT_STATE = "Published";
    private final String BOGUS_STATE = "Removed";

    @BeforeClass
    public void init() {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Configuration configuration = configurationManager.getConfiguration();
        lifecycleStates = configuration.getLifecycleStates();
        lifecycleStateManger = new LifecycleStateManger(lifecycleStates);
    }

    @Test
    public void checkValidNextLifecycleState() {
        Set<String> proceedingStates = lifecycleStateManger.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertTrue("Invalid proceeding state of: " + CURRENT_STATE,
                          proceedingStates.contains(NEXT_STATE));
    }

    @Test
    public void checkInvalidNextLifecycleState() {
        Set<String> proceedingStates = lifecycleStateManger.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertFalse("Invalid proceeding state of: " + CURRENT_STATE,
                          proceedingStates.contains(BOGUS_STATE));
    }

    @Test
    public void checkValidStateChange() {
        Assert.assertTrue("Invalid state transition from: " + CURRENT_STATE + " to: " + NEXT_STATE,
                           lifecycleStateManger.isValidStateChange(CURRENT_STATE, NEXT_STATE));
    }

    @Test
    public void checkInvalidStateChange() {
        Assert.assertFalse("Invalid state transition from: " + CURRENT_STATE + " to: " + BOGUS_STATE,
                          lifecycleStateManger.isValidStateChange(CURRENT_STATE, BOGUS_STATE));
    }

}
