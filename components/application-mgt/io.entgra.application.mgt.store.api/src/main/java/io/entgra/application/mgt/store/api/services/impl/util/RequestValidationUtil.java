/*
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.entgra.application.mgt.store.api.services.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.core.exception.BadRequestException;
import io.entgra.application.mgt.store.api.util.Constants;

import java.util.List;

public class RequestValidationUtil {

    private static final Log log = LogFactory.getLog(RequestValidationUtil.class);

    /**
     * Checks if user requested status codes are valid.
     *
     * @param statusList status codes upon to filter operation logs using status
     */
    public static void validateStatus(List<String> statusList) throws BadRequestException {
        for (String status : statusList) {
            switch (status) {
                case "ACTIVE":
                case "INACTIVE":
                case "UNCLAIMED":
                case "UNREACHABLE":
                case "SUSPENDED":
                case "DISENROLLMENT_REQUESTED":
                case "REMOVED":
                case "BLOCKED":
                case "CREATED":
                    break;
                default:
                    String msg = "Invalid enrollment status type: " + status + ". \nValid status types " +
                            "are ACTIVE | INACTIVE | UNCLAIMED | UNREACHABLE | SUSPENDED | " +
                            "DISENROLLMENT_REQUESTED | REMOVED | BLOCKED | CREATED";
                    log.error(msg);
                    throw new BadRequestException(msg);
            }
        }
    }

    /**
     * Checks if user requested action is valid.
     *
     * @param action action upon to filter devices using action
     */
    public static void validateAction(String action) throws BadRequestException {
        if (action.equals("SUBSCRIBED") || action.equals("UNSUBSCRIBED")) {
        } else {
            String msg = "Invalid action type received.Valid action types are SUBSCRIBED | UNSUBSCRIBED";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    /**
     * Checks if user requested ownerships are valid.
     *
     * @param ownership ownerships upon to filter devices using ownership
     */
    public static void validateOwnershipType(String ownership) throws BadRequestException {
        switch (ownership) {
            case "BYOD":
            case "COPE":
            case "WORK_PROFILE":
            case "GOOGLE_ENTERPRISE":
            case "COSU":
            case "FULLY_MANAGED":
            case "DEDICATED_DEVICE":
                break;
            default:
                String msg = "Invalid ownership type received.Valid ownership types are BYOD | COPE | WORK_PROFILE |" +
                        "GOOGLE_ENTERPRISE | COSU | FULLY_MANAGED | DEDICATED_DEVICE";
                log.error(msg);
                throw new BadRequestException(msg);
        }
    }

    /**
     * Checks if user requested Action status codes are valid.
     *
     * @param status status codes upon to filter operation logs using status
     */
    public static void validateStatusFiltering(String status) throws BadRequestException {
        if (Constants.OperationStatus.COMPLETED.toUpperCase().equals(status)
                || Constants.OperationStatus.ERROR.toUpperCase().equals(status)
                || Constants.OperationStatus.NOTNOW.toUpperCase().equals(status)
                || Constants.OperationStatus.REPEATED.toUpperCase().equals(status)
                || Constants.OperationStatus.PENDING.toUpperCase().equals(status)
                || Constants.OperationStatus.IN_PROGRESS.toUpperCase().equals(status)) {
        } else {
            String msg = "Invalid status type: " + status + ". \nValid status types are COMPLETED | ERROR | " +
                    "IN_PROGRESS | NOTNOW | PENDING | REPEATED";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }
}
