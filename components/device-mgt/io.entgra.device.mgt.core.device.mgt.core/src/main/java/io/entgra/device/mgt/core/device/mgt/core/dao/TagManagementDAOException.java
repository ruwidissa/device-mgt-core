package io.entgra.device.mgt.core.device.mgt.core.dao;

/**
 * Custom exception class for tag management data access related exceptions.
 */
public class TagManagementDAOException extends Exception {

    private static final long serialVersionUID = 2021891706072918864L;
    private String message;
    private boolean uniqueConstraintViolation;

    /**
     * Constructs a new exception with the specified detail message and nested exception.
     *
     * @param message         error message
     * @param nestedException exception
     */
    public TagManagementDAOException(String message, Exception nestedException) {
        super(message, nestedException);
        setErrorMessage(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of this exception.
     */
    public TagManagementDAOException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public TagManagementDAOException(String message) {
        super(message);
        setErrorMessage(message);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of this exception.
     */
    public TagManagementDAOException(Throwable cause) {
        super(cause);
    }

    public TagManagementDAOException(String message, Throwable cause, boolean uniqueConstraintViolation) {
        super(message, cause);
        setErrorMessage(message);
        this.uniqueConstraintViolation = uniqueConstraintViolation;
    }

    public String getMessage() {
        return message;
    }

    public void setErrorMessage(String errorMessage) {
        this.message = errorMessage;
    }

    public boolean isUniqueConstraintViolation() {
        return uniqueConstraintViolation;
    }
}
