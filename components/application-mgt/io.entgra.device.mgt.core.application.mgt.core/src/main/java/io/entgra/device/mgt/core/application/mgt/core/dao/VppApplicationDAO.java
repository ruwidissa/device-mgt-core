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

package io.entgra.device.mgt.core.application.mgt.core.dao;

import io.entgra.device.mgt.core.application.mgt.common.dto.VppAssetDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppAssociationDTO;
import io.entgra.device.mgt.core.application.mgt.common.dto.VppUserDTO;
import io.entgra.device.mgt.core.application.mgt.core.exception.ApplicationManagementDAOException;


public interface VppApplicationDAO {

    int addVppUser(VppUserDTO userDTO, int tenantId) throws ApplicationManagementDAOException;

    VppUserDTO updateVppUser(VppUserDTO userDTO, int tenantId) throws ApplicationManagementDAOException;

    VppUserDTO getUserByDMUsername(String emmUsername, int tenantId) throws ApplicationManagementDAOException;

    VppAssetDTO getAssetByAppId(int appId, int tenantId) throws ApplicationManagementDAOException;

    int addAsset(VppAssetDTO vppAssetDTO, int tenantId) throws ApplicationManagementDAOException;

    VppAssetDTO updateAsset(VppAssetDTO vppAssetDTO, int tenantId) throws ApplicationManagementDAOException;

    VppAssociationDTO getAssociation(int assetId, int userId, int tenantId) throws ApplicationManagementDAOException;

    int addAssociation(VppAssociationDTO vppAssociationDTO, int tenantId) throws ApplicationManagementDAOException;

    VppAssociationDTO updateAssociation(VppAssociationDTO vppAssociationDTO, int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete associations of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteAssociationByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete Vpp users of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteVppUserByTenant(int tenantId) throws ApplicationManagementDAOException;

    /**
     * Delete assets of tenant
     *
     * @param tenantId Tenant ID
     * @throws ApplicationManagementDAOException thrown if an error occurs while deleting data
     */
    void deleteAssetsByTenant(int tenantId) throws ApplicationManagementDAOException;
}
