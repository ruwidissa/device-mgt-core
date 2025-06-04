/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.core.common.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SystemPropertyUtil {
    private static final Log log = LogFactory.getLog(SystemPropertyUtil.class);

    /**
     * Retrieves the system property for the given key, ensuring it is non-null, non-empty, and contains no whitespace.
     * This method is intended to be used after the server is fully started and all system properties are initialized.
     * @param key the name of the system property
     * @return the validated property value
     * @throws IllegalStateException if the property is missing or invalid
     */
    public static String getRequiredProperty(String key) {
        String value = System.getProperty(key, "");
        if (StringUtils.isBlank(value)) {
            String msg = "Required system property '" + key + "' is not set or contains only whitespace.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return value;
    }
}
