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
package io.entgra.device.mgt.core.task.mgt.core.util;

import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.device.mgt.core.task.mgt.common.bean.DynamicTask;
import io.entgra.device.mgt.core.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementException;
import io.entgra.device.mgt.core.task.mgt.core.internal.TaskManagerDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides utility methods required by the task management bundle.
 */
public class TaskManagementUtil {

    private static final Log log = LogFactory.getLog(TaskManagementUtil.class);

    public static Document convertToDocument(File file) throws TaskManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new TaskManagementException(
                    "Error occurred while parsing file, while converting " +
                            "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    public static String generateNTaskName(int dynamicTaskId) throws TaskManagementException {
        try {
            int serverHashIdx = TaskManagerDataHolder.getInstance().getHeartBeatService()
                    .getServerCtxInfo().getLocalServerHashIdx();
            return generateNTaskName(dynamicTaskId, serverHashIdx);
        } catch (HeartBeatManagementException e) {
            String msg = "Failed to generate task id for a dynamic task " + dynamicTaskId;
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        }
    }

    public static String generateNTaskName(int dynamicTaskId, int serverHashIdx) {
        return TaskMgtConstants.Task.DYNAMIC_TASK_TYPE + TaskMgtConstants.Task.NAME_SEPARATOR + dynamicTaskId
                + TaskMgtConstants.Task.NAME_SEPARATOR + serverHashIdx;
    }

    public static Map<String, String> populateNTaskProperties(DynamicTask dynamicTask,
                                                              String nTaskName) throws TaskManagementException {
        try {
            int serverHashIdx = TaskManagerDataHolder.getInstance().getHeartBeatService()
                    .getServerCtxInfo().getLocalServerHashIdx();
            return populateNTaskProperties(dynamicTask, nTaskName, serverHashIdx);
        } catch (HeartBeatManagementException e) {
            String msg = "Failed to populate nTask properties a dynamic task " + dynamicTask.getDynamicTaskId();
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        }
    }

    public static Map<String, String> populateNTaskProperties(DynamicTask dynamicTask,
                                                              String nTaskName, int serverHashIdx) {
        Map<String, String> taskProperties = new HashMap<>();
        for (String propertyKey : dynamicTask.getProperties().keySet()) {
            taskProperties.put(propertyKey, dynamicTask.getProperties().get(propertyKey));
        }
        taskProperties.put(TaskMgtConstants.Task.DYNAMIC_TASK_ID, String.valueOf(dynamicTask.getDynamicTaskId()));
        taskProperties.put(TaskMgtConstants.Task.LOCAL_TASK_NAME, nTaskName);
        taskProperties.put(TaskMgtConstants.Task.LOCAL_HASH_INDEX, String.valueOf(serverHashIdx));
        taskProperties.put(TaskMgtConstants.Task.TENANT_ID_PROP,
                String.valueOf(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId()));
        return taskProperties;
    }

}
