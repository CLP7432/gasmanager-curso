import React from "react";

const Acciones = ({fila, onEditar, onEliminar, etiquetaEliminar = 'Eliminar', onExtra, etiquetaExtra, habilitadoEliminar = true, onToggle, etiquetaToggle}) => {
    return (
        <div style={{display:'flex', flexDirection:'row', gap:'4px', alignItems:'center'}}>
            {onEditar && (
                <button className="btn btn-primary" style={{padding:'4px 8px',fontSize:'12px'}}
                onClick={() => onEditar(fila)}>Editar</button>
            )}
            {onToggle && (
                <button className="btn btn-warning" style={{padding:'4px 8px',fontSize:'12px'}}
                        onClick={() => onToggle(fila)}>{etiquetaToggle}</button>
            )}
            {onExtra && (
                <button className="btn btn-info" style={{padding:'4px 8px',fontSize:'12px'}}
                        onClick={() => onExtra(fila)}>{etiquetaExtra}</button>
            )}
            {onEliminar && (
                <button className="btn btn-danger" style={{padding:'4px 8px',fontSize:'12px'}}
                        onClick={() => onEliminar(fila)} disabled={!habilitadoEliminar}>{etiquetaEliminar}</button>
            )}
        </div>
    );
}

export default Acciones;