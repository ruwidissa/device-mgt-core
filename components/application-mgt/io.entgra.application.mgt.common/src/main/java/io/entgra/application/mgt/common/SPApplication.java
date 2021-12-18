package io.entgra.application.mgt.common;


import io.entgra.application.mgt.common.response.Application;

import java.util.List;

public class SPApplication {
    private String id;
    private String name;
    private String description;
    private String image;
    private String accessUrl;
    private String access;
    private String self;
    private List<Application> existingApplications;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public List<Application> getExistingApplications() {
        return existingApplications;
    }

    public void setExistingApplications(List<Application> existingApplications) {
        this.existingApplications = existingApplications;
    }
}
