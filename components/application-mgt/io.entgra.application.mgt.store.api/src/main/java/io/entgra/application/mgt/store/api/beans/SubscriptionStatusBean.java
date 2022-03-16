package io.entgra.application.mgt.store.api.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This is used to map the status of subscription.
 */
@ApiModel(
        value = "SubscriptionStatusBean",
        description = "This class carries all information related map statuses of the subscription."
)
public class SubscriptionStatusBean {
    @ApiModelProperty(
            name = "sub id",
            value = "Subscription Id.",
            required = true
    )
    private int subId;

    @ApiModelProperty(
            name = "status",
            value = "Status of the subscription.",
            required = true
    )
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getSubId() {
        return subId;
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }
}
