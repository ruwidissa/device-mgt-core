/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.jaxrs.beans.analytics;

import io.swagger.annotations.ApiModelProperty;

/**
 * Execution plan definition including :
 * Attributes : Name and Plan data.
 */
public class SiddhiExecutionPlan {
    @ApiModelProperty(value = "Execution plan name")
    private String executionPlanName;
    @ApiModelProperty(value = "Execution plan")
    private String executionPlanData;

    public String getExecutionPlanName() {
        return executionPlanName;
    }

    public void setExecutionPlanName(String executionPlanName) {
        this.executionPlanName = executionPlanName;
    }

    public String getExecutionPlanData() {
        return executionPlanData;
    }

    public void setExecutionPlanData(String executionPlanData) {
        this.executionPlanData = executionPlanData;
    }
}
