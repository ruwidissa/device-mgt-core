import React from "react";
import {Modal, Typography,List, Avatar} from 'antd';
import {connect} from 'react-redux';

// connecting state.releaseView with the component
const mapStateToProps = state => {
    console.log(state);
    return {
        nextState: state.lifecycleModal.nextState,
        visible: state.lifecycleModal.visible
    }
};

const Text = Typography;

class ConnectedLifecycleModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            visible: false
        };
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps !== this.props) {
            console.log(nextProps);
            this.setState({
                visible: nextProps.visible
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
        if (this.props.nextState != null) {
            const nextState = this.props.nextState;
            return (
                <div>
                    <Modal
                        title="Change State"
                        visible={this.state.visible}
                        onOk={this.handleOk}
                        onCancel={this.handleCancel}
                    >
                        <p>Some contents...</p>
                    </Modal>
                </div>
            );
        } else {
            return null;
        }
    }
}

const LifecycleModal = connect(mapStateToProps, null)(ConnectedLifecycleModal);

export default LifecycleModal;