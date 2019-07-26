import React from "react";
import {List, message, Avatar, Spin, Button, notification} from 'antd';
import "./Reviews.css";

import InfiniteScroll from 'react-infinite-scroller';
import SingleReview from "./singleReview/SingleReview";
import axios from "axios";
import {withConfigContext} from "../../../../context/ConfigContext";

const limit = 5;

class Reviews extends React.Component {
    state = {
        data: [],
        loading: false,
        hasMore: false,
        loadMore: false
    };


    componentDidMount() {
        this.fetchData(0, limit, res => {
            this.setState({
                data: res,
            });
        });
    }

    fetchData = (offset, limit, callback) => {

        const {uuid, type} = this.props;
        const config = this.props.context;

        axios.get(
            window.location.origin+ config.serverConfig.invoker.uri +config.serverConfig.invoker.store+"/reviews/"+type+"/"+uuid,
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }).then(res => {
            if (res.status === 200) {
                let reviews = res.data.data.data;
                callback(reviews);
            }

        }).catch(function (error) {
            if (error.response.status === 401) {
                window.location.href = window.location.origin+ '/store/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load reviews.",
                });
            }
        });
    };

    handleInfiniteOnLoad = (count) => {
        const offset = count * limit;
        let data = this.state.data;
        this.setState({
            loading: true,
        });
        if (data.length > 149) {
            this.setState({
                hasMore: false,
                loading: false,
            });
            return;
        }
        this.fetchData(offset, limit, res => {
            if (res.length > 0) {
                data = data.concat(res);
                this.setState({
                    data,
                    loading: false,
                });
            } else {
                this.setState({
                    hasMore: false,
                    loading: false
                });
            }
        });
    };

    enableLoading = () => {
        this.setState({
            hasMore: true,
            loadMore: true
        });
    };

    deleteCallback = () =>{
        this.fetchData(0, limit, res => {
            this.setState({
                data: res,
            });
        });
    };

    render() {
        const {loading, hasMore, data, loadMore} = this.state;
        const {uuid} = this.props;
        return (
            <div className="infinite-container">
                <InfiniteScroll
                    initialLoad={false}
                    pageStart={0}
                    loadMore={this.handleInfiniteOnLoad}
                    hasMore={!loading && hasMore}
                    useWindow={true}
                >
                    <List
                        dataSource={data}
                        renderItem={item => (
                            <List.Item key={item.id}>
                                <SingleReview uuid={uuid} review={item} isDeletable={true} isEditable={false} deleteCallback={this.deleteCallback}/>
                            </List.Item>
                        )}
                    >
                        {loading && hasMore && (
                            <div className="loading-container">
                                <Spin/>
                            </div>
                        )}
                    </List>
                </InfiniteScroll>
                {!loadMore && (data.length >= limit) && (<div style={{textAlign: "center"}}>
                    <Button type="dashed" htmlType="button" onClick={this.enableLoading}>Read All Reviews</Button>
                </div>)}
            </div>
        );
    }
}

export default withConfigContext(Reviews);