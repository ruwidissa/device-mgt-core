package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "JITEnrollmentInvitation", description = "Holds data related to JIT enrollment invitations")
public class JITEnrollmentInvitation {
    @ApiModelProperty(name = "mailProfiles", value = "Mail profiles to send mail invitations", required = true)
    private List<InvitationMailProfile> mailProfiles;
    @ApiModelProperty(name = "ownershipType", value = "Ownership type of the enrollment", required = true)
    private String ownershipType;
    @ApiModelProperty(name = "deviceType", value = "Device type", required = true)
    private String deviceType;
    @ApiModelProperty(name = "sp", value = "Service provider name", required = true)
    private String sp;

    public List<InvitationMailProfile> getMailProfiles() {
        return mailProfiles;
    }

    public void setMailProfiles(List<InvitationMailProfile> mailProfiles) {
        this.mailProfiles = mailProfiles;
    }

    public String getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(String ownershipType) {
        this.ownershipType = ownershipType;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSp() {
        return sp;
    }

    public void setSp(String sp) {
        this.sp = sp;
    }
}
