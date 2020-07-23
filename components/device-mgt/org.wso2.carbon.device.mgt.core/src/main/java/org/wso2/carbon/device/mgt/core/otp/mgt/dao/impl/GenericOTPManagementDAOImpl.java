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

package org.wso2.carbon.device.mgt.core.otp.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.DBConnectionException;
import org.wso2.carbon.device.mgt.common.otp.mgt.dto.OTPMailDTO;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.AbstractDAOImpl;
import org.wso2.carbon.device.mgt.core.otp.mgt.dao.OTPManagementDAO;
import org.wso2.carbon.device.mgt.core.otp.mgt.exception.OTPManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

public class GenericOTPManagementDAOImpl extends AbstractDAOImpl implements OTPManagementDAO {

    private static final Log log = LogFactory.getLog(GenericOTPManagementDAOImpl.class);

    @Override
    public int addOTPData(OTPMailDTO otpMailDTO) throws OTPManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to create an OTP data entry");
            log.debug("OTP Details : ");
            log.debug("OTP key : " + otpMailDTO.getOtpToken() + " Email : " + otpMailDTO.getEmail());
        }

        String sql = "INSERT INTO DM_OTP_DATA "
                + "(OTP_TOKEN, "
                + "TENANT_DOMAIN,"
                + "EMAIL, "
                + "EMAIL_TYPE, "
                + "META_INFO, "
                + "CREATED_AT) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            Calendar calendar = Calendar.getInstance();
            Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, otpMailDTO.getOtpToken());
                stmt.setString(2, otpMailDTO.getTenantDomain());
                stmt.setString(3, otpMailDTO.getEmail());
                stmt.setString(4, otpMailDTO.getEmailType());
                stmt.setString(5, otpMailDTO.getMetaInfo());
                stmt.setTimestamp(6, timestamp);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return -1;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to create an opt entry for email "
                    + otpMailDTO.getEmail();
            log.error(msg, e);
            throw new OTPManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing SQL to create an otp entry for email " + otpMailDTO.getEmail();
            log.error(msg, e);
            throw new OTPManagementDAOException(msg, e);
        }
    }
}
