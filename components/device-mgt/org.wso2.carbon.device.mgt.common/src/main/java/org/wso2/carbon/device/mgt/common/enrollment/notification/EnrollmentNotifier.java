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

package org.wso2.carbon.device.mgt.common.enrollment.notification;

import org.wso2.carbon.device.mgt.common.Device;

/***
 *
 */
public interface EnrollmentNotifier {

    /***
     * notify method could be used to notify an enrollment of IoTS to a desired endpoint. This method could
     * be invoked when a successful new enrollment completes.
     *
     * @throws EnrollmentNotifierException, if an error occurs while notify the enrollment to a defined end point
     *
     */
    void notify(Device device) throws EnrollmentNotifierException;
}
