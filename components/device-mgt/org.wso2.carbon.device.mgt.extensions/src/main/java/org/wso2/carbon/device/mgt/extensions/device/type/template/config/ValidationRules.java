package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlElement;

public class ValidationRules {
    @XmlElement(name = "Regex", required = true)
    protected String regex;

    @XmlElement(name = "ValidationMsg", required = true)
    protected String validationMsg;

    @XmlElement(name = "Required", required = true)
    protected boolean required;

    public String getRegexString(){
        return regex;
    }

    public void setRegexString(String regex){
        this.regex = regex;
    }

    public String getValidationMessage(){
        return validationMsg;
    }

    public void setValidationMessage(String validationMsg){
        this.validationMsg = validationMsg;
    }

    public boolean getIsRequired(){
        return required;
    }

    public void setIsRequired(boolean required){
        this.required = required;
    }
}
