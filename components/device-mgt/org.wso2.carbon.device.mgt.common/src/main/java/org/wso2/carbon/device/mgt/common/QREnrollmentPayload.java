package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class represents the information of QR enrollment payload.
 */
@ApiModel(value = "QREnrollmentPayload",
        description = "Details related to QR enrollment to generate QR code.")
public class QREnrollmentPayload implements Serializable {

    @ApiModelProperty(name = "payload", value = "Platform configuration payload to generate QR code.", required = true)
    private Map<String, Object> payload;

    @ApiModelProperty(name = "invalidPlatformConfigs", value = "Invalid platform configs to show when an " +
            "invalidation occurs.", required = true)
    private List<String> invalidPlatformConfigs;

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public List<String> getInvalidPlatformConfigs() {
        return invalidPlatformConfigs;
    }

    public void setInvalidPlatformConfigs(List<String> invalidPlatformConfigs) {
        this.invalidPlatformConfigs = invalidPlatformConfigs;
    }
}
