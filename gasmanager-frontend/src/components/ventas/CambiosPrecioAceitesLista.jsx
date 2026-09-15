import React, {useEffect, useState} from "react";
import {aceitesService} from "../../api/inventarios/auth.js";
import {useAuth} from "../../contexts/AuthContext.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";

const dinero = (v) => `$${Number(v).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const CambiosPrecioAceitesLista = () => {
    const {isAdmin} = useAuth();
    const [aceites, setAceites] = useState([]);
    const [loading, setLoading] = useState(true);
    const [valores, setValores] = useState({});
    const [buscando, setBuscando] = useState('');

    const cargar = async () => {
        setLoading(true);
        try {
            const lista = await aceitesService.listarActivos().catch(() => []);
            setAceites(lista);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { cargar(); }, []);

    const handlePrecio = (id, valor) => setValores(prev => ({...prev, [id]: valor}));

    const handleGuardar = async (id) => {
        const precio = Number(valores[id]);
        if (isNaN(precio) || precio < 0) { alert('Precio de venta inválido'); return; }
        try {
            await aceitesService.actualizarPrecio(id, precio);
            alert('Precio actualizado');
            setValores(prev => ({...prev, [id]: undefined}));
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al actualizar el precio');
        }
    };

    const filtrados = aceites.filter(a =>
        !buscando || (a.nombre || '').toLowerCase().includes(buscando.toLowerCase())
    );

    return (
        <div>
            <PageHeader
                titulo="Cambios de Precios de Aceites"
                subtitulo={isAdmin
                    ? "Actualiza el precio de venta del catálogo; los cortes toman el precio vigente"
                    : "Solo el encargado puede modificar los precios de aceites"}
                mostrarAccion={false}
            />

            {!isAdmin && (
                <div className="alert alert-warning">No tienes permisos para modificar precios.</div>
            )}

            <div className="mb-3" style={{maxWidth: '320px'}}>
                <input className="form-control" placeholder="Buscar aceite..." value={buscando}
                       onChange={(e) => setBuscando(e.target.value)}/>
            </div>

            <EstadoCarga cargando={loading}>
                <table className="table table-bordered">
                    <thead>
                    <tr>
                        <th>Producto</th>
                        <th>Categoría</th>
                        <th className="text-end">Presentación</th>
                        <th className="text-end">Precio actual</th>
                        <th style={{width: '260px'}}>Nuevo precio (encargado)</th>
                    </tr>
                    </thead>
                    <tbody>
                    {filtrados.map(a => (
                        <tr key={a.id}>
                            <td>{a.nombre}</td>
                            <td>{a.tipoAceite || '-'}</td>
                            <td className="text-end">{a.presentacion || '-'}</td>
                            <td className="text-end">{dinero(a.precioVenta)}</td>
                            <td>
                                <div className="d-flex gap-2">
                                    <input type="number" step="0.01" min="0" className="form-control form-control-sm"
                                           value={valores[a.id] ?? ''}
                                           disabled={!isAdmin}
                                           placeholder="0.00"
                                           onChange={(e) => handlePrecio(a.id, e.target.value)}/>
                                    <button className="btn btn-sm btn-primary" disabled={!isAdmin}
                                            onClick={() => handleGuardar(a.id)}>Guardar</button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    {filtrados.length === 0 && (
                        <tr><td colSpan="5" className="text-muted">Sin aceites activos</td></tr>
                    )}
                    </tbody>
                </table>
            </EstadoCarga>
        </div>
    );
};

export default CambiosPrecioAceitesLista;
