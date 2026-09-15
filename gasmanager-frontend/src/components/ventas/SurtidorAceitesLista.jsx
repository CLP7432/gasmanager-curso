import React, {useEffect, useState} from "react";
import {surtidoresAceiteService, dispensariosService, turnosService} from "../../api/ventas/auth.js";
import {aceitesService} from "../../api/inventarios/auth.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";

const SurtidorAceitesLista = () => {
    const [surtidores, setSurtidores] = useState([]);
    const [selectId, setSelectId] = useState('');
    const [stock, setStock] = useState([]);
    const [surtidor, setSurtidor] = useState(null);
    const [cargando, setCargando] = useState(false);
    const [mostrarNuevo, setMostrarNuevo] = useState(false);
    const [aceites, setAceites] = useState([]);
    const [turnos, setTurnos] = useState([]);
    const [turnoId, setTurnoId] = useState('');

    const [form, setForm] = useState({dispensarioId: '', nombre: ''});
    const [items, setItems] = useState([]);
    const [dispensarios, setDispensarios] = useState([]);

    const cargarTodos = async () => {
        setSurtidores(await surtidoresAceiteService.listar());
    };

    const cargarAceites = async () => {
        setAceites(await aceitesService.listarActivos().catch(() => []));
    };

    useEffect(() => {
        const inicial = async () => {
            const [listaSurt, listaAceites, listaTurnos, listaDispensarios] = await Promise.all([
                surtidoresAceiteService.listar(),
                aceitesService.listarActivos().catch(() => []),
                turnosService.listar(),
                dispensariosService.listarCompletos().catch(() => [])
            ]);
            setSurtidores(listaSurt);
            setAceites(listaAceites);
            setTurnos(listaTurnos);
            setDispensarios(listaDispensarios);
            const activo = listaTurnos.find(t => t.estado === 'ABIERTO');
            setTurnoId(activo ? String(activo.id) : '');
        };
        inicial();
    }, []);

    const cargarStock = async (id) => {
        setCargando(true);
        try {
            const [det, stk] = await Promise.all([
                surtidoresAceiteService.obtener(id),
                surtidoresAceiteService.stock(id)
            ]);
            setSurtidor(det);
            setStock(stk);
            setSelectId(String(id));
        } finally {
            setCargando(false);
        }
    };

    const handleSelect = (e) => {
        const id = e.target.value;
        if (id) cargarStock(id);
        else { setSelectId(''); setSurtidor(null); setStock([]); }
    };

    const handleEntregar = async (aceiteId, cantidad) => {
        const n = Number(cantidad);
        if (!n || n <= 0) { alert('Cantidad inválida'); return; }
        try {
            await surtidoresAceiteService.entregar({
                surtidorAceiteId: surtidor.id,
                aceiteId: Number(aceiteId),
                cantidad: n,
                turnoId: turnoId ? Number(turnoId) : null
            });
            alert('Aceite entregado a la isla');
            cargarStock(surtidor.id);
            cargarTodos();
            cargarAceites();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al entregar aceite');
        }
    };

    const handleNuevo = () => {
        setForm({dispensarioId: '', nombre: ''});
        setItems((aceites || []).filter(a => Number(a.stockActual || 0) > 0).map(a => ({aceiteId: a.id, stockActual: 10})));
        setMostrarNuevo(true);
    };

    const handleSelectDispensario = (id) => {
        const disp = (dispensarios || []).find(d => String(d.id) === String(id));
        setForm({
            dispensarioId: id,
            nombre: disp ? `Aceites ${disp.nombre}` : ''
        });
    };

    const handleFormChange = (e) => setForm({...form, [e.target.name]: e.target.value});

    const handleGuardar = async (e) => {
        e.preventDefault();
        const disp = (dispensarios || []).find(d => String(d.id) === String(form.dispensarioId));
        if (!form.dispensarioId || !disp) { alert('Selecciona el dispensario de gasolina (isla) al que se asignará el dispensario de aceites'); return; }
        if (items.length === 0) { alert('No hay aceites con inventario en bodega para surtir'); return; }
        try {
            // El surtidor de aceites pertenece a la isla, sin despachador:
            // el despachador solo se liga a la isla al abrir el turno.
            const creado = await surtidoresAceiteService.guardar({
                dispensarioId: Number(disp.id),
                nombre: form.nombre.trim() || `Aceites ${disp.nombre}`,
                items: items.map(i => ({aceiteId: Number(i.aceiteId), stockActual: Number(i.stockActual) || 0}))
            });
            alert('Dispensario de aceites creado');
            setMostrarNuevo(false);
        } catch (error) {
            const data = error.response?.data;
            const detalle = typeof data === 'string' ? data : (data?.message || JSON.stringify(data || {}));
            console.error('Crear surtidor:', error.message, '| code:', error.code, '| respondio:', !!error.response, '| enviado:', !!error.request);
            alert(`Error al crear el surtidor (${error.response?.status || error.code || error.message || 'sin respuesta'}): ${detalle || 'sin detalle'}`);
            return;
        }
        // Refresco fuera del try: si falla no debe parecer que no se guardó
        try {
            await cargarTodos();
            const lista = await surtidoresAceiteService.listar();
            const mio = lista.find(s => String(s.dispensarioId) === String(disp.id));
            if (mio) cargarStock(mio.id);
        } catch {
            cargarTodos().catch(() => {});
        }
    };

    const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

    return (
        <div>
            <PageHeader
                titulo="Surtidor de Aceites"
                subtitulo="Control de aceites por isla: entregas desde bodega, stock y alertas"
                mostrarAccion
                etiquetaAccion="+ Crear Surtidor"
                onAccion={handleNuevo}
            />

            <div className="row g-3 mb-3">
                <div className="col-md-6">
                    <label className="form-label">Surtidor de aceites</label>
                    <select className="form-select" value={selectId} onChange={handleSelect}>
                        <option value="">Seleccionar...</option>
                        {surtidores.map(s => (
                            <option key={s.id} value={s.id}>{s.nombre || 'Surtidor'}</option>
                        ))}
                    </select>
                </div>
            </div>

            {mostrarNuevo && (
                <div className="card" style={{padding: '1rem', marginBottom: '1rem'}}>
                    <h4>Nuevo Dispensario de Aceites</h4>
                    <form onSubmit={handleGuardar}>
                        <div className="row g-3">
                            <div className="col-md-6">
                                <label className="form-label">Dispensario de gasolina (isla) *</label>
                                <select className="form-select" value={form.dispensarioId} onChange={(e) => handleSelectDispensario(e.target.value)}>
                                    <option value="">Seleccionar dispensario...</option>
                                    {dispensarios.filter(d => d.activo && !(surtidores || []).some(s => String(s.dispensarioId) === String(d.id))).map(d => (
                                        <option key={d.id} value={d.id}>Dispensario {d.numero} — {d.nombre} · Isla {d.ubicacion || 'sin asignar'}</option>
                                    ))}
                                </select>
                                {dispensarios.filter(d => d.activo).length > 0 &&
                                 dispensarios.filter(d => d.activo && !(surtidores || []).some(s => String(s.dispensarioId) === String(d.id))).length === 0 && (
                                    <small className="text-muted">Todas las islas ya tienen su dispensario de aceites.</small>
                                )}
                            </div>
                            <div className="col-md-6">
                                <label className="form-label">Nombre del dispensario de aceites</label>
                                <input className="form-control" name="nombre" value={form.nombre} onChange={handleFormChange}
                                       placeholder="Aceites ..."/>
                            </div>
                        </div>
                        <h6 className="mt-3">Productos (automático)</h6>
                        <p className="text-muted small mb-2">
                            Se agregan de jalón todos los aceites con inventario en bodega, 10 piezas de cada uno.
                        </p>
                        {items.length === 0 ? (
                            <div className="alert alert-warning py-2">
                                No hay aceites con inventario en bodega. Registra inventario de aceites antes de crear el dispensario de aceites.
                            </div>
                        ) : (
                            <table className="table table-sm table-bordered">
                                <thead>
                                <tr>
                                    <th>Aceite</th>
                                    <th className="text-end">Piezas iniciales</th>
                                </tr>
                                </thead>
                                <tbody>
                                {items.map((it, idx) => {
                                    const a = (aceites || []).find(x => String(x.id) === String(it.aceiteId));
                                    return (
                                        <tr key={idx}>
                                            <td>
                                                {a?.nombre || it.aceiteId}
                                                <small className="text-muted"> (bodega: {Number(a?.stockActual || 0)} pzas)</small>
                                            </td>
                                            <td className="text-end">{it.stockActual}</td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        )}
                        <div className="d-flex gap-2 mt-3">
                            <button type="submit" className="btn btn-success" disabled={items.length === 0}>Guardar</button>
                            <button type="button" className="btn btn-secondary" onClick={() => setMostrarNuevo(false)}>Cancelar</button>
                        </div>
                    </form>
                </div>
            )}

            <EstadoCarga cargando={cargando}>
                {surtidor && (
                    <div className="card" style={{padding: '1rem'}}>
                        <h5 className="mb-2">Stock — {surtidor.nombre}</h5>
                        <div className="row g-3 mb-3">
                            <div className="col-md-6">
                                <label className="form-label">Turno para las entregas</label>
                                <select className="form-select" value={turnoId} onChange={(e) => setTurnoId(e.target.value)}>
                                    <option value="">Sin turno</option>
                                    {turnos.map(t => (
                                        <option key={t.id} value={t.id}>{t.codigoTurno} — {t.nombre} — {t.estado}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                        <table className="table table-bordered">
                            <thead>
                            <tr>
                                <th>Producto</th>
                                <th>Categoría</th>
                                <th className="text-end">Stock en isla</th>
                                <th className="text-end">Precio venta</th>
                                <th>Alerta</th>
                                <th style={{width: '220px'}}>Entregar desde bodega</th>
                            </tr>
                            </thead>
                            <tbody>
                            {stock.map(s => (
                                <tr key={s.aceiteId}>
                                    <td>{s.aceiteNombre}</td>
                                    <td>{s.categoria || '-'}</td>
                                    <td className="text-end">
                                        <span className={s.alerta ? 'text-danger fw-bold' : ''}>{Number(s.stockActual).toFixed(2)}</span>
                                    </td>
                                    <td className="text-end">{dinero(s.precioVenta)}</td>
                                    <td>{s.alerta
                                        ? <span className="badge badge-danger">{s.mensaje}</span>
                                        : <span className="badge badge-success">OK</span>}
                                    </td>
                                    <td>
                                        <EntregaFila aceiteId={s.aceiteId} onEntregar={handleEntregar}/>
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                )}
                {!surtidor && !cargando && (
                    <div className="card" style={{padding: '1rem'}}>
                        <p className="text-muted mb-0">Selecciona un surtidor de aceites para ver su inventario.</p>
                    </div>
                )}
            </EstadoCarga>
        </div>
    );
};

const EntregaFila = ({aceiteId, onEntregar}) => {
    const [cantidad, setCantidad] = useState('');
    return (
        <div className="d-flex gap-2">
            <input type="number" className="form-control form-control-sm" placeholder="Cantidad" value={cantidad}
                   onChange={(e) => setCantidad(e.target.value)}/>
            <button type="button" className="btn btn-sm btn-primary" onClick={() => onEntregar(aceiteId, cantidad)}>Entregar</button>
        </div>
    );
};

export default SurtidorAceitesLista;
