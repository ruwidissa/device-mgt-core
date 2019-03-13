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

/***
 * The EnrollmentNotifierException wraps all unchecked standard Java exception and this could be thrown if error occurs
 * while notifying enrollment for defined endpoint and also if and only if enrollment notification is enabled.
 *
 */
public class EnrollmentNotifierException extends Exception {

    private static final long serialVersionUID = -5980273112833902095L;

    public EnrollmentNotifierException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public EnrollmentNotifierException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnrollmentNotifierException(String msg) {
        super(msg);
    }

    public EnrollmentNotifierException() {
        super();
    }

    public EnrollmentNotifierException(Throwable cause) {
        super(cause);
    }
}
