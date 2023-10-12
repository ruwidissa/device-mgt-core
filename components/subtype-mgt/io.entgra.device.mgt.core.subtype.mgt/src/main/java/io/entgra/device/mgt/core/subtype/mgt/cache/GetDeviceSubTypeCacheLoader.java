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

package io.entgra.device.mgt.core.subtype.mgt.cache;

import com.google.common.cache.CacheLoader;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubTypeCacheKey;
import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAO;
import io.entgra.device.mgt.core.subtype.mgt.dao.DeviceSubTypeDAOFactory;
import io.entgra.device.mgt.core.subtype.mgt.dao.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.DBConnectionException;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtDAOException;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtPluginException;
import io.entgra.device.mgt.core.subtype.mgt.util.DeviceSubTypeMgtUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class GetDeviceSubTypeCacheLoader extends CacheLoader<String, DeviceSubType> {

    private static final Log log = LogFactory.getLog(GetDeviceSubTypeCacheLoader.class);

    private final DeviceSubTypeDAO deviceSubTypeDAO;

    public GetDeviceSubTypeCacheLoader() {
        this.deviceSubTypeDAO = DeviceSubTypeDAOFactory.getDeviceSubTypeDAO();
    }

    @Override
    public DeviceSubType load(String key) throws SubTypeMgtPluginException {
        DeviceSubTypeCacheKey deviceSubTypeCacheKey = DeviceSubTypeMgtUtil.getDeviceSubTypeCacheKey(key);
        int tenantId = deviceSubTypeCacheKey.getTenantId();
        String subTypeId = deviceSubTypeCacheKey.getSubTypeId();
        String deviceType = deviceSubTypeCacheKey.getDeviceType();

        if (log.isTraceEnabled()) {
            log.trace("Loading Device subtype for " + deviceType + " subtype & subtype Id : " + subTypeId);
        }
        try {
            ConnectionManagerUtil.openDBConnection();
            return deviceSubTypeDAO.getDeviceSubType(subTypeId, tenantId, deviceType);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the database connection to retrieve device subtype for " +
                    deviceType + " subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } catch (InvalidCacheLoadException e) {
            String msg = "CacheLoader returned null for device subtype: " + deviceType + " subtype & subtype Id: " +
                    subTypeId;
            log.error(msg, e);
            return null;
        } catch (SubTypeMgtDAOException e) {
            String msg = "Error occurred in the database level while retrieving device subtype for " + deviceType
                    + " subtype & subtype Id: " + subTypeId;
            log.error(msg);
            throw new SubTypeMgtPluginException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

}
