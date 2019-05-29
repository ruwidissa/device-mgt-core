import React, {Component} from 'react';
import RcViewer from 'rc-viewer';
import {Col} from "antd";

class ImgViewer extends Component {
    render() {
        const options = {
            title: false,
            toolbar: {
                zoomIn: 0,
                zoomOut: 0,
                oneToOne: 0,
                reset: 0,
                prev: 1,
                play: {
                    show: 0
                },
                next: 1,
                rotateLeft: 0,
                rotateRight: 0,
                flipHorizontal: 0,
                flipVertical: 0
            },
            rotatable: false,
            transition: false,
            movable : false
        };
        return (
            <div>
                <RcViewer options={options} ref='viewer'>
                    {this.props.images.map((screenshotUrl) => {
                        return (
                            <Col key={"col-" + screenshotUrl} lg={6} md={8} xs={8} className="release-screenshot">
                                <img key={screenshotUrl} src={screenshotUrl}/>
                            </Col>
                        )
                    })}
                </RcViewer>
            </div>
        );

    }
}

export default ImgViewer;