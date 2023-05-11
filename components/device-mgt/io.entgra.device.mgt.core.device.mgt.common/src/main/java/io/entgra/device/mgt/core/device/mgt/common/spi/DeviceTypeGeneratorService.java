package io.entgra.device.mgt.core.device.mgt.common.spi;

import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;

/**
 * This implementation populates device management service.
 */
public interface DeviceTypeGeneratorService {

    DeviceManagementService populateDeviceManagementService(String deviceTypeName
            , DeviceTypeMetaDefinition deviceTypeMetaDefinition);

}
