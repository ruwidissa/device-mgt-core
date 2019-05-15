package org.wso2.carbon.device.application.mgt.core.dto;

import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsDTO {

    public static ApplicationDTO getApp1() {
        ApplicationDTO app = new ApplicationDTO();
        List<String> categories = new ArrayList<>();

        categories.add("Test Category");
        app.setAppCategories(categories);
        app.setDescription("Test app Description");
        app.setDeviceTypeId(1);
        app.setName("First Test App");
        app.setSubType("I dont Know");
        app.setType("Idontknow");
        return app;
    }
}
