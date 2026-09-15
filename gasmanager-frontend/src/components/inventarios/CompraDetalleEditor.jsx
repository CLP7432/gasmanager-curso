import React from "react";

const dinero = (v) => Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2});

const calcular = (item) => {
    const precio = parseFloat(item.precioUnitario) || 0;
    if (item.tipoProducto === 'COMBUSTIBLE') {
        const cantidad = parseFloat(item.cantidad) || 0;
        return {cantidad, subtotal: cantidad * precio};
    }
    const cajas = parseInt(item.cajas) || 0;
    const piezas = parseInt(item.piezas) || 0;
    const unidadesPorCaja = parseInt(item.unidadesPorCaja) || 1;
    const cantidad = cajas * unidadesPorCaja + piezas;
    return {cantidad, subtotal: cantidad * precio};
};

const CompraDetalleEditor = ({items, onChange, aceites, combustibles, agregarRef}) => {

    const actualizar = (index, cambios) => {
        const nuevos = items.map((item, i) => {
            if (i !== index) return item;
            const itemNuevo = {...item, ...cambios};
            const {cantidad, subtotal} = calcular(itemNuevo);
            return {...itemNuevo, cantidad, subtotal};
        });
        onChange(nuevos);
    };

    const agregar = () => {
        onChange([...items, {tipoProducto: 'ACEITE', aceiteId: '', combustibleId: '', cajas: 1, piezas: 0, unidadesPorCaja: 12, cantidad: 0, precioUnitario: '', subtotal: 0}]);
    };

    const remover = (index) => onChange(items.filter((_, i) => i !== index));

    if (agregarRef) agregarRef.current = agregar;

    const totalSubtotal = items.reduce((acc, i) => acc + (parseFloat(i.subtotal) || 0), 0);
    const totalIva = totalSubtotal * 0.16;
    const total = totalSubtotal + totalIva;

    const fila = (item, index) => {
        if (item.tipoProducto === 'COMBUSTIBLE') {
            const sel = combustibles.find(c => String(c.id) === String(item.combustibleId));
            return (
                <React.Fragment>
                    <div className="col-md-3">
                        <label>Producto (Combustible)</label>
                        <select className="form-control" value={item.combustibleId}
                                onChange={(e) => {
                                    const c = combustibles.find(x => String(x.id) === e.target.value);
                                    actualizar(index, {combustibleId: e.target.value, productoNombre: c ? c.nombre : '', precioUnitario: c && c.precioCompra > 0 ? c.precioCompra : ''});
                                }}>
                            <option value="">Seleccione...</option>
                            {combustibles.map(c => <option key={c.id} value={c.id}>{c.nombre}</option>)}
                        </select>
                    </div>
                    <div className="col-md-2">
                        <label>Litros</label>
                        <input type="number" min="0" step="0.001" className="form-control" value={item.cantidad}
                               onChange={(e) => actualizar(index, {cantidad: e.target.value})}/>
                    </div>
                            <div className="col-md-2">
                                <label>Precio $/L (factura)</label>
                                <input type="number" min="0" step="0.01"
                                       className={`form-control ${(() => {
                                           const c = combustibles.find(x => String(x.id) === String(item.combustibleId));
                                           return (c && (parseFloat(item.precioUnitario) || 0) > Number(c.precioActual)) ? 'is-invalid' : '';
                                       })()}`}
                                       value={item.precioUnitario}
                                       onChange={(e) => actualizar(index, {precioUnitario: e.target.value})}/>
                                {(() => {
                                    const c = combustibles.find(x => String(x.id) === String(item.combustibleId));
                                    if (!c) return null;
                                    const excede = (parseFloat(item.precioUnitario) || 0) > Number(c.precioActual);
                                    return (
                                        <>
                                            <small className="text-muted d-block">Venta: ${Number(c.precioActual).toFixed(2)}</small>
                                            {excede && <small className="d-block" style={{color: '#dc3545'}}>No se puede comprar más caro que la venta</small>}
                                        </>
                                    );
                                })()}
                            </div>
                    <div className="col-md-2">
                        <label>Subtotal</label>
                        <div>${dinero(item.subtotal)}</div>
                    </div>
                </React.Fragment>
            );
        }
        const sel = aceites.find(a => String(a.id) === String(item.aceiteId));
        return (
            <React.Fragment>
                <div className="col-md-3">
                    <label>Producto (Aceite)</label>
                    <select className="form-control" value={item.aceiteId}
                            onChange={(e) => {
                                const a = aceites.find(x => String(x.id) === e.target.value);
                                actualizar(index, {
                                    aceiteId: e.target.value,
                                    productoNombre: a ? a.nombre : '',
                                    unidadesPorCaja: a ? (a.unidadesPorCaja || 12) : item.unidadesPorCaja,
                                    presentacion: a ? a.presentacion : '',
                                    precioUnitario: a && a.precioCompra > 0 ? a.precioCompra : item.precioUnitario
                                });
                            }}>
                        <option value="">Seleccione...</option>
                        {aceites.map(a => <option key={a.id} value={a.id}>{a.nombre} ({a.presentacion})</option>)}
                    </select>
                </div>
                <div className="col-md-2">
                    <label>Cajas</label>
                    <input type="number" min="0" className="form-control" value={item.cajas}
                           onChange={(e) => actualizar(index, {cajas: e.target.value})}/>
                </div>
                <div className="col-md-2">
                    <label>Piezas sueltas</label>
                    <input type="number" min="0" className="form-control" value={item.piezas}
                           onChange={(e) => actualizar(index, {piezas: e.target.value})}/>
                </div>
                <div className="col-md-2">
                    <label>Unid. por caja</label>
                    <input type="number" min="1" className="form-control" value={item.unidadesPorCaja}
                           onChange={(e) => actualizar(index, {unidadesPorCaja: e.target.value})}/>
                </div>
                            <div className="col-md-2">
                                <label>Precio $/pieza (factura)</label>
                                <input type="number" min="0" step="0.01"
                                       className={`form-control ${(() => {
                                           const a = aceites.find(x => String(x.id) === String(item.aceiteId));
                                           return (a && (parseFloat(item.precioUnitario) || 0) > Number(a.precioVenta)) ? 'is-invalid' : '';
                                       })()}`}
                                       value={item.precioUnitario}
                                       onChange={(e) => actualizar(index, {precioUnitario: e.target.value})}/>
                                {(() => {
                                    const a = aceites.find(x => String(x.id) === String(item.aceiteId));
                                    if (!a) return null;
                                    const excede = (parseFloat(item.precioUnitario) || 0) > Number(a.precioVenta);
                                    return (
                                        <>
                                            <small className="text-muted d-block">Venta: ${Number(a.precioVenta).toFixed(2)}</small>
                                            {excede && <small className="d-block" style={{color: '#dc3545'}}>No se puede comprar más caro que la venta</small>}
                                        </>
                                    );
                                })()}
                            </div>
            </React.Fragment>
        );
    };

    return (
        <div>
            <div style={{marginBottom: '10px'}}>
                <h5>Productos de la compra</h5>
            </div>
            {items.map((item, index) => (
                <div className="card mb-2" key={index}>
                    <div className="card-body" style={{padding: '10px 12px'}}>
                        <div className="row g-2 align-items-center">
                            <div className="col-md-1">
                                <label>Tipo</label>
                                <select className="form-control" value={item.tipoProducto}
                                        onChange={(e) => actualizar(index, {tipoProducto: e.target.value})}>
                                    <option value="ACEITE">Aceite</option>
                                    <option value="COMBUSTIBLE">Combustible</option>
                                </select>
                            </div>
                            {fila(item, index)}
                            <div className="col-md-1 text-end">
                                <label>&nbsp;</label>
                                <div>
                                    <button type="button" className="btn btn-sm btn-danger" onClick={() => remover(index)}>X</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            ))}
            <div className="d-flex justify-content-end gap-4" style={{fontSize: '0.95em', marginTop: '10px'}}>
                <div>Subtotal: <strong>${dinero(totalSubtotal)}</strong></div>
                <div>IVA (16%): <strong>${dinero(totalIva)}</strong></div>
                <div>Total: <strong style={{color: '#198754'}}>${dinero(total)}</strong></div>
            </div>
        </div>
    );
};

export default CompraDetalleEditor;