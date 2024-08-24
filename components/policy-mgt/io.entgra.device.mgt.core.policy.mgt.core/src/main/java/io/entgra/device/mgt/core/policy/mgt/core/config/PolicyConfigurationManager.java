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

package io.entgra.device.mgt.core.policy.mgt.core.config;

import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.policy.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.policy.mgt.core.util.PolicyManagementConstants;
import io.entgra.device.mgt.core.policy.mgt.core.util.PolicyManagerUtil;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Class responsible for the rss manager configuration initialization
 */
public class PolicyConfigurationManager {

    private PolicyManagementConfig currentPolicyConfig;
    private static PolicyConfigurationManager policyConfigurationManager;

    private final String deviceMgtConfigXMLPath = CarbonUtils.getCarbonConfigDirPath() + File.separator  +
            PolicyManagementConstants.DEVICE_CONFIG_XML_NAME;

    public static PolicyConfigurationManager getInstance() {
        if (policyConfigurationManager == null) {
            synchronized (PolicyConfigurationManager.class) {
                if (policyConfigurationManager == null) {
                    policyConfigurationManager = new PolicyConfigurationManager();
                }
            }
        }
        return policyConfigurationManager;
    }

    public synchronized void initConfig() throws PolicyManagementException {
        try {
            File deviceMgtConfig = new File(deviceMgtConfigXMLPath);
            Document doc = PolicyManagerUtil.convertToDocument(deviceMgtConfig);

            /* Un-marshaling Device Management configuration */
            JAXBContext rssContext = JAXBContext.newInstance(PolicyManagementConfig.class);
            Unmarshaller unmarshaller = rssContext.createUnmarshaller();
            this.currentPolicyConfig = (PolicyManagementConfig) unmarshaller.unmarshal(doc);
        } catch (Exception e) {
            throw new PolicyManagementException("Error occurred while initializing RSS config", e);
        }
    }

    public PolicyManagementConfig getPolicyManagementConfig() {
        return currentPolicyConfig;
    }

    public DataSourceConfig getDataSourceConfig() {
        return currentPolicyConfig.getPolicyManagementRepository().getDataSourceConfig();
    }


}
