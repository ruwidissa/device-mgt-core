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

package io.entgra.device.mgt.core.cea.mgt.core.dao;

import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.core.exception.CEAPolicyManagementDAOException;

import java.util.Date;
import java.util.List;

/**
 * DAO class for Conditional Email Access management
 */
public interface CEAPolicyDAO {
    /**
     * Create CEA policy for a tenant if a CEA policy not already exists
     * @param ceaPolicy {@link CEAPolicy}
     * @return Created CEA policy
     * @throws CEAPolicyManagementDAOException Throws when error occurred while creating CEA policy
     */
    CEAPolicy createCEAPolicy(CEAPolicy ceaPolicy) throws CEAPolicyManagementDAOException;

    /**
     * Retrieve CEA policy
     * @return {@link CEAPolicy}
     * @throws CEAPolicyManagementDAOException Throws when error occurred while retrieving CEA policy
     */
    CEAPolicy retrieveCEAPolicy() throws CEAPolicyManagementDAOException;

    /**
     * Retrieve all available CEA policies
     * @return List of CEA policies
     * @throws CEAPolicyManagementDAOException Throws when error occurred while retrieving CEA policies
     */
    List<CEAPolicy> retrieveAllCEAPolicies() throws CEAPolicyManagementDAOException;

    /**
     * Update CEA policy
     * @param existingCEAPolicy Existing CEA policy
     * @param ceaPolicy Updated CEA policy
     * @return Updated CEA policy
     * @throws CEAPolicyManagementDAOException Throws when error occurred while updating CEA policy
     */
    CEAPolicy updateCEAPolicy(CEAPolicy existingCEAPolicy, CEAPolicy ceaPolicy) throws CEAPolicyManagementDAOException;

    /**
     * Update last sync time with the active sync server
     * @param status True on a successful sync, otherwise false
     * @param syncedTime Synced time stamp
     * @throws CEAPolicyManagementDAOException Throws when error occurred while updating sync time
     */
    void updateLastSyncedTime(boolean status, Date syncedTime) throws CEAPolicyManagementDAOException;

    /**
     * Delete CEA policy
     * @throws CEAPolicyManagementDAOException Throws when error occurred while deleting CEA policy
     */
    void deleteCEAPolicy() throws CEAPolicyManagementDAOException;
}
