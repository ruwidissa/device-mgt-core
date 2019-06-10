import React from "react";
import {Button, Modal, Tabs} from "antd";
import UserInstall from "./UserInstall";

const { TabPane } = Tabs;

class AppInstallModal extends React.Component{
    render() {
        return (
            <div>
                <Modal
                    title="Install App"
                    visible={this.props.visible}
                    // onOk={this.handleOk}
                    onCancel={this.props.onClose}
                >
                    <Tabs defaultActiveKey="1">
                        <TabPane tab="User" key="1">
                            <UserInstall onInstall={this.props.onInstall}/>
                        </TabPane>
                        <TabPane tab="Device" key="2">
                            Tab 2
                        </TabPane>
                        <TabPane tab="Role" key="3">
                            Tab 3
                        </TabPane>
                        <TabPane tab="Group" key="4">
                            Tab 3
                        </TabPane>
                    </Tabs>
                </Modal>
            </div>
        );
    }
}

export default AppInstallModal;