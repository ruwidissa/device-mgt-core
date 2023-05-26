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

package io.entgra.device.mgt.core.policy.decision.point.internal;

import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;
import org.wso2.carbon.user.core.service.RealmService;

public class PolicyDecisionPointDataHolder {

    private RealmService realmService;
    private PolicyManagerService policyManagerService;

    private static PolicyDecisionPointDataHolder dataHolder = new PolicyDecisionPointDataHolder();

    private PolicyDecisionPointDataHolder() {
    }

    public static PolicyDecisionPointDataHolder getInstance() {
        return dataHolder;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public PolicyManagerService getPolicyManagerService() {
        return policyManagerService;
    }

    public void setPolicyManagerService(PolicyManagerService policyManagerService) {
        this.policyManagerService = policyManagerService;
    }
}
