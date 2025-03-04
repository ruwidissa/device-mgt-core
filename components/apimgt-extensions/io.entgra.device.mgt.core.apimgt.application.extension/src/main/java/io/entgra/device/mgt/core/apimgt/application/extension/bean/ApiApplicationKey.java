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

package io.entgra.device.mgt.core.apimgt.application.extension.bean;

import io.entgra.device.mgt.core.apimgt.application.extension.constants.ApiApplicationConstants;
import org.json.JSONObject;

/**
 * This holds api application consumer key and secret.
 */
public class ApiApplicationKey {
    private String client_id;
    private String client_secret;

    public ApiApplicationKey(String client_id, String client_secret) {
        this.client_id = client_id;
        this.client_secret = client_secret;
    }

    public ApiApplicationKey() {
    }

    public String getClientId() {
        return this.client_id;
    }

    public void setClientId(String client_id) {
        this.client_id = client_id;
    }

    public String getClientSecret() {
        return this.client_secret;
    }

    public void setClientSecret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put(ApiApplicationConstants.OAUTH_CLIENT_ID, this.getClientId());
        obj.put(ApiApplicationConstants.OAUTH_CLIENT_SECRET, this.getClientSecret());
        return obj.toString();
    }
}
