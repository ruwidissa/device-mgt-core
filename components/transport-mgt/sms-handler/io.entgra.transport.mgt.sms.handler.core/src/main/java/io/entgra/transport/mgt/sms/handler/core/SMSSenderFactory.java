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

package io.entgra.transport.mgt.sms.handler.core;

import io.entgra.transport.mgt.sms.handler.common.config.SMSGateway;
import io.entgra.transport.mgt.sms.handler.common.spi.SMSSender;
import io.entgra.transport.mgt.sms.handler.core.config.SMSConfigurationManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory class to retrieve the relevant SMSSender based on SMS Configuration.
 * There can be multiple SMSSender Implementation and an instance of that specific SMSSender can be
 * created and retrieved via this factory class.
 */
public class SMSSenderFactory {

    private static final Log log = LogFactory.getLog(SMSSenderFactory.class);

    /**
     * Retrieve the SMSSender based on the default SMS Gateway in SMS configuration.
     * @return an instance of {@link SMSSender}
     */
    public static SMSSender getSMSSender() {
        SMSGateway smsGateway = SMSConfigurationManager.getInstance().getSMSConfig().getDefaultSMSGateway();
        if (smsGateway != null) {
            return getSMSSender(smsGateway.getExtensionClass());
        }
        return null;
    }

    /**
     * Retrieve the SMSSender based on the provided extension class name.
     * @param extensionClass has the class name of the implemented SMSSender using which
     *                       an instance of SMSSender is created
     * @return an instance of {@link SMSSender}
     */
    public static SMSSender getSMSSender(String extensionClass) {
        if (StringUtils.isNotBlank(extensionClass)) {
            try {
                Class<?> clz = Class.forName(extensionClass);
                return (SMSSender) clz.newInstance();
            } catch (ClassNotFoundException e) {
                log.error("Extension class '" + extensionClass + "' cannot be located", e);
            } catch (IllegalAccessException e) {
                log.error("Can't access  the class '" + extensionClass
                        + "' or its nullary constructor is not accessible", e);
            } catch (InstantiationException e) {
                log.error("Extension class '" + extensionClass + "' instantiation is failed", e);
            }
        }
        return null;
    }
}
