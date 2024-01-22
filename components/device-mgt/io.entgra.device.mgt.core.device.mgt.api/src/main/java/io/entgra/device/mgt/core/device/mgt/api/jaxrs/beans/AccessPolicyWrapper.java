package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Set;

@ApiModel(value = "AccessPolicyWrapper", description = "Access policy transferring DTO")
public class AccessPolicyWrapper {
    @JsonProperty(value = "defaultAccessPolicy", required = true)
    @ApiModelProperty(name = "defaultAccessPolicy", value = "Default access policy value", required = true)
    private String defaultAccessPolicy;

    @JsonProperty(value = "emailOutlookAccessPolicy", required = true)
    @ApiModelProperty(name = "emailOutlookAccessPolicy", value = "Email outlook access policy value", required = true)
    private Set<String> emailOutlookAccessPolicy;

    @JsonProperty(value = "POPIMAPAccessPolicy", required = true)
    @ApiModelProperty(name = "POPIMAPAccessPolicy", value = "POP/IMAP access policy value", required = true)
    private String POPIMAPAccessPolicy;

    @JsonProperty(value = "webOutlookAccessPolicy", required = true)
    @ApiModelProperty(name = "webOutlookAccessPolicy", value = "Web outlook access policy value", required = true)
    private String webOutlookAccessPolicy;

    public String getDefaultAccessPolicy() {
        return defaultAccessPolicy;
    }

    public void setDefaultAccessPolicy(String defaultAccessPolicy) {
        this.defaultAccessPolicy = defaultAccessPolicy;
    }

    public Set<String> getEmailOutlookAccessPolicy() {
        return emailOutlookAccessPolicy;
    }

    public void setEmailOutlookAccessPolicy(Set<String> emailOutlookAccessPolicy) {
        this.emailOutlookAccessPolicy = emailOutlookAccessPolicy;
    }

    public String getPOPIMAPAccessPolicy() {
        return POPIMAPAccessPolicy;
    }

    public void setPOPIMAPAccessPolicy(String POPIMAPAccessPolicy) {
        this.POPIMAPAccessPolicy = POPIMAPAccessPolicy;
    }

    public String getWebOutlookAccessPolicy() {
        return webOutlookAccessPolicy;
    }

    public void setWebOutlookAccessPolicy(String webOutlookAccessPolicy) {
        this.webOutlookAccessPolicy = webOutlookAccessPolicy;
    }
}
