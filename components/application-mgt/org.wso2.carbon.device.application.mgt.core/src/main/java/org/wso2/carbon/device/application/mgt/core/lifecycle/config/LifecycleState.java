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
    private String permission;
    private List<String> proceedingStates;
    private boolean isAppInstallable;
    private boolean isAppUpdatable;
    private boolean isInitialState;
    private boolean isEndState;

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

    @XmlElement(name = "Permission")
    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @XmlElement(name = "IsAppInstallable")
    public boolean isAppInstallable() {
        return isAppInstallable;
    }

    public void setAppInstallable(boolean isAppInstallable) {
        this.isAppInstallable = isAppInstallable;
    }

    @XmlElement(name = "IsAppUpdatable")
    public boolean isAppUpdatable() {
        return isAppUpdatable;
    }

    public void setAppUpdatable(boolean isAppUpdatable) {
        this.isAppUpdatable = isAppUpdatable;
    }

    @XmlElement(name = "IsInitialState")
    public boolean isInitialState() {
        return isInitialState;
    }

    public void setInitialState(boolean isInitialState) {
        this.isInitialState = isInitialState;
    }

    @XmlElement(name = "IsEndState")
    public boolean isEndState() {
        return isEndState;
    }

    public void setEndState(boolean isEndState) {
        this.isEndState = isEndState;
    }

}
