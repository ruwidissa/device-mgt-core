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

package io.entgra.device.mgt.core.cea.mgt.core.impl;

import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.bean.ui.CEAPolicyUIConfiguration;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAManagementException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyAlreadyExistsException;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAPolicyNotFoundException;
import io.entgra.device.mgt.core.cea.mgt.common.service.CEAManagementService;
import io.entgra.device.mgt.core.cea.mgt.core.mgt.CEAManager;
import io.entgra.device.mgt.core.cea.mgt.core.mgt.impl.CEAManagerImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CEAManagementServiceImpl implements CEAManagementService {
    private static final Log log = LogFactory.getLog(CEAManagementServiceImpl.class);

    private final CEAManager ceaManager;

    public CEAManagementServiceImpl() {
        ceaManager = CEAManagerImpl.getInstance();
    }

    @Override
    public CEAPolicyUIConfiguration getCEAPolicyUIConfiguration() throws CEAManagementException {
        return ceaManager.getCEAPolicyUIConfiguration();
    }

    @Override
    public CEAPolicy createCEAPolicy(CEAPolicy ceaPolicy) throws CEAManagementException,
            CEAPolicyAlreadyExistsException {
        return ceaManager.createCEAPolicy(ceaPolicy);
    }

    @Override
    public CEAPolicy retrieveCEAPolicy() throws CEAManagementException {
        return ceaManager.retrieveCEAPolicy();
    }

    @Override
    public CEAPolicy updateCEAPolicy(CEAPolicy ceaPolicy) throws CEAManagementException, CEAPolicyNotFoundException {
        return ceaManager.updateCEAPolicy(ceaPolicy);
    }

    @Override
    public void deleteCEAPolicy() throws CEAManagementException, CEAPolicyNotFoundException {
        ceaManager.deleteCEAPolicy();
    }

    @Override
    public void syncNow() throws CEAManagementException {
        ceaManager.syncNow();
    }
}
