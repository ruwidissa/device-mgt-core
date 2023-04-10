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

import java.util.List;

public interface OTPManagementDAO {

    /**
     * Save OTP token data and tenant details of registering user
     * @param oneTimePinDTOList OTPMailDTO
     * @throws OTPManagementDAOException if error occurred whule storing data
     */
    void addOTPData(List<OneTimePinDTO> oneTimePinDTOList) throws OTPManagementDAOException;

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
    boolean expireOneTimeToken(String oneTimeToken) throws OTPManagementDAOException;

    /**
     * Update OTP with renewed OTP
     * @param id ID
     * @param oneTimeToken One Time Token
     * @throws OTPManagementDAOException if error occured while updating OTP
     */
    void renewOneTimeToken(int id, String oneTimeToken) throws OTPManagementDAOException;

    void restoreOneTimeToken(int id, String oneTimeToken) throws OTPManagementDAOException;


    /**
     * To veify whether email and email type exists or not
     * @param email email
     * @param emailType email type
     * @return true if email and email type exists otherwise returns false
     * @throws OTPManagementDAOException if error occurred while verify existance of the email and email type
     */
    boolean isEmailExist (String email, String emailType) throws OTPManagementDAOException;

    OneTimePinDTO getOtpDataByEmailAndMailType(String email, String emailType) throws OTPManagementDAOException;

}
