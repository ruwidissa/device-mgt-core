/*
 * Copyright (C) 2018 - 2023 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.operation.template.util;

import io.entgra.device.mgt.operation.template.exception.BadOperationRequestException;
import io.entgra.device.mgt.operation.template.exception.OperationTemplateMgtPluginException;
import org.apache.commons.lang.StringUtils;

public class AssertUtils {

    private AssertUtils() {
        throw new IllegalStateException("ValidationUtils class");
    }

    /**
     * @param object
     * @param msg
     * @throws OperationTemplateMgtPluginException
     */
    public static void isNull(Object object, String msg) throws OperationTemplateMgtPluginException {
        if (object == null) {
            throw new BadOperationRequestException(msg);
        }
    }

    /**
     * @param object
     * @param msg
     * @throws OperationTemplateMgtPluginException
     */
    public static void notNull(Object object, String msg) throws OperationTemplateMgtPluginException {
        if (object != null) {
            throw new BadOperationRequestException(msg);
        }
    }

    /**
     * @param text
     * @param msg
     * @throws OperationTemplateMgtPluginException
     */
    public static void hasText(String text, String msg) throws OperationTemplateMgtPluginException {
        if (StringUtils.isEmpty(text)) {
            throw new BadOperationRequestException(msg);
        }
    }

    /**
     * @param expression
     * @param msg
     * @throws OperationTemplateMgtPluginException
     */
    public static void isTrue(boolean expression, String msg) throws OperationTemplateMgtPluginException {
        if (!expression) {
            throw new BadOperationRequestException(msg);
        }
    }
}
