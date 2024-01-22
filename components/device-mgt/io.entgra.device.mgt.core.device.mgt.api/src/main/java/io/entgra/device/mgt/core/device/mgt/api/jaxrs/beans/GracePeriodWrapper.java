package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

@ApiModel(value = "GracePeriodWrapper", description = "GracePeriod transferring DTO")
public class GracePeriodWrapper {

    @JsonProperty(value = "gracePeriod", required = true)
    @ApiModelProperty(name = "gracePeriod", value = "Grace period in days", required = true)
    private int gracePeriod;

    @JsonProperty(value = "graceAllowedPolicy", required = true)
    @ApiModelProperty(name = "graceAllowedPolicy", value = "Grace allowed policy values", required = true)
    private String graceAllowedPolicy;

    public int getGracePeriod() {
        return gracePeriod;
    }

    public void setGracePeriod(int gracePeriod) {
        this.gracePeriod = gracePeriod;
    }

    public String getGraceAllowedPolicy() {
        return graceAllowedPolicy;
    }

    public void setGraceAllowedPolicy(String graceAllowedPolicy) {
        this.graceAllowedPolicy = graceAllowedPolicy;
    }
}
