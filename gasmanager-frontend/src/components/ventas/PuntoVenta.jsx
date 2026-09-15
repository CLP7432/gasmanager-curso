import React, {useEffect, useRef, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useLista} from "../../kernel/hooks/useLista.js";
import {ventasService, turnosService, dispensariosService, cortesService} from "../../api/ventas/auth.js";
import {combustiblesService, tanquesService} from "../../api/inventarios/auth.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import CardBase from "../../kernel/components/CardBase.jsx";
import TicketVenta from "./TicketVenta.jsx";
import {lealtadService} from "../../api/lealtad/auth.js";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;
// Litros con 4 decimales al guardar (la columna lo soporta): en pesos el
// importe queda exacto; en pantalla se muestran 3 como la bomba.
const r3 = (v) => Math.round((Number(v) || 0) * 1000) / 1000;
const r4 = (v) => Math.round((Number(v) || 0) * 10000) / 10000;

// Colores por grado como en islas reales (Pemex): Magna verde, Premium rojo, Diésel negro
const colorGrado = (tipo) => {    const t = String(tipo || '').toUpperCase();
    if (t.includes('MAGNA')) return '#178a4c';
    if (t.includes('PREMIUM')) return '#c0392b';
    if (t.includes('DIESEL') || t.includes('DIÉSEL')) return '#2c3e50';
    return '#6c757d';
};

const estiloLCD = {
    background: '#0a0f0a', color: '#7CFC00',
    fontFamily: "'Courier New', monospace", borderRadius: '6px', padding: '4px 8px'
};

