/*
 * Copyright (c) 2022, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymgt.extension.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.extension.DCRResponse;
import org.wso2.carbon.apimgt.keymgt.extension.TokenRequest;
import org.wso2.carbon.apimgt.keymgt.extension.TokenResponse;
import org.wso2.carbon.apimgt.keymgt.extension.exception.BadRequestException;
import org.wso2.carbon.apimgt.keymgt.extension.exception.KeyMgtException;
import org.wso2.carbon.apimgt.keymgt.extension.service.KeyMgtService;
import org.wso2.carbon.apimgt.keymgt.extension.service.KeyMgtServiceImpl;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class KeyManagerServiceImpl implements KeyManagerService {

    @Override
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/dynamic-client-registration")
    public Response dynamicClientRegistration(DCRRequest dcrRequest) {
        try {
            KeyMgtService keyMgtService = new KeyMgtServiceImpl();
            DCRResponse resp = keyMgtService.dynamicClientRegistration(dcrRequest.getClientName(), dcrRequest.getOwner(),
                    dcrRequest.getGrantTypes(), dcrRequest.getCallBackUrl(), dcrRequest.getTags(), dcrRequest.getIsSaasApp());
            return Response.status(Response.Status.CREATED).entity(resp).build();
        } catch (KeyMgtException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/token")
    public Response generateAccessToken(@FormParam("client_id") String clientId,
                                        @FormParam("client_secret") String clientSecret,
                                        @FormParam("refresh_token") String refreshToken,
                                        @FormParam("scope") String scope,
                                        @FormParam("grant_type") String grantType) {
        try {
            KeyMgtService keyMgtService = new KeyMgtServiceImpl();
            TokenResponse resp = keyMgtService.generateAccessToken(
                    new TokenRequest(clientId, clientSecret, refreshToken, scope, grantType));
            return Response.status(Response.Status.CREATED).entity(resp).build();
        } catch (KeyMgtException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
