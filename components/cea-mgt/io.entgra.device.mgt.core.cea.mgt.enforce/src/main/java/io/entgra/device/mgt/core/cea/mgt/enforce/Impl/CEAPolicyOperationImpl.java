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

package io.entgra.device.mgt.core.cea.mgt.enforce.Impl;

import io.entgra.device.mgt.core.cea.mgt.common.bean.CEAPolicy;
import io.entgra.device.mgt.core.cea.mgt.common.service.CEAEnforcementService;
import io.entgra.device.mgt.core.cea.mgt.enforce.exception.CEAPolicyOperationException;
import io.entgra.device.mgt.core.cea.mgt.enforce.service.CEAPolicyOperation;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.annotation.Enforce;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

public class CEAPolicyOperationImpl implements CEAPolicyOperation {
    private static final Log log = LogFactory.getLog(CEAPolicyOperationImpl.class);
    private final CEAEnforcementService ceaEnforcementService;
    private final CEAPolicy ceaPolicy;

    public CEAPolicyOperationImpl(CEAEnforcementService ceaEnforcementService, CEAPolicy ceaPolicy) {
        this.ceaEnforcementService = ceaEnforcementService;
        this.ceaPolicy = ceaPolicy;
    }

    @Override
    public void enforce() throws CEAPolicyOperationException {
        try {
            Method[] methods = ceaEnforcementService.getClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Enforce.class)) {
                    method.setAccessible(true);
                    method.invoke(ceaEnforcementService, ceaPolicy);
                }
            }
        } catch (Exception e) {
            String msg = "Error occurred while invoking CEA enforcement service";
            log.error(msg, e);
            throw new CEAPolicyOperationException(msg, e);
        }
    }
}
