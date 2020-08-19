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
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.OTPManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.UnAuthorizedException;
import org.wso2.carbon.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitation;
import org.wso2.carbon.device.mgt.common.invitation.mgt.DeviceEnrollmentInvitationDetails;
import org.wso2.carbon.device.mgt.common.invitation.mgt.DeviceEnrollmentType;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.otp.mgt.OTPEmailTypes;
import org.wso2.carbon.device.mgt.common.otp.mgt.dto.OneTimePinDTO;
import org.wso2.carbon.device.mgt.common.spi.OTPManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.keymanager.KeyManagerConfigurations;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.OTPManagementDAO;
import org.wso2.carbon.device.mgt.common.otp.mgt.wrapper.OTPWrapper;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.OTPManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.otp.mgt.exception.OTPManagementDAOException;
import org.wso2.carbon.device.mgt.core.otp.mgt.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.EmailMetaInfo;
import org.apache.commons.validator.routines.EmailValidator;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;

import static org.wso2.carbon.device.mgt.common.DeviceManagementConstants.OTPProperties;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
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
    public void sendUserVerifyingMail(OTPWrapper otpWrapper) throws OTPManagementException, DeviceManagementException {
        Tenant tenant = validateTenantCreatingDetails(otpWrapper);
        OneTimePinDTO oneTimePinDTO = createOneTimePin(otpWrapper.getEmail(), otpWrapper.getEmailType(),
                otpWrapper.getUsername(), tenant, -1234);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (this.otpManagementDAO.addOTPData(oneTimePinDTO) == -1) {
                ConnectionManagerUtil.rollbackDBTransaction();
                String msg = "OTP data saving failed. Please, contact Administrator";
                log.error(msg);
                throw new OTPManagementException(msg);
            }
            Properties props = new Properties();
            props.setProperty("first-name", tenant.getAdminFirstName());
            props.setProperty("otp-token", oneTimePinDTO.getOtpToken());
            sendMail(props, tenant.getEmail(), DeviceManagementConstants.EmailAttributes.USER_VERIFY_TEMPLATE);
            ConnectionManagerUtil.commitDBTransaction();
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
            String msg = "Error occurred while saving the OTP data. Email address: " + oneTimePinDTO.getEmail();
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public OneTimePinDTO isValidOTP(String oneTimeToken) throws OTPManagementException, BadRequestException {
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
                oneTimePinDTO.getCreatedAt().getTime() + oneTimePinDTO.getExpiryTime() * 1000);

        if (currentTimestamp.after(expiredTimestamp)) {
            String renewedOTP = UUID.randomUUID().toString();
            renewOTP(oneTimePinDTO, renewedOTP);
            Gson gson = new Gson();
            Tenant tenant = gson.fromJson(oneTimePinDTO.getMetaInfo(), Tenant.class);

            Properties props = new Properties();
            props.setProperty("first-name", tenant.getAdminFirstName());
            props.setProperty("otp-token", renewedOTP);
            sendMail(props, oneTimePinDTO.getEmail(), DeviceManagementConstants.EmailAttributes.USER_VERIFY_TEMPLATE);
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
        DeviceManagementProviderService dms = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider();
        StringBuilder enrollmentSteps = new StringBuilder();
        DeviceEnrollmentInvitationDetails deviceEnrollmentInvitationDetails;
        for (DeviceEnrollmentType deviceEnrollmentType : deviceEnrollmentInvitation.getDeviceEnrollmentTypes()) {
            deviceEnrollmentInvitationDetails = dms.getDeviceEnrollmentInvitationDetails(
                    deviceEnrollmentType.getDeviceType());
            if (deviceEnrollmentInvitationDetails != null &&
                deviceEnrollmentInvitationDetails.getEnrollmentDetails() != null) {
                for (String enrollmentType : deviceEnrollmentType.getEnrollmentType()) {
                    deviceEnrollmentInvitationDetails.getEnrollmentDetails().stream()
                            .filter(details -> enrollmentType.equals(details.getEnrollmentType())).findFirst()
                            .ifPresent(details -> enrollmentSteps.append(details.getEnrollmentSteps()));
                }
            }
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        OneTimePinDTO oneTimePinDTO;
        Properties props = new Properties();
        props.setProperty("enrollment-steps", enrollmentSteps.toString());
        try {
            for (String username : deviceEnrollmentInvitation.getUsernames()) {
                String emailAddress = DeviceManagerUtil.getUserClaimValue(
                        username, DeviceManagementConstants.User.CLAIM_EMAIL_ADDRESS);
                oneTimePinDTO = createOneTimePin(emailAddress, OTPEmailTypes.DEVICE_ENROLLMENT.toString(), username,
                        null, tenantId);
                props.setProperty("first-name", DeviceManagerUtil.
                        getUserClaimValue(username, DeviceManagementConstants.User.CLAIM_FIRST_NAME));
                props.setProperty("username", username);
                props.setProperty("otp-token", oneTimePinDTO.getOtpToken());
                sendMail(props, emailAddress, DeviceManagementConstants.EmailAttributes.USER_ENROLLMENT_TEMPLATE);
            }
        } catch (UserStoreException e) {
            String msg = "Error occurred while getting claim values to invite user";
            log.error(msg, e);
            throw new OTPManagementException(msg, e);
        }
    }


    /**
     * Create One Time Token
     * @param email email
     * @param emailType email type
     * @param userName username
     * @param metaDataObj meta data object
     * @param tenantId tenant Id
     * @return {@link OneTimePinDTO}
     */
    private OneTimePinDTO createOneTimePin(String email, String emailType, String userName, Object metaDataObj,
            int tenantId) {

        String otpValue = UUID.randomUUID().toString();

        Gson gson = new Gson();
        String metaInfo = gson.toJson(metaDataObj);

        OneTimePinDTO oneTimePinDTO = new OneTimePinDTO();
        oneTimePinDTO.setEmail(email);
        oneTimePinDTO.setTenantId(tenantId);
        oneTimePinDTO.setUsername(userName);
        oneTimePinDTO.setEmailType(emailType);
        oneTimePinDTO.setMetaInfo(metaInfo);
        oneTimePinDTO.setOtpToken(otpValue);

        return oneTimePinDTO;
    }

    /**
     * Get OTPData from DB
     * @param oneTimeToken One Time Token
     * @return {@link OneTimePinDTO}
     * @throws OTPManagementException if error occurred while getting OTP data for given OTP in DB
     */
    private OneTimePinDTO getOTPDataByToken ( String oneTimeToken) throws OTPManagementException {
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
     * Validate Tenant details
     * @param otpWrapper OTP-Wrapper
     * @return {@link Tenant} if its valid payload otherwise throws {@link DeviceManagementException}
     * @throws DeviceManagementException if invalid payload or unauthorized request received
     */
    private Tenant validateTenantCreatingDetails(OTPWrapper otpWrapper) throws DeviceManagementException {

        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                .getDeviceManagementConfig();
        KeyManagerConfigurations kmConfig = deviceManagementConfig.getKeyManagerConfigurations();

        if (StringUtils.isBlank(otpWrapper.getUsername())) {
            String msg = "Received Blank username to create OTP. Username: " + otpWrapper.getUsername();
            log.error(msg);
            throw new BadRequestException(msg);
        }

        String[] superTenantDetails = otpWrapper.getUsername().split("@");
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(superTenantDetails[superTenantDetails.length - 1])
                || !superTenantDetails[0].equals(kmConfig.getAdminUsername())) {
            String msg = "You don't have required permission to create OTP";
            log.error(msg);
            throw new UnAuthorizedException(msg);
        }

        Tenant tenant = new Tenant();
        List<Metadata> properties = otpWrapper.getProperties();
        for (Metadata property : properties) {
            if (property == null) {
                String msg = "Received invalid property to create OTP.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
            switch (property.getMetaKey()) {
                case OTPProperties.FIRST_NAME:
                    String firstName = property.getMetaValue();
                    if (StringUtils.isBlank(firstName)) {
                        String msg = "Received empty or blank first name field with OTP creating payload.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    tenant.setAdminFirstName(firstName);
                    break;
                case OTPProperties.LAST_NAME:
                    String lastName = property.getMetaValue();
                    if (StringUtils.isBlank(lastName)) {
                        String msg = "Received empty or blank last name field with OTP creating payload.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    tenant.setAdminLastName(lastName);
                    break;
                case OTPProperties.TENANT_ADMIN_PASSWORD:
                    String pwd = property.getMetaValue();
                    if (StringUtils.isBlank(pwd)) {
                        String msg = "Received empty or blank admin password field with OTP creating payload.";
                        log.error(msg);
                        throw new BadRequestException(msg);
                    }
                    tenant.setAdminPassword(pwd);
                    break;
                default:
                    String msg = "Received invalid key with OTP properties for creating OTP.";
                    log.error(msg);
                    throw new BadRequestException(msg);
            }
        }

        if (StringUtils.isBlank(otpWrapper.getEmail())) {
            String msg = "Received empty or blank email field with OTP creating payload.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(otpWrapper.getEmail())) {
            String msg = "Found invalid email. Hence please verify the email address and re-try. Email: " + otpWrapper
                    .getEmail();
            log.error(msg);
            throw new BadRequestException(msg);
        }

        if (StringUtils.isBlank(otpWrapper.getEmailType())) {
            String msg = "Received empty or blank email type field with OTP creating payload.";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        try {
            ConnectionManagerUtil.openDBConnection();
            if (otpManagementDAO.isEmailExist(otpWrapper.getEmail(), otpWrapper.getEmailType())) {
                String msg = "Email is registered to execute the same action. Hence can't proceed.";
                log.error(msg);
                throw new BadRequestException(msg);
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to validate the given email and email type.";
            log.error(msg);
            throw new DeviceManagementException(msg);
        } catch (OTPManagementDAOException e) {
            String msg = "Error occurred while executing SQL query to validate the given email and email type.";
            log.error(msg);
            throw new DeviceManagementException(msg);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }

        String[] tenantUsernameDetails = otpWrapper.getEmail().split("@");
        tenant.setAdminName(tenantUsernameDetails[0]);
        tenant.setDomain(tenantUsernameDetails[tenantUsernameDetails.length - 1]);
        tenant.setEmail(otpWrapper.getEmail());
        return tenant;
    }

    /**
     * If OTP expired, resend the user verifying mail with renewed OTP
     * @param props Mail body properties
     * @param mailAddress Mail Address of the User
     * @param template Mail template to be used
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
     * @param renewedOTP Renewed OTP
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
}
