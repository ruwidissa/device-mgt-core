import React from "react";
import {Modal, Tabs} from "antd";
import UserInstall from "./UserInstall";
import GroupInstall from "./GroupInstall";
import RoleInstall from "./RoleInstall";
import DeviceInstall from "./DeviceInstall";

const { TabPane } = Tabs;

class AppInstallModal extends React.Component{
    state={
        data:[]
    };
    render() {
        const {deviceType} = this.props;
        return (
            <div>
                <Modal
                    title="Install App"
                    visible={this.props.visible}
                    onCancel={this.props.onClose}
                    footer={null}
                >
                    <Tabs defaultActiveKey="device">
                        <TabPane tab="Device" key="device">
                            <DeviceInstall deviceType={deviceType} onInstall={this.props.onInstall}/>
                        </TabPane>
                        <TabPane tab="User" key="user">
                            <UserInstall onInstall={this.props.onInstall}/>
                        </TabPane>
                        <TabPane tab="Role" key="role">
                            <RoleInstall onInstall={this.props.onInstall}/>
                        </TabPane>
                        <TabPane tab="Group" key="group">
                            <GroupInstall onInstall={this.props.onInstall}/>
                        </TabPane>
                    </Tabs>
                </Modal>
            </div>
        );
    }
}

export default AppInstallModal;