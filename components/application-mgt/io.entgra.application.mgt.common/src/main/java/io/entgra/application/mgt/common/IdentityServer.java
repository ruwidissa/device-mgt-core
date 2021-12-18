package io.entgra.application.mgt.common;


public class IdentityServer {
    private int id;
    private String name;
    private String description;
    private String url;
    private String spAppsUri;
    private String spAppsApi;
    private String userName;
    private String password;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSpAppsUri() {
        return spAppsUri;
    }

    public void setSpAppsURI(String spAppsUri) {
        this.spAppsUri = spAppsUri;
    }

    public String getSpAppsApi() {
        return spAppsApi;
    }

    public void setSpAppsApi(String spAppsApi) {
        this.spAppsApi = spAppsApi;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
