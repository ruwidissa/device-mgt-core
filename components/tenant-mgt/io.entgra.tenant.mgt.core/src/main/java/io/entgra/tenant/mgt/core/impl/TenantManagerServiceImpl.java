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
