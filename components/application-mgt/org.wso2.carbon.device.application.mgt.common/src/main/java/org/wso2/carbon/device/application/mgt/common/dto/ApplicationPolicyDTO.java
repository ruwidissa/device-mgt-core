package org.wso2.carbon.device.application.mgt.common.dto;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

public class ApplicationPolicyDTO {
    ApplicationDTO applicationDTO;
    String policy;
    List<DeviceIdentifier> deviceIdentifierList;
    String action;

    public List<DeviceIdentifier> getDeviceIdentifierList() {
        return deviceIdentifierList;
    }

    public void setDeviceIdentifierList(List<DeviceIdentifier> deviceIdentifierList) {
        this.deviceIdentifierList = deviceIdentifierList;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ApplicationDTO getApplicationDTO() {
        return applicationDTO;
    }

    public void setApplicationDTO(ApplicationDTO applicationDTO) {
        this.applicationDTO = applicationDTO;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}
