import React from 'react';
import {Drawer, Row, Col, Typography, Divider, Tag, Avatar, List} from 'antd';
import "../../../App.css";
import DetailedRating from "../detailed-rating/DetailedRating";

const {Text, Title, Paragraph} = Typography;

class AppDetailsDrawer extends React.Component {

    render() {
        const {app, visible, onClose} = this.props;
        if (app == null) {
            return null;
        }
        return (
            <div>

                <Drawer
                    placement="right"
                    width={640}
                    closable={false}
                    onClose={onClose}
                    visible={visible}
                >
                    <div style={{textAlign: "center"}}>
                        <img
                            style={{
                                marginBottom: 10,
                                width: 100,
                                borderRadius: "28%",
                                border: "1px solid #ddd"
                            }}
                            src={app.applicationReleases[0].iconPath}
                        />
                        <Title level={2}>{app.name}</Title>
                    </div>
                    <Divider/>
                    <Paragraph type="secondary" ellipsis={{rows: 3, expandable: true}}>{app.description}</Paragraph>
                    <Divider dashed={true}/>
                    <Text strong={true}>Categories</Text>
                    <br/>
                    <br/>
                    <span>
                    {app.categories.map(category => {
                        return (
                            <Tag color="blue" key={category} style={{paddingBottom: 5}}>
                                {category}
                            </Tag>
                        );
                    })}
                    </span>

                    <Divider dashed={true}/>
                    <Text strong={true}>Tags</Text>
                    <br/>
                    <br/>
                    <span>
                    {app.tags.map(category => {
                        return (
                            <Tag color="gold" key={category} style={{paddingBottom: 5}}>
                                {category}
                            </Tag>
                        );
                    })}
                    </span>
                    <Divider dashed={true}/>
                    <Text strong={true}>Releases</Text>
                    <br/>
                    <List
                        itemLayout="horizontal"
                        dataSource={app.applicationReleases}
                        renderItem={release => (
                            <List.Item>
                                <List.Item.Meta
                                    title={<a href={"apps/releases/"+release.uuid}>{release.version}</a>}
                                    description={
                                        <div>
                                            Status : <Tag>{release.currentStatus}</Tag> Release Type <Tag color="green">{release.releaseType}</Tag>
                                        </div>
                                    }
                                />
                            </List.Item>
                        )}
                    />
                    <Divider dashed={true}/>

                    <DetailedRating type="app" uuid={app.applicationReleases[0].uuid}/>
                </Drawer>
            </div>
        );
    }
}

export default AppDetailsDrawer;