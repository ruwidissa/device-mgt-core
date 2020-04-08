/*
 *  Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.metadata.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.util.MetadataDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the implementation of MetadataDAO which can be used to support SQLServer db syntax.
 */
public class SQLServerMetadataDAOImpl extends AbstractMetadataDAOImpl {

    private static final Log log = LogFactory.getLog(SQLServerMetadataDAOImpl.class);

    @Override
    public List<Metadata> getAllMetadata(PaginationRequest request, int tenantId)
            throws MetadataManagementDAOException {
        List<Metadata> metadata;
        String sql = "SELECT DATA_TYPE, METADATA_KEY, METADATA_VALUE " +
                "FROM DM_METADATA " +
                "WHERE TENANT_ID = ? " +
                "ORDER BY METADATA_KEY";
        if (request.getRowCount() != -1) {
            sql = sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        }
        try {
            Connection conn = MetadataManagementDAOFactory.getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, tenantId);
                if (request.getRowCount() != -1) {
                    stmt.setInt(2, request.getStartIndex());
                    stmt.setInt(3, request.getRowCount());
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    metadata = new ArrayList<>();
                    while (rs.next()) {
                        metadata.add(MetadataDAOUtil.getMetadata(rs));
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving all metadata";
            log.error(msg, e);
            throw new MetadataManagementDAOException(msg, e);
        }
        return metadata;
    }

}
