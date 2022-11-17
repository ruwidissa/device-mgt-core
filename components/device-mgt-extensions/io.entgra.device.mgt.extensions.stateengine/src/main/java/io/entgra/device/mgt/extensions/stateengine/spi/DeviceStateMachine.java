/*
 * Copyright (C) 2018 - 2021 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.extensions.stateengine.spi;

import io.entgra.device.mgt.extensions.stateengine.dto.StateUpdateResult;
import io.entgra.device.mgt.extensions.stateengine.exception.StateValidationException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;

public interface DeviceStateMachine {

    StateUpdateResult updateState(EnrolmentInfo.Status oldStatus,
                                  EnrolmentInfo.Status newStatus, Device oldDevice, Device updatedDevice)
            throws StateValidationException;

    StateUpdateResult revertState(EnrolmentInfo.Status oldStatus,
                                  EnrolmentInfo.Status newStatus, Device oldDevice, Device updatedDevice)
            throws StateValidationException;

}
