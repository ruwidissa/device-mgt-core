package org.wso2.carbon.device.application.mgt.core;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
import org.wso2.carbon.device.application.mgt.core.config.Configuration;
import org.wso2.carbon.device.application.mgt.core.config.ConfigurationManager;
import org.wso2.carbon.device.application.mgt.core.lifecycle.LifecycleStateManager;
import org.wso2.carbon.device.application.mgt.core.lifecycle.config.LifecycleState;

import java.util.List;
import java.util.Set;

public class LifecycleManagementTest {

    private List<LifecycleState> lifecycleStates;
    private LifecycleStateManager lifecycleStateManager;

    private final String CURRENT_STATE = "Approved";
    private final String NEXT_STATE = "Published";
    private final String BOGUS_STATE = "Removed";
    private final String UPDATABLE_STATE = "Created";
    private final String NON_UPDATABLE_STATE = "Removed";
    private final String INSTALLABLE_STATE = "Published";
    private final String UNINSTALlABLE_STATE = "Removed";


    @BeforeClass
    public void init() throws LifecycleManagementException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Configuration configuration = configurationManager.getConfiguration();
        lifecycleStates = configuration.getLifecycleStates();
        lifecycleStateManager = new LifeCycleStateManagerTest();
        ((LifeCycleStateManagerTest) lifecycleStateManager).initializeLifeCycleDetails(lifecycleStates);
    }

    @Test
    public void checkValidNextLifecycleState() {
        Set<String> proceedingStates = lifecycleStateManager.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertTrue("Invalid proceeding state of: " + CURRENT_STATE,
                proceedingStates.contains(NEXT_STATE.toUpperCase()));
    }

    @Test
    public void checkInvalidNextLifecycleState() {
        Set<String> proceedingStates = lifecycleStateManager.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertFalse("Invalid proceeding state of: " + CURRENT_STATE,
                proceedingStates.contains(BOGUS_STATE.toUpperCase()));
    }

//    @Test
//    public void CheckUpdatableState() {
//        boolean isUpdatableState = lifecycleStateManager.isUpdatableState(UPDATABLE_STATE);
//        System.out.println(isUpdatableState);
//        Assert.assertTrue("Updatable state: " + UPDATABLE_STATE, isUpdatableState);
//    }
//
//    @Test
//    public void CheckNonUpdatableState() {
//        boolean isUpdatableState = lifecycleStateManager.isUpdatableState(NON_UPDATABLE_STATE);
//        Assert.assertFalse("Non Updatable state: " + NON_UPDATABLE_STATE, isUpdatableState);
//    }
//
//    @Test
//    public void CheckInstallableState() {
//        boolean isInstallableState = lifecycleStateManager.isInstallableState(INSTALLABLE_STATE);
//        Assert.assertTrue("Installable state: " + INSTALLABLE_STATE, isInstallableState);
//    }
//
//    @Test
//    public void CheckUnInstallableState() {
//        boolean isInstallableState = lifecycleStateManager.isInstallableState(UNINSTALlABLE_STATE);
//        Assert.assertFalse("UnInstallable state: " + UNINSTALlABLE_STATE, isInstallableState);
//    }
//
//    @Test
//    public void check() {
//        Set<String> proceedingStates = lifecycleStateManager.getNextLifecycleStates(CURRENT_STATE);
//        Assert.assertFalse("Invalid proceeding state of: " + CURRENT_STATE,
//                proceedingStates.contains(BOGUS_STATE.toUpperCase()));
//    }

}
