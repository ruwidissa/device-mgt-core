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
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.OTPManagementException;
import org.wso2.carbon.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitation;
import org.wso2.carbon.device.mgt.common.otp.mgt.dto.OneTimePinDTO;

import java.util.Map;

public interface OTPManagementService {

    /**
     * Check the validity of the OTP
     * @param oneTimeToken OTP
     * @return The OTP data
     * @throws OTPManagementException if error occurred whle verifying validity of the OPT
     * @throws BadRequestException if found an null value for OTP
     */
    OneTimePinDTO isValidOTP(String oneTimeToken) throws OTPManagementException, BadRequestException;

    /**
     * Invalidate the OTP and send welcome mail
     * @param oneTimeToken OTP
     * @param email email address
     * @param properties email properties to add to email body
     * @throws OTPManagementException if error occurred while invalidate the OTP or send welcome email
     */
    void completeSelfRegistration(String oneTimeToken, String email, Map<String, String> properties)
            throws OTPManagementException;

    /**
     * Create OTP token and send device enrollment invitation
     * @param deviceEnrollmentInvitation object which contains device enrollment invitation related details
     * @throws OTPManagementException if error occurred while creating OTP token &/ sending mail
     */
    void sendDeviceEnrollmentInvitationMail(DeviceEnrollmentInvitation deviceEnrollmentInvitation)
            throws OTPManagementException;


    boolean hasEmailRegistered(String email, String emailDomain) throws OTPManagementException,
            DeviceManagementException;

    OneTimePinDTO generateOneTimePin(String email, String emailType, String userName, Object metaDataObj,
                                     int tenantId, boolean persistPin) throws OTPManagementException;

    OneTimePinDTO getRenewedOtpByEmailAndMailType(String email, String emailType) throws OTPManagementException;

}