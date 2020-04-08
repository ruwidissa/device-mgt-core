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

package org.wso2.carbon.device.mgt.core.metadata.mgt.dao;

import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;

import java.util.List;

/**
 * This class defines the methods to be implemented by MetadataDAO layer.
 */
public interface MetadataDAO {

    /**
     * Insert metadata entry to datasource.
     *
     * @param tenantId  Tenant Id
     * @param metadata  Metadata object.
     * @return          a number, auto-generated assigned to Metadata.id
     * @throws MetadataManagementDAOException might occur while executing database queries
     */
    int addMetadata(int tenantId, Metadata metadata) throws MetadataManagementDAOException;

    /**
     * Select a Metadata entry by the provided metaKey.
     *
     * @param tenantId  Tenant Id
     * @param metaKey   a string to be search against the Metadata.metaKey
     * @return          the Metadata entry for specified Metadata.metaKey
     * @throws MetadataManagementDAOException might occur while executing database queries
     */
    Metadata getMetadata(int tenantId, String metaKey) throws MetadataManagementDAOException;

    /**
     * Check if the specified Metadata entry exists.
     *
     * @param tenantId  Tenant Id
     * @param metaKey   the key value of the Metadata entry to be checked
     * @return          true if the Metadata entry is exist,
     *                  false otherwise.
     * @throws MetadataManagementDAOException might occur while executing database queries
     */
    boolean isExist(int tenantId, String metaKey) throws MetadataManagementDAOException;

    /**
     * Update the specified Metadata entry.
     *
     * @param tenantId  Tenant Id
     * @param metadata  Metadata object.
     * @return          true if the Metadata entry is updated successfully,
     *                  false otherwise.
     * @throws MetadataManagementDAOException might occur while executing database queries
     */
    boolean updateMetadata(int tenantId, Metadata metadata) throws MetadataManagementDAOException;

    /**
     * Delete specified Metadata entry.
     *
     * @param tenantId  Tenant Id
     * @param metaKey   the key value of the Metadata entry to be deleted
     * @return          true if the Metadata entry is deleted successfully,
     *                  false otherwise.
     * @throws MetadataManagementDAOException might occur while executing database queries
     */
    boolean deleteMetadata(int tenantId, String metaKey) throws MetadataManagementDAOException;

    /**
     * Select Metadata entries based on PaginationRequest.
     *
     * @param request   {@link PaginationRequest}
     * @param tenantId  Tenant Id
     * @return          a list of Metadata entries
     * @throws MetadataManagementDAOException might occur while executing database queries
     */
    List<Metadata> getAllMetadata(PaginationRequest request, int tenantId) throws MetadataManagementDAOException;

    /**
     * Count number of Metadata entries.
     *
     * @param tenantId  Tenant Id
     * @return          Metadata entry count of given tenant
     * @throws MetadataManagementDAOException might occur while executing database queries
     */
    int getMetadataCount(int tenantId) throws MetadataManagementDAOException;

}
