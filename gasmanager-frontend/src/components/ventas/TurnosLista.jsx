import React, {useEffect, useState} from "react";
import {turnosService, dispensariosService, surtidoresAceiteService} from "../../api/ventas/auth.js";
import {empleadosService} from "../../api/nomina/auth.js";
import {tanquesService, aceitesService} from "../../api/inventarios/auth.js";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {idCol, columna} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";

const badgeEstado = (estado) => estado === 'ABIERTO'
    ? <span className="badge badge-success">ABIERTO</span>
    : <span className="badge badge-secondary">CERRADO</span>;

const hoy = () => new Date().toISOString().slice(0, 10);
const nombreCompleto = (emp) => (emp.nombreCompleto || `${emp.nombre} ${emp.apellidoPaterno}`.trim());

const siguienteNumero = (turnos) => {
    const max = (turnos || []).reduce((acc, t) => {
        const n = parseInt((t.nombre || '').replace(/\D/g, ''), 10);
        return isNaN(n) ? acc : Math.max(acc, n);
    }, 0);
    return max + 1;
};

const TurnosLista = () => {
    const {isAdmin, user} = useAuth();
    const [turnos, setTurnos] = useState([]);
    const [loading, setLoading] = useState(true);
    const [estadoFiltro, setEstadoFiltro] = useState('');
    const [mostrarForm, setMostrarForm] = useState(false);
    const [form, setForm] = useState({nombre: '', fechaTurno: hoy()});
    const [asignaciones, setAsignaciones] = useState({});
    const [rellenarPlan, setRellenarPlan] = useState({});
    const {datos: despachadores} = useLista(empleadosService, 'listarDespachadores');
    const {datos: dispensarios} = useLista(dispensariosService, 'listarCompletos');
    const {datos: tanques} = useLista(tanquesService, 'listarActivos');
    const {datos: aceites, cargar: cargarAceites} = useLista(aceitesService, 'listarActivos');
    const {datos: surtidores, cargar: cargarSurtidores} = useLista(surtidoresAceiteService, 'listar');

    const sinInventarioCombustible = !(tanques || []).some(t => Number(t.stockLitros || 0) > 0);
    const sinInventarioAceites = !(aceites || []).some(a => Number(a.stockActual || 0) > 0);
    const sinInventario = sinInventarioCombustible || sinInventarioAceites;

    const cargar = async (estado) => {
        setLoading(true);
        try {
            setTurnos(await turnosService.listar(estado || undefined));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { cargar(estadoFiltro); }, [estadoFiltro]);

    const handleNuevo = async () => {
        if (sinInventario) {
            alert('No existen inventarios de combustible ni de aceites disponibles. Carga combustible en los tanques y registra inventario de aceites antes de abrir un turno.');
            return;
        }
        let numero = 1;
        const todos = await turnosService.listar().catch(() => null);
        if (todos) numero = siguienteNumero(todos);
        const inicial = {};
        (dispensarios || [])
            .filter(d => d.activo && d.despachadorId)
            .forEach(d => { inicial[d.id] = d.despachadorId; });
        setAsignaciones(inicial);
        setRellenarPlan({});
        setForm({nombre: `Turno ${String(numero).padStart(4, '0')}`, fechaTurno: hoy()});
        setMostrarForm(true);
    };

    const handleChange = (e) => setForm({...form, [e.target.name]: e.target.value});

    const handleAbrir = async (e) => {
        e.preventDefault();
        if (!form.nombre.trim()) { alert('Escribe un nombre para el turno'); return; }
        if (sinInventario) {
            alert('No existen inventarios de combustible ni de aceites disponibles. Carga combustible en los tanques y registra inventario de aceites antes de abrir un turno.');
            return;
        }
        const activos = dispensarios.filter(d => d.activo);
        if (despachadores.length === 0) {
            alert('No hay despachadores dados de alta. Registra al menos un empleado con el puesto "Despachador" en Nómina antes de abrir un turno.');
            return;
        }
        for (const d of activos) {
            if (!asignaciones[d.id]) {
                alert(`Debes asignar un despachador al dispensario ${d.nombre} antes de abrir el turno.`);
                return;
            }
        }
        const bodega = new Map((aceites || []).map(a => [String(a.id), Number(a.stockActual || 0)]));
        const activosAceites = (dispensarios || []).filter(d => d.activo);
        for (const d of activosAceites) {
            const surtidor = (surtidores || []).find(s => String(s.dispensarioId) === String(d.id));
            if (!surtidor || surtidor.items.length === 0) {
                alert(`El dispensario ${d.numero} (${d.nombre}) no tiene un dispensario de aceites asignado. Regístralo en "Surtidor de Aceites" antes de abrir el turno.`);
                return;
            }
            const pendientes = surtidor.items
                .map(it => ({...it, faltante: Math.max(0, 10 - Number(it.stockActual || 0))}))
                .filter(x => x.faltante > 0);
            if (pendientes.length === 0) continue;
            if (!rellenarPlan[d.id]) {
                alert(`El dispensario de aceites de ${d.numero} (${d.nombre}) aún no inicia lleno. Pulsa "➕ Agregar / Rellenar aceites" para marcarlo: se rellenará a 10 piezas al guardar el turno.`);
                return;
            }
            for (const it of pendientes) {
                const disponible = bodega.get(String(it.aceiteId)) || 0;
                if (it.faltante > disponible) {
                    alert(`Bodega insuficiente para llenar "${it.aceiteNombre}" en ${d.nombre}: faltan ${it.faltante} y solo hay ${disponible} en bodega.`);
                    return;
                }
            }
        }
        try {
            const turnoCreado = await turnosService.crear({
                nombre: form.nombre.trim(),
                fechaTurno: form.fechaTurno,
                supervisorId: user?.idUsuario,
                supervisorNombre: user?.correo
            });
            for (const d of activos) {
                const empleado = despachadores.find(emp => String(emp.id) === String(asignaciones[d.id]));
                if (empleado) {
                    await dispensariosService.asignarDespachador(d.id, empleado.id, nombreCompleto(empleado));
                }
            }
            for (const d of activosAceites) {
                if (!rellenarPlan[d.id]) continue;
                const surtidor = (surtidores || []).find(s => String(s.dispensarioId) === String(d.id));
                const pendientes = surtidor.items
                    .map(it => ({...it, faltante: Math.max(0, 10 - Number(it.stockActual || 0))}))
                    .filter(x => x.faltante > 0);
                for (const it of pendientes) {
                    await surtidoresAceiteService.entregar({
                        surtidorAceiteId: surtidor.id,
                        aceiteId: it.aceiteId,
                        cantidad: it.faltante,
                        turnoId: turnoCreado.id,
                        usuarioId: user?.idUsuario
                    });
                }
            }
            await cargarSurtidores();
            await cargarAceites();
            alert('Turno abierto, despachadores asignados. Dispensarios de aceites con 10 piezas por producto.');
            setMostrarForm(false);
            setRellenarPlan({});
            cargar('');
        } catch (error) {
            alert(error.response?.data?.message || 'Error al abrir el turno');
        }
    };

    const marcarRellenar = (disp) => {
        const surtidor = (surtidores || []).find(s => String(s.dispensarioId) === String(disp.id));
        if (!surtidor || surtidor.items.length === 0) return;
        const bodega = new Map((aceites || []).map(a => [String(a.id), Number(a.stockActual || 0)]));
        const pendientes = surtidor.items
            .map(it => ({...it, faltante: Math.max(0, 10 - Number(it.stockActual || 0))}))
            .filter(x => x.faltante > 0);
        if (pendientes.length === 0) return;
        for (const it of pendientes) {
            const disponible = bodega.get(String(it.aceiteId)) || 0;
            if (it.faltante > disponible) {
                alert(`Bodega insuficiente para llenar "${it.aceiteNombre}" en ${disp.nombre}: faltan ${it.faltante} y solo hay ${disponible} en bodega.`);
                return;
            }
        }
        setRellenarPlan(prev => ({...prev, [disp.id]: true}));
    };

    const desmarcarRellenar = (disp) => {
        setRellenarPlan(prev => {
            const nuevo = {...prev};
            delete nuevo[disp.id];
            return nuevo;
        });
    };

    const handleCerrar = async (turno) => {
        if (window.confirm(`¿Cerrar el turno ${turno.codigoTurno}?`)) {
            try {
                await turnosService.cerrar(turno.id);
                cargar('');
            } catch (error) {
                alert(error.response?.data?.message || 'Error al cerrar el turno');
            }
        }
    };
    const columnas = [
        idCol,
        columna('codigoTurno', 'Código'),
        columna('nombre', 'Nombre'),
        columna('fechaTurno', 'Fecha'),
        columna('horaInicio', 'Inicio', (t) => (t.horaInicio || '').slice(0, 5) || '-'),
        columna('horaFin', 'Fin', (t) => (t.horaFin || '').slice(0, 5) || '-'),
        columna('supervisorNombre', 'Supervisor'),
        {
            key: 'estado',
            label: 'Estado',
            render: (t) => badgeEstado(t.estado)
        }
    ];

    return (
        <div>
            <PageHeader
                titulo="Turnos de Trabajo"
                subtitulo="Un solo turno ABIERTO a la vez"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Abrir Turno"
                onAccion={handleNuevo}
            />
            <div className="btn-group mb-3">
                {['', 'ABIERTO', 'CERRADO'].map(estado => (
                    <button key={estado || 'todos'}
                            className={`btn btn-sm ${estadoFiltro === estado ? 'btn-primary' : 'btn-outline-primary'}`}
                            onClick={() => setEstadoFiltro(estado)}>
                        {estado || 'Todos'}
                    </button>
                ))}
            </div>
            {sinInventario && (
                <div className="alert alert-danger">
                    🚫 <strong>No existen inventarios disponibles.</strong> No se puede abrir un turno hasta que se cargue combustible en los tanques y haya inventario de aceites (módulo de Inventarios).
                </div>
            )}
            {mostrarForm && isAdmin && (
                <div className="card" style={{maxWidth: '880px', padding: '1rem'}}>
                    <h4>Abrir Turno</h4>
                    <form onSubmit={handleAbrir}>
                        <div className="row g-3">
                            <div className="col-md-7">
                                <label className="form-label">Nombre (automático)</label>
                                <input className="form-control" name="nombre" value={form.nombre} readOnly
                                       style={{backgroundColor: '#f5f5f5'}}/>
                                <small className="text-muted">Conteo de turno: se asigna solo y no permite repetirse.</small>
                            </div>
                            <div className="col-md-5">
                                <label className="form-label">Fecha</label>
                                <input type="date" className="form-control" name="fechaTurno" value={form.fechaTurno}
                                       onChange={handleChange}/>
                            </div>
                            <div className="col-12">
                                <hr style={{margin: '0 0 12px 0'}}/>
                                <label className="form-label">Asignar despachador por dispensario</label>
                                {despachadores.length === 0 && (
                                    <div className="alert alert-warning">
                                        ⚠️ No hay despachadores dados de alta. Registra empleados con el puesto
                                        <strong> "Despachador"</strong> en Nómina para poder abrir un turno.
                                    </div>
                                )}
                                {dispensarios.filter(d => d.activo).length === 0 ? (
                                    <div className="alert alert-info py-2">No hay dispensarios activos registrados.</div>
                                ) : (
                                    dispensarios.filter(d => d.activo).map(d => (
                                        <div key={d.id}
                                             className="d-flex align-items-center justify-content-between gap-2 mb-2">
                                            <div>
                                                <strong>{d.nombre}</strong>
                                                <small className="text-muted"> · Núm. {d.numero}</small>
                                            </div>
                                            <select className="form-select form-select-sm" style={{width: '260px'}}
                                                    value={asignaciones[d.id] ?? ''}
                                                    onChange={(e) => setAsignaciones({...asignaciones, [d.id]: e.target.value})}>
                                                <option value="">Sin asignar</option>
                                                {despachadores.map(emp => (
                                                    <option key={emp.id} value={emp.id}>{nombreCompleto(emp)}</option>
                                                ))}
                                            </select>
                                        </div>
                                    ))
                                )}
                            </div>
                            <div className="col-12">
                                <hr style={{margin: '0 0 12px 0'}}/>
                                <label className="form-label">🛢️ Surtir aceites en los dispensarios de aceites (antes de abrir)</label>
                                <small className="text-muted d-block mb-2">
                                    Cada dispensario inicia operaciones con su dispensario de aceites lleno: 10 piezas de cada
                                    producto. Si el turno anterior dejó existencias, solo se rellena lo faltante.
                                </small>
                                {dispensarios.filter(d => d.activo).length === 0 ? (
                                    <div className="alert alert-info py-2">No hay dispensarios registrados.</div>
                                ) : (
                                    dispensarios.filter(d => d.activo).map(d => {
                                        const surtidor = (surtidores || []).find(s => String(s.dispensarioId) === String(d.id));
                                        if (!surtidor || surtidor.items.length === 0) {
                                            return (
                                                <div key={d.id} className="card mb-2" style={{padding: '0.6rem'}}>
                                                    <div className="d-flex justify-content-between align-items-center">
                                                        <strong>⛽ Dispensario {d.numero} — {d.nombre}</strong>
                                                        <small className="text-muted">Isla {d.ubicacion || 'sin asignar'}</small>
                                                    </div>
                                                    <small className="text-muted">
                                                        Sin dispensario de aceites asignado. Regístralo en <strong>"Surtidor de Aceites"</strong>.
                                                    </small>
                                                </div>
                                            );
                                        }
                                        const todosLlenos = surtidor.items.every(it => Number(it.stockActual || 0) >= 10);
                                        return (
                                            <div key={d.id} className="card mb-2" style={{padding: '0.6rem'}}>
                                                <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-1">
                                                    <div>
                                                        <strong>⛽ Dispensario {d.numero} — {d.nombre}</strong>
                                                        <small className="text-muted d-block">Isla {d.ubicacion || 'sin asignar'} · Despachador: {surtidor.despachadorNombre || 'sin asignar'}</small>
                                                    </div>
                                                    {todosLlenos ? (
                                                        <span className="badge bg-success">🛢️ Dispensario de aceites lleno (10 pzas)</span>
                                                    ) : rellenarPlan[d.id] ? (
                                                        <span className="badge bg-warning text-dark">
                                                            ⏳ Pendiente: se rellenará a 10 pzas al abrir turno
                                                            <button type="button" className="btn btn-sm btn-outline-secondary ms-2"
                                                                    onClick={() => desmarcarRellenar(d)}>Desmarcar</button>
                                                        </span>
                                                    ) : (
                                                        <button type="button" className="btn btn-sm btn-primary"
                                                                onClick={() => marcarRellenar(d)}>
                                                            ➕ Agregar / Rellenar aceites
                                                        </button>
                                                    )}
                                                </div>
                                                <table className="table table-sm table-bordered mb-0" style={{fontSize: '0.85em'}}>
                                                    <thead>
                                                    <tr>
                                                        <th>Aceite</th>
                                                        <th className="text-end">Existe en dispensario</th>
                                                        <th className="text-end">Falta para 10</th>
                                                        <th className="text-end">Bodega</th>
                                                    </tr>
                                                    </thead>
                                                    <tbody>
                                                    {surtidor.items.map(it => {
                                                        const existente = Number(it.stockActual || 0);
                                                        const faltante = Math.max(0, 10 - existente);
                                                        const bodegaStock = Number((aceites || []).find(a => String(a.id) === String(it.aceiteId))?.stockActual || 0);
                                                        return (
                                                            <tr key={it.id}>
                                                                <td>{it.aceiteNombre}</td>
                                                                <td className="text-end">{existente}</td>
                                                                <td className="text-end">
                                                                    {faltante > 0
                                                                        ? <span className="badge bg-warning text-dark">{faltante}</span>
                                                                        : <span className="badge bg-success">Lleno</span>}
                                                                </td>
                                                                <td className="text-end">{bodegaStock}</td>
                                                            </tr>
                                                        );
                                                    })}
                                                    </tbody>
                                                </table>
                                            </div>
                                        );
                                    })
                                )}
                            </div>
                        </div>
                        <div className="d-flex gap-2 mt-3">
                            <button type="submit" className="btn btn-success" disabled={sinInventario}>🚀 Abrir Turno</button>
                            <button type="button" className="btn btn-secondary" onClick={() => {setMostrarForm(false); setRellenarPlan({});}}>Cancelar</button>
                        </div>
                    </form>
                </div>
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={turnos}
                    acciones={(turno) => (
                        turno.estado === 'ABIERTO' ? (
                            <button className="btn btn-warning btn-sm" onClick={() => handleCerrar(turno)}>🔒 Cerrar</button>
                        ) : null
                    )}
                />
            </EstadoCarga>
        </div>
    );
};
export default TurnosLista;