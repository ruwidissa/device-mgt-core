import React from "react";
import {PageHeader, Typography, Input, Button, Row, Col, Avatar, Card} from "antd";
import {connect} from "react-redux";
import ReleaseView from "../../../../components/apps/release/ReleaseView";
import {getRelease} from "../../../../js/actions";
import LifeCycle from "../../../../components/apps/release/LifeCycle";

const Search = Input.Search;
const {Title} = Typography;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'Publisher',
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
    return {release: state.release}
};

const mapDispatchToProps = dispatch => ({
    getRelease: (uuid) => dispatch(getRelease(uuid))
});

class ConnectedRelease extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;

    }

    componentDidMount() {
        const {uuid} = this.props.match.params;
        this.props.getRelease(uuid);
    }

    render() {

        const release = this.props.release;
        if (release == null) {
            return (
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Title level={3}>No Releases Found</Title>
                </div>
            );
        }
        return (
            <div>
                <PageHeader
                    breadcrumb={{routes}}
                />
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Row style={{padding: 10}}>
                        <Col span={16}>
                            <Card>
                                <ReleaseView release={release}/>
                            </Card>
                        </Col>
                        <Col span={8}>
                            <Card>
                               <LifeCycle/>
                            </Card>
                        </Col>
                    </Row>
                </div>

            </div>

        );
    }
}

const Release = connect(mapStateToProps,mapDispatchToProps)(ConnectedRelease);

export default Release;
