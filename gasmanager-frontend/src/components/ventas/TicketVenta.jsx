import React from "react";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
const fecha = (s) => new Date(s).toLocaleString('es-MX', {dateStyle: 'short', timeStyle: 'short'});

const TicketVenta = ({venta, titulo = 'GASMANAGER S.A. DE C.V.', despachadorNombre, puntos}) => {
    const esCombustible = (d) => d.tipoProducto === 'COMBUSTIBLE';

    return (
        <>
            <style>{`
                @media print {
                    body * { visibility: hidden; }
                    #ticket-visible, #ticket-visible * { visibility: visible; }
                    #ticket-visible { position: absolute; left: 0; top: 0; width: 80mm; }
                }
            `}</style>

            <div id="ticket-visible" style={{fontFamily: "'Courier New', monospace", maxWidth: '340px', margin: '0 auto', fontSize: '0.92rem'}}>
                <div className="text-center border-bottom pb-2 mb-2">
                    <div className="fw-bold fs-5">{titulo}</div>
                    <div className="small text-muted">Folio: <strong>{venta.folio}</strong></div>
                    <div className="small text-muted">{fecha(venta.fechaHora)}</div>
                </div>

                <div className="small mb-2">
                    <div className="d-flex justify-content-between"><span className="text-muted">Despachador</span><strong>{despachadorNombre || 'ID ' + venta.usuarioId}</strong></div>
                    <div className="d-flex justify-content-between"><span className="text-muted">Pago</span><strong className="text-uppercase">{venta.metodoPago}</strong></div>
                </div>

                <table className="w-100 small mb-2" style={{borderCollapse: 'collapse'}}>
                    <thead>
                    <tr className="border-bottom">
                        <th className="text-start">DESCRIPCIÓN</th>
                        <th className="text-end">CANT.</th>
                        <th className="text-end">IMPORTE</th>
                    </tr>
                    </thead>
                    <tbody>
                    {venta.detalles.map((d, i) => (
                        <tr key={i} className="border-bottom">
                            <td className="text-start">
                                {d.productoNombre}
                                {esCombustible(d) && <div className="small text-muted">{Number(d.cantidad).toFixed(3)} L × {dinero(d.precioUnitario)}</div>}
                            </td>
                            <td className="text-end align-middle">{esCombustible(d) ? `${Number(d.cantidad).toFixed(3)} L` : Number(d.cantidad)}</td>
                            <td className="text-end align-middle fw-bold">{dinero(d.subtotal)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>

                <div className="border-top pt-1 mb-1">
                    <div className="d-flex justify-content-between small"><span className="text-muted">Subtotal</span><span>{dinero(venta.subtotal)}</span></div>
                    <div className="d-flex justify-content-between small"><span className="text-muted">IVA 16%</span><span>{dinero(venta.iva)}</span></div>
                    <div className="d-flex justify-content-between fs-5 fw-bold"><span>TOTAL</span><span>{dinero(venta.total)}</span></div>
                    {puntos && puntos.programaActivo && Number(puntos.puntos) > 0 && (
                        <div className="d-flex justify-content-between small fw-bold"><span>⭐ PUNTOS {puntos.programaNombre || ''}</span><span>{puntos.puntos} pts</span></div>
                    )}
                </div>

                <div className="text-center small mt-2 pt-1 border-top">
                    {venta.estado === 'CANCELADA'
                        ? <div className="fw-bold text-danger">VENTA CANCELADA</div>
                        : <><div className="text-muted fst-italic">¡Gracias por su compra!</div>
                            <div className="text-muted">Conserve su ticket</div></>}
                </div>
            </div>
        </>
    );
};

export default TicketVenta;