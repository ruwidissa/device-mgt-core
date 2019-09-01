package org.wso2.carbon.device.mgt.common.configuration.mgt;

public class AmbiguousConfigurationException extends Exception{
    private static final long serialVersionUID = 7039039961721642766L;

    public AmbiguousConfigurationException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public AmbiguousConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmbiguousConfigurationException(String msg) {
        super(msg);
    }

    public AmbiguousConfigurationException() {
        super();
    }

    public AmbiguousConfigurationException(Throwable cause) {
        super(cause);
    }
}
