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
package org.wso2.carbon.device.mgt.core.otp.mgt.service;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.OTPManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.otp.mgt.dto.OTPMailDTO;
import org.wso2.carbon.device.mgt.common.spi.OTPManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.OTPManagementDAO;
import org.wso2.carbon.device.mgt.common.otp.mgt.wrapper.OTPMailWrapper;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.OTPManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.otp.mgt.exception.OTPManagementDAOException;
import org.wso2.carbon.device.mgt.core.otp.mgt.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.core.service.EmailMetaInfo;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;

public class OTPManagementServiceImpl implements OTPManagementService {

    private static final Log log = LogFactory.getLog(OTPManagementServiceImpl.class);
    private OTPManagementDAO otpManagementDAO;

    public OTPManagementServiceImpl() {
        initDataAccessObjects();
    }

    private void initDataAccessObjects() {
        otpManagementDAO = OTPManagementDAOFactory.getOTPManagementDAO();
    }

    @Override
    public String createOTPToken(OTPMailWrapper otpMailWrapper) throws OTPManagementException, BadRequestException {

        if (!isValidOTPTokenCreatingRequest(otpMailWrapper)){
            String msg = "Found invalid payload with OTP creating request";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        Gson gson = new Gson();
        String metaInfo = gson.toJson(otpMailWrapper);
        String otpValue = UUID.randomUUID().toString();

        OTPMailDTO otpMailDTO = new OTPMailDTO();
        otpMailDTO.setEmail(otpMailWrapper.getEmail());
        otpMailDTO.setTenantId(otpMailDTO.getTenantId());
        otpMailDTO.setUsername(otpMailWrapper.getAdminUsername());
        otpMailDTO.setEmailType(otpMailWrapper.getEmailType());
        otpMailDTO.setMetaInfo(metaInfo);
        otpMailDTO.setOtpToken(otpValue);

        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (this.otpManagementDAO.addOTPData(otpMailDTO) == -1) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "OTP data saving failed. Please, contact Administrator";
                log.error(msg);
                throw new OTPManagementException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
            return otpValue;
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while disabling AutoCommit.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (OTPManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while saving the OTP data. Email address: " + otpMailDTO.getEmail();
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public OTPMailDTO isValidOTP(String oneTimeToken) throws OTPManagementException, BadRequestException {
        OTPMailDTO otpMailDTO = getOTPDataByToken(oneTimeToken);
        if (otpMailDTO == null) {
            String msg = "Couldn't found OTP data for the requesting OTP " + oneTimeToken + " In the system.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        if (otpMailDTO.isExpired()) {
            log.warn("Token is expired. OTP: " + oneTimeToken);
            return null;
        }
        if (otpMailDTO.isTenantCreated()) {
            log.warn("Tenant is already created for the token. OTP: " + oneTimeToken);
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
        Timestamp expiredTimestamp = new Timestamp(
                otpMailDTO.getCreatedAt().getTime() + otpMailDTO.getExpiryTime() * 1000);

        if (currentTimestamp.after(expiredTimestamp)) {
            String renewedOTP = UUID.randomUUID().toString();
            renewOTP(otpMailDTO, renewedOTP);
            Gson gson = new Gson();
            OTPMailWrapper otpMailWrapper = gson.fromJson(otpMailDTO.getMetaInfo(), OTPMailWrapper.class);
            resendUserVerifyingMail(otpMailWrapper.getFirstName(), renewedOTP, otpMailDTO.getEmail());
            return null;
        }
        return otpMailDTO;
    }

    /**
     * Get OTPData from DB
     * @param oneTimeToken One Time Token
     * @return {@link OTPMailDTO}
     * @throws OTPManagementException if error occurred while getting OTP data for given OTP in DB
     */
    private OTPMailDTO getOTPDataByToken ( String oneTimeToken) throws OTPManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            return otpManagementDAO.getOTPDataByToken(oneTimeToken);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to validate the given OTP.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (OTPManagementDAOException e) {
            String msg = "Error occurred while getting OTP data from DB. OTP: " + oneTimeToken;
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * Validate OTP token creating payload
     * @param otpMailWrapper OTPMailWrapper
     * @return true if its valid payload otherwise returns false
     */
    private boolean isValidOTPTokenCreatingRequest(OTPMailWrapper otpMailWrapper) {
        if (StringUtils.isBlank(otpMailWrapper.getFirstName())) {
            log.error("Received empty or blank first name field with OTP creating payload.");
            return false;
        }
        if (StringUtils.isBlank(otpMailWrapper.getLastName())) {
            log.error("Received empty or blank last name field with OTP creating payload.");
            return false;
        }
        if (StringUtils.isBlank(otpMailWrapper.getAdminUsername())) {
            log.error("Received empty or blank admin username field with OTP creating payload.");
            return false;
        }
        if (StringUtils.isBlank(otpMailWrapper.getAdminPassword())) {
            log.error("Received empty or blank admin password field with OTP creating payload.");
            return false;
        }
        if (StringUtils.isBlank(otpMailWrapper.getEmail())) {
            log.error("Received empty or blank email field with OTP creating payload.");
            return false;
        }
        if (StringUtils.isBlank(otpMailWrapper.getEmailType())) {
            log.error("Received empty or blank email type field with OTP creating payload.");
            return false;
        }
        if (otpMailWrapper.getTenantId() != -1234 && otpMailWrapper.getTenantId() < 1) {
            log.error("Invalid tenant Id field with OTP creating payload.");
            return false;
        }
        return true;
    }

    /**
     * If OTP expired, resend the user verifying mail with renewed OTP
     * @param firstName First Name of the User
     * @param renewedOTP Renewed OTP
     * @param mailAddress Mail Address of the User
     * @throws OTPManagementException if error occurred while resend the user verifying mail
     */
    private void resendUserVerifyingMail(String firstName, String renewedOTP, String mailAddress)
            throws OTPManagementException {
        Properties props = new Properties();
        props.setProperty("first-name", firstName);
        props.setProperty("otp-token", renewedOTP);

        EmailMetaInfo metaInfo = new EmailMetaInfo(mailAddress, props);
        try {
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                    .sendEnrolmentInvitation(DeviceManagementConstants.EmailAttributes.USER_VERIFY_TEMPLATE, metaInfo);
        } catch (DeviceManagementException e) {
            e.printStackTrace();
            throw new OTPManagementException(e);
        } catch (ConfigurationManagementException e) {
            throw new OTPManagementException(e);
        }
    }

    /**
     * Renew the OTP
     * @param otpMailDTO {@link OTPMailDTO}
     * @param renewedOTP Renewed OTP
     * @throws OTPManagementException if error occurred while renew the OTP
     */
    private void renewOTP(OTPMailDTO otpMailDTO, String renewedOTP) throws OTPManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            this.otpManagementDAO.renewOneTimeToken(otpMailDTO.getId(), renewedOTP);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while disabling AutoCommit to renew the OTP.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to renew the OTP.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (OTPManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while renew the OTP. OTP: " + renewedOTP;
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }
}
