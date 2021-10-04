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

package io.entgra.application.mgt.common.dto;

import com.google.gson.Gson;
import io.entgra.application.mgt.common.ExecutionStatus;
import io.entgra.application.mgt.common.SubscriptionType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents a DTO for AP_SCHEDULED_SUBSCRIPTION table
 */
public class ScheduledSubscriptionDTO {
    /**
     * Generated ID of the subscription.
     */
    private int id;

    /**
     * Name of the task which is related to the subscription.
     *
     * Task name is a generated field and in the following pattern:
     * {@code <SUBSCRIPTION-TYPE>_<ACTION>_<HASH-VALUE>}
     * {@code SUBSCRIPTION-TYPE} - {@see {@linkplain SubscriptionType}}
     * {@code ACTION} - {@see {@linkplain io.entgra.application.mgt.common.SubAction}
     * {@code HASH-VALUE} - this is a hash value of the combination of application uuid and the subscriber list.
     *
     * Example: {@code DEVICE_INSTALL_e593e00e8ef55efc764295b6aa9ad56b}
     */
    private String taskName;

    /**
     * UUID of the application release which is subscribed to.
     * {@see {@link io.entgra.application.mgt.common.response.ApplicationRelease}}
     */
    private String applicationUUID;

    /**
     * List of subscribers for the application release. The type of the list depends on the subscription type.
     * {@see {@link SubscriptionType}}. If the subscription type is {@code SubscriptionType.DEVICE} the type will be
     * {@link org.wso2.carbon.device.mgt.common.DeviceIdentifier} and if not the type will be {@link String}.
     */
    private List<?> subscriberList;

    /**
     * Status of the subscription. {@see {@link ExecutionStatus}}
     */
    private ExecutionStatus status;

    /**
     * Scheduled time of subscription.
     */
    private long scheduledAt;

    /**
     * Username of the scheduler.
     */
    private String scheduledBy;

    /**
     * If the subscription is marked as deleted or not.
     * {@code true} means that the related task is removed from the {@link org.wso2.carbon.ntask.core.TaskManager}.
     */
    private boolean deleted;

    public ScheduledSubscriptionDTO() {

    }

    public ScheduledSubscriptionDTO(String taskName, String applicationUUID, long scheduledAt,
            List<?> subscriberList, String scheduledBy) {
        this.taskName = taskName;
        this.applicationUUID = applicationUUID;
        this.scheduledAt = scheduledAt;
        this.subscriberList = subscriberList;
        this.scheduledBy = scheduledBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getApplicationUUID() {
        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {
        this.applicationUUID = applicationUUID;
    }

    public List<?> getSubscriberList() {
        return subscriberList;
    }

    public void setSubscriberList(List<?> subscriberList) {
        this.subscriberList = subscriberList;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getScheduledBy() {
        return scheduledBy;
    }

    public void setScheduledBy(String scheduledBy) {
        this.scheduledBy = scheduledBy;
    }

    /**
     * @return the string representation of the subscriber list.
     */
    public String getSubscribersString() {
        if (this.taskName.startsWith(SubscriptionType.DEVICE.toString())) {
            return new Gson().toJson(this.subscriberList);
        } else {
            return this.subscriberList.stream().map(String.class::cast).collect(Collectors.joining(","));
        }
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
