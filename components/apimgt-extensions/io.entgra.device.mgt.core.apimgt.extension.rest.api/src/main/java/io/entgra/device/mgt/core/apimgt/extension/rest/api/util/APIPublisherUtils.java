/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.apimgt.extension.rest.api.util;

import io.entgra.device.mgt.core.apimgt.extension.rest.api.constants.Constants;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This class contains utility methods needed for API publishing
 */
public class APIPublisherUtils {
    private static final Log log = LogFactory.getLog(APIPublisherUtils.class);

    /**
     * This method will create the temporary user created to publish scopes to the sub tenant space.
     * @param tenantDomain sub tenant domain from which the user will be created
     * @throws APIServicesException if the user was unable to be created
     */
    public static void createScopePublishUserIfNotExists(String tenantDomain) throws APIServicesException {
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                UserStoreManager userStoreManager =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
                if (!userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(Constants.SCOPE_PUBLISH_RESERVED_USER_NAME))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating scope publish user '" + Constants.SCOPE_PUBLISH_RESERVED_USER_NAME + "' in '" +
                                tenantDomain + "' tenant domain.");
                    }
                    String[] roles = {Constants.ADMIN_ROLE_KEY};
                    userStoreManager.addUser(
                            MultitenantUtils.getTenantAwareUsername(Constants.SCOPE_PUBLISH_RESERVED_USER_NAME),
                            Constants.SCOPE_PUBLISH_RESERVED_USER_PASSWORD,
                            roles,
                            null,
                            ""
                    );
                }
            } catch (UserStoreException e) {
                String msg = "Error occurred while creating scope publishing user in tenant: '" + tenantDomain + "'.";
                log.error(msg);
                throw new APIServicesException(msg, e);
            }
        }
    }

    /**
     * This method will delete the temporary user created to publish scopes to the sub tenant space.
     * @param tenantDomain sub tenant domain from which the scope publish user will be removed from
     */
    public static void removeScopePublishUserIfExists(String tenantDomain) {
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                UserStoreManager userStoreManager =
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
                if (userStoreManager.isExistingUser(MultitenantUtils.getTenantAwareUsername(Constants.SCOPE_PUBLISH_RESERVED_USER_NAME))) {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting scope publish user '" + Constants.SCOPE_PUBLISH_RESERVED_USER_NAME + "' from '" +
                                tenantDomain + "' tenant domain.");
                    }
                    userStoreManager.deleteUser(MultitenantUtils.getTenantAwareUsername(Constants.SCOPE_PUBLISH_RESERVED_USER_NAME));
                }
            } catch(UserStoreException e){
                String msg = "Error occurred while deleting scope publishing user from tenant: '" + tenantDomain + "'.";
                log.error(msg);
            }
        }
    }
}
