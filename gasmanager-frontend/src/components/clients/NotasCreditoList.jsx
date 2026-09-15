import React, {useState, useEffect} from "react";
import {useNavigate, useLocation} from "react-router-dom";
import {notasCreditoService} from "../../api/clients/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const dinero = (v) => Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2});

const NotasCreditoList = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const {datos: notas, loading} = useLista(notasCreditoService);

    const [clienteFiltro, setClienteFiltro] = useState(location.state?.clienteId || '');
    const [abiertas, setAbiertas] = useState({});

    useEffect(() => {
        setClienteFiltro(location.state?.clienteId || '');
    }, [location.state?.clienteId]);

    const toggleDetalle = (id) => setAbiertas(prev => ({...prev, [id]: !prev[id]}));

    const notasFiltradas = clienteFiltro
        ? notas.filter(n => String(n.clienteId) === String(clienteFiltro))
        : notas;

    const notasActivas = clienteFiltro
        ? notas.filter(n => String(n.clienteId) === String(clienteFiltro) && n.estado === 'ACTIVA')
        : notas.filter(n => n.estado === 'ACTIVA');

    const clientesConNotas = Object.values(notas.reduce((acc, n) => {
        // Respaldo para notas viejas guardadas con nombre en blanco (física sin razón):
        // nunca mostrar el select vacío.
        if (n.clienteId) acc[String(n.clienteId)] = {id: n.clienteId, nombre: (n.clienteNombre && n.clienteNombre.trim()) ? n.clienteNombre : `Cliente #${n.clienteId}`};
        return acc;
    }, {}));

    const totalCliente = notasFiltradas.reduce((sum, n) => sum + (Number(n.saldo) || 0), 0);
    const totalActivoCliente = notasActivas.reduce((sum, n) => sum + (Number(n.saldo) || 0), 0);

    const creditosDelCliente = Object.values(notasActivas.filter(n => n.creditoId).reduce((acc, n) => {
        if (!acc[String(n.creditoId)]) {
            acc[String(n.creditoId)] = {
                creditoId: n.creditoId,
                creditoFolio: n.creditoFolio,
                saldo: 0
            };
        }
        acc[String(n.creditoId)].saldo += Number(n.saldo) || 0;
        return acc;
    }, {}));

    const seleccionarCliente = (idCliente, nombre) => {
        setClienteFiltro(idCliente);
        navigate('/clientes/notas-credito', {state: {clienteId: idCliente, clienteNombre: nombre}});
    };

    const getEstadoBadge = (estado) => {
        const colores = {
            ACTIVA: 'badge badge-success',
            AGOTADA: 'badge badge-secondary',
            BLOQUEADA: 'badge badge-warning',
            VENCIDA: 'badge badge-danger',
            PAGADA: 'badge badge-info'
        };
        return <span className={colores[estado] || 'badge badge-secondary'}>{estado}</span>;
    };

    const itemsDe = (nota) => {
        const items = nota.items || [];
        if (items.length === 0) return <span className="text-muted">Sin productos</span>;
        const fila = {display: 'flex', gap: '8px', padding: '2px 0', borderBottom: '1px solid #f1f3f5'};
        return (
            <div style={{fontSize: '0.85em'}}>
                <div className="fw-bold" style={{...fila, borderBottom: '1px solid #dee2e6', marginBottom: '4px'}}>
                    <span style={{flex: 2}}>Producto</span>
                    <span style={{flex: 1}}>Cant.</span>
                    <span style={{flex: 1}}>P. Unit.</span>
                    <span style={{flex: 1, textAlign: 'right'}}>Subtotal</span>
                </div>
                {items.map(i => (
                    <div key={i.id} style={fila}>
                        <span style={{flex: 2}}>{i.producto}</span>
                        <span style={{flex: 1}}>{i.cantidad} {i.unidad}</span>
                        <span style={{flex: 1}}>${Number(i.precioUnitario).toFixed(2)}</span>
                        <span style={{flex: 1, textAlign: 'right'}}><strong>${Number(i.subtotal).toFixed(2)}</strong></span>
                    </div>
                ))}
            </div>
        );
    };

    const campoMini = (label, valor, extra) => (
        <div className={extra}>
            <small className="text-muted">{label}</small>
            <div>{valor || '-'}</div>
        </div>
    );

    const clienteSeleccionado = clientesConNotas.find(c => String(c.id) === String(clienteFiltro));

    return (
        <div>
            <PageHeader titulo="Notas de Crédito" />
            <div style={{marginBottom: '20px', maxWidth: '900px'}}>
                <div className="card shadow-sm">
                    <div className="card-body">
                        <div className="d-flex gap-2 align-items-end">
                            <div style={{flex: 1}}>
                                <label className="form-label" style={{fontSize: '0.9em'}}>Seleccionar cliente</label>
                                <select className="form-select" value={clienteFiltro}
                                        onChange={(e) => setClienteFiltro(e.target.value)}>
                                    <option value="">Todos los clientes con notas</option>
                                    {clientesConNotas.map(c => (
                                        <option key={c.id} value={c.id}>{c.nombre}</option>
                                    ))}
                                </select>
                            </div>
                            <button className="btn btn-outline-primary"
                                    onClick={() => clienteSeleccionado
                                        ? seleccionarCliente(clienteSeleccionado.id, clienteSeleccionado.nombre)
                                        : alert('Primero elige un cliente de la lista')}>
                                Seleccionar
                            </button>
                            {clienteFiltro && (
                                <button className="btn btn-outline-secondary"
                                        onClick={() => navigate('/clientes/notas-credito')}>
                                    Limpiar
                                </button>
                            )}
                        </div>
                        {clienteFiltro && (
                            <div className="row g-3 mt-2">
                                <div className="col-md-3">
                                    <small className="text-muted">Cliente</small>
                                    <div className="fw-bold">{clienteSeleccionado?.nombre || location.state?.clienteNombre || '-'}</div>
                                </div>
                                <div className="col-md-3">
                                    <small className="text-muted">Notas del cliente</small>
                                    <div className="fw-bold">{notasFiltradas.length}</div>
                                </div>
                                <div className="col-md-3">
                                    <small className="text-muted">Suma total de notas</small>
                                    <div className="fw-bold" style={{color: '#0d6efd'}}>${dinero(totalCliente)}</div>
                                </div>
                                <div className="col-md-3">
                                    <small className="text-muted">Pendiente por liquidar (ACTIVAS)</small>
                                    <div className="fw-bold" style={{color: '#198754'}}>${dinero(totalActivoCliente)}</div>
                                </div>
                            </div>
                        )}
                        {clienteFiltro && totalActivoCliente > 0 && (
                            <div className="mt-3 d-flex align-items-center gap-2 flex-wrap"
                                 style={{borderTop: '1px solid #eee', paddingTop: '12px'}}>
                                <strong style={{fontSize: '0.9em'}}>Liquidar:</strong>
                                {creditosDelCliente.map(c => (
                                    <button key={c.creditoId} className="btn btn-success btn-sm"
                                            onClick={() => navigate(`/clientes/creditos/${c.creditoId}/liquidar`)}>
                                        {c.creditoFolio || `Crédito ${c.creditoId}`} — ${dinero(c.saldo)}
                                    </button>
                                ))}
                            </div>
                        )}
                        {clienteFiltro && totalActivoCliente <= 0 && (
                            <div className="mt-3 text-muted" style={{borderTop: '1px solid #eee', paddingTop: '12px'}}>
                                Este cliente no tiene notas pendientes por liquidar.
                            </div>
                        )}
                    </div>
                </div>
            </div>
            <EstadoCarga cargando={loading}>
                {notasFiltradas.length === 0 ? (
                    <p className="text-muted">No hay notas{clienteFiltro ? ' para este cliente' : ''}.</p>
                ) : (
                    <div style={{display: 'flex', flexDirection: 'column', gap: '8px', maxWidth: '900px'}}>
                        {notasFiltradas.map(nota => (
                            <div className="card shadow-sm" key={nota.id}>
                                <div className="card-header d-flex justify-content-between align-items-center py-1" style={{background: '#0f172a', color: '#fff'}}>
                                    <div className="d-flex align-items-center gap-2" style={{fontSize: '0.85em'}}>
                                        <strong>{nota.numero}</strong>
                                        {getEstadoBadge(nota.estado)}
                                    </div>
                                    <small style={{color: '#adb5bd', fontSize: '0.75em'}}>{nota.fechaCarga}</small>
                                </div>
                                <div className="card-body" style={{padding: '5px 12px', fontSize: '0.85em'}}>
                                    <div className="row g-1 align-items-center">
                                        {campoMini('Cliente', nota.clienteNombre, "col-6 col-md-3")}
                                        {campoMini('Crédito', nota.creditoFolio, "col-6 col-md-3")}
                                        <div className="col-6 col-md-3">
                                            <small className="text-muted">Total</small>
                                            <div className="fw-bold" style={{color: '#198754'}}>${dinero(nota.saldo)}</div>
                                        </div>
                                        <div className="col-12 col-md-3 text-md-end">
                                            <Acciones
                                                fila={nota}
                                                onToggle={() => toggleDetalle(nota.id)}
                                                etiquetaToggle={abiertas[nota.id] ? 'Ocultar' : 'Detalle'}
                                                onExtra={() => navigate(`/clientes/creditos/${nota.creditoId}`, {state: {from: '/clientes/notas-credito'}})}
                                                etiquetaExtra="Ver crédito"
                                                onEditar={nota.estado !== 'PAGADA' ? () => navigate(`/clientes/notas-credito/${nota.id}`) : undefined}
                                            />
                                        </div>
                                    </div>
                                    {abiertas[nota.id] && (
                                        <React.Fragment>
                                            <hr style={{margin: '8px 0'}}/>
                                            <div className="row g-1">
                                                {campoMini('Vehículo', nota.vehiculo, "col-6")}
                                                {campoMini('Conductor', nota.conductor, "col-6")}
                                            </div>
                                            <h6 className="fw-bold" style={{marginBottom: '4px', marginTop: '6px'}}>Productos</h6>
                                            {itemsDe(nota)}
                                        </React.Fragment>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </EstadoCarga>
        </div>
    );
};

export default NotasCreditoList;