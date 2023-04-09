package io.entgra.tenant.mgt.common.exception;

public class TenantMgtException extends Exception {
    public TenantMgtException(String msg, Throwable ex) {
        super(msg, ex);
    }

    public TenantMgtException(String msg) {
        super(msg);
    }
}
