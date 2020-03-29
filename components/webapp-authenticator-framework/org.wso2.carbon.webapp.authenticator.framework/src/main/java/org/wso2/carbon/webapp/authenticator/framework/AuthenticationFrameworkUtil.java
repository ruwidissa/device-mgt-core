/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.webapp.authenticator.framework;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.webapp.authenticator.framework.internal.AuthenticatorFrameworkDataHolder;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

public class AuthenticationFrameworkUtil {

    private static final Log log = LogFactory.getLog(AuthenticationFrameworkUtil.class);
    private static final String UI_EXECUTE = "ui.execute";

    static void handleResponse(Request request, Response response, int statusCode, String payload) {
        response.setStatus(statusCode);
        String targetResponseContentType =
                request.getHeader(Constants.HTTPHeaders.HEADER_HTTP_ACCEPT);
        if (targetResponseContentType != null && !"".equals(targetResponseContentType) &&
                !Constants.ContentTypes.CONTENT_TYPE_ANY.equals(targetResponseContentType)) {
            response.setContentType(targetResponseContentType);
        } else {
            response.setContentType(Constants.ContentTypes.CONTENT_TYPE_APPLICATION_XML);
        }
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(payload);
        } catch (IOException e) {
            log.error("Error occurred while sending faulty response back to the client", e);
        }
    }

    public static Document convertToDocument(File file) throws AuthenticatorFrameworkException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new AuthenticatorFrameworkException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

    static boolean isUserAuthorized(int tenantId, String tenantDomain, String username, String
            permission) throws
            AuthenticationException {
        boolean tenantFlowStarted = false;

        try{
            //If this is a tenant user
            if(tenantId != MultitenantConstants.SUPER_TENANT_ID){
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(username);
                tenantFlowStarted = true;
            }

            RealmService realmService = AuthenticatorFrameworkDataHolder.getInstance().getRealmService();
            if (realmService == null) {
                String msg = "RealmService is not initialized";
                log.error(msg);
                throw new AuthenticationException(msg);
            }
            UserRealm userRealm = realmService.getTenantUserRealm(tenantId);

            return userRealm.getAuthorizationManager()
                    .isUserAuthorized(MultitenantUtils
                            .getTenantAwareUsername(username), permission, UI_EXECUTE);

        } catch (UserStoreException e) {
            String msg = "Error while getting username";
            log.error(msg, e);
            throw new AuthenticationException(msg, e);
        }
        finally {
            if (tenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

}
