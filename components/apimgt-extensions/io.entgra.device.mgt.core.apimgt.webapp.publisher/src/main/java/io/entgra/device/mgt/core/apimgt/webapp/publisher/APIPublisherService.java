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
package io.entgra.device.mgt.core.apimgt.webapp.publisher;


import io.entgra.device.mgt.core.apimgt.webapp.publisher.exception.APIManagerPublisherException;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.DefaultPermission;

import java.util.List;

/**
 * This interface represents all methods related to API manipulation that's done as part of API-Management tasks.
 *
 */
public interface APIPublisherService {

    /**
     * This method registers an API within the underlying API-Management infrastructure.
     *
     * @param api An instance of the bean that passes metadata related to the API being published
     * @throws APIManagerPublisherException Is thrown if some unexpected event occurs while publishing the API
     */
    void publishAPI(APIConfig api) throws APIManagerPublisherException;

    void updateScopeRoleMapping() throws APIManagerPublisherException;

    /**
     * Add default scopes defined in the cdm-config.xml
     */
    void addDefaultScopesIfNotExist(List<DefaultPermission> defaultPermissions) throws APIManagerPublisherException;

    /**
     * If the permissions are in the permission list, identify the relevant scopes of the supplied permission list
     * and put the role there; if the permissions are in the removedPermission list, update the relevant scopes by
     * deleting the role from those scopes.
     *
     * @param roleName Role Name
     * @param permissions List of adding permissions
     * @param removedPermissions List of removing permissions
     * @throws APIManagerPublisherException If error occurred while updating the scope role mapping
     */
    void updateScopeRoleMapping(String roleName, String[] permissions, String[] removedPermissions) throws APIManagerPublisherException;

}
