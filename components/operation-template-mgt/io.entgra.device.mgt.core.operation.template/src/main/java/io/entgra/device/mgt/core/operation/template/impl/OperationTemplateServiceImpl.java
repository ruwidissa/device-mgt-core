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
import io.entgra.device.mgt.core.operation.template.cache.OperationTemplateCodesCacheLoader;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Operation Template service impl class.
 */
public class OperationTemplateServiceImpl implements OperationTemplateService {

    private static final Log log = LogFactory.getLog(OperationTemplateServiceImpl.class);
    private static final LoadingCache<String, OperationTemplate> operationTemplateCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES).build(new OperationTemplateCacheLoader());

    private static final LoadingCache<String, Set<String>> operationTemplateCodeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES).build(new OperationTemplateCodesCacheLoader());
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
            addOperationTemplateDetailsForCacheLoader(operationTemplate);
        }
    }

    public void addOperationTemplateDetailsForCacheLoader(OperationTemplate operationTemplate) {
        try {
            String operationTemplateKey = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                    operationTemplate.getDeviceType(), operationTemplate.getSubTypeId(), operationTemplate.getCode());
            String operationTemplateCodeKey = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                    operationTemplate.getDeviceType(), operationTemplate.getSubTypeId());

            operationTemplateCache.put(operationTemplateKey, operationTemplate);

            Set<String> operationCodeList = operationTemplateCodeCache.get(operationTemplateCodeKey);
            if (operationCodeList == null) {
                operationCodeList = new HashSet<>();
            }
            operationCodeList.add(operationTemplate.getCode());
            operationTemplateCodeCache.put(operationTemplateCodeKey, operationCodeList);
        } catch (Exception e) {
            log.error("Error occurred while adding operation template details for the cache loader", e);
        }
    }

    public void deleteOperationTemplateDetailsFromCacheLoader(String deviceType, String subTypeId, String code) {
        try {
            String operationTemplateKey = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                    deviceType, subTypeId, code);
            String operationTemplateCodeKey = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                    deviceType, subTypeId);

            operationTemplateCache.invalidate(operationTemplateKey);
            operationTemplateCodeCache.invalidate(operationTemplateCodeKey);
        } catch (Exception e) {
            log.error("Error occurred removing operation template details for the cache loader", e);
        }
    }

    public void refreshOperationTemplateDetailsFromCacheLoader(OperationTemplate operationTemplate) {
        try {
            if (operationTemplate != null) {
                String operationTemplateKey = OperationTemplateManagementUtil.setOperationTemplateCacheKey(operationTemplate.getDeviceType(),
                        operationTemplate.getSubTypeId(), operationTemplate.getCode());
                String operationTemplateCodeKey = OperationTemplateManagementUtil.setOperationTemplateCacheKey(
                        operationTemplate.getDeviceType(), operationTemplate.getSubTypeId());

                operationTemplateCache.put(operationTemplateKey, operationTemplate);
                Set<String> codeList = operationTemplateCodeCache.get(operationTemplateCodeKey);
                codeList.remove(operationTemplate.getCode());
                operationTemplateCodeCache.put(operationTemplateCodeKey, codeList);


            }
        } catch (ExecutionException e) {
            log.error("Error occurred while updating operation template cache loader");
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
                refreshOperationTemplateDetailsFromCacheLoader(updatedOperationTemplate);
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
    public OperationTemplate getOperationTemplate(String deviceType, String subTypeId, String operationCode)
            throws OperationTemplateMgtPluginException {
        try {

            validateGetOperationTemplate(subTypeId, deviceType, operationCode);
            String key = OperationTemplateManagementUtil.setOperationTemplateCacheKey(deviceType, subTypeId,
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
     * @param deviceType
     * @return
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public List<OperationTemplate> getAllOperationTemplates(String deviceType)
            throws OperationTemplateMgtPluginException {
        AssertUtils.hasText(deviceType, "Invalid device type.");
        try {
            ConnectionManagerUtils.openDBConnection();
            return operationTemplateDAO.getAllOperationTemplates(deviceType);
        } catch (DBConnectionException | OperationTemplateManagementDAOException e) {
            log.error(e.getMessage());
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();

        }
    }

    /**
     * @param subTypeId
     * @param deviceType
     * @param operationCode
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public void deleteOperationTemplate(String deviceType, String subTypeId, String operationCode)
            throws OperationTemplateMgtPluginException {

        String msg = "Operation Template does not exist for subtype id : " + subTypeId
                + " and device type : " + deviceType + " and operation code : "
                + operationCode;
        AssertUtils.isNull(getOperationTemplate(deviceType, subTypeId, operationCode), msg);

        boolean isDelete = false;
        try {
            ConnectionManagerUtils.beginDBTransaction();
            isDelete = operationTemplateDAO.deleteOperationTemplate(deviceType, subTypeId, operationCode);
            ConnectionManagerUtils.commitDBTransaction();
            if (log.isDebugEnabled()) {
                String debugMsg = "Operation Template deleted successfully,for subtype id "
                        + subTypeId + " and operation code "
                        + operationCode + "";
                log.debug(debugMsg);
            }
        } catch (DBConnectionException | OperationTemplateManagementDAOException e) {
            log.error(e.getMessage());
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } finally {
            ConnectionManagerUtils.closeDBConnection();
            if (isDelete) {
                deleteOperationTemplateDetailsFromCacheLoader(deviceType, subTypeId, operationCode);
            }

        }
    }

    /**
     * @param deviceType
     * @param subTypeId
     * @return
     * @throws OperationTemplateMgtPluginException
     */
    @Override
    public Set<String> getOperationTemplateCodes(String deviceType, String subTypeId)
            throws OperationTemplateMgtPluginException {

        try {
            AssertUtils.hasText(subTypeId, "Invalid device subtype id: " + subTypeId);
            AssertUtils.isTrue(Integer.parseInt(subTypeId) > 0,
                    "Invalid device subtype id: " + subTypeId);
            AssertUtils.hasText(deviceType, "Invalid device type.");

            String key = OperationTemplateManagementUtil.setOperationTemplateCacheKey(deviceType, subTypeId);
            return operationTemplateCodeCache.get(key);
        } catch (ExecutionException e) {
            log.error(e.getMessage());
            throw new OperationTemplateMgtPluginException(e.getMessage(), e);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            String msg = "Operation Template codes doesn't exist for subtype id : " + subTypeId + " and device type : "
                    + deviceType;
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
    private void validateGetOperationTemplate(String subTypeId, String deviceType, String operationCode)
            throws OperationTemplateMgtPluginException {

        AssertUtils.hasText(subTypeId, "Invalid device subtype id: " + subTypeId);
        AssertUtils.isTrue(Integer.parseInt(subTypeId) > 0, "Invalid device subtype id: " + subTypeId);
        AssertUtils.hasText(operationCode, "Validation failed due to invalid operation code: " + operationCode);
        AssertUtils.hasText(deviceType, "Invalid device type.");
    }

    /**
     * @param operationTemplate
     * @throws OperationTemplateMgtPluginException
     */
    private void validateAddOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateMgtPluginException {

        AssertUtils.isNull(operationTemplate, "Operation Template can not be null");
        AssertUtils.hasText(operationTemplate.getOperationDefinition(), "Operation definition can not be null");

        String msg = "Operation Template already exist for subtype id : " + operationTemplate.getSubTypeId()
                + " and device type : " + operationTemplate.getDeviceType() + " and operation code : "
                + operationTemplate.getCode();
        AssertUtils.notNull(getOperationTemplate(operationTemplate.getDeviceType(), operationTemplate.getSubTypeId(),
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
        AssertUtils.isNull(getOperationTemplate(operationTemplate.getDeviceType(), operationTemplate.getSubTypeId(),
                operationTemplate.getCode()), msg);
    }

}