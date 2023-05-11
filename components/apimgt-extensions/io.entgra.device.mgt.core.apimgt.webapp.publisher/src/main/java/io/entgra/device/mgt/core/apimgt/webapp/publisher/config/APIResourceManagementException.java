package io.entgra.device.mgt.core.apimgt.webapp.publisher.config;

public class APIResourceManagementException extends Exception {
    private static final long serialVersionUID = -3151279311929070297L;


    public APIResourceManagementException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }
}
