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

package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "confirmationTexts", propOrder = {
        "deleteConfirmModalTitle",
        "deleteConfirmModalText",
        "deleteConfirmationTextDescribe",
        "deleteConfirmationText",
        "cancelText",
        "confirmText",
        "inputLabel",
        "inputRequireMessage"
})
public class ConfirmationTexts {

    @XmlElement(name = "deleteConfirmModalTitle")
    private String deleteConfirmModalTitle;

    @XmlElement(name = "deleteConfirmModalText")
    private String deleteConfirmModalText;

    @XmlElement(name = "deleteConfirmationTextDescribe")
    private String deleteConfirmationTextDescribe;

    @XmlElement(name = "deleteConfirmationText")
    private String deleteConfirmationText;

    @XmlElement(name = "cancelText")
    private String cancelText;

    @XmlElement(name = "confirmText")
    private String confirmText;

    @XmlElement(name = "inputLabel")
    private String inputLabel;

    @XmlElement(name = "inputRequireMessage")
    private String inputRequireMessage;

    public String getCancelText() {
        return cancelText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public String getInputRequireMessage() {
        return inputRequireMessage;
    }

    public void setInputRequireMessage(String inputRequireMessage) {
        this.inputRequireMessage = inputRequireMessage;
    }

    public String getInputLabel() {
        return inputLabel;
    }

    public void setInputLabel(String inputLabel) {
        this.inputLabel = inputLabel;
    }

    public String getConfirmText() {
        return confirmText;
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
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
