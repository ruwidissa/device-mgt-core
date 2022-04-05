package io.entgra.application.mgt.common.dto;

import java.util.List;

public class IdentityServiceProviderDTO {
    private String name;
    private List<String> requiredApiParams;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRequiredApiParams() {
        return requiredApiParams;
    }

    public void setRequiredApiParams(List<String> requiredApiParams) {
        this.requiredApiParams = requiredApiParams;
    }
}
