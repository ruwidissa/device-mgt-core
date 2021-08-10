/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.application.mgt.core.task;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.application.mgt.common.ExecutionStatus;
import io.entgra.application.mgt.common.SubscriptionType;
import io.entgra.application.mgt.common.dto.ScheduledSubscriptionDTO;
import io.entgra.application.mgt.common.exception.ApplicationManagementException;
import io.entgra.application.mgt.common.exception.SubscriptionManagementException;
import io.entgra.application.mgt.common.services.SubscriptionManager;
import io.entgra.application.mgt.core.impl.SubscriptionManagerImpl;
import io.entgra.application.mgt.core.util.Constants;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.task.impl.RandomlyAssignedScheduleTask;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ScheduledAppSubscriptionTask extends RandomlyAssignedScheduleTask {
    private static Log log = LogFactory.getLog(ScheduledAppSubscriptionTask.class);
    private static final String TASK_NAME = "SCHEDULE_APP_SUBSCRIPTION";

    private SubscriptionManager subscriptionManager;
    private String payload;
    private String subscribers;
    private String subscriptionType;
    private String application;
    private String action;
    private String subscriber;
    private String tenantDomain;
    private String taskName;
    private int tenantId;

    @Override
    public void setProperties(Map<String, String> map) {
        this.subscribers = map.get(Constants.SUBSCRIBERS);
        this.payload = map.get(Constants.PAYLOAD);
        this.subscriptionType = map.get(Constants.SUB_TYPE);
        this.application = map.get(Constants.APP_UUID);
        this.action = map.get(Constants.ACTION);
        this.subscriber = map.get(Constants.SUBSCRIBER);
        this.tenantDomain = map.get(Constants.TENANT_DOMAIN);
        this.tenantId = Integer.parseInt(map.get(Constants.TENANT_ID));
        this.taskName = map.get(Constants.TASK_NAME);
    }

    @Override
    public void executeRandomlyAssignedTask() {
        if(isQualifiedToExecuteTask()) {
            try {
                ScheduledSubscriptionDTO subscriptionDTO = subscriptionManager.getPendingScheduledSubscription(
                        this.taskName);
                if (subscriptionDTO == null) {
                    log.error("Unable to execute the task. Task entry for [" + this.taskName + "] cannot be retrieved " +
                              "from the database.");
                    return;
                }
                if (StringUtils.isNotEmpty(this.subscribers)) {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    carbonContext.setTenantDomain(this.tenantDomain);
                    carbonContext.setTenantId(this.tenantId);
                    carbonContext.setUsername(this.subscriber);

                    if (this.subscriptionType.equals(SubscriptionType.DEVICE.toString())) {
                        List<DeviceIdentifier> deviceIdentifiers = new Gson().fromJson(this.subscribers,
                                                                                       new TypeToken<List<DeviceIdentifier>>() {
                                                                                       }.getType());
                        try {
                            Properties properties = new Gson().fromJson(payload, Properties.class);
                            subscriptionManager.performBulkAppOperation(this.application, deviceIdentifiers,
                                                                        this.subscriptionType, this.action, properties);
                            subscriptionDTO.setStatus(ExecutionStatus.EXECUTED);
                        } catch (ApplicationManagementException e) {
                            log.error(
                                    "Error occurred while " + this.action + "ing application " + this.application
                                    + "to/from the following devices: " + this.subscribers, e);
                            subscriptionDTO.setStatus(ExecutionStatus.FAILED);
                        }
                    } else {
                        List<String> subscriberList = Pattern.compile(",").splitAsStream(this.subscribers).collect(
                                Collectors.toList());
                        try {
                            Properties properties = new Gson().fromJson(payload, Properties.class);
                            subscriptionManager.performBulkAppOperation(this.application, subscriberList,
                                                                        this.subscriptionType, this.action, properties);
                            subscriptionDTO.setStatus(ExecutionStatus.EXECUTED);
                        } catch (ApplicationManagementException e) {
                            log.error(
                                    "Error occurred while " + this.action + "ing application " + this.application
                                    + "to/from the following " + this.subscriptionType + "s: " + this.subscribers, e);
                            subscriptionDTO.setStatus(ExecutionStatus.FAILED);
                        }
                    }
                } else {
                    log.warn(
                            "Subscriber list is empty. Therefore skipping scheduled task to " + this.action + "application "
                            + this.application);
                    subscriptionDTO.setStatus(ExecutionStatus.FAILED);
                }
                subscriptionManager.updateScheduledSubscriptionStatus(subscriptionDTO.getId(), subscriptionDTO.getStatus());
            } catch (SubscriptionManagementException e) {
                log.error("Error occurred while executing the task: " + this.taskName, e);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    @Override
    protected void setup() {
        if (this.subscriptionManager == null) {
            this.subscriptionManager = new SubscriptionManagerImpl();
        }
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }
}
