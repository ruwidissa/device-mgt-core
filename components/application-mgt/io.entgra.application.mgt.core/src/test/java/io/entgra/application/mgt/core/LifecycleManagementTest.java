/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.core;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.entgra.application.mgt.common.exception.LifecycleManagementException;
import io.entgra.application.mgt.core.config.Configuration;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.core.lifecycle.LifecycleStateManager;
import io.entgra.application.mgt.common.config.LifecycleState;

import java.util.List;

public class LifecycleManagementTest {

    private List<LifecycleState> lifecycleStates;
    private LifecycleStateManager lifecycleStateManager;

    private final String CURRENT_STATE = "Approved";
    private final String NEXT_STATE = "Published";
    private final String BOGUS_STATE = "Retired";
    private final String UPDATABLE_STATE = "Created";
    private final String NON_UPDATABLE_STATE = "Retired";
    private final String INSTALLABLE_STATE = "Published";
    private final String INITIAL_STATE = "Created";
    private final String END_STATE = "Retired";


    @BeforeClass
    public void init() {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Configuration configuration = configurationManager.getConfiguration();
        lifecycleStates = configuration.getLifecycleStates();
        lifecycleStateManager = new LifeCycleStateManagerTest();
        ((LifeCycleStateManagerTest) lifecycleStateManager).initializeLifeCycleDetails(lifecycleStates);
    }

    @Test
    public void checkValidNextLifecycleState() {
        List<String> proceedingStates = lifecycleStateManager.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertTrue("Invalid proceeding state of: " + CURRENT_STATE,
                proceedingStates.contains(NEXT_STATE.toUpperCase()));
    }

    @Test
    public void checkInvalidNextLifecycleState() {
        List<String> proceedingStates = lifecycleStateManager.getNextLifecycleStates(CURRENT_STATE);
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
        List<String> proceedingStates = lifecycleStateManager.getNextLifecycleStates(CURRENT_STATE);
        Assert.assertFalse("Invalid proceeding state of: " + CURRENT_STATE,
                proceedingStates.contains(BOGUS_STATE.toUpperCase()));
    }
}
