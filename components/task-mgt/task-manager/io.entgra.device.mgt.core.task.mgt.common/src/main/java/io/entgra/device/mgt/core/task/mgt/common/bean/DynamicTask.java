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
package io.entgra.device.mgt.core.task.mgt.common.bean;

import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.Map;
import java.util.Objects;

public class DynamicTask {

    private int dynamicTaskId;
    private String name;
    private String cronExpression;
    private long intervalMillis;
    private boolean isEnabled;
    private int tenantId;
    private String taskClassName;
    private Map<String, String> properties;

    public int getDynamicTaskId() {
        return dynamicTaskId;
    }

    public void setDynamicTaskId(int dynamicTaskId) {
        this.dynamicTaskId = dynamicTaskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public void setIntervalMillis(long intervalMillis) {
        this.intervalMillis = intervalMillis;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enable) {
        isEnabled = enable;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getTaskClassName() {
        return taskClassName;
    }

    public void setTaskClassName(String taskClassName) {
        this.taskClassName = taskClassName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Generate nTask trigger info.
     *
     * @return {@link TaskInfo.TriggerInfo}
     */
    public TaskInfo.TriggerInfo getTriggerInfo() {
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
        // Millisecond intervals are more precision than the cron. Hence, giving priority to millisecond intervals
        if (intervalMillis > 0) {
            triggerInfo.setIntervalMillis(intervalMillis);
            triggerInfo.setRepeatCount(-1);
            return triggerInfo;
        }

        triggerInfo.setCronExpression(cronExpression);
        return triggerInfo;
    }

    /**
     * Check if the trigger information are equal or not.
     *
     * @param thatInfo {@link TaskInfo.TriggerInfo} to compare.
     * @return True if equals otherwise False.
     */
    public boolean isTriggerInfoEquals(TaskInfo.TriggerInfo thatInfo) {
        if (intervalMillis > 0 && thatInfo.getIntervalMillis() > 0) {
            return Objects.equals(intervalMillis, thatInfo.getIntervalMillis());
        }

        if (cronExpression != null && thatInfo.getCronExpression() != null) {
            return Objects.equals(cronExpression, thatInfo.getCronExpression());
        }

        return false;
    }

}
