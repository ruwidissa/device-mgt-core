/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.application.mgt.common.services;

import io.entgra.application.mgt.common.AppOperation;
import io.entgra.application.mgt.common.exception.DeviceConnectorException;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

/**
 * This interface contains operations necessary to perform actions on a device such as install and application on a
 * device, uninstall or upgrade. This must be implemented to connect to an external device management server.
 */
public interface DeviceConnector {

    Boolean sendOperationToDevice(AppOperation appOperation, DeviceIdentifier deviceIdentifier) throws
            DeviceConnectorException;

    Boolean sendOperationToGroup(AppOperation appOperation, String groupID) throws DeviceConnectorException;

    Boolean sendOperationToUser(AppOperation appOperation, List<String> userList) throws DeviceConnectorException;

    Boolean sendOperationToRole(AppOperation appOperation, String role) throws DeviceConnectorException;

}
