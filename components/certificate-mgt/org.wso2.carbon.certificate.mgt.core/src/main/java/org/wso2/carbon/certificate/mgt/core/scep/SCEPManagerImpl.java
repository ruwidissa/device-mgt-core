/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 *  Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 *  Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.certificate.mgt.core.scep;

import org.apache.commons.collections.map.SingletonMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.internal.CertificateManagementDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class SCEPManagerImpl implements SCEPManager {
    private static final Log log = LogFactory.getLog(SCEPManagerImpl.class);
    DeviceManagementProviderService dms;

    public SCEPManagerImpl() {
        this.dms = CertificateManagementDataHolder.getInstance().getDeviceManagementService();
    }

    @Override
    public TenantedDeviceWrapper getValidatedDevice(DeviceIdentifier deviceIdentifier)
            throws SCEPException {
        SingletonMap deviceMap;

        try {
            deviceMap = dms.getTenantedDevice(deviceIdentifier, false);
            if (deviceMap == null) {
                String msg = "Lookup device not found for the device identifier " + deviceIdentifier.getId() +
                             " of device type " + deviceIdentifier.getType();
                log.error(msg);
                throw new SCEPException(msg);
            }

        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting device " + deviceIdentifier;
            log.error(msg);
            throw new SCEPException(msg, e);
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            ctx.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            ctx.setTenantId(MultitenantConstants.SUPER_TENANT_ID);

            RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
            if (realmService == null) {
                String msg = "RealmService is not initialized";
                log.error(msg);
                throw new SCEPException(msg);
            }
            TenantedDeviceWrapper tenantedDeviceWrapper = new TenantedDeviceWrapper();
            int tenantId = (int) deviceMap.getKey();
            String tenantDomain = realmService.getTenantManager().getDomain(tenantId);

            tenantedDeviceWrapper.setTenantId(tenantId);
            tenantedDeviceWrapper.setTenantDomain(tenantDomain);
            tenantedDeviceWrapper.setDevice((Device) deviceMap.getValue());

            return tenantedDeviceWrapper;

        } catch (UserStoreException e) {
            String msg = "Error occurred while getting the tenant domain.";
            log.error(msg);
            throw new SCEPException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
