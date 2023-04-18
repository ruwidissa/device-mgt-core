/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.tenant.mgt.core.internal;

import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.tenant.mgt.core.TenantManager;
import org.wso2.carbon.device.mgt.common.metadata.mgt.WhiteLabelManagementService;
import org.wso2.carbon.user.core.service.RealmService;

public class TenantMgtDataHolder {
    private static final TenantMgtDataHolder instance = new TenantMgtDataHolder();
    private TenantManager tenantManager;

    private ApplicationManager applicationManager;

    private WhiteLabelManagementService whiteLabelManagementService;

    private RealmService realmService;

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public ApplicationManager getApplicationManager() {
        return applicationManager;
    }

    public void setApplicationManager(ApplicationManager applicationManager) {
        this.applicationManager = applicationManager;
    }

    public WhiteLabelManagementService getWhiteLabelManagementService() {
        return whiteLabelManagementService;
    }

    public void setWhiteLabelManagementService(WhiteLabelManagementService whiteLabelManagementService) {
        this.whiteLabelManagementService = whiteLabelManagementService;
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    public void setTenantManager(TenantManager tenantManager) {
        this.tenantManager = tenantManager;
    }

    public static TenantMgtDataHolder getInstance() {
        return instance;
    }
}
