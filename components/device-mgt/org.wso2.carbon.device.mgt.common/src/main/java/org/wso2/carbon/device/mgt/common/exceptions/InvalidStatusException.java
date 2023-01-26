package org.wso2.carbon.device.mgt.common.exceptions;

public class InvalidStatusException extends Exception{

    private static final long serialVersionUID = -7379721600057895944L;


    public InvalidStatusException() {
    }

    public InvalidStatusException(String message) {
        super(message);
    }

    public InvalidStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidStatusException(Throwable cause) {
        super(cause);
    }

    public InvalidStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }


}
