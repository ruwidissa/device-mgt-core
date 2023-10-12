/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.common.service;

import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAEnforcementException;

public interface CEAEnforcementService {
    /**
     * Sync default access policy with active sync server
     *
     * @param ceaPolicy {@link CEAPolicy}
     * @throws CEAEnforcementException Throws when error occurred while enforcing the policy
     */
    void enforceDefaultAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException;

    /**
     * Enforce email outlook access policy
     *
     * @param ceaPolicy {@link CEAPolicy}
     * @throws CEAEnforcementException Throws when error occurred while enforcing the policy
     */
    void enforceEmailOutlookAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException;

    /**
     * Enforce POP/IMAP access policy
     *
     * @param ceaPolicy {@link CEAPolicy}
     * @throws CEAEnforcementException Throws when error occurred while enforcing the policy
     */
    void enforcePOPIMAPAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException;

    /**
     * Enforce web outlook access policy
     *
     * @param ceaPolicy {@link CEAPolicy}
     * @throws CEAEnforcementException Throws when error occurred while enforcing the policy
     */
    void enforceWebOutlookAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException;

    /**
     * Enforce conditional email access policy honoring to the grace period
     *
     * @param ceaPolicy {@link CEAPolicy}
     * @throws CEAEnforcementException Throws when error occurred while enforcing the policy
     */
    void enforceConditionalAccessPolicy(CEAPolicy ceaPolicy) throws CEAEnforcementException;
}
