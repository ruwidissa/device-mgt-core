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

package io.entgra.device.mgt.core.operation.template.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.entgra.device.mgt.core.operation.template.cache.OperationTemplateCacheLoader;
import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAO;
import io.entgra.device.mgt.core.operation.template.dao.OperationTemplateDAOFactory;
import io.entgra.device.mgt.core.operation.template.dao.impl.util.OperationTemplateManagementUtil;
import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.core.operation.template.exception.DBConnectionException;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateManagementDAOException;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateMgtPluginException;
import io.entgra.device.mgt.core.operation.template.spi.OperationTemplateService;
import io.entgra.device.mgt.core.operation.template.util.ConnectionManagerUtils;
import io.entgra.device.mgt.core.operation.template.util.AssertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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

        validateAddOperationTemplate(operationTemplate);

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

        validateUpdateOperationTemplate(operationTemplate);
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
    public OperationTemplate getOperationTemplate(String subTypeId, String deviceType, String operationCode)
            throws OperationTemplateMgtPluginException {
        try {

            validateGetOperationTemplate(subTypeId, deviceType, operationCode);
            String key = OperationTemplateManagementUtil.setOperationTemplateCacheKey(subTypeId, deviceType,
                    operationCode);
            return operationTemplateCache.get(key);
        } catch (ExecutionException e) {
            log.error(e.getMessage());
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            String msg = "Operation Template doesn't exist for subtype id : " + subTypeId + " and device type : "
                    + deviceType + " and operation code : " + operationCode;
            log.error(msg, e);
            return null;
        }
    }

    /**
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public void deleteOperationTemplate(String subTypeId, String deviceType, String operationCode)
            throws OperationTemplateMgtPluginException {

        String msg = "Operation Template does not exist for subtype id : " + subTypeId
                + " and device type : " + deviceType + " and operation code : "
                + operationCode;
        AssertUtils.isNull(getOperationTemplate(subTypeId, deviceType, operationCode), msg);

        try {
            ConnectionManagerUtils.beginDBTransaction();
            operationTemplateDAO.deleteOperationTemplate(subTypeId, deviceType, operationCode);
            ConnectionManagerUtils.commitDBTransaction();
            if (log.isDebugEnabled()) {
                String debugMsg = "Operation Template deleted successfully,for subtype id "
                        + subTypeId + " and operation code "
                        + operationCode + "";
                log.debug(debugMsg);
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
     *
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @throws OperationTemplateMgtPluginException
     */
    private void validateGetOperationTemplate(String subTypeId, String deviceType, String operationCode)
            throws OperationTemplateMgtPluginException {

        AssertUtils.hasText(subTypeId, "Invalid meter device subtype id: " + subTypeId);
        AssertUtils.isTrue(Integer.valueOf(subTypeId)>0, "Invalid meter device subtype id: " + subTypeId);
        AssertUtils.hasText(operationCode, "Validation failed due to invalid operation code: " + operationCode);
        AssertUtils.hasText(deviceType, "Invalid device type.");
        AssertUtils.isTrue(deviceType.equals("METER"), "Invalid device type. ");
    }

    /**
     * @param operationTemplate
     * @throws OperationTemplateMgtPluginException
     */
    private void validateAddOperationTemplate(OperationTemplate operationTemplate) throws OperationTemplateMgtPluginException {

        AssertUtils.isNull(operationTemplate, "Operation Template can not be null");
        AssertUtils.hasText(operationTemplate.getOperationDefinition(), "Operation definition can not be null");

        String msg = "Operation Template already exist for subtype id : " + operationTemplate.getSubTypeId()
                + " and device type : " + operationTemplate.getDeviceType() + " and operation code : "
                + operationTemplate.getCode();
        AssertUtils.notNull(getOperationTemplate(operationTemplate.getSubTypeId(), operationTemplate.getDeviceType(),
                operationTemplate.getCode()), msg);
    }

    /**
     * @param operationTemplate
     * @throws OperationTemplateMgtPluginException
     */
    private void validateUpdateOperationTemplate(OperationTemplate operationTemplate) throws OperationTemplateMgtPluginException {

        AssertUtils.isNull(operationTemplate, "Operation Template can not be null");
        AssertUtils.hasText(operationTemplate.getOperationDefinition(), "Operation definition can not be null");

        String msg = "Operation Template does not exist for subtype id : " + operationTemplate.getSubTypeId()
                + " and device type : " + operationTemplate.getDeviceType() + " and operation code : "
                + operationTemplate.getCode();
        AssertUtils.isNull(getOperationTemplate(operationTemplate.getSubTypeId(), operationTemplate.getDeviceType(),
                operationTemplate.getCode()), msg);
    }

}