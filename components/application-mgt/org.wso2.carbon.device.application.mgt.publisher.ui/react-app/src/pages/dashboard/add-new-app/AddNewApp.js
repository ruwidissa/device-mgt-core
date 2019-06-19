import React from "react";
import "antd/dist/antd.css";
import {
    PageHeader,
    Typography,
    Card,
    Steps,
    Button,
    message,
    Row,
    Col,
    Tag,
    Tooltip,
    Input,
    Icon,
    Select,
    Switch,
    Form,
    Upload,
    Divider
} from "antd";
import Step1 from "./Step1";
import Step2 from "./Step2";
import Step3 from "./Step3";
import styles from "./Style.less";
import IconImage from "./IconImg";
import UploadScreenshots from "./UploadScreenshots";

const Paragraph = Typography;
const Dragger = Upload.Dragger;
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

const props = {
    name: 'file',
    multiple: false,
    action: '//jsonplaceholder.typicode.com/posts/',
    onChange(info) {
        const status = info.file.status;
        if (status !== 'uploading') {
            console.log(info.file, info.fileList);
        }
        if (status === 'done') {
            message.success(`${info.file.name} file uploaded successfully.`);
        } else if (status === 'error') {
            message.error(`${info.file.name} file upload failed.`);
        }
    },
};

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


const {Option} = Select;
const {TextArea} = Input;
const InputGroup = Input.Group;

const formItemLayout = {
    labelCol: {
        span: 4,
    },
    wrapperCol: {
        span: 20,
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
        this.setState({tags});
    }

    showInput = () => {
        this.setState({inputVisible: true}, () => this.input.focus());
    }

    handleInputChange = (e) => {
        this.setState({inputValue: e.target.value});
    }

    handleInputConfirm = () => {
        const {inputValue} = this.state;
        let {tags} = this.state;
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
        const {tags, inputVisible, inputValue} = this.state;
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
                        style={{width: 78}}
                        value={inputValue}
                        onChange={this.handleInputChange}
                        onBlur={this.handleInputConfirm}
                        onPressEnter={this.handleInputConfirm}
                    />
                )}
                {!inputVisible && (
                    <Tag
                        onClick={this.showInput}
                        style={{background: '#fff', borderStyle: 'dashed'}}
                    >
                        <Icon type="plus"/> New Tag
                    </Tag>
                )}
            </div>
        );
    }
}

class AddNewApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
        };
    }

    tags = [];

    addTag(key, value){
        this.tags.push(<Option key={key}>{value}</Option>);
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
        this.addTag('1','Lorem');
        this.addTag('2','Ipsum');
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
                        <Col span={20} offset={2}>
                            <Card>
                                <Row>
                                    <Col span={12}>
                                        <div>
                                            <Form labelAlign="left" layout="horizontal" className={styles.stepForm}
                                                  hideRequiredMark>
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
                                                <Form.Item {...formItemLayout} label="App Name">
                                                    <Input placeholder="ex: Lorem App"/>
                                                </Form.Item>
                                                <Form.Item {...formItemLayout} label="Description">
                                                    <TextArea placeholder="Enter the description..." rows={7}/>
                                                </Form.Item>
                                                <Form.Item {...formItemLayout} label="Category">
                                                    <Select placeholder="Select a category">
                                                        <Option value="travel">Travel</Option>
                                                        <Option value="entertainment">Entertainment</Option>
                                                    </Select>
                                                </Form.Item>
                                                <Form.Item {...formItemLayout} label="Price">
                                                    <Input prefix="$" placeholder="00.00"/>
                                                </Form.Item>
                                                <Form.Item {...formItemLayout} label="Is Sahred?">
                                                    <Switch checkedChildren={<Icon type="check" />} unCheckedChildren={<Icon type="close" />} defaultChecked />
                                                </Form.Item>
                                                <Divider/>
                                                <Form.Item {...formItemLayout} label="Tags">

                                                    <InputGroup>
                                                        <Row gutter={8}>
                                                            <Col span={22}>
                                                                <Select
                                                                    mode="multiple"
                                                                    style={{ width: '100%' }}
                                                                    placeholder="Tags Mode"
                                                                >
                                                                    {this.tags}
                                                                </Select>
                                                            </Col>
                                                            <Col span={2}>
                                                                <Button type="dashed" shape="circle" icon="plus"/>
                                                            </Col>
                                                        </Row>
                                                    </InputGroup>
                                                </Form.Item>
                                                <Form.Item {...formItemLayout} label="Meta Daa">
                                                    <InputGroup>
                                                        <Row gutter={8}>
                                                            <Col span={10}>
                                                                <Input placeholder="Key"/>
                                                            </Col>
                                                            <Col span={12}>
                                                                <Input placeholder="value"/>
                                                            </Col>
                                                            <Col span={2}>
                                                                <Button type="dashed" shape="circle" icon="plus"/>
                                                            </Col>
                                                        </Row>
                                                    </InputGroup>
                                                </Form.Item>
                                            </Form>
                                        </div>
                                    </Col>
                                    <Col span={12} style={{paddingTop: 40, paddingLeft: 20}}>
                                        <p>Application</p>
                                        <div style={{height: 170}}>
                                            <Dragger {...props}>
                                                <p className="ant-upload-drag-icon">
                                                    <Icon type="inbox"/>
                                                </p>
                                                <p className="ant-upload-text">Click or drag file to this area to
                                                    upload</p>
                                                <p className="ant-upload-hint">Support for a single or bulk upload.
                                                    Strictly prohibit from uploading company data or other band
                                                    files</p>
                                            </Dragger>
                                        </div>
                                        <Row style={{marginTop: 40}}>
                                            <Col span={12}>
                                                <p>Icon</p>
                                                <IconImage/>
                                            </Col>
                                            <Col span={12}>
                                                <p>Banner</p>
                                                <IconImage/>
                                            </Col>
                                        </Row>


                                        <Row style={{marginTop: 40}}>
                                            <Col span={24}>
                                                <p>Screenshots</p>
                                                <UploadScreenshots/>
                                            </Col>
                                        </Row>

                                    </Col>

                                </Row>
                            </Card>
                        </Col>
                    </Row>

                </div>

            </div>

        );
    }
}

export default AddNewApp;
