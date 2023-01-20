/*
 * Copyright (c) 2023, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
package io.entgra.logger.impl;

import io.entgra.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.notification.logger.DeviceLogContext;
import io.entgra.notification.logger.impl.EntgraLoggerImpl;
import org.testng.annotations.Test;

public class EntgraLoggerImplTest {

    private final EntgraLogger log = new EntgraLoggerImpl(EntgraLoggerImplTest.class);

    @Test
    public void logTest() {

        DeviceLogContext.Builder deviceLogContext = new DeviceLogContext.Builder();

        deviceLogContext.setDeviceName("M02S");
        deviceLogContext.setDeviceType("Android");
        deviceLogContext.setOperationCode("1222");
        deviceLogContext.setTenantID("1234");

        log.debug("Test debug message", deviceLogContext.build());
        log.info("Test info message", deviceLogContext.build());
        log.error("Test error message", deviceLogContext.build());
        log.warn("Test warn message", deviceLogContext.build());
        log.trace("Test trace message", deviceLogContext.build());
        log.fatal("Test fatal message", deviceLogContext.build());
        log.error("debug message test", new Throwable("error throw"));
        log.info("info message test");

//        log.isDebugEnabled();
//        log.isErrorEnabled();
//        log.isFatalEnabled();
//        log.isInfoEnabled();
//        log.isTraceEnabled();
//        log.isWarnEnabled();

    }

}
