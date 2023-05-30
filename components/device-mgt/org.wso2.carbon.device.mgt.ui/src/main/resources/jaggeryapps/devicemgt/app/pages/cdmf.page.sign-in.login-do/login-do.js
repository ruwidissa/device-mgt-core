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
    var sessionDataKey = request.getParameter("sessionDataKey");
    var authFailure = request.getParameter("authFailure");

    //if sso enabled and sessionDataKey is empty redirect
    var ssoConfigs = authModuleConfigs["sso"];
    if (ssoConfigs && (ssoConfigs["enabled"].toString() == "true") && !sessionDataKey) {
        // SSO is enabled in Auth module.
        var redirectUri = context.app.context + "/uuf/login";
        var queryString = request.getQueryString();
        if (queryString && (queryString.length > 0)) {
            redirectUri = redirectUri + "?" + queryString;
        }
        response.sendRedirect(encodeURI(redirectUri));
        exit();
    }

    var viewModel = {};
    var loginActionUrl = context.app.context + "/uuf/login";
    if (sessionDataKey) {
        loginActionUrl = "/commonauth";
    }

    if (authFailure) {
        viewModel.message = "Login failed! Please recheck the username and password and try again.";
    }

    viewModel.sessionDataKey = sessionDataKey;
    viewModel.loginActionUrl = loginActionUrl;
    return viewModel;
}