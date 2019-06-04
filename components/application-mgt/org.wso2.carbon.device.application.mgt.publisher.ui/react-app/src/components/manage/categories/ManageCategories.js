import React from "react";
import {Card, Tag, message, Icon, Input, notification, Divider, Button, Spin, Tooltip, Popconfirm, Modal} from "antd";
import axios from "axios";
import config from "../../../../public/conf/config.json";
import {TweenOneGroup} from 'rc-tween-one';


class ManageCategories extends React.Component {
    state = {
        loading: false,
        searchText: '',
        categories: [],
        tempElements: [],
        inputVisible: false,
        inputValue: '',
        isAddNewVisible: false,
        isEditModalVisible: false,
        currentlyEditingId: null,
        editingValue: null
    };

    componentDidMount() {
        const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/applications/categories";
        axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                let categories = JSON.parse(res.data.data);
                this.setState({
                    categories: categories,
                    loading: false
                });
            }

        }).catch((error) => {
            if (error.response.status === 401) {
                window.location.href = 'https://localhost:9443/publisher/login';
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

    deleteCategory = (id) => {
        this.setState({
            loading: true
        });
        const request = "method=delete&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/admin/applications/categories/" + id;
        axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: "Done!",
                    description:
                        "Category Removed Successfully!",
                });

                this.setState({
                    loading: false
                });
                // this.setState({
                //     categories: [...categories, ...tempElements],
                //     tempElements: [],
                //     inputVisible: false,
                //     inputValue: '',
                //     loading: false,
                //     isAddNewVisible: false
                // });
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty("status") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = 'https://localhost:9443/publisher/login';
            } else {
                message.warning('Something went wrong');
            }
            this.setState({
                loading: false
            });
        });
    };

    renderElement = (category) => {
        const categoryName = category.categoryName;
        const tagElem = (
            <Tag
                color="blue"
            >
                {categoryName}
                <Divider type="vertical"/>
                <Tooltip title="edit">
                    <Icon onClick={() => {
                        this.openEditModal(categoryName)
                    }} style={{color: 'rgba(0,0,0,0.45)'}} type="edit"/>
                </Tooltip>
                <Divider type="vertical"/>
                <Tooltip title="delete">
                    <Popconfirm
                        title="Are you sure delete this category?"
                        onConfirm={() => {
                            if (category.isCategoryDeletable) {
                                this.deleteCategory(categoryName);
                            } else {
                                notification["error"]({
                                    message: 'Cannot delete "' + categoryName + '"',
                                    description:
                                        "This category is currently used. Please unassign the category from apps.",
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
            <span key={category.categoryName} style={{display: 'inline-block'}}>
                {tagElem}
            </span>
        );
    };

    renderTempElement = (category) => {
        const {tempElements} = this.state;
        const tagElem = (
            <Tag
                closable
                onClose={e => {
                    e.preventDefault();
                    const remainingElements = tempElements.filter(function (value) {

                        return value.categoryName !== category.categoryName;

                    });
                    this.setState({
                        tempElements: remainingElements
                    });
                }}
            >
                {category.categoryName}
            </Tag>
        );
        return (
            <span key={category.categoryName} style={{display: 'inline-block'}}>
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
        const {inputValue, categories} = this.state;
        let {tempElements} = this.state;
        if (inputValue) {
            if ((categories.findIndex(i => i.categoryName === inputValue) === -1) && (tempElements.findIndex(i => i.categoryName === inputValue) === -1)) {
                tempElements = [...tempElements, {categoryName: inputValue, isCategoryDeletable: true}];
            } else {
                message.warning('Category already exists');
            }
        }

        this.setState({
            tempElements,
            inputVisible: false,
            inputValue: '',
        });
    };

    handleSave = () => {
        const {tempElements, categories} = this.state;
        this.setState({
            loading: true
        });

        const dataArray = JSON.stringify(tempElements.map(category => category.categoryName));

        const request = "method=post&content-type=application/json&payload=" + dataArray + "&api-endpoint=/application-mgt-publisher/v1.0/admin/applications/categories";
        axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: "Done!",
                    description:
                        "New Categories were added successfully",
                });

                this.setState({
                    categories: [...categories, ...tempElements],
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
                window.location.href = 'https://localhost:9443/publisher/login';
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

        const {editingValue, currentlyEditingId, categories} = this.state;

        this.setState({
            loading: true,
            isEditModalVisible: false,
        });
        const request = "method=put&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/admin/applications/categories?from="+currentlyEditingId+"%26to="+editingValue;
        axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: "Done!",
                    description:
                        "Category was edited successfully",
                });

                categories[categories.findIndex(i => i.categoryName === currentlyEditingId)].categoryName = editingValue;

                this.setState({
                    categories: categories,
                    loading: false,
                    editingValue: null
                });
            }

        }).catch((error) => {
            if (error.response.hasOwnProperty("status") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = 'https://localhost:9443/publisher/login';
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
        const {categories, inputVisible, inputValue, tempElements, isAddNewVisible} = this.state;
        const categoriesElements = categories.map(this.renderElement);
        const temporaryElements = tempElements.map(this.renderTempElement);
        return (
            <div>
                <Card title="Categories">
                    <Spin tip="Working on it..." spinning={this.state.loading}>
                        {!isAddNewVisible &&
                        <Button
                            size="small"
                            onClick={() => {
                                this.setState({
                                    isAddNewVisible: true,
                                    inputVisible: true
                                }, () => this.input.focus())
                            }} htmlType="button">Add Categories
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
                                            <Icon type="plus"/> New Category
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
                                {categoriesElements}
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

export default ManageCategories;