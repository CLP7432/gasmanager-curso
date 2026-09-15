import React from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {permisoService} from "../../api/admin/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import {idCol, estadoCol, columna} from "../../kernel/helpers/columnas.jsx";
import {campoTexto} from "../../kernel/helpers/campos.jsx";

const permisoInicial = {
    codigoPermiso: '',
    nombrePermiso: '',
    descripcion: ''
};
const camposPermiso = [
    campoTexto('codigoPermiso', 'Código del Permiso', {required: true}),
    campoTexto('nombrePermiso', 'Nombre', {required: true}),
    campoTexto('descripcion', 'Descripción')
];

const PermisosLista = () => {
    const {isAdmin} = useAuth();
    const {datos: permisos, loading, cargar} = useLista(permisoService, 'listar');

    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(permisoService, permisoInicial, 'Permiso guardado', cargar);

    const handleEliminar = async (permiso) => {
        if (window.confirm('¿Estás seguro de eliminar este permiso?')) {
            try {
                await permisoService.eliminar(permiso.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al eliminar permiso');
            }
        }
    };

    const columnas = [
        idCol,
        columna('codigoPermiso', 'Código', (p) => <code>{p.codigoPermiso}</code>),
        columna('nombrePermiso', 'Nombre'),
        columna('descripcion', 'Descripción'),
        estadoCol((p) => p.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Permisos"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Permiso"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Permiso"
                    editando={editandoId}
                    campos={camposPermiso}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={permisos}
                    acciones={(permiso) => (
                        <Acciones
                            fila={permiso}
                            onEditar={handleEditar}
                            onEliminar={handleEliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
};

export default PermisosLista;