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
import java.util.ArrayList;
import java.util.List;

/**
 * This DAOUtil class is responsible for making sure single instance of each Extension Manager is used throughout for
 * all the tasks.
 */
public class SPApplicationManagementUtil {

    private static Log log = LogFactory.getLog(SPApplicationManagementUtil.class);

    private static List<String> getDefaultSPAppCategories() {
        List<String> categories = new ArrayList<>();
        categories.add(Constants.SP_APP_CATEGORY);
        return categories;
    }


}
