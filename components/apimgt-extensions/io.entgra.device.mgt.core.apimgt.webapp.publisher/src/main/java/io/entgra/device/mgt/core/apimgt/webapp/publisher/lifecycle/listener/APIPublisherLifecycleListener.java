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
package io.entgra.device.mgt.core.apimgt.webapp.publisher.lifecycle.listener;

import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherUtil;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.APIResourceConfiguration;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.WebappPublisherConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused")
public class APIPublisherLifecycleListener implements LifecycleListener {

    public static final String PROFILE_DEFAULT = "default";
    private static final Log log = LogFactory.getLog(APIPublisherLifecycleListener.class);
    private static final String PARAM_MANAGED_API_ENABLED = "managed-api-enabled";

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {

            APIPublisherDataHolder apiPublisherDataHolder = APIPublisherDataHolder.getInstance();
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();
            String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
            boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);

            if (isManagedApi) {
                if (WebappPublisherConfig.getInstance().isPublished() || WebappPublisherConfig.getInstance().isEnabledUpdateApi()) {

                    try {
                        List<APIResourceConfiguration> apiResourceConfigurations =
                                APIPublisherUtil.getAPIResourceConfiguration(context, servletContext);

                        if (WebappPublisherConfig.getInstance().isPublished()) {
                            for (APIResourceConfiguration apiDefinition : apiResourceConfigurations) {
                                APIConfig apiConfig = APIPublisherUtil.buildApiConfig(servletContext, apiDefinition);
                                if (apiPublisherDataHolder.isServerStarted()) {
                                    APIPublisherUtil.publishAPIAfterServerStartup(apiConfig);
                                } else {
                                    apiPublisherDataHolder.getUnpublishedApis().push(apiConfig);
                                }
                            }
                        }
                    } catch (IOException e) {
                        log.error("Error encountered while discovering annotated classes", e);
                    } catch (ClassNotFoundException e) {
                        log.error("Error while scanning class for annotations", e);
                    } catch (UserStoreException e) {
                        log.error("Error while retrieving tenant admin user for the tenant domain"
                                + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e);
                    } catch (Throwable e) {
                        // This is done to stop tomcat failure if a webapp failed to publish apis.
                        log.error("Failed to Publish api from " + servletContext.getContextPath(), e);
                    }
                }
            }
        }
    }
}
