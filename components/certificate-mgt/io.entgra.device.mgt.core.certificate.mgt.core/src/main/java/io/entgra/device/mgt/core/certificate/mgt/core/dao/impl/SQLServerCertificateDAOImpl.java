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

package io.entgra.device.mgt.core.certificate.mgt.core.dao.impl;

import io.entgra.device.mgt.core.device.mgt.common.CertificatePaginationRequest;
import io.entgra.device.mgt.core.certificate.mgt.core.dto.CertificateResponse;
import io.entgra.device.mgt.core.certificate.mgt.core.impl.CertificateGenerator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.certificate.mgt.core.dao.CertificateManagementDAOException;
import io.entgra.device.mgt.core.certificate.mgt.core.dao.CertificateManagementDAOFactory;
import io.entgra.device.mgt.core.certificate.mgt.core.service.PaginationResult;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the SQLServer implementation of CertificateDAO which can be used to support SQLServer specific
 * db syntax.
 */
public class SQLServerCertificateDAOImpl extends AbstractCertificateDAOImpl {

    private static final Log log = LogFactory.getLog(SQLServerCertificateDAOImpl.class);

    @Override
    public PaginationResult getAllCertificates(CertificatePaginationRequest request) throws CertificateManagementDAOException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CertificateResponse certificateResponse;
        List<CertificateResponse> certificates = new ArrayList<>();
        PaginationResult paginationResult;
        String serialNumber = request.getSerialNumber();
        String deviceIdentifier = request.getDeviceIdentifier();
        String username = request.getUsername();
        boolean isCertificateSerialNumberProvided = false;
        boolean isCertificateDeviceIdentifierProvided = false;
        boolean isCertificateUsernameProvided = false;

        try {
            Connection conn = this.getConnection();
            String query = "SELECT * " +
                    "FROM DM_DEVICE_CERTIFICATE " +
                    "WHERE TENANT_ID = ? ";
            if (StringUtils.isNotEmpty(serialNumber)) {
                query += "AND SERIAL_NUMBER LIKE ? ";
                isCertificateSerialNumberProvided = true;
            }

            if (StringUtils.isNotEmpty(deviceIdentifier)) {
                query += "AND DEVICE_IDENTIFIER = ? ";
                isCertificateDeviceIdentifierProvided = true;
            }

            if (StringUtils.isNotEmpty(username)) {
                query += "AND USERNAME LIKE ? ";
                isCertificateUsernameProvided = true;
            }

            query += "ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                int paramIdx = 1;
                stmt.setInt(paramIdx++, tenantId);
                if (isCertificateSerialNumberProvided) {
                    stmt.setString(paramIdx++, "%" + serialNumber + "%");
                }
                if (isCertificateDeviceIdentifierProvided) {
                    stmt.setString(paramIdx++, deviceIdentifier);
                }
                if (isCertificateUsernameProvided) {
                    stmt.setString(paramIdx++, "%" + username + "%");
                }
                stmt.setInt(paramIdx++, request.getStartIndex());
                stmt.setInt(paramIdx++, request.getRowCount());
                try (ResultSet resultSet =  stmt.executeQuery()) {
                    while (resultSet.next()) {
                        certificateResponse = new CertificateResponse();
                        byte[] certificateBytes = resultSet.getBytes("CERTIFICATE");
                        certificateResponse.setSerialNumber(resultSet.getString("SERIAL_NUMBER"));
                        certificateResponse.setCertificateId(resultSet.getString("ID"));
                        certificateResponse.setDeviceIdentifier(resultSet.getString("DEVICE_IDENTIFIER"));
                        certificateResponse.setTenantId(resultSet.getInt("TENANT_ID"));
                        certificateResponse.setUsername(resultSet.getString("USERNAME"));
                        CertificateGenerator.extractCertificateDetails(certificateBytes, certificateResponse);
                        certificates.add(certificateResponse);
                    }
                    paginationResult = new PaginationResult();
                    paginationResult.setData(certificates);
                    paginationResult.setRecordsTotal(this.getCertificateCount(request));
                }
            }
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the certificates.";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        }
        return paginationResult;
    }

    private Connection getConnection() throws SQLException {
        return CertificateManagementDAOFactory.getConnection();
    }

    private int getCertificateCount(CertificatePaginationRequest request) throws CertificateManagementDAOException {
        int certificateCount = 0;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String serialNumber = request.getSerialNumber();
        String deviceIdentifier = request.getDeviceIdentifier();
        String username = request.getUsername();

        try {
            Connection conn = this.getConnection();
            String sql = "SELECT COUNT(*) AS DEVICE_CERTIFICATE_COUNT " +
                    "FROM DM_DEVICE_CERTIFICATE " +
                    "WHERE TENANT_ID = ?";

            if (StringUtils.isNotEmpty(serialNumber)) {
                sql += " AND SERIAL_NUMBER LIKE ?";
            }

            if (StringUtils.isNotEmpty(deviceIdentifier)) {
                sql += " AND DEVICE_IDENTIFIER = ?";
            }

            if (StringUtils.isNotEmpty(username)) {
                sql += " AND USERNAME LIKE ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);

                int paramIdx = 2;
                if (StringUtils.isNotEmpty(serialNumber)) {
                    stmt.setString(paramIdx++, "%" + serialNumber + "%");
                }

                if (StringUtils.isNotEmpty(deviceIdentifier)) {
                    stmt.setString(paramIdx++, deviceIdentifier);
                }

                if (StringUtils.isNotEmpty(username)) {
                    stmt.setString(paramIdx, "%" + username + "%");
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        certificateCount = rs.getInt("DEVICE_CERTIFICATE_COUNT");
                    }
                }
            }
        } catch (SQLException e) {
            String errorMsg = "SQL error occurred while retrieving the certificate count.";
            log.error(errorMsg, e);
            throw new CertificateManagementDAOException(errorMsg, e);
        }
        return certificateCount;
    }

}