const PuntoVenta = () => {
    const navigate = useNavigate();
    const {datos: dispensarios, loading: cargandoD} = useLista(dispensariosService, 'listarCompletos');
    const {datos: combustibles, loading: cargandoC} = useLista(combustiblesService, 'listarActivos');
    const {datos: tanques, loading: cargandoT} = useLista(tanquesService, 'listarActivos');

    const [turno, setTurno] = useState(null);
    const [turnoLoaded, setTurnoLoaded] = useState(false);
    const [ventasTurno, setVentasTurno] = useState([]);
    const [cortesTurno, setCortesTurno] = useState([]);
    
    const [controles, setControles] = useState({});
    const [ultimaVenta, setUltimaVenta] = useState(null);
    const [puntosUltima, setPuntosUltima] = useState(null);
    const timer = useRef(null);

    const setControl = (mangueraId, cambios) =>
        setControles(prev => ({
            ...prev,
            [mangueraId]: {...(prev[mangueraId] || {modo: 'litros', valor: ''}), ...cambios}
        }));

    const cargarTurno = async () => {
        try {
            const abiertos = await turnosService.listar('ABIERTO');
            const activo = abiertos[0] || null;
            setTurno(activo);
            if (activo) {
                setVentasTurno(await ventasService.listarPorTurno(activo.id));
                setCortesTurno(await cortesService.listarPorTurno(activo.id).catch(() => []));
            } else {
                setCortesTurno([]);
            }
        } finally {
            setTurnoLoaded(true);
        }
    };

    useEffect(() => {
        cargarTurno();
    }, []);
    useEffect(() => () => clearInterval(timer.current), []);

    const resumen = ventasTurno.reduce((acc, v) => {
        if (v.estado === 'CANCELADA') return acc;
        acc.numero += 1;
        acc.litros += Number(v.detalles?.[0]?.cantidad) || 0;
        acc.total += Number(v.total) || 0;
        return acc;
    }, {numero: 0, litros: 0, total: 0});

    const combustibleDe = (manguera) => combustibles.find(c => c.id === manguera.combustibleId);
    const tanqueDe = (manguera) => tanques.find(t => t.tipoCombustible === manguera.tipoCombustible);
    const dispensarioConCorte = (dispensarioId) =>
        (cortesTurno || []).some(c => String(c.dispensarioId) === String(dispensarioId));
    const iniciarCarga = (manguera, dispensario) => {
        const combustible = combustibleDe(manguera);
        const control = controles[manguera.id] || {modo: 'litros', valor: ''};
        const valor = Number(control.valor);
        if (!turno) {
            setControl(manguera.id, {error: 'No hay turno ABIERTO. Ábrelo en Turnos.'});
            return;
        }
        if (dispensarioConCorte(dispensario.id)) {
            setControl(manguera.id, {error: `Este dispensario ya tiene corte en ${turno.codigoTurno}. Cierra el turno antes de vender de nuevo.`});
            return;
        }
        if (!dispensario.despachadorId) {
            setControl(manguera.id, {error: 'Esta isla no tiene despachador asignado.'});
            return;
        }
        if (!combustible) {
            setControl(manguera.id, {error: 'La manguera no tiene combustible.'});
            return;
        }
        if (!valor || valor <= 0) {
            setControl(manguera.id, {error: 'Ingresa una cantidad mayor a 0.'});
            return;
        }
        const tanque = tanqueDe(manguera);
        if (!tanque) {
            setControl(manguera.id, {error: 'No hay tanque para ' + manguera.tipoCombustible + '.'});
            return;
        }

        const totalLitros = control.modo === 'litros'
            ? r4(valor)
            : r4(valor / combustible.precioActual);
        setUltimaVenta(null);
        setControl(manguera.id, {simulando: true, error: '', litros: 0, total: 0});

        const incremento = totalLitros / 40;
        let litrosActual = 0;
        clearInterval(timer.current);
        timer.current = setInterval(() => {
            litrosActual += incremento;
            if (litrosActual >= totalLitros) {
                litrosActual = totalLitros;
                clearInterval(timer.current);
                registrarVenta(manguera, dispensario, combustible, tanque, totalLitros);
            }
            setControl(manguera.id, {
                litros: litrosActual,
                total: litrosActual * combustible.precioActual,
                progreso: (litrosActual / totalLitros) * 100
            });
        }, 50);
    };

    const registrarVenta = async (manguera, dispensario, combustible, tanque, totalLitros) => {
        try {
            const venta = await ventasService.registrar({
                turnoId: turno.id,
                despachadorId: dispensario.despachadorId,
                despachadorNombre: dispensario.despachadorNombre,
                dispensarioId: dispensario.id,
                mangueraId: manguera.id,
                detalles: [{
                    tipoProducto: 'COMBUSTIBLE',
                    productoId: combustible.id,
                    productoNombre: combustible.nombre,
                    tanqueId: tanque.id,
                    cantidad: totalLitros,
                    precioUnitario: combustible.precioActual
                }]
            });
            setUltimaVenta(venta);
            // Puntos best-effort: si no hay programa activo da 0 y no falla la venta
            lealtadService.acumular(venta.id)
                .then(setPuntosUltima)
                .catch(() => setPuntosUltima(null));
            setVentasTurno(await ventasService.listarPorTurno(turno.id));
            setControl(manguera.id, {simulando: false, litros: 0, total: 0, progreso: 0, valor: '', error: ''});
        } catch (e) {
            setControl(manguera.id, {
                simulando: false,
                error: e.response?.data?.message || 'No se pudo registrar la venta'
            });
        }
    };

    const detenerCarga = (mangueraId) => {
        clearInterval(timer.current);
        setControl(mangueraId, {simulando: false, litros: 0, total: 0, progreso: 0});
    };

    const cerrarTurno = async () => {
        if (window.confirm('¿Cerrar el turno actual?')) {
            try {
                await turnosService.cerrar(turno.id);
                setTurno(null);
                setVentasTurno([]);
            } catch (e) {
                alert(e.response?.data?.message || 'Error al cerrar el turno');
            }
        }
    };

    const hayCarga = Object.values(controles).some(c => c && c.simulando);

    const mangueras = [];
    for (const d of dispensarios) {
        if (!d.activo) continue;
        for (const cara of d.caras || []) {
            for (const m of cara.mangueras || []) {
                if (m.activo && m.combustibleId) mangueras.push({manguera: m, dispensario: d});
            }
        }
    }
    const renderManguera = (manguera, dispensario, lado) => {
        const combustible = combustibleDe(manguera);
        const c = controles[manguera.id] || {modo: 'litros', valor: '', litros: 0, total: 0};
        const precio = combustible ? combustible.precioActual : 0;
        const bloqueado = dispensarioConCorte(dispensario.id);
        const litrosPreview = c.modo === 'pesos' ? (precio ? r4((Number(c.valor) || 0) / precio) : 0) : r4(Number(c.valor) || 0);
        const importePreview = litrosPreview * precio;
        return (
            <div key={manguera.id} style={{flex: '1 1 0', minWidth: '220px'}}>
                <div className="card h-100" style={{padding: '0.75rem', borderTop: `4px solid ${colorGrado(manguera.tipoCombustible)}`}}>
                    <div className="d-flex justify-content-between align-items-center">
                        <strong>🔫 {manguera.nombre}</strong>
                        <span className="badge" style={{background: colorGrado(manguera.tipoCombustible), color: '#fff'}}>{manguera.tipoCombustible}</span>
                    </div>
                    <div><small className="text-muted">Lado {lado || '—'}</small></div>
                    <div className="mt-1" style={{fontSize: '12px'}}>
                        <span className="text-muted">Precio: </span><strong>{combustible ? dinero(precio) + '/L' : '-'}</strong>
                    </div>
                    <div className="d-flex gap-2 mt-2">
                        <div className="btn-group btn-group-sm">
                            <button className={`btn ${c.modo === 'litros' ? 'btn-primary' : 'btn-outline-primary'}`}
                                    onClick={() => setControl(manguera.id, {modo: 'litros', error: ''})}>Litros</button>
                            <button className={`btn ${c.modo === 'pesos' ? 'btn-primary' : 'btn-outline-primary'}`}
                                    onClick={() => setControl(manguera.id, {modo: 'pesos', error: ''})}>Pesos</button>
                        </div>
                        <input type="number" step="0.001" className="form-control form-control-sm"
                               value={c.valor}
                               disabled={hayCarga || bloqueado}
                               onChange={(e) => setControl(manguera.id, {valor: e.target.value, error: ''})}
                               placeholder="0"/>
                    </div>
                    <div className="d-flex justify-content-between mt-2" style={estiloLCD}>
                        <span>L: <strong>{c.simulando ? Number(c.litros).toFixed(3) : litrosPreview.toFixed(3)}</strong></span>
                        <span>$ <strong>{dinero(c.simulando ? c.total : importePreview).slice(1)}</strong></span>
                    </div>
                    {c.simulando && (
                        <div className="progress mt-2" style={{height: '10px'}}>
                            <div className="progress-bar progress-bar-striped progress-bar-animated bg-success"
                                 style={{width: `${c.progreso}%`}}/>
                        </div>
                    )}
                    {c.error && <div className="alert alert-danger py-1 px-2 mt-2 mb-0" style={{fontSize: '12px'}}>{c.error}</div>}
                    <button className={`btn btn-sm mt-auto ${c.simulando ? 'btn-danger' : 'btn-success'}`}
                            disabled={bloqueado || (hayCarga && !c.simulando)}
                            onClick={() => c.simulando
                                ? detenerCarga(manguera.id)
                                : iniciarCarga(manguera, dispensario)}>
                        {bloqueado ? '🔒 Corte hecho' : (c.simulando ? '⏹ Detener' : '⛽ Iniciar Carga')}
                    </button>
                </div>
            </div>
        );
    };
    if (cargandoD || cargandoC || cargandoT) return <EstadoCarga cargando={true}/>;

    if (turnoLoaded && !turno) {
        return (
            <div>
                <PageHeader titulo="Punto de Venta" subtitulo="Simulador de despacho por manguera" mostrarAccion={false}/>
                <CardBase titulo="No hay turno abierto">
                    <p>Para poder vender es necesario <strong>abrir un turno</strong> y asignar el despachador de cada dispensario.</p>
                    <button className="btn btn-primary" onClick={() => navigate('/ventas/turnos')}>+ Abrir Turno</button>
                </CardBase>
            </div>
        );
    }

    return (
        <div>
            <PageHeader titulo="Punto de Venta" subtitulo="Simulador de despacho por manguera" mostrarAccion={false}/>

            {turno && (
                <div className="card mb-2" style={{padding: '8px 12px'}}>
                    <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
                        <div>
                            <strong>🕧 {turno.codigoTurno}</strong> — {turno.nombre}
                            <span className="badge badge-success ms-2">ABIERTO</span>
                            <small className="text-muted ms-2">
                                Ventas: <strong>{resumen.numero}</strong> · Litros: <strong>{resumen.litros.toFixed(2)}</strong> · Total: <strong>{dinero(resumen.total)}</strong>
                            </small>
                        </div>
                        <button className="btn btn-warning btn-sm" onClick={cerrarTurno}>🔒 Cerrar Turno</button>
                    </div>
                </div>
            )}

            {mangueras.length === 0 ? (
                <CardBase titulo="Sin mangueras configurables">
                    <p>Registra un dispensario con combustibles en el catálogo antes de vender.</p>
                    <button className="btn btn-primary" onClick={() => navigate('/ventas/dispensarios')}>Ir a Dispensarios</button>
                </CardBase>
            ) : (
                <div className="row g-3">
                    {dispensarios.filter(d => d.activo && (d.caras || []).some(cara =>
                        (cara.mangueras || []).some(m => m.activo && m.combustibleId))).map(d => {
                        return (
                            <div key={d.id} className="col-12">
                                <div className="card" style={{border: 'none', boxShadow: '0 2px 10px rgba(0,0,0,0.12)'}}>
                                    <div className="card-header d-flex justify-content-between align-items-center flex-wrap gap-2"
                                         style={{background: 'linear-gradient(135deg, #0f172a 0%, #1f3a5f 100%)', color: '#fff'}}>
                                        <div>
                                            <strong>⛽ Dispensario {d.numero} — {d.nombre}</strong>
                                            <span className="badge bg-secondary ms-2">{d.ubicacion || 'Isla sin asignar'}</span>
                                            {dispensarioConCorte(d.id) && (
                                                <span className="badge bg-danger ms-2">🔒 Corte hecho</span>
                                            )}
                                        </div>
                                        <div>
                                            <small>Despachador: <strong>{d.despachadorNombre || 'SIN ASIGNAR'}</strong></small>
                                        </div>
                                    </div>
                                    <div className="card-body" style={{background: '#eef1f7'}}>
                                        <div className="d-flex gap-3 flex-nowrap" style={{overflowX: 'auto'}}>
                                            {(d.caras || []).flatMap(cara =>
                                                (cara.mangueras || [])
                                                    .filter(m => m.activo && m.combustibleId)
                                                    .map(m => renderManguera(m, d, cara.codigo || cara.nombre)))}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {ultimaVenta && (
                <CardBase titulo="Venta registrada" style={{padding: '1rem'}}>
                    <TicketVenta venta={ultimaVenta} despachadorNombre={ultimaVenta.despachadorNombre} puntos={puntosUltima}/>
                    <div className="d-flex justify-content-center gap-2 mt-3">
                        <button className="btn btn-primary" onClick={() => window.print()}>Imprimir Ticket</button>
                        <button className="btn btn-outline-primary" onClick={() => { setUltimaVenta(null); setPuntosUltima(null); }}>Cerrar</button>
                    </div>
                </CardBase>
            )}
        </div>
    );
};
export default PuntoVenta;