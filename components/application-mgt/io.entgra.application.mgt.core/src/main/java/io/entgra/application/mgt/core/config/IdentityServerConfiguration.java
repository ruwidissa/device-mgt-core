/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package io.entgra.application.mgt.core.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Represents the Application Management Configuration.
 */
@XmlRootElement(name = "IdentityServerConfiguration")
public class IdentityServerConfiguration {

    private List<IdentityServerDetail> identityServers;

    @XmlElementWrapper(name = "IdentityServers")
    @XmlElement(name = "IdentityServerDTO")
    public List<IdentityServerDetail> getIdentityServers() {
        return identityServers;
    }

    public IdentityServerDetail getIdentityServerDetailByProviderName(String identityServerProviderName) {
        for (IdentityServerDetail identityServerDetail : identityServers) {
            if (identityServerDetail.getProviderName().equals(identityServerProviderName)) {
                return identityServerDetail;
            }
        }
        return null;
    }

    public void setIdentityServers(List<IdentityServerDetail> identityServers) {
        this.identityServers = identityServers;
    }
}

