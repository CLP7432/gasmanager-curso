import React, {useState, useEffect} from "react";
import {useNavigate, useLocation} from "react-router-dom";
import {creditosService, notasCreditoService} from "../../api/clients/auth.js";
import CardDatos from "../../kernel/components/CardDatos.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";

const dinero = (v) => Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2});

const LiquidarNotas = ({creditoId}) => {
    const navigate = useNavigate();
    const location = useLocation();
    const [credito, setCredito] = useState(null);
    const [notas, setNotas] = useState([]);
    const [seleccionadas, setSeleccionadas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [enviando, setEnviando] = useState(false);
    const [form, setForm] = useState({fechaPago: new Date().toISOString().split('T')[0], metodoPago: 'EFECTIVO', referenciaPago: '', notas: ''});
    const [pagoOk, setPagoOk] = useState(null);

    const cargarDatos = async () => {
        setLoading(true);
        try {
            const [c, n] = await Promise.all([
                creditosService.obtenerPorId(creditoId),
                notasCreditoService.listarPorCredito(creditoId)
            ]);
            setCredito(c);
            const activas = (Array.isArray(n) ? n : []).filter(x => x.estado === 'ACTIVA');
            setNotas(activas);
            setSeleccionadas(activas.map(x => x.id));
        } catch (error) {
            console.error('Error:', error);
        }
        setLoading(false);
    };

    useEffect(() => {
        cargarDatos();
    }, [creditoId]);

    const toggleSeleccion = (id) => {
        setSeleccionadas(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
    };

    const seleccionarTodas = () => setSeleccionadas(notas.map(x => x.id));
    const limpiarSeleccion = () => setSeleccionadas([]);

    const notasSeleccionadas = notas.filter(n => seleccionadas.includes(n.id));
    const totalSeleccion = notasSeleccionadas.reduce((sum, n) => sum + (Number(n.saldo) || 0), 0);

    const handleChange = (e) => {
        const {name, value} = e.target;
        setForm(prev => ({...prev, [name]: value}));
    };

    const handleLiquidar = async () => {
        if (seleccionadas.length === 0) return alert('Selecciona al menos una nota a pagar');
        if (!window.confirm(`Registrar el pago de $${dinero(totalSeleccion)} y marcar ${seleccionadas.length} nota(s) como pagada(s)?`)) return;
        setEnviando(true);
        try {
            await notasCreditoService.liquidar({
                notaIds: seleccionadas,
                fechaPago: form.fechaPago,
                metodoPago: form.metodoPago,
                referenciaPago: form.referenciaPago,
                notas: form.notas
            });
            // El cliente está presente pagando: ofrecer facturar esas notas de inmediato
            setPagoOk({notas: notasSeleccionadas, total: totalSeleccion, metodoPago: form.metodoPago, fechaPago: form.fechaPago});
        } catch (error) {
            alert(error.response?.data?.message || 'Error al registrar el pago');
        }
        setEnviando(false);
    };

    if (loading) return <EstadoCarga cargando={true}/>;
    if (!credito) return <p>Crédito no encontrado.</p>;

    // Pago registrado: el cliente está en oficina y pide su factura
    if (pagoOk) {
        return (
            <div>
                <div className="card shadow-sm" style={{maxWidth: '900px', borderTop: '4px solid #198754'}}>
                    <div className="card-body">
                        <h4>✅ Pago registrado: ${dinero(pagoOk.total)}</h4>
                        <p className="text-muted mb-2">
                            {pagoOk.notas.length} nota(s) de {credito.clienteNombre} marcada(s) como pagada(s) · {pagoOk.metodoPago} · {pagoOk.fechaPago}
                        </p>
                        {(pagoOk.notas || []).map(n => (
                            <div key={n.id} className="d-flex justify-content-between" style={{borderBottom: '1px solid #f1f3f5', padding: '3px 0'}}>
                                <span><strong>{n.numero}</strong> <small className="text-muted">{(n.items || []).map(i => i.producto).join(', ')}</small></span>
                                <span className="fw-bold" style={{color: '#198754'}}>${dinero(n.saldo)}</span>
                            </div>
                        ))}
                        <div className="d-flex gap-2 mt-3 flex-wrap">
                            <button
                                className="btn btn-primary"
                                onClick={() => navigate('/facturacion/facturas', {
                                    state: {facturarNotas: pagoOk.notas, clienteId: credito.clienteId, clienteNombre: credito.clienteNombre}
                                })}
                            >
                                🧾 Facturar ahora
                            </button>
                            <button
                                className="btn btn-secondary"
                                onClick={() => navigate('/clientes/notas-credito', {
                                    state: {clienteId: credito.clienteId, clienteNombre: credito.clienteNombre}
                                })}
                            >
                                Volver a Notas
                            </button>
                            <button
                                className="btn btn-outline-secondary"
                                onClick={() => { setPagoOk(null); setSeleccionadas([]); cargarDatos(); }}
                            >
                                Registrar otro pago
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div>
            <button className="btn btn-secondary" style={{marginBottom: '20px'}}
                    onClick={() => navigate('/clientes/notas-credito', {
                        state: {clienteId: credito.clienteId, clienteNombre: credito.clienteNombre}
                    })}>
                ← Volver a Notas de Crédito
            </button>

            <div className="row g-3 mb-3">
                <div className="col-md-5">
                    <CardDatos
                        titulo={`Liquidar notas de ${credito.clienteNombre}`}
                        objeto={credito}
                        campos={[
                            {label: 'Crédito (límite)', campo: 'folioCredito'},
                            {label: 'Cliente', campo: 'clienteNombre'},
                            {label: 'Límite del crédito', campo: 'montoTotal'},
                            {label: 'Estado', campo: 'estado'}
                        ]}
                    />
                </div>
                <div className="col-md-7">
                    <div className="card shadow-sm" style={{maxWidth: '900px'}}>
                        <div className="card-header d-flex justify-content-between align-items-center" style={{background: '#0f172a', color: '#fff', padding: '6px 12px'}}>
                            <strong>Registrar pago</strong>
                            <div style={{color: '#adb5bd', fontSize: '0.85em'}}>
                                A pagar: <span className="fw-bold" style={{color: '#fff'}}>${dinero(totalSeleccion)}</span>
                            </div>
                        </div>
                        <div className="card-body" style={{padding: '10px 12px'}}>
                            <div className="row g-2">
                                <div className="col-md-6">
                                    <label className="form-label small mb-1">Monto a pagar</label>
                                    <input className="form-control form-control-sm" value={dinero(totalSeleccion)} disabled/>
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small mb-1">Fecha de pago</label>
                                    <input type="date" className="form-control form-control-sm" name="fechaPago" value={form.fechaPago} onChange={handleChange}/>
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small mb-1">Método de pago</label>
                                    <select className="form-select form-select-sm" name="metodoPago" value={form.metodoPago} onChange={handleChange}>
                                        <option value="EFECTIVO">Efectivo</option>
                                        <option value="TRANSFERENCIA">Transferencia</option>
                                        <option value="TARJETA">Tarjeta</option>
                                        <option value="CHEQUE">Cheque</option>
                                    </select>
                                </div>
                                <div className="col-md-6">
                                    <label className="form-label small mb-1">Referencia de pago</label>
                                    <input className="form-control form-control-sm" name="referenciaPago" value={form.referenciaPago} onChange={handleChange}/>
                                </div>
                                <div className="col-12">
                                    <label className="form-label small mb-1">Observaciones</label>
                                    <textarea className="form-control form-control-sm" rows="1" name="notas" value={form.notas} onChange={handleChange}/>
                                </div>
                                <div className="col-12 d-flex justify-content-end">
                                    <button className="btn btn-sm btn-success" disabled={enviando || seleccionadas.length === 0} onClick={handleLiquidar}>
                                        {enviando ? 'Registrando...' : `Pagar $${dinero(totalSeleccion)}`}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="card shadow-sm mt-1" style={{maxWidth: '1100px'}}>
                <div className="card-header d-flex justify-content-between align-items-center" style={{background: '#0f172a', color: '#fff', padding: '6px 12px'}}>
                    <strong>Notas por pagar de {credito.clienteNombre} ({notas.length})</strong>
                    <div style={{color: '#adb5bd', fontSize: '0.85em'}}>
                        Total pendiente: <span className="fw-bold" style={{color: '#fff'}}>${dinero(notas.reduce((s, n) => s + Number(n.saldo || 0), 0))}</span>
                        <button className="btn btn-sm btn-outline-light ms-2 py-0" onClick={seleccionarTodas}>Todas</button>
                        <button className="btn btn-sm btn-outline-light ms-1 py-0" onClick={limpiarSeleccion}>Ninguna</button>
                    </div>
                </div>
                <div className="card-body" style={{padding: '8px 12px'}}>
                    {notas.length === 0 ? (
                        <p className="text-muted mb-0 small">Este cliente no tiene notas pendientes por pagar.</p>
                    ) : (
                        <div style={{display: 'flex', flexDirection: 'column', gap: '4px', maxHeight: '320px', overflowY: 'auto'}}>
                            {notas.map(nota => (
                                <div key={nota.id} className="d-flex align-items-center gap-2 px-2 py-1"
                                     style={{border: seleccionadas.includes(nota.id) ? '1px solid #0d6efd' : '1px solid #e9ecef', borderRadius: '6px', background: seleccionadas.includes(nota.id) ? '#f0f7ff' : '#fff', fontSize: '0.85em'}}>
                                    <input type="checkbox" className="form-check-input" style={{marginTop: 0}}
                                           checked={seleccionadas.includes(nota.id)}
                                           onChange={() => toggleSeleccion(nota.id)}/>
                                    <div className="flex-grow-1 text-truncate">
                                        <strong>{nota.numero}</strong> <small className="text-muted">{nota.fechaCarga}</small>{' '}
                                        <span className="text-muted">
                                            {nota.vehiculo ? `· ${nota.vehiculo} ` : ''}
                                            {(nota.items || []).map(i => i.producto).join(', ')}
                                        </span>
                                    </div>
                                    <div className="fw-bold" style={{color: '#198754', whiteSpace: 'nowrap'}}>${dinero(nota.saldo)}</div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default LiquidarNotas;