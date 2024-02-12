/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.application.mgt.common;

import java.util.Objects;

public class TransferLink {
    private static final String SCHEMA_SEPARATOR = "://";
    private static final String URL_SEPARATOR = "/";
    private static final String COLON = ":";
    private final String schema;
    private final String host;
    private final String port;
    private final String endpoint;
    private final String artifactHolderUUID;

    private TransferLink(String schema, String host, String port, String endpoint, String artifactHolderUUID) {
        this.schema = schema;
        this.host = host;
        this.port = port;
        this.endpoint = endpoint;
        this.artifactHolderUUID = artifactHolderUUID;
    }

    public String getDirectTransferLink() {
        return schema + SCHEMA_SEPARATOR + host + COLON + port + URL_SEPARATOR + endpoint + URL_SEPARATOR + artifactHolderUUID;
    }

    public String getRelativeTransferLink() {
        return endpoint + URL_SEPARATOR + artifactHolderUUID;
    }

    @Override
    public String toString() {
        return getDirectTransferLink();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferLink that = (TransferLink) o;
        return Objects.equals(schema, that.schema) && Objects.equals(host, that.host) && Objects.equals(port, that.port)
                && Objects.equals(endpoint, that.endpoint) && Objects.equals(artifactHolderUUID, that.artifactHolderUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, host, port, endpoint, artifactHolderUUID);
    }

    public static class TransferLinkBuilder {
        private static final String DEFAULT_SCHEMA = "https";
        private static final String ENDPOINT = "application-mgt-publisher/v1.0/applications/uploads";
        private static final String IOT_GW_HOST_ENV_VAR = "iot.gateway.host";
        private static final String IOT_GW_HTTPS_PORT_ENV_VAR = "iot.gateway.https.port";
        private static final String IOT_GW_HTTP_PORT_ENV_VAR = "iot.gateway.http.port";
        private String schema;
        private String endpoint;
        private final String artifactHolderUUID;

        public TransferLinkBuilder(String artifactHolderUUID) {
            this.schema = DEFAULT_SCHEMA;
            this.endpoint = ENDPOINT;
            this.artifactHolderUUID = artifactHolderUUID;
        }

        public TransferLinkBuilder withSchema(String schema) {
            this.schema = schema;
            return this;
        }

        public TransferLinkBuilder withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public TransferLink build() {
            return new TransferLink(this.schema, resolveHost(), resolvePort(), this.endpoint, this.artifactHolderUUID);
        }

        private String resolveHost() {
            return System.getProperty(IOT_GW_HOST_ENV_VAR);
        }

        private String resolvePort() {
            return Objects.equals(this.schema, DEFAULT_SCHEMA) ? System.getProperty(IOT_GW_HTTPS_PORT_ENV_VAR)
                    : System.getProperty(IOT_GW_HTTP_PORT_ENV_VAR);
        }
    }
}
