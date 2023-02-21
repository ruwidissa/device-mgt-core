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
package io.entgra.task.mgt.core.config;

import io.entgra.task.mgt.common.constant.TaskMgtConstants;
import io.entgra.task.mgt.common.exception.TaskManagementException;
import io.entgra.task.mgt.core.util.TaskManagementUtil;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class responsible for the task mgt configuration initialization.
 */
public class TaskConfigurationManager {

    private TaskManagementConfig taskManagementConfig;
    private static volatile TaskConfigurationManager taskConfigurationManager;

    private static final String TASK_MGT_CONFIG_PATH =
            CarbonUtils.getCarbonConfigDirPath() + File.separator +
                    TaskMgtConstants.DataSourceProperties.TASK_CONFIG_XML_NAME;

    public static TaskConfigurationManager getInstance() {
        if (taskConfigurationManager == null) {
            synchronized (TaskConfigurationManager.class) {
                if (taskConfigurationManager == null) {
                    taskConfigurationManager = new TaskConfigurationManager();
                }
            }
        }
        return taskConfigurationManager;
    }

    public synchronized void initConfig() throws TaskManagementException {
        try {
            File taskMgtConfig = new File(TASK_MGT_CONFIG_PATH);
            Document doc = TaskManagementUtil.convertToDocument(taskMgtConfig);

            /* Un-marshaling Device Management configuration */
            JAXBContext cdmContext = JAXBContext.newInstance(TaskManagementConfig.class);
            Unmarshaller unmarshaller = cdmContext.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            this.taskManagementConfig = (TaskManagementConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new TaskManagementException("Error occurred while initializing Data Source config", e);
        }
    }

    public TaskManagementConfig getTaskManagementConfig() throws TaskManagementException {
        if (taskManagementConfig == null) {
            initConfig();
        }
        return taskManagementConfig;
    }

    public void setTaskManagementConfig(TaskManagementConfig taskManagementConfig) {
        this.taskManagementConfig = taskManagementConfig;
    }
}
