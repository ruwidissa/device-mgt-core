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

package io.entgra.device.mgt.core.application.mgt.core.util.subscription.mgt.service;

import io.entgra.device.mgt.core.application.mgt.common.SubscriptionInfo;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionResponse;
import io.entgra.device.mgt.core.application.mgt.common.SubscriptionStatistics;
import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.device.mgt.core.application.mgt.core.dao.ApplicationDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.ApplicationReleaseDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.SubscriptionDAO;
import io.entgra.device.mgt.core.application.mgt.core.dao.common.ApplicationManagementDAOFactory;

public interface SubscriptionManagementHelperService {
    SubscriptionDAO subscriptionDAO = ApplicationManagementDAOFactory.getSubscriptionDAO();
    ApplicationDAO applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
    ApplicationReleaseDAO applicationReleaseDAO = ApplicationManagementDAOFactory.getApplicationReleaseDAO();

    SubscriptionResponse getStatusBaseSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException;

    SubscriptionResponse getSubscriptions(SubscriptionInfo subscriptionInfo, int limit, int offset)
            throws ApplicationManagementException;

    SubscriptionStatistics getSubscriptionStatistics(SubscriptionInfo subscriptionInfo)
            throws ApplicationManagementException;
}
