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
package io.entgra.application.mgt.core.dao;

import io.entgra.application.mgt.common.LifecycleState;
import io.entgra.application.mgt.core.exception.LifeCycleManagementDAOException;

import java.util.List;

/**
 * This is responsible for all the DAO operations related to Lifecycle state.
 */
public interface LifecycleStateDAO {

    /**
     * To get the latest lifecycle state for the given application id and the application release UUID.
     * @param uuid UUID of the application release
     *
     * @return Latest Lifecycle State for the given application release
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    LifecycleState getLatestLifecycleState(String uuid) throws LifeCycleManagementDAOException;

    /**
     * To get all changed lifecycle states for the given application release id.
     * @param appReleaseId id of the application release.
     * @param tenantId Tenant Id.
     *
     * @return Lifecycle States for the given application release
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    List<LifecycleState> getLifecycleStates(int appReleaseId, int tenantId) throws LifeCycleManagementDAOException;

    /**
     * To add new lifecycle states for the given application release.
     * @param state LifecycleState.
     * @param tenantId Tenant id
     *
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    void addLifecycleState(LifecycleState state, int appReleaseId, int tenantId) throws LifeCycleManagementDAOException;

    /**
     * To delete lifecycle state data of specific application release.
     * @param releaseId Id of the LifecycleState.
     *
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    void deleteLifecycleStateByReleaseId(int releaseId) throws LifeCycleManagementDAOException;

    void deleteLifecycleStates(List<Integer> appReleaseIds) throws LifeCycleManagementDAOException;


    /***
     *
     * @param appId ID of the application
     * @param uuid UUID of the application release
     * @return Username of the application release creator
     * @throws LifeCycleManagementDAOException {@link LifeCycleManagementDAOException}
     */
    String getAppReleaseCreatedUsername(int appId, String uuid, int tenantId) throws LifeCycleManagementDAOException;

    }
