package org.wso2.carbon.device.mgt.common.type.mgt;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;

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
