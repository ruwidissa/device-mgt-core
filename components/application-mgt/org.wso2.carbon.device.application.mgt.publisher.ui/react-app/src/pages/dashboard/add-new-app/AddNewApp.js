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
import axios from "axios";
import AddNewAppForm from "../../../components/new-app/AddNewAppForm"
import config from "../../../../public/conf/config.json";

const Paragraph = Typography;
const Dragger = Upload.Dragger;

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

//
// const steps = [{
//     title: 'First',
//     content: Step1
// }, {
//     title: 'Second',
//     content: Step2,
// }, {
//     title: 'Last',
//     content: Step3,
// }];


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

class AddNewApp extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            current: 0,
            categories: []
        };
    }

    componentDidMount() {
        // this.getCategories();
    }

    next() {
        const current = this.state.current + 1;
        this.setState({current});
    }

    prev() {
        const current = this.state.current - 1;
        this.setState({current});
    }

    getCategories = () => {
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/categories",
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }).then(res => {
            if (res.status === 200) {
                let categories = JSON.parse(res.data.data);
                this.setState({
                    categories: categories,
                    loading: false
                });
            }

        }).catch((error) => {
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                message.warning('Something went wrong');

            }
            this.setState({
                loading: false
            });
        });
    };

    handleCategoryChange = (value) => {
        console.log(`selected ${value}`);
    };


    render() {
        const {categories} = this.state;
        return (
            <div>
                <PageHeader
                    title="Add New App"
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
                    <AddNewAppForm/>
                </div>

            </div>

        );
    }
}

export default AddNewApp;
