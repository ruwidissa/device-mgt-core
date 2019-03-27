import React from "react";
import "antd/dist/antd.css";
import {PageHeader, Typography, Card, Steps, Button, message} from "antd";

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
    content: 'First-content',
}, {
    title: 'Second',
    content: 'Second-content',
}, {
    title: 'Last',
    content: 'Last-content',
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
        const { current } = this.state;
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
                    <Card>
                        <div>
                            <Steps current={current}>
                                {steps.map(item => <Step key={item.title} title={item.title}/>)}
                            </Steps>
                            <div className="steps-content">{steps[current].content}</div>
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
                </div>

            </div>

        );
    }
}

export default AddNewApp;
