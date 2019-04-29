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
    private final String INITIAL_STATE = "Created";
    private final String END_STATE = "Removed";


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

    @Test
    public void CheckUpdatableState() throws LifecycleManagementException {
        boolean isUpdatable = lifecycleStateManager.isUpdatableState(UPDATABLE_STATE);
        System.out.println(isUpdatable);
        Assert.assertTrue("Updatable state: " + UPDATABLE_STATE, isUpdatable);
    }

    @Test
    public void CheckNonUpdatableState() throws LifecycleManagementException {
        boolean isUpdatable = lifecycleStateManager.isUpdatableState(NON_UPDATABLE_STATE);
        Assert.assertFalse("Non Updatable state: " + NON_UPDATABLE_STATE, isUpdatable);
    }

    @Test
    public void CheckInstallableState() throws LifecycleManagementException {
        boolean isInstallable = lifecycleStateManager.isInstallableState(INSTALLABLE_STATE);
        Assert.assertTrue("Installable state: " + INSTALLABLE_STATE, isInstallable);
    }

    @Test
    public void CheckUnInstallableState() throws LifecycleManagementException {
        boolean isInstallable = lifecycleStateManager.isInstallableState(UNINSTALlABLE_STATE);
        Assert.assertFalse("UnInstallable state: " + UNINSTALlABLE_STATE, isInstallable);
    }

    @Test
    public void CheckGetInitialState() throws LifecycleManagementException {
        boolean isInitialState = lifecycleStateManager.getInitialState().equalsIgnoreCase(INITIAL_STATE);
        Assert.assertTrue("Initial state: " + INITIAL_STATE, isInitialState);
    }

    @Test
    public void CheckGetNonInitialState() throws LifecycleManagementException {
        boolean isInitialState = lifecycleStateManager.getInitialState().equalsIgnoreCase(END_STATE);
        Assert.assertFalse("Non initial state: " + END_STATE, isInitialState);
    }

    @Test
    public void CheckGetEndState() throws LifecycleManagementException {
        boolean isEndState = lifecycleStateManager.getEndState().equalsIgnoreCase(END_STATE);
        Assert.assertTrue("End State: " + END_STATE, isEndState);
    }

    @Test
    public void CheckGetNonEndState() throws LifecycleManagementException {
        boolean isEndState = lifecycleStateManager.getEndState().equalsIgnoreCase(INITIAL_STATE);
        Assert.assertFalse("Non End State : " + INITIAL_STATE, isEndState);
    }

    @Test
    public void CheckIsInitialState() throws LifecycleManagementException {
        boolean isInitialState = lifecycleStateManager.isInitialState(INITIAL_STATE);
        Assert.assertTrue("Initial state: " + INITIAL_STATE, isInitialState);
    }

    @Test
    public void CheckIsNonInitialState() throws LifecycleManagementException {
        boolean isInitialState = lifecycleStateManager.isInitialState(END_STATE);
        Assert.assertFalse("Non Initial state: " + END_STATE, isInitialState);
    }

    @Test
    public void CheckIsEndState() throws LifecycleManagementException {
        boolean isEndState = lifecycleStateManager.isEndState(END_STATE);
        Assert.assertTrue("End state: " + END_STATE, isEndState);
    }

    @Test
    public void CheckIsNonEndState() throws LifecycleManagementException {
        boolean isEndState = lifecycleStateManager.isEndState(INITIAL_STATE);
        Assert.assertFalse("Non End state: " + INITIAL_STATE, isEndState);
    }

    @Test
    public void check() {
        Set<String> proceedingStates = lifecycleStateManager.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertFalse("Invalid proceeding state of: " + CURRENT_STATE,
                proceedingStates.contains(BOGUS_STATE.toUpperCase()));
    }
}
