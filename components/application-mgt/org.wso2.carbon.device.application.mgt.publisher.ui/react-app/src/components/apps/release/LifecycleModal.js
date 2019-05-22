import React from "react";
import {Modal, Typography, Icon, Input, Form, Checkbox, Button} from 'antd';
import {connect} from 'react-redux';
import {closeLifecycleModal, updateLifecycleState} from "../../../js/actions";

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
    closeLifecycleModal : () => dispatch(closeLifecycleModal()),
    updateLifecycleState : (uuid, nextState, reason) => dispatch(updateLifecycleState(uuid, nextState, reason))
});

const Text = Typography;

class ConnectedLifecycleModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            visible: false
        };
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps !== this.props) {
            this.setState({
                visible: nextProps.visible,
                loading: false
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
        this.props.closeLifecycleModal();
    };

    handleCancel = (e) => {
        this.setState({
            visible: false,
            loading: false
        });
        this.props.closeLifecycleModal();
    };
    handleSubmit = event => {
        this.setState({ loading: true });
        event.preventDefault();
        console.log(this.reason);
        console.log("uuid", this.props.uuid);
        this.props.updateLifecycleState(this.props.uuid, this.props.nextState, this.reason.state.value)
    };

    render() {
        if (this.props.nextState != null) {
            const nextState = this.props.nextState;
            return (
                <div>
                    <Modal
                        title="Change State"
                        visible={this.state.visible}
                        onCancel={this.handleCancel}
                        footer={null}
                    >
                        <Title level={4}>{this.props.currentStatus} <Icon type="arrow-right" /> {nextState}</Title>
                        <form onSubmit={this.handleSubmit}>
                            <Form.Item>
                                <label htmlFor="username">Reason</label>

                                <Input placeholder="Enter the reason"  ref={(input) => this.reason = input}/>
                            </Form.Item>
                            {/*<Form.Item>*/}
                            {/*<TextArea*/}
                                {/*placeholder="Please enter the reason..."*/}
                                {/*ref={(input) => this.input = input}*/}
                                {/*autosize*/}
                            {/*/>*/}
                            {/*</Form.Item>*/}
                            <Button key="back" onClick={this.handleCancel}>
                                Cancel
                            </Button>,
                            <Button key="submit" type="primary" htmlType="submit" loading={this.state.loading}>
                                Submit
                            </Button>
                        </form>
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