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

package io.entgra.device.mgt.core.device.mgt.core.config.permission;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DefaultPermission")
public class DefaultPermission {

    private String name;
    private ScopeMapping scopeMapping;
    private Boolean isAssignableToDefaultRoles;

    @XmlElement(name = "Name", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "MappedScopeDetails", required = true)
    public ScopeMapping getScopeMapping() {
        return scopeMapping;
    }

    public void setScopeMapping(ScopeMapping scopeMapping) {
        this.scopeMapping = scopeMapping;
    }

    @XmlElement(name = "IsAssignableToDefaultRoles", required = false)
    public Boolean isAssignableToDefaultRoles() {
        return isAssignableToDefaultRoles;
    }

    public void setAssignableToDefaultRoles(Boolean isAssignableToDefaultRoles) {
        this.isAssignableToDefaultRoles = isAssignableToDefaultRoles;
    }
}
