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

package io.entgra.device.mgt.core.subtype.mgt.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.entgra.device.mgt.core.subtype.mgt.cache.DeviceSubTypeCacheLoader;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubTypeCacheKey;
import io.entgra.device.mgt.core.subtype.mgt.exception.BadRequestException;
import io.entgra.device.mgt.core.subtype.mgt.exception.DBConnectionException;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtDAOException;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtPluginException;
import io.entgra.device.mgt.core.subtype.mgt.util.DeviceSubTypeMgtUtil;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAO;
import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAOFactory;
import io.entgra.device.mgt.core.subtype.mgt.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.subtype.mgt.spi.DeviceSubTypeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DeviceSubTypeServiceImpl implements DeviceSubTypeService {

    private static final Log log = LogFactory.getLog(DeviceSubTypeServiceImpl.class);

    private static final LoadingCache<DeviceSubTypeCacheKey, DeviceSubType> deviceSubTypeCache
            = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(new DeviceSubTypeCacheLoader());
    private final DeviceSubTypeDAO deviceSubTypeDAO;

    public DeviceSubTypeServiceImpl() {
        this.deviceSubTypeDAO = DeviceSubTypeDAOFactory.getDeviceSubTypeDAO();
    }

    @Override
    public boolean addDeviceSubType(DeviceSubType deviceSubType) throws SubTypeMgtPluginException {
        String msg;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        deviceSubType.setTenantId(tenantId);

        try {
            ConnectionManagerUtil.beginDBTransaction();
            boolean result = deviceSubTypeDAO.addDeviceSubType(deviceSubType);
            if (result) {
                msg = "Device subtype added successfully,for " + deviceSubType.getDeviceType() + " subtype & subtype " +
                        "Id: " + deviceSubType.getSubTypeId();
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
                msg = "Device subtype failed to add,for " + deviceSubType.getDeviceType() + " subtype & subtype Id: " +
                        deviceSubType.getSubTypeId();
                throw new SubTypeMgtPluginException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
            return true;
        } catch (DBConnectionException e) {
            msg = "Error occurred while obtaining the database connection to add device subtype for " +
                    deviceSubType.getDeviceType() + " subtype & subtype Id: " + deviceSubType.getSubTypeId();
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } catch (SubTypeMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            msg = "Error occurred in the database level while adding device subtype for " +
                    deviceSubType.getDeviceType() + " subtype & subtype Id: " + deviceSubType.getSubTypeId();
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
            DeviceSubTypeCacheKey key = DeviceSubTypeMgtUtil.getDeviceSubTypeCacheKey(tenantId,
                    deviceSubType.getSubTypeId(),
                    deviceSubType.getDeviceType());
            deviceSubTypeCache.refresh(key);
        }
    }

    @Override
    public boolean updateDeviceSubType(String subTypeId, int tenantId, String deviceType,
                                       String subTypeName, String typeDefinition)
            throws SubTypeMgtPluginException {
        String msg;
        DeviceSubType deviceSubTypeOld = getDeviceSubType(subTypeId, tenantId, deviceType);

        if (deviceSubTypeOld == null) {
            String errorMsg = "Cannot find device subtype for " + deviceType + " subtype & subtype Id: " + subTypeId;
            log.error(errorMsg);
            throw new BadRequestException(errorMsg);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            boolean result = deviceSubTypeDAO.updateDeviceSubType(subTypeId, tenantId, deviceType, subTypeName,
                    typeDefinition);
            if (result) {
                msg = "Device subtype updated successfully,for " + deviceType + " subtype & subtype Id: " + subTypeId;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
            } else {
                ConnectionManagerUtil.rollbackDBTransaction();
                msg = "Device subtype failed to update,for " + deviceType + " subtype & subtype Id: " + subTypeId;
                throw new SubTypeMgtPluginException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
            return true;
        } catch (DBConnectionException e) {
            msg = "Error occurred while obtaining the database connection to update device subtype for " + deviceType
                    + " subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } catch (SubTypeMgtDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            msg = "Error occurred in the database level while updating device subtype for " + deviceType +
                    " subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
            DeviceSubTypeCacheKey key = DeviceSubTypeMgtUtil.getDeviceSubTypeCacheKey(tenantId, subTypeId, deviceType);
            deviceSubTypeCache.refresh(key);
        }
    }

    @Override
    public synchronized DeviceSubType getDeviceSubType(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtPluginException {
        try {
            DeviceSubTypeCacheKey key = DeviceSubTypeMgtUtil.getDeviceSubTypeCacheKey(tenantId, subTypeId, deviceType);
            return deviceSubTypeCache.get(key);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            String msg = "Not having any" + deviceType + " subtype for subtype id: " + subTypeId;
            log.error(msg, e);
            return null;
        } catch (ExecutionException e) {
            String msg = "Error occurred while obtaining device subtype for subtype id: " + subTypeId;
            log.error(msg, e);
            throw new SubTypeMgtPluginException(msg, e);
        }
    }

    @Override
    public List<DeviceSubType> getAllDeviceSubTypes(int tenantId, String deviceType)
            throws SubTypeMgtPluginException {
        try {
            ConnectionManagerUtil.openDBConnection();
            List<DeviceSubType> subtypes = deviceSubTypeDAO.getAllDeviceSubTypes(tenantId, deviceType);
            DeviceSubTypeCacheKey key;
            for (DeviceSubType dst: subtypes) {
                key = DeviceSubTypeMgtUtil.getDeviceSubTypeCacheKey(tenantId, dst.getSubTypeId(), deviceType);
                deviceSubTypeCache.put(key, dst);
            }
            return subtypes;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve all device subtype for " +
                    deviceType + " subtypes";
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } catch (SubTypeMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving all device subtype for " + deviceType
                    + " subtypes";
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public int getDeviceSubTypeCount(String deviceType) throws SubTypeMgtPluginException {
        try {
            return deviceSubTypeDAO.getDeviceSubTypeCount(deviceType);
        } catch (SubTypeMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving device subtypes count for " + deviceType
                    + " subtypes";
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public DeviceSubType getDeviceSubTypeByProvider(String subTypeName, int tenantId,
                                                    String deviceType)
            throws SubTypeMgtPluginException {
        try {
            return deviceSubTypeDAO.getDeviceSubTypeByProvider(subTypeName, tenantId, deviceType);
        } catch (SubTypeMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving device subtype for " + deviceType
                    + " subtype & subtype name: " + subTypeName;
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public boolean checkDeviceSubTypeExist(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtPluginException {
        try {
            return deviceSubTypeDAO.checkDeviceSubTypeExist(subTypeId, tenantId, deviceType);
        } catch (SubTypeMgtDAOException e) {
            String msg = "Error occurred in the database level while checking device subtype exist  for " + deviceType
                    + " subtype & subtype id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }
}
