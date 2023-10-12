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

import io.entgra.device.mgt.core.cea.mgt.common.exception.EnforcementServiceManagerException;
import io.entgra.device.mgt.core.cea.mgt.common.service.CEAEnforcementService;
import io.entgra.device.mgt.core.cea.mgt.common.service.EnforcementServiceManager;
import io.entgra.device.mgt.core.cea.mgt.enforce.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EnforcementServiceManagerImpl implements EnforcementServiceManager {
    private static final Log log = LogFactory.getLog(EnforcementServiceManagerImpl.class);

    @Override
    public CEAEnforcementService getEnforcementService(String enforcementServiceClassName) throws EnforcementServiceManagerException {
        try {
            Class<?> enforcementServiceClass = Class.forName(enforcementServiceClassName);
            Method method = enforcementServiceClass.getMethod(Constants.METHOD_NAME_GET_INSTANCE);
            return (CEAEnforcementService) method.invoke(null);
        } catch (ClassNotFoundException e) {
            String msg = enforcementServiceClassName + " not found";
            log.error(msg, e);
            throw new EnforcementServiceManagerException(msg, e);
        } catch (NoSuchMethodException e) {
            String msg = Constants.METHOD_NAME_GET_INSTANCE + " not found in " + enforcementServiceClassName;
            log.error(msg, e);
            throw new EnforcementServiceManagerException(msg, e);
        } catch (InvocationTargetException e) {
            String msg = "Error occurred while invoking " + Constants.METHOD_NAME_GET_INSTANCE + " in "
                    + enforcementServiceClassName;
            log.error(msg, e);
            throw new EnforcementServiceManagerException(msg, e);
        } catch (IllegalAccessException e) {
            String msg = "Can't access the method " + Constants.METHOD_NAME_GET_INSTANCE + " in "
                    + enforcementServiceClassName;
            log.error(msg, e);
            throw new EnforcementServiceManagerException(msg, e);
        }
    }
}
