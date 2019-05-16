import React from "react";
import {Graph} from 'react-d3-graph';
import {connect} from "react-redux";
import {getLifecycle, openLifecycleModal} from "../../../js/actions";

const mapDispatchToProps = dispatch => ({
    openLifecycleModal: (nextState) => dispatch(openLifecycleModal(nextState))
});

// the graph configuration, you only need to pass down properties
// that you want to override, otherwise default ones will be used
const myConfig = {
    nodeHighlightBehavior: true,
    directed: true,
    height: 400,
    d3: {
        alphaTarget: 0.05,
        gravity: -200,
        linkLength: 200,
        linkStrength: 1
    },
    node: {
        color: "#d3d3d3",
        fontColor: "black",
        fontSize: 12,
        fontWeight: "normal",
        highlightFontSize: 12,
        highlightFontWeight: "bold",
        highlightStrokeColor: "SAME",
        highlightStrokeWidth: 1.5,
        labelProperty: "id",
        mouseCursor: "pointer",
        opacity: 1,
        strokeColor: "none",
        strokeWidth: 1.5,
        svg: "",
        symbolType: "circle",
    },
    link: {
        highlightColor: 'lightblue'
    }
};



class ConnectedLifeCycleGraph extends React.Component {


    constructor(props){
        super(props);
        this.nextStates = null;
        this.onClickNode = this.onClickNode.bind(this);
    }

    onClickNode = function(nodeId) {
        const nextStates = this.nextStates;
        if(nextStates.includes(nodeId)){
            this.props.openLifecycleModal(nodeId);
        }
    };

    render() {
// graph payload (with minimalist structure)

        const lifecycle = this.props.lifecycle;
        const nodes = [];
        const links = [];
        this.nextStates = lifecycle[this.props.currentStatus].proceedingStates;


        Object.keys(lifecycle).forEach((stateName) => {
            const state = lifecycle[stateName];
            let color = "rgb(83, 92, 104)";
            if (stateName === this.props.currentStatus) {
                color = "rgb(39, 174, 96)";
            } else if (this.nextStates.includes(stateName)) {
                color = "rgb(0,192,255)";
            }
            let node = {
                id: stateName,
                color: color
            };
            nodes.push(node);

            //todo: remove checking property
            if (state.hasOwnProperty("proceedingStates")) {
                state.proceedingStates.forEach((proceedingState) => {
                    let link = {
                        source: stateName,
                        target: proceedingState
                    };
                    links.push(link);
                });
            }
        });

        const data = {
            nodes: nodes,
            links: links
        };


        return (
            <div>
                <Graph
                    id="graph-id" // id is mandatory, if no id is defined rd3g will throw an error
                    data={data}
                    config={myConfig}
                    onClickNode={this.onClickNode}
                />
            </div>
        );
    }
}

const LifeCycleGraph = connect(null,mapDispatchToProps)(ConnectedLifeCycleGraph);
export default LifeCycleGraph;