package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.dto;

public class RootChildrenRequest extends PaginationRequest{

    int maxDepth;
    boolean includeDevice;

    public RootChildrenRequest(int start, int limit) {
        super(start, limit);
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public boolean isIncludeDevice() {
        return includeDevice;
    }

    public void setIncludeDevice(boolean includeDevice) {
        this.includeDevice = includeDevice;
    }
}
