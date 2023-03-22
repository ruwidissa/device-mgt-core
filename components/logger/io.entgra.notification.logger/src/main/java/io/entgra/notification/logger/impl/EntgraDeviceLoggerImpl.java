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
package io.entgra.notification.logger.impl;


import io.entgra.device.mgt.extensions.logger.LogContext;
import io.entgra.device.mgt.extensions.logger.spi.EntgraLogger;
import io.entgra.notification.logger.DeviceLogContext;
import io.entgra.notification.logger.util.MDCContextUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;


public class EntgraDeviceLoggerImpl implements EntgraLogger {

    private static Log log = null;

    public EntgraDeviceLoggerImpl(Class<?> clazz) {
        log = LogFactory.getLog(clazz);
    }

    @Override
    public void info(Object object, LogContext logContext) {
    }

    @Override
    public void info(Object object, Throwable t, LogContext logContext) {
    }

    @Override
    public void debug(Object object, LogContext logContext) {
    }

    @Override
    public void debug(Object object, Throwable t, LogContext logContext) {
    }

    @Override
    public void error(Object object, LogContext logContext) {
    }

    @Override
    public void error(Object object, Throwable t, LogContext logContext) {
    }

    @Override
    public void fatal(Object object, LogContext logContext) {
    }

    @Override
    public void fatal(Object object, Throwable t, LogContext logContext) {
    }

    @Override
    public void trace(Object object, LogContext logContext) {

    }

    @Override
    public void trace(Object object, Throwable t, LogContext logContext) {

    }

    @Override
    public void warn(Object object, LogContext logContext) {
    }

    @Override
    public void warn(Object object, Throwable t, LogContext logContext) {
    }

    public void info(String message) {
    }

    public void info(String message, Throwable t) {
        log.info(message, t);
    }

    @Override
    public void info(String message, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.info(message);
    }


    public void debug(String message) {
        log.debug(message);
    }


    public void debug(String message, Throwable t) {
        log.debug(message, t);
    }

    @Override
    public void debug(String message, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.debug(message);
    }

    public void error(String message) {
        log.error(message);
    }


    public void error(String message, Throwable t) {
        log.error(message, t);
    }

    @Override
    public void error(String message, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.error(message);
    }

    @Override
    public void error(String message, Throwable t, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.error(message, t);
    }


    public void warn(String message) {
        log.warn(message);
    }


    public void warn(String message, Throwable t) {
        log.warn(message, t);
    }

    @Override
    public void warn(String message, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.warn(message);
    }

    @Override
    public void warn(String message, Throwable t, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.warn(message, t);
    }


    public void trace(String message) {
        log.trace(message);
    }


    public void trace(String message, Throwable t) {
        log.trace(message, t);
    }

    @Override
    public void trace(String message, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.trace(message);
    }


    public void fatal(String message) {
        log.fatal(message);
    }


    public void fatal(String message, Throwable t) {
        log.fatal(message, t);
    }

    @Override
    public void fatal(String message, LogContext logContext) {
        DeviceLogContext deviceLogContext = (DeviceLogContext) logContext;
        MDCContextUtil.populateDeviceMDCContext(deviceLogContext);
        log.fatal(message);
    }

    @Override
    public void debug(Object o) {
        log.debug(o);
    }

    @Override
    public void debug(Object o, Throwable throwable) {
        log.debug(o, throwable);
    }

    @Override
    public void error(Object o) {
        log.error(o);
    }

    @Override
    public void error(Object o, Throwable throwable) {
        log.error(o, throwable);
    }

    @Override
    public void fatal(Object o) {
        log.fatal(0);
    }

    @Override
    public void fatal(Object o, Throwable throwable) {
        log.fatal(0, throwable);
    }

    @Override
    public void info(Object o) {
        log.info(o);
    }

    @Override
    public void info(Object o, Throwable throwable) {
        log.info(o, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void trace(Object o) {
        log.trace(o);
    }

    @Override
    public void trace(Object o, Throwable throwable) {
        log.trace(o, throwable);
    }

    @Override
    public void warn(Object o) {
        log.warn(o);
    }

    @Override
    public void warn(Object o, Throwable throwable) {
        log.warn(o, throwable);
    }

    @Override
    public void clearLogContext() {
        MDC.clear();
    }
}
