import React from "react";
import {Button, Modal, Tabs} from "antd";
import UserInstall from "./UserInstall";
import GroupInstall from "./GroupInstall";
import RoleInstall from "./RoleInstall";

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
                    footer={null}
                >
                    <Tabs defaultActiveKey="1">
                        <TabPane tab="User" key="1">
                            <UserInstall onInstall={this.props.onInstall}/>
                        </TabPane>
                        <TabPane tab="Device" key="2">
                            Device install
                        </TabPane>
                        <TabPane tab="Role" key="3">
                            <RoleInstall onInstall={this.props.onInstall}/>
                        </TabPane>
                        <TabPane tab="Group" key="4">
                            <GroupInstall onInstall={this.props.onInstall}/>
                        </TabPane>
                    </Tabs>
                </Modal>
            </div>
        );
    }
}

export default AppInstallModal;