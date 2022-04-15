package io.entgra.application.mgt.common;

public class OperationStatusBean {

    private int operationId;
    private String status;
    private String operationCode;

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }
}
