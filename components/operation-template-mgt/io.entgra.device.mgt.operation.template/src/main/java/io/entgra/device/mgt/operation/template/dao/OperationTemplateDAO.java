/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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

package io.entgra.device.mgt.operation.template.dao;

import io.entgra.device.mgt.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.operation.template.exception.OperationTemplateManagementDAOException;

/**
 * This class represents the key operations associated with persisting mobile-device related
 * information.
 */
public interface OperationTemplateDAO {
    void addOperationTemplate(OperationTemplate operationTemplate) throws OperationTemplateManagementDAOException;

    OperationTemplate updateOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateManagementDAOException;

    OperationTemplate getOperationTemplate(String subTypeId, String deviceType, String operationCode) throws OperationTemplateManagementDAOException;

    void deleteOperationTemplate(String subTypeId, String deviceCode, String operationCode) throws OperationTemplateManagementDAOException;

}
