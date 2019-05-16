import React from "react";

import "./CustomNode.css";


/**
 * Component that renders a person's name and gender, along with icons
 * representing if they have a driver license for bike and / or car.
 * @param {Object} props component props to render.
 */
function CustomNode({ node }) {

    return (
        <div className="node" style={{backgroundColor: node.color}}>
            <div className="name">{node.id}</div>

            {/*<div className="flex-container fill-space flex-container-row">*/}
                {/*<div className="fill-space">*/}
                    {/*<div*/}
                        {/*className="icon"*/}
                        {/*style={{ backgroundImage: `url('${isMale ? ICON_TYPES.MAN : ICON_TYPES.WOMAN}')` }}*/}
                    {/*/>*/}
                {/*</div>*/}

                {/*<div className="icon-bar">*/}
                    {/*{person.hasBike && (*/}
                        {/*<div className="icon" style={{ backgroundImage: `url('${ICON_TYPES.BIKE}')` }} />*/}
                    {/*)}*/}
                    {/*{person.hasCar && <div className="icon" style={{ backgroundImage: `url('${ICON_TYPES.CAR}')` }} />}*/}
                {/*</div>*/}
            {/*</div>*/}
        </div>
    );
}

export default CustomNode;