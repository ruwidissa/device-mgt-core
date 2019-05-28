import React from "react";
import '../../../../App.css';
import {Skeleton, Typography, Row, Col, Card} from "antd";
import {connect} from "react-redux";
import ReleaseView from "../../../../components/apps/release/ReleaseView";
import {getRelease, setLoading} from "../../../../js/actions";

const {Title} = Typography;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'store',
    },
    {
        path: 'first',
        breadcrumbName: 'Dashboard',
    },
    {
        path: 'second',
        breadcrumbName: 'Apps',
    },
];

const mapStateToProps = state => {
    return {
        release: state.release,
        releaseLoading: state.loadingState.release
    }
};

const mapDispatchToProps = dispatch => ({
    getRelease: (uuid) => dispatch(getRelease(uuid)),
    setLoading: (stateToLoad) => dispatch(setLoading(stateToLoad))
});

class ConnectedRelease extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;

    }

    componentDidMount() {
        const {uuid} = this.props.match.params;
        this.props.setLoading("release");
        this.props.getRelease(uuid);
    }

    render() {

        const release = this.props.release;
        let content = <Title level={3}>No Releases Found</Title>;

        if (release != null) {
            content = <ReleaseView release={release}/>;
        }


        return (
            <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                <Row style={{padding: 10}}>
                    <Col lg={4}>

                    </Col>
                    <Col lg={16} md={24} style={{padding: 3}}>
                        <Card>
                            <Skeleton loading={this.props.releaseLoading} avatar={{size: 'large'}} active paragraph={{rows: 8}}>
                                {content}
                            </Skeleton>
                        </Card>
                    </Col>
                </Row>

            </div>
        );


        // //todo remove uppercase
        // return (
        //     <div>
        //         <div className="main-container">
        //             <Row style={{padding: 10}}>
        //                 <Col lg={4}>
        //
        //                 </Col>
        //                 <Col lg={16} md={24} style={{padding: 3}}>
        //                     <Card>
        //                         <ReleaseView release={release}/>
        //                     </Card>
        //                 </Col>
        //             </Row>
        //         </div>
        //     </div>
        //
        // );
    }
}

const Release = connect(mapStateToProps, mapDispatchToProps)(ConnectedRelease);

export default Release;
