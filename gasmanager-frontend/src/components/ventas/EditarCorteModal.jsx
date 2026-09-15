import React, {useEffect, useState, useRef} from "react";
import {cortesService} from "../../api/ventas/auth.js";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const hoy = () => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
};

const familiaCombustible = (nombre) => {
    const n = String(nombre || '').toLowerCase();
    if (n.includes('magna')) return 'MAGNA';
    if (n.includes('premium')) return 'PREMIUM';
    if (n.includes('diesel') || n.includes('diésel')) return 'DIESEL';
    return null;
};

const parsearCreditosDeObservaciones = (obs) => {
    if (!obs) return null;
    const idx = obs.indexOf('Cargos a crédito:');
    if (idx < 0) return null;
    let resto = obs.slice(idx + 'Cargos a crédito:'.length).split('|')[0].trim();
    if (!resto) return null;
    const items = resto.split(/\s*,\s*/).map(item => {
        const p = item.match(/^\s*([A-Z]+)\s+(.*?)\s+([0-9]+(?:\.[0-9]+)?)L\s+\$([0-9]+(?:\.[0-9]+)?)$/);
        if (!p) return null;
        return {clienteId: null, clienteNombre: p[2], tipoCombustible: p[1], litros: Number(p[3]), importe: Number(p[4]), aceites: 0, fecha: ''};
    }).filter(Boolean);
    return items.length ? items : null;
};

const creditosOriginales = (corte) => {
    if (corte != null && corte.creditosTotal != null) return Number(corte.creditosTotal) || 0;
    return (parsearCreditosDeObservaciones(corte?.observaciones) || [])
        .reduce((s, c) => s + (Number(c.importe) || 0) + (Number(c.aceites) || 0), 0);
};

