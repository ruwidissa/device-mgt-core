/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.core.config.analytics;

import io.entgra.device.mgt.core.device.mgt.core.config.analytics.operation.OperationResponseConfigurations;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the information related to Device Operation Analytics configuration.
 */
@XmlRootElement(name = "OperationAnalyticsConfiguration")
public class OperationAnalyticsConfiguration {

    private boolean isPublishDeviceInfoResponseEnabled;
    private boolean isPublishLocationResponseEnabled;
    private OperationResponseConfigurations operationResponseConfigurations;

    public boolean isPublishDeviceInfoResponseEnabled() {
        return isPublishDeviceInfoResponseEnabled;
    }

    @XmlElement(name = "PublishDeviceInfoResponse", required = true)
    public void setPublishDeviceInfoResponseEnabled(boolean publishDeviceInfoResponseEnabled) {
        this.isPublishDeviceInfoResponseEnabled = publishDeviceInfoResponseEnabled;
    }

    public boolean isPublishLocationResponseEnabled() {
        return isPublishLocationResponseEnabled;
    }

    @XmlElement(name = "PublishLocationResponse", required = true)
    public void setPublishLocationResponseEnabled(boolean publishLocationResponseEnabled) {
        this.isPublishLocationResponseEnabled = publishLocationResponseEnabled;
    }

    public OperationResponseConfigurations getOperationResponseConfigurations() {
        return operationResponseConfigurations;
    }

    @XmlElement(name = "PublishOperationResponse", required = true)
    public void setOperationResponseConfigurations(
            OperationResponseConfigurations operationResponseConfigurations) {
        this.operationResponseConfigurations = operationResponseConfigurations;
    }
}
