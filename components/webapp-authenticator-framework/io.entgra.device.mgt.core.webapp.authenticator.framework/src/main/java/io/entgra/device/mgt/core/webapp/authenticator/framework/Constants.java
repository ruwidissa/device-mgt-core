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

package io.entgra.device.mgt.core.webapp.authenticator.framework;

public final class Constants {

    public static final String AUTHORIZATION_HEADER_PREFIX_BEARER = "Bearer";
    public static final String NO_MATCHING_AUTH_SCHEME = "noMatchedAuthScheme";
    public static final String PROXY_TENANT_ID = "Proxy-Tenant-Id";

    public static final class HTTPHeaders {
        private HTTPHeaders() {
            throw new AssertionError();
        }

        public static final String HEADER_HTTP_ACCEPT = "Accept";
        public static final String HEADER_HTTP_AUTHORIZATION = "Authorization";
        public static final String ONE_TIME_TOKEN_HEADER = "one-time-token";
        public static final String MUTUAL_AUTH_HEADER = "mutual-auth-header";
        public static final String PROXY_MUTUAL_AUTH_HEADER = "proxy-mutual-auth-header";
        public static final String CERTIFICATE_VERIFICATION_HEADER = "Mdm-Signature";
    }

    public static final class ContentTypes {
        private ContentTypes() {
            throw new AssertionError();
        }

        public static final String CONTENT_TYPE_ANY = "*/*";
        public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    }

    public static final class PermissionMethod {
        private PermissionMethod() {
            throw new AssertionError();
        }

        public static final String READ = "read";
        public static final String WRITE = "write";
        public static final String DELETE = "delete";
        public static final String ACTION = "action";
    }

    public static final class Certificate {
        private Certificate() {
            throw new AssertionError();
        }

        public static final String ORGANIZATION_ATTRIBUTE = "O=";
    }
}
