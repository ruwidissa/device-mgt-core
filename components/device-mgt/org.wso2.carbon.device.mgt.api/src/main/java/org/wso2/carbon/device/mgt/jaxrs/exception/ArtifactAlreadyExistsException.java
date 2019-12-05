package org.wso2.carbon.device.mgt.jaxrs.exception;

public class ArtifactAlreadyExistsException extends Exception {
    private static final long serialVersionUID = 6459451028947683202L;
    private String message;
    private Throwable cause;

    public ArtifactAlreadyExistsException(String message) {
        this.message = message;
    }

    public ArtifactAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }
}
