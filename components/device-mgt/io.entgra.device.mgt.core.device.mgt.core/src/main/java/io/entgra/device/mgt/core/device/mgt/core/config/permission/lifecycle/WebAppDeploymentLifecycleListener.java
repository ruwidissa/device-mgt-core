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

package io.entgra.device.mgt.core.device.mgt.core.config.permission.lifecycle;

import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.Permission;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.permission.mgt.PermissionManagerService;
import io.entgra.device.mgt.core.device.mgt.core.config.permission.AnnotationProcessor;
import io.entgra.device.mgt.core.device.mgt.core.permission.mgt.PermissionManagerServiceImpl;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * This listener class will initiate the permission addition of permissions defined in
 * permission.xml of any web-app.
 */
@SuppressWarnings("unused")
public class WebAppDeploymentLifecycleListener implements LifecycleListener {

    private static final String PARAM_MANAGED_API_ENABLED = "managed-api-enabled";

    private static final Log log = LogFactory.getLog(WebAppDeploymentLifecycleListener.class);

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        if (Lifecycle.AFTER_START_EVENT.equals(lifecycleEvent.getType())) {
            StandardContext context = (StandardContext) lifecycleEvent.getLifecycle();
            ServletContext servletContext = context.getServletContext();
            String contextPath = context.getServletContext().getContextPath();
            String param = servletContext.getInitParameter(PARAM_MANAGED_API_ENABLED);
            boolean isManagedApi = (param != null && !param.isEmpty()) && Boolean.parseBoolean(param);

            if (isManagedApi) {
                try {
                    AnnotationProcessor annotationProcessor = new AnnotationProcessor(context);
                    Set<String> annotatedAPIClasses = annotationProcessor.
                            scanStandardContext(io.swagger.annotations.SwaggerDefinition.class.getName());
                    List<Permission> permissions = annotationProcessor.extractPermissions(annotatedAPIClasses);
                    PermissionManagerService permissionManagerService = PermissionManagerServiceImpl.getInstance();
                    permissionManagerService.addPermission(contextPath, permissions);

                } catch (PermissionManagementException e) {
                    log.error("Exception occurred while adding the permissions from webapp : "
                            + servletContext.getContextPath(), e);
                } catch (IOException e) {
                    log.error("Cannot find API annotation Class in the webapp '" + contextPath + "' class path", e);
                }
            }

        }
    }

}
