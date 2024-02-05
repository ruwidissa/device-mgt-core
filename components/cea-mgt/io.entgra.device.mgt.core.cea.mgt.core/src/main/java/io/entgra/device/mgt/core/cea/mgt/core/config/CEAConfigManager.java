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

package io.entgra.device.mgt.core.cea.mgt.core.config;

import io.entgra.device.mgt.core.cea.mgt.common.bean.ui.CEAPolicyUIConfiguration;
import io.entgra.device.mgt.core.cea.mgt.common.exception.CEAConfigManagerException;
import io.entgra.device.mgt.core.cea.mgt.core.bean.CEAConfiguration;
import io.entgra.device.mgt.core.cea.mgt.core.config.datasource.CEADeviceMgtConfiguration;
import io.entgra.device.mgt.core.cea.mgt.core.config.datasource.CEAPolicyManagementRepository;
import io.entgra.device.mgt.core.cea.mgt.core.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class CEAConfigManager {
    private static final Log log = LogFactory.getLog(CEAConfigManager.class);

    private static final String CDM_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.CDM_CONFIG_FILE_NAME;
    private static final String CEA_UI_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.CEA_POLICY_UI_FILE_NAME;
    private static final String CEA_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.CEA_CONFIG_FILE_NAME;
    private CEAPolicyManagementRepository ceaPolicyManagementRepository;
    private CEAConfiguration ceaConfiguration;
    private CEAPolicyUIConfiguration ceaPolicyUIConfiguration;

    CEAConfigManager() {
    }

    public static CEAConfigManager getInstance() {
        return CEAConfigManagerHolder.INSTANCE;
    }

    private <T> T initConfig(String docPath, Class<T> configClass) throws JAXBException {
        File doc = new File(docPath);
        JAXBContext jaxbContext = JAXBContext.newInstance(configClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return configClass.cast(jaxbUnmarshaller.unmarshal(doc));
    }

    private void initDatasourceConfig() throws JAXBException {
        ceaPolicyManagementRepository = initConfig(CDM_CONFIG_PATH, CEADeviceMgtConfiguration.class)
                .getCeaPolicyManagementRepository();
    }

    private void initCEAPConfig() throws JAXBException {
        ceaConfiguration = initConfig(CEA_CONFIG_PATH, CEAConfiguration.class);
    }

    private void initCEAPolicyUIConfig() throws JAXBException {
        ceaPolicyUIConfiguration = initConfig(CEA_UI_CONFIG_PATH, CEAPolicyUIConfiguration.class);
    }

    public CEAPolicyManagementRepository getCeaPolicyManagementRepository() throws CEAConfigManagerException {
        try {
            if (ceaPolicyManagementRepository == null) {
                initDatasourceConfig();
            }
            return ceaPolicyManagementRepository;
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing datasource configuration";
            throw new CEAConfigManagerException(msg, e);
        }
    }

    public CEAConfiguration getCeaConfiguration() throws CEAConfigManagerException {
        try {
            if (ceaConfiguration == null) {
                initCEAPConfig();
            }
            return ceaConfiguration;
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing CEA configuration";
            throw new CEAConfigManagerException(msg, e);
        }
    }

    public CEAPolicyUIConfiguration getCeaPolicyUIConfiguration() throws CEAConfigManagerException {
        try {
            if (ceaPolicyUIConfiguration == null) {
                initCEAPolicyUIConfig();
            }
            return ceaPolicyUIConfiguration;
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing policy UI configuration";
            throw new CEAConfigManagerException(msg, e);
        }
    }

    private static class CEAConfigManagerHolder {
        public static final CEAConfigManager INSTANCE = new CEAConfigManager();
    }
}
