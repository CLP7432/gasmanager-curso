import React, {useEffect, useState} from "react";
import {cortesService, turnosService, dispensariosService} from "../../api/ventas/auth.js";
import {aceitesService} from "../../api/inventarios/auth.js";
import {creditosService} from "../../api/clients/auth.js";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {columna} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import EditarCorteModal from "./EditarCorteModal.jsx";

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

const badgeEstado = (estado) => {
    if (estado === 'PENDIENTE') return <span className="badge badge-warning">PENDIENTE</span>;
    if (estado === 'VALIDADO') return <span className="badge badge-info">VALIDADO</span>;
    return <span className="badge badge-success">CERRADO</span>;
};

const corteVacio = () => ({
    turnoId: '',
    dispensarioId: '',
    efectivoEntregado: '',
    tarjetaRecibido: '',
    transferenciaRecibido: '',
    observaciones: '',
    aceites: [],
    notasCredito: [],
    creditos: []
});

const CortesLista = () => {
    const {user} = useAuth();
    const [cortes, setCortes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [estadoFiltro, setEstadoFiltro] = useState('');
    const [mostrarForm, setMostrarForm] = useState(false);

    const [turnos, setTurnos] = useState([]);
    const [dispensarios, setDispensarios] = useState([]);
    const [resumen, setResumen] = useState(null);
    const [cargandoResumen, setCargandoResumen] = useState(false);
    const [catalogoAceites, setCatalogoAceites] = useState([]);
    const [clientesCredito, setClientesCredito] = useState([]);
    const [crediForm, setCrediForm] = useState({creditoId: '', tipoCombustible: '', importe: '', aceites: ''});
    const [form, setForm] = useState(corteVacio());
    const [turnosDisponibles, setTurnosDisponibles] = useState([]);
    const [dispensariosPorTurno, setDispensariosPorTurno] = useState({});
    const [cortesExistentes, setCortesExistentes] = useState([]);
    const [corteModalEdicion, setCorteModalEdicion] = useState(null);

    const cargar = async (estado) => {
        setLoading(true);
        try {
            setCortes(await cortesService.listar(estado || undefined));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { cargar(estadoFiltro); }, [estadoFiltro]);

    useEffect(() => {
        const inicial = async () => {
            const [listaTurnos, disp, listaCortes] = await Promise.all([
                turnosService.listar(),
                dispensariosService.listarCompletos(),
                cortesService.listar().catch(() => [])
            ]);
            setTurnos(listaTurnos);
            setDispensarios(disp.filter(d => d.activo));
            setCortesExistentes(listaCortes);
            calcularDisponibles(listaTurnos, disp.filter(d => d.activo), listaCortes);
        };
        inicial();
    }, []);

    useEffect(() => {
        aceitesService.listar()
            .then(setCatalogoAceites)
            .catch(() => setCatalogoAceites([]));
        creditosService.listarActivosConSaldo()
            .then(setClientesCredito)
            .catch(() => setClientesCredito([]));
    }, []);

    const calcularDisponibles = (listaTurnos, listaDisp, listaCortes) => {
        const cortados = {};
        (listaCortes || []).forEach(c => {
            if (c.turnoId == null) return;
            if (!cortados[c.turnoId]) cortados[c.turnoId] = new Set();
            cortados[c.turnoId].add(String(c.dispensarioId));
        });
        const activos = (listaDisp || []).filter(d => d.activo);
        const disponibles = (listaTurnos || []).filter(t =>
            activos.some(d => !(cortados[t.id] || new Set()).has(String(d.id))));
        const porTurno = {};
        (listaTurnos || []).forEach(t => {
            porTurno[t.id] = activos.filter(d => !(cortados[t.id] || new Set()).has(String(d.id)));
        });
        setTurnosDisponibles(disponibles);
        setDispensariosPorTurno(porTurno);
        return disponibles;
    };

    const handleNuevo = async () => {
        const [listaTurnos, listaCortes] = await Promise.all([
            turnosService.listar(),
            cortesService.listar().catch(() => [])
        ]);
        setTurnos(listaTurnos);
        setCortesExistentes(listaCortes);
        const disponibles = calcularDisponibles(listaTurnos, dispensarios, listaCortes);
        const preseleccion = disponibles.find(t => t.estado === 'ABIERTO') || disponibles[0];
        setForm({
            ...corteVacio(),
            turnoId: preseleccion ? String(preseleccion.id) : ''
        });
        setResumen(null);
        setCorteModalEdicion(null);
        setMostrarForm(true);
    };

    const handleChange = (e) => {
        const {name, value} = e.target;
        if (name === 'turnoId') {
            setForm(prev => ({...prev, turnoId: value, dispensarioId: ''}));
            setResumen(null);
        } else {
            setForm(prev => ({...prev, [name]: value}));
        }
    };

    useEffect(() => {
        if (mostrarForm && form.turnoId && form.dispensarioId) {
            const cargarResumen = async () => {
                setCargandoResumen(true);
                setResumen(null);
                try {
                    const r = await cortesService.resumen(Number(form.turnoId), Number(form.dispensarioId));
                    setResumen(r);
                    setCrediForm(prev => {
                        const familias = familiasDeResumen(r);
                        const vigente = prev.tipoCombustible && familias.includes(prev.tipoCombustible);
                        return {...prev, tipoCombustible: vigente ? prev.tipoCombustible : (familias[0] || '')};
                    });
                    const sobrantes = (r.aceitesCorte || []).map(a => ({
                        surtidorAceiteId: a.surtidorAceiteId,
                        aceiteId: a.aceiteId,
                        vendidos: 0
                    }));
                    setForm(prev => ({...prev, aceites: sobrantes}));
                } catch (error) {
                    alert(error.response?.data?.message || 'Error al consultar el detalle del turno');
                } finally {
                    setCargandoResumen(false);
                }
            };
            cargarResumen();
        } else {
            setResumen(null);
        }
    }, [mostrarForm, form.turnoId, form.dispensarioId]);

    const familiasDeResumen = (r) => [...new Set((r?.combustibles || [])
        .map(c => familiaCombustible(c.producto))
        .filter(Boolean))];

    const opcionesCombustible = {};
    (resumen?.combustibles || []).forEach(c => {
        const f = familiaCombustible(c.producto);
        if (!f) return;
        const cantidad = Number(c.cantidad) || 0;
        if (!opcionesCombustible[f]) {
            opcionesCombustible[f] = cantidad > 0 ? Number(c.importe) / cantidad : 0;
        }
    });

    const precioLitro = opcionesCombustible[crediForm.tipoCombustible] || 0;
    const litrosCalculados = crediForm.importe && precioLitro > 0 ? Number(crediForm.importe) / precioLitro : 0;

    const vendidoPorFamilia = {};
    (resumen?.combustibles || []).forEach(c => {
        const f = familiaCombustible(c.producto);
        if (!f) return;
        if (!vendidoPorFamilia[f]) vendidoPorFamilia[f] = {litros: 0, importe: 0};
        vendidoPorFamilia[f].litros += Number(c.cantidad) || 0;
        vendidoPorFamilia[f].importe += Number(c.importe) || 0;
    });

    const creditosUsadosPorFamilia = {};
    (form.creditos || []).forEach(c => {
        const f = (c.tipoCombustible || '').toUpperCase();
        if (!f) return;
        if (!creditosUsadosPorFamilia[f]) creditosUsadosPorFamilia[f] = {litros: 0, importe: 0};
        creditosUsadosPorFamilia[f].litros += Number(c.litros) || 0;
        creditosUsadosPorFamilia[f].importe += Number(c.importe) || 0;
    });

    const aceitesCorte = resumen?.aceitesCorte || [];
    const aceitesParaTabla = [...aceitesCorte.map(a => ({...a, enIsla: true}))];
    catalogoAceites.forEach(c => {
        if (!aceitesParaTabla.some(a => a.aceiteId === c.id)) {
            aceitesParaTabla.push({aceiteId: c.id, aceiteNombre: c.nombre, recibidoTotal: 0, precioVenta: c.precioVenta, enIsla: false});
        }
    });
    const importeAceites = aceitesCorte.reduce((sum, a) => {
        const entrada = form.aceites.find(f => f.aceiteId === a.aceiteId);
        const vendidos = entrada ? Number(entrada.vendidos) || 0 : Number(a.recibidoTotal || 0);
        return sum + vendidos * Number(a.precioVenta || 0);
    }, 0);

    const notasCreditoTotal = Number(resumen?.notasCreditoTotal || 0);
    const notasCreditoManual = (form.notasCredito || []).reduce((sum, n) => sum + (Number(n.importe) || 0), 0);
    const creditoTotal = (form.creditos || []).reduce((sum, c) => sum + (Number(c.importe) || 0) + (Number(c.aceites) || 0), 0);
    const cargosConcepto = notasCreditoTotal + notasCreditoManual + creditoTotal;
    const tarjetaRecibido = Number(form.tarjetaRecibido) || 0;
    const transferenciaRecibido = Number(form.transferenciaRecibido) || 0;
    const netoEfectivo = Number(resumen?.esperadoEfectivo || 0) + importeAceites - cargosConcepto - tarjetaRecibido - transferenciaRecibido;
    const efectivoEntregado = Number(form.efectivoEntregado) || 0;
    const diferencia = netoEfectivo - efectivoEntregado;

    const [mostrarAceitesModal, setMostrarAceitesModal] = useState(false);

    const handleVendidos = (aceiteId, valor) => {
        setForm(prev => {
            const existe = prev.aceites.some(a => a.aceiteId === aceiteId);
            const base = aceitesParaTabla.find(a => a.aceiteId === aceiteId);
            return {
                ...prev,
                aceites: existe
                    ? prev.aceites.map(a => a.aceiteId === aceiteId ? {...a, vendidos: valor} : a)
                    : [...prev.aceites, {aceiteId, surtidorAceiteId: base?.surtidorAceiteId ?? null, vendidos: valor}]
            };
        });
    };

    const handleAddCredito = () => {
        const importe = Number(crediForm.importe);
        const credito = clientesCredito.find(c => String(c.id) === String(crediForm.creditoId));
        if (!credito) { alert('Selecciona un crédito de cliente con saldo disponible'); return; }
        if (!crediForm.tipoCombustible) {
            alert('Este corte no tiene ventas del combustible para registrar el cargo a crédito');
            return;
        }
        if (!importe || importe <= 0) { alert('Importe inválido'); return; }

        const fam = crediForm.tipoCombustible.toUpperCase();
        const vendido = vendidoPorFamilia[fam] || {litros: 0, importe: 0};
        const usado = creditosUsadosPorFamilia[fam] || {litros: 0, importe: 0};

        const litrosDisponibles = vendido.litros - usado.litros;
        const importeDisponible = vendido.importe - usado.importe;

        if (litrosCalculados > litrosDisponibles) {
            alert(`No puedes agregar esta nota de crédito, supera lo vendido de ${fam}`);
            return;
        }
        if (importe > importeDisponible) {
            alert(`No puedes agregar esta nota de crédito, supera lo vendido de ${fam}`);
            return;
        }

        const aceitesEnCredito = Number(crediForm.aceites) || 0;
        if (aceitesEnCredito > 0 && importeAceites <= 0) {
            alert('No puedes agregar aceites, no se vendieron aceites en este turno');
            return;
        }
        const aceitesUsados = (form.creditos || []).reduce((s, c) => s + (Number(c.aceites) || 0), 0);
        if (aceitesUsados + aceitesEnCredito > importeAceites) {
            alert('No puedes agregar aceites, supera lo vendido en el turno');
            return;
        }

        setForm(prev => ({
            ...prev,
            creditos: [...prev.creditos, {
                creditoId: credito.id,
                clienteId: credito.clienteId,
                clienteNombre: credito.clienteNombre,
                tipoCombustible: crediForm.tipoCombustible,
                litros: Number(litrosCalculados.toFixed(4)),
                importe,
                aceites: Number(crediForm.aceites) || 0,
                fecha: hoy()
            }]
        }));
        setCrediForm({creditoId: '', tipoCombustible: crediForm.tipoCombustible, importe: '', aceites: ''});
    };

    const handleRemoveCredito = (idx) => {
        setForm(prev => ({
            ...prev,
            creditos: prev.creditos.filter((_, i) => i !== idx)
        }));
    };

    const handleGenerar = async (e) => {
        e.preventDefault();
        if (!form.turnoId || !form.dispensarioId) { alert('Selecciona turno y surtidor'); return; }
        if (!resumen) { alert('Espera a que cargue el detalle de las ventas'); return; }
        try {
            await cortesService.generar({
                turnoId: Number(form.turnoId),
                dispensarioId: Number(form.dispensarioId),
                efectivoRecibido: efectivoEntregado,
                tarjetaRecibido: Number(form.tarjetaRecibido) || 0,
                transferenciaRecibido: Number(form.transferenciaRecibido) || 0,
                observaciones: form.observaciones.trim(),
                aceites: form.aceites.map(a => {
                    const rec = aceitesCorte.find(x => x.aceiteId === a.aceiteId);
                    const recibido = rec ? Number(rec.recibidoTotal || 0) : 0;
                    const vendidos = Number(a.vendidos) || 0;
                    return {
                        surtidorAceiteId: a.surtidorAceiteId,
                        aceiteId: a.aceiteId,
                        sobrante: Math.max(recibido - vendidos, 0)
                    };
                }),
                notasCredito: form.notasCredito.map(n => ({
                    folio: n.folio,
                    fecha: n.fecha,
                    cliente: n.cliente,
                    litros: n.litros !== undefined ? Number(n.litros) : undefined,
                    importe: Number(n.importe) || 0
                })),
                creditos: form.creditos.map(c => ({
                    creditoId: c.creditoId,
                    clienteId: c.clienteId,
                    clienteNombre: c.clienteNombre,
                    tipoCombustible: c.tipoCombustible,
                    litros: c.litros,
                    importe: c.importe,
                    aceites: c.aceites !== undefined ? Number(c.aceites) || 0 : undefined,
                    fecha: c.fecha
                }))
            });
            alert('Corte generado');
            calcularDisponibles(turnos, dispensarios, [...cortesExistentes, {
                turnoId: Number(form.turnoId),
                dispensarioId: Number(form.dispensarioId)
            }]);
            setMostrarForm(false);
            cargar('');
        } catch (error) {
            alert(error.response?.data?.message || 'Error al guardar el corte');
        }
    };

    const handleValidar = async (corte) => {
        const autorizadoPor = window.prompt('Nombre de quien autoriza la validación:', user?.correo || '');
        if (!autorizadoPor) return;
        try {
            await cortesService.validar(corte.id, autorizadoPor);
            cargar('');
        } catch (error) {
            alert(error.response?.data?.message || 'Error al validar el corte');
        }
    };

    const handleCerrar = async (corte) => {
        if (window.confirm(`¿Cerrar el corte ${corte.codigoCorte}?`)) {
            try {
                await cortesService.cerrar(corte.id);
                cargar('');
            } catch (error) {
                alert(error.response?.data?.message || 'Error al cerrar el corte');
            }
        }
    };

    const handleEditar = async (corte) => {
        setCorteModalEdicion(corte);
    };

    const columnas = [
        columna('codigoCorte', 'Código'),
        columna('dispensarioNombre', 'Dispensario'),
        columna('despachadorNombre', 'Despachador'),
        columna('numeroVentas', 'Ventas'),
        columna('totalVentas', 'Total', (c) => dinero(c.totalVentas)),
        columna('esperadoEfectivo', 'Esperado', (c) => dinero(c.esperadoEfectivo)),
        columna('efectivoRecibido', 'Recibido', (c) => dinero(c.efectivoRecibido)),
        {
            key: 'diferencia',
            label: 'Diferencia',
            render: (c) => {
                const dif = Number(c.diferenciaEfectivo);
                const signo = dif > 0 ? '▲' : (dif < 0 ? '▼' : '');
                const clase = dif > 0 ? 'text-danger fw-bold' : 'text-success';
                return <span className={clase}>{signo} {dinero(Math.abs(dif))}</span>;
            }
        },
        {key: 'estado', label: 'Estado', render: (c) => badgeEstado(c.estado)}
    ];

    return (
        <div>
            <PageHeader
                titulo="Cortes de Turno"
                subtitulo="Esperado vs declarado por isla; la diferencia dispara la incidencia FALTANTE"
                mostrarAccion
                etiquetaAccion="+ Generar Corte"
                onAccion={handleNuevo}
            />
            <div className="btn-group mb-3">
                {['', 'PENDIENTE', 'VALIDADO', 'CERRADO'].map(estado => (
                    <button key={estado || 'todos'}
                            className={`btn btn-sm ${estadoFiltro === estado ? 'btn-primary' : 'btn-outline-primary'}`}
                            onClick={() => setEstadoFiltro(estado)}>
                        {estado || 'Todos'}
                    </button>
                ))}
            </div>

            {mostrarForm && (
                <div className="card" style={{maxWidth: '1100px', padding: '1rem'}}>
                    <h4>Generar Corte de Turno</h4>
                    <form onSubmit={handleGenerar}>
                        <div className="row g-3 mb-3">
                            <div className="col-md-6">
                                <label className="form-label">Turno</label>
                                <select className="form-select" name="turnoId" value={form.turnoId} onChange={handleChange}>
                                    <option value="">Seleccionar...</option>
                                    {turnosDisponibles.length === 0 ? (
                                        <option value="" disabled>No hay turnos con cortes pendientes</option>
                                    ) : (
                                        turnosDisponibles.map(t => (
                                            <option key={t.id} value={t.id}>
                                                {t.codigoTurno} — {t.nombre} ({t.fechaTurno}) — {t.estado || 'SIN ESTADO'}
                                            </option>
                                        ))
                                    )}
                                </select>
                            </div>
                            <div className="col-md-6">
                                <label className="form-label">Surtidor (Dispensario)</label>
                                <select className="form-select" name="dispensarioId" value={form.dispensarioId} onChange={handleChange}>
                                    <option value="">Seleccionar...</option>
                                    {(dispensariosPorTurno[form.turnoId] || []).length === 0 ? (
                                        <option value="" disabled>
                                            {form.turnoId ? 'Este turno ya no tiene dispensarios por cortar' : 'Elige primero un turno con cortes pendientes'}
                                        </option>
                                    ) : (
                                        (dispensariosPorTurno[form.turnoId] || []).map(d => (
                                            <option key={d.id} value={d.id}>{d.nombre} — {d.despachadorNombre || 'sin despachador'}</option>
                                        ))
                                    )}
                                </select>
                            </div>
                        </div>

                        {cargandoResumen && <p className="text-muted">Calculando ventas del turno...</p>}

                        {resumen && !cargandoResumen && (
                            <div>
                                <div className="row g-2 mb-2">
                                    <div className="col-md-6">
                                        <div className="card h-100" style={{padding: 0}}>
                                            <div className="card-body py-1">
                                                <div className="mb-1">
                                                    <strong>{resumen.dispensarioNombre}</strong>
                                                    {' '}· Despachador: <strong>{resumen.despachadorNombre || 'sin asignar'}</strong>
                                                    {' '}· <span className="text-muted">{resumen.numeroVentas} venta(s), {Number(resumen.totalLitros).toFixed(2)} L</span>
                                                </div>
                                                <h6 className="mb-1">⛽ Combustible vendido</h6>
                                                <table className="table table-sm table-bordered mb-0">
                                                    <thead>
                                                    <tr><th>Producto</th><th className="text-end">Litros</th><th className="text-end">Importe</th></tr>
                                                    </thead>
                                                    <tbody>
                                                    {(resumen.combustibles || []).map(c => (
                                                        <tr key={c.producto}>
                                                            <td>{c.producto}</td>
                                                            <td className="text-end">{Number(c.cantidad).toFixed(2)}</td>
                                                            <td className="text-end">{dinero(c.importe)}</td>
                                                        </tr>
                                                    ))}
                                                    {!resumen.combustibles?.length && (
                                                        <tr><td colSpan="3" className="text-muted">Sin ventas de combustible</td></tr>
                                                    )}
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-md-6">
                                        <div className="card h-100" style={{padding: 0}}>
                                            <div className="card-body py-1">
                                                <div className="d-flex justify-content-between align-items-center mb-1">
                                                    <h6 className="mb-0">🛢️ Aceites en la isla</h6>
                                                    <button type="button" className="btn btn-sm btn-outline-primary" onClick={() => setMostrarAceitesModal(true)}>➕ Agregar aceites</button>
                                                </div>
                                                <div className="d-flex justify-content-between">
                                                    <span>Total aceites vendidos</span>
                                                    <strong>{dinero(importeAceites)}</strong>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {mostrarAceitesModal && (
                                    <>
                                        <div className="modal-backdrop fade show" style={{zIndex: 1040}} onClick={() => setMostrarAceitesModal(false)}/>
                                        <div className="modal fade show d-block" tabIndex="-1" style={{zIndex: 1055}} role="dialog">
                                            <div className="modal-dialog modal-lg modal-dialog-scrollable">
                                                <div className="modal-content">
                                                    <div className="modal-header">
                                                        <h5 className="modal-title">Agregar aceites vendidos</h5>
                                                        <button type="button" className="btn-close" onClick={() => setMostrarAceitesModal(false)}/>
                                                    </div>
                                                    <div className="modal-body">
                                                        <table className="table table-sm table-bordered mb-0">
                                                            <thead>
                                                            <tr>
                                                                <th>Producto</th>
                                                                <th className="text-end">Recibido</th>
                                                                <th className="text-end">Sobrante físico</th>
                                                                <th className="text-end">Vendidos</th>
                                                                <th className="text-end">Importe</th>
                                                            </tr>
                                                            </thead>
                                                            <tbody>
                                                            {aceitesParaTabla.map(a => {
                                                                const entrada = form.aceites.find(f => f.aceiteId === a.aceiteId);
                                                                const recibido = Number(a.recibidoTotal || 0);
                                                                const vendidos = entrada ? Number(entrada.vendidos) || 0 : 0;
                                                                const sobrante = Math.max(recibido - vendidos, 0);
                                                                const importe = vendidos * Number(a.precioVenta || 0);
                                                                return (
                                                                    <tr key={a.aceiteId} className={a.enIsla ? '' : 'text-muted small'}>
                                                                        <td>{a.aceiteNombre}</td>
                                                                        <td className="text-end">{recibido.toFixed(0)}</td>
                                                                        <td className="text-end">{sobrante.toFixed(0)}</td>
                                                                        <td>
                                                                            <input type="number" min="0" step="1" className="form-control form-control-sm"
                                                                                   value={vendidos}
                                                                                   onChange={(e) => handleVendidos(a.aceiteId, e.target.value)}/>
                                                                        </td>
                                                                        <td className="text-end">{dinero(importe)}</td>
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
                                                    </div>
                                                    <div className="modal-footer">
                                                        <button type="button" className="btn btn-secondary" onClick={() => setMostrarAceitesModal(false)}>Cancelar</button>
                                                        <button type="button" className="btn btn-success" onClick={() => setMostrarAceitesModal(false)}>✔ Aceptar</button>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </>
                                )}

                                <div className="row g-2 mb-2">
                                    <div className="col-12">
                                        <div className="card" style={{padding: 0}}>
                                            <div className="card-body py-1">
                                                <div className="d-flex justify-content-between align-items-center mb-1">
                                                    <h6 className="text-danger mb-0">🧾 Cargo a crédito de cliente</h6>
                                                    <button type="button" className="btn btn-sm btn-outline-danger" onClick={handleAddCredito}>+ Agregar</button>
                                                </div>
                                                <div className="row g-2">
                                                    <div className="col-md-3">
                                                        <label className="form-label small mb-1">Crédito (cliente)</label>
                                                        <select className="form-select form-select-sm" value={crediForm.creditoId}
                                                                onChange={(e) => setCrediForm({...crediForm, creditoId: e.target.value})}>
                                                            <option value="">— Selecciona —</option>
                                                            {clientesCredito.map(c => (
                                                                <option key={c.id} value={c.id}>
                                                                    {c.folioCredito} — {c.clienteNombre} (saldo: ${Number(c.saldoPendiente).toLocaleString('es-MX', {minimumFractionDigits: 2})})
                                                                </option>
                                                            ))}
                                                        </select>
                                                    </div>
                                                    <div className="col-md-3">
                                                        <label className="form-label small mb-1">Combustible (según el corte)</label>
                                                        <select className="form-select form-select-sm" value={crediForm.tipoCombustible}
                                                                onChange={(e) => setCrediForm({...crediForm, tipoCombustible: e.target.value})}
                                                                disabled={Object.keys(opcionesCombustible).length === 0}>
                                                            {Object.keys(opcionesCombustible).length === 0 ? (
                                                                <option value="">Sin ventas de combustible</option>
                                                            ) : (
                                                                Object.keys(opcionesCombustible).map(f => (
                                                                    <option key={f} value={f}>{f}</option>
                                                                ))
                                                            )}
                                                        </select>
                                                    </div>
                                                    <div className="col-md-3">
                                                        <label className="form-label small mb-1">Importe $</label>
                                                        <input type="number" step="0.01" min="0" className="form-control form-control-sm" placeholder="0.00"
                                                               value={crediForm.importe}
                                                               onChange={(e) => setCrediForm({...crediForm, importe: e.target.value})}/>
                                                    </div>
                                                    <div className="col-md-3">
                                                        <label className="form-label small mb-1">Aceites $</label>
                                                        <input type="number" step="0.01" min="0" className="form-control form-control-sm" placeholder="0.00"
                                                               value={crediForm.aceites}
                                                               disabled={importeAceites <= 0}
                                                               onChange={(e) => setCrediForm({...crediForm, aceites: e.target.value})}/>
                                                    </div>
                                                </div>
                                                {form.creditos.length > 0 && (
                                                    <table className="table table-sm table-bordered mt-2 mb-0">
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
                                                        {form.creditos.map((c, idx) => (
                                                            <tr key={idx}>
                                                                <td>{c.fecha}</td>
                                                                <td>{c.clienteNombre}</td>
                                                                <td>{c.tipoCombustible}</td>
                                                                <td className="text-end">{c.litros}</td>
                                                                <td className="text-end">{dinero(c.importe)}</td>
                                                                <td className="text-end">{c.aceites ? dinero(c.aceites) : '—'}</td>
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
                                                            <th className="text-end">{dinero(form.creditos.reduce((s, c) => s + (Number(c.importe) || 0), 0))}</th>
                                                            <th className="text-end">{dinero(form.creditos.reduce((s, c) => s + (Number(c.aceites) || 0), 0))}</th>
                                                            <th className="text-end fw-bold">{dinero(form.creditos.reduce((s, c) => s + (Number(c.importe) || 0) + (Number(c.aceites) || 0), 0))}</th>
                                                            <th/>
                                                        </tr>
                                                        </tfoot>
                                                    </table>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="row g-2 mb-2">
                                    <div className="col-12">
                                        <div className="card" style={{padding: 0}}>
                                            <div className="card-body py-1">
                                                <h6 className="mb-1">💵 Esperado</h6>
                                                <div className="row g-4">
                                                <div className="col-auto">
                                                <table className="table table-sm table-bordered mb-0">
                                                    <tbody>
                                                    <tr>
                                                        <td>Efectivo vendido (bruto combustible)</td>
                                                        <td className="text-end">{dinero(resumen.esperadoEfectivo)}</td>
                                                    </tr>
                                                    <tr>
                                                        <td>Aceites vendidos en isla</td>
                                                        <td className="text-end">{dinero(importeAceites)}</td>
                                                    </tr>
                                                    {cargosConcepto > 0 && (
                                                        <tr>
                                                            <td>Cargos a crédito de cliente</td>
                                                            <td className="text-end text-danger">− {dinero(cargosConcepto)}</td>
                                                        </tr>
                                                    )}
                                                    {tarjetaRecibido > 0 && (
                                                        <tr>
                                                            <td>Tarjeta recibida (declarada)</td>
                                                            <td className="text-end text-danger">− {dinero(tarjetaRecibido)}</td>
                                                        </tr>
                                                    )}
                                                    {transferenciaRecibido > 0 && (
                                                        <tr>
                                                            <td>Transferencia recibida (declarada)</td>
                                                            <td className="text-end text-danger">− {dinero(transferenciaRecibido)}</td>
                                                        </tr>
                                                    )}
                                                    <tr className="table-primary">
                                                        <td><strong>EFECTIVO ESPERADO (neto)</strong></td>
                                                        <td className="text-end"><strong>{dinero(netoEfectivo)}</strong></td>
                                                    </tr>
                                                    <tr>
                                                        <td>Esperado tarjeta</td>
                                                        <td className="text-end">{dinero(resumen.esperadoTarjeta)}</td>
                                                    </tr>
                                                    <tr>
                                                        <td>Esperado transferencia</td>
                                                        <td className="text-end">{dinero(resumen.esperadoTransferencia)}</td>
                                                    </tr>
                                                    <tr>
                                                        <td>Esperado crédito</td>
                                                        <td className="text-end">{dinero(resumen.esperadoCredito)}</td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                                </div>
                                                <div className="col">
                                                    <div className="row g-3">
                                                        <div className="col-md-4">
                                                            <label className="form-label">Efectivo que ENTREGA ($)</label>
                                                            <input type="number" step="0.01" className="form-control" name="efectivoEntregado" value={form.efectivoEntregado} onChange={handleChange} placeholder="0.00"/>
                                                        </div>
                                                        <div className="col-md-4">
                                                            <label className="form-label">Tarjeta recibida ($)</label>
                                                            <input type="number" step="0.01" className="form-control" name="tarjetaRecibido" value={form.tarjetaRecibido} onChange={handleChange} placeholder="0.00"/>
                                                        </div>
                                                        <div className="col-md-4">
                                                            <label className="form-label">Transferencia recibida ($)</label>
                                                            <input type="number" step="0.01" className="form-control" name="transferenciaRecibido" value={form.transferenciaRecibido} onChange={handleChange} placeholder="0.00"/>
                                                        </div>
                                                    </div>
                                                    <div className="mt-3 p-3 border rounded" style={{maxWidth: '560px', background: '#f8f9fa'}}>
                                                        <div className="d-flex justify-content-between">
                                                            <span>Efectivo esperado (neto, descontando tarjeta/transfer)</span>
                                                            <strong>{dinero(netoEfectivo)}</strong>
                                                        </div>
                                                        <div className="d-flex justify-content-between">
                                                            <span>Efectivo entregado</span>
                                                            <strong>{dinero(efectivoEntregado)}</strong>
                                                        </div>
                                                        <hr/>
                                                        <div className="d-flex justify-content-between align-items-center">
                                                            <span><strong>Resultado del corte</strong></span>
                                                            {diferencia > 0 ? (
                                                                <span className="badge badge-danger fs-6">FALTANTE ▲ {dinero(diferencia)}</span>
                                                            ) : diferencia < 0 ? (
                                                                <span className="badge badge-success fs-6">SOBRANTE ▼ {dinero(Math.abs(diferencia))}</span>
                                                            ) : (
                                                                <span className="badge badge-secondary fs-6">CUADRADO {dinero(0)}</span>
                                                            )}
                                                        </div>
                                                    </div>
                                                </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="col-12 mb-3">
                                    <label className="form-label">Observaciones</label>
                                    <input className="form-control" name="observaciones" value={form.observaciones} onChange={handleChange} placeholder="Ej. faltante de efectivo"/>
                                </div>
                            </div>
                        )}

                        <div className="d-flex gap-2">
                            <button type="submit" className="btn btn-success">Generar Corte</button>
                            <button type="button" className="btn btn-secondary" onClick={() => setMostrarForm(false)}>Cancelar</button>
                        </div>
                    </form>
                </div>
            )}

            {corteModalEdicion && (
                <EditarCorteModal
                    corte={corteModalEdicion}
                    clientesCredito={clientesCredito}
                    onCerrar={() => { setCorteModalEdicion(null); cargar(''); }}
                />
            )}

            <EstadoCarga cargando={loading}>
                <div className="table-compact">
                    <TablaDinamica
                        columnas={columnas}
                        datos={cortes}
                        acciones={(corte) => (
                            <div className="d-flex gap-1">
                                {corte.estado === 'PENDIENTE' && (
                                    <>
                                        <button className="btn btn-outline-primary btn-sm" onClick={() => handleEditar(corte)}>✏️ Editar</button>
                                        <button className="btn btn-info btn-sm" onClick={() => handleValidar(corte)}>✔ Validar</button>
                                    </>
                                )}
                                {corte.estado === 'VALIDADO' && (
                                    <button className="btn btn-warning btn-sm" onClick={() => handleCerrar(corte)}>🔒 Cerrar</button>
                                )}
                            </div>
                        )}
                    />
                </div>
            </EstadoCarga>
        </div>
    );
};
export default CortesLista;