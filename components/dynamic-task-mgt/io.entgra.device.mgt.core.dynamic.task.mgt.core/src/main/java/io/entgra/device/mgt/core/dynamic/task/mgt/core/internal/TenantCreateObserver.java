/*
 *   Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.core.internal;

import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskScheduleException;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskManagementUtil;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskSchedulerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

public class TenantCreateObserver implements TenantMgtListener {
    public static final int LISTENER_EXECUTION_ORDER = 11;
    private static final Log log = LogFactory.getLog(TenantCreateObserver.class);

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) throws StratosException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantInfoBean.getTenantDomain(),
                    true);
            DynamicTaskManagementUtil.addConfigurableDefaultPlatformConfigurationEntryToTenant();
            DynamicTaskSchedulerUtil.scheduleDynamicTasks();
        } catch (DynamicTaskManagementException e) {
            String msg = "Failed to add default categorized dynamic task configurations.";
            log.error(msg);
            throw new IllegalStateException(msg, e);
        } catch (DynamicTaskScheduleException e) {
            String msg = "Failed to schedule categorized dynamic tasks for tenant [" + PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getTenantDomain() + "].";
            log.error(msg);
            throw new IllegalStateException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void onTenantUpdate(TenantInfoBean tenantInfoBean) throws StratosException {

    }

    @Override
    public void onTenantDelete(int i) {

    }

    @Override
    public void onTenantRename(int i, String s, String s1) throws StratosException {

    }

    @Override
    public void onTenantInitialActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantActivation(int i) throws StratosException {

    }

    @Override
    public void onTenantDeactivation(int i) throws StratosException {

    }

    @Override
    public void onSubscriptionPlanChange(int i, String s, String s1) throws StratosException {

    }

    @Override
    public int getListenerOrder() {
        return LISTENER_EXECUTION_ORDER;
    }

    @Override
    public void onPreDelete(int i) throws StratosException {

    }
}
