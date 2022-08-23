/*
 *
 *  * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *  *
 *  * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  * Version 2.0 (the "License"); you may not use this file except
 *  * in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

/**
 * Custom exception class for handling bad request exceptions.
 */
package org.wso2.carbon.apimgt.keymgt.extension.exception;

public class BadRequestException extends Exception {

    private static final long serialVersionUID = -2387103750774855056L;

    public BadRequestException(String errorMessage) {
        super(errorMessage);
    }
}
