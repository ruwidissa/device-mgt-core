/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common.policy.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

@ApiModel(
        value = "CorrectiveAction",
        description = "This bean carries all information related corrective action which is required " +
                      "when a policy is violated."
)
public class CorrectiveAction implements Serializable {

    private static final long serialVersionUID = -3414709449056070148L;

    @ApiModelProperty(
            name = "action",
            value = "Corrective action type (POLICY or OPERATION) to trigger when a policy is violated.",
            example = "POLICY",
            required = true
    )
    private String actionType;

    @ApiModelProperty(
            name = "policyId",
            value = "When corrective action is POLICY, the corrective policy ID to be applied when a policy " +
                    "is violated.",
            example = "1"
    )
    private int policyId;

    @ApiModelProperty(
            name = "operations",
            value = "When corrective action is OPERATION, the list of operations in features to be applied " +
                    "when a policy is violated."
    )
    private List<ProfileFeature> operations;

    private int featureId;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public List<ProfileFeature> getOperations() {
        return operations;
    }

    public void setOperations(List<ProfileFeature> operations) {
        this.operations = operations;
    }

    public int getFeatureId() {
        return featureId;
    }

    public void setFeatureId(int featureId) {
        this.featureId = featureId;
    }
}
