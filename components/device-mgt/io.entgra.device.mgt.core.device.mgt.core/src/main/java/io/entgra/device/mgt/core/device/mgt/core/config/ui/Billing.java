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

package io.entgra.device.mgt.core.device.mgt.core.config.ui;

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
