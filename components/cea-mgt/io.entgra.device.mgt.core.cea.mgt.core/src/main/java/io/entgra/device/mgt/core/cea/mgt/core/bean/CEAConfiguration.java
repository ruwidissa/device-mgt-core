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

import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "CEAConfiguration")
public class CEAConfiguration {
    private List<ActiveSyncServerConfiguration> activeSyncServerConfigurations;
    private MonitoringConfiguration monitoringConfiguration;

    public List<ActiveSyncServerConfiguration> getActiveSyncServerConfigurations() {
        return activeSyncServerConfigurations;
    }

    @XmlElementWrapper(name = "ActiveSyncServerConfigurations", required = true)
    @XmlElement(name = "ActiveSyncServerConfiguration", required = true)
    public void setActiveSyncServerConfigurations(List<ActiveSyncServerConfiguration> activeSyncServerConfigurations) {
        this.activeSyncServerConfigurations = activeSyncServerConfigurations;
    }

    public ActiveSyncServerConfiguration getActiveSyncServerConfiguration(ActiveSyncServer activeSyncServer) {
        ActiveSyncServerConfiguration activeSyncServerConfiguration = null;
        for (ActiveSyncServerConfiguration config : activeSyncServerConfigurations) {
            if (Objects.equals(config.getKey(), activeSyncServer.getKey())) {
                activeSyncServerConfiguration = config;
            }
        }
        return activeSyncServerConfiguration;
    }

    public boolean isServerSupport(ActiveSyncServer activeSyncServer) {
        for (ActiveSyncServerConfiguration config : activeSyncServerConfigurations) {
            if (Objects.equals(config.getKey(), activeSyncServer.getKey())) {
                return true;
            }
        }
        return false;
    }

    public MonitoringConfiguration getMonitoringConfiguration() {
        return monitoringConfiguration;
    }

    @XmlElement(name = "MonitoringConfiguration", required = true)
    public void setMonitoringConfiguration(MonitoringConfiguration monitoringConfiguration) {
        this.monitoringConfiguration = monitoringConfiguration;
    }
}
