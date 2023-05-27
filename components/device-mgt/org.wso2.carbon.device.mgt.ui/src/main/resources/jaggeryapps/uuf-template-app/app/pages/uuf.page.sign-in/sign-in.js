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

function onRequest(context) {
    var authModuleConfigs = context.app.conf["authModule"];
    if (authModuleConfigs && (authModuleConfigs["enabled"].toString() == "true")) {
        // Auth module is enabled.
        if (context.user) {
            // User is already logged in.
            response.sendRedirect(context.app.context + "/");
            exit();
        } else {
            // User is not logged in.
            var ssoConfigs = authModuleConfigs["sso"];
            if (ssoConfigs && (ssoConfigs["enabled"].toString() == "true")) {
                // SSO is enabled in Auth module.
                var redirectUri = context.app.context + "/uuf/login";
                var queryString = request.getQueryString();
                if (queryString && (queryString.length > 0)) {
                    redirectUri = redirectUri + "?" + queryString;
                }
                response.sendRedirect(encodeURI(redirectUri));
                exit();
            } else {
                // Generic login process is enabled.
                return {
                    message: request.getParameter("error"),
                    referer: request.getParameter("referer")
                };
            }
        }
    }
}