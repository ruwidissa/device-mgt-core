package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class SubFormList {
    @XmlAttribute(name = "id", required = true)
    protected String id;

    @XmlElementWrapper(name = "PanelItems")
    @XmlElement(name = "PanelItem")
    private List<PanelItem> panelItem;

    public List<PanelItem> getPanelItemList() {
        return panelItem;
    }

    public void setPanelItemList(List<PanelItem> panelItem) {
        this.panelItem = panelItem;
    }
}

