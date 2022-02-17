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
package io.entgra.analytics.mgt.grafana.proxy.core.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.entgra.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.DashboardNotFound;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.DatasourceNotFound;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.GrafanaEnvVariablesNotDefined;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.PanelNotFound;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.QueryNotFound;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.TemplateNotFound;
import io.entgra.analytics.mgt.grafana.proxy.core.service.GrafanaAPIService;
import io.entgra.analytics.mgt.grafana.proxy.core.service.bean.Datasource;
import io.entgra.analytics.mgt.grafana.proxy.core.service.cache.CacheManager;
import io.entgra.analytics.mgt.grafana.proxy.core.service.cache.QueryTemplateCacheKey;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaConstants;
import io.entgra.analytics.mgt.grafana.proxy.core.util.GrafanaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

public class GrafanaAPIServiceImpl implements GrafanaAPIService {

    private static final Log log = LogFactory.getLog(GrafanaAPIServiceImpl.class);

    public String getQueryTemplate(String dashboardUID, String panelId, String refId,
                                   String requestScheme) throws IOException, GrafanaManagementException {
        try {
            return getPanelQuery(dashboardUID, panelId, refId, requestScheme);
        } catch (QueryNotFound e) {
            return  getTemplateQuery(dashboardUID, refId, requestScheme);
        }
    }

    /**
     * Get predefined query template from grafana for given panel
     * @param dashboardUID
     * @param panelId
     * @param refId
     * @param requestScheme
     * @return query of the given panel
     * @throws IOException
     * @throws GrafanaManagementException
     */
    public String getPanelQuery(String dashboardUID, String panelId, String refId,
                                   String requestScheme) throws IOException, GrafanaManagementException {
        JsonObject panel = getPanelDetails(dashboardUID, panelId, requestScheme);
        JsonArray queries = panel.getAsJsonArray(GrafanaConstants.DASHBOARD_PANEL_DETAIL_QUERIES_KEY);
        JsonObject query = getQueryByRefId(queries, refId);
        if (query == null) {
            throw new QueryNotFound("No query exists for {dashboard: " + dashboardUID +
                    ", panelId: " + panelId + ", refId: " +  refId + "}");
        }
        String queryTemplate =  query.get(GrafanaConstants.RAW_SQL_KEY).getAsString();
        CacheManager.getInstance().getQueryTemplateAPICache().
                put(new QueryTemplateCacheKey(dashboardUID, panelId, refId), queryTemplate);
        return queryTemplate;
    }

    /**
     * Get predefined query template from grafana for given variable (template variable)
     * Note: Here template query means the grafana template variable queries
     * @param dashboardUID
     * @param refId
     * @param requestScheme
     * @return query of grafana template variable
     * @throws IOException
     * @throws GrafanaManagementException
     */
    // Here template query means the grafana template variable queries
    public String getTemplateQuery(String dashboardUID, String refId,
                                   String requestScheme) throws IOException, GrafanaManagementException {
        JsonObject template = getTemplatingDetails(dashboardUID, refId, requestScheme);
        JsonElement queryElement = template.get(GrafanaConstants.TEMPLATE_QUERY_KEY);
        if (queryElement == null) {
            throw new QueryNotFound("No template query exists for {dashboard: " + dashboardUID +
                    ", refId: " +  refId + "}");
        }
        String query =  queryElement.getAsString();
        CacheManager.getInstance().getQueryTemplateAPICache().
                put(new QueryTemplateCacheKey(dashboardUID, null, refId), query);
        return query;
    }

    /**
     * Get grafana template variable details of given dashboard uid
     * @param dashboardUID
     * @param refId
     * @param requestScheme
     * @return Template variable details of given dashboard uid
     * @throws IOException
     * @throws GrafanaManagementException
     */
    public JsonObject getTemplatingDetails(String dashboardUID, String refId, String requestScheme) throws
            IOException, GrafanaManagementException {
        JsonObject dashboard = getDashboardDetails(dashboardUID, requestScheme);
        JsonObject template = getTemplateByRefId(dashboard, refId);
        if (template == null) {
            String errorMsg = "Template for {refId: " + refId + ", dashboard: " + dashboardUID + "} is not found";
            log.error(errorMsg);
            throw new TemplateNotFound(errorMsg);
        }
        return template;
    }

    public JsonObject getPanelDetails(String dashboardUID, String panelId, String requestScheme) throws
            IOException, GrafanaManagementException {
        JsonObject dashboard = getDashboardDetails(dashboardUID, requestScheme);
        JsonObject panel = getPanelById(dashboard, panelId);
        if (panel == null) {
            String errorMsg = "Panel " + panelId + " for dashboard " + dashboardUID + " is not found";
            log.error(errorMsg);
            throw new PanelNotFound(errorMsg);
        }
        return panel;
    }

