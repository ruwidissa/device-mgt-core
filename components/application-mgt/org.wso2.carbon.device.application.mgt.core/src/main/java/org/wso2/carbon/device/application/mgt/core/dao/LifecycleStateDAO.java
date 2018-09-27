/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;

import java.util.List;

/**
 * This is responsible for all the DAO operations related to Lifecycle state.
 */
public interface LifecycleStateDAO {

    /**
     * To get the latest lifecycle state for the given application release id.
     * @param applicationReleaseId id of the application release.
     *
     * @return Latest Lifecycle State for the given application release
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    LifecycleState getLatestLifeCycleStateByReleaseID(int applicationReleaseId) throws LifeCycleManagementDAOException;

    /**
     * To get the latest lifecycle state for the given application id and the application release UUID.
     * @param appId id of the application.
     * @param uuid UUID of the application release
     *
     * @return Latest Lifecycle State for the given application release
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    LifecycleState getLatestLifeCycleState(int appId, String uuid) throws LifeCycleManagementDAOException;

    /**
     * To get all changed lifecycle states for the given application release id.
     * @param appReleaseId id of the application release.
     *
     * @return Lifecycle States for the given application release
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    List<LifecycleState> getLifecycleStates(int appReleaseId) throws LifeCycleManagementDAOException;

    /**
     * To add new lifecycle states for the given application release.
     * @param releaseId Id of the application release.
     * @param appId Id of the application.
     * @param state LifecycleState.
     * @param tenantId Tenant id
     *
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    void addLifecycleState(LifecycleState state, int appId, int releaseId, int tenantId)
            throws LifeCycleManagementDAOException;

    /**
     * To delete a specific lifecycle state for application release.
     * @param identifier Id of the LifecycleState.
     *
     * @throws LifeCycleManagementDAOException Lifecycle Management DAO Exception.
     */
    void deleteLifecycleState(int identifier) throws LifeCycleManagementDAOException;
}
