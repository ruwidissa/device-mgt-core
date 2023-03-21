/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
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

    OperationTemplate getOperationTemplate(int subTypeId, String deviceType, String operationCode) throws OperationTemplateManagementDAOException;

    void deleteOperationTemplate(int subTypeId, String deviceCode, String operationCode) throws OperationTemplateManagementDAOException;

    boolean isExistsOperationTemplateBySubtypeIdAndOperationCode(int subTypeId, String deviceType, String operationCode) throws OperationTemplateManagementDAOException;
}
