/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;

import java.util.List;

/**
 * This interface provides the list of operations that are performed in the database layer with respect to the
 * visibility.
 *
 */
public interface VisibilityDAO {

    /***
     * This method is used to add unrestricted roles for a particular application.
     *
     * @param unrestrictedRoles List of roles. User should have assigned at least one role from unrestricted role list
     *                         to view the application.
     * @param applicationId Id of the application.
     * @param tenantId Tenant Id.
     * @throws VisibilityManagementDAOException if an error occured while executing the query.
     */
    void addUnrestrictedRoles(List<String> unrestrictedRoles, int applicationId, int tenantId)
            throws VisibilityManagementDAOException;

    /***
     * This method is used to get unrestricted roles of an particular application.
     *
     * @param applicationId Id of the application.
     * @param tenantId Tenant Id.
     * @return List of unrestricted roles of the application.
     * @throws VisibilityManagementDAOException if an error occured while executing the query.
     */
    List<String> getUnrestrictedRoles(int applicationId, int tenantId) throws VisibilityManagementDAOException;

    /***
     * This method is used to delete unrestricted roles of an particular application.
     *
     * @param unrestrictedRoles List of unrestricted roles which are going to remove from the application.
     * @param applicationId Id of the application.
     * @param tenantId Tenant Id.
     * @throws VisibilityManagementDAOException if an error occured while executing the query.
     */
    void deleteUnrestrictedRoles(List<String> unrestrictedRoles, int applicationId, int tenantId)
            throws VisibilityManagementDAOException;

    /**
     * This method is responsible to delete all application unrestricted roles
     *
     * @param applicationId Application Id
     * @param tenantId Tenant Id
     * @throws VisibilityManagementDAOException if error occurred while deleting application unrestricted roles
     */
    void deleteAppUnrestrictedRoles(int applicationId, int tenantId) throws VisibilityManagementDAOException;

}
