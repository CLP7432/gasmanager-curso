import React from "react";

const PageHeader = ({titulo, subtitulo, mostrarAccion = true, etiquetaAccion = '+ Nuevo', onAccion}) => {
    return (
        <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:'20px'}}>
            <div>
                <h2 style={{marginBottom: subtitulo ? '4px' : 0}}>{titulo}</h2>
                {subtitulo && <small className="text-muted">{subtitulo}</small>}
            </div>
            {mostrarAccion && (
                <button className="btn btn-primary" onClick={onAccion}>
                    {etiquetaAccion}
                </button>
            )}
        </div>
    );
}
export default PageHeader;