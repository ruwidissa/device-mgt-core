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

package io.entgra.device.mgt.core.device.mgt.core.metadata.mgt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.AllowedDeviceStatus;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.DeviceStatusManagementService;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.DeviceStatusConfigurations;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.DeviceStatusItem;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.UIConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.util.MetadataConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DeviceStatusManagementServiceImpl implements DeviceStatusManagementService {

    private static final Log log = LogFactory.getLog(DeviceStatusManagementServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public DeviceStatusManagementServiceImpl() {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }

    @Override
    public void addDefaultDeviceStatusFilterIfNotExist(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY) && !metadataDAO.isExist(tenantId, MetadataConstants.IS_DEVICE_STATUS_CHECK_META_KEY)) {
                Metadata defaultDeviceStatusMetadata = constructDeviceStatusMetadata(getDefaultDeviceStatus());
                Metadata defaultDeviceStatusCheckMetadata = constructDeviceStatusCheckMetadata(getDefaultDeviceStatusCheck());
                // Add default device status and device status check metadata entries
                addMetadataEntry(tenantId, defaultDeviceStatusMetadata, MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY);
                addMetadataEntry(tenantId, defaultDeviceStatusCheckMetadata, MetadataConstants.IS_DEVICE_STATUS_CHECK_META_KEY);
            }
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while inserting default device status metadata entry.";
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
    public void resetToDefaultDeviceStausFilter() throws MetadataManagementException {

    }

    @Override
    public void updateDefaultDeviceStatusFilters(int tenantId, String deviceType, List<String> deviceStatus) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            // Retrieve the current device status metadata
            Metadata metadata = metadataDAO.getMetadata(tenantId, MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY);
            if (metadata != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<AllowedDeviceStatus>>() {
                }.getType();
                List<AllowedDeviceStatus> currentStatusList = gson.fromJson(metadata.getMetaValue(), listType);

                // Find the status for the specified deviceType
                for (AllowedDeviceStatus status : currentStatusList) {
                    if (status.getType().equalsIgnoreCase(deviceType)) {
                        // Update the status list for the specified deviceType
                        status.setStatus(deviceStatus);
                        break;
                    }
                }
                metadata.setMetaValue(gson.toJson(currentStatusList));
                updateMetadataEntry(tenantId, metadata, MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY);
            }
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating device status metadata entry.";
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
    public boolean updateDefaultDeviceStatusCheck(int tenantId, boolean isChecked) throws MetadataManagementException {
        boolean success = false;
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (metadataDAO.isExist(tenantId, MetadataConstants.IS_DEVICE_STATUS_CHECK_META_KEY)) {
                Metadata isDeviceStatusChecked = constructDeviceStatusCheckMetadata(isChecked);
                // Add default device status check metadata entries
                updateMetadataEntry(tenantId, isDeviceStatusChecked, MetadataConstants.IS_DEVICE_STATUS_CHECK_META_KEY);
                success = true;
            }
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating device status check metadata entry.";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
        return success;
    }

    @Override
    public List<AllowedDeviceStatus> getDeviceStatusFilters(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.openConnection();
            Metadata metadata = metadataDAO.getMetadata(tenantId, MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<AllowedDeviceStatus>>() {}.getType();
            List<AllowedDeviceStatus> statusList = gson.fromJson(metadata.getMetaValue(), listType);

            return statusList;
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving device status meta data for tenant:" + tenantId;
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

    public List<String> getDeviceStatusFilters(String deviceType, int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.openConnection();
            Metadata metadata = metadataDAO.getMetadata(tenantId, MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<AllowedDeviceStatus>>() {}.getType();
            List<AllowedDeviceStatus> statusList = gson.fromJson(metadata.getMetaValue(), listType);

            for (AllowedDeviceStatus status : statusList) {
                if (status.getType().equalsIgnoreCase(deviceType)) {
                    return status.getStatus();
                }
            }
            // Device type not found in metadata
            return Collections.emptyList();
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving device status meta data for tenant: " + tenantId;
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
    public boolean getDeviceStatusCheck(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.openConnection();
            Metadata metadata = metadataDAO.getMetadata(tenantId, MetadataConstants.IS_DEVICE_STATUS_CHECK_META_KEY);
            String metaValue = metadata.getMetaValue();
            return Boolean.parseBoolean(metaValue);
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving device status check meta data for tenant:" + tenantId;
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
    public boolean isDeviceStatusValid(String deviceType, String deviceStatus, int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.openConnection();
            Metadata metadata = metadataDAO.getMetadata(tenantId, MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<AllowedDeviceStatus>>() {
            }.getType();
            List<AllowedDeviceStatus> statusList = gson.fromJson(metadata.getMetaValue(), listType);

            for (AllowedDeviceStatus status : statusList) {
                if (status.getType().equalsIgnoreCase(deviceType)) {
                    List<String> allowedStatus = status.getStatus();
                    return allowedStatus.contains(deviceStatus);
                }
            }

            return false; // Device type not found in metadata
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving device status meta data for tenant: " + tenantId;
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

    private void addMetadataEntry(int tenantId, Metadata metadata, String key) throws MetadataManagementDAOException {
        metadataDAO.addMetadata(tenantId, metadata);
        if (log.isDebugEnabled()) {
            log.debug(key + " metadata entry has been inserted successfully");
        }
    }

    private void updateMetadataEntry(int tenantId, Metadata metadata, String key) throws MetadataManagementDAOException {
        metadataDAO.updateMetadata(tenantId, metadata);
        if (log.isDebugEnabled()) {
            log.debug(key + " metadata entry has been updated successfully");
        }
    }

    private Metadata constructDeviceStatusMetadata(List<DeviceStatusItem> deviceStatusItems) {
        Gson gson = new Gson();
        String deviceStatusItemsJsonString = gson.toJson(deviceStatusItems);

        Metadata metadata = new Metadata();
        metadata.setMetaKey(MetadataConstants.ALLOWED_DEVICE_STATUS_META_KEY);
        metadata.setMetaValue(deviceStatusItemsJsonString);

        return metadata;
    }

    private Metadata constructDeviceStatusCheckMetadata(boolean deviceStatusCheck) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("deviceStatusCheck", String.valueOf(deviceStatusCheck));
        Metadata metadata = new Metadata();
        metadata.setMetaKey(MetadataConstants.IS_DEVICE_STATUS_CHECK_META_KEY);
        metadata.setMetaValue(String.valueOf(deviceStatusCheck));

        return metadata;
    }

    private List<DeviceStatusItem> getDefaultDeviceStatus() {
        DeviceStatusConfigurations deviceStatusConfigurations = UIConfigurationManager.getInstance().getUIConfig().getDeviceStatusConfigurations();
        List<DeviceStatusItem> deviceStatusItems = new ArrayList<>();

        if (deviceStatusConfigurations != null) {
            // Access the list of DeviceStatusItem objects
            deviceStatusItems = deviceStatusConfigurations.getDeviceStatusItems();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("DeviceStatusConfigurations is null.");
            }
        }

        return deviceStatusItems;
    }

    private boolean getDefaultDeviceStatusCheck() {
        DeviceStatusConfigurations deviceStatusConfigurations = UIConfigurationManager.getInstance().getUIConfig().getDeviceStatusConfigurations();
        boolean deviceStatusCheck = false;

        if (deviceStatusConfigurations != null) {
            // Access the deviceStatusCheck
            deviceStatusCheck = deviceStatusConfigurations.isDeviceStatusCheck();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("DeviceStatusConfigurations is null.");
            }
        }
        return deviceStatusCheck;
    }
}
