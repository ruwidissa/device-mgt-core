package org.wso2.carbon.device.mgt.core.config.enrollment.guide;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EnrollmentGuideConfiguration")
public class EnrollmentGuideConfiguration {

    private boolean isEnabled;
    private String mail;

    @XmlElement(name = "Enable", required = true)
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @XmlElement(name = "Mail", required = true)
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

}
