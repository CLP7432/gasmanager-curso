import React from "react";

const opcionesTipo = [
    {value: 'COMBUSTIBLE', label: 'Combustible'},
    {value: 'ACEITE', label: 'Aceite'},
    {value: 'ADITIVO', label: 'Aditivo'}
];

const toNum = (v) => parseFloat(v) || 0;

const EditorItems = ({items, onChange, combustibles = [], aceites = [], readOnly = false}) => {

    const cantidadDe = (item) => {
        if (item.tipo === 'COMBUSTIBLE') {
            const precio = toNum(item.precioUnitario);
            const monto = toNum(item.monto);
            return precio > 0 ? monto / precio : 0;
        }
        return toNum(item.cantidad);
    };

    const subtotal = (item) => {
        const cantidad = cantidadDe(item);
        const precio = toNum(item.precioUnitario);
        return item.tipo === 'COMBUSTIBLE' ? toNum(item.monto) : cantidad * precio;
    };

    const total = items.reduce((acc, i) => acc + subtotal(i), 0);

    const cambiar = (index, campo, valor) => {
        onChange(items.map((item, i) => {
            if (i !== index) return item;
            const nuevo = {...item, [campo]: valor};
            if (campo === 'tipo') {
                nuevo.producto = '';
                nuevo.precioUnitario = '';
                nuevo.monto = '';
                nuevo.cantidad = '';
            }
            if (campo === 'producto' && valor) {
                const fuente = nuevo.tipo === 'COMBUSTIBLE' ? combustibles : aceites;
                const p = fuente.find(x => String(x.nombre) === String(valor));
                if (p) nuevo.precioUnitario = nuevo.tipo === 'COMBUSTIBLE' ? p.precioActual : p.precioVenta;
            }
            return nuevo;
        }));
    };

    const agregar = () => {
        onChange([...items, {tipo: 'COMBUSTIBLE', producto: '', monto: '', cantidad: '', precioUnitario: ''}]);
    };
    const quitar = (index) => {
        onChange(items.filter((_, i) => i !== index));
    };

    const productos = (tipo) => tipo === 'COMBUSTIBLE' ? combustibles : aceites;

    const renderItem = (item, i) => {
        const esCombustible = item.tipo === 'COMBUSTIBLE';
        const litros = cantidadDe(item);
        const sub = subtotal(item);
        return (
            <div className="row g-2" key={i} style={{marginBottom: '8px', alignItems: 'center'}}>
                <div className="col-2">
                    <select className="form-control" value={item.tipo ?? 'COMBUSTIBLE'} disabled={readOnly}
                            onChange={(e) => cambiar(i, 'tipo', e.target.value)}>
                        {opcionesTipo.map(op => <option key={op.value} value={op.value}>{op.label}</option>)}
                    </select>
                </div>
                <div className="col-3">
                    {item.tipo === 'ADITIVO'
                        ? <input className="form-control" placeholder="Producto" value={item.producto ?? ''}
                                 disabled={readOnly}
                                 onChange={(e) => cambiar(i, 'producto', e.target.value)}/>
                        : <select className="form-control" value={item.producto ?? ''} disabled={readOnly}
                                  onChange={(e) => cambiar(i, 'producto', e.target.value)}>
                            <option value="">Seleccionar...</option>
                            {productos(item.tipo).map(p => (
                                <option key={p.id} value={p.nombre}>{p.nombre}</option>
                            ))}
                        </select>}
                </div>
                <div className="col-3">
                    {esCombustible ? (
                        <React.Fragment>
                            <input className="form-control" type="number" min="0.01" step="0.01"
                                   placeholder="Monto ($)" value={item.monto ?? ''} disabled={readOnly}
                                   onChange={(e) => cambiar(i, 'monto', e.target.value)}/>
                            <small className="text-muted">Litros: {litros.toFixed(3)} L</small>
                        </React.Fragment>
                    ) : (
                        <input className="form-control" type="number" min="0.01" step="0.01"
                               placeholder="Cantidad" value={item.cantidad ?? ''} disabled={readOnly}
                               onChange={(e) => cambiar(i, 'cantidad', e.target.value)}/>
                    )}
                </div>
                <div className="col-2">
                    <input className="form-control" type="number" min="0.01" step="0.01" placeholder="P. Unit."
                           value={item.precioUnitario ?? ''} disabled={readOnly}
                           onChange={(e) => cambiar(i, 'precioUnitario', e.target.value)}/>
                </div>
                <div className="col-2 d-flex justify-content-between align-items-center">
                    <strong>${sub.toFixed(2)}</strong>
                    {!readOnly && (
                        <button type="button" className="btn btn-danger btn-sm" onClick={() => quitar(i)}>×</button>
                    )}
                </div>
            </div>
        );
    };

    return (
        <div>
            <div className="row g-2 fw-bold" style={{marginBottom: '6px'}}>
                <div className="col-2">Tipo</div>
                <div className="col-3">Producto</div>
                <div className="col-3">Monto $ / Cantidad</div>
                <div className="col-2">P. Unit.</div>
                <div className="col-2">Subtotal</div>
            </div>
            {items.length === 0
                ? <p className="text-muted">Sin productos. Agrega lo cargado (combustible, aceites...).</p>
                : items.map(renderItem)}
            {!readOnly && (
                <button type="button" className="btn btn-outline-primary btn-sm" onClick={agregar}>
                    + Agregar producto
                </button>
            )}
            <p style={{marginTop: '8px'}}><strong>Total productos: ${total.toFixed(2)}</strong></p>
        </div>
    );
};

export default EditorItems;