package org.wso2.carbon.device.application.mgt.core.dto;

import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;

public class ApplicationsDTO {

    public static ApplicationDTO getApp1() {
        ApplicationDTO app = new ApplicationDTO();

        app.setAppCategory("Test Category");
        app.setDescription("Test app Description");
        app.setDeviceTypeId(1);
        app.setName("First Test App");
        app.setSubType("I dont Know");
        app.setType("Idontknow");
        return app;
    }
}
