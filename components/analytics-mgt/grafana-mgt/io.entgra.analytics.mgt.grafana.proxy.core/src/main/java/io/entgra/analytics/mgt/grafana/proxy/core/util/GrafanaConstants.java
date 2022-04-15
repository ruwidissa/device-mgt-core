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
package io.entgra.analytics.mgt.grafana.proxy.core.util;

public class GrafanaConstants {

    public static final String QUERY_API_CACHE_NAME = "queryAPICache";
    public static final String ENCODED_QUERY_CACHE_NAME = "encodedQueryCache";
    public static final String REFERER_HEADER = "Referer";
    public static final String CONFIG_XML_NAME = "grafana-config.xml";
    public static final String X_GRAFANA_ORG_ID_HEADER = "x-org-grafana-id";

    public static class XML {
        public static final String FEATURES_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
    }

    public static class DATASOURCE_TYPE {
        public static final String MYSQL = "mysql";
        public static final String POSTGRESQL = "postgresql";
        public static final String MICROSOFT_SQL_SERVER = "mssql";
        public static final String ORACLE = "oracle";
    }

    public static final int IFRAME_URL_DASHBOARD_UID_INDEX = 1;

    public static final String TENANT_ID_VAR_NAME = "tenantId";
    public static final String VAR_PREFIX = "$";
    public static final String TENANT_ID_VAR = VAR_PREFIX + TENANT_ID_VAR_NAME;
    public static final String QUERY_PARAM_VAR_PREFIX = "var-";
    public static final String TENANT_ID_VAR_QUERY_PARAM = QUERY_PARAM_VAR_PREFIX + TENANT_ID_VAR_NAME;
    public static final String API_PATH = "/api";
    public static final String DASHBOARD_KEY = "dashboard";
    public static final String DATASOURCE_TYPE_KEY = "type";
    public static final String DATASOURCE_DB_KEY = "database";
    public static final String DATASOURCE_ID_KEY = "datasourceId";
    public static final String DATASOURCE_NAME_KEY = "name";
    public static final String DATASOURCE_URL_KEY = "name";
    public static final String PANEL_KEY = "panels";
    public static final String TEMPLATING_KEY = "templating";
    public static final String TEMPLATING_NAME_KEY = "name";
    public static final String TEMPLATE_QUERY_KEY = "query";
    public static final String TEMPLATING_LIST_KEY = "list";
    public static final String RAW_SQL_KEY = "rawSql";
    public static final String QUERY_BODY_QUERIES_KEY = "queries";
    public static final String DASHBOARD_PANEL_DETAIL_QUERIES_KEY = "targets";
    public static final String QUERY_REF_ID_KEY = "refId";
    public static final String PANEL_ID_QUERY_PARAM = "panelId";
    public static final String ORG_ID_QUERY_PARAM = "orgId";
    public static final String ID_KEY = "id";

    public static final String WS_LIVE_API = "/api/live/ws";
    public static final String DASHBOARD_API = "/api/dashboards/uid";
    public static final String DATASOURCE_API = "/api/datasources";
    public static final String HTTP_HOST_ENV_VAR = "iot.grafana.http.host";
    public static final String HTTPS_HOST_ENV_VAR = "iot.grafana.https.host";
}
