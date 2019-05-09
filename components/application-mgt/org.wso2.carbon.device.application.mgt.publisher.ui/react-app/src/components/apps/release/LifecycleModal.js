import React from "react";
import {Modal, Typography,Icon,Input} from 'antd';
import {connect} from 'react-redux';
import {closeLifecycleModal} from "../../../js/actions";

const { TextArea } = Input;
const { Title } = Typography;

// connecting state.releaseView with the component
const mapStateToProps = state => {
    return {
        nextState: state.lifecycleModal.nextState,
        visible: state.lifecycleModal.visible
    }
};

const mapDispatchToProps = dispatch => ({
    closeLifecycleModal : () => dispatch(closeLifecycleModal())
});

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
        this.props.closeLifecycleModal();
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
                        <Title level={4}>{this.props.currentStatus} <Icon type="arrow-right" /> {nextState}</Title>
                        <p>Reason:</p>
                        <TextArea placeholder="Please enter the reason..." autosize />
                    </Modal>
                </div>
            );
        } else {
            return null;
        }
    }
}

const LifecycleModal = connect(mapStateToProps, mapDispatchToProps)(ConnectedLifecycleModal);

export default LifecycleModal;