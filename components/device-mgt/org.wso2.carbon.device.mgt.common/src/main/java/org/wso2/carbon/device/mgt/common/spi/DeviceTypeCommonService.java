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

package org.wso2.carbon.device.mgt.common.spi;

import org.wso2.carbon.device.mgt.common.QREnrollmentPayload;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.general.QREnrollmentDetails;

/**
 * This implementation populates device type plugin management service.
 */
public interface DeviceTypeCommonService {

    /**
     * To get Enrollment QR code against Ownership type
     *
     * @param qrEnrollmentDetails QR Enrollment Details
     * @return {@link QREnrollmentPayload} object with payload to generate QR, invalidPlatformConfigs
     * and optionalPlatformConfigs.
     * @throws DeviceManagementException if error occurred while generating the QR String for Ownership
     */
    QREnrollmentPayload getEnrollmentQRCode(QREnrollmentDetails qrEnrollmentDetails) throws DeviceManagementException;
}
