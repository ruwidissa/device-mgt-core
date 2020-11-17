/*
 * Copyright (c) 2020, Entgra Pvt Ltd. (http://www.wso2.org) All Rights Reserved.
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

package io.entgra.server.bootup.heartbeat.beacon;

import io.entgra.server.bootup.heartbeat.beacon.exception.InvalidConfigurationStateException;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

@XmlRootElement(name = "HeartBeatBeaconConfig")
public class HeartBeatBeaconConfig {

    private int notifierFrequency;
    private int notifierDelay;
    private int serverTimeOutIntervalInSeconds;
    private int timeSkew;

    private static HeartBeatBeaconConfig config;

    private static final String HEART_BEAT_NOTIFIER_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "heart-beat-config.xml";

    private HeartBeatBeaconConfig() {
    }

    public static HeartBeatBeaconConfig getInstance() {
        if (config == null) {
            throw new InvalidConfigurationStateException("Webapp Authenticator Configuration is not " +
                                                         "initialized properly");
        }
        return config;
    }

    @XmlElement(name = "NotifierInitialDelay", required = true)
    public int getNotifierDelay() {
        return notifierDelay;
    }

    public void setNotifierDelay(int notifierDelay) {
        this.notifierDelay = notifierDelay;
    }

    @XmlElement(name = "NotifierFrequency", required = true)
    public int getNotifierFrequency() {
        return notifierFrequency;
    }

    public void setNotifierFrequency(int notifierFrequency) {
        this.notifierFrequency = notifierFrequency;
    }

    @XmlElement(name = "timeSkew", required = true)
    public int getTimeSkew() {
        return timeSkew;
    }

    public void setTimeSkew(int timeSkew) {
        this.timeSkew = timeSkew;
    }

    @XmlElement(name = "ServerTimeOutIntervalInSeconds", required = true)
    public int getServerTimeOutIntervalInSeconds() {
        return serverTimeOutIntervalInSeconds;
    }

    public void setServerTimeOutIntervalInSeconds(int serverTimeOutIntervalInSeconds) {
        this.serverTimeOutIntervalInSeconds = serverTimeOutIntervalInSeconds;
    }

    public static void init() throws HeartBeatBeaconConfigurationException {
        try {
            File emailSenderConfig = new File(HEART_BEAT_NOTIFIER_CONFIG_PATH);
            Document doc = HeartBeatBeaconUtils.convertToDocument(emailSenderConfig);

            /* Un-marshaling Email Sender configuration */
            JAXBContext ctx = JAXBContext.newInstance(HeartBeatBeaconConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            config = (HeartBeatBeaconConfig) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            throw new HeartBeatBeaconConfigurationException("Error occurred while un-marshalling " +
                                                            "heart beat configuration file", e);
        }
    }

}
