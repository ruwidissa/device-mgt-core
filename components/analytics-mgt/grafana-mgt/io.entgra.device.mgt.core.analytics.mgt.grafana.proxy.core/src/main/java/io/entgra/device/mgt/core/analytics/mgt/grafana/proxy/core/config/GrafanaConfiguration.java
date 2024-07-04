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

package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.config;

import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.config.xml.bean.CacheConfiguration;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.config.xml.bean.ValidationConfig;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.config.xml.bean.User;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "GrafanaConfiguration")
public class GrafanaConfiguration {

    private User adminUser;
    private ValidationConfig validationConfig;
    private List<CacheConfiguration> caches;

    @XmlElement(name = "AdminUser")
    public User getAdminUser() {
        return adminUser;
    }

    @XmlElement(name = "ValidationConfig")
    public ValidationConfig getValidationConfig() {
        return validationConfig;
    }

    public void setValidationConfig(ValidationConfig validationConfig) {
        this.validationConfig = validationConfig;
    }

    public void setAdminUser(User user) {
        this.adminUser = user;
    }


    @XmlElementWrapper(name = "Cache")
    @XmlElement(name = "CacheConfiguration")
    public List<CacheConfiguration> getCaches() {
        return caches;
    }

    public CacheConfiguration getCacheByName(String cacheName) {
        for (CacheConfiguration cache : caches) {
            if (cache.getName().equals(cacheName)) {
                return cache;
            }
        }
        return null;
    }

    public void setCaches(List<CacheConfiguration> caches) {
        this.caches = caches;
    }
}
