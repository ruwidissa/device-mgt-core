/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.webapp.authenticator.framework.authorizer;

import org.apache.catalina.connector.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionManagerServiceImpl;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationException;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationFrameworkUtil;
import org.wso2.carbon.webapp.authenticator.framework.AuthenticationInfo;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class PermissionAuthorizer {

    private static final Log log = LogFactory.getLog(PermissionAuthorizer.class);

    public static WebappAuthenticator.Status authorize(Request request, AuthenticationInfo authenticationInfo) {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();
        String context = request.getContextPath();

        if (requestUri == null || requestUri.isEmpty() || requestMethod == null || requestMethod.isEmpty()) {
            return WebappAuthenticator.Status.CONTINUE;
        }

        PermissionManagerService registryBasedPermissionManager =
                PermissionManagerServiceImpl.getInstance();
        List<Permission> matchingPermissions = null;
        try {
            matchingPermissions = registryBasedPermissionManager.getPermission(context);
        } catch (PermissionManagementException e) {
            log.error(
                    "Error occurred while fetching the permission for URI : " + requestUri +
                            ", msg = " + e.getMessage());
        }

        if (matchingPermissions == null) {
            if (log.isDebugEnabled()) {
                log.debug("Permission to request '" + requestUri + "' is not defined in the configuration");
            }
            return WebappAuthenticator.Status.FAILURE;
        }

        String requiredPermission = null;
        List<MatchingResource> matchingResources = new ArrayList<>();
        for (Permission permission : matchingPermissions) {
            if (requestMethod.equals(permission.getMethod()) && requestUri.matches(permission.getUrlPattern())) {
                if (requestUri.equals(permission.getUrl())) { // is there a exact match
                    requiredPermission = permission.getPath();
                    break;
                } else { // all templated urls add to a list
                    matchingResources.add(new MatchingResource(permission.getUrlPattern().replace(context, ""), permission.getPath()));
                }
            }
        }

        if (requiredPermission == null) {
            if (matchingResources.size() == 1) { // only 1 templated url found
                requiredPermission = matchingResources.get(0).getPermission();
            }

            if (matchingResources.size() > 1) { // more than 1 templated urls found
                String urlWithoutContext = requestUri.replace(context, "");
                StringTokenizer st = new StringTokenizer(urlWithoutContext, "/");
                int tokenPosition = 1;
                while (st.hasMoreTokens()) {
                    List<MatchingResource> tempList = new ArrayList<>();
                    String currentToken = st.nextToken();
                    for (MatchingResource matchingResource : matchingResources) {
                        StringTokenizer stmr = new StringTokenizer(matchingResource.getUrlPattern(), "/");
                        int internalTokenPosition = 1;
                        while (stmr.hasMoreTokens()) {
                            String internalToken = stmr.nextToken();
                            if ((tokenPosition == internalTokenPosition)  && currentToken.equals(internalToken)) {
                                tempList.add(matchingResource);
                            }
                            internalTokenPosition++;
                            if (tokenPosition < internalTokenPosition) {
                                break;
                            }
                        }
                    }
                    if (tempList.size() == 1) {
                        requiredPermission = tempList.get(0).getPermission();
                        break;
                    }
                    tokenPosition++;
                }
            }
        }

        if (requiredPermission == null) {
            if (log.isDebugEnabled()) {
                log.debug("Matching permission not found for " + requestUri);
            }
            return WebappAuthenticator.Status.FAILURE;
        }

        boolean isUserAuthorized;
        try {
                isUserAuthorized = AuthenticationFrameworkUtil.isUserAuthorized(
                        authenticationInfo.getTenantId(), authenticationInfo.getTenantDomain(),
                        authenticationInfo.getUsername(), requiredPermission);
        } catch (AuthenticationException e) {
            log.error("Error occurred while retrieving user store. " + e.getMessage());
            return WebappAuthenticator.Status.FAILURE;
        }

        if (isUserAuthorized) {
            return WebappAuthenticator.Status.SUCCESS;
        } else {
            return WebappAuthenticator.Status.FAILURE;
        }

    }

}



