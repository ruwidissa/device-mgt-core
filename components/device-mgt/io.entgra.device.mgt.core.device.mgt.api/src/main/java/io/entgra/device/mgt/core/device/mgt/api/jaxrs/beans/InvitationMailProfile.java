package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "InvitationMailProfile", description = "Holds data related to JIT Enrollment invitation mails")
public class InvitationMailProfile {
    @ApiModelProperty(name = "username", value = "Username (same as username in external IDP)", required = true)
    private String username;
    @ApiModelProperty(name = "mail", value = "Mail will be sent to this mail address", required = true)
    private String mail;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
