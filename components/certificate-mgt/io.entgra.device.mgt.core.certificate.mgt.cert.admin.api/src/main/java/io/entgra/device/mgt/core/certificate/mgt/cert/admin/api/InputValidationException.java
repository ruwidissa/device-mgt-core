/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package io.entgra.device.mgt.core.certificate.mgt.cert.admin.api;

import io.entgra.device.mgt.core.certificate.mgt.cert.admin.api.beans.ErrorResponse;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.Serializable;

public class InputValidationException extends WebApplicationException implements Serializable {

    private static final long serialVersionUID = 147843589458906890L;

    public InputValidationException(ErrorResponse error) {
        super(Response.status(Response.Status.BAD_REQUEST).entity(error).build());
    }

}