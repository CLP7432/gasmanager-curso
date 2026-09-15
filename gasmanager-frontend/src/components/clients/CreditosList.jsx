import React from "react";
import {useAuth} from "../../contexts/AuthContext.jsx";
import {useNavigate} from "react-router-dom";
import {clientesService, creditosService} from "../../api/clients/auth.js";
import {useLista} from "../../kernel/hooks/useLista.js";
import {useFormulario} from "../../kernel/hooks/useFormulario.js";
import FormularioBase from "../../kernel/components/FormularioBase.jsx";
import TablaDinamica from "../../kernel/components/TablaDinamica.jsx";
import PageHeader from "../../kernel/components/PageHeader.jsx";
import EstadoCarga from "../../kernel/components/EstadoCarga.jsx";
import Acciones from "../../kernel/components/Acciones.jsx";
import {idCol, columna} from "../../kernel/helpers/columnas.jsx";
import {campoNumero, campoFecha, campoSelect, campoTextarea} from "../../kernel/helpers/campos.jsx";

const creditoInicial = {
    clienteId: '',
    montoTotal: '',
    plazoMeses: '',
    tasaInteres: '',
    montoInteres: '',
    fechaInicio: '',
    fechaVencimiento: '',
    metodoPago: 'MENSUAL',
    diaPago: '',
    notas: ''
};
const camposCredito = (clientes) => [
    campoSelect('clienteId', 'Cliente', clientes.map(c => ({
        value: c.id, label: c.razonSocial || c.nombre || c.nombreComercial
    })), {required: true}),
    campoNumero('montoTotal', 'Monto Total', {required: true, step: '0.01'}),
    campoNumero('plazoMeses', 'Plazo (meses)', {required: true}),
    campoNumero('tasaInteres', 'Tasa de Interés (%)', {step: '0.01'}),
    campoNumero('montoInteres', 'Monto de Interés', {step: '0.01', readOnly: true}),
    campoFecha('fechaInicio', 'Fecha de Inicio', {required: true}),
    campoFecha('fechaVencimiento', 'Fecha de Vencimiento', {required: true}),
    campoSelect('metodoPago', 'Frecuencia de Pago', [
        {value: 'SEMANAL', label: 'Semanal'},
        {value: 'QUINCENAL', label: 'Quincenal'},
        {value: 'MENSUAL', label: 'Mensual'},
        {value: 'PERSONALIZADO', label: 'Personalizado'}
    ], {required: true}),
    campoNumero('diaPago', 'Día de Pago', {min: 1, max: 31}),
    campoTextarea('notas', 'Notas')
];

const CreditosList = () => {
    const {isAdmin} = useAuth();
    const navigate = useNavigate();
    const {datos: creditos, loading, cargar} = useLista(creditosService, 'listarTodos');
    const {datos: clientes} = useLista(clientesService, 'listarActivos');

    const {mostrarForm, editandoId, objeto, setObjeto, handleNuevo, handleEditar, handleChange, handleSubmit, cerrar} =
        useFormulario(creditosService, creditoInicial, 'Crédito guardado', cargar);

    const handleChangeCredito = (e) => {
        handleChange(e);
        const {name, value} = e.target;
        if (name === 'montoTotal' || name === 'tasaInteres') {
            setObjeto(prev => {
                const monto = parseFloat(name === 'montoTotal' ? value : prev.montoTotal) || 0;
                const tasa = parseFloat(name === 'tasaInteres' ? value : prev.tasaInteres) || 0;
                return {...prev, montoInteres: monto * (tasa / 100)};
            });
        }
    };

    const handleCancelar = async (credito) => {
        const motivo = window.prompt('Motivo de cancelación (opcional):');
        if (motivo == null) return;
        if (window.confirm('¿Estás seguro de cancelar este crédito?')) {
            try {
                await creditosService.cancelar(credito.id, motivo || '');
                cargar();
            } catch (error) {
                alert(error.response?.data?.message || 'Error al cancelar crédito');
            }
        }
    };

    const getEstadoBadge = (estado) => {
        const colores = {
            ACTIVO: 'badge badge-success',
            PAGADO: 'badge badge-primary',
            VENCIDO: 'badge badge-danger',
            CANCELADO: 'badge badge-warning',
            EN_COBRANZA: 'badge badge-danger'
        };
        return <span className={colores[estado] || 'badge badge-secondary'}>{estado}</span>;
    };

    const columnas = [
        idCol,
        columna('folioCredito', 'Folio'),
        columna('clienteNombre', 'Cliente'),
        columna('montoTotal', 'Monto Total'),
        columna('saldoPendiente', 'Saldo'),
        columna('fechaVencimiento', 'Vencimiento'),
        columna('estado', 'Estado', (c) => getEstadoBadge(c.estado))
    ];

    return (
        <div>
            <PageHeader
                titulo="Gestión de Créditos"
                mostrarAccion={isAdmin}
                etiquetaAccion="+ Nuevo Crédito"
                onAccion={handleNuevo}
            />
            {mostrarForm && isAdmin && (
                <FormularioBase
                    titulo="Crédito"
                    editando={editandoId}
                    campos={camposCredito(clientes)}
                    objeto={objeto}
                    handleChange={handleChangeCredito}
                    handleSubmit={handleSubmit}
                    cerrar={cerrar}
                />
            )}
            <EstadoCarga cargando={loading}>
                <TablaDinamica
                    columnas={columnas}
                    datos={creditos}
                    acciones={(credito) => (
                        <Acciones
                            fila={credito}
                            onExtra={() => navigate(`/clientes/creditos/${credito.id}`)}
                            etiquetaExtra="Abonos"
                            onEditar={handleEditar}
                            onEliminar={handleCancelar}
                            etiquetaEliminar="Cancelar"
                        />
                    )}
                />
            </EstadoCarga>
        </div>
    );
};

export default CreditosList;