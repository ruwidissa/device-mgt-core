package org.wso2.carbon.device.application.mgt.core.lifecycle.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * This class represents the lifecycle state config
 */
public class LifecycleState {

    private String name;

    private List<String> proceedingStates;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "ProceedingStates")
    @XmlElement(name = "State")
    public List<String> getProceedingStates() {
        return proceedingStates;
    }

    public void setProceedingStates(List<String> proceedingStates) {
        this.proceedingStates = proceedingStates;
    }
}
