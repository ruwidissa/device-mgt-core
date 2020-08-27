package org.wso2.carbon.device.mgt.common.invitation.mgt;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceEnrollmentInvitationDetails", propOrder = {
        "enrollmentDetails"
})
public class DeviceEnrollmentInvitationDetails {

    @XmlElement(name = "EnrollmentDetails")
    private List<EnrollmentDetails> enrollmentDetails;

    public List<EnrollmentDetails> getEnrollmentDetails() {
        return enrollmentDetails;
    }

    public void setEnrollmentDetails(List<EnrollmentDetails> enrollmentDetails) {
        this.enrollmentDetails = enrollmentDetails;
    }
}
