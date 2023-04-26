/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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

package io.entgra.device.mgt.subtype.mgt.exception;

/**
 * Represents the exception thrown during validating the request.
 */
public class BadRequestException extends SubTypeMgtPluginException {

    private static final long serialVersionUID = 4082332498085984791L;

    public BadRequestException(String message, Throwable ex) {
        super(message, ex);
    }

    public BadRequestException(String message) {
        super(message);
    }
}
