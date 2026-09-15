import {useEffect, useRef, useState} from "react";
import {comprasService} from "../../api/inventarios/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import CardBase from "../../kernel/components/CardBase.jsx";

const PIPA_LITROS = 30000;
const formatoLitros = (v) => Number(v).toLocaleString('es-MX', {maximumFractionDigits: 2});

const TanquesCargas = () => {
    const {datos: pendientes, loading, cargar} = useLista(comprasService, 'pendientesCarga');
    const [compraId, setCompraId] = useState('');
    const [progreso, setProgreso] = useState(0);
    const [simulando, setSimulando] = useState(false);
    const [resultado, setResultado] = useState(null);
    const [error, setError] = useState('');
    const timer = useRef(null);

    const porCompra = {};
    (pendientes || []).forEach(p => {
        if (!porCompra[p.compraId]) {
            porCompra[p.compraId] = {
                compraId: p.compraId,
                folioFactura: p.folioFactura,
                fechaFactura: p.fechaFactura,
                items: []
            };
        }
        porCompra[p.compraId].items.push(p);
    });
    const compras = Object.values(porCompra);
    const compraIdEfectivo = compraId || (compras.length > 0 ? String(compras[0].compraId) : '');
    const compraSel = compras.find(c => String(c.compraId) === String(compraIdEfectivo)) || null;
    const siguiente = compraSel
        ? compraSel.items.find(i => i.litrosPendientes > 0 && i.tanqueId) || null
        : null;
    const litrosPipa = siguiente ? Math.min(PIPA_LITROS, siguiente.litrosPendientes) : 0;

    useEffect(() => () => clearInterval(timer.current), []);

    const comenzar = () => {
        setError('');
        setResultado(null);
        if (!siguiente) {
            const hayPendientes = compraSel && compraSel.items.some(i => i.litrosPendientes > 0);
            setError(hayPendientes
                ? 'Hay productos pendientes pero no tienen un tanque activo asociado. Crea o activa el tanque correcto.'
                : 'No hay productos pendientes de descarga en esta compra.');
            return;
        }
        setSimulando(true);
        setProgreso(0);
        const pasos = 40;
        const inc = 100 / pasos;
        let actual = 0;
        clearInterval(timer.current);
        timer.current = setInterval(() => {
            actual += inc;
            if (actual >= 100) {
                actual = 100;
                clearInterval(timer.current);
                ejecutar();
            }
            setProgreso(actual);
        }, 60);
    };

    const ejecutar = async () => {
        try {
            const resp = await comprasService.descargarPipa({detalleId: siguiente.detalleId, litros: litrosPipa});
            setResultado(resp);
            await cargar();
        } catch (e) {
            setError(e?.response?.data?.message || 'No se pudo realizar la descarga');
        }
        setSimulando(false);
        setProgreso(0);
    };

    if (loading) return <EstadoCarga cargando={true}/>;

    return (
        <div>
            <PageHeader titulo="Carga de Tanques - Simulador de Pipa" mostrarAccion={false}/>

            <CardBase titulo="Llegada de la pipa" style={{maxWidth: '860px', padding: '1rem'}}>
                <div className="d-flex gap-3 flex-wrap align-items-end">
                    <div style={{flex: 1, minWidth: '260px'}}>
                        <label className="form-label">Compra (cotejo con lo facturado)</label>
                        <select className="form-select" value={compraIdEfectivo} onChange={(e) => {
                            setCompraId(e.target.value);
                            setResultado(null);
                            setError('');
                        }}>
                            <option value="">Seleccionar...</option>
                            {compras.map(c => (
                                <option key={c.compraId} value={c.compraId}>
                                    {c.folioFactura} — {c.fechaFactura} ({c.items.filter(i => i.litrosPendientes > 0).length} producto(s) pendiente(s))
                                </option>
                            ))}
                        </select>
                        {compras.length === 0 && (
                            <small className="text-muted d-block mt-1">
                                No hay compras de combustible pendientes de descargar.
                            </small>
                        )}
                    </div>
                    {siguiente && (
                        <div>
                            <label className="form-label">Litros a descargar (autom&aacute;ticos)</label>
                            <div className="form-control" style={{background: '#f8f9fa'}}>
                                {formatoLitros(litrosPipa)} L de {siguiente.combustibleNombre}
                            </div>
                        </div>
                    )}
                </div>
                <div className="mt-3">
                    <button className="btn btn-success" onClick={comenzar} disabled={simulando || !siguiente}>
                        {simulando ? 'Descargando...' : '🚛 Simular llegada de pipa'}
                    </button>
                    {simulando && <button className="btn btn-danger ms-2" onClick={() => { clearInterval(timer.current); setSimulando(false); setProgreso(0); }}>Detener</button>}
                </div>
                {error && <div className="alert alert-danger py-2 mt-3 mb-0">{error}</div>}
            </CardBase>

            {compraSel && (
                <CardBase titulo={`Productos de la compra ${compraSel.folioFactura}`} style={{maxWidth: '860px', padding: '1rem'}}>
                    <table className="table table-sm align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Producto</th>
                            <th className="text-end">Comprados (L)</th>
                            <th className="text-end">Descargados (L)</th>
                            <th className="text-end">Pendientes (L)</th>
                            <th>Tanque (auto)</th>
                            <th className="text-end">Espacio disponible</th>
                            <th>Estado</th>
                        </tr>
                        </thead>
                        <tbody>
                        {compraSel.items.map(i => {
                            const completo = i.litrosPendientes <= 0;
                            return (
                                <tr key={i.detalleId}>
                                    <td><strong>{i.combustibleNombre}</strong> <span className="badge bg-secondary ms-1">{i.tipoCombustible}</span></td>
                                    <td className="text-end">{formatoLitros(i.litrosComprados)}</td>
                                    <td className="text-end">{formatoLitros(i.litrosDescargados)}</td>
                                    <td className="text-end"><strong>{formatoLitros(i.litrosPendientes)}</strong></td>
                                    <td>{i.tanqueNombre || <span className="text-danger">Sin tanque</span>}</td>
                                    <td className="text-end">{i.tanqueEspacioDisponible != null ? formatoLitros(i.tanqueEspacioDisponible) : '—'}</td>
                                    <td>
                                        {completo
                                            ? <span className="badge bg-secondary">Completo</span>
                                            : i.tanqueId
                                                ? <span className="badge bg-success">Listo para descargar</span>
                                                : <span className="badge bg-danger">Sin tanque</span>}
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                </CardBase>
            )}

            {simulando && (
                <CardBase titulo="Descarga de la pipa" style={{maxWidth: '860px', padding: '1rem'}}>
                    <div className="d-flex justify-content-between mb-1">
                        <span>Progreso</span>
                        <strong>{progreso.toFixed(0)}%</strong>
                    </div>
                    <div className="progress" style={{height: '14px'}}>
                        <div className="progress-bar progress-bar-striped progress-bar-animated bg-info" style={{width: `${progreso}%`}}/>
                    </div>
                </CardBase>
            )}

            {resultado && (
                <CardBase titulo="Descarga completada" style={{maxWidth: '860px', padding: '1rem', textAlign: 'center'}}>
                    <p className="mb-1">
                        <strong>{resultado.combustibleNombre}</strong> descargado en{' '}
                        <strong>{resultado.tanqueNombre}</strong> (+{formatoLitros(litrosPipa)} L)
                    </p>
                    <p className="mb-1">
                        {resultado.combustibleNombre}: <strong>{formatoLitros(resultado.litrosDescargados)} L</strong> descargados de {formatoLitros(resultado.litrosComprados)} L
                    </p>
                    <p className="mb-1">
                        Pendiente: <strong>{formatoLitros(resultado.litrosPendientes)} L</strong>
                    </p>
                    <button className="btn btn-outline-primary" onClick={() => { setResultado(null); cargar(); }}>Siguiente pipa</button>
                </CardBase>
            )}
        </div>
    );
};

export default TanquesCargas;