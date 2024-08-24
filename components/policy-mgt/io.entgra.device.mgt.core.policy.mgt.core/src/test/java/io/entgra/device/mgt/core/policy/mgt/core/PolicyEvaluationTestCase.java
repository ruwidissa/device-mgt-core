/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package io.entgra.device.mgt.core.policy.mgt.core;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyAdministratorPoint;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyEvaluationException;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyEvaluationPoint;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.policy.mgt.core.internal.PolicyManagementDataHolder;
import io.entgra.device.mgt.core.policy.mgt.core.services.SimplePolicyEvaluationTest;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.ntask.common.TaskException;

import java.util.Collections;
import java.util.List;

public class PolicyEvaluationTestCase extends BasePolicyManagementDAOTest {

    private static final String ANDROID = "android";
    private static final Log log = LogFactory.getLog(PolicyEvaluationTestCase.class);


    @BeforeClass
    public void init() throws Exception {
        log.info("Initializing policy tests");
        super.initializeServices();
        PolicyEvaluationPoint evaluationPoint = new SimplePolicyEvaluationTest();
        PolicyManagementDataHolder.getInstance().setPolicyEvaluationPoint(evaluationPoint.getName(), evaluationPoint);
    }

    @Test
    public void activatePolicies() {
        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();
        PolicyAdministratorPoint administratorPoint = null;
        try {
            administratorPoint = policyManagerService.getPAP();
        } catch (PolicyManagementException e) {
            log.error("Error occurred while loading the policy administration point", e);
            Assert.fail();
        }

        List<Policy> policies = null;
        try {
            policies = policyManagerService.getPolicies(ANDROID);
        } catch (PolicyManagementException e) {
            log.error("Error occurred while retrieving the list of policies defined against the device type '" +
                    ANDROID + "'", e);
            Assert.fail();
        }

        for (Policy policy : policies) {
            log.debug("Policy status : " + policy.getPolicyName() + "  - " + policy.isActive() + " - " + policy
                    .isUpdated() + " Policy id : " + policy.getId());

            if (!policy.isActive()) {
                try {
                    administratorPoint.activatePolicy(policy.getId());
                } catch (PolicyManagementException e) {
                    log.error("Error occurred while activating the policy, which carries the id '" +
                            policy.getId() + "'", e);
                    Assert.fail();
                }
            }
        }
        // This cannot be called due to task service cannot be started from the
        //administratorPoint.publishChanges();
    }

    @DataProvider(name = "deviceIdentifierDataProvider")
    public static Object[][] deviceIdentifierData() {
        return new Object[][] {{new DeviceIdentifier()}};
    }

    @Test(dependsOnMethods = "activatePolicies", dataProvider = "deviceIdentifierDataProvider")
    public void getEffectivePolicy(DeviceIdentifier identifier) throws DeviceManagementException, PolicyEvaluationException {

        log.debug("Getting effective policy for device started ..........");

        DeviceManagementProviderService service = new DeviceManagementProviderServiceImpl();
        List<Device> devices = service.getAllDevices(ANDROID, false);

        PolicyEvaluationPoint evaluationPoint = PolicyManagementDataHolder.getInstance().getPolicyEvaluationPoint();

        for (Device device : devices) {
            identifier.setType(device.getType());
            identifier.setId(device.getDeviceIdentifier());
            Policy policy = evaluationPoint.getEffectivePolicy(identifier);

            if (policy != null) {
                log.debug("Name of the policy applied to device is " + policy.getPolicyName());
            } else {
                log.debug("No policy is applied to device.");
            }
        }
    }


    @Test(dependsOnMethods = ("getEffectivePolicy"))
    public void updatePriorities() throws PolicyManagementException, TaskException {

        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();
        PolicyAdministratorPoint administratorPoint = policyManagerService.getPAP();

        List<Policy> policies = administratorPoint.getPolicies();

        log.debug("Re-enforcing policy started...!");

        int size = policies.size();

        sortPolicies(policies);
        int x = 0;
        for (Policy policy : policies) {
            policy.setPriorityId(size - x);
            x++;
        }

        administratorPoint.updatePolicyPriorities(policies);
     //   administratorPoint.publishChanges();
    }


    @Test(dependsOnMethods = ("updatePriorities"))
    public void checkDelegations() {

        log.debug("Delegation methods calls started because tasks cannot be started due to osgi constraints.....!");

        //DelegationTask delegationTask = new DelegationTask();
        //delegationTask.execute();
    }

    public void sortPolicies(List<Policy> policyList)  {
        Collections.sort(policyList);
    }
}
