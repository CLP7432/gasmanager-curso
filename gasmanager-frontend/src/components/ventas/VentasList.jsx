import {useState} from "react";
import {ventasService, turnosService} from "../../api/ventas/auth.js";
import {columna} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import CardBase from "../../kernel/components/CardBase.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const estilosEstado = {
    COMPLETADA: {clase: 'badge bg-success', texto: 'COMPLETADA'},
    CANCELADA: {clase: 'badge bg-danger', texto: 'CANCELADA'},
    PENDIENTE: {clase: 'badge bg-warning text-dark', texto: 'PENDIENTE'},
    FACTURADA: {clase: 'badge bg-info', texto: 'FACTURADA'}
};

const VentasList = () => {
    const {datos: turnos, loading: cargandoTurnos} = useLista(turnosService, 'listar');
    const [turnoId, setTurnoId] = useState('');
    const [ventas, setVentas] = useState([]);
    const [cargandoVentas, setCargandoVentas] = useState(false);
    const [error, setError] = useState('');
    const [detalle, setDetalle] = useState(null);

    const turnoSel = turnos.find(t => String(t.id) === String(turnoId)) || null;

    const cargarVentasTurno = async (id) => {
        setError('');
        setDetalle(null);
        setTurnoId(id);
        if (!id) {
            setVentas([]);
            return;
        }
        setCargandoVentas(true);
        try {
            setVentas(await ventasService.listarPorTurno(id));
        } catch {
            setError('No se pudieron cargar las ventas del turno');
            setVentas([]);
        }
        setCargandoVentas(false);
    };

    const cancelarVenta = async (venta) => {
        if (!window.confirm(`¿Cancelar la venta ${venta.folio}?`)) return;
        try {
            await ventasService.cancelar(venta.id);
            cargarVentasTurno(turnoId);
        } catch {
            alert('No se pudo cancelar la venta');
        }
    };

    const columnas = [
        columna('folio', 'Folio'),
        columna('fechaHora', 'Fecha'),
        columna('metodoPago', 'Pago'),
        columna('usuarioId', 'Despacho', (v) => v.usuarioId ?? '-'),
        columna('estado', 'Estado', (v) => {
            const e = estilosEstado[v.estado] || {clase: 'badge bg-secondary', texto: v.estado};
            return <span className={e.clase}>{e.texto}</span>;
        }),
        columna('total', 'Total', (v) => <strong style={{color: '#198754'}}>{dinero(v.total)}</strong>)
    ];

    return (
        <div>
            <PageHeader titulo="Historial de Ventas" mostrarAccion={false}/>

            <CardBase titulo="Ventas por turno" style={{maxWidth: '760px', padding: '1rem', marginBottom: '15px'}}>
                <label className="form-label">Selecciona el turno</label>
                <select className="form-select" value={turnoId} onChange={(e) => cargarVentasTurno(e.target.value)}>
                    <option value="">Seleccionar turno...</option>
                    {turnos.map(t => (
                        <option key={t.id} value={t.id}>
                            {t.codigoTurno} — {t.nombre || 'Sin nombre'} ({t.fechaTurno} · {t.horaInicio})
                        </option>
                    ))}
                </select>
                {turnoSel && (
                    <small className="text-muted d-block mt-2">
                        Turno <strong>{turnoSel.codigoTurno}</strong> · Estado: <strong>{turnoSel.estado}</strong> · Supervisor: {turnoSel.supervisorNombre || '-'}
                    </small>
                )}
                {error && <div className="alert alert-danger py-2 mt-3 mb-0">{error}</div>}
            </CardBase>

            {detalle && (
                <CardBase titulo={`Venta ${detalle.folio} — ${detalle.fechaHora}`} style={{maxWidth: '760px', padding: '1rem', marginBottom: '15px'}}>
                    {detalle.detalles.map((d, i) => (
                        <div key={i} className="d-flex gap-2" style={{borderBottom: '1px solid #f1f3f5', padding: '3px 0'}}>
                            <span style={{flex: 2}}>{d.productoNombre} ({d.tipoProducto})</span>
                            <span style={{flex: 1}}>x{d.cantidad}</span>
                            <span style={{flex: 1}}>${d.precioUnitario}</span>
                            <span style={{flex: 1, textAlign: 'right'}}>${d.subtotal}</span>
                        </div>
                    ))}
                    <div className="d-flex justify-content-end gap-4" style={{marginTop: '8px'}}>
                        <span>Subtotal: ${detalle.subtotal}</span>
                        <span>IVA: ${detalle.iva}</span>
                        <span><strong>Total: ${detalle.total}</strong></span>
                    </div>
                    <div className="d-flex justify-content-end mt-2">
                        <button className="btn btn-sm btn-secondary" onClick={() => setDetalle(null)}>Cerrar</button>
                    </div>
                </CardBase>
            )}

            <EstadoCarga cargando={cargandoTurnos || cargandoVentas}>
                {!turnoId ? (
                    <CardBase titulo="Selecciona un turno">
                        <p className="mb-0">Elige un turno arriba para ver sus ventas.</p>
                    </CardBase>
                ) : (
                    <TablaDinamica
                        columnas={columnas}
                        datos={ventas}
                        acciones={(venta) => (
                            <Acciones
                                fila={venta}
                                onExtra={() => setDetalle(venta)}
                                etiquetaExtra="Detalle"
                                onEliminar={venta.estado === 'CANCELADA' ? undefined : cancelarVenta}
                                etiquetaEliminar="Cancelar"
                                habilitadoEliminar={venta.estado !== 'CANCELADA'}
                            />
                        )}
                    />
                )}
            </EstadoCarga>
        </div>
    );
}
export default VentasList;