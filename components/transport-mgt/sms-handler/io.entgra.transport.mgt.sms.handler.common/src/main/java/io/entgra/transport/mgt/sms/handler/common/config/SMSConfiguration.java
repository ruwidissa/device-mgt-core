/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.transport.mgt.sms.handler.common.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "SMSConfiguration")
public class SMSConfiguration {

    private List<SMSGateway> smsGateways;

    @XmlElementWrapper(name = "Gateways")
    @XmlElement(name = "Gateway")
    public List<SMSGateway> getSMSGateways() {
        return smsGateways;
    }

    public void setSMSGateways(List<SMSGateway> smsGateways) {
        this.smsGateways = smsGateways;
    }

    /**
     * Retrieve the default SMS Gateway as defined in the SMS configuration.
     * @return default {@link SMSGateway}
     */
    public SMSGateway getDefaultSMSGateway() {
        for (SMSGateway smsGateway : smsGateways) {
            if (smsGateway.isDefault()) {
                return smsGateway;
            }
        }
        return null;
    }

    /**
     * Retrieve SMS Gateway by the provided Gateway Name
     * @param gatewayName has the name of the Gateway to be retrieved
     * @return retrieved {@link SMSGateway}
     */
    public SMSGateway getSMSGateway(String gatewayName) {
        for (SMSGateway smsGateway : smsGateways) {
            if (gatewayName.equals(smsGateway.getName())) {
                return smsGateway;
            }
        }
        return null;
    }
}
