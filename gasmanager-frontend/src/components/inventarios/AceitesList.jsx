import React from "react";
import {campoTexto, campoSelect, campoCurrency, campoNumero, campoTextarea} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {aceitesService} from "../../api/inventarios/auth.js";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const aceiteInicial = {
    codigo: '',
    nombre: '',
    descripcion: '',
    marca: '',
    tipoAceite: '',
    presentacion: '',
    unidadesPorCaja: 12,
    precioCompra: '',
    precioVenta: '',
    ubicacion: ''
};
const camposAceite = [
    campoTexto('nombre', 'Nombre', {required: true}),
    campoTexto('marca', 'Marca'),
    campoSelect('tipoAceite', 'Tipo de Aceite', [
        {value: '', label: 'Seleccione...'},
        {value: 'MOTOR', label: 'Motor'},
        {value: 'TRANSMISION', label: 'Transmisión'},
        {value: 'HIDRAULICO', label: 'Hidráulico'},
        {value: 'FRENOS', label: 'Frenos'},
        {value: 'ADITIVO', label: 'Aditivo'},
        {value: 'AGUA', label: 'Agua'}
    ]),
    campoSelect('presentacion', 'Presentación', [
        {value: '', label: 'Seleccione...'},
        {value: '1 L', label: '1 Litro'},
        {value: '500 ml', label: '500 ml'},
        {value: '946 ml', label: '946 ml (1 cuarto)'},
        {value: '473 ml', label: '473 ml (16 oz)'},
        {value: '250 ml', label: '250 ml'},
        {value: '20 L', label: 'Garrafa 20 L'}
    ]),
    campoNumero('unidadesPorCaja', 'Unidades por Caja', {required: true, min: 1}),
    campoCurrency('precioCompra', 'Precio de Compra'),
    campoCurrency('precioVenta', 'Precio de Venta'),
    campoTexto('ubicacion', 'Ubicación'),
    campoTextarea('descripcion', 'Descripción', {colsClase: 'col-md-6', rows: 3})
];

const AceitesList = () => {

    const {isAdmin} = useAuth();
    const {datos: aceites, loading, cargar} = useLista(aceitesService, 'listar');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(aceitesService, aceiteInicial, 'Aceite guardado', cargar);

    const handleToggleActivo = async (aceite) => {
        if (window.confirm('¿Estás seguro de cambiar el estado de este aceite?')) {
            try {
                await aceitesService.toggleActivo(aceite.id);
                cargar();
            } catch (error) {
                alert('Error al cambiar el estado del aceite');
            }
        }
    };

    const handleEliminar = async (aceite) => {
        if (window.confirm('¿Estás seguro de eliminar  este aceite?')) {
            try {
                await aceitesService.eliminar(aceite.id);
                cargar();
            } catch (error) {
                alert('Error al eliminar el aceite');
            }
        }
    };

    const columnas = [
        idCol,
        columna('codigo', 'Código'),
        columna('nombre', 'Nombre'),
        columna('marca', 'Marca'),
        columna('tipoAceite', 'Tipo'),
        columna('presentacion', 'Presentación'),
        columna('precioVenta', 'Precio Venta'),
        columna('margen', 'Margen', (c) => (c.precioCompra != null && c.precioVenta != null)
            ? <strong style={{color: '#198754'}}>${(Number(c.precioVenta) - Number(c.precioCompra)).toFixed(2)}</strong>
            : '—'),
        columna('stockActual', 'Stock'),
        estadoCol((a) => a.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Aceites"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Aceite"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Aceite"
                    editando={editandoId}
                    campos={camposAceite}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                    compacto
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={aceites}
                    acciones={(aceite) => (
                        <Acciones
                            fila={aceite}
                            onEditar={handleEditar}
                            onToggle={handleToggleActivo}
                            etiquetaToggle={aceite.activo ? 'Desactivar' : 'Activar'}
                            onEliminar={handleEliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default AceitesList;