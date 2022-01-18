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
package org.wso2.carbon.device.mgt.core.grafana.mgt.bean;

public class GrafanaPanelIdentifier {

    private String orgId;
    private String dashboardUID;
    private String panelId;

    public GrafanaPanelIdentifier(String orgId, String dashboardUID, String panelId) {
        this.orgId = orgId;
        this.dashboardUID = dashboardUID;
        this.panelId = panelId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getDashboardId() {
        return dashboardUID;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardUID = dashboardId;
    }

    public String getPanelId() {
        return panelId;
    }

    public void setPanelId(String panelId) {
        this.panelId = panelId;
    }
}
