package io.entgra.device.mgt.core.apimgt.extension.rest.api.util.APIUtils;

public class AdvertiseInfo {

    private boolean advertised;
    private String originalDevPortalUrl;
    private String apiOwner;
    private String vendor;

    public boolean isAdvertised() {
        return advertised;
    }

    public void setAdvertised(boolean advertised) {
        this.advertised = advertised;
    }

    public String getOriginalDevPortalUrl() {
        return originalDevPortalUrl;
    }

    public void setOriginalDevPortalUrl(String originalDevPortalUrl) {
        this.originalDevPortalUrl = originalDevPortalUrl;
    }

    public String getApiOwner() {
        return apiOwner;
    }

    public void setApiOwner(String apiOwner) {
        this.apiOwner = apiOwner;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