    public JsonObject getDashboardDetails(String dashboardUID, String requestScheme) throws IOException,
            GrafanaManagementException {
        String dashboardAPI = generateGrafanaAPIBaseUri(GrafanaConstants.DASHBOARD_API, requestScheme) + dashboardUID;
        HttpGet request = new HttpGet(dashboardAPI);
        JsonObject dashboardResponseJsonBody = getGrafanaAPIJsonResponse(request);
        JsonObject dashboardDetails = dashboardResponseJsonBody.getAsJsonObject(GrafanaConstants.DASHBOARD_KEY);
        if (dashboardDetails == null) {
            throw new DashboardNotFound("Grafana response: " + dashboardResponseJsonBody);
        }
        return dashboardResponseJsonBody.getAsJsonObject(GrafanaConstants.DASHBOARD_KEY);
    }

    public Datasource getDatasource(int datasourceId, String requestScheme) throws IOException, GrafanaManagementException {
        String datasourceAPI = generateGrafanaAPIBaseUri(GrafanaConstants.DATASOURCE_API, requestScheme) + datasourceId;
        HttpGet request = new HttpGet(datasourceAPI);
        JsonObject datasourceDetails  = getGrafanaAPIJsonResponse(request);
        if (datasourceDetails.get(GrafanaConstants.DATASOURCE_NAME_KEY) == null) {
            throw new DatasourceNotFound("Grafana response: " + datasourceDetails);
        }
        String url = datasourceDetails.get(GrafanaConstants.DATASOURCE_URL_KEY).getAsString();
        String name = datasourceDetails.get(GrafanaConstants.DATASOURCE_NAME_KEY).getAsString();
        String type = datasourceDetails.get(GrafanaConstants.DATASOURCE_TYPE_KEY).getAsString();
        String database = datasourceDetails.get(GrafanaConstants.DATASOURCE_DB_KEY).getAsString();
        Datasource ds = new Datasource(datasourceId, url, name, type, database);
        CacheManager.getInstance().getDatasourceAPICache().put(datasourceId, ds);
        return ds;
    }

    private String generateGrafanaAPIBaseUri(String api, String requestScheme) throws GrafanaEnvVariablesNotDefined {
        return GrafanaUtil.getGrafanaHTTPBase(requestScheme) + api + Constants.URI_SEPARATOR;
    }


    private JsonObject getGrafanaAPIJsonResponse(HttpRequestBase request) throws IOException,
            GrafanaManagementException {
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = invokeAPI(client, request);
            return GrafanaUtil.getJsonBody(HttpUtil.getResponseString(response));
        }
    }

    private HttpResponse invokeAPI(HttpClient client, HttpRequestBase request) throws IOException, GrafanaManagementException {
        setBasicAuthHeader(request);
        return client.execute(request);
    }

    private void setBasicAuthHeader(HttpRequestBase request) throws GrafanaManagementException {
        String basicAuth = GrafanaUtil.getBasicAuthBase64Header();
        request.setHeader(HttpHeaders.AUTHORIZATION, basicAuth);
    }

    private JsonObject getQueryByRefId(JsonArray queries, String refId) {
        for (int i = 0; i < queries.size(); i++) {
            JsonObject query = queries.get(i).getAsJsonObject();
            String queryRefId = query.get(GrafanaConstants.QUERY_REF_ID_KEY).getAsString();
            if (queryRefId.equals(refId)) {
                return query;
            }
        }
        return null;
    }

    private JsonObject getTemplateByRefId(JsonObject dashboard, String refId) {
        JsonArray templates = dashboard.getAsJsonObject(GrafanaConstants.TEMPLATING_KEY).
                getAsJsonArray(GrafanaConstants.TEMPLATING_LIST_KEY);
        for (int i = 0; i < templates.size(); i++) {
            JsonObject templateObj = templates.get(i).getAsJsonObject();
            String name = templateObj.get(GrafanaConstants.TEMPLATING_NAME_KEY).getAsString();
            // RefId in query body corresponds to template name
            if (refId.equals(name)) {
                return templateObj;
            }
        }
        return null;
    }

    private JsonObject getPanelById(JsonObject dashboard, String panelId) {
        JsonArray panels = dashboard.getAsJsonArray(GrafanaConstants.PANEL_KEY);
        for (int i = 0; i < panels.size(); i++) {
            JsonObject panelObj = panels.get(i).getAsJsonObject();
            String id = panelObj.get(GrafanaConstants.ID_KEY).getAsString();
            if (id.equals(panelId)) {
                return panelObj;
            }
        }
        return null;
    }
}
