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

package io.entgra.device.mgt.core.device.mgt.common;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;
import io.swagger.annotations.*;

@ApiModel(
        value = "Feature",
        description = "This class carries all information related to a device feature."
)
public class Feature implements Serializable {

    @ApiModelProperty(
            name = "id",
            value = "Feature Id.",
            required = true
    )
    private int id;

    @ApiModelProperty(
            name = "code",
            value = "The code of the feature. For example the code to lock a device  is DEVICE_LOCK.",
            required = true
    )
    private String code;

    @ApiModelProperty(
            name = "name",
            value = "A name that describes a feature.",
            required = true
    )
    private String name;

    @ApiModelProperty(
            name = "description",
            value = "Provides a description of the features.",
            required = true
    )
    private String description;

    @ApiModelProperty(
            name = "tooltip",
            value = "Provides a tooltip for the features.",
            required = false
    )
    private String tooltip;

    @ApiModelProperty(
            name = "type",
            value = "Type of the feature.",
            required = true
    )
    private String type;

    @ApiModelProperty(
            name = "hidden",
            value = "If the feature is hidden from the UI."
    )
    private boolean hidden;

    @ApiModelProperty(
            name = "deviceType",
            value = "Provide the device type for the respective feature.  Features allow you to perform operations " +
                    "on any device type, such as android, iOS or windows.",
            required = true
    )
    private String deviceType;
    
    @ApiModelProperty(
            name = "metadataEntries",
            value = "Properties related to features.",
            required = true
    )
    private List<MetadataEntry> metadataEntries;

    @ApiModelProperty(
            name = "confirmationTexts",
            value = "Disenroll delete confirmation modal texts.",
            required = false
    )
    private ConfirmationTexts confirmationTexts;

    @ApiModelProperty(
            name = "dangerZoneTooltipTexts",
            value = "Danger zone tooltip texts.",
            required = false
    )
    private DangerZoneTooltipTexts dangerZoneTooltipTexts;

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MetadataEntry> getMetadataEntries() {
        return metadataEntries;
    }

    public void setMetadataEntries(List<MetadataEntry> metadataEntries) {
        this.metadataEntries = metadataEntries;
    }

    @XmlElement
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }


    @XmlAttribute
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @XmlElement
    public ConfirmationTexts getConfirmationTexts() {
        return confirmationTexts;
    }

    public void setConfirmationTexts(ConfirmationTexts confirmationTexts) {
        this.confirmationTexts = confirmationTexts;
    }

    @XmlElement
    public DangerZoneTooltipTexts getDangerZoneTooltipTexts() {
        return dangerZoneTooltipTexts;
    }

    public void setDangerZoneTooltipTexts(DangerZoneTooltipTexts dangerZoneTooltipTexts) {
        this.dangerZoneTooltipTexts = dangerZoneTooltipTexts;
    }

    public static class MetadataEntry implements Serializable {

        private int id;
        private String name;
        private Object value;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    public static class ConfirmationTexts implements Serializable {
        private int id;
        private String deleteConfirmModalTitle;
        private String deleteConfirmModalText;
        private String deleteConfirmationTextDescribe;
        private String deleteConfirmationText;
        private String cancelText;
        private String confirmText;
        private String inputLabel;
        private String inputRequireMessage;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCancelText() {
            return cancelText;
        }

        public void setCancelText(String cancelText) {
            this.cancelText = cancelText;
        }

        public String getConfirmText() {
            return confirmText;
        }

        public void setConfirmText(String confirmText) {
            this.confirmText = confirmText;
        }

        public String getInputLabel() {
            return inputLabel;
        }

        public void setInputLabel(String inputLabel) {
            this.inputLabel = inputLabel;
        }

        public String getInputRequireMessage() {
            return inputRequireMessage;
        }

        public void setInputRequireMessage(String inputRequireMessage) {
            this.inputRequireMessage = inputRequireMessage;
        }

        public String getDeleteConfirmModalTitle() {
            return deleteConfirmModalTitle;
        }

        public void setDeleteConfirmModalTitle(String deleteConfirmModalTitle) {
            this.deleteConfirmModalTitle = deleteConfirmModalTitle;
        }

        public String getDeleteConfirmModalText() {
            return deleteConfirmModalText;
        }

        public void setDeleteConfirmModalText(String deleteConfirmModalText) {
            this.deleteConfirmModalText = deleteConfirmModalText;
        }

        public String getDeleteConfirmationTextDescribe() {
            return deleteConfirmationTextDescribe;
        }

        public void setDeleteConfirmationTextDescribe(String deleteConfirmationTextDescribe) {
            this.deleteConfirmationTextDescribe = deleteConfirmationTextDescribe;
        }

        public String getDeleteConfirmationText() {
            return deleteConfirmationText;
        }

        public void setDeleteConfirmationText(String deleteConfirmationText) {
            this.deleteConfirmationText = deleteConfirmationText;
        }
    }

    public static class DangerZoneTooltipTexts implements Serializable {
        private String toolTipTitle;
        private String toolTipPopConfirmText;
        private String confirmText;
        private String cancelText;
        private String toolTipAvailable;

        public String getToolTipAvailable() {
            return toolTipAvailable;
        }

        public void setToolTipAvailable(String toolTipAvailable) {
            this.toolTipAvailable = toolTipAvailable;
        }

        public String getToolTipTitle() {
            return toolTipTitle;
        }

        public void setToolTipTitle(String toolTipTitle) {
            this.toolTipTitle = toolTipTitle;
        }

        public String getToolTipPopConfirmText() {
            return toolTipPopConfirmText;
        }

        public void setToolTipPopConfirmText(String toolTipPopConfirmText) {
            this.toolTipPopConfirmText = toolTipPopConfirmText;
        }

        public String getConfirmText() {
            return confirmText;
        }

        public void setConfirmText(String confirmText) {
            this.confirmText = confirmText;
        }

        public String getCancelText() {
            return cancelText;
        }

        public void setCancelText(String cancelText) {
            this.cancelText = cancelText;
        }
    }
}
