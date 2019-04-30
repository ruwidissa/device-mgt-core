import React from "react";
import {Modal, Button} from 'antd';
import { connect } from 'react-redux';

// connecting state.releaseView with the component
const mapStateToProps = state => {
    return {releaseView: state.releaseView}
};

class ConnectedReleaseModal extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            visible: false
        };
    }
    componentWillReceiveProps(nextProps) {
        if (nextProps !== this.props) {
            this.setState({
               visible: nextProps.releaseView.visible
            })
        }
    }

    showModal = () => {
        this.setState({
            visible: true,
        });
    };

    handleOk = (e) => {
        console.log(e);
        this.setState({
            visible: false,
        });
    };

    handleCancel = (e) => {
        console.log(e);
        this.setState({
            visible: false,
        });
    };

    render() {
        return (
            <div>
                <Button type="primary" onClick={this.showModal}>
                    Open Modal
                </Button>
                <Modal
                    title={this.props.releaseView.title}
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
    }
}

const ReleaseModal = connect(mapStateToProps,null)(ConnectedReleaseModal);

export default ReleaseModal;