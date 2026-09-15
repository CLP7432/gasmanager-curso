import React, {useState, useEffect, useMemo} from "react";
import {useLocation, useNavigate} from "react-router-dom";
import {facturasService, clientesFiscalesService} from "../../api/facturacion/auth.js";
import {ventasService} from "../../api/ventas/auth.js";
import {aceitesService} from "../../api/inventarios/auth.js";
import {columna} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import CardBase from "../../kernel/components/CardBase.jsx";
import FacturaVista from "./FacturaVista.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

// El P. unit. del renglón viaja NETO (sin IVA, como en el concepto impreso),
// máximo 4 decimales. Los renglones TICKET se anclan después a los totales
// del ticket para dar exacto.
const IVA = 0.16;
const r2 = (v) => Math.round((Number(v) || 0) * 100) / 100;
const r4 = (v) => Number((Number(v) || 0).toFixed(2));
// Neto con precisión completa para el cálculo (se muestra a 2 decimales);
// el P. unit. impreso va sin IVA y el total queda exacto.
const netoExacto = (bruto) => (Number(bruto) || 0) / (1 + IVA);
const lineaBruto = (c) => r2((Number(c.cantidad) || 0) * (Number(c.valorUnitario) || 0));
const lineaIva = (c) => r2(lineaBruto(c) * IVA / (1 + IVA));

const conceptoVacio = () => ({
    origen: 'TICKET',
    origenFolio: '',
    descripcion: 'MAGNA',
    claveProdServ: '15101514',
    claveUnidad: 'LTR',
    unidad: 'L',
    cantidad: '',
    valorUnitario: '',
    esCombustible: true
});

