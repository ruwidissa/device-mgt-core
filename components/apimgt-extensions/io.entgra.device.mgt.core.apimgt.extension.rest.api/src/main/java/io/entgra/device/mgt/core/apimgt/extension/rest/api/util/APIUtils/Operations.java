package io.entgra.device.mgt.core.apimgt.extension.rest.api.util.APIUtils;


import java.util.Set;

/**
 * This hold the api operations information.
 */

public class Operations {

    private String id;
    private String target;
    private String verb;
    private String authType;
    private String throttlingPolicy;
    private Set<String> scopes;
    private String usedProductIds;
    private String amznResourceName;
    private String amznResourceTimeout;
    private String payloadSchema;
    private String uriMapping;

    public Operations() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getThrottlingPolicy() {
        return throttlingPolicy;
    }

    public void setThrottlingPolicy(String throttlingPolicy) {
        this.throttlingPolicy = throttlingPolicy;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
    }

    public String getUsedProductIds() {
        return usedProductIds;
    }

    public void setUsedProductIds(String usedProductIds) {
        this.usedProductIds = usedProductIds;
    }

    public String getAmznResourceName() {
        return amznResourceName;
    }

    public void setAmznResourceName(String amznResourceName) {
        this.amznResourceName = amznResourceName;
    }

    public String getAmznResourceTimeout() {
        return amznResourceTimeout;
    }

    public void setAmznResourceTimeout(String amznResourceTimeout) {
        this.amznResourceTimeout = amznResourceTimeout;
    }

    public String getPayloadSchema() {
        return payloadSchema;
    }

    public void setPayloadSchema(String payloadSchema) {
        this.payloadSchema = payloadSchema;
    }

    public String getUriMapping() {
        return uriMapping;
    }

    public void setUriMapping(String uriMapping) {
        this.uriMapping = uriMapping;
    }

}
