package org.wso2.carbon.device.mgt.extensions.device.type.template.exception;

public class DeviceTypePluginExtensionException extends RuntimeException {

    public DeviceTypePluginExtensionException(String msg) {
        super(msg);
    }

    public DeviceTypePluginExtensionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
