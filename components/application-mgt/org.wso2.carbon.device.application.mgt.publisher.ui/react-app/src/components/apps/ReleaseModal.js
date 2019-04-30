import React from "react";
import {Modal, Typography} from 'antd';
import { connect } from 'react-redux';

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
        if(this.props.releaseView.app != null){
            const app = this.props.app;

            return (
                <div>
                    <Modal
                        title={app.title}
                        visible={this.state.visible}
                        onOk={this.handleOk}
                        onCancel={this.handleCancel}
                    >
                        <p>Some contents...</p>
                        <p>Some contents...</p>
                        <p>Some contents...</p>
                    </Modal>
                </div>
            );
        }else {
            return null;
        }
    }
}

const ReleaseModal = connect(mapStateToProps,null)(ConnectedReleaseModal);

export default ReleaseModal;