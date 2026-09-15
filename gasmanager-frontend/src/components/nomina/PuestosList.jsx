import React from "react";
import {campoTexto, campoCurrency, campoSelect, campoTextarea} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {puestosService} from "../../api/nomina/auth.js";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const dinero = (v) => `$${Number(v || 0).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const puestoInicial = {
    nombre: '',
    descripcion: '',
    salarioBase: '',
    riesgoPuesto: ''
};

const PuestosList = () => {

    const {isAdmin} = useAuth();
    const {datos: puestos, loading, cargar} = useLista(puestosService, 'listar');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(puestosService, puestoInicial, 'Puesto guardado', cargar);

    const camposPuesto = [
        campoTexto('nombre', 'Nombre', {required: true}),
        campoSelect('riesgoPuesto', 'Riesgo', [
            {value: '', label: 'Seleccione...'},
            {value: 'BAJO', label: 'Bajo'},
            {value: 'MEDIO', label: 'Medio'},
            {value: 'ALTO', label: 'Alto'}
        ]),
        campoCurrency('salarioBase', 'Salario Base'),
        campoTextarea('descripcion', 'Descripción', {colsClase: 'col-md-6', rows: 3})
    ];

    const handleToggleActivo = async (puesto) => {
        if (window.confirm('¿Estás seguro de cambiar el estado de este puesto?')) {
            try {
                await puestosService.toggleActivo(puesto.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al cambiar el estado del puesto');
            }
        }
    };

    const handleEliminar = async (puesto) => {
        if (window.confirm('¿Estás seguro de eliminar este puesto?')) {
            try {
                await puestosService.eliminar(puesto.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'No se puede eliminar el puesto');
            }
        }
    };

    const columnas = [
        idCol,
        columna('nombre', 'Nombre'),
        columna('salarioBase', 'Salario Base', (p) => dinero(p.salarioBase)),
        columna('salarioDiario', 'Salario Diario', (p) => dinero(p.salarioDiario)),
        columna('riesgoPuesto', 'Riesgo'),
        estadoCol((p) => p.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Puestos"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Puesto"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Puesto"
                    editando={editandoId}
                    campos={camposPuesto}
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
                    datos={puestos}
                    acciones={(puesto) => (
                        <Acciones
                            fila={puesto}
                            onEditar={handleEditar}
                            onToggle={handleToggleActivo}
                            etiquetaToggle={puesto.activo ? 'Desactivar' : 'Activar'}
                            onEliminar={handleEliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default PuestosList;