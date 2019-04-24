/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.application.mgt.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.Filter;
import org.wso2.carbon.device.application.mgt.common.services.*;
import org.wso2.carbon.device.application.mgt.common.ErrorResponse;

import javax.ws.rs.core.Response;


/**
 * Holds util methods required for ApplicationDTO-Mgt API component.
 */
public class APIUtil {

    private static Log log = LogFactory.getLog(APIUtil.class);

    private static ApplicationManager applicationManager;
    private static ApplicationStorageManager applicationStorageManager;
    private static SubscriptionManager subscriptionManager;
    private static ReviewManager reviewManager;
    private static AppmDataHandler appmDataHandler;

    public static ApplicationManager getApplicationManager() {
        if (applicationManager == null) {
            synchronized (APIUtil.class) {
                if (applicationManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationManager =
                            (ApplicationManager) ctx.getOSGiService(ApplicationManager.class, null);
                    if (applicationManager == null) {
                        String msg = "ApplicationDTO Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationManager;
    }

    /**
     * To get the ApplicationDTO Storage Manager from the osgi context.
     * @return ApplicationStoreManager instance in the current osgi context.
     */
    public static ApplicationStorageManager getApplicationStorageManager() {
        if (applicationStorageManager == null) {
            synchronized (APIUtil.class) {
                if (applicationStorageManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    applicationStorageManager = (ApplicationStorageManager) ctx
                            .getOSGiService(ApplicationStorageManager.class, null);
                    if (applicationStorageManager == null) {
                        String msg = "ApplicationDTO Storage Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }
        return applicationStorageManager;
    }

    public static Response getResponse(Exception ex, Response.Status status) {
        return getResponse(ex.getMessage(), status);
    }

    public static Response getResponse(String message, Response.Status status) {
        ErrorResponse errorMessage = new ErrorResponse();
        errorMessage.setMessage(message);
        if (status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        errorMessage.setCode(status.getStatusCode());
        return Response.status(status).entity(errorMessage).build();
    }

    /**
     * To get the Subscription Manager from the osgi context.
     * @return SubscriptionManager instance in the current osgi context.
     */
    public static SubscriptionManager getSubscriptionManager() {
        if (subscriptionManager == null) {
            synchronized (APIUtil.class) {
                if (subscriptionManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    subscriptionManager =
                            (SubscriptionManager) ctx.getOSGiService(SubscriptionManager.class, null);
                    if (subscriptionManager == null) {
                        String msg = "Subscription Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

        return subscriptionManager;
    }

    /**
     * To get the Review Manager from the osgi context.
     * @return ReviewManager instance in the current osgi context.
     */
    public static ReviewManager getReviewManager() {
        if (reviewManager == null) {
            synchronized (APIUtil.class) {
                if (reviewManager == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    reviewManager =
                            (ReviewManager) ctx.getOSGiService(ReviewManager.class, null);
                    if (reviewManager == null) {
                        String msg = "Review Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

        return reviewManager;
    }

    /**
     * To get the DataHandler from the osgi context.
     * @return AppmDataHandler instance in the current osgi context.
     */
    public static AppmDataHandler getDataHandler() {
        if (appmDataHandler == null) {
            synchronized (APIUtil.class) {
                if (appmDataHandler == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    appmDataHandler =
                            (AppmDataHandler) ctx.getOSGiService(AppmDataHandler.class, null);
                    if (appmDataHandler == null) {
                        String msg = "Config Manager service has not initialized.";
                        log.error(msg);
                        throw new IllegalStateException(msg);
                    }
                }
            }
        }

        return appmDataHandler;
    }

//    public static Filter constructFilter( String appName, String appType, String appCategory, String tags,
//            boolean isFullMatch, String releaseState, int offset, int limit, String sortBy) {
//        Filter filter = new Filter();
//        filter.setOffset(offset);
//        filter.setLimit(limit);
//        filter.setSortBy(sortBy);
//        filter.setFullMatch(isFullMatch);
//        if (!StringUtils.isEmpty(appName)) {
//            filter.setAppName(appName);
//        }
//        if (!StringUtils.isEmpty(appType)) {
//            filter.setAppType(appType);
//        }
//        if (!StringUtils.isEmpty(appCategory)) {
//            filter.setAppCategories(appCategory);
//        }
//        if (!StringUtils.isEmpty(tags)) {
//            filter.setAppCategories(appCategory);
//        }
//        if (!StringUtils.isEmpty(releaseState)) {
//            filter.setAppReleaseState(releaseState);
//        }
//        return filter;
//    }

}
