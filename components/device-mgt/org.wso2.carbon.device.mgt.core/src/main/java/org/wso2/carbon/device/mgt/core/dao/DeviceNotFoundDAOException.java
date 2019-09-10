package org.wso2.carbon.device.mgt.core.dao;

/**
 * Throws if the querying device is not found in the DB
 */
public class DeviceNotFoundDAOException extends Exception {
    private static final long serialVersionUID = 2126172787830234694L;

    public DeviceNotFoundDAOException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public DeviceNotFoundDAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceNotFoundDAOException(String msg) {
        super(msg);
    }

    public DeviceNotFoundDAOException() {
        super();
    }

    public DeviceNotFoundDAOException(Throwable cause) {
        super(cause);
    }
}
