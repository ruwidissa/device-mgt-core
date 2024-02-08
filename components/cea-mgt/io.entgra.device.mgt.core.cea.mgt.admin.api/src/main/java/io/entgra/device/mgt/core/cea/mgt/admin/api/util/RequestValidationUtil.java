/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.admin.api.util;

import io.entgra.device.mgt.core.cea.mgt.admin.api.bean.AccessPolicyWrapper;
import io.entgra.device.mgt.core.cea.mgt.admin.api.bean.CEAPolicyWrapper;
import io.entgra.device.mgt.core.cea.mgt.admin.api.bean.GracePeriodWrapper;
import io.entgra.device.mgt.core.cea.mgt.admin.api.exception.BadRequestException;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ActiveSyncServer;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.DefaultAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.EmailOutlookAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.GraceAllowedPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.POPIMAPAccessPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.enums.WebOutlookAccessPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestValidationUtil {
    private static final Log log = LogFactory.getLog(RequestValidationUtil.class);

    /**
     * Validate conditional access policy
     * @param ceaPolicyWrapper {@link CEAPolicyWrapper}
     */
    public static void validateCEAPolicy(CEAPolicyWrapper ceaPolicyWrapper) {
        if (ceaPolicyWrapper == null) {
            String msg = "CEA policy should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        validateActiveSyncServer(ceaPolicyWrapper.getActiveSyncServerEntries());
        validateCEAAccessPolicy(ceaPolicyWrapper.getConditionalAccessPolicyEntries());
        validateCEAGracePeriod(ceaPolicyWrapper.getGracePeriodEntries());
    }

    /**
     * Validate active sync server configurations
     * @param activeSyncServer {@link ActiveSyncServer}
     */
    public static void validateActiveSyncServer(ActiveSyncServer activeSyncServer) {
        if (activeSyncServer == null) {
            String msg = "Active sync server should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (activeSyncServer.getGatewayUrl() == null) {
            String msg = "Active sync server url should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (activeSyncServer.getKey() == null) {
            String msg = "Active sync server type should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (activeSyncServer.getClient() == null) {
            String msg = "Active sync server username should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (activeSyncServer.getSecret() == null) {
            String msg = "Active sync server secret should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    /**
     * Validate conditional access policy configurations
     * @param accessPolicyWrapper {@link AccessPolicyWrapper}
     */
    public static void validateCEAAccessPolicy(AccessPolicyWrapper accessPolicyWrapper) {
        if (accessPolicyWrapper == null) {
            String msg = "Access policy should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            Enum.valueOf(DefaultAccessPolicy.class, accessPolicyWrapper.getDefaultAccessPolicy());
            Enum.valueOf(WebOutlookAccessPolicy.class, accessPolicyWrapper.getWebOutlookAccessPolicy());
            Enum.valueOf(POPIMAPAccessPolicy.class, accessPolicyWrapper.getPOPIMAPAccessPolicy());
            for(String value : accessPolicyWrapper.getEmailOutlookAccessPolicy()) {
                Enum.valueOf(EmailOutlookAccessPolicy.class, value);
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            String msg = "Access policy contains illegal arguments";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    /**
     * Validate grace period configurations
     * @param gracePeriodWrapper {@link GracePeriodWrapper}
     */
    public static void validateCEAGracePeriod(GracePeriodWrapper gracePeriodWrapper) {
        if (gracePeriodWrapper == null) {
            String msg = "Grace period should not be null";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (gracePeriodWrapper.getGracePeriod() < 0 || gracePeriodWrapper.getGracePeriod() >
                io.entgra.device.mgt.core.cea.mgt.common.util.Constants.MAX_GRACE_PERIOD_IN_DAYS) {
            String msg = "Grace period should in range of 0-30 days";
            log.error(msg);
            throw new BadRequestException(msg);
        }
        try {
            Enum.valueOf(GraceAllowedPolicy.class, gracePeriodWrapper.getGraceAllowedPolicy());
        } catch (IllegalArgumentException | NullPointerException e) {
            String msg = "Grace allowed policy contains illegal arguments";
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }
}
