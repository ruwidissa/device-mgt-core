package org.wso2.carbon.device.application.mgt.core;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.application.mgt.common.exception.LifecycleManagementException;
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
    private final String UPDATABLE_STATE = "Created";
    private final String NON_UPDATABLE_STATE= "Removed";
    private final String INSTALLABLE_STATE = "Published";
    private final String UNINSTALlABLE_STATE = "Removed";



    @BeforeClass
    public void init() throws LifecycleManagementException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Configuration configuration = configurationManager.getConfiguration();
        lifecycleStates = configuration.getLifecycleStates();
        lifecycleStateManger = new LifecycleStateManger();
        lifecycleStateManger.initializeLifeCycleDetails(lifecycleStates);
    }

   @Test
    public void checkValidNextLifecycleState() {
        Set<String> proceedingStates = lifecycleStateManger.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertTrue("Invalid proceeding state of: " + CURRENT_STATE,
                          proceedingStates.contains(NEXT_STATE.toUpperCase()));
    }

    @Test
    public void checkInvalidNextLifecycleState() {
        Set<String> proceedingStates = lifecycleStateManger.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertFalse("Invalid proceeding state of: " + CURRENT_STATE,
                          proceedingStates.contains(BOGUS_STATE.toUpperCase()));
    }

    @Test
    public void CheckUpdatableState() {
        Boolean isUpdatable = lifecycleStateManger.isUpdatable(UPDATABLE_STATE);
        System.out.println(isUpdatable);
        Assert.assertTrue("Updatable state: " + UPDATABLE_STATE, isUpdatable);
    }

   @Test
    public void CheckNonUpdatableState() {
        Boolean isUpdatable = lifecycleStateManger.isUpdatable(NON_UPDATABLE_STATE);
        Assert.assertFalse("Non Updatable state: " + CURRENT_STATE, isUpdatable);
    }

    @Test
    public void CheckInstallableState() {
        Boolean isInstallable = lifecycleStateManger.isInstallable(INSTALLABLE_STATE);
        Assert.assertTrue("Installable state: " + INSTALLABLE_STATE,isInstallable);
    }

    @Test
    public void CheckUnInstallableState() {
        Boolean isInstallable = lifecycleStateManger.isInstallable(UNINSTALlABLE_STATE);
        Assert.assertFalse("UnInstallable state: " + UNINSTALlABLE_STATE,isInstallable);
    }

    @Test
    public void check() {
        Set<String> proceedingStates = lifecycleStateManger.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertFalse("Invalid proceeding state of: " + CURRENT_STATE,
                proceedingStates.contains(BOGUS_STATE.toUpperCase()));
    }


}
