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

    void info(Object object, LogContext logContext);
    
    void info(Object object, Throwable t, LogContext logContext);

    void debug(Object object, LogContext logContext);

    void debug(Object object, Throwable t, LogContext logContext);

    void error(Object object, LogContext logContext);

    void error(Object object, Throwable t, LogContext logContext);

    void fatal(Object object, LogContext logContext);

    void fatal(Object object, Throwable t, LogContext logContext);

    void trace(Object object, LogContext logContext);

    void trace(Object object, Throwable t, LogContext logContext);

    void warn(Object object, LogContext logContext);

    void warn(Object object, Throwable t, LogContext logContext);

    void info(String message, LogContext logContext);

    void debug(String message, LogContext logContext);

    void error(String message, LogContext logContext);

    void error(String message, Throwable t, LogContext logContext);

    void warn(String message, LogContext logContext);

    void warn(String message, Throwable t, LogContext logContext);

    void trace(String message, LogContext logContext);

    void fatal(String message, LogContext logContext);

    void clearLogContext();

}
