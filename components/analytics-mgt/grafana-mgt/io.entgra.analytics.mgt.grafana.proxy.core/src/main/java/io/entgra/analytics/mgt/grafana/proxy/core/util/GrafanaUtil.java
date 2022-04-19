/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.analytics.mgt.grafana.proxy.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.entgra.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.analytics.mgt.grafana.proxy.core.bean.GrafanaPanelIdentifier;
import io.entgra.analytics.mgt.grafana.proxy.core.config.GrafanaConfiguration;
import io.entgra.analytics.mgt.grafana.proxy.core.config.GrafanaConfigurationManager;
import io.entgra.analytics.mgt.grafana.proxy.core.exception.GrafanaEnvVariablesNotDefined;
import io.entgra.analytics.mgt.grafana.proxy.core.service.GrafanaAPIService;
import io.entgra.analytics.mgt.grafana.proxy.core.service.GrafanaQueryService;
import io.entgra.analytics.mgt.grafana.proxy.core.service.impl.GrafanaAPIServiceImpl;
import io.entgra.analytics.mgt.grafana.proxy.core.service.impl.GrafanaQueryServiceImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.common.util.HttpUtil;
import org.wso2.carbon.device.mgt.core.report.mgt.Constants;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class GrafanaUtil {

    private static final Log log = LogFactory.getLog(GrafanaUtil.class);

    public static boolean isGrafanaAPI(String uri) {
        return uri.contains(GrafanaConstants.API_PATH);
    }

    public static String generateGrafanaUrl(String requestPath, String base) {
        base += Constants.URI_SEPARATOR;
        return base + requestPath;
    }
    public static String getGrafanaWebSocketBase(String requestScheme) throws GrafanaEnvVariablesNotDefined {
        return getGrafanaBase(requestScheme, Constants.WS_PROTOCOL, Constants.WSS_PROTOCOL);
    }
    public static String getGrafanaHTTPBase(String requestScheme) throws GrafanaEnvVariablesNotDefined {
        return getGrafanaBase(requestScheme, Constants.HTTP_PROTOCOL, Constants.HTTPS_PROTOCOL);
    }

    public static String getGrafanaBase(String requestScheme, String httpProtocol, String httpsProtocol)
            throws GrafanaEnvVariablesNotDefined {
        String grafanaHost = System.getProperty(GrafanaConstants.HTTPS_HOST_ENV_VAR);
        String scheme = httpsProtocol;
        if (Constants.HTTP_PROTOCOL.equals(requestScheme) || StringUtils.isEmpty(grafanaHost)){
            grafanaHost = System.getProperty(GrafanaConstants.HTTP_HOST_ENV_VAR);
            scheme = httpProtocol;
        }
        if(StringUtils.isEmpty(grafanaHost)) {
            String errMsg = "Grafana host is not defined in the iot-server.sh properly.";
            log.error(errMsg);
            throw new GrafanaEnvVariablesNotDefined(errMsg);
        }
        return scheme + Constants.SCHEME_SEPARATOR + grafanaHost;
    }

    public static String getPanelId(URI iframeURL) {
        Map<String, List<String>> queryMap = HttpUtil.getQueryMap(iframeURL);
        return HttpUtil.getFirstQueryValue(GrafanaConstants.PANEL_ID_QUERY_PARAM, queryMap);
    }
    public static String getOrgId(URI iframeURL) {
        Map<String, List<String>> queryMap = HttpUtil.getQueryMap(iframeURL);
        return HttpUtil.getFirstQueryValue(GrafanaConstants.ORG_ID_QUERY_PARAM, queryMap);
    }

    public static String getDashboardUID(URI iframeURL) {
        return HttpUtil.extractRequestSubPathFromEnd(iframeURL, GrafanaConstants.IFRAME_URL_DASHBOARD_UID_INDEX);
    }

    public static JsonObject getJsonBody(String body) {
        return new Gson().fromJson(body, JsonObject.class);
    }

    public static String getBasicAuthBase64Header() throws GrafanaManagementException {
        GrafanaConfiguration configuration = GrafanaConfigurationManager.getInstance().getGrafanaConfiguration();
        String username = configuration.getAdminUser().getUsername();
        String password = configuration.getAdminUser().getPassword();
        return Constants.BASIC_AUTH_HEADER_PREFIX + GrafanaUtil.getBase64Encode(username, password);
    }

    public static String getBase64Encode(String key, String value) {
        return new String(Base64.encodeBase64((key + ":" + value).getBytes()));
    }

    public static GrafanaPanelIdentifier getPanelIdentifierFromReferer(String referer) {
        URI refererUri = HttpUtil.createURI(referer);
        String orgId = GrafanaUtil.getOrgId(refererUri);
        String dashboardUID = GrafanaUtil.getDashboardUID(refererUri);
        String panelId = GrafanaUtil.getPanelId(refererUri);
        return new GrafanaPanelIdentifier(orgId, dashboardUID, panelId);
    }

    public static int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
    }
}
