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

package org.wso2.carbon.device.mgt.core.otp.mgt.dao;

import org.wso2.carbon.device.mgt.common.otp.mgt.dto.OneTimePinDTO;
import org.wso2.carbon.device.mgt.core.otp.mgt.exception.OTPManagementDAOException;

public interface OTPManagementDAO {

    /**
     * Save OTP token data and tenant details of registering user
     * @param oneTimePinDTO OTPMailDTO
     * @return  Primary key of the newly adding data raw
     * @throws OTPManagementDAOException if error occurred whule storing data
     */
    int addOTPData(OneTimePinDTO oneTimePinDTO) throws OTPManagementDAOException;

    /**
     * Get OTP data for requesting One Time Token
     * @param oneTimeToken One Time Token
     * @return {@link OneTimePinDTO}
     * @throws OTPManagementDAOException if error ocured while getting OTP data for requesting one time token
     */
    OneTimePinDTO getOTPDataByToken (String oneTimeToken) throws OTPManagementDAOException;

    /**
     * Expire the OTP
     * @param oneTimeToken OTP
     * @throws OTPManagementDAOException if error occurred while updating the OTP validity.
     */
    void expireOneTimeToken(String oneTimeToken) throws OTPManagementDAOException;

    /**
     * Update OTP with renewed OTP
     * @param id ID
     * @param oneTimeToken One Time Token
     * @throws OTPManagementDAOException if error occured while updating OTP
     */
    void renewOneTimeToken(int id, String oneTimeToken) throws OTPManagementDAOException;

}
