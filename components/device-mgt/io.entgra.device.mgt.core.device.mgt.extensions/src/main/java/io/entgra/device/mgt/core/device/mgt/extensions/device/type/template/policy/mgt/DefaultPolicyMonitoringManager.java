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
package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.policy.mgt;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.PolicyMonitoringManager;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.monitor.ComplianceFeature;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.monitor.NonComplianceData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation policy monitoring manager.
 */
public class DefaultPolicyMonitoringManager implements PolicyMonitoringManager {

    private static final Log log = LogFactory.getLog(DefaultPolicyMonitoringManager.class);

    @Override
    public NonComplianceData checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Policy policy, Object response) {
        if (log.isDebugEnabled()) {
            log.debug("Checking policy compliance status of device '" + deviceIdentifier.getId() + "'");
        }
        NonComplianceData nonComplianceData = new NonComplianceData();
        if (response == null || policy == null) {
            return nonComplianceData;
        }

        List<ComplianceFeature> complianceFeatures = (List<ComplianceFeature>) response;
        List<ComplianceFeature> nonComplianceFeatures = new ArrayList<>();

        for (ComplianceFeature complianceFeature : complianceFeatures) {
            if (!complianceFeature.isCompliant()) {
                nonComplianceFeatures.add(complianceFeature);
                nonComplianceData.setStatus(false);
            }
        }
        nonComplianceData.setComplianceFeatures(nonComplianceFeatures);

        return nonComplianceData;
    }
}
