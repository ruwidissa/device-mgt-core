import React from "react";
import {Card, Tag, message, Icon, Input, notification, Divider, Button, Spin, Tooltip, Popconfirm, Modal} from "antd";
import axios from "axios";
import config from "../../../../public/conf/config.json";
import {TweenOneGroup} from 'rc-tween-one';


class ManageTags extends React.Component {
    state = {
        loading: false,
        searchText: '',
        tags: [],
        tempElements: [],
        inputVisible: false,
        inputValue: '',
        isAddNewVisible: false,
        isEditModalVisible: false,
        currentlyEditingId: null,
        editingValue: null
    };

    componentDidMount() {
        axios.get(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri+"/applications/tags",
            {
                headers: { 'X-Platform': config.serverConfig.platform }
            }).then(res => {
            if (res.status === 200) {
                let tags = JSON.parse(res.data.data);
                this.setState({
                    tags: tags,
                    loading: false
                });
            }

        }).catch((error) => {
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/publisher/login';
            } else {
                message.warning('Something went wrong');

            }
            this.setState({
                loading: false
            });
        });
    }

    handleCloseButton = () => {
        this.setState({
            tempElements: [],
            isAddNewVisible: false
        });
    };

    deleteTag = (id) => {

        this.setState({
            loading: true
        });

        axios.delete(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri+"/admin/applications/tags/"+id,
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: "Done!",
                    description:
                        "Tag Removed Successfully!",
                });

                const {tags} = this.state;
                const remainingElements = tags.filter(function (value) {
                    return value.tagName !== id;

                });

                this.setState({
                    loading: false,
                    tags: remainingElements
                });
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty("status") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/publisher/login';
            } else {
                message.warning('Something went wrong');
            }
            this.setState({
                loading: false
            });
        });
    };

    renderElement = (tag) => {
        const tagName = tag.tagName;
        const tagElem = (
            <Tag
                color="gold"
            >
                {tagName}
                <Divider type="vertical"/>
                <Tooltip title="edit">
                    <Icon onClick={() => {
                        this.openEditModal(tagName)
                    }} style={{color: 'rgba(0,0,0,0.45)'}} type="edit"/>
                </Tooltip>
                <Divider type="vertical"/>
                <Tooltip title="delete">
                    <Popconfirm
                        title="Are you sure delete this tag?"
                        onConfirm={() => {
                            if (tag.isTagDeletable) {
                                this.deleteTag(tagName);
                            } else {
                                notification["error"]({
                                    message: 'Cannot delete "' + tagName + '"',
                                    description:
                                        "This tag is currently used. Please unassign the tag from apps.",
                                });
                            }
                        }}
                        okText="Yes"
                        cancelText="No"
                    >
                        <Icon style={{color: 'rgba(0,0,0,0.45)'}} type="delete"/>
                    </Popconfirm>
                </Tooltip>
            </Tag>
        );
        return (
            <span key={tag.tagName} style={{display: 'inline-block'}}>
                {tagElem}
            </span>
        );
    };

    renderTempElement = (tag) => {
        const {tempElements} = this.state;
        const tagElem = (
            <Tag
                closable
                onClose={e => {
                    e.preventDefault();
                    const remainingElements = tempElements.filter(function (value) {

                        return value.tagName !== tag.tagName;

                    });
                    this.setState({
                        tempElements: remainingElements
                    });
                }}
            >
                {tag.tagName}
            </Tag>
        );
        return (
            <span key={tag.tagName} style={{display: 'inline-block'}}>
                {tagElem}
            </span>
        );
    };

    showInput = () => {
        this.setState({inputVisible: true}, () => this.input.focus());
    };

    handleInputChange = e => {
        this.setState({inputValue: e.target.value});
    };

    handleInputConfirm = () => {
        const {inputValue, tags} = this.state;
        let {tempElements} = this.state;
        if (inputValue) {
            if ((tags.findIndex(i => i.tagName === inputValue) === -1) && (tempElements.findIndex(i => i.tagName === inputValue) === -1)) {
                tempElements = [...tempElements, {tagName: inputValue, isTagDeletable: true}];
            } else {
                message.warning('Tag already exists');
            }
        }

        this.setState({
            tempElements,
            inputVisible: false,
            inputValue: '',
        });
    };

    handleSave = () => {
        const {tempElements, tags} = this.state;
        this.setState({
            loading: true
        });

        const data = tempElements.map(tag => tag.tagName);

        axios.post(config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri+"/applications/tags",
            data,
            {
                headers: { 'X-Platform': config.serverConfig.platform }
            }).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: "Done!",
                    description:
                        "New tags were added successfully",
                });

                this.setState({
                    tags: [...tags, ...tempElements],
                    tempElements: [],
                    inputVisible: false,
                    inputValue: '',
                    loading: false,
                    isAddNewVisible: false
                });
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty("status") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/publisher/login';
            } else {
                message.warning('Something went wrong');
            }
            this.setState({
                loading: false
            });
        });


    };

    saveInputRef = input => (this.input = input);

    closeEditModal = e => {
        console.log(e);
        this.setState({
            isEditModalVisible: false,
            currentlyEditingId: null
        });
    };

    openEditModal = (id) => {
        this.setState({
            isEditModalVisible: true,
            currentlyEditingId: id,
            editingValue: id
        })
    };

    editItem = () => {

        const {editingValue, currentlyEditingId, tags} = this.state;

        this.setState({
            loading: true,
            isEditModalVisible: false,
        });

        axios.put(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri+"/applications/tags/rename?from="+currentlyEditingId+"&to="+editingValue,
            {},
            {
                headers: { 'X-Platform': config.serverConfig.platform }
            }
        ).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: "Done!",
                    description:
                        "Tag was edited successfully",
                });

                tags[tags.findIndex(i => i.tagName === currentlyEditingId)].tagName = editingValue;

                this.setState({
                    tags: tags,
                    loading: false,
                    editingValue: null
                });
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty("status") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort+'/publisher/login';
            } else {
                message.warning('Something went wrong');
            }
            this.setState({
                loading: false,
                editingValue: null
            });
        });


    };

    handleEditInputChange = (e) => {
        this.setState({
            editingValue: e.target.value
        });
    };

    render() {
        const {tags, inputVisible, inputValue, tempElements, isAddNewVisible} = this.state;
        const tagsElements = tags.map(this.renderElement);
        const temporaryElements = tempElements.map(this.renderTempElement);
        return (
            <div>
                <Card title="Tags">
                    <Spin tip="Working on it..." spinning={this.state.loading}>
                        {!isAddNewVisible &&
                        <Button
                            size="small"
                            onClick={() => {
                                this.setState({
                                    isAddNewVisible: true,
                                    inputVisible: true
                                }, () => this.input.focus())
                            }} htmlType="button">Add Tags
                        </Button>
                        }
                        {isAddNewVisible &&
                        <div>
                            <div style={{marginBottom: 16}}>
                                <TweenOneGroup
                                    enter={{
                                        scale: 0.8,
                                        opacity: 0,
                                        type: 'from',
                                        duration: 100,
                                        onComplete: e => {
                                            e.target.style = '';
                                        },
                                    }}
                                    leave={{opacity: 0, width: 0, scale: 0, duration: 200}}
                                    appear={false}
                                >
                                    {temporaryElements}

                                    {inputVisible && (
                                        <Input
                                            ref={this.saveInputRef}
                                            type="text"
                                            size="small"
                                            style={{width: 120}}
                                            value={inputValue}
                                            onChange={this.handleInputChange}
                                            onBlur={this.handleInputConfirm}
                                            onPressEnter={this.handleInputConfirm}
                                        />
                                    )}
                                    {!inputVisible && (
                                        <Tag onClick={this.showInput}
                                             style={{background: '#fff', borderStyle: 'dashed'}}>
                                            <Icon type="plus"/> New Tag
                                        </Tag>
                                    )}
                                </TweenOneGroup>
                            </div>
                            <div>
                                <Button
                                    onClick={this.handleSave}
                                    htmlType="button" type="primary"
                                    size="small"
                                    disabled={tempElements.length === 0}>
                                    Save
                                </Button>
                                <Divider type="vertical"/>
                                <Button
                                    onClick={this.handleCloseButton}
                                    size="small">
                                    Cancel
                                </Button>
                            </div>
                        </div>
                        }
                        <Divider dashed="true"/>
                        <div style={{marginTop: 16}}>
                            <TweenOneGroup
                                enter={{
                                    scale: 0.8,
                                    opacity: 0,
                                    type: 'from',
                                    duration: 100,
                                    onComplete: e => {
                                        e.target.style = '';
                                    },
                                }}
                                leave={{opacity: 0, width: 0, scale: 0, duration: 200}}
                                appear={false}
                            >
                                {tagsElements}
                            </TweenOneGroup>
                        </div>
                    </Spin>
                </Card>
                <Modal
                    title="Edit"
                    visible={this.state.isEditModalVisible}
                    onCancel={this.closeEditModal}
                    onOk={this.editItem}
                >
                    <Input value={this.state.editingValue} ref={(input) => this.editingInput = input} onChange={this.handleEditInputChange}/>
                </Modal>
            </div>
        );
    }
}

export default ManageTags;