import React from "react";
import {campoTexto, campoEmail, campoFecha, campoCurrency, campoSelect} from "../../kernel/helpers/campos.jsx";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useLista} from "../../kernel/hooks/useLista.js";
import {empleadosService, puestosService, departamentosService} from "../../api/nomina/auth.js";
import {idCol, columna, estadoCol} from "../../kernel/helpers/columnas.jsx";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";

const dinero = (v) => `$${Number(v || 0).toLocaleString('es-MX', {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

const empleadoInicial = {
    nombre: '',
    apellidoPaterno: '',
    apellidoMaterno: '',
    rfc: '',
    curp: '',
    nss: '',
    email: '',
    telefono: '',
    celular: '',
    fechaNacimiento: '',
    fechaIngreso: '',
    tipoContrato: '',
    tipoJornada: '',
    salarioDiario: '',
    puestoId: '',
    departamentoId: '',
    numeroCuenta: '',
    banco: ''
};

const EmpleadosList = () => {

    const {isAdmin} = useAuth();
    const {datos: empleados, loading, cargar} = useLista(empleadosService, 'listar');
    const {datos: puestos} = useLista(puestosService, 'listarActivos');
    const {datos: departamentos} = useLista(departamentosService, 'listarActivos');
    const {mostrarForm, editandoId, objeto, handleNuevo, handleEditar, handleChange, cerrar} =
        useFormulario(empleadosService, empleadoInicial, 'Empleado guardado', cargar);

    // Los selects vacíos viajan como "" y rompen los Long del backend: normalizar
    const guardarEmpleado = async (e) => {
        e.preventDefault();
        const payload = {
            ...objeto,
            puestoId: objeto.puestoId ? Number(objeto.puestoId) : null,
            departamentoId: objeto.departamentoId ? Number(objeto.departamentoId) : null,
            salarioDiario: objeto.salarioDiario === '' ? null : Number(objeto.salarioDiario),
            tipoContrato: objeto.tipoContrato || null,
            tipoJornada: objeto.tipoJornada || null
        };
        try {
            if (editandoId) await empleadosService.actualizar(editandoId, payload);
            else await empleadosService.crear(payload);
            alert('Empleado guardado');
            cerrar();
            cargar();
        } catch (error) {
            const data = error.response?.data;
            alert(typeof data === 'string' ? data : (data?.message || 'Error al guardar el empleado'));
        }
    };

    const camposEmpleado = [
        campoTexto('nombre', 'Nombre', {required: true}),
        campoTexto('apellidoPaterno', 'Apellido Paterno', {required: true}),
        campoTexto('apellidoMaterno', 'Apellido Materno'),
        campoSelect('tipoContrato', 'Contrato', [
            {value: '', label: 'Seleccione...'},
            {value: 'INDEFINIDO', label: 'Indefinido'},
            {value: 'TEMPORAL', label: 'Temporal'},
            {value: 'POR_TIEMPO_DETERMINADO', label: 'Por tiempo determinado'},
            {value: 'PRACTICAS', label: 'Prácticas'}
        ]),
        campoSelect('tipoJornada', 'Jornada', [
            {value: '', label: 'Seleccione...'},
            {value: 'DIURNA', label: 'Diurna'},
            {value: 'NOCTURNA', label: 'Nocturna'},
            {value: 'MIXTA', label: 'Mixta'},
            {value: 'TURNO_ROTATIVO', label: 'Turno rotativo'}
        ]),
        campoSelect('puestoId', 'Puesto',
            puestos.map(p => ({value: p.id, label: p.nombre})),
            {required: true}),
        campoSelect('departamentoId', 'Departamento',
            departamentos.map(d => ({value: d.id, label: d.nombre})),
            {required: true}),
        campoCurrency('salarioDiario', 'Salario Diario', {required: true}),
        campoFecha('fechaIngreso', 'Fecha de Ingreso', {required: true}),
        campoFecha('fechaNacimiento', 'Fecha de Nacimiento'),
        campoEmail('email', 'Email'),
        campoTexto('telefono', 'Teléfono'),
        campoTexto('celular', 'Celular'),
        campoTexto('rfc', 'RFC'),
        campoTexto('curp', 'CURP'),
        campoTexto('nss', 'NSS'),
        campoTexto('numeroCuenta', 'Nº de Cuenta'),
        campoTexto('banco', 'Banco')
    ];

    const handleToggleActivo = async (empleado) => {
        const accion = empleado.activo
            ? (window.confirm('¿Desactivar este empleado?'))
            : (window.confirm('¿Reactivar este empleado?'));
        if (!accion) return;
        try {
            if (empleado.activo) {
                await empleadosService.desactivar(empleado.id);
            } else {
                await empleadosService.reactivar(empleado.id);
            }
            cargar();
        } catch (error) {
            alert(error.response?.data?.message || 'Error al cambiar el estado del empleado');
        }
    };

    const columnas = [
        idCol,
        columna('codigoEmpleado', 'Código'),
        columna('nombreCompleto', 'Nombre'),
        columna('puestoNombre', 'Puesto'),
        columna('departamentoNombre', 'Departamento'),
        columna('tipoContrato', 'Contrato'),
        columna('salarioDiario', 'Salario Diario', (e) => dinero(e.salarioDiario)),
        estadoCol((e) => e.activo)
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Empleados"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Empleado"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Empleado"
                    editando={editandoId}
                    campos={camposEmpleado}
                    objeto={objeto}
                    handleChange={handleChange}
                    handleSubmit={guardarEmpleado}
                    cerrar={cerrar}
                    compacto
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={empleados}
                    acciones={(empleado) => (
                        <Acciones
                            fila={empleado}
                            onEditar={handleEditar}
                            onToggle={handleToggleActivo}
                            etiquetaToggle={empleado.activo ? 'Desactivar' : 'Reactivar'}
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
}
export default EmpleadosList;