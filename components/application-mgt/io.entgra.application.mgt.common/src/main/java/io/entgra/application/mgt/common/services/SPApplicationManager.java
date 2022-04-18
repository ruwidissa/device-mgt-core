/* Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.IdentityServerResponse;
import io.entgra.application.mgt.common.SPApplicationListResponse;
import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import io.entgra.application.mgt.common.dto.IdentityServiceProviderDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.response.Application;
import java.util.List;

public interface SPApplicationManager {

    /**
     * Removes consumer application from service provider
     *
     * @param identityServerId of the service provider
     * @param spUID uid of the service provider
     * @param appIds List of application ids to be removed
     * @throws ApplicationManagementException if errors while removing appIds from service provider
     */
    void detachSPApplications(int identityServerId, String spUID, List<Integer> appIds) throws ApplicationManagementException;

    /**
     * Maps consumer applications to service provider
     *
     * @param identityServerId of the service provider
     * @param spUID uid of the service provider
     * @param appIds List of application ids to be mapped
     * @throws ApplicationManagementException if errors while mapping appIds to service provider
     */
    void attachSPApplications(int identityServerId, String spUID, List<Integer> appIds)  throws ApplicationManagementException;

    /**
     *
     * @param identityServerId of the identity server that is to be retrieved
     * @return Identity server for the given ID
     * @throws ApplicationManagementException if error occurred while getting identity server
     */
    IdentityServerResponse getIdentityServerResponse(int identityServerId) throws ApplicationManagementException;

    /**
     *
     * @return Available identity servers
     * @throws ApplicationManagementException if error occurred while getting identity servers
     */
    List<IdentityServerResponse> getIdentityServers() throws ApplicationManagementException;

    /**
     * Create a new Identity Server
     *
     * @return {@link IdentityServerResponse}
     * @throws ApplicationManagementException if error occurred while getting identity servers
     */
    IdentityServerResponse createIdentityServer(IdentityServerDTO identityServerDTO) throws ApplicationManagementException;

    /**
     * Update existing Identity Server
     *
     * @param id of the identity server to be updated
     * @param updateIdentityServerDTO identity server dto bean with updated fields
     * @throws ApplicationManagementException if error occurred while getting identity servers
     */
    IdentityServerResponse updateIdentityServer(IdentityServerDTO updateIdentityServerDTO, int id) throws ApplicationManagementException;

    /**
     * Delete Identity Server
     *
     * @param id of the identity server to be deleted
     * @throws ApplicationManagementException if error occurred while getting identity servers
     */
    void deleteIdentityServer(int id) throws ApplicationManagementException;

    /**
     * Check if Identity Server exists with the same name
     *
     * @param name of the identity server
     * @return if name already exists for identity server
     */
    boolean isIdentityServerNameExist(String name) throws ApplicationManagementException;

    /**
     * Check if Identity Server exists with the same url
     *
     * @param url of the identity server
     * @return if url already exists for identity server
     */
    boolean isIdentityServerUrlExist(String url) throws ApplicationManagementException;

    /**
     * Retrieve service provider apps from identity server
     *
     * @param identityServerId Id of the identity server
     * @return {@link SPApplicationListResponse}
     * @throws ApplicationManagementException if error while retrieving sp applications
     */
    SPApplicationListResponse retrieveSPApplicationFromIdentityServer(int identityServerId, Integer offset, Integer limit)
            throws ApplicationManagementException;

    /**
     *
     * @param identityServerId of the service provider
     * @param spUID uid of the service provider
     * @return  Applications that are mapped to given service provider uid and identity server id
     * @throws ApplicationManagementException
     */
    List<Application> getSPApplications(int identityServerId, String spUID) throws ApplicationManagementException;

    /**
     * This method is responsible for creating a new application and mapping it the given service provider uid
     * and identity server id
     *
     * @param app Application wrapper of the application that should be created
     * @param identityServerId id of the identity server to which the created application should be mapped
     * @param spId uid of the service provder to which the created application should be mapped
     * @param <T> Application wrapper class which depends on application type (PUBLIC, ENTERPRISE & etc)
     * @param isPublished If the app should be added in PUBLISHED state instead of initial state
     * @return Application bean of the created application
     * @throws ApplicationManagementException if errors while creating and mapping the application
     * @throws RequestValidatingException if app contains any invalid payload
     */
    <T> Application createSPApplication(T app, int identityServerId, String spId, boolean isPublished) throws ApplicationManagementException, RequestValidatingException;

    /**
     * Validates application ids of the applications that should be attached
     *
     * @param appIds application ids to be validated
     * @throws ApplicationManagementException if invalid service provider, identity server Id or app Ids provided
     */
    void validateAttachAppsRequest(int identityServerId, String serviceProviderId, List<Integer> appIds) throws ApplicationManagementException;

    /**
     * Validates application ids of the applications that should be detached
     *
     * @param identityServerId id of identity server
     * @param spId uid of service provider from which applications should be detached
     * @param appIds applications ids to be detached
     * @throws ApplicationManagementException
     */
    void validateDetachAppsRequest(int identityServerId, String spId, List<Integer> appIds) throws ApplicationManagementException;

    /**
     * Get available identity service providers
     *
     * @return list of available service providers' names
     */
    List<IdentityServiceProviderDTO> getIdentityServiceProviders() throws ApplicationManagementException;
}
