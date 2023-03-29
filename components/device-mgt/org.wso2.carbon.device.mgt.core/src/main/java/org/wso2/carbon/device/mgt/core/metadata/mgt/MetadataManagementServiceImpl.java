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

package org.wso2.carbon.device.mgt.core.metadata.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import org.wso2.carbon.device.mgt.common.exceptions.MetadataManagementException;
import org.wso2.carbon.device.mgt.common.exceptions.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.metadata.mgt.Metadata;
import org.wso2.carbon.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import org.wso2.carbon.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import java.sql.SQLException;
import java.util.List;

/**
 * This class implements the MetadataManagementService.
 */
public class MetadataManagementServiceImpl implements MetadataManagementService {

    private static final Log log = LogFactory.getLog(MetadataManagementServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public MetadataManagementServiceImpl() {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }

    @Override
    public Metadata createMetadata(Metadata metadata)
            throws MetadataManagementException, MetadataKeyAlreadyExistsException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Metadata : [" + metadata.toString() + "]");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (metadataDAO.isExist(tenantId, metadata.getMetaKey())) {
                String msg = "Specified metaKey is already exist. {metaKey:" + metadata.getMetaKey() + "}";
                log.error(msg);
                throw new MetadataKeyAlreadyExistsException(msg);
            }
            metadataDAO.addMetadata(tenantId, metadata);
            MetadataManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("Metadata entry created successfully. " + metadata.toString());
            }
            return metadata;
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while creating the metadata entry. " + metadata.toString();
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException("Error occurred while creating metadata record", e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Metadata retrieveMetadata(String metaKey) throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Metadata for metaKey:" + metaKey);
        }
        try {
            MetadataManagementDAOFactory.openConnection();
            int tenantId;
            if (metaKey.equals("EVALUATE_TENANTS")){
                // for getting evaluate tenant list to provide the live chat feature
                 tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                 tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            }
            return metadataDAO.getMetadata(tenantId, metaKey);
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving the metadata entry for metaKey:" + metaKey;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public List<Metadata> retrieveAllMetadata() throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all Metadata entries");
        }
        try {
            MetadataManagementDAOFactory.openConnection();
            PaginationRequest request = new PaginationRequest(0, -1);
            return metadataDAO.getAllMetadata(request,
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving all metadata entries";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult retrieveAllMetadata(PaginationRequest request) throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Metadata entries for given PaginationRequest [rowCount:" +
                    request.getRowCount() + ", startIndex:" + request.getStartIndex() + "]");
        }
        PaginationResult paginationResult = new PaginationResult();
        request = DeviceManagerUtil.validateMetadataListPageSize(request);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.openConnection();
            List<Metadata> metadata = metadataDAO.getAllMetadata(request, tenantId);
            int count = metadataDAO.getMetadataCount(tenantId);
            paginationResult.setData(metadata);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);
            return paginationResult;
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving metadata entries for given PaginationRequest [rowCount:" +
                    request.getRowCount() + ", startIndex:" + request.getStartIndex() + "]";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Metadata updateMetadata(Metadata metadata) throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating Metadata : [" + metadata.toString() + "]");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (metadataDAO.isExist(tenantId, metadata.getMetaKey())) {
                metadataDAO.updateMetadata(tenantId, metadata);
                if (log.isDebugEnabled()) {
                    log.debug("A Metadata entry has updated successfully. " + metadata.toString());
                }
            } else {
                metadataDAO.addMetadata(tenantId, metadata);
                if (log.isDebugEnabled()) {
                    log.debug("Metadata entry has inserted successfully, due to the absence of provided metaKey " +
                            metadata.toString());
                }
            }
            MetadataManagementDAOFactory.commitTransaction();
            return metadata;
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating metadata entry. " + metadata.toString();
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public boolean deleteMetadata(String key) throws MetadataManagementException, MetadataKeyNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting metadata entry. {metaKey:" + key + "}");
        }
        try {
            MetadataManagementDAOFactory.beginTransaction();
            boolean status = metadataDAO.deleteMetadata(
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true), key);
            MetadataManagementDAOFactory.commitTransaction();
            if (status) {
                if (log.isDebugEnabled()) {
                    log.debug("Metadata entry has deleted successfully. {metaKey:" + key + "}");
                }
                return true;
            } else {
                String msg = "Specified Metadata entry has not found. {metaKey:" + key + "}";
                log.error(msg);
                throw new MetadataKeyNotFoundException(msg);
            }
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting metadata entry. {metaKey:" + key + "}";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

}
