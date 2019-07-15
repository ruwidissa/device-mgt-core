import React from "react";
import {Modal, Button, Tag, Collapse, List,Typography} from 'antd';
import pSBC from "shade-blend-color";
import config from "../../../../../../public/conf/config.json";

const {Text} = Typography;
const lifeCycleConfig = config.lifecycle;

class LifeCycleDetailsModal extends React.Component {

    constructor(props) {
        super(props);
        this.state = {visible: false};
    }

    showModal = () => {
        this.setState({
            visible: true,
        });
    };

    handleCancel = e => {
        console.log(e);
        this.setState({
            visible: false,
        });
    };

    render() {
        const {lifecycle} = this.props;
        return (
            <div>
                <Button
                    size="small"
                    icon="question-circle"
                    onClick={this.showModal}
                >
                    Learn more
                </Button>
                <Modal
                    title="Lifecycle"
                    visible={this.state.visible}
                    footer = {null}
                    onCancel={this.handleCancel}
                >

                    <List
                        itemLayout="horizontal"
                        dataSource={Object.keys(lifecycle)}
                        renderItem={lifecycleState => {
                            let text = "";
                            let footerText = "";
                            let nextProceedingStates = [];

                            if (lifeCycleConfig.hasOwnProperty(lifecycleState)) {
                                text = lifeCycleConfig[lifecycleState].text;
                            }
                            if (lifecycle[lifecycleState].hasOwnProperty("proceedingStates")) {
                                nextProceedingStates = lifecycle[lifecycleState].proceedingStates;
                                footerText = "You can only proceed to one of the following states:"
                            }

                            return (
                                <List.Item>
                                    <List.Item.Meta
                                        title={lifecycleState}
                                    />
                                    {text}
                                    <br/>
                                    <Text type="secondary">{footerText}</Text>
                                    <div>
                                        {
                                            nextProceedingStates.map(lifecycleState => {
                                                return (
                                                    <Tag
                                                        key={lifecycleState}
                                                        style={{margin: 5}}
                                                        color={pSBC(0.30, config.theme.primaryColor)}
                                                    >
                                                        {lifecycleState}
                                                    </Tag>
                                                )
                                            })
                                        }
                                    </div>
                                </List.Item>
                            )
                        }}
                    />
                </Modal>
            </div>
        );
    }
}

export default LifeCycleDetailsModal;
