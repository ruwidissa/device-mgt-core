package org.wso2.carbon.device.mgt.common.invitation.mgt;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnrollmentDetails", propOrder = {
        "enrollmentType",
        "enrollmentSteps"
})
public class EnrollmentDetails {

    @XmlElement(name = "EnrollmentType")
    private String enrollmentType;

    @XmlElement(name = "EnrollmentSteps")
    private String enrollmentSteps;

    public String getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(String enrollmentType) {
        this.enrollmentType = enrollmentType;
    }

    public String getEnrollmentSteps() {
        return enrollmentSteps;
    }

    public void setEnrollmentSteps(String enrollmentSteps) {
        this.enrollmentSteps = enrollmentSteps;
    }
}
