/*
 * Copyright (C) 2018 - 2021 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.extensions.stateengine.dto;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;

public class StateUpdateResult {

    private EnrolmentInfo.Status oldStatus;
    private EnrolmentInfo.Status newStatus;
    private Response response;
    private String message;
    private Exception exception;

    public EnrolmentInfo.Status getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(EnrolmentInfo.Status oldStatus) {
        this.oldStatus = oldStatus;
    }

    public EnrolmentInfo.Status getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(EnrolmentInfo.Status newStatus) {
        this.newStatus = newStatus;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public enum Response {
        OK, ILLEGAL, UNAUTHORIZED, REQUIRED_DATA_MISSING, EXCEPTION, NOT_FOUND
    }

}
