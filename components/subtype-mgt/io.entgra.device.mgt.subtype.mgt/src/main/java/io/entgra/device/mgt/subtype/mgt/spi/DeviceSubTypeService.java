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

package io.entgra.device.mgt.subtype.mgt.spi;

import io.entgra.device.mgt.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.subtype.mgt.exception.SubTypeMgtPluginException;

import java.util.List;

public interface DeviceSubTypeService {

    boolean addDeviceSubType(DeviceSubType deviceSubType) throws SubTypeMgtPluginException;

    boolean updateDeviceSubType(int subTypeId, int tenantId, DeviceSubType.DeviceType deviceType, String subTypeName,
                                String typeDefinition) throws SubTypeMgtPluginException;

    DeviceSubType getDeviceSubType(int subTypeId, int tenantId, DeviceSubType.DeviceType deviceType)
            throws SubTypeMgtPluginException;

    List<DeviceSubType> getAllDeviceSubTypes(int tenantId, DeviceSubType.DeviceType deviceType)
            throws SubTypeMgtPluginException;

    int getDeviceSubTypeCount(DeviceSubType.DeviceType deviceType) throws SubTypeMgtPluginException;

    DeviceSubType getDeviceSubTypeByProvider(String subTypeName, int tenantId, DeviceSubType.DeviceType deviceType)
            throws SubTypeMgtPluginException;
}
