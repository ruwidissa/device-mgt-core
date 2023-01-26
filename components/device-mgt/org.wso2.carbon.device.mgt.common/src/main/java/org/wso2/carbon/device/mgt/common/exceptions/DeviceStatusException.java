package org.wso2.carbon.device.mgt.common.exceptions;

public class DeviceStatusException extends Exception{

    private static final long serialVersionUID = 1608833587090532707L;

    public DeviceStatusException() {
    }

    public DeviceStatusException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }


    public DeviceStatusException(String message) {
        super(message);
    }

    public DeviceStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceStatusException(Throwable cause) {
        super(cause);
    }

    public DeviceStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
