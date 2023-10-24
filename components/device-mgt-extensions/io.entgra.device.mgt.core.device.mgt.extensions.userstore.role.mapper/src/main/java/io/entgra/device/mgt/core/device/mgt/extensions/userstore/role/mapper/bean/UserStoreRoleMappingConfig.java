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
package io.entgra.device.mgt.core.device.mgt.extensions.userstore.role.mapper.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(
        name = "UserStoreRoleMappingConfig"
)
public class UserStoreRoleMappingConfig {

    private boolean enabled;
    private List<RoleMapping> mappings;

    private long initialDelayInSeconds;

    private long periodInSeconds;

    @XmlElement(
            name = "enabled"
    )
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlElementWrapper(
            name = "mappings"
    )
    @XmlElement(
            name = "mapping"
    )
    public List<RoleMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<RoleMapping> mappings) {
        this.mappings = mappings;
    }

    @XmlElement(
            name = "initialDelayInSeconds"
    )
    public long getInitialDelayInSeconds() {
        return initialDelayInSeconds;
    }

    public void setInitialDelayInSeconds(long initialDelayInSeconds) {
        this.initialDelayInSeconds = initialDelayInSeconds;
    }

    @XmlElement(
            name = "periodInSeconds"
    )
    public long getPeriodInSeconds() {
        return periodInSeconds;
    }

    public void setPeriodInSeconds(long periodInSeconds) {
        this.periodInSeconds = periodInSeconds;
    }
}