const FacturasList = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const {datos: facturas, loading, cargar} = useLista(facturasService, 'listar');
    const {datos: fiscales} = useLista(clientesFiscalesService, 'listar');
    const [mostrarForm, setMostrarForm] = useState(false);
    const [clienteFiscalId, setClienteFiscalId] = useState('');
    const [formaPago, setFormaPago] = useState('01');
    const [metodoPago, setMetodoPago] = useState('PUE');
    const [conceptos, setConceptos] = useState([conceptoVacio()]);
    const [detalle, setDetalle] = useState(null);
    const [modalAceite, setModalAceite] = useState(false);

    // Llegada desde LiquidarNotas: prefactura con las notas recién pagadas.
    // Si el cliente no tiene registro fiscal, se pide crearlo primero.
    useEffect(() => {
        const notas = location.state?.facturarNotas;
        if (!notas || notas.length === 0) return;
        const clienteId = location.state?.clienteId;
        (async () => {
            try {
                const fiscal = await clientesFiscalesService.obtenerPorCliente(clienteId);
                setClienteFiscalId(String(fiscal.id));
                setConceptos(notas.flatMap(n => (n.items || []).map(it => {
                    const t = String(it.tipo || '');
                    const combustible = /COMB|GAS|DIESEL/i.test(t);
                    return {
                        origen: 'NOTA_CREDITO',
                        origenFolio: n.numero,
                        descripcion: it.producto,
                        claveProdServ: combustible ? '15101514' : '15111515',
                        claveUnidad: combustible ? 'LTR' : 'H87',
                        unidad: it.unidad || (combustible ? 'L' : 'Pieza'),
                        cantidad: it.cantidad,
                        valorUnitario: netoExacto(it.precioUnitario),
                        esCombustible: combustible,
                        bloqueado: true
                    };
                })));
                setMostrarForm(true);
            } catch {
                alert('El cliente aún no tiene registro fiscal. Regístralo en Clientes Fiscales y vuelve a facturar.');
                navigate('/facturacion/clientes-fiscales');
                return;
            }
            navigate(location.pathname, {replace: true, state: {}});
        })();
    }, [location.state]);

    const setConcepto = (idx, campo, valor) => {
        setConceptos(prev => prev.map((c, i) => {
            if (i !== idx) return c;
            const n = {...c, [campo]: valor};
            // Defaults por origen
            if (campo === 'origen') {
                if (valor === 'TICKET') {
                    n.claveProdServ = '15101514'; n.claveUnidad = 'LTR'; n.unidad = 'L';
                    n.descripcion = 'MAGNA'; n.esCombustible = true;
                } else {
                    n.claveProdServ = '15101514'; n.claveUnidad = 'LTR'; n.unidad = 'L';
                    n.esCombustible = true;
                }
            }
            return n;
        }));
    };

    // Folios ya facturados (se excluyen canceladas): TICKET y NOTA_CRÉDITO
    // no se pueden volver a facturar.
    const foliosFacturados = useMemo(() => {
        const mapa = {};
        (facturas || []).filter(f => f.estado !== 'CANCELADA').forEach(f => {
            (f.conceptos || []).forEach(c => {
                if ((c.origen === 'TICKET' || c.origen === 'NOTA_CREDITO') && c.origenFolio) {
                    mapa[`${c.origen}::${c.origenFolio}`] = f.folio;
                }
            });
        });
        return mapa;
    }, [facturas]);

    // Al capturar el folio del ticket se jala la venta real: sus consumos
    // (combustible y aceites) entran como conceptos con su importe.
    const cargarTicket = async (idx) => {
        const folio = (conceptos[idx]?.origenFolio || '').trim();
        if (!folio) { alert('Captura el folio del ticket'); return; }
        const ya = foliosFacturados[`TICKET::${folio}`];
        if (ya) { alert(`Ese ticket ya fue facturado en ${ya}`); return; }
        try {
            const venta = await ventasService.obtenerPorFolio(folio);
            if (venta.estado === 'CANCELADA') { alert(`El ticket ${folio} está cancelado y no puede facturarse`); return; }
            const lineas = (venta.detalles || []).map(d => {
                const comb = String(d.tipoProducto || '').toUpperCase() === 'COMBUSTIBLE';
                return {
                    origen: 'TICKET',
                    origenFolio: venta.folio,
                    descripcion: d.productoNombre,
                    claveProdServ: comb ? '15101514' : '15111515',
                    claveUnidad: comb ? 'LTR' : 'H87',
                    unidad: comb ? 'L' : 'Pieza',
                    cantidad: d.cantidad,
                    valorUnitario: d.precioUnitario,
                    esCombustible: comb,
                    bloqueado: true,
                    bruto: r2((Number(d.cantidad) || 0) * (Number(d.precioUnitario) || 0))
                };
            });
            if (lineas.length === 0) { alert(`El ticket ${folio} no trae consumos`); return; }
            // Anclaje al total del ticket (igual que el backend): se reparten
            // su subtotal e IVA entre los renglones; el último absorbe redondeo.
            // El P. unit. mostrado es el NETO de ese reparto (sin IVA).
            const G = lineas.reduce((s, l) => s + l.bruto, 0);
            let impAcum = 0, ivaAcum = 0;
            lineas.forEach((l, j) => {
                if (G > 0 && venta.subtotal != null && venta.iva != null && j < lineas.length - 1) {
                    l.reparto = {imp: r2(venta.subtotal * l.bruto / G), iva: r2(venta.iva * l.bruto / G)};
                    impAcum = r2(impAcum + l.reparto.imp);
                    ivaAcum = r2(ivaAcum + l.reparto.iva);
                } else if (G > 0 && venta.subtotal != null && venta.iva != null) {
                    l.reparto = {imp: r2(venta.subtotal - impAcum), iva: r2(venta.iva - ivaAcum)};
                }
                if (l.reparto) l.valorUnitario = r4(l.reparto.imp / (Number(l.cantidad) || 1));
            });
            setConceptos(prev => [...prev.slice(0, idx), ...lineas, ...prev.slice(idx + 1)]);
        } catch {
            alert(`No se encontró el ticket ${folio}`);
        }
    };

    const guardar = async (e) => {
        e.preventDefault();
        try {
            await facturasService.crear({
                clienteFiscalId: Number(clienteFiscalId),
                formaPago,
                metodoPago,
                conceptos: conceptos.map(c => ({
                    origen: c.origen,
                    origenFolio: c.origenFolio || undefined,
                    descripcion: c.descripcion,
                    claveProdServ: c.claveProdServ,
                    claveUnidad: c.claveUnidad,
                    unidad: c.unidad,
                    cantidad: Number(c.cantidad),
                    valorUnitario: Number(c.valorUnitario),
                    esCombustible: !!c.esCombustible
                }))
            });
            alert('Factura creada (sin timbrar)');
            setMostrarForm(false);
            setConceptos([conceptoVacio()]);
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al facturar');
        }
    };

    const descargar = async (id, tipo) => {
        try {
            const blob = tipo === 'xml' ? await facturasService.xml(id) : await facturasService.pdf(id);
            const url = window.URL.createObjectURL(new Blob([blob]));
            const a = document.createElement('a');
            a.href = url; a.download = `factura-${id}.${tipo}`;
            a.click();
        } catch {
            alert(`No se pudo descargar el ${tipo.toUpperCase()}`);
        }
    };

    const cancelar = async (fila) => {
        const motivo = window.prompt('Motivo de cancelación:', 'Error de captura');
        if (motivo === null) return;
        try {
            await facturasService.cancelar(fila.id, motivo);
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'No se pudo cancelar');
        }
    };

    const enviar = async (fila) => {
        try {
            const r = await facturasService.enviarCorreo(fila.id);
            alert(r.mensaje || 'Correo procesado');
        } catch {
            alert('No se pudo enviar el correo');
        }
    };

    const importes = conceptos.map(c => c.reparto ? c.reparto.imp : r2((Number(c.cantidad) || 0) * (Number(c.valorUnitario) || 0)));
    const ivas = conceptos.map(c => c.reparto ? c.reparto.iva : r2(importes[conceptos.indexOf(c)] * IVA));
    const totalCombustible = conceptos.reduce((s, c, i) => s + (c.esCombustible ? importes[i] : 0), 0);
    const totalAceite = conceptos.reduce((s, c, i) => s + (!c.esCombustible ? importes[i] : 0), 0);
    const subtotal = r2(totalCombustible + totalAceite);
    const iva = r2(ivas.reduce((s, v) => s + v, 0));
    const total = r2(subtotal + iva);

    const columnas = [
        columna('folio', 'Folio'),
        columna('receptorRfc', 'RFC receptor'),
        columna('receptorNombre', 'Receptor'),
        columna('total', 'Total', (v) => <strong style={{color: '#198754'}}>{dinero(v.total)}</strong>),
        columna('estado', 'Estado', (v) => (
            <span className={`badge ${v.estado === 'CANCELADA' ? 'bg-danger' : v.estado === 'TIMBRADA' ? 'bg-success' : 'bg-warning text-dark'}`}>
                {v.estado}{v.timbrada ? '' : ' · Sin timbrar'}
            </span>
        ))
    ];

    return (
        <div>
            <PageHeader
                titulo="Facturas"
                subtitulo="Emisión de CFDI con XML y PDF"
                etiquetaAccion="+ Nueva factura"
                onAccion={() => setMostrarForm(true)}
            />
            {mostrarForm && (
                <CardBase titulo="Nueva factura" style={{maxWidth: '1100px', padding: '1rem', marginBottom: '15px'}}>
                    <form onSubmit={guardar}>
                        <div className="row g-2 mb-2">
                            <div className="col-md-6">
                                <label className="form-label">Cliente fiscal</label>
                                <select className="form-select" value={clienteFiscalId} onChange={(e) => setClienteFiscalId(e.target.value)} required>
                                    <option value="">Seleccionar...</option>
                                    {fiscales.map(f => (
                                        <option key={f.id} value={f.id}>{f.rfc} — {f.razonSocial || 'sin razón'}</option>
                                    ))}
                                </select>
                            </div>
                            <div className="col-md-3">
                                <label className="form-label">Forma pago</label>
                                <select className="form-select" value={formaPago} onChange={(e) => setFormaPago(e.target.value)}>
                                    <option value="01">01 - Efectivo</option>
                                    <option value="03">03 - Transferencia</option>
                                    <option value="04">04 - Tarjeta</option>
                                </select>
                            </div>
                            <div className="col-md-3">
                                <label className="form-label">Método</label>
                                <select className="form-select" value={metodoPago} onChange={(e) => setMetodoPago(e.target.value)}>
                                    <option value="PUE">PUE - Único</option>
                                    <option value="PPD">PPD - Parcialidades</option>
                                </select>
                            </div>
                        </div>
                        {conceptos.map((c, i) => (
                            <div className="row g-2 mb-2" key={i} style={{borderBottom: '1px solid #eee', paddingBottom: '8px', fontSize: c.origen === 'TICKET' ? '1.05em' : undefined}}>
                                <div className="col-md-2">
                                    {c.origen === 'MANUAL_ACEITE' ? (
                                        <span className="badge bg-success" style={{fontSize: '0.9em'}}>ACEITE</span>
                                    ) : (
                                        <select className="form-select" value={c.origen} onChange={(e) => setConcepto(i, 'origen', e.target.value)}>
                                            <option value="TICKET">TICKET</option>
                                            <option value="NOTA_CREDITO">NOTA_CRÉDITO</option>
                                        </select>
                                    )}
                                </div>
                                <div className={c.origen === 'TICKET' ? 'col-md-3' : 'col-md-2'}>
                                    {c.origen === 'MANUAL_ACEITE' ? (
                                        <span className="text-muted">—</span>
                                    ) : (
                                        <>
                                        <div className="d-flex gap-1">
                                            <input className="form-control" placeholder="Folio origen (ej VEN-00001)" value={c.origenFolio} onChange={(e) => setConcepto(i, 'origenFolio', e.target.value)} />
                                            {c.origen === 'TICKET' && (
                                                <button type="button" className="btn btn-sm btn-outline-primary" title="Jalar datos del ticket" onClick={() => cargarTicket(i)}>↓</button>
                                            )}
                                        </div>
                                        {c.origenFolio && foliosFacturados[`${c.origen}::${c.origenFolio.trim()}`] && (
                                            <small className="text-danger">Ya facturado en {foliosFacturados[`${c.origen}::${c.origenFolio.trim()}`]}</small>
                                        )}
                                        </>
                                    )}
                                </div>
                                <div className={c.origen === 'TICKET' ? 'col-md-3' : 'col-md-3'}>
                                    <input className="form-control" placeholder="Descripción" value={c.descripcion} onChange={(e) => setConcepto(i, 'descripcion', e.target.value)} required readOnly={!!c.bloqueado} />
                                </div>
                                <div className="col-md-1">
                                    <input className="form-control" type="number" step="0.001" min="0.001" placeholder="Cant." value={c.cantidad} onChange={(e) => setConcepto(i, 'cantidad', e.target.value)} required readOnly={!!c.bloqueado} />
                                </div>
                                <div className="col-md-2">
                                    <input className="form-control" type="number" step="0.01" min="0.01" placeholder="P. unit." value={c.bloqueado ? Number(c.valorUnitario).toFixed(2) : c.valorUnitario} onChange={(e) => setConcepto(i, 'valorUnitario', e.target.value)} required readOnly={!!c.bloqueado} title="Precio unitario sin IVA" />
                                </div>
                                <div className="col-md-1 d-flex align-items-center gap-1">
                                    <button type="button" className="btn btn-sm btn-danger" onClick={() => setConceptos(prev => prev.filter((_, x) => x !== i))}>×</button>
                                </div>
                                <div className="col-12">
                                    <small className="text-muted">{c.origenFolio ? `Folio ${c.origenFolio} · ` : ''}Importe: <strong>{dinero(importes[i])}</strong></small>
                                </div>
                            </div>
                        ))}
                        <button type="button" className="btn btn-sm btn-outline-primary mb-2" onClick={() => setConceptos(prev => [...prev, conceptoVacio()])}>+ Agregar concepto</button>
                        <button type="button" className="btn btn-sm btn-outline-success mb-2 ms-2" onClick={() => setModalAceite(true)}>+ Agregar aceite</button>
                        <table className="table table-sm table-bordered mb-2" style={{maxWidth: '520px'}}>
                            <tbody>
                            <tr>
                                <td>Combustible</td>
                                <td className="text-end">{dinero(totalCombustible)}</td>
                            </tr>
                            <tr>
                                <td>Aceites</td>
                                <td className="text-end">{dinero(totalAceite)}</td>
                            </tr>
                            <tr>
                                <td>Subtotal</td>
                                <td className="text-end">{dinero(subtotal)}</td>
                            </tr>
                            <tr>
                                <td>IVA 16%</td>
                                <td className="text-end">{dinero(iva)}</td>
                            </tr>
                            <tr className="table-primary">
                                <td><strong>Total a facturar</strong></td>
                                <td className="text-end"><strong>{dinero(total)}</strong></td>
                            </tr>
                            </tbody>
                        </table>
                        <div className="d-flex gap-2">
                            <button type="submit" className="btn btn-primary">Facturar</button>
                            <button type="button" className="btn btn-secondary" onClick={() => setMostrarForm(false)}>Cancelar</button>
                        </div>
                    </form>
                </CardBase>
            )}
            {detalle && (
                <FacturaVista factura={detalle} onCerrar={() => setDetalle(null)} />
            )}
            {modalAceite && (
                <ModalAceite
                    onAgregar={(c) => { setConceptos(prev => [...prev, c]); setModalAceite(false); }}
                    onCerrar={() => setModalAceite(false)}
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={facturas}
                    acciones={(fila) => (
                        <div className="d-flex gap-1 flex-wrap">
                            <Acciones
                                fila={fila}
                                onExtra={() => setDetalle(fila)}
                                etiquetaExtra="Vista"
                                onEliminar={fila.estado === 'CANCELADA' ? undefined : cancelar}
                                etiquetaEliminar="Cancelar"
                                habilitadoEliminar={fila.estado !== 'CANCELADA'}
                            />
                            <button className="btn btn-sm btn-outline-secondary" style={{padding: '4px 8px', fontSize: '12px'}} onClick={() => descargar(fila.id, 'xml')}>XML</button>
                            <button className="btn btn-sm btn-outline-secondary" style={{padding: '4px 8px', fontSize: '12px'}} onClick={() => descargar(fila.id, 'pdf')}>PDF</button>
                            <button className="btn btn-sm btn-outline-info" style={{padding: '4px 8px', fontSize: '12px'}} onClick={() => enviar(fila)}>✉️</button>
                        </div>
                    )}
                />
            </EstadoCarga>
        </div>
    );
};

// Modal para agregar aceite a la factura: solo catálogo y precio,
// sin mover inventarios. Entra como concepto MANUAL_ACEITE.
const ModalAceite = ({onAgregar, onCerrar}) => {
    const [aceites, setAceites] = useState([]);
    const [aceiteId, setAceiteId] = useState('');
    const [cantidad, setCantidad] = useState('');

    useEffect(() => {
        aceitesService.listar().then(setAceites).catch(() => setAceites([]));
    }, []);

    const sel = (aceites || []).find(a => String(a.id) === String(aceiteId)) || null;
    const precioLista = Number(sel?.precioVenta) || 0;
    const precioNeto = netoExacto(precioLista);
    const total = (Number(cantidad) || 0) * precioLista;

    const agregar = () => {
        if (!sel) { alert('Selecciona el aceite'); return; }
        if (!Number(cantidad) || Number(cantidad) <= 0) { alert('Cantidad inválida'); return; }
        onAgregar({
            origen: 'MANUAL_ACEITE',
            origenFolio: '',
            descripcion: sel.nombre,
            claveProdServ: '15111515',
            claveUnidad: 'H87',
            unidad: 'Pieza',
            cantidad: Number(cantidad),
            valorUnitario: netoExacto(precioLista),
            esCombustible: false,
            bloqueado: true
        });
    };

    return (
        <>
            <div className="modal-backdrop fade show" style={{zIndex: 1040}} onClick={onCerrar}/>
            <div className="modal fade show d-block" tabIndex="-1" style={{zIndex: 1055}} role="dialog">
                <div className="modal-dialog" style={{margin: '60px auto'}}>
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">Agregar aceite</h5>
                            <button type="button" className="btn-close" onClick={onCerrar}/>
                        </div>
                        <div className="modal-body">
                            <label className="form-label">Aceite</label>
                            <select className="form-select mb-2" value={aceiteId} onChange={(e) => setAceiteId(e.target.value)}>
                                <option value="">Seleccionar...</option>
                                {aceites.map(a => (
                                    <option key={a.id} value={a.id}>{a.nombre} — ${Number(a.precioVenta).toFixed(2)}</option>
                                ))}
                            </select>
                            <div className="row g-2 align-items-end">
                                <div className="col-md-6">
                                    <label className="form-label">Precio lista (con IVA)</label>
                                    <input className="form-control" value={precioLista ? `$${precioLista.toFixed(2)}` : ''} readOnly />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">P. unit. neto (sin IVA)</label>
                                    <input className="form-control" value={precioNeto ? `$${precioNeto.toFixed(2)}` : ''} readOnly />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Cantidad</label>
                                    <input type="number" min="1" step="1" className="form-control" value={cantidad} onChange={(e) => setCantidad(e.target.value)} />
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label">Total (con IVA)</label>
                                    <input className="form-control" value={total ? `$${total.toFixed(2)}` : ''} readOnly />
                                </div>
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button type="button" className="btn btn-secondary" onClick={onCerrar}>Cancelar</button>
                            <button type="button" className="btn btn-success" onClick={agregar}>Agregar</button>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default FacturasList;
