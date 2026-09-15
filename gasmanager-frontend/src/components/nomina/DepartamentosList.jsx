import React from "react";
import {campoTexto, campoTextarea} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {departamentosService} from "../../api/nomina/auth.js";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const departamentoInicial = {
    nombre: '',
    descripcion: ''
};

const DepartamentosList = () => {

    const {isAdmin} = useAuth();
    const {datos: departamentos, loading, cargar} = useLista(departamentosService, 'listar');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(departamentosService, departamentoInicial, 'Departamento guardado', cargar);

    const camposDepartamento = [
        campoTexto('nombre', 'Nombre', {required: true}),
        campoTextarea('descripcion', 'Descripción', {colsClase: 'col-md-6', rows: 3})
    ];

    const handleToggleActivo = async (departamento) => {
        if (window.confirm('¿Estás seguro de cambiar el estado de este departamento?')) {
            try {
                await departamentosService.toggleActivo(departamento.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al cambiar el estado del departamento');
            }
        }
    };

    const handleEliminar = async (departamento) => {
        if (window.confirm('¿Estás seguro de eliminar este departamento?')) {
            try {
                await departamentosService.eliminar(departamento.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'No se puede eliminar el departamento');
            }
        }
    };

    const columnas = [
        idCol,
        columna('nombre', 'Nombre'),
        columna('descripcion', 'Descripción'),
        estadoCol((d) => d.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Departamentos"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Departamento"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Departamento"
                    editando={editandoId}
                    campos={camposDepartamento}
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
                    datos={departamentos}
                    acciones={(departamento) => (
                        <Acciones
                            fila={departamento}
                            onEditar={handleEditar}
                            onToggle={handleToggleActivo}
                            etiquetaToggle={departamento.activo ? 'Desactivar' : 'Activar'}
                            onEliminar={handleEliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default DepartamentosList;