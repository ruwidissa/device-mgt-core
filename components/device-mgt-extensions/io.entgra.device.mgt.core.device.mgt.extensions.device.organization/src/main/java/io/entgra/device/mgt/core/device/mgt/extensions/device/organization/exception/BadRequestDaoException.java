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
 * This exception is thrown when a bad request is encountered in the Device Organization Management DAO layer.
 * It typically indicates issues with the input parameters or data during DAO operations.
 */
public class BadRequestDaoException extends DeviceOrganizationMgtDAOException {

    private static final long serialVersionUID = -6275360486437601206L;

    public BadRequestDaoException(String message) {
        super(message);
    }

    public BadRequestDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
