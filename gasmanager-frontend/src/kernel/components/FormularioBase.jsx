import React from "react";
import CampoControl from "./CampoControl.jsx";

const FormularioBase = (
    {titulo, editando, campos, objeto, handleChange, handleSubmit, cerrar, renderExtra, compacto, botonesExtra}) => {

    const renderGrupo = (listado) => (
        <div className={compacto ? 'row g-2' : 'row g-3'}>
            {listado.map(campo => (
                <div className={campo.type === 'textarea' ? (campo.colsClase || 'col-12') : compacto ? 'col-md-3' : 'col-md-4'} key={campo.name}>
                    <div className="form-group">
                        <label>{campo.label}</label>
                        <CampoControl campo={campo} valor={objeto[campo.name]} onChange={handleChange} />
                    </div>
                </div>
            ))}
        </div>
    );
    return (
        <div className="card" style={{maxWidth: compacto ? '900px' : '1000px'}}>
            {compacto ? (
                <h4 style={{marginBottom: '8px'}}>{editando ? 'Editar' : 'Crear Nuevo'} {titulo}</h4>
            ) : (
                <h3>{editando ? 'Editar' : 'Crear Nuevo'} {titulo}</h3>
            )}
            <form onSubmit={handleSubmit}>
                {renderGrupo(campos)}
                {renderExtra && renderExtra()}
                <div className="d-flex gap-2 align-items-center flex-wrap" style={{marginTop: '10px'}}>
                    {botonesExtra}
                    <button type="submit" className="btn btn-primary">Guardar</button>
                    <button
                        type="button"
                        className="btn btn-secondary"
                        onClick={cerrar}
                    >
                        Cancelar
                    </button>
                </div>
            </form>
        </div>
    );
}

export default FormularioBase;