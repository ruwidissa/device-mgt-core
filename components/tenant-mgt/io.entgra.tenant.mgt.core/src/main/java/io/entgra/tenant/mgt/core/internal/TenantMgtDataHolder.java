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
