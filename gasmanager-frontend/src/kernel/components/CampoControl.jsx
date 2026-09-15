import React from "react";

const CampoControl = ({campo, valor, onChange}) => {

    const {name, label, type = 'text', required = false, opciones = [], step, min, max, placeholder, readOnly, rows} = campo;

    const props = {
        name,
        value: valor ?? '',
        onChange,
        required,
        placeholder
    };
    if(step !== undefined) props.step = step;
    if(min !== undefined) props.min = min;
    if(max !== undefined) props.max = max;
    if(readOnly !== undefined) props.readOnly = readOnly;
    if(rows !== undefined) props.rows = rows;

    switch (type){
        case 'select':
            return (
                <select {...props}>
                    <option value="">Selecciona una opción</option>
                    {opciones.map(op => (
                        <option key={op.value} value={op.value}>{op.label}</option>
                    ))}
                </select>
            );
        case 'textarea':
            return <textarea {...props} />;
        case 'checkbox':
            return <input {...props} type="checkbox" checked={!!valor}/>;
        case 'currency':
            return <input {...props} type="number" step="0.01"/>;
        default:
            return <input {...props} type={type} />;
    }

}

export default CampoControl;