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

import com.google.gson.Gson;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiScope;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherService;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.APIPublisherUtil;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.APIResourceConfiguration;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.config.WebappPublisherConfig;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.lifecycle.util.AnnotationProcessor;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public class APIPublisherLifecycleListener implements LifecycleListener {

    private static final Log log = LogFactory.getLog(APIPublisherLifecycleListener.class);
    private static final String PARAM_MANAGED_API_ENABLED = "managed-api-enabled";
    public static final String PROPERTY_PROFILE = "profile";
    public static final String PROFILE_DT_WORKER = "dtWorker";
    public static final String PROFILE_DEFAULT = "default";

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType()) ) {
            if (WebappPublisherConfig.getInstance()
                    .isPublished()) {
                StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
                ServletContext servletContext = context.getServletContext();
                String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
                boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);

                String profile = System.getProperty(PROPERTY_PROFILE);
                if (WebappPublisherConfig.getInstance().getProfiles().getProfile().contains(profile.toLowerCase())
                        && isManagedApi) {
                    try {
                        AnnotationProcessor annotationProcessor = new AnnotationProcessor(context);
                        Set<String> annotatedSwaggerAPIClasses = annotationProcessor.
                                scanStandardContext(io.swagger.annotations.SwaggerDefinition.class.getName());
                        List<APIResourceConfiguration> apiDefinitions = annotationProcessor.extractAPIInfo(servletContext,
                                annotatedSwaggerAPIClasses);

                        APIPublisherDataHolder apiPublisherDataHolder = APIPublisherDataHolder.getInstance();
                        MetadataManagementService metadataManagementService =
                                apiPublisherDataHolder.getMetadataManagementService();
                        Metadata metadata = metadataManagementService.retrieveMetadata("perm-scope-mapping");
                        if (metadata != null) {
                            HashMap<String, String> permScopeMapping =
                                    new Gson().fromJson(metadata.getMetaValue().toString(), HashMap.class);
                            apiPublisherDataHolder.setPermScopeMapping(permScopeMapping);
                        }

                        Map<String, String> permScopeMap = apiPublisherDataHolder.getPermScopeMapping();
                        for (APIResourceConfiguration apiDefinition : apiDefinitions) {
                            APIConfig apiConfig = APIPublisherUtil.buildApiConfig(servletContext, apiDefinition);
                            for (ApiScope scope : apiConfig.getScopes()) {
                                permScopeMap.put(scope.getPermissions(), scope.getKey());
                            }
                            APIPublisherUtil.setResourceAuthTypes(servletContext,apiConfig);
                            try {
                                int tenantId = APIPublisherDataHolder.getInstance().getTenantManager().
                                        getTenantId(apiConfig.getTenantDomain());

                                boolean isTenantActive = APIPublisherDataHolder.getInstance().
                                        getTenantManager().isTenantActive(tenantId);
                                if (isTenantActive) {
                                    boolean isServerStarted = APIPublisherDataHolder.getInstance().isServerStarted();
                                    if (isServerStarted) {
                                        APIPublisherService apiPublisherService =
                                                APIPublisherDataHolder.getInstance().getApiPublisherService();
                                        if (apiPublisherService == null) {
                                            throw new IllegalStateException(
                                                    "API Publisher service is not initialized properly");
                                        }
                                        apiPublisherService.publishAPI(apiConfig);
                                    } else {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Server has not started yet. Hence adding API '" +
                                                    apiConfig.getName() + "' to the queue");
                                        }
                                        APIPublisherDataHolder.getInstance().getUnpublishedApis().push(apiConfig);
                                    }
                                } else {
                                    log.error("No tenant [" + apiConfig.getTenantDomain() + "] " +
                                            "found when publishing the Web app");
                                }
                            } catch (Throwable e) {
                                log.error("Error occurred while publishing API '" + apiConfig.getName() +
                                        "' with the context '" + apiConfig.getContext() +
                                        "' and version '" + apiConfig.getVersion() + "'", e);
                            }
                        }

                        Metadata existingMetaData = metadataManagementService.retrieveMetadata("perm-scope" +
                                "-mapping");

                        if (existingMetaData != null) {
                            existingMetaData.setMetaValue(new Gson().toJson(permScopeMap));
                            metadataManagementService.updateMetadata(existingMetaData);
                        } else {
                            Metadata newMetaData = new Metadata();
                            newMetaData.setMetaKey("perm-scope-mapping");
                            newMetaData.setMetaValue(new Gson().toJson(permScopeMap));
                            metadataManagementService.createMetadata(newMetaData);
                        }
                        apiPublisherDataHolder.setPermScopeMapping(permScopeMap);
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
            } else {
                APIPublisherDataHolder apiPublisherDataHolder = APIPublisherDataHolder.getInstance();
                MetadataManagementService metadataManagementService =
                        apiPublisherDataHolder.getMetadataManagementService();
                try {
                    Metadata existingMetaData = metadataManagementService.retrieveMetadata("perm-scope" +
                            "-mapping");
                    if (existingMetaData != null) {
                        existingMetaData.setMetaValue(new Gson().toJson(apiPublisherDataHolder.getPermScopeMapping()
                        ));
                        metadataManagementService.updateMetadata(existingMetaData);
                    } else {
                        log.error("Couldn't find 'perm-scope-mapping' Meta entry while API publishing has been turned" +
                                " off.");
                    }
                } catch (MetadataManagementException e) {
                    log.error("Failed to Load Meta-Mgt data.", e);
                }
            }
        }
    }
}
