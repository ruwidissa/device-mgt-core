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

package io.entgra.transport.mgt.sms.handler.common.spi;

import io.entgra.transport.mgt.sms.handler.common.bean.SMSMessage;
import io.entgra.transport.mgt.sms.handler.common.exception.SMSSenderException;

/**
 * Manages the sending of SMS.
 * This interface can be implemented by SMS Senders and specific implementation can be done
 * depending on the SMS Sender.
 */
public interface SMSSender {

    /**
     * Responsible for sending SMS.
     * This method can be implemented by the relevant SMS Senders for specific sender related implementation.
     * @param smsMessage which has data on the SMS to be send
     * @throws SMSSenderException catches all other exception and {@link SMSSenderException} is thrown
     */
    void sendSMS(SMSMessage smsMessage) throws SMSSenderException;
}
