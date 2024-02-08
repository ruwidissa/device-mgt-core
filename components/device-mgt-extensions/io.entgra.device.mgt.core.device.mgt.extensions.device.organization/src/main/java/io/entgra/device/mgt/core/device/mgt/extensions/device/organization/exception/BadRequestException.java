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

package io.entgra.device.mgt.core.device.mgt.extensions.device.organization.exception;

/**
 * Represents an exception thrown during the validation of a request in the Device Organization
 * Management Plugin. This exception is typically thrown when the request parameters or
 * data are invalid or do not meet the required criteria.
 */
public class BadRequestException extends DeviceOrganizationMgtPluginException {
    private static final long serialVersionUID = -2036794959420530981L;

    public BadRequestException(String message, Throwable ex) {
        super(message, ex);
    }

    public BadRequestException(String message) {
        super(message);
    }
}
