/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.AppOperation;
import org.wso2.carbon.device.application.mgt.common.exception.DeviceConnectorException;
import org.wso2.carbon.device.application.mgt.common.services.DeviceConnector;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;

import java.util.ArrayList;
import java.util.List;

public class MAMDeviceConnectorImpl implements DeviceConnector{
    private static final Log log = LogFactory.getLog(MAMDeviceConnectorImpl.class);
    private ApplicationDAO applicationDAO;
    private SubscriptionDAO subscriptionDAO;

    public  MAMDeviceConnectorImpl() {
        this.applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        this.subscriptionDAO = ApplicationManagementDAOFactory.getSubscriptionDAO();
    }

    @Override
    public Boolean sendOperationToDevice(AppOperation appOperation, DeviceIdentifier deviceIdentifier) throws DeviceConnectorException {
        if (String.valueOf(appOperation.getType()).equals("INSTALL")) {

        } else if (String.valueOf(appOperation.getType()).equals("UNINSTALL")) {

        } else if (String.valueOf(appOperation.getType()).equals("UPDATE")) {

        }
        return null;
    }

    @Override
    public Boolean sendOperationToGroup(AppOperation appOperation, String groupID) throws DeviceConnectorException {
        return null;
    }

    @Override
    public Boolean sendOperationToUser(AppOperation appOperation, List<String> userList) throws DeviceConnectorException {
        if (String.valueOf(appOperation.getType()).equals("INSTALL")) {
            //First subscribe the user to the app.
            try {

                subscriptionDAO.subscribeUserToApplication(appOperation.getTenantId(), appOperation.getSubscribedBy(),
                        userList, appOperation.getAppReleaseId());
                for (String username: userList) {
                    List<Device> devices = getDeviceManagementService().getDevicesOfUser(username);
                    List<DeviceIdentifier> deviceIdentifiers = convertDeviceToDeviceIdentifier(devices);
//                    getDeviceManagementService().addOperation(appOperation.getApplication().getDeviceTypeName(),
//                            operationEKA, devices);
                    subscriptionDAO.subscribeDeviceToApplicationTmp(appOperation.getTenantId(), appOperation.getSubscribedBy(),
                            devices, appOperation.getApplication().getId(), appOperation.getAppReleaseId(),
                            String.valueOf(AppOperation.InstallState.PENDING));
                }
            } catch (ApplicationManagementDAOException e) {
                String msg = "Error subscribing the user to the application Id" + appOperation.getApplication().getId();
                log.error(msg, e);
                throw new DeviceConnectorException(msg, e);
            } catch (DeviceManagementException e) {
                String msg = "Error getting the list of user devices.";
                log.error(msg, e);
                throw new DeviceConnectorException(msg, e);
            }

        } else if (String.valueOf(appOperation.getType()).equals("UNINSTALL")) {

        } else if (String.valueOf(appOperation.getType()).equals("UPDATE")) {

        }
        return null;
    }

    @Override
    public Boolean sendOperationToRole(AppOperation appOperation, String role) throws DeviceConnectorException {
        return null;
    }

    private List<DeviceIdentifier> convertDeviceToDeviceIdentifier(List<Device> devices) {
        List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
        for (Device device:devices) {
            deviceIdentifiers.add(new DeviceIdentifier(device.getDeviceIdentifier(), device.getType()));
        }
        return  deviceIdentifiers;
    }

    public DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "DeviceImpl Management provider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }
}
