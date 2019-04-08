import React from "react";
import "antd/dist/antd.css";
import {PageHeader, Typography, Card, Steps, Button, message, Row, Col} from "antd";
import Step1 from "./Step1"
import Step2 from "./Step2"
import Step3 from "./Step3"

const Paragraph = Typography;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'publisher',
    },
    {
        path: 'first',
        breadcrumbName: 'dashboard',
    },
    {
        path: 'second',
        breadcrumbName: 'add new app',
    },
];

const Step = Steps.Step;

const steps = [{
    title: 'First',
    content: Step1
}, {
    title: 'Second',
    content: Step2,
}, {
    title: 'Last',
    content: Step3,
}];


class AddNewApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
        };
    }

    next() {
        const current = this.state.current + 1;
        this.setState({current});
    }

    prev() {
        const current = this.state.current - 1;
        this.setState({current});
    }


    render() {
        const {current} = this.state;
        const Content = steps[current].content;
        return (
            <div>
                <PageHeader
                    title="Add New App"
                    breadcrumb={{routes}}
                >
                    <div className="wrap">
                        <div className="content">
                            <Paragraph>
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempo.
                            </Paragraph>
                        </div>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>
                    <Row>
                        <Col span={16} offset={4}>
                            <Card>
                                <div>
                                    <Steps current={current}>
                                        {steps.map(item => <Step key={item.title} title={item.title}/>)}
                                    </Steps>
                                    <Content/>
                                    <div className="steps-action">
                                        {
                                            current < steps.length - 1
                                            && <Button type="primary" onClick={() => this.next()}>Next</Button>
                                        }
                                        {
                                            current === steps.length - 1
                                            && <Button type="primary"
                                                       onClick={() => message.success('Processing complete!')}>Done</Button>
                                        }
                                        {
                                            current > 0
                                            && (
                                                <Button style={{marginLeft: 8}} onClick={() => this.prev()}>
                                                    Previous
                                                </Button>
                                            )
                                        }
                                    </div>
                                </div>
                            </Card>
                        </Col>
                    </Row>

                </div>

            </div>

        );
    }
}

export default AddNewApp;
