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

import io.entgra.device.mgt.core.cea.mgt.common.service.CEAManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class CEAManagementApiUtil {
    private static volatile CEAManagementService ceaManagementService;

    public static CEAManagementService getCEAManagementService() {
        if (ceaManagementService == null) {
            synchronized (CEAManagementApiUtil.class) {
                if (ceaManagementService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    ceaManagementService = (CEAManagementService)
                            ctx.getOSGiService(CEAManagementService.class, null);
                    if (ceaManagementService == null) {
                        throw new IllegalStateException("Conditional Email Access Management Service is not initialize");
                    }
                }
            }
        }
        return ceaManagementService;
    }
}
