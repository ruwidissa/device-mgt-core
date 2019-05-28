import React from "react";
import {Form, Input, Button, Select, Divider, Tag, Tooltip, Icon, Checkbox, Row, Col} from "antd";
import styles from './Style.less';

const { Option } = Select;
const { TextArea } = Input;
const InputGroup = Input.Group;

const formItemLayout = {
    labelCol: {
        span: 8,
    },
    wrapperCol: {
        span: 16,
    },
};

class EditableTagGroup extends React.Component {
    state = {
        tags: [],
        inputVisible: false,
        inputValue: '',
    };

    handleClose = (removedTag) => {
        const tags = this.state.tags.filter(tag => tag !== removedTag);
        console.log(tags);
        this.setState({ tags });
    }

    showInput = () => {
        this.setState({ inputVisible: true }, () => this.input.focus());
    }

    handleInputChange = (e) => {
        this.setState({ inputValue: e.target.value });
    }

    handleInputConfirm = () => {
        const { inputValue } = this.state;
        let { tags } = this.state;
        if (inputValue && tags.indexOf(inputValue) === -1) {
            tags = [...tags, inputValue];
        }
        console.log(tags);
        this.setState({
            tags,
            inputVisible: false,
            inputValue: '',
        });
    }

    saveInputRef = input => this.input = input

    render() {
        const { tags, inputVisible, inputValue } = this.state;
        return (
            <div>
                {tags.map((tag, index) => {
                    const isLongTag = tag.length > 20;
                    const tagElem = (
                        <Tag key={tag} closable={index !== 0} onClose={() => this.handleClose(tag)}>
                            {isLongTag ? `${tag.slice(0, 20)}...` : tag}
                        </Tag>
                    );
                    return isLongTag ? <Tooltip title={tag} key={tag}>{tagElem}</Tooltip> : tagElem;
                })}
                {inputVisible && (
                    <Input
                        ref={this.saveInputRef}
                        type="text"
                        size="small"
                        style={{ width: 78 }}
                        value={inputValue}
                        onChange={this.handleInputChange}
                        onBlur={this.handleInputConfirm}
                        onPressEnter={this.handleInputConfirm}
                    />
                )}
                {!inputVisible && (
                    <Tag
                        onClick={this.showInput}
                        style={{ background: '#fff', borderStyle: 'dashed' }}
                    >
                        <Icon type="plus" /> New Tag
                    </Tag>
                )}
            </div>
        );
    }
}

class Step1 extends React.Component {
    render() {
        console.log("hhhoohh");
        return (
            <div>
                <Form layout="horizontal" className={styles.stepForm} hideRequiredMark>

                    <Form.Item {...formItemLayout} label="Platform">
                            <Select placeholder="ex: android">
                                <Option value="Android">Android</Option>
                                <Option value="iOS">iOS</Option>
                            </Select>
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Type">
                            <Select value="Enterprise">
                                <Option value="Enterprise" selected>Enterprise</Option>
                            </Select>
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Name">
                        <Input placeholder="App Name" />
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Description">
                        <TextArea placeholder="Enter the description" rows={4} />
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Category">
                        <Select placeholder="Select a category">
                            <Option value="travel">Travel</Option>
                            <Option value="entertainment">Entertainment</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Tags">
                        <EditableTagGroup/>
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Price">
                        <Input prefix="$" placeholder="00.00" />
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Share with all tenents?">
                        <Checkbox > </Checkbox>
                    </Form.Item>
                    <Form.Item {...formItemLayout} label="Meta Daa">
                        <InputGroup>
                            <Row gutter={8}>
                                <Col span={5}>
                                    <Input placeholder="Key" />
                                </Col>
                                <Col span={10}>
                                    <Input placeholder="value" />
                                </Col>
                                <Col span={4}>
                                    <Button type="dashed" shape="circle" icon="plus" />
                                </Col>
                            </Row>
                        </InputGroup>
                    </Form.Item>
                </Form>
            </div>
        );
    }
}

export default Step1;