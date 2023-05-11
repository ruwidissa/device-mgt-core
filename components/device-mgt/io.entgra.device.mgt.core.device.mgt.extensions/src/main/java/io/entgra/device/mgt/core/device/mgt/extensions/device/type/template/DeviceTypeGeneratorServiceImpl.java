package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template;

import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceTypeGeneratorService;
import io.entgra.device.mgt.core.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;

public class DeviceTypeGeneratorServiceImpl implements DeviceTypeGeneratorService {

    @Override
    public DeviceManagementService populateDeviceManagementService(String deviceTypeName
            , DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        return new HTTPDeviceTypeManagerService(deviceTypeName, deviceTypeMetaDefinition);
    }
}
