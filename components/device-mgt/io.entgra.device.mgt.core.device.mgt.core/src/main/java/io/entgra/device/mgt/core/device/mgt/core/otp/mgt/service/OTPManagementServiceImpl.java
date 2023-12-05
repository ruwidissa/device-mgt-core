/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.core.otp.mgt.service;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.*;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitation;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.DeviceEnrollmentType;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.EnrollmentTypeMail;
import io.entgra.device.mgt.core.device.mgt.common.invitation.mgt.UserMailAttributes;
import io.entgra.device.mgt.core.device.mgt.common.otp.mgt.dto.OneTimePinDTO;
import io.entgra.device.mgt.core.device.mgt.common.spi.OTPManagementService;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.otp.mgt.dao.OTPManagementDAO;
import io.entgra.device.mgt.core.device.mgt.core.otp.mgt.dao.OTPManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.otp.mgt.exception.OTPManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.otp.mgt.util.ConnectionManagerUtil;
import io.entgra.device.mgt.core.device.mgt.core.service.EmailMetaInfo;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;

import java.sql.Timestamp;
import java.util.*;

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
    public boolean hasEmailRegistered(String email, String emailDomain) throws OTPManagementException,
            DeviceManagementException {
        try {
            ConnectionManagerUtil.openDBConnection();
            if (otpManagementDAO.isEmailExist(email, emailDomain)) {
                return true;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to validate the given email and email type.";
            log.error(msg);
            throw new DeviceManagementException(msg);
        } catch (OTPManagementDAOException e) {
            String msg = "Error occurred while executing SQL query to validate the given email and email type.";
            log.error(msg);
            throw new OTPManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        return false;
    }

    public OneTimePinDTO getRenewedOtpByEmailAndMailType(String email, String emailType) throws OTPManagementException{
        OneTimePinDTO oneTimePinDTO;
        String newToken = UUID.randomUUID().toString();
        try {
            ConnectionManagerUtil.beginDBTransaction();
            oneTimePinDTO = otpManagementDAO.getOtpDataByEmailAndMailType(email, emailType);
            if (oneTimePinDTO == null) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "Can't find OTP data for email: " + email + " and email type: " + emailType;
                log.error(msg);
                throw new OTPManagementException(msg);
            }
            otpManagementDAO.restoreOneTimeToken(oneTimePinDTO.getId(), newToken);
            ConnectionManagerUtil.commitDBTransaction();

        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while getting database connection to validate the given email and email type.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (OTPManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while executing SQL query to validate the given email and email type.";
            log.error(msg, e);
            throw new OTPManagementException(msg);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while starting the DB transaction";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
        oneTimePinDTO.setOtpToken(newToken);
        return oneTimePinDTO;
    }

    @Override
    public OneTimePinDTO isValidOTP(String oneTimeToken, boolean requireRenewal) throws OTPManagementException,
            BadRequestException {
        if (StringUtils.isBlank(oneTimeToken)){
            String msg = "Received blank OTP to verify. OTP: " + oneTimeToken;
            log.error(msg);
            throw new BadRequestException(msg);
        }

        OneTimePinDTO oneTimePinDTO = getOTPDataByToken(oneTimeToken);
        if (oneTimePinDTO == null) {
            String msg = "Couldn't found OTP data for the requesting OTP " + oneTimeToken + " In the system.";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (oneTimePinDTO.isExpired()) {
            log.warn("Token is expired. OTP: " + oneTimeToken);
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        Timestamp currentTimestamp = new Timestamp(calendar.getTime().getTime());
        Timestamp expiredTimestamp = new Timestamp(
                oneTimePinDTO.getCreatedAt().getTime() + oneTimePinDTO.getExpiryTime() * 1000L);

        if (currentTimestamp.after(expiredTimestamp)) {
            if (requireRenewal) {
                String renewedOTP = UUID.randomUUID().toString();
                renewOTP(oneTimePinDTO, renewedOTP);
                Gson gson = new Gson();
                Tenant tenant = gson.fromJson(oneTimePinDTO.getMetaInfo(), Tenant.class);

                Properties props = new Properties();
                props.setProperty("first-name", tenant.getAdminFirstName());
                props.setProperty("otp-token", renewedOTP);
                props.setProperty("email", oneTimePinDTO.getEmail());
                props.setProperty("type", oneTimePinDTO.getEmailType());
                sendMail(props, oneTimePinDTO.getEmail(), DeviceManagementConstants.EmailAttributes.USER_VERIFY_TEMPLATE);
            }
            return null;
        }
        return oneTimePinDTO;
    }

    @Override
    public void completeSelfRegistration(String oneTimeToken, String email, Map<String, String> properties)
            throws OTPManagementException {
        try {
            invalidateOTP(oneTimeToken);
            Properties props = new Properties();
            properties.forEach(props::setProperty);
            sendMail(props, email, DeviceManagementConstants.EmailAttributes.USER_WELCOME_TEMPLATE);
        } catch (OTPManagementException e) {
            String msg = "Error occurred while completing the self registration via OTP";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        }
    }

    /**
     * Invalidate the OTP
     * @param oneTimeToken OTP
     * @throws OTPManagementException If error occurred while invalidating the OTP
     */
    private void invalidateOTP(String oneTimeToken) throws OTPManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (!otpManagementDAO.expireOneTimeToken(oneTimeToken)) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "Couldn't find OTP entry for OTP: " + oneTimeToken;
                log.error(msg);
                throw new OTPManagementException(msg);
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (OTPManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            String msg = "Error occurred while invalidate the OTP: " + oneTimeToken;
            log.error(msg);
            throw new OTPManagementException(msg);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while disabling AutoCommit to invalidate OTP.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to invalidate OPT.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void sendDeviceEnrollmentInvitationMail(DeviceEnrollmentInvitation deviceEnrollmentInvitation)
            throws OTPManagementException {
        List<EnrollmentTypeMail> enrollmentTypeMails =
                getEnrollmentTypeMails(deviceEnrollmentInvitation.getDeviceEnrollmentTypes());
        sendEnrollmentTypeMails(deviceEnrollmentInvitation.getUsernames(), enrollmentTypeMails);
    }

    /**
     * Create One Time Token
     * @param oneTimePinDTO Data related to the one time pin
     * @return {@link OneTimePinDTO}
     */
    @Override
    public OneTimePinDTO generateOneTimePin(OneTimePinDTO oneTimePinDTO, boolean persistPin) throws OTPManagementException {

        String otpValue = UUID.randomUUID().toString();
        String metaInfo = oneTimePinDTO.getMetaInfo();

        oneTimePinDTO.setMetaInfo(metaInfo);
        oneTimePinDTO.setOtpToken(otpValue);

        if (persistPin) {
            try {
                ConnectionManagerUtil.beginDBTransaction();
                this.otpManagementDAO.addOTPData(Collections.singletonList(oneTimePinDTO));
                ConnectionManagerUtil.commitDBTransaction();
            } catch (TransactionManagementException e) {
                String msg = "Error occurred while disabling AutoCommit.";
                log.error(msg, e);
                throw new OTPManagementException(msg, e);
            } catch (DBConnectionException e) {
                String msg = "Error occurred while getting database connection to add OPT data.";
                log.error(msg, e);
                throw new OTPManagementException(msg, e);
            } catch (OTPManagementDAOException e) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "Error occurred while saving the OTP data for given email";
                log.error(msg, e);
                throw new OTPManagementException(msg, e);
            } finally {
                ConnectionManagerUtil.closeDBConnection();
            }
        }
        return oneTimePinDTO;
    }

    /**
     * Get OTPData from DB
     * @param oneTimeToken One Time Token
     * @return {@link OneTimePinDTO}
     * @throws OTPManagementException if error occurred while getting OTP data for given OTP in DB
     */
    private OneTimePinDTO getOTPDataByToken(String oneTimeToken) throws OTPManagementException {
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
     * If OTP expired, resend the user verifying mail with renewed OTP
     * @param props Mail body properties
     * @param mailAddress Mail Address of the User
     * @param template    Mail template to be used
     * @throws OTPManagementException if error occurred while resend the user verifying mail
     */
    private void sendMail(Properties props, String mailAddress, String template) throws OTPManagementException {
        try {
            EmailMetaInfo metaInfo = new EmailMetaInfo(mailAddress, props);
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                    .sendEnrolmentInvitation(template, metaInfo);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while sending email using email template '" + template + "'.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (ConfigurationManagementException e) {
            String msg = "Configuration error occurred. Hence  mail sending failed.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        }
    }

    /**
     * Renew the OTP
     * @param oneTimePinDTO {@link OneTimePinDTO}
     * @param renewedOTP    Renewed OTP
     * @throws OTPManagementException if error occurred while renew the OTP
     */
    private void renewOTP(OneTimePinDTO oneTimePinDTO, String renewedOTP) throws OTPManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            this.otpManagementDAO.renewOneTimeToken(oneTimePinDTO.getId(), renewedOTP);
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

    /**
     * Send enrollment type mails to users
     * @param usernames List of usernames to send enrollment type mails
     * @param enrollmentTypeMails List of enrollment types
     * @throws OTPManagementException Throws when error occurred while sending emails
     */
    private void sendEnrollmentTypeMails(List<String> usernames, List<EnrollmentTypeMail> enrollmentTypeMails)
            throws OTPManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            for (String username : usernames) {
                populateUserAttributes(getUserMailAttributes(username), enrollmentTypeMails);
                for (EnrollmentTypeMail enrollmentTypeMail : enrollmentTypeMails) {
                    sendMail(enrollmentTypeMail);
                }
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (UserStoreException e) {
            String msg = "Error occurred while populating user attributes";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add OTP data.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "SQL Error occurred when adding OPT data to send device enrollment Invitation.";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * Send enrollment type mail
     * @param enrollmentTypeMail Data related to the enrollment mail
     * @throws OTPManagementException Throws when error occurred while sending email
     */
    private void sendMail(EnrollmentTypeMail enrollmentTypeMail) throws OTPManagementException {
        sendMail(enrollmentTypeMail.getProperties(), enrollmentTypeMail.getRecipient(), enrollmentTypeMail.getTemplate());
    }

    /**
     * Get user claims based on the username
     * @param username Username
     * @return {@link UserMailAttributes}
     * @throws UserStoreException Throws when error occurred while retrieving user claims
     */
    private UserMailAttributes getUserMailAttributes(String username) throws UserStoreException {
        UserMailAttributes userMailAttributes = new UserMailAttributes();
        userMailAttributes.setEmail(DeviceManagerUtil.getUserClaimValue(
                username, DeviceManagementConstants.User.CLAIM_EMAIL_ADDRESS));
        userMailAttributes.setFirstName(DeviceManagerUtil.
                getUserClaimValue(username, DeviceManagementConstants.User.CLAIM_FIRST_NAME));
        userMailAttributes.setUsername(username);
        return userMailAttributes;
    }

    /**
     * Populate enrollment type mails with provided user attributes
     * @param userMailAttributes User attributes
     * @param enrollmentTypeMails Enrollment type mails
     */
    private void populateUserAttributes(UserMailAttributes userMailAttributes, List<EnrollmentTypeMail> enrollmentTypeMails) {
        for (EnrollmentTypeMail enrollmentTypeMail : enrollmentTypeMails) {
            Properties properties = new Properties();
            properties.setProperty(userMailAttributes.getEmailPlaceholder(), userMailAttributes.getEmail());
            properties.setProperty(userMailAttributes.getFirstNamePlaceholder(), userMailAttributes.getFirstName());
            properties.setProperty(userMailAttributes.getUsernamePlaceholder(), userMailAttributes.getUsername());
            enrollmentTypeMail.setProperties(properties);
            enrollmentTypeMail.setUsername(userMailAttributes.getUsername());
            enrollmentTypeMail.setRecipient(userMailAttributes.getEmail());
        }
    }

    /**
     * Generate enrollment type mail
     * @param deviceType Device type of the enrollment type
     * @param enrollmentType Enrollment type
     * @return {@link EnrollmentTypeMail}
     */
    private EnrollmentTypeMail getEnrollmentTypeMail(String deviceType, String enrollmentType) {
        EnrollmentTypeMail enrollmentTypeMail = new EnrollmentTypeMail();
        enrollmentTypeMail.setUsername(enrollmentTypeMail.getUsername());
        enrollmentTypeMail.setTemplate(String.join(DeviceManagementConstants.EmailAttributes.TEMPLATE_NAME_PART_JOINER,
                deviceType.toLowerCase(), enrollmentType.toLowerCase().
                        replace(DeviceManagementConstants.EmailAttributes.ENROLLMENT_TYPE_SPLITTER,
                                DeviceManagementConstants.EmailAttributes.TEMPLATE_NAME_PART_JOINER),
                DeviceManagementConstants.EmailAttributes.DEVICE_ENROLLMENT_MAIL_KEY));
        return enrollmentTypeMail;
    }

    /**
     * Generate enrollment type mails from device enrollment types
     * @param deviceEnrollmentTypes List of device enrollment types
     * @return List of enrollment type mails
     */
    private List<EnrollmentTypeMail> getEnrollmentTypeMails(List<DeviceEnrollmentType> deviceEnrollmentTypes) {
        List<EnrollmentTypeMail> enrollmentTypeMails = new ArrayList<>();
        for (DeviceEnrollmentType deviceEnrollmentType : deviceEnrollmentTypes) {
            String deviceType = deviceEnrollmentType.getDeviceType();
            for (String enrollmentType : deviceEnrollmentType.getEnrollmentType()) {
                enrollmentTypeMails.add(getEnrollmentTypeMail(deviceType, enrollmentType));
            }
        }
        return enrollmentTypeMails;
    }
}