import {
    Skeleton, Switch, Card, Icon, Avatar, Typography
} from 'antd';
import React from "react";
import config from "../../../public/conf/config.json";
import {openReleasesModal} from "../../js/actions";
import {connect} from "react-redux";

const { Meta } = Card;
const { Text } = Typography;

const mapDispatchToProps = dispatch => ({
    openReleasesModal: (app) => dispatch(openReleasesModal(app))
});

class ConnectedAppCard extends React.Component {

    constructor(props){
        super(props);
        this.handleReleasesClick = this.handleReleasesClick.bind(this);
    }

    handleReleasesClick(){
        this.props.openReleasesModal(this.props.app);
    }


    render() {
        const defaultPlatformIcons = config.defaultPlatformIcons;
        let icon = defaultPlatformIcons.default;
        if(defaultPlatformIcons.hasOwnProperty(this.props.platform)){
            icon = defaultPlatformIcons[this.props.platform];
        }
        let descriptionText = this.props.description;
        if(descriptionText.length>50){
            descriptionText = descriptionText.substring(0,50)+"...";
        }
        const description = (
            <div>
                <p>{descriptionText}</p>
                <Text code>{this.props.type}</Text>
                <Text> {this.props.subType}</Text>
            </div>
        );

        return (
                <Card style={{marginTop: 16 }}  actions={[<Icon type="edit" />, <Icon type="delete" />, <Icon type="appstore" theme="twoTone" onClick={this.handleReleasesClick} />]}>
                    <Meta
                        avatar={<Avatar src={icon} />}
                        title={this.props.name}
                        description={description}
                    />
                </Card>
        );
    }
}

const AppCard = connect(null,mapDispatchToProps)(ConnectedAppCard);

export default AppCard;