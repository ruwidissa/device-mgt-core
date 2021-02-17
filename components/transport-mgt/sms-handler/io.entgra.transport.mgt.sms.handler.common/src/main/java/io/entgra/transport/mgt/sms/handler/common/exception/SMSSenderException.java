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

package io.entgra.transport.mgt.sms.handler.common.exception;

/**
 * Exception that will be thrown during SMS Sender Management
 */
public class SMSSenderException extends Exception {

    private static final long serialVersionUID = 3570365211845277909L;

    public SMSSenderException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public SMSSenderException(String message, Throwable cause) {
        super(message, cause);
    }

    public SMSSenderException(String msg) {
        super(msg);
    }

    public SMSSenderException() {
        super();
    }

    public SMSSenderException(Throwable cause) {
        super(cause);
    }
}
