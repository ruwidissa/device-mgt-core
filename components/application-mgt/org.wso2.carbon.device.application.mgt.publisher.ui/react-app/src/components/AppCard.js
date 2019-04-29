import {
    Skeleton, Switch, Card, Icon, Avatar, Typography
} from 'antd';
import React from "react";
import config from "../../public/conf/config.json";

const { Meta } = Card;
const { Text } = Typography;

class AppCard extends React.Component {

    constructor(props){
        super(props);
    }


    render() {
        const defaultPlatformIcons = config.defaultPlatformIcons;
        let icon = defaultPlatformIcons.default;
        if(defaultPlatformIcons.hasOwnProperty(this.props.platform)){
            icon = defaultPlatformIcons[this.props.platform];
        }
        const description = (
            <div>
                <p>{this.props.description}</p>
                <Text code>{this.props.type}</Text>
                <Text> {this.props.subType}</Text>
            </div>
        );

        return (
                <Card style={{marginTop: 16 }}  actions={[<Icon type="edit" />, <Icon type="close" />, <Icon type="ellipsis" />]}>
                    <Meta
                        avatar={<Avatar src={icon} />}
                        title={this.props.name}
                        description={description}
                    />
                </Card>
        );
    }
}

export default AppCard;