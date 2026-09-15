import React from "react";

const EstadoCarga = ({cargando, mensaje = 'Cargando...', children}) => {
    return (
        <div className="card">
            {cargando ? <p>{mensaje}</p> : children}
        </div>
    );
}
export default EstadoCarga;