const EditarCorteModal = ({corte, clientesCredito, onCerrar}) => {
    const [efectivo, setEfectivo] = useState(String(corte.efectivoRecibido || ''));
    const [tarjeta, setTarjeta] = useState(String(corte.tarjetaRecibido || ''));
    const [transferencia, setTransferencia] = useState(String(corte.transferenciaRecibido || ''));
    const [observaciones, setObservaciones] = useState(corte.observaciones || '');
    const [aceites, setAceites] = useState([]);
    const [creditos, setCreditos] = useState(() => parsearCreditosDeObservaciones(corte.observaciones) || []);
    const [resumen, setResumen] = useState(null);
    const [cargando, setCargando] = useState(true);
    const [crediForm, setCrediForm] = useState({creditoId: '', tipoCombustible: '', importe: '', aceites: ''});
    const [importeAceitesOriginal, setImporteAceitesOriginal] = useState(null);

    useEffect(() => {
        let activo = true;
        const cargar = async () => {
            try {
                const detalle = await cortesService.obtener(corte.id);
                if (!activo) return;
                if (detalle.creditos && detalle.creditos.length > 0) setCreditos(detalle.creditos);
                const r = await cortesService.resumen(Number(corte.turnoId), Number(corte.dispensarioId)).catch(() => null);
                if (!activo) return;
                setResumen(r);

                const mapa = {};
                detalle.aceites?.forEach(a => {
                    mapa[a.aceiteId] = {...a, vendidos: Number(a.vendidos) || 0, recibidoTotal: Number(a.recibidoTotal) || 0};
                });
                r?.aceitesCorte?.forEach(a => {
                    if (!mapa[a.aceiteId]) {
                        mapa[a.aceiteId] = {...a, vendidos: 0, recibidoTotal: Number(a.recibidoTotal) || 0};
                    }
                });
                setAceites(Object.values(mapa));
                setImporteAceitesOriginal((detalle.aceites || []).reduce((s, a) => s + (Number(a.vendidos) || 0) * (Number(a.precioVenta) || 0), 0));
            } catch (err) {
                alert(err.response?.data?.message || 'Error al cargar el detalle del corte');
            } finally {
                if (activo) setCargando(false);
            }
        };
        cargar();
        return () => { activo = false; };
    }, [corte.id, corte.turnoId, corte.dispensarioId]);

    const aceitesBase = importeAceitesOriginal != null
        ? importeAceitesOriginal
        : (Number(corte.totalAceites) || 0);
    const creditosBase = corte.creditosTotal != null
        ? (Number(corte.creditosTotal) || 0)
        : creditosOriginales(corte);
    const notasBase = Number(corte.notasCreditoTotal) || 0;

    const ventasContado = (Number(corte.esperadoEfectivo) || 0) - aceitesBase + notasBase + creditosBase;

    const opcionesCombustible = {};
    (resumen?.combustibles || []).forEach(c => {
        const fam = familiaCombustible(c.producto);
        if (fam && !opcionesCombustible[fam]) {
            const cantidad = Number(c.cantidad) || 0;
            opcionesCombustible[fam] = cantidad > 0 ? Number(c.importe) / cantidad : 0;
        }
    });
    const litrosCalculados = Number(crediForm.importe) > 0 && opcionesCombustible[crediForm.tipoCombustible]
        ? Number(crediForm.importe) / opcionesCombustible[crediForm.tipoCombustible]
        : 0;

    const vendidoPorFamilia = {};
    (resumen?.combustibles || []).forEach(c => {
        const fam = familiaCombustible(c.producto);
        if (!fam) return;
        if (!vendidoPorFamilia[fam]) vendidoPorFamilia[fam] = {litros: 0, importe: 0};
        vendidoPorFamilia[fam].litros += Number(c.cantidad) || 0;
        vendidoPorFamilia[fam].importe += Number(c.importe) || 0;
    });

    const creditosUsadosPorFamilia = {};
    creditos.forEach(c => {
        const fam = (c.tipoCombustible || '').toUpperCase();
        if (!fam) return;
        if (!creditosUsadosPorFamilia[fam]) creditosUsadosPorFamilia[fam] = {litros: 0, importe: 0};
        creditosUsadosPorFamilia[fam].litros += Number(c.litros) || 0;
        creditosUsadosPorFamilia[fam].importe += Number(c.importe) || 0;
    });

    const importeAceites = aceites.reduce((s, a) => s + (Number(a.vendidos) || 0) * (Number(a.precioVenta) || 0), 0);
    const creditoTotal = creditos.reduce((s, c) => s + (Number(c.importe) || 0) + (Number(c.aceites) || 0), 0);
    const cargosConcepto = creditoTotal + notasBase;
    const tarjetaRecibido = Number(tarjeta) || 0;
    const transferenciaRecibido = Number(transferencia) || 0;
    const baseCaja = ventasContado + importeAceites - cargosConcepto;
    const netoEfectivo = baseCaja - tarjetaRecibido - transferenciaRecibido;
    const efectivoEntregado = Number(efectivo) || 0;
    const diferencia = netoEfectivo - efectivoEntregado;

    const handleVendidos = (idx, valor) => {
        setAceites(prev => prev.map((a, i) => i === idx ? {...a, vendidos: valor} : a));
    };

    const handleCredito = (idx, campo, valor) => {
        setCreditos(prev => prev.map((c, i) => i === idx ? {...c, [campo]: valor} : c));
    };

    const handleRemoveCredito = (idx) => {
        setCreditos(prev => prev.filter((_, i) => i !== idx));
    };

    const handleAddCredito = () => {
        const importe = Number(crediForm.importe);
        const credito = (clientesCredito || []).find(c => String(c.id) === String(crediForm.creditoId));
        if (!credito) { alert('Selecciona un crédito de cliente con saldo disponible'); return; }
        if (!crediForm.tipoCombustible && Object.keys(opcionesCombustible).length > 0) {
            alert('Selecciona el combustible del cargo');
            return;
        }
        if (!importe || importe <= 0) { alert('Importe inválido'); return; }

        const fam = (crediForm.tipoCombustible || Object.keys(opcionesCombustible)[0] || '').toUpperCase();
        const vendido = vendidoPorFamilia[fam] || {litros: 0, importe: 0};
        const usado = creditosUsadosPorFamilia[fam] || {litros: 0, importe: 0};
        if (litrosCalculados > (vendido.litros - usado.litros) || importe > (vendido.importe - usado.importe)) {
            alert(`No puedes agregar esta nota de crédito, supera lo vendido de ${fam}`);
            return;
        }

        setCreditos(prev => [...prev, {
            creditoId: credito.id,
            clienteId: credito.clienteId,
            clienteNombre: credito.clienteNombre,
            tipoCombustible: fam,
            litros: Number(litrosCalculados.toFixed(4)),
            importe,
            aceites: Number(crediForm.aceites) || 0,
            fecha: hoy()
        }]);
        setCrediForm({creditoId: '', tipoCombustible: fam, importe: '', aceites: ''});
    };

    const handleGuardar = async (e) => {
        e.preventDefault();
        try {
            await cortesService.actualizar(corte.id, {
                turnoId: Number(corte.turnoId),
                dispensarioId: Number(corte.dispensarioId),
                efectivoRecibido: Number(efectivo) || 0,
                tarjetaRecibido: Number(tarjeta) || 0,
                transferenciaRecibido: Number(transferencia) || 0,
                observaciones,
                aceites: aceites.map(a => ({
                    aceiteId: a.aceiteId,
                    surtidorAceiteId: a.surtidorAceiteId,
                    sobrante: Math.max((Number(a.recibidoTotal) || 0) - (Number(a.vendidos) || 0), 0)
                })),
                creditos: creditos.map(c => ({
                    creditoId: c.creditoId,
                    clienteId: c.clienteId,
                    clienteNombre: c.clienteNombre,
                    tipoCombustible: c.tipoCombustible,
                    litros: c.litros,
                    importe: Number(c.importe) || 0,
                    aceites: Number(c.aceites) || 0,
                    fecha: c.fecha
                }))
            });
            alert('Corte actualizado');
            onCerrar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al actualizar el corte');
        }
    };

    return (
        <>
            <div className="modal-backdrop fade show" style={{zIndex: 1040}} onClick={onCerrar}/>
            <div className="modal fade show d-block" tabIndex="-1" style={{zIndex: 1055}} role="dialog">
                <div className="modal-dialog modal-xl" style={{
                    maxHeight: 'calc(100vh - 60px)',
                    margin: '30px auto',
                    display: 'flex',
                    flexDirection: 'column'
                }}>
                    <div className="modal-content" style={{
                        maxHeight: 'calc(100vh - 60px)',
                        display: 'flex',
                        flexDirection: 'column',
                        overflow: 'hidden'
                    }}>
                        <form onSubmit={handleGuardar} className="d-flex flex-column" style={{minHeight: 0, flex: 1}}>
                            <div className="modal-header"> 
                                <h5 className="modal-title">
                                    Editar Corte {corte.codigoCorte}
                                    <span className="ms-2 text-muted small">{corte.dispensarioNombre} · {corte.despachadorNombre}</span>
                                </h5>
                                <button type="button" className="btn-close" onClick={onCerrar}/>
                            </div>
                            <div className="modal-body" style={{overflowY: 'auto', minHeight: 0}}>
                                <div className="row g-3">
                                    <div className="col-md-4">
                                        <label className="form-label">Efectivo que ENTREGA ($)</label>
                                        <input type="number" step="0.01" min="0" className="form-control" value={efectivo} onChange={(e) => setEfectivo(e.target.value)} placeholder="0.00"/>
                                    </div>
                                    <div className="col-md-4">
                                        <label className="form-label">Tarjeta recibida ($)</label>
                                        <input type="number" step="0.01" min="0" className="form-control" value={tarjeta} onChange={(e) => setTarjeta(e.target.value)} placeholder="0.00"/>
                                    </div>
                                    <div className="col-md-4">
                                        <label className="form-label">Transferencia recibida ($)</label>
                                        <input type="number" step="0.01" min="0" className="form-control" value={transferencia} onChange={(e) => setTransferencia(e.target.value)} placeholder="0.00"/>
                                    </div>

                                    <div className="col-12">
                                        <div className="card" style={{padding: 0}}>
                                            <div className="card-body py-1">
                                                <h6 className="mb-1">🛢️ Aceites vendidos en isla</h6>
                                                {aceites.length === 0 ? (
                                                    <p className="text-muted small mb-0">
                                                        {cargando ? 'Cargando aceites del turno...' : 'Este turno no tiene aceites en la isla para el corte.'}
                                                    </p>
                                                ) : (
                                                    <table className="table table-sm table-bordered mb-0">
                                                        <thead>
                                                        <tr>
                                                            <th>Producto</th>
                                                            <th className="text-end">Recibido</th>
                                                            <th className="text-end">Vendidos</th>
                                                            <th className="text-center">✓</th>
                                                            <th className="text-end">Importe</th>
                                                        </tr>
                                                        </thead>
                                                        <tbody>
                                                        {aceites.map((a, idx) => {
                                                            const vendidos = Number(a.vendidos) || 0;
                                                            return (
                                                                <tr key={a.aceiteId} className={vendidos > 0 ? 'table-success' : ''}>
                                                                    <td>{a.aceiteNombre}</td>
                                                                    <td className="text-end">{Number(a.recibidoTotal).toFixed(0)}</td>
                                                                    <td style={{width: '130px'}}>
                                                                        <input type="number" min="0" step="1" className="form-control form-control-sm"
                                                                               value={vendidos}
                                                                               onChange={(e) => handleVendidos(idx, e.target.value)}/>
                                                                    </td>
                                                                    <td className="text-center">
                                                                        {vendidos > 0
                                                                            ? <span className="text-success fw-bold">✔</span>
                                                                            : <span className="text-muted">—</span>}
                                                                    </td>
                                                                    <td className="text-end">{dinero(vendidos * (Number(a.precioVenta) || 0))}</td>
                                                                </tr>
                                                            );
                                                        })}
                                                        </tbody>
                                                        <tfoot>
                                                        <tr className="table-light">
                                                            <th colSpan="4">Total aceites vendidos</th>
                                                            <th className="text-end">{dinero(importeAceites)}</th>
                                                        </tr>
                                                        </tfoot>
                                                    </table>
                                                )}
                                            </div>
                                        </div>
                                    </div>

                                    <div className="col-12">
                                        <div className="card" style={{padding: 0}}>
                                            <div className="card-body py-1">
                                                <h6 className="text-danger mb-1">🧾 Cargos a crédito de cliente</h6>
                                                <div className="row g-2 align-items-end mb-2">
                                                    <div className="col-md-3">
                                                        <label className="form-label small mb-1">Crédito (cliente)</label>
                                                        <select className="form-select form-select-sm" value={crediForm.creditoId}
                                                                onChange={(e) => setCrediForm({...crediForm, creditoId: e.target.value})}>
                                                            <option value="">— Selecciona —</option>
                                                            {(clientesCredito || []).map(c => (
                                                                <option key={c.id} value={c.id}>
                                                                    {c.folioCredito} — {c.clienteNombre} (saldo: ${Number(c.saldoPendiente).toLocaleString('es-MX', {minimumFractionDigits: 2})})
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </div>
                                                    <div className="col-md-2">
                                                        <label className="form-label small mb-1">Combustible</label>
                                                        <select className="form-select form-select-sm" value={crediForm.tipoCombustible}
                                                                onChange={(e) => setCrediForm({...crediForm, tipoCombustible: e.target.value})}
                                                                disabled={Object.keys(opcionesCombustible).length === 0}>
                                                            <option value="">—</option>
                                                            {Object.keys(opcionesCombustible).map(f => (
                                                                <option key={f} value={f}>{f}</option>
                                                            ))}
                                                        </select>
                                                    </div>
                                                    <div className="col-md-2">
                                                        <label className="form-label small mb-1">Importe $</label>
                                                        <input type="number" step="0.01" min="0" className="form-control form-control-sm"
                                                               value={crediForm.importe}
                                                               onChange={(e) => setCrediForm({...crediForm, importe: e.target.value})}/>
                                                    </div>
                                                    <div className="col-md-2">
                                                        <label className="form-label small mb-1">Aceites $</label>
                                                        <input type="number" step="0.01" min="0" className="form-control form-control-sm"
                                                               value={crediForm.aceites}
                                                               onChange={(e) => setCrediForm({...crediForm, aceites: e.target.value})}/>
                                                    </div>
                                                    <div className="col-md-3">
                                                        <button type="button" className="btn btn-sm btn-outline-danger" onClick={handleAddCredito}>+ Agregar cargo</button>
                                                    </div>
                                                </div>

                                                {creditos.length === 0 ? (
                                                    <p className="text-muted small mb-0">Sin cargos a crédito registrados. Agrega los que correspondan.</p>
                                                ) : (
                                                    <table className="table table-sm table-bordered mb-0">
                                                        <thead>
                                                        <tr>
                                                            <th>Fecha</th>
                                                            <th>Cliente</th>
                                                            <th>Combustible</th>
                                                            <th className="text-end">Litros</th>
                                                            <th className="text-end">Importe gas.</th>
                                                            <th className="text-end">Aceite</th>
                                                            <th className="text-end">Total</th>
                                                            <th>✕</th>
                                                        </tr>
                                                        </thead>
                                                        <tbody>
                                                        {creditos.map((c, idx) => (
                                                            <tr key={idx}>
                                                                <td>{c.fecha}</td>
                                                                <td>{c.clienteNombre}</td>
                                                                <td>{c.tipoCombustible}</td>
                                                                <td className="text-end">{c.litros}</td>
                                                                <td style={{width: '130px'}}>
                                                                    <input type="number" step="0.01" min="0" className="form-control form-control-sm text-end"
                                                                           value={c.importe}
                                                                           onChange={(e) => handleCredito(idx, 'importe', e.target.value)}/>
                                                                </td>
                                                                <td style={{width: '130px'}}>
                                                                    <input type="number" step="0.01" min="0" className="form-control form-control-sm text-end"
                                                                           value={c.aceites}
                                                                           onChange={(e) => handleCredito(idx, 'aceites', e.target.value)}/>
                                                                </td>
                                                                <td className="text-end fw-bold">{dinero((Number(c.importe) || 0) + (Number(c.aceites) || 0))}</td>
                                                                <td style={{width: '40px'}}>
                                                                    <button type="button" className="btn btn-sm btn-outline-secondary" onClick={() => handleRemoveCredito(idx)}>✕</button>
                                                                </td>
                                                            </tr>
                                                        ))}
                                                        </tbody>
                                                        <tfoot>
                                                        <tr className="table-light">
                                                            <th colSpan="4">Total</th>
                                                            <th className="text-end">{dinero(creditos.reduce((s, c) => s + (Number(c.importe) || 0), 0))}</th>
                                                            <th className="text-end">{dinero(creditos.reduce((s, c) => s + (Number(c.aceites) || 0), 0))}</th>
                                                            <th className="text-end fw-bold">{dinero(creditoTotal)}</th>
                                                            <th/>
                                                        </tr>
                                                        </tfoot>
                                                    </table>
                                                )}
                                            </div>
                                        </div>
                                    </div>

                                    <div className="col-12">
                                        <div className="card" style={{padding: 0}}>
                                            <div className="card-body py-1">
                                                <h6 className="mb-1">💵 Total y resultado del corte</h6>
                                                <div className="row g-2">
                                                    <div className="col-md-6">
                                                        <table className="table table-sm table-bordered mb-0">
                                                            <tbody>
                                                            <tr>
                                                                <td>Ventas de contado (recuperado)</td>
                                                                <td className="text-end">{dinero(ventasContado)}</td>
                                                            </tr>
                                                            <tr>
                                                                <td>Aceites vendidos en isla</td>
                                                                <td className="text-end">{dinero(importeAceites)}</td>
                                                            </tr>
                                                            <tr>
                                                                <td>Cargos a crédito de cliente</td>
                                                                <td className="text-end text-danger">− {dinero(cargosConcepto)}</td>
                                                            </tr>
                                                            <tr>
                                                                <td>Tarjeta recibida</td>
                                                                <td className="text-end text-danger">− {dinero(tarjetaRecibido)}</td>
                                                            </tr>
                                                            <tr>
                                                                <td>Transferencia recibida</td>
                                                                <td className="text-end text-danger">− {dinero(transferenciaRecibido)}</td>
                                                            </tr>
                                                            <tr className="table-primary">
                                                                <td><strong>EFECTIVO ESPERADO (neto)</strong></td>
                                                                <td className="text-end"><strong>{dinero(netoEfectivo)}</strong></td>
                                                            </tr>
                                                            <tr>
                                                                <td>Efectivo entregado</td>
                                                                <td className="text-end">{dinero(efectivoEntregado)}</td>
                                                            </tr>
                                                            <tr className={diferencia > 0 ? 'table-danger' : (diferencia < 0 ? 'table-success' : '')}>
                                                                <td><strong>Resultado</strong></td>
                                                                <td className="text-end fw-bold">
                                                                    {diferencia > 0 ? `FALTANTE ▲ ${dinero(diferencia)}`
                                                                        : diferencia < 0 ? `SOBRANTE ▼ ${dinero(Math.abs(diferencia))}`
                                                                            : `CUADRADO ${dinero(0)}`}
                                                                </td>
                                                            </tr>
                                                            </tbody>
                                                        </table>
                                                    </div>
                                                    <div className="col-md-6">
                                                        <h6 className="small text-muted mb-1">Detalle esperado por método (ventas)</h6>
                                                        <table className="table table-sm table-bordered mb-0">
                                                            <tbody>
                                                            <tr>
                                                                <td>Esperado efectivo (bruto)</td>
                                                                <td className="text-end">{dinero(Number(corte.esperadoEfectivo) || 0)}</td>
                                                            </tr>
                                                            <tr>
                                                                <td>Esperado tarjeta</td>
                                                                <td className="text-end">{dinero(Number(corte.esperadoTarjeta) || 0)}</td>
                                                            </tr>
                                                            <tr>
                                                                <td>Esperado transferencia</td>
                                                                <td className="text-end">{dinero(Number(corte.esperadoTransferencia) || 0)}</td>
                                                            </tr>
                                                            <tr>
                                                                <td>Esperado crédito</td>
                                                                <td className="text-end">{dinero(Number(corte.esperadoCredito) || 0)}</td>
                                                            </tr>
                                                            </tbody>
                                                        </table>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="col-12">
                                        <label className="form-label">Observaciones</label>
                                        <input className="form-control" value={observaciones} onChange={(e) => setObservaciones(e.target.value)} placeholder="Ej. faltante de efectivo"/>
                                    </div>
                                </div>
                            </div>
                            <div className="modal-footer" style={{flexShrink: 0}}>
                                <button type="button" className="btn btn-secondary" onClick={onCerrar}>Cancelar</button>
                                <button type="submit" className="btn btn-success" disabled={cargando}>💾 Guardar cambios</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </>
    );
};

export default EditarCorteModal;