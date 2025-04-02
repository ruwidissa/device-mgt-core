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
package io.entgra.device.mgt.core.tenant.mgt.core.listener;

import io.entgra.device.mgt.core.tenant.mgt.common.exception.TenantMgtException;
import io.entgra.device.mgt.core.tenant.mgt.core.TenantManager;
import io.entgra.device.mgt.core.tenant.mgt.core.internal.TenantMgtDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

public class DeviceMgtTenantListener implements TenantMgtListener {

    private static final Log log = LogFactory.getLog(DeviceMgtTenantListener.class);
    public static final int LISTENER_EXECUTION_ORDER = 10;

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) {
        // Any work to be performed after a tenant creation
        TenantManager tenantManager = TenantMgtDataHolder.getInstance().getTenantManager();
        try {
            tenantManager.addDefaultRoles(tenantInfoBean);
            tenantManager.addDefaultAppCategories(tenantInfoBean);
            tenantManager.addDefaultDeviceStatusFilters(tenantInfoBean);
        } catch (TenantMgtException e) {
            String msg = "Error occurred while executing tenant creation flow";
            log.error(msg, e);
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {
        // Any work to be performed after a tenant information update happens
    }

    @Override
    public void onTenantDelete(int i) {
        // Any work to be performed after a tenant deletion
    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {
        // Any work to be performed after a tenant rename happens
    }

    @Override
    public void onTenantInitialActivation(int tenantId) throws StratosException {
        TenantManager tenantManager = TenantMgtDataHolder.getInstance().getTenantManager();
        String tenantDomain = null;
        try {
            tenantDomain = tenantManager.getTenantDomain(tenantId);
            tenantManager.publishScopesToTenant(tenantDomain);
        } catch (TenantMgtException e) {
            log.error("Error occurred while executing tenant initial activation flow", e);
        }
    }

    @Override
    public void onTenantActivation(int i) throws StratosException {
        // Any work to be performed after a tenant activation
    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {
        // Any work to be performed after a tenant deactivation
    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {
        // Any work to be performed after subscription plan change
    }

    @Override
    public int getListenerOrder() {
        return LISTENER_EXECUTION_ORDER;
    }

    @Override
    public void onPreDelete(int i) throws StratosException {
        // Any work to be performed before a tenant is deleted
    }
}
