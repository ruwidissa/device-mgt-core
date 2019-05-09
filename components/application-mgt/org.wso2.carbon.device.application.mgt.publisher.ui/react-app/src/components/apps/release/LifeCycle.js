import React from "react";
import LifeCycleGraph from "./LifeCycleGraph";
import {connect} from "react-redux";
import {getLifecycle, openLifecycleModal} from "../../../js/actions";
import {Button} from "antd";
import LifecycleModal from "./LifecycleModal";

const mapDispatchToProps = dispatch => ({
    getLifecycle: () => dispatch(getLifecycle()),
    openLifecycleModal: (nextState) => dispatch(openLifecycleModal(nextState))
});

const mapStateToProps = state => {
    return {
        lifecycle: state.lifecycle,
        currentStatus : state.release.currentStatus.toUpperCase()
    };
};

class ConnectedLifeCycle extends React.Component {

    constructor(props){
        super(props);

        this.openModal = this.openModal.bind(this);
    }

    componentDidMount() {
        this.props.getLifecycle();
    }

    openModal() {
        this.props.openLifecycleModal("IN_REVIEW");
    }

    render() {
        const lifecycle = this.props.lifecycle;
        if (lifecycle != null) {
            return (
                <div>
                    <LifecycleModal currentStatus={this.props.currentStatus}/>
                    <Button onClick={this.openModal}>aaaa</Button>
                    <LifeCycleGraph currentStatus={this.props.currentStatus} lifecycle={this.props.lifecycle}/>
                </div>
            );

        } else {
            return null;
        }
    }
}

const LifeCycle = connect(mapStateToProps, mapDispatchToProps)(ConnectedLifeCycle);

export default LifeCycle;