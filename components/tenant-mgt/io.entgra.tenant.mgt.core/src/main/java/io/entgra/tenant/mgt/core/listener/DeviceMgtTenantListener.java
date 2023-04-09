package io.entgra.tenant.mgt.core.listener;

import io.entgra.tenant.mgt.core.TenantManager;
import io.entgra.tenant.mgt.common.exception.TenantMgtException;
import io.entgra.tenant.mgt.core.internal.TenantMgtDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.exception.StratosException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

public class DeviceMgtTenantListener implements TenantMgtListener {

    private static final Log log = LogFactory.getLog(DeviceMgtTenantListener.class);
    public static final int LISTENER_EXECUTION_ORDER = 11;

    @Override
    public void onTenantCreate(TenantInfoBean tenantInfoBean) {
        // Any work to be performed after a tenant creation
        TenantManager tenantManager = TenantMgtDataHolder.getInstance().getTenantManager();
        try {
            tenantManager.addDefaultRoles(tenantInfoBean);
            tenantManager.addDefaultAppCategories(tenantInfoBean);
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
    public void onTenantInitialActivation(int i) throws StratosException {
        // Any work to be performed after a tenant's initial activation happens
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
