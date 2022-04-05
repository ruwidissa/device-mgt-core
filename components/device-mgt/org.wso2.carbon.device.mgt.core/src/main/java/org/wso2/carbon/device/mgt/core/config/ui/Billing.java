package org.wso2.carbon.device.mgt.core.config.ui;

import javax.xml.bind.annotation.XmlElement;

public class Billing {
    private boolean isHideBillGenerationInSuperTenant;
    private boolean isHideBillGenerationInSubTenant;
    private boolean isHideTotalCalculationInSuperTenant;
    private boolean isHideTotalCalculationInSubTenant;
    private boolean isHideDomainSelectionInSuperTenant;
    private boolean isHideDomainSelectionInSubTenant;

    @XmlElement(name = "HideDomainSelectionInSuperTenant")
    public boolean isHideDomainSelectionInSuperTenant() {
        return isHideDomainSelectionInSuperTenant;
    }

    public void setHideDomainSelectionInSuperTenant(boolean hideDomainSelectionInSuperTenant) {
        isHideDomainSelectionInSuperTenant = hideDomainSelectionInSuperTenant;
    }

    @XmlElement(name = "HideDomainSelectionInSubTenant")
    public boolean isHideDomainSelectionInSubTenant() {
        return isHideDomainSelectionInSubTenant;
    }

    public void setHideDomainSelectionInSubTenant(boolean hideDomainSelectionInSubTenant) {
        isHideDomainSelectionInSubTenant = hideDomainSelectionInSubTenant;
    }

    @XmlElement(name = "HideBillGenerationInSuperTenant")
    public boolean isHideBillGenerationInSuperTenant() {
        return isHideBillGenerationInSuperTenant;
    }

    public void setHideBillGenerationInSuperTenant(boolean hideBillGenerationInSuperTenant) {
        isHideBillGenerationInSuperTenant = hideBillGenerationInSuperTenant;
    }

    @XmlElement(name = "HideBillGenerationInSubTenant")
    public boolean isHideBillGenerationInSubTenant() {
        return isHideBillGenerationInSubTenant;
    }

    public void setHideBillGenerationInSubTenant(boolean hideBillGenerationInSubTenant) {
        isHideBillGenerationInSubTenant = hideBillGenerationInSubTenant;
    }

    @XmlElement(name = "HideTotalCalculationInSuperTenant")
    public boolean isHideTotalCalculationInSuperTenant() {
        return isHideTotalCalculationInSuperTenant;
    }

    public void setHideTotalCalculationInSuperTenant(boolean hideTotalCalculationInSuperTenant) {
        isHideTotalCalculationInSuperTenant = hideTotalCalculationInSuperTenant;
    }

    @XmlElement(name = "HideTotalCalculationInSubTenant")
    public boolean isHideTotalCalculationInSubTenant() {
        return isHideTotalCalculationInSubTenant;
    }

    public void setHideTotalCalculationInSubTenant(boolean hideTotalCalculationInSubTenant) {
        isHideTotalCalculationInSubTenant = hideTotalCalculationInSubTenant;
    }
}
