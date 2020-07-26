/* Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.OTPManagementException;
import org.wso2.carbon.device.mgt.common.otp.mgt.wrapper.OTPMailWrapper;

public interface OTPManagementService {

    /**
     * Cretae OTP token and store tenant details in the DB
     * @param otpMailWrapper OTP Mail Wrapper object which contains tenant details of registering user
     * @return OTPToken
     * @throws OTPManagementException if error occurs while creating OTP token and storing tenant details.
     * @throws BadRequestException if found and incompatible payload to create OTP token.
     */
    String createOTPToken (OTPMailWrapper otpMailWrapper) throws OTPManagementException, BadRequestException;
}
