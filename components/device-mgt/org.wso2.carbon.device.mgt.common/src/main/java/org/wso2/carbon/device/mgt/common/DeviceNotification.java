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

package org.wso2.carbon.device.mgt.common;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "device")
public class DeviceNotification {


    @XmlAttribute(name = "id")
    private int deviceId;
    @XmlElement(name = "name")
    private String deviceName;
    @XmlElement(name = "type")
    private String deviceType;
    @XmlElement(name = "description")
    private String description;
    @XmlElement(name = "properties")
    private DevicePropertyNotification properties;
    @XmlElement(name = "enrollment_info")
    private DeviceEnrollmentInfoNotification enrollmentInfo;

    public DeviceNotification(){}

    public DeviceNotification(int deviceId, String deviceName, String deviceType, String description,
            DevicePropertyNotification devicePropertyNotification,
            DeviceEnrollmentInfoNotification deviceEnrollmentInfoNotification) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.description = description;
        this.properties = devicePropertyNotification;
        this.enrollmentInfo = deviceEnrollmentInfoNotification;
    }
}
