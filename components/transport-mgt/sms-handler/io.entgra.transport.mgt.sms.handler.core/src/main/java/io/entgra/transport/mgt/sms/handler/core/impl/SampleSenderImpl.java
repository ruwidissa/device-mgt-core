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

package io.entgra.transport.mgt.sms.handler.core.impl;

import io.entgra.transport.mgt.sms.handler.common.bean.SMSMessage;
import io.entgra.transport.mgt.sms.handler.common.config.SMSGateway;
import io.entgra.transport.mgt.sms.handler.common.exception.SMSSenderException;
import io.entgra.transport.mgt.sms.handler.common.spi.SMSSender;
import io.entgra.transport.mgt.sms.handler.core.config.SMSConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sample sender class on how to implement the SMSSender
 */
public class SampleSenderImpl implements SMSSender {

    private static final Log log = LogFactory.getLog(SampleSenderImpl.class);

    @Override
    public void sendSMS(SMSMessage smsMessage) throws SMSSenderException {

        //Retrieve the default (isDefault=true) SMS Gateway in the SMS configuration in sms-config.xml
        SMSGateway smsGateway = SMSConfigurationManager.getInstance().getSMSConfig().getDefaultSMSGateway();

        //Retrieve the SMS Gateway by passing the Gateway name
        smsGateway = SMSConfigurationManager.getInstance().getSMSConfig().getSMSGateway("sample");

        //Retrieve the properties in the SMS Gateway by passing the property name
        String sampleProperty = smsGateway.getPropertyByName("sample-property").getValue();

        if (log.isDebugEnabled()) {
            log.debug("This is a Sample Sender Implementation and the property value retrieved is '"
                    + sampleProperty + "'");
        }

        //If any exception is catched, wrap and throw as SMSSenderException
    }
}
