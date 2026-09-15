import React from "react";

const CardBase = ({titulo, children, style}) => {
    return(
        <div className="card" style={style}>
            {titulo && <h3 style={{marginBottom:'15px'}}>{titulo}</h3>}
            {children}
        </div>
    );
};
export default CardBase;