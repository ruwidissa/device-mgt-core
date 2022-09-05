/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.traccar.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "TraccarConfiguration")
public class TraccarConfiguration {

    private List<TraccarGateway> traccarGateways;

    @XmlElementWrapper(name = "Gateways")
    @XmlElement(name = "Gateway")
    public List<TraccarGateway> getTraccarGateways() {
        return traccarGateways;
    }

    public void setTraccarGateways(List<TraccarGateway> traccarGateways) {
        this.traccarGateways = traccarGateways;
    }

    /**
     * Retrieve the default Traccar Gateway as defined in the Traccar configuration.
     * @return default {@link TraccarGateway}
     */
    public TraccarGateway getDefaultTraccarGateway() {
        for (TraccarGateway traccarGateway : traccarGateways) {
            if (traccarGateway.isDefault()) {
                return traccarGateway;
            }
        }
        return null;
    }

    /**
     * Retrieve Traccar Gateway by the provided Gateway Name
     * @param gatewayName has the name of the Gateway to be retrieved
     * @return retrieved {@link TraccarGateway}
     */
    public TraccarGateway getTraccarGateway(String gatewayName) {
        for (TraccarGateway traccarGateway : traccarGateways) {
            if (gatewayName.equals(traccarGateway.getName())) {
                return traccarGateway;
            }
        }
        return null;
    }
}
