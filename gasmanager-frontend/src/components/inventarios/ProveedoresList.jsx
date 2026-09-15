import React from "react";
import {campoTexto, campoSelect, campoEmail} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {proveedoresService} from "../../api/inventarios/auth.js";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const proveedorInicial = {
    rfc: '',
    razonSocial: '',
    nombreComercial: '',
    contacto: '',
    telefono: '',
    email: '',
    tipoProveedor: 'ACEITES'
};
const camposProveedor = [
    campoTexto('rfc', 'RFC', {required: true}),
    campoTexto('razonSocial', 'Razón Social', {required: true}),
    campoTexto('nombreComercial', 'Nombre Comercial'),
    campoSelect('tipoProveedor', 'Tipo de Proveedor', [
        {value: 'COMBUSTIBLE', label: 'Combustibles (PEMEX)'},
        {value: 'ACEITES', label: 'Aceites y Aditivos'},
        {value: 'VARIOS', label: 'Varios'}
    ], {required: true}),
    campoTexto('contacto', 'Contacto'),
    campoTexto('telefono', 'Teléfono'),
    campoEmail('email', 'Email')
];

const ProveedoresList = () => {
    const {isAdmin} = useAuth();
    const {datos: proveedores, loading, cargar} = useLista(proveedoresService, 'listar');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(proveedoresService, proveedorInicial, 'Proveedor guardado', cargar);

    const handleToggle = async (proveedor) => {
        if (window.confirm('¿Estás seguro de cambiar el estado de este proveedor?')) {
            try {
                await proveedoresService.toggleActivo(proveedor.id);
                cargar();
            } catch (error) {
                alert('Error al cambiar el estado del proveedor');
            }
        }
    };

    const handleEliminar = async (proveedor) => {
        if (window.confirm('¿Estás seguro de eliminar este proveedor?')) {
            try {
                await proveedoresService.toggleActivo(proveedor.id);
                cargar();
            } catch (error) {
                alert('Error al eliminar el proveedor');
            }
        }
    };

    const columnas = [
        idCol,
        columna('rfc', 'RFC'),
        columna('razonSocial', 'Razón Social'),
        columna('nombreComercial', 'Comercial'),
        columna('tipoProveedor', 'Tipo'),
        columna('contacto', 'Contacto'),
        columna('telefono', 'Teléfono'),
        estadoCol((p) => p.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Proveedores"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Proveedor"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Proveedor"
                    editando={editandoId}
                    campos={camposProveedor}
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
                    datos={proveedores}
                    acciones={(proveedor) => (
                        <Acciones
                            fila={proveedor}
                            onEditar={handleEditar}
                            onToggle={handleToggle}
                            etiquetaToggle={proveedor.activo ? 'Desactivar' : 'Activar'}
                            onEliminar={handleEliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default ProveedoresList;