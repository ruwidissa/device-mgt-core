package org.wso2.carbon.device.mgt.common;

import java.io.Serializable;
import java.util.List;

/**
 * This class carries information related to device filtering values which will be used in the UI to filter devices.
 */
public class DeviceFilters implements Serializable {

    private static final long serialVersionUID = -5249449134602406387L;

    private List<String> deviceTypes;
    private List<String> ownerships;
    private List<String> statuses;

    public List<String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public List<String> getOwnerships() {
        return ownerships;
    }

    public void setOwnerships(List<String> ownerships) {
        this.ownerships = ownerships;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }
}
