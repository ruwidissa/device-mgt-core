/*
 *  Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *  Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.device.mgt.common.exceptions;

public class BadRequestException extends Exception {
    private static final long serialVersionUID = 2304023531260840549L;

    public BadRequestException() {
        super();
    }

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public BadRequestException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

