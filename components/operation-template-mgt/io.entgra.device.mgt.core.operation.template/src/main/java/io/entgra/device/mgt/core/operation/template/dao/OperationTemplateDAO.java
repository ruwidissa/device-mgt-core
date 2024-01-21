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

package io.entgra.device.mgt.core.operation.template.dao;

import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateManagementDAOException;

import java.util.List;
import java.util.Set;

/**
 * This class represents the key operations associated with persisting mobile-device related
 * information.
 */
public interface OperationTemplateDAO {
    void addOperationTemplate(OperationTemplate operationTemplate) throws OperationTemplateManagementDAOException;

    OperationTemplate updateOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateManagementDAOException;

    OperationTemplate getOperationTemplateByDeviceTypeAndSubTypeIdAndOperationCode(String deviceType, String subTypeId, String operationCode) throws OperationTemplateManagementDAOException;

    List<OperationTemplate> getAllOperationTemplatesByDeviceType(String deviceType) throws OperationTemplateManagementDAOException;

    int deleteOperationTemplateByDeviceTypeAndSubTypeIdAndOperationCode(String deviceType, String subTypeId, String operationCode) throws OperationTemplateManagementDAOException;

     Set<String> getOperationTemplateCodesByDeviceTypeAndSubTypeId(String deviceType, String subTypeId) throws OperationTemplateManagementDAOException;
}
