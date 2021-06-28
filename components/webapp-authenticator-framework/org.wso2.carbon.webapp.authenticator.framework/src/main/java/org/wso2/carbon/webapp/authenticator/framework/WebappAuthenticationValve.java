/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.webapp.authenticator.framework;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;
import org.wso2.carbon.webapp.authenticator.framework.authorizer.PermissionAuthorizer;
import org.wso2.carbon.webapp.authenticator.framework.authorizer.WebappTenantAuthorizer;

import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class WebappAuthenticationValve extends CarbonTomcatValve {

    private static final Log log = LogFactory.getLog(WebappAuthenticationValve.class);
    private static final TreeMap<String, String> nonSecuredEndpoints = new TreeMap<>();
    private static InetAddress inetAddress = null;

    @Override
    public void invoke(Request request, Response response, CompositeValve compositeValve) {
        if (response != null) {
            if (inetAddress == null) {
                try {
                    Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                    while (ifaces.hasMoreElements()) {
                        NetworkInterface iface = ifaces.nextElement();
                        if (!iface.isLoopback() && iface.isUp()) {
                            Enumeration<InetAddress> addresses = iface.getInetAddresses();
                            while (addresses.hasMoreElements()) {
                                inetAddress = addresses.nextElement();
                                break;
                            }
                        }
                        break;
                    }
                } catch (SocketException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Unable to get IP address of the node.", e);
                    }
                }
            }
            if (inetAddress != null) {
                response.setHeader("IoT-Node-IP", inetAddress.getHostAddress());
            }
        }

        if ((this.isContextSkipped(request) ||  this.skipAuthentication(request))) {
                this.getNext().invoke(request, response, compositeValve);
            return;
        }

        WebappAuthenticator authenticator = WebappAuthenticatorFactory.getAuthenticator(request);
        if (authenticator == null) {
            String msg = "Failed to load an appropriate authenticator to authenticate the request";
            AuthenticationFrameworkUtil.handleResponse(request, response, HttpServletResponse.SC_UNAUTHORIZED, msg);
            return;
        }
        AuthenticationInfo authenticationInfo = authenticator.authenticate(request, response);
        if (isManagedAPI(request) && (authenticationInfo.getStatus() == WebappAuthenticator.Status.CONTINUE ||
                authenticationInfo.getStatus() == WebappAuthenticator.Status.SUCCESS)) {
            WebappAuthenticator.Status status = WebappTenantAuthorizer.authorize(request, authenticationInfo);
            authenticationInfo.setStatus(status);
        }

        // This section will allow to validate a given access token is authenticated to access given
        // resource(permission)
        if (request.getCoyoteRequest() != null
                && (authenticationInfo.getStatus() == WebappAuthenticator.Status.CONTINUE ||
                authenticationInfo.getStatus() == WebappAuthenticator.Status.SUCCESS)) {
            boolean isAllowed;
            WebappAuthenticator.Status authorizeStatus = PermissionAuthorizer.authorize(request, authenticationInfo);
            isAllowed = WebappAuthenticator.Status.SUCCESS == authorizeStatus;
            if (!isAllowed) {
                log.error("Unauthorized message from user " + authenticationInfo.getUsername());
                AuthenticationFrameworkUtil.handleResponse(request, response,
                        HttpServletResponse.SC_FORBIDDEN, "Unauthorized to access the API");
                return;
            }
        }

            Tenant tenant = null;
        if (authenticationInfo.getTenantId() != -1) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                privilegedCarbonContext.setTenantId(authenticationInfo.getTenantId());
                privilegedCarbonContext.setTenantDomain(authenticationInfo.getTenantDomain());
                privilegedCarbonContext.setUsername(authenticationInfo.getUsername());
                if (authenticationInfo.isSuperTenantAdmin() && request.getHeader(Constants
                        .PROXY_TENANT_ID) != null) {
                    // If this is a call from super admin to an API and the ProxyTenantId is also
                    // present, this is a call that is made with super admin credentials to call
                    // an API on behalf of another tenant. Hence the actual tenants, details are
                    // resolved instead of calling processRequest.
                    int tenantId = Integer.valueOf(request.getHeader(Constants.PROXY_TENANT_ID));
                    RealmService realmService = (RealmService) PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
                    if (realmService == null) {
                        String msg = "RealmService is not initialized";
                        log.error(msg);
                        AuthenticationFrameworkUtil.handleResponse(request, response,
                                HttpServletResponse.SC_BAD_REQUEST, msg);
                        return;
                    }
                    tenant = realmService.getTenantManager().getTenant(tenantId);
                } else {
                    this.processRequest(request, response, compositeValve, authenticationInfo);
                }
            } catch (UserStoreException e) {
                String msg = "Could not locate the tenant";
                log.error(msg);
                AuthenticationFrameworkUtil.handleResponse(request, response,
                            HttpServletResponse.SC_BAD_REQUEST, msg);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }

            // A call from super admin to a child tenant. Start a new tenant flow of the target
            // tenant and pass to the API.
            if (tenant != null) {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    privilegedCarbonContext.setTenantId(tenant.getId());
                    privilegedCarbonContext.setTenantDomain(tenant.getDomain());
                    privilegedCarbonContext.setUsername(tenant.getAdminName());
                    this.processRequest(request, response, compositeValve, authenticationInfo);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } else {
            this.processRequest(request, response, compositeValve, authenticationInfo);
        }
    }

    private boolean skipAuthentication(Request request) {
        String param = request.getContext().findParameter("doAuthentication");
        return (param == null || !Boolean.parseBoolean(param) || isNonSecuredEndPoint(request));
    }

    private boolean isManagedAPI(Request request) {
        String param = request.getContext().findParameter("managed-api-enabled");
        return (param != null && Boolean.parseBoolean(param));
    }

    private boolean isContextSkipped(Request request) {
        Context context = request.getContext();
        String ctx = context == null ? null :context.getPath();
        if (ctx == null || "".equals(ctx)) {
            ctx = request.getContextPath();
            if (ctx == null || "".equals(ctx)) {
                String requestUri = request.getRequestURI();
                if ("/".equals(requestUri)) {
                    return true;
                }
                StringTokenizer tokenizer = new StringTokenizer(request.getRequestURI(), "/");
                if (!tokenizer.hasMoreTokens()) {
                    return false;
                }
                ctx = tokenizer.nextToken();
            }
        }
        return ("carbon".equalsIgnoreCase(ctx) || "services".equalsIgnoreCase(ctx));
    }

    private boolean isNonSecuredEndPoint(Request request) {
        if (request.getCoyoteRequest() != null && request.getCoyoteRequest().getMimeHeaders() !=
                null && request.getCoyoteRequest().getMimeHeaders().getValue(Constants
                .HTTPHeaders.HEADER_HTTP_AUTHORIZATION) != null) {
            //This is to handle the DEP behaviours of the same endpoint being non-secured in the
            // first call and then being secured in the second call which comes with the basic
            // auth header.
            return false;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            uri = "";
        }
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        String contextPath = request.getContextPath();
        //Check the contextPath in nonSecuredEndpoints. If so it means cache is not populated for this web-app.
        if (!nonSecuredEndpoints.containsKey(contextPath)) {
            String param = request.getContext().findParameter("nonSecuredEndPoints");
            String skippedEndPoint;
            boolean isUriUnsecured = false;
            if (param != null && !param.isEmpty()) {
                //Add the nonSecured end-points to cache
                StringTokenizer tokenizer = new StringTokenizer(param, ",");
                nonSecuredEndpoints.put(contextPath, "true");
                while (tokenizer.hasMoreTokens()) {
                    skippedEndPoint = tokenizer.nextToken();
                    skippedEndPoint = skippedEndPoint.replace("\n", "").replace("\r", "").trim();
                    if (!skippedEndPoint.endsWith("/")) {
                        skippedEndPoint = skippedEndPoint + "/";
                    }
                    nonSecuredEndpoints.put(skippedEndPoint, "true");
                    if (uri.equals(skippedEndPoint) || Pattern.matches(skippedEndPoint, uri)){
                        isUriUnsecured = true;
                    }
                }
                return isUriUnsecured;
            }
        } else {
            if (nonSecuredEndpoints.containsKey(uri)) {
                return true;
            }
            for (String endpoint : nonSecuredEndpoints.keySet()) {
                if (Pattern.matches(endpoint, uri)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void processRequest(Request request, Response response, CompositeValve compositeValve,
                                AuthenticationInfo authenticationInfo) {
        switch (authenticationInfo.getStatus()) {
        case SUCCESS:
        case CONTINUE:
            this.getNext().invoke(request, response, compositeValve);
            break;
        case FAILURE:
            String msg = "Failed to authorize incoming request";
            if (authenticationInfo.getMessage() != null && !authenticationInfo.getMessage().isEmpty()) {
                msg = authenticationInfo.getMessage();
                response.setHeader("WWW-Authenticate", "Basic");
            }

            if (log.isDebugEnabled()) {
                log.debug(msg + " , API : " + Encode.forUriComponent(request.getRequestURI()));
            }
            AuthenticationFrameworkUtil.handleResponse(request, response, HttpServletResponse.SC_UNAUTHORIZED, msg);
            break;
        }
    }
}