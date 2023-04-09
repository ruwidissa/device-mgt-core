package io.entgra.tenant.mgt.common.spi;

import io.entgra.tenant.mgt.common.exception.TenantMgtException;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;

public interface TenantManagerService {
    void addDefaultRoles(TenantInfoBean tenantInfoBean) throws TenantMgtException;

    void addDefaultAppCategories(TenantInfoBean tenantInfoBean) throws TenantMgtException;
}