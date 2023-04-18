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
package io.entgra.tenant.mgt.core.impl;

import io.entgra.tenant.mgt.common.spi.TenantManagerService;
import io.entgra.tenant.mgt.core.TenantManager;
import io.entgra.tenant.mgt.common.exception.TenantMgtException;
import io.entgra.tenant.mgt.core.internal.TenantMgtDataHolder;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;

public class TenantManagerServiceImpl implements TenantManagerService {

    private final TenantManager tenantManager;

    public TenantManagerServiceImpl() {
        tenantManager = new TenantManagerImpl();
        TenantMgtDataHolder.getInstance().setTenantManager(tenantManager);
    }

    @Override
    public void addDefaultRoles(TenantInfoBean tenantInfoBean) throws TenantMgtException {
        tenantManager.addDefaultRoles(tenantInfoBean);
    }

    @Override
    public void addDefaultAppCategories(TenantInfoBean tenantInfoBean) throws TenantMgtException {
        tenantManager.addDefaultAppCategories(tenantInfoBean);
    }
}
