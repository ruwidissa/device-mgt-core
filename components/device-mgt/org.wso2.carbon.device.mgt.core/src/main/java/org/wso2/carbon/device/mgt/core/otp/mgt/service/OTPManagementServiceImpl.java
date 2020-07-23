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
import org.wso2.carbon.device.mgt.common.exceptions.BadRequestException;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.exceptions.OTPManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.otp.mgt.dto.OTPMailDTO;
import org.wso2.carbon.device.mgt.common.spi.OTPManagementService;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.OTPManagementDAO;
import org.wso2.carbon.device.mgt.common.otp.mgt.wrapper.OTPMailWrapper;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.OTPManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.otp.mgt.exception.OTPManagementDAOException;
import org.wso2.carbon.device.mgt.core.otp.mgt.util.ConnectionManagerUtil;

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
        otpMailDTO.setTenantDomain(otpMailWrapper.getTenantDomain());
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
        if (StringUtils.isBlank(otpMailWrapper.getTenantDomain())) {
            log.error("Received empty or blank tenant domain field with OTP creating payload.");
            return false;
        }
        return true;
    }
}
