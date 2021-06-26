package org.wso2.carbon.webapp.authenticator.framework.authorizer;

/**
 * Created by amalka on 6/26/21.
 */
public class MatchingResource {
    private String urlPattern;
    private String permission;

    public MatchingResource(String urlPattern, String permission) {
        this.urlPattern = urlPattern;
        this.permission = permission;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
