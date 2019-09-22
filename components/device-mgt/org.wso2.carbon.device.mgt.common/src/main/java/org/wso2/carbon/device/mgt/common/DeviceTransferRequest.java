package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Represents an individual configuration entry.
 */
@ApiModel(value = "DeviceTransferRequest", description = "This class carries all information related to device " +
        "transfer from super tenant to another tenant.")
public class DeviceTransferRequest {

    @ApiModelProperty(name = "deviceType", value = "Type of the device", required = true)
    private String deviceType;

    @ApiModelProperty(name = "deviceIds", value = "Ids of devices to transfer", required = true)
    private List<String> deviceIds;

    @ApiModelProperty(name = "destinationTenant", value = "Destination Tenant ID", required = true)
    private String destinationTenant;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceId(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public String getDestinationTenant() {
        return destinationTenant;
    }

    public void setDestinationTenant(String destinationTenant) {
        this.destinationTenant = destinationTenant;
    }

}
