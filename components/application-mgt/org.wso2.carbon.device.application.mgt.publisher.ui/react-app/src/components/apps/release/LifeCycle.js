import React from "react";
import LifeCycleGraph from "./LifeCycleGraph";
import {connect} from "react-redux";
import {getLifecycle, openReleasesModal} from "../../../js/actions";

const mapDispatchToProps = dispatch => ({
    getLifecycle: () => dispatch(getLifecycle())
});

const mapStateToProps = state => {
    return {
        lifecycle: state.lifecycle
    };
};

class ConnectedLifeCycle extends React.Component {
    componentDidMount() {
        this.props.getLifecycle();
    }

    render() {
        console.log();
        const lifecycle = this.props.lifecycle;
        if(lifecycle != null){
            return (
                <LifeCycleGraph currentStatus={this.props.currentStatus} lifecycle={this.props.lifecycle}/>
            );

        }else {
            return null;
        }
    }
}

const LifeCycle = connect(mapStateToProps, mapDispatchToProps)(ConnectedLifeCycle);

export default LifeCycle;