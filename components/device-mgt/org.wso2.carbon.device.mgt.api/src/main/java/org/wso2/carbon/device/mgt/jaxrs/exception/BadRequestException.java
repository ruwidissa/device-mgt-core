/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.jaxrs.exception;

import org.wso2.carbon.device.mgt.jaxrs.beans.ErrorResponse;
import org.wso2.carbon.device.mgt.jaxrs.util.Constants;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Custom exception class for wrapping BadRequest related exceptions.
 */
public class BadRequestException extends WebApplicationException {
    private String message;
    private static final long serialVersionUID = -24991723711391192L;

    public BadRequestException(ErrorResponse error) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }

    public BadRequestException(ErrorDTO errorDTO) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                      .entity(errorDTO)
                      .header(Constants.DeviceConstants.HEADER_CONTENT_TYPE, Constants.DeviceConstants.APPLICATION_JSON)
                      .build());
        message = errorDTO.getMessage();
    }

    public BadRequestException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
