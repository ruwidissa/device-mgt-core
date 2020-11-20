package org.wso2.carbon.device.mgt.common;

public class DynamicTaskContext {

    private int serverHashIndex;
    private int activeServerCount;
    private boolean partitioningEnabled = false;

    public int getServerHashIndex() {
        return serverHashIndex;
    }

    public void setServerHashIndex(int serverHashIndex) {
        this.serverHashIndex = serverHashIndex;
    }

    public int getActiveServerCount() {
        return activeServerCount;
    }

    public void setActiveServerCount(int activeServerCount) {
        this.activeServerCount = activeServerCount;
    }

    public boolean isPartitioningEnabled() {
        return partitioningEnabled;
    }

    public void setPartitioningEnabled(boolean partitioningEnabled) {
        this.partitioningEnabled = partitioningEnabled;
    }
}
