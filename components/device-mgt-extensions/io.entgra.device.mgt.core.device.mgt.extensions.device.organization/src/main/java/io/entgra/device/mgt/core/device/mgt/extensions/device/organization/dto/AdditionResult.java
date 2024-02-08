package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto;

public class AdditionResult {

    private boolean isCyclic;
    private boolean isInserted;

    public boolean isCyclic() {
        return isCyclic;
    }

    public void setCyclic(boolean cyclic) {
        isCyclic = cyclic;
    }

    public boolean isInserted() {
        return isInserted;
    }

    public void setInserted(boolean inserted) {
        isInserted = inserted;
    }
}
