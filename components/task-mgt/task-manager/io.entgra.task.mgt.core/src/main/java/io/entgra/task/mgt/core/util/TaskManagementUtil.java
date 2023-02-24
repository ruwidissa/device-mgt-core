/*
 * Copyright (c) 2023, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
 *
 * Entgra Pvt Ltd. licenses this file to you under the Apache License,
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
package io.entgra.task.mgt.core.util;

import com.google.gson.Gson;
import io.entgra.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.task.mgt.common.exception.TaskManagementException;
import io.entgra.task.mgt.core.internal.TaskManagerDataHolder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
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

    public static String generateTaskId(int dynamicTaskId) throws TaskManagementException {
        try {
            int serverHashIdx = TaskManagerDataHolder.getInstance().getHeartBeatService()
                    .getServerCtxInfo().getLocalServerHashIdx();
            return generateTaskId(dynamicTaskId, serverHashIdx);
        } catch (HeartBeatManagementException e) {
            String msg = "Failed to generate task id for a dynamic task " + dynamicTaskId;
            log.error(msg, e);
            throw new TaskManagementException(msg, e);
        }
    }

    public static String generateTaskId(int dynamicTaskId, int serverHashIdx) {
        return TaskMgtConstants.Task.DYNAMIC_TASK_TYPE + TaskMgtConstants.Task.NAME_SEPARATOR + dynamicTaskId
                + TaskMgtConstants.Task.NAME_SEPARATOR + serverHashIdx;
    }

    public static String generateTaskPropsMD5(Map<String, String> taskProperties) {
        taskProperties.remove(TaskMgtConstants.Task.TENANT_ID_PROP);
        taskProperties.remove(TaskMgtConstants.Task.LOCAL_HASH_INDEX);
        taskProperties.remove(TaskMgtConstants.Task.LOCAL_TASK_NAME);
        Gson gson = new Gson();
        String json = gson.toJson(taskProperties);
        return DigestUtils.md5Hex(json);
    }

}
