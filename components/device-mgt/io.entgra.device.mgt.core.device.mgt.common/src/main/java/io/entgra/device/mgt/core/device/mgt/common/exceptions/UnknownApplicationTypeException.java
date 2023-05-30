/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.common.exceptions;

public class UnknownApplicationTypeException extends Exception {

    private static final long serialVersionUID = -3151279311929080299L;

    public UnknownApplicationTypeException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public UnknownApplicationTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownApplicationTypeException(String msg) {
        super(msg);
    }

    public UnknownApplicationTypeException() {
        super();
    }

    public UnknownApplicationTypeException(Throwable cause) {
        super(cause);
    }

}
