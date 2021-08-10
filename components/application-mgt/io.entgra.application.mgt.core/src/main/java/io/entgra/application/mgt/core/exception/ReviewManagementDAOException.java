/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.core.exception;

import io.entgra.application.mgt.common.exception.ReviewManagementException;

/**
 * Exception thrown during the Review Management DAO operations.
 */
public class ReviewManagementDAOException extends ReviewManagementException {

    public ReviewManagementDAOException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ReviewManagementDAOException(String message) {
        super(message, new Exception());
    }
}
