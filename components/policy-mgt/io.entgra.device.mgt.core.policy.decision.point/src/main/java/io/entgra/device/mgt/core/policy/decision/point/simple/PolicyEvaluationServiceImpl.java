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

package io.entgra.device.mgt.core.policy.decision.point.simple;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.ProfileFeature;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyEvaluationException;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyEvaluationPoint;

import java.util.List;

public class PolicyEvaluationServiceImpl implements PolicyEvaluationPoint {

    private SimpleEvaluationImpl evaluation;
    private static final String policyEvaluationPoint = "Simple";

    public PolicyEvaluationServiceImpl() {
        evaluation = new SimpleEvaluationImpl();
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        return evaluation.getEffectivePolicy(deviceIdentifier);
    }

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier)
            throws PolicyEvaluationException {
        List<ProfileFeature> effectiveFeatures = null;
        Policy effectivePolicy = evaluation.getEffectivePolicy(deviceIdentifier);
        if (effectivePolicy != null) {
            effectiveFeatures = effectivePolicy.getProfile().getProfileFeaturesList();
        }
        return effectiveFeatures;
    }

    @Override
    public String getName() {
        return policyEvaluationPoint;
    }
}
