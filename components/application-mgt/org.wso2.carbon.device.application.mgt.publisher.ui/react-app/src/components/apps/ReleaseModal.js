import React from "react";
import {Modal, Typography,List, Avatar} from 'antd';
import {connect} from 'react-redux';

// connecting state.releaseView with the component
const mapStateToProps = state => {
    return {releaseView: state.releaseView}
};

const Text = Typography;

class ConnectedReleaseModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            visible: false,
            app: null
        };
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps !== this.props) {
            this.setState({
                visible: nextProps.releaseView.visible,
                app: nextProps.releaseView.app
            })
        }
    }

    showModal = () => {
        this.setState({
            visible: true,
        });
    };

    handleOk = (e) => {
        this.setState({
            visible: false,
        });
    };

    handleCancel = (e) => {
        this.setState({
            visible: false,
        });
    };

    render() {
        if (this.props.releaseView.app != null) {
            const app = this.props.releaseView.app;
            return (
                <div>
                    <Modal
                        title={app.name}
                        visible={this.state.visible}
                        onOk={this.handleOk}
                        onCancel={this.handleCancel}
                    >
                        <p>Some contents...</p>
                        <List
                        itemLayout="horizontal"
                        dataSource={app.applicationReleases}
                        renderItem={item => (
                            <List.Item>
                                <List.Item.Meta
                                    avatar={<Avatar src={item.iconPath} />}
                                    title={<a href="https://ant.design">Title</a>}
                                    description={item.description}
                                />
                            </List.Item>
                        )}
                    />
                    </Modal>
                </div>
            );
        } else {
            return null;
        }
    }
}

const ReleaseModal = connect(mapStateToProps, null)(ConnectedReleaseModal);

export default ReleaseModal;