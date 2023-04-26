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

public class SubTypeMgtPluginException extends Exception {

    private static final long serialVersionUID = -8034442883708462443L;
    private String errorMessage;

    public SubTypeMgtPluginException() {
        super();
    }

    public SubTypeMgtPluginException(Throwable cause) {
        super(cause);
    }

    public SubTypeMgtPluginException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public SubTypeMgtPluginException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public SubTypeMgtPluginException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
