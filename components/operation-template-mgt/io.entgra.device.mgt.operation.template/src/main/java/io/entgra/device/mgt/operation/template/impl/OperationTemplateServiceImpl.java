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

package io.entgra.device.mgt.operation.template.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import io.entgra.device.mgt.operation.template.cache.OperationTemplateCacheLoader;
import io.entgra.device.mgt.operation.template.dao.OperationTemplateDAO;
import io.entgra.device.mgt.operation.template.dao.OperationTemplateDAOFactory;
import io.entgra.device.mgt.operation.template.dao.impl.util.OperationTemplateManagementUtil;
import io.entgra.device.mgt.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.operation.template.exception.DBConnectionException;
import io.entgra.device.mgt.operation.template.exception.OperationTemplateManagementDAOException;
import io.entgra.device.mgt.operation.template.exception.OperationTemplateMgtPluginException;
import io.entgra.device.mgt.operation.template.spi.OperationTemplateService;
import io.entgra.device.mgt.operation.template.util.ConnectionManagerUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Operation Template service impl class.
 */
public class OperationTemplateServiceImpl implements OperationTemplateService {

    private static final Log log = LogFactory.getLog(OperationTemplateServiceImpl.class);
    private static final LoadingCache<String, OperationTemplate> operationTemplateCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES).build(new OperationTemplateCacheLoader());
    private final OperationTemplateDAO operationTemplateDAO;

    public OperationTemplateServiceImpl() {
        this.operationTemplateDAO = OperationTemplateDAOFactory.getOperationTemplateDAO();
    }

    /**
     * @param operationTemplate
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public void addOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateMgtPluginException {

        try {
            ConnectionManagerUtils.beginDBTransaction();
            operationTemplateDAO.addOperationTemplate(operationTemplate);
            ConnectionManagerUtils.commitDBTransaction();

            String key = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                    operationTemplate.getSubTypeId(), operationTemplate.getDeviceType(), operationTemplate.getCode());
            operationTemplateCache.put(key, operationTemplate);

            if (log.isDebugEnabled()) {
                String msg = "Operation Template added successfully,for subtype id "
                        + operationTemplate.getSubTypeId() + " and operation code "
                        + operationTemplate.getCode() + "";
                log.debug(msg);
            }
        } catch (DBConnectionException | OperationTemplateManagementDAOException e) {
            log.error(e.getMessage(), e);
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();
        }
    }

    /**
     * @param operationTemplate
     * @return
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public OperationTemplate updateOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateMgtPluginException {

        OperationTemplate updatedOperationTemplate = null;
        try {
            ConnectionManagerUtils.beginDBTransaction();
            updatedOperationTemplate = operationTemplateDAO.updateOperationTemplate(
                    operationTemplate);
            ConnectionManagerUtils.commitDBTransaction();
            if (log.isDebugEnabled()) {
                String msg = "Operation Template updated successfully,for subtype id "
                        + operationTemplate.getSubTypeId() + " and operation code " + operationTemplate.getCode()
                        + "";
                log.debug(msg);
            }
            return updatedOperationTemplate;
        } catch (DBConnectionException | OperationTemplateManagementDAOException e) {
            log.error(e.getMessage(), e);
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();

            if (updatedOperationTemplate != null) {
                String key = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                        operationTemplate.getSubTypeId(), operationTemplate.getDeviceType(), operationTemplate.getCode());
                operationTemplateCache.refresh(key);
            }
        }
    }

    /**
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @return
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public OperationTemplate getOperationTemplate(int subTypeId, String deviceType, String operationCode)
            throws OperationTemplateMgtPluginException {
        try {
            String key = OperationTemplateManagementUtil.setOperationTemplateCacheKey(subTypeId, deviceType,
                    operationCode);
            return operationTemplateCache.get(key);
        } catch (ExecutionException e) {
            log.error(e.getMessage());
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        }
    }

    /**
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public void deleteOperationTemplate(int subTypeId, String deviceType, String operationCode)
            throws OperationTemplateMgtPluginException {

        try {
            ConnectionManagerUtils.beginDBTransaction();
            operationTemplateDAO.deleteOperationTemplate(subTypeId, deviceType, operationCode);
            ConnectionManagerUtils.commitDBTransaction();
            if (log.isDebugEnabled()) {
                String msg = "Operation Template deleted successfully,for subtype id "
                        + subTypeId + " and operation code "
                        + operationCode + "";
                log.debug(msg);
            }
            String key = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                    subTypeId, deviceType, operationCode);
            operationTemplateCache.invalidate(key);
        } catch (DBConnectionException | OperationTemplateManagementDAOException e) {
            log.error(e.getMessage());
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();

        }
    }

    /**
     * @param subTypeId
     * @param operationCode
     * @param deviceType
     * @return
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public boolean isExistsOperationTemplateBySubtypeIdAndOperationCode(int subTypeId,
                                                                        String operationCode, String deviceType) throws OperationTemplateMgtPluginException {
        try {
            ConnectionManagerUtils.openDBConnection();
            return operationTemplateDAO.isExistsOperationTemplateBySubtypeIdAndOperationCode(subTypeId, deviceType,
                    operationCode);

        } catch (DBConnectionException | OperationTemplateManagementDAOException e) {
            log.error(e.getMessage());
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();
        }
    }

}