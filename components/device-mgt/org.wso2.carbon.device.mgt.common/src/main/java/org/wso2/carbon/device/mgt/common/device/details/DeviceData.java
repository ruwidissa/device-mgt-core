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

package org.wso2.carbon.device.mgt.common.device.details;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.Date;

public class DeviceData {

    /***
     * Identifier of the device, which contains identifier and device ype.
     */
    private DeviceIdentifier deviceIdentifier;

    /***
     * User who is enrolled the device.
     */
    private String deviceOwner;

    /***
     * Last modified date.
     */
    private Date lastModifiedDate;

    /***
     * Current status of the device, e.g ACTIVE, INACTIVE, REMOVED etc.
     */
    private String deviceOwnership;

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public String getDeviceOwner() {
        return deviceOwner;
    }

    public void setDeviceOwner(String deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getDeviceOwnership() {
        return deviceOwnership;
    }

    public void setDeviceOwnership(String deviceOwnership) {
        this.deviceOwnership = deviceOwnership;
    }
}
