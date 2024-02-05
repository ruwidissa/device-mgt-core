/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.admin.api.bean;

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
