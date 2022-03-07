/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.IdentityServer;
import io.entgra.application.mgt.common.dto.IdentityServerDTO;
import io.entgra.application.mgt.common.SPApplication;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.RequestValidatingException;
import io.entgra.application.mgt.common.response.Application;
import java.util.List;

public interface SPApplicationManager {

    /**
     * This method adds existing consumer applications of service providers to the SPApplication bean
     *
     * @param identityServerId identity  server id of the service provider
     * @param applications Service providers list to which the existing applications should be added
     * @throws ApplicationManagementException if error occurred while adding existing applications
     */
    void addExistingApps(int identityServerId, List<SPApplication> applications) throws ApplicationManagementException;

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
    IdentityServer getIdentityServer(int identityServerId) throws ApplicationManagementException;

    /**
     *
     * @return Available identity servers
     * @throws ApplicationManagementException if error occurred while getting identity servers
     */
    List<IdentityServer> getIdentityServers() throws ApplicationManagementException;

    IdentityServer createIdentityServer(IdentityServerDTO identityServerDTO) throws ApplicationManagementException;

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
     * @return Application bean of the created application
     * @throws ApplicationManagementException if errors while creating and mapping the application
     * @throws RequestValidatingException if app contains any invalid payload
     */
    <T> Application createSPApplication(T app, int identityServerId, String spId) throws ApplicationManagementException, RequestValidatingException;

    /**
     * Validates application ids of the applications that should be attached
     *
     * @param appIds application ids to be validated
     * @throws ApplicationManagementException
     */
    void validateAttachAppsRequest(int identityServerId, List<Integer> appIds) throws ApplicationManagementException;

    /**
     * Validates application ids of the applications that should be detached
     *
     * @param identityServerId id of identity server
     * @param spId uid of service provider from which applications should be detached
     * @param appIds applications ids to be detached
     * @throws ApplicationManagementException
     */
    void validateDetachAppsRequest(int identityServerId, String spId, List<Integer> appIds) throws ApplicationManagementException;

}
