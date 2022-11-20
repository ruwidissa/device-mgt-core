/*
 * Copyright (C) 2018 - 2021 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.extensions.logger.spi;

import io.entgra.device.mgt.extensions.logger.LogContext;
import org.apache.commons.logging.Log;

public interface EntgraLogger extends Log {

    void info(String message);

    void info(String message, Throwable t);

    void info(String message, LogContext logContext);

    void debug(String message);

    void debug(String message, Throwable t);

    void debug(String message, LogContext logContext);

    void error(String message);

    void error(String message, Throwable t);

    void error(String message, LogContext logContext);

    void error(String message, Throwable t, LogContext logContext);

    void warn(String message);

    void warn(String message, Throwable t);

    void warn(String message, LogContext logContext);

    void warn(String message, Throwable t, LogContext logContext);

    void trace(String message);

    void trace(String message, Throwable t);

    void trace(String message, LogContext logContext);

    void fatal(String message);

    void fatal(String message, Throwable t);

    void fatal(String message, LogContext logContext);

    boolean isDebugEnabled();

    boolean isErrorEnabled();

    boolean isFatalEnabled();

    boolean isInfoEnabled();

    boolean isTraceEnabled();

    boolean isWarnEnabled();

    void clearLogContext();

}
