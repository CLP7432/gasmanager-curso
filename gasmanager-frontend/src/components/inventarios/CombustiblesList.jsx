import React, {useState} from "react";
import {campoCurrency, campoSelect, campoTextarea, campoTexto} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {usePermisos} from "../../kernel/hooks/usePermisos.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import {combustiblesService} from "../../api/inventarios/auth.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import {estadoCol, columna, idCol} from "../../kernel/helpers/columnas.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";

const combustibleInicial = {
    tipo: '',
    nombre: '',
    descripcion: '',
    precioActual: ''
};

const camposCombustible = [
    campoSelect('tipo', 'Tipo de Combustible', [
        {value: '', label: 'Seleccione...'},
        {value: 'MAGNA', label: 'Magna'},
        {value: 'PREMIUM', label: 'Premium'},
        {value: 'DIESEL', label: 'Diésel'}
    ], {required: true}),
    campoTexto('nombre', 'Nombre', {required: true}),
    campoTextarea('descripcion', 'Descripción'),
    campoCurrency('precioActual', 'Precio Venta', {required: true})
];

const CombustiblesList = () => {

    const {isAdmin} = useAuth();
    const {puede} = usePermisos();
    const {datos: combustibles, loading, cargar} = useLista(combustiblesService, 'listar');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(combustiblesService, combustibleInicial, 'Combustible guardado', cargar);

    const [mostrarFormPrecio, setMostrarFormPrecio] = useState(false);
    const [combustibleSel, setCombustibleSel] = useState(null);
    const [nuevoPrecio, setNuevoPrecio] = useState('');
    const [motivo, setMotivo] = useState('');
    const [historial, setHistorial] = useState(null);

    const handleCambiarPrecio = (combustible) => {
        setCombustibleSel(combustible);
        setNuevoPrecio(combustible.precioActual);
        setMotivo('');
        setMostrarFormPrecio(true);
    };
    const handleGuardarPrecio = async () => {
        try {
            await combustiblesService.cambiarPrecio(combustibleSel.id, {
                nuevoPrecio: parseFloat(nuevoPrecio),
                motivo
            });
            setMostrarFormPrecio(false);
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al cambiar el precio');
        }
    };

    const handleVerHistorial = async (combustible) => {
        try {
            const datos = await combustiblesService.listarHistorial(combustible.id);
            setHistorial(datos);
            setCombustibleSel(combustible);
        } catch (error) {
            alert('Error al cargar el historial');
        }
    };

    const handleToggleActivo = async (combustible) => {
        if (window.confirm('¿Estás seguro de cambiar el estado de este combistible?')) {
            try {
                await combustiblesService.toggleActivo(combustible.id);
                cargar();
            } catch (error) {
                alert('Error al cambiar el estado');
            }
        }
    };
    const columnas = [
        idCol,
        columna('tipo', 'Tipo'),
        columna('nombre', 'Nombre'),
        columna('precioCompra', 'Compra', (c) => c.precioCompra != null ? `$${Number(c.precioCompra).toFixed(2)}` : '—'),
        columna('precioActual', 'Venta', (c) => `$${Number(c.precioActual).toFixed(2)}`),
        columna('margen', 'Margen/L', (c) => c.precioCompra != null
            ? <strong style={{color: '#198754'}}>${(Number(c.precioActual) - Number(c.precioCompra)).toFixed(2)}</strong>
            : '—'),
        columna('fechaUltimoCambioPrecio', 'Último Cambio'),
        estadoCol((c) => c.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Combustibles"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Combustible"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Combustible"
                    editando={editandoId}
                    campos={camposCombustible}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                />
            )}
            {mostrarFormPrecio && (
                <div className="card" style={{marginBottom: '20px', padding: '20px', border: '2px solid #fd7e14'}}>
                    <h4>Cambiar Precio — {combustibleSel?.nombre} ({combustibleSel?.tipo})</h4>
                    <p>Precio actual: ${combustibleSel?.precioActual}</p>
                    <div style={{marginBottom: '10px'}}>
                        <label>Nuevo precio:</label>
                        <input type="number" step="0.01" value={nuevoPrecio}
                               onChange={(e) => setNuevoPrecio(e.target.value)}
                               style={{marginLeft: '10px', padding: '5px'}}/>
                    </div>
                    <div style={{marginBottom: '10px'}}>
                        <label>Motivo del cambio:</label>
                        <input type="text" value={motivo}
                               onChange={(e) => setMotivo(e.target.value)}
                               placeholder="Ej: Ajuste por inflación..."
                               style={{marginLeft: '10px', padding: '5px', width: '400px'}}/>
                    </div>
                    <div className="d-flex gap-2 mt-2" style={{maxWidth: '340px'}}>
                        <button className="btn btn-sm btn-primary flex-fill" style={{padding: '4px 8px', fontSize: '12px'}} onClick={handleGuardarPrecio}>
                            Guardar Nuevo Precio
                        </button>
                        <button
                            className="btn btn-sm btn-secondary flex-fill"
                            onClick={() => setMostrarFormPrecio(false)}
                            style={{padding: '4px 8px', fontSize: '12px'}}
                        >
                            Cancelar
                        </button>
                    </div>
                </div>
            )}{historial && (
            <div className="card" style={{marginBottom: '20px', padding: '20px', border: '2px solid #0d6efd'}}>
                <h4>Historial de Precios — {combustibleSel?.nombre} ({combustibleSel?.tipo})</h4>
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
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={combustibles}
                    acciones={(combustible) => (
                        <Acciones
                            fila={combustible}
                            onEditar={isAdmin ? handleEditar : undefined}
                            onToggle={isAdmin ? handleToggleActivo : undefined}
                            etiquetaToggle={combustible.activo ? 'Desactivar' : 'Activar'}
                            onExtra={puede('CAMBIAR_PRECIO') ? () => handleCambiarPrecio(combustible) : undefined}
                            etiquetaExtra="Cambiar Precio"
                            onEliminar={puede('CAMBIAR_PRECIO') ? () => handleVerHistorial(combustible) : undefined}
                            etiquetaEliminar="Ver Historial"
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}

export default CombustiblesList;