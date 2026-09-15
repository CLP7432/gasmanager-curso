import React, {useEffect, useState} from "react";
import {combustiblesService} from "../../api/inventarios/auth.js";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {usePermisos} from "../../kernel/hooks/usePermisos.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const CambiosPrecioCombustiblesLista = () => {
    const {isAdmin} = useAuth();
    const {puede} = usePermisos();
    const puedeCambiar = isAdmin || puede('CAMBIAR_PRECIO');
    const [combustibles, setCombustibles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [valores, setValores] = useState({});
    const [motivos, setMotivos] = useState({});
    const [historial, setHistorial] = useState(null);
    const [historialSel, setHistorialSel] = useState(null);
    const [buscando, setBuscando] = useState('');

    const cargar = async () => {
        setLoading(true);
        try {
            const lista = await combustiblesService.listarActivos().catch(() => []);
            setCombustibles(lista);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { cargar(); }, []);

    const handlePrecio = (id, valor) => setValores(prev => ({...prev, [id]: valor}));
    const handleMotivo = (id, valor) => setMotivos(prev => ({...prev, [id]: valor}));

    const handleGuardar = async (id) => {
        const nuevoPrecio = Number(valores[id]);
        if (isNaN(nuevoPrecio) || nuevoPrecio < 0) { alert('Precio inválido'); return; }
        try {
            await combustiblesService.cambiarPrecio(id, {nuevoPrecio, motivo: motivos[id] || ''});
            alert('Precio actualizado');
            setValores(prev => ({...prev, [id]: undefined}));
            setMotivos(prev => ({...prev, [id]: ''}));
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al cambiar el precio');
        }
    };

    const handleVerHistorial = async (combustible) => {
        try {
            const datos = await combustiblesService.listarHistorial(combustible.id);
            setHistorialSel(combustible);
            setHistorial(datos);
        } catch (error) {
            alert('Error al cargar el historial');
        }
    };

    const filtrados = combustibles.filter(c =>
        !buscando ||
        (c.nombre || '').toLowerCase().includes(buscando.toLowerCase()) ||
        (c.tipo || '').toLowerCase().includes(buscando.toLowerCase())
    );

    return (
        <div>
            <PageHeader
                titulo="Cambios de Precios de Combustibles"
                subtitulo={puedeCambiar
                    ? "Actualiza el precio de venta del catálogo; el punto de venta toma el precio vigente"
                    : "Solo el encargado puede modificar los precios de combustibles"}
                mostrarAccion={false}
            />

            {!puedeCambiar && (
                <div className="alert alert-warning">No tienes permisos para modificar precios.</div>
            )}

            <div className="mb-3" style={{maxWidth: '320px'}}>
                <input className="form-control" placeholder="Buscar combustible..." value={buscando}
                       onChange={(e) => setBuscando(e.target.value)}/>
            </div>

            <EstadoCarga cargando={loading}>
                <table className="table table-bordered">
                    <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Tipo</th>
                        <th className="text-end">Compra (PEMEX)</th>
                        <th className="text-end">Venta actual</th>
                        <th className="text-end">Margen/L</th>
                        <th style={{width: '180px'}}>Nuevo precio (MXN/L)</th>
                        <th>Motivo del cambio</th>
                        <th style={{width: '100px'}}></th>
                    </tr>
                    </thead>
                    <tbody>
                    {filtrados.map(c => (
                        <tr key={c.id}>
                            <td>{c.nombre}</td>
                            <td>{c.tipo}</td>
                            <td className="text-end">{c.precioCompra != null ? dinero(c.precioCompra) : '—'}</td>
                            <td className="text-end">{dinero(c.precioActual)}</td>
                            <td className="text-end">{c.precioCompra != null
                                ? <strong style={{color: '#198754'}}>{dinero(Number(c.precioActual) - Number(c.precioCompra))}</strong>
                                : '—'}</td>
                            <td>
                                <input type="number" step="0.01" min="0" className="form-control form-control-sm"
                                       value={valores[c.id] ?? ''}
                                       disabled={!puedeCambiar}
                                       placeholder="0.00"
                                       onChange={(e) => handlePrecio(c.id, e.target.value)}/>
                            </td>
                            <td>
                                <input type="text" className="form-control form-control-sm"
                                       value={motivos[c.id] ?? ''}
                                       disabled={!puedeCambiar}
                                       placeholder="Ej: Ajuste por inflación..."
                                       onChange={(e) => handleMotivo(c.id, e.target.value)}/>
                            </td>
                            <td className="text-end">
                                <div className="d-flex gap-2 justify-content-end">
                                    <button className="btn btn-sm btn-primary" disabled={!puedeCambiar}
                                            onClick={() => handleGuardar(c.id)}>Guardar</button>
                                    <button className="btn btn-sm btn-secondary"
                                            onClick={() => handleVerHistorial(c)}>Historial</button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    {filtrados.length === 0 && (
                        <tr><td colSpan="6" className="text-muted">Sin combustibles activos</td></tr>
                    )}
                    </tbody>
                </table>
            </EstadoCarga>

            {historial && (
                <div className="card" style={{marginBottom: '20px', padding: '20px', border: '2px solid #0d6efd'}}>
                    <h4>Historial de Precios — {historialSel?.nombre} ({historialSel?.tipo})</h4>
                    <button className="btn btn-sm btn-secondary" onClick={() => setHistorial(null)}
                            style={{marginBottom: '10px'}}>Cerrar
                    </button>
                    {historial.length === 0 ? (
                        <p>No hay cambios de precio registrados.</p>
                    ) : (
                        <table className="table table-sm">
                            <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Precio Anterior</th>
                                <th>Precio Nuevo</th>
                                <th>Motivo</th>
                                <th>Cambiado por</th>
                            </tr>
                            </thead>
                            <tbody>
                            {historial.map((h, i) => (
                                <tr key={i}>
                                    <td>{h.fechaCambio}</td>
                                    <td>${h.precioAnterior}</td>
                                    <td>${h.precioNuevo}</td>
                                    <td>{h.motivoCambio || '—'}</td>
                                    <td>{h.cambiadoPor} (ID: {h.cambiadoPorId})</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    )}
                </div>
            )}
        </div>
    );
};

export default CambiosPrecioCombustiblesLista;