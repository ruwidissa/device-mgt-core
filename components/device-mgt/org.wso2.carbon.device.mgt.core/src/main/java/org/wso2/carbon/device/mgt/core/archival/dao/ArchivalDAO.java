/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.archival.dao;

import java.sql.Timestamp;
import java.util.List;

/**
 * Operations to move data from DM database to archival database
 */
public interface ArchivalDAO {

    List<Integer> getNonRemovableOperationMappingIDs(Timestamp time) throws ArchivalDAOException;

    int getLargeOperationResponseCount(Timestamp time, List<Integer> nonRemovableMappings) throws ArchivalDAOException;

    int getOpMappingsCount(Timestamp time) throws ArchivalDAOException;

    int getOperationResponseCount(Timestamp time, List<Integer> nonRemovableMappings) throws ArchivalDAOException;

    void transferOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings) throws ArchivalDAOException;

    void transferLargeOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings) throws ArchivalDAOException;

    void removeLargeOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings) throws ArchivalDAOException;

    void removeOperationResponses(int batchSize, Timestamp time, List<Integer> nonRemovableMappings) throws ArchivalDAOException;

    void moveNotifications(Timestamp time) throws ArchivalDAOException;

    void transferEnrollmentOpMappings(int batchSize, Timestamp time) throws ArchivalDAOException;

    void removeEnrollmentOPMappings(int batchSize, Timestamp time) throws ArchivalDAOException;

    void transferOperations() throws ArchivalDAOException;

    void removeOperations() throws ArchivalDAOException;

}
