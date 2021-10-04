/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.application.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.application.mgt.common.exception.InvalidConfigurationException;
import io.entgra.application.mgt.common.services.ApplicationManager;
import io.entgra.application.mgt.common.services.ApplicationStorageManager;
import io.entgra.application.mgt.common.services.ReviewManager;
import io.entgra.application.mgt.common.services.SubscriptionManager;
import io.entgra.application.mgt.core.config.ConfigurationManager;
import io.entgra.application.mgt.core.config.Extension;
import io.entgra.application.mgt.core.lifecycle.LifecycleStateManager;

import java.lang.reflect.Constructor;

/**
 * This DAOUtil class is responsible for making sure single instance of each Extension Manager is used throughout for
 * all the tasks.
 */
public class ApplicationManagementUtil {

    private static Log log = LogFactory.getLog(ApplicationManagementUtil.class);

    public static ApplicationManager getApplicationManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ApplicationManager);
        return getInstance(extension, ApplicationManager.class);
    }

    public static ReviewManager getReviewManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ReviewManager);
        return getInstance(extension, ReviewManager.class);
    }

    public static SubscriptionManager getSubscriptionManagerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.SubscriptionManager);
        return getInstance(extension, SubscriptionManager.class);
    }

    public static ApplicationStorageManager getApplicationStorageManagerInstance() throws
            InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.ApplicationStorageManager);
        return getInstance(extension, ApplicationStorageManager.class);
    }

    public static LifecycleStateManager getLifecycleStateMangerInstance() throws InvalidConfigurationException {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        Extension extension = configurationManager.getExtension(Extension.Name.LifecycleStateManager);
        return getInstance(extension, LifecycleStateManager.class);
    }

    private static <T> T getInstance(Extension extension, Class<T> cls) throws InvalidConfigurationException {
        try {
            Class theClass = Class.forName(extension.getClassName());
            if (extension.getParameters() != null && extension.getParameters().size() > 0) {
                Class[] types = new Class[extension.getParameters().size()];
                Object[] paramValues = new String[extension.getParameters().size()];
                for (int i = 0; i < extension.getParameters().size(); i++) {
                    types[i] = String.class;
                    paramValues[i] = extension.getParameters().get(i).getValue();
                }
                Constructor<T> constructor = theClass.getConstructor(types);
                return constructor.newInstance(paramValues);
            } else {
                Constructor<T> constructor = theClass.getConstructor();
                return constructor.newInstance();
            }
        } catch (Exception e) {
            String msg = "Unable to get instance of extension - " + extension.getName() + " , for class - " + extension
                    .getClassName();
            log.error(msg, e);
            throw new InvalidConfigurationException(msg, e);
        }
    }
}
