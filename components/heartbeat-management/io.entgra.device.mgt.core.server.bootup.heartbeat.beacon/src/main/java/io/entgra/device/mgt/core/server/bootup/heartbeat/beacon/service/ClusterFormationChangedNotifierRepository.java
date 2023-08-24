/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClusterFormationChangedNotifierRepository {

    private Map<String, ClusterFormationChangedNotifier> notifiers;
    private static final Log log = LogFactory.getLog(ClusterFormationChangedNotifierRepository.class);

    public ClusterFormationChangedNotifierRepository() {
        this.notifiers = new ConcurrentHashMap<>();
    }

    public void addNotifier(ClusterFormationChangedNotifier notifier) {
        notifiers.put(notifier.getType(), notifier);
    }

    public void addNotifier(String className) {
        try {
            if (!StringUtils.isEmpty(className)) {
                Class<?> clz = Class.forName(className);
                ClusterFormationChangedNotifier notifier = (ClusterFormationChangedNotifier) clz.newInstance();
                notifiers.put(notifier.getType(), notifier);
            }
        } catch (ClassNotFoundException e) {
            log.error("Provided ClusterFormationChangedNotifier implementation '" + className + "' cannot be found", e);
        } catch (InstantiationException e) {
            log.error("Error occurred while instantiating ClusterFormationChangedNotifier implementation '" +
                    className + "'", e);
        } catch (IllegalAccessException e) {
            log.error("Error occurred while adding ClusterFormationChangedNotifier implementation '" + className + "'", e);
        }
    }

    public ClusterFormationChangedNotifier getNotifier(String type) {
        return notifiers.get(type);
    }

    public Map<String, ClusterFormationChangedNotifier> getNotifiers() {
        return notifiers;
    }
}
