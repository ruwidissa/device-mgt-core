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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.exception;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.util.Constants;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Exception class that is corresponding to 401 Forbidden response
 */

public class ForbiddenException extends WebApplicationException {

    private String message;

    public ForbiddenException() {
        super(Response.status(Response.Status.FORBIDDEN)
                .build());
    }

    public ForbiddenException(ErrorDTO errorDTO) {
        super(Response.status(Response.Status.FORBIDDEN)
                .entity(errorDTO)
                .header(Constants.DeviceConstants.HEADER_CONTENT_TYPE, Constants.DeviceConstants.APPLICATION_JSON)
                .build());
        message = errorDTO.getDescription();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
