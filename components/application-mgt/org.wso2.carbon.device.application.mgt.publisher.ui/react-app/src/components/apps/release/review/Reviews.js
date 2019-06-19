import React from "react";
import {List, message, Avatar, Spin, Button} from 'antd';
import "./Reviews.css";

import InfiniteScroll from 'react-infinite-scroller';
import SingleReview from "./SingleReview";
import axios from "axios";
import config from "../../../../../public/conf/config.json";

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

        const {uuid} = this.props;

        axios.get(
            config.serverConfig.protocol + "://"+config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri +config.serverConfig.invoker.store+"/reviews/app/"+uuid,
            {
                headers: {'X-Platform': config.serverConfig.platform}
            }).then(res => {
            if (res.status === 200) {
                let reviews = res.data.data.data;
                callback(reviews);
            }

        }).catch(function (error) {
            if (error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/store/login';
            } else {
                message.warning('Something went wrong');

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

    render() {
        const review = {
            id: 2,
            content: "Btw, it was clear to me that I can cancel the 1 year subscription before the free trial week and so I did. Dont understand the negative reviews about that. It has a good collection of excercises, meditations etc. You just answer 5 questions and you get challenges assigned to you. I would have liked something even more personalized. I didnt like the interface. It is a bit messy and difficult to follow your tasks. So, I didnt want to do a full-year subscription. There could be more options.",
            rootParentI: -1,
            immediateParentId: -1,
            createdAt: "Fri, 24 May 2019 17:27:22 IST",
            modifiedAt: "Fri, 24 May 2019 17:27:22 IST",
            rating: 4,
            replies: []
        };
        // console.log(this.state.loadMore);
        // console.log(this.state.data.length);
        return (
            <div className="demo-infinite-container">
                <InfiniteScroll
                    initialLoad={false}
                    pageStart={0}
                    loadMore={this.handleInfiniteOnLoad}
                    hasMore={!this.state.loading && this.state.hasMore}
                    useWindow={true}
                >
                    <List
                        dataSource={this.state.data}
                        renderItem={item => (
                            <List.Item key={item.id}>
                                <SingleReview review={item}/>
                            </List.Item>
                        )}
                    >
                        {this.state.loading && this.state.hasMore && (
                            <div className="demo-loading-container">
                                <Spin/>
                            </div>
                        )}
                    </List>
                </InfiniteScroll>
                {!this.state.loadMore && (this.state.data.length >= limit) && (<div style={{textAlign: "center"}}>
                    <Button type="dashed" htmlType="button" onClick={this.enableLoading}>Read All Reviews</Button>
                </div>)}
            </div>
        );
    }
}

export default Reviews;