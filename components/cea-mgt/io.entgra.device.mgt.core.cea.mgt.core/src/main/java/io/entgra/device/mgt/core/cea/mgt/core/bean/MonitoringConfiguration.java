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

package io.entgra.device.mgt.core.cea.mgt.core.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MonitoringConfiguration")
public class MonitoringConfiguration {
    private boolean monitoringEnable;
    private long monitoringFrequency;
    private String monitoringClazz;

    public boolean isMonitoringEnable() {
        return monitoringEnable;
    }

    @XmlElement(name = "MonitoringEnable", required = true)
    public void setMonitoringEnable(boolean monitoringEnable) {
        this.monitoringEnable = monitoringEnable;
    }

    public long getMonitoringFrequency() {
        return monitoringFrequency;
    }

    @XmlElement(name = "MonitoringFrequency", required = true)
    public void setMonitoringFrequency(long monitoringFrequency) {
        this.monitoringFrequency = monitoringFrequency;
    }

    public String getMonitoringClazz() {
        return monitoringClazz;
    }

    @XmlElement(name = "MonitoringClazz", required = true)
    public void setMonitoringClazz(String monitoringClazz) {
        this.monitoringClazz = monitoringClazz;
    }
}
