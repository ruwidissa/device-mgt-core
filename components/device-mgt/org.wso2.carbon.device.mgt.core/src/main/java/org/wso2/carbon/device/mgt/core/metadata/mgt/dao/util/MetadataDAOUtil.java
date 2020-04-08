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

package org.wso2.carbon.device.mgt.core.metadata.mgt.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;

import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * This class includes the utility methods required by MetadataMgmt functionalities.
 */
public class MetadataDAOUtil {

    private static final Log log = LogFactory.getLog(MetadataDAOUtil.class);

    /**
     * Populates {@link Metadata} object with the result obtained from the database.
     *
     * @param rs    {@link ResultSet} obtained from the database
     * @return      {@link Metadata} object populated with the data
     * @throws SQLException If unable to populate {@link Metadata} object with the data
     */
    public static Metadata getMetadata(ResultSet rs) throws SQLException {
        Metadata metadata = new Metadata();
        metadata.setMetaKey(rs.getString("METADATA_KEY"));
        metadata.setMetaValue(rs.getString("METADATA_VALUE"));
        metadata.setDataType(rs.getString("DATA_TYPE"));
        return metadata;
    }
}
