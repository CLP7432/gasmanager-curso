import React from "react";
import {campoFecha, campoNumero, campoCurrency, campoSelect, campoTexto, campoTextarea} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {incidenciasService, empleadosService} from "../../api/nomina/auth.js";
import {idCol, columna} from "../../kernel/helpers/columnas.jsx";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const dinero = (v) => `$${Number(v || 0).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const incidenciaInicial = {
    empleadoId: '',
    tipo: '',
    fecha: '',
    cantidad: '',
    monto: '',
    observaciones: '',
    autorizadoPor: ''
};

const tiposIncidencia = [
    {value: '', label: 'Seleccione...'},
    {value: 'FALTA', label: 'Falta'},
    {value: 'RETARDO', label: 'Retardo (horas)'},
    {value: 'HORA_EXTRA_DOBLE', label: 'Hora extra doble'},
    {value: 'HORA_EXTRA_TRIPLE', label: 'Hora extra triple'},
    {value: 'BONO', label: 'Bono (monto)'},
    {value: 'PERMISO_CON_GOCE', label: 'Permiso con goce'},
    {value: 'PERMISO_SIN_GOCE', label: 'Permiso sin goce (días)'},
    {value: 'VACACION', label: 'Vacaciones'},
    {value: 'AGUINALDO', label: 'Aguinaldo'},
    {value: 'PRIMA_VACACIONAL', label: 'Prima vacacional'},
    {value: 'FALTANTE', label: 'Faltante de caja (monto)'},
    {value: 'SOBRANTE', label: 'Sobrante de caja (monto)'}
];

const IncidenciasList = () => {

    const {isAdmin} = useAuth();
    const {datos: incidencias, loading, cargar} = useLista(incidenciasService, 'listar');
    const {datos: empleados} = useLista(empleadosService, 'listarActivos');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(incidenciasService, incidenciaInicial, 'Incidencia guardada', cargar);

    const camposIncidencia = [
        campoSelect('empleadoId', 'Empleado',
            empleados.map(e => ({value: e.id, label: e.nombreCompleto})),
            {required: true}),
        campoSelect('tipo', 'Tipo de Incidencia', tiposIncidencia, {required: true}),
        campoFecha('fecha', 'Fecha', {required: true}),
        campoNumero('cantidad', 'Cantidad (días/horas)'),
        campoCurrency('monto', 'Monto'),
        campoTexto('autorizadoPor', 'Autorizado por'),
        campoTextarea('observaciones', 'Observaciones', {colsClase: 'col-md-6', rows: 3})
    ];

    const handleEliminar = async (incidencia) => {
        if (window.confirm('¿Estás seguro de eliminar esta incidencia?')) {
            try {
                await incidenciasService.eliminar(incidencia.id);
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al eliminar la incidencia');
            }
        }
    };

    const columnas = [
        idCol,
        columna('empleadoNombre', 'Empleado'),
        columna('tipo', 'Tipo'),
        columna('fecha', 'Fecha'),
        columna('cantidad', 'Cantidad'),
        columna('monto', 'Monto', (i) => dinero(i.monto))
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Incidencias"
                subtitulo="Faltantes/sobrantes llegan solos de cortes; aquí se registran faltas, enfermedades y bajas"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nueva Incidencia"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Incidencia"
                    editando={editandoId}
                    campos={camposIncidencia}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={incidencias}
                    acciones={(incidencia) => (
                        <Acciones
                            fila={incidencia}
                            onEditar={handleEditar}
                            onEliminar={handleEliminar}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default IncidenciasList;