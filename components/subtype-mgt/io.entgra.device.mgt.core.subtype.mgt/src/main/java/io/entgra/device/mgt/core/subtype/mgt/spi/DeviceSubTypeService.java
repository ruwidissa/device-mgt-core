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

package io.entgra.device.mgt.core.subtype.mgt.spi;

import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtPluginException;

import java.util.List;

public interface DeviceSubTypeService {

    boolean addDeviceSubType(DeviceSubType deviceSubType) throws SubTypeMgtPluginException;

    boolean updateDeviceSubType(String subTypeId, int tenantId, String deviceType, String subTypeName,
                                String typeDefinition) throws SubTypeMgtPluginException;

    DeviceSubType getDeviceSubType(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtPluginException;

    List<DeviceSubType> getAllDeviceSubTypes(int tenantId, String deviceType)
            throws SubTypeMgtPluginException;

    int getDeviceSubTypeCount(String deviceType) throws SubTypeMgtPluginException;

    DeviceSubType getDeviceSubTypeByProvider(String subTypeName, int tenantId, String deviceType)
            throws SubTypeMgtPluginException;

    boolean checkDeviceSubTypeExist(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtPluginException;
}
