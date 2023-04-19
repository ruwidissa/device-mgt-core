/*
 *  Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.common.metadata.mgt;

import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import java.util.List;

/**
 * Defines the contract of MetadataManagementService.
 */
public interface MetadataManagementService {

    /**
     * Persist the provided Metadata entry.
     *
     * @param metadata  the Metadata entry to be persisted
     * @return          the Metadata entry along with the updated Metadata.id
     * @throws MetadataManagementException          If a data source related exception occurred
     * @throws MetadataKeyAlreadyExistsException    If the provided Metadata.metaKey already exist
     */
    Metadata createMetadata(Metadata metadata) throws MetadataManagementException, MetadataKeyAlreadyExistsException;

    /**
     * Get the specified Metadata entry.
     *
     * @param metaKey   a string to be search against the Metadata.metaKey
     * @return          the Metadata entry for specified Metadata.metaKey
     * @throws MetadataManagementException     If a data source related exception occurred
     */
    Metadata retrieveMetadata(String metaKey) throws MetadataManagementException;

    /**
     * Get all Metadata entries.
     *
     * @return  a list of Metadata entries
     * @throws MetadataManagementException If a data source related exception occurred
     */
    List<Metadata> retrieveAllMetadata() throws MetadataManagementException;

    /**
     * Get a paginated list of Metadata entries.
     *
     * @param request   {@link PaginationRequest} obtained from the user
     * @return          {@link PaginationResult} enriched with metadata entries
     * @throws MetadataManagementException If a data source related exception occurred
     */
    PaginationResult retrieveAllMetadata(PaginationRequest request) throws MetadataManagementException;

    /**
     * Update the provided Metadata entry.
     * a new entry will be created if the provided Metadata.metaKey is not exist
     *
     * @param metadata  the Metadata entry to be updated/created
     * @return          the updated/created Metadata entry
     * @throws MetadataManagementException If a data source related exception occurred
     */
    Metadata updateMetadata(Metadata metadata) throws MetadataManagementException;

    /**
     * Delete the specified Metadata entry.
     *
     * @param metaKey   the key value of the Metadata entry to be deleted
     * @return          true if the Metadata entry is deleted successfully,
     *                  false otherwise.
     * @throws MetadataManagementException     If a data source related exception occurred
     * @throws MetadataKeyNotFoundException    If the provided Metadata.metaKey not found
     */
    boolean deleteMetadata(String metaKey) throws MetadataManagementException, MetadataKeyNotFoundException;

}
