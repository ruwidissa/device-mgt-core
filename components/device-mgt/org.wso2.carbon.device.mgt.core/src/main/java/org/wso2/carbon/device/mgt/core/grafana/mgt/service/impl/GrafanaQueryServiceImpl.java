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
package org.wso2.carbon.device.mgt.core.grafana.mgt.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.exceptions.GrafanaManagementException;
import org.wso2.carbon.device.mgt.core.grafana.mgt.exception.MaliciousQueryAttempt;
import org.wso2.carbon.device.mgt.core.grafana.mgt.exception.QueryMisMatch;
import org.wso2.carbon.device.mgt.core.grafana.mgt.exception.QueryNotFound;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.GrafanaAPIService;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.GrafanaQueryService;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.bean.Datasource;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.CacheManager;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.QueryTemplateCacheKey;
import org.wso2.carbon.device.mgt.core.grafana.mgt.sql.query.GrafanaPreparedQueryBuilder;
import org.wso2.carbon.device.mgt.core.grafana.mgt.sql.query.PreparedQuery;
import org.wso2.carbon.device.mgt.core.grafana.mgt.sql.query.encoder.QueryEncoderFactory;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaConstants;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

public class GrafanaQueryServiceImpl implements GrafanaQueryService {

    private static final Log log = LogFactory.getLog(GrafanaQueryServiceImpl.class);
    private final GrafanaAPIService grafanaAPIService;

    public GrafanaQueryServiceImpl(GrafanaAPIService grafanaAPIService) {
        this.grafanaAPIService = grafanaAPIService;
    }

    public void buildSafeQuery(JsonObject queryRequestBody, String dashboardUID, String panelId, URI requestUri)
            throws IOException, SQLException, GrafanaManagementException, DBConnectionException {
        JsonArray queries = queryRequestBody.getAsJsonArray(GrafanaConstants.QUERY_BODY_QUERIES_KEY);
        for (int i = 0; i < queries.size(); i++) {
            JsonObject queryObj = queries.get(i).getAsJsonObject();
            JsonElement refIdJson = queryObj.get(GrafanaConstants.QUERY_REF_ID_KEY);
            JsonElement rawSqlJson = queryObj.get(GrafanaConstants.RAW_SQL_KEY);
            JsonElement datasourceIdJson = queryObj.get(GrafanaConstants.DATASOURCE_ID_KEY);
            if (refIdJson == null || rawSqlJson == null || datasourceIdJson == null) {
                String errMsg = "Query json body: refId, rawSql and datasourceId cannot be null";
                log.error(errMsg);
                throw new MaliciousQueryAttempt(errMsg);
            }
            String refId = refIdJson.getAsString();
            String rawSql = rawSqlJson.getAsString();
            int datasourceId = datasourceIdJson.getAsInt();
            CacheManager cacheManager = CacheManager.getInstance();
            String encodedQuery = cacheManager.getEncodedQueryCache().getIfPresent(rawSql);
            if (cacheManager.getEncodedQueryCache().getIfPresent(rawSql) != null) {
                queryObj.addProperty(GrafanaConstants.RAW_SQL_KEY, encodedQuery);
                return;
            }
            Datasource datasource = cacheManager.getDatasourceAPICache().getIfPresent(datasourceId);
            if (datasource == null) {
                datasource = grafanaAPIService.getDatasource(datasourceId, requestUri.getScheme());
            }
            String queryTemplate = cacheManager.getQueryTemplateAPICache().
                    getIfPresent(new QueryTemplateCacheKey(dashboardUID, panelId, refId));
            try {
                if (queryTemplate != null) {
                    try {
                        encodeQuery(queryObj, datasource, queryTemplate, rawSql);
                    } catch (QueryMisMatch e) {
                        log.error("Error occurred while encoding query, " +
                                "retrying to encode by getting the query template from api instead of cache", e);
                        queryTemplate = grafanaAPIService.getQueryTemplate(dashboardUID, panelId, refId, requestUri.getScheme());
                        encodeQuery(queryObj, datasource, queryTemplate, rawSql);
                    }
                } else {
                    queryTemplate = grafanaAPIService.getQueryTemplate(dashboardUID, panelId, refId, requestUri.getScheme());
                    encodeQuery(queryObj, datasource, queryTemplate, rawSql);
                }
            } catch (QueryNotFound e) {
                String errMsg = "No query exists for {dashboard: " + dashboardUID +
                        ", panelId: " + panelId + ", refId: " +  refId + "}";
                log.error(errMsg);
                throw new QueryNotFound(errMsg);
            }
        }
    }

    private void encodeQuery(JsonObject queryObj, Datasource datasource, String queryTemplate, String rawSql)
            throws SQLException, GrafanaManagementException, DBConnectionException {
        PreparedQuery pq = GrafanaPreparedQueryBuilder.build(queryTemplate, rawSql);
        String encodedQuery = QueryEncoderFactory.createEncoder(datasource.getType(), datasource.getName()).encode(pq);
        CacheManager.getInstance().getEncodedQueryCache().put(rawSql, encodedQuery);
        if(log.isDebugEnabled()) {
            log.debug("Encoded query: " + encodedQuery);
        }
        queryObj.addProperty(GrafanaConstants.RAW_SQL_KEY, encodedQuery);
    }

}
