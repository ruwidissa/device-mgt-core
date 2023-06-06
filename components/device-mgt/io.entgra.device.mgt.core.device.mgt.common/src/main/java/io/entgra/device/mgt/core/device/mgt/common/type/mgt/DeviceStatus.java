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

package io.entgra.device.mgt.core.device.mgt.common.type.mgt;

import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;

import java.util.Date;

public class DeviceStatus {
  private EnrolmentInfo.Status status;
  private Date updateTime;
  private int enrolmentId;
  private int deviceId;
  private String changedBy;

  public DeviceStatus(int enrolmentId, int deviceId, EnrolmentInfo.Status status, Date updateTime, String changedBy) {
    this.status = status;
    this.updateTime = updateTime;
    this.enrolmentId = enrolmentId;
    this.deviceId = deviceId;
    this.changedBy = changedBy;
  }

  public String getChangedBy() {
    return changedBy;
  }

  public void setChangedBy(String changedBy) {
    this.changedBy = changedBy;
  }

  public EnrolmentInfo.Status getStatus() {
    return status;
  }

  public void setStatus(EnrolmentInfo.Status status) {
    this.status = status;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public int getEnrolmentId() {
    return enrolmentId;
  }

  public void setEnrolmentId(int enrolmentId) {
    this.enrolmentId = enrolmentId;
  }

  public int getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(int deviceId) {
    this.deviceId = deviceId;
  }
}
