/*
 *   Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *   Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */
package org.wso2.carbon.device.mgt.common.exceptions;

/**
 * This class is used for exception handling in report generating operations
 */
public class ReportManagementException extends Exception {

    private static final long serialVersionUID = -409298183404045217L;

    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ReportManagementException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public ReportManagementException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public ReportManagementException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public ReportManagementException() {
        super();
    }

    public ReportManagementException(Throwable cause) {
        super(cause);
    }
}
