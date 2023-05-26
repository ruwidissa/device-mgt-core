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

package io.entgra.device.mgt.core.operation.template.cache;

import com.google.common.cache.CacheLoader;
import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAO;
import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAOFactory;
import io.entgra.device.mgt.core.operation.template.dao.impl.util.OperationTemplateManagementUtil;
import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.core.operation.template.dto.OperationTemplateCacheKey;
import io.entgra.device.mgt.core.operation.template.exception.DBConnectionException;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateManagementDAOException;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateMgtPluginException;
import io.entgra.device.mgt.core.operation.template.util.ConnectionManagerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class for the Operation Template cache.
 */
public class OperationTemplateCacheLoader extends CacheLoader<String, OperationTemplate> {

    private static final Log log = LogFactory.getLog(OperationTemplateCacheLoader.class);

    private final OperationTemplateDAO operationTemplateDAO;

    public OperationTemplateCacheLoader() {
        this.operationTemplateDAO = OperationTemplateDAOFactory.getOperationTemplateDAO();
    }

    /**
     *
     * @param key the non-null key whose value should be loaded
     * @return
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public OperationTemplate load(String key) throws OperationTemplateMgtPluginException {
        OperationTemplateCacheKey operationTemplateCacheKey = OperationTemplateManagementUtil.getOperationTemplateCacheKey(
                key);
        String subTypeId = operationTemplateCacheKey.getSubTypeId();
        String operationCode = operationTemplateCacheKey.getOperationCode();
        String deviceType = operationTemplateCacheKey.getDeviceType();

        if (log.isTraceEnabled()) {
            log.trace(
                    "Loading operation template for subtype Id : " + subTypeId + " & deviceType : " + deviceType + " operation code : "
                            + operationCode);
        }
        try {
            ConnectionManagerUtils.openDBConnection();
            return operationTemplateDAO.getOperationTemplate(subTypeId, deviceType, operationCode);
        } catch (DBConnectionException e) {
            String msg =
                    "Error occurred while obtaining the database connection to retrieve operation template for "
                            +
                            "subtype Id : " + subTypeId + " & operation code : " + operationCode;
            log.error(msg);
            throw new OperationTemplateMgtPluginException(msg, e);
        } catch (InvalidCacheLoadException e) {
            String msg =
                    "CacheLoader returned null for operation template for subtype Id : " + subTypeId
                            + " & operation code : " + operationCode;
            log.error(msg, e);
            return null;
        } catch (OperationTemplateManagementDAOException e) {
            String msg =
                    "Error occurred in the database level while retrieving operation template for subtype Id : "
                            + subTypeId + " & operation code : " + operationCode;
            log.error(msg);
            throw new OperationTemplateMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();
        }
    }

}
