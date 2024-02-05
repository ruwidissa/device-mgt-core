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

package io.entgra.device.mgt.core.cea.mgt.core.mgt;

import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ui.CEAPolicyUIConfiguration;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAManagementException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyAlreadyExistsException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyNotFoundException;

import java.util.Date;
import java.util.List;

public interface CEAManager {
    /**
     * Retrieve conditional access policy UI configuration
     *
     * @return {@link CEAPolicyUIConfiguration}
     * @throws CEAManagementException Throws when retrieving UI configurations
     */
    CEAPolicyUIConfiguration getCEAPolicyUIConfiguration() throws CEAManagementException;

    /**
     * Trigger sync task with active sync server
     *
     * @throws CEAManagementException Throws when error occurred while triggering the sync operation
     */
    void syncNow() throws CEAManagementException;

    /**
     * Create conditional access policy
     *
     * @param ceaPolicy {@link CEAPolicy}
     * @return {@link CEAPolicy} Created conditional access policy
     * @throws CEAManagementException          Throws when error occurred while creating the policy
     * @throws CEAPolicyAlreadyExistsException Throws when conflict occurs
     */
    CEAPolicy createCEAPolicy(CEAPolicy ceaPolicy) throws CEAManagementException, CEAPolicyAlreadyExistsException;

    /**
     * Retrieve conditional access policy for the tenant
     *
     * @return {@link CEAPolicy}
     * @throws CEAManagementException Throws when error occurred while retrieving the policy
     */
    CEAPolicy retrieveCEAPolicy() throws CEAManagementException;

    /**
     * Retrieve all conditional access policies
     *
     * @return List of conditional access policies
     * @throws CEAManagementException Throws when error occurred while retrieving policies
     */
    List<CEAPolicy> retrieveAllCEAPolicies() throws CEAManagementException;

    /**
     * Update conditional access policy
     *
     * @param ceaPolicy {@link CEAPolicy}
     * @return {@link CEAPolicy} Returns update conditional access policy
     * @throws CEAManagementException     Throws when error occurred while updating the policy
     * @throws CEAPolicyNotFoundException Throws when policy doesn't exist
     */
    CEAPolicy updateCEAPolicy(CEAPolicy ceaPolicy) throws CEAManagementException, CEAPolicyNotFoundException;

    /**
     * Delete the conditional access policy
     *
     * @throws CEAManagementException     Throws when error occurred while deleting the policy
     * @throws CEAPolicyNotFoundException Throws when a conditional access policy doesn't exist
     */
    void deleteCEAPolicy() throws CEAManagementException, CEAPolicyNotFoundException;

    /**
     * Update sync status of the conditional access policy
     *
     * @param status     Whether the sync success or not
     * @param syncedTime Synced timestamp
     * @throws CEAManagementException Throws when error occurred while updating the status
     */
    void updateSyncStatus(boolean status, Date syncedTime) throws CEAManagementException;
}
