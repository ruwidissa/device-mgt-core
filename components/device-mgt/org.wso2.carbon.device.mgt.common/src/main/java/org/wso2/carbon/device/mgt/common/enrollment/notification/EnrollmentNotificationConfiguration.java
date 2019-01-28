/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.enrollment.notification;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the information related to Enrollment Configuration configuration.
 */
@XmlRootElement(name = "EnrolmentNotificationConfiguration")
public class EnrollmentNotificationConfiguration {

    private boolean notifyThroughExtension;
    private boolean enabled;
    private String extensionClass;
    private String notyfyingInternalHost;

    /**
     * Enrollment Notification enabled
     *
     * @return If it is required to send notification for each enrollment, returns true otherwise returns false
     */
    @XmlElement(name = "Enabled", required = true)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setNotifyThroughExtension(boolean notifyThroughExtension) {
        this.notifyThroughExtension = notifyThroughExtension;
    }

    /**
     * Enable notifying the enrollment through extension
     *
     * @return IF notifications are sending through the extension, returns true otherwise returns false
     */
    @XmlElement(name = "NotifyThroughExtension", required = true)
    public boolean getNotifyThroughExtension() {
        return notifyThroughExtension;
    }

    /**
     * Extension Class
     *
     * @return extension full class path is returned
     */
    @XmlElement(name = "ExtensionClass", required = true)
    public String getExtensionClass() {
        return extensionClass;
    }

    public void setExtensionClass(String extensionClass) {
        this.extensionClass = extensionClass;
    }

    /**
     * Extension Class
     *
     * @return extension full class path is returned
     */
    @XmlElement(name = "NotifyingInternalHost", required = true)
    public String getNotyfyingInternalHost() {
        return notyfyingInternalHost;
    }

    public void setNotyfyingInternalHost(String notyfyingInternalHost) {
        this.notyfyingInternalHost = notyfyingInternalHost;
    }
}
