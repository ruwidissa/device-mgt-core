/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.tenant.mgt.core.impl;

import io.entgra.device.mgt.core.tenant.mgt.common.exception.TenantMgtException;
import io.entgra.device.mgt.core.tenant.mgt.common.spi.TenantManagerAdminService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.user.api.UserStoreException;


public class TenantManagerAdminServiceImpl implements TenantManagerAdminService {

    private static final Log log = LogFactory.getLog(TenantManagerAdminServiceImpl.class);

    private static final TenantMgtAdminService tenantMgtAdminService = new TenantMgtAdminService();

    @Override
    public void deleteTenant(String tenantDomain) throws TenantMgtException {
        try {
            tenantMgtAdminService.deleteTenant(tenantDomain);
        } catch (StratosException | UserStoreException e) {
            String msg = "Error occurred while deleting tenant of domain: " + tenantDomain;
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        }
    }

    @Override
    public int getTenantId(String tenantDomain) throws TenantMgtException {
        try {
            return tenantMgtAdminService.getTenant(tenantDomain).getTenantId();
        } catch (Exception e){
            String msg = "Error occurred while getting tenant ID of domain: " + tenantDomain;
            log.error(msg, e);
            throw new TenantMgtException(msg, e);
        }
    }
}